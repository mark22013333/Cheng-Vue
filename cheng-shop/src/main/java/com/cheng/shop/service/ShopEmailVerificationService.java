package com.cheng.shop.service;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopEmailVerification;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.enums.MemberStatus;
import com.cheng.shop.mapper.ShopEmailVerificationMapper;
import com.cheng.shop.security.MemberTokenService;
import com.cheng.shop.security.ShopMemberLoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/**
 * Email 驗證服務
 * <p>
 * 採用 selector + hashed token 設計（與 {@link ShopPasswordResetService} 一致）：
 * <ul>
 *   <li>selector：用於 DB 查詢（索引），公開出現在 URL 中</li>
 *   <li>validator：高熵度隨機 token，僅以 SHA-256 雜湊存入 DB</li>
 *   <li>驗證時使用常數時間比對，防止時間側信道攻擊</li>
 * </ul>
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopEmailVerificationService {

    private final ShopEmailVerificationMapper verificationMapper;
    private final IShopMemberService memberService;
    private final ShopMailService mailService;
    private final ShopConfigService configService;
    private final MemberTokenService memberTokenService;
    private final RedisCache redisCache;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** selector 長度（bytes），Base64URL 編碼後約 27 字元 */
    private static final int SELECTOR_LENGTH = 20;

    /** validator 長度（bytes），256-bit 熵 */
    private static final int VALIDATOR_LENGTH = 32;

    // ==================== 公開方法 ====================

    /**
     * 發送 Email 驗證信
     * <p>
     * 產生 selector + validator → 存 DB（hashed validator）→ 寄送驗證連結 email
     *
     * @param email     收件人 Email
     * @param memberId  會員 ID
     * @param ipAddress 請求 IP
     * @param userAgent 請求 User-Agent
     */
    public void requestEmailVerification(String email, Long memberId, String ipAddress, String userAgent) {
        // 1. 使同一會員的舊 token 全部失效
        verificationMapper.invalidateByMemberId(memberId);

        // 2. 產生 selector + validator
        byte[] selectorBytes = new byte[SELECTOR_LENGTH];
        byte[] validatorBytes = new byte[VALIDATOR_LENGTH];
        SECURE_RANDOM.nextBytes(selectorBytes);
        SECURE_RANDOM.nextBytes(validatorBytes);

        String selector = Base64.getUrlEncoder().withoutPadding().encodeToString(selectorBytes);
        String validator = Base64.getUrlEncoder().withoutPadding().encodeToString(validatorBytes);
        String hashedToken = sha256Hex(validator);

        // 3. 計算過期時間
        int expireMinutes = configService.getInt(ShopConfigKey.EMAIL_VERIFY_TOKEN_EXPIRE);
        Date expiresAt = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L);

        // 4. 存入 DB
        ShopEmailVerification record = new ShopEmailVerification();
        record.setSelector(selector);
        record.setHashedToken(hashedToken);
        record.setEmail(email);
        record.setMemberId(memberId);
        record.setExpiresAt(expiresAt);
        record.setUsed(false);
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.setCreatedAt(new Date());
        verificationMapper.insert(record);

        // 5. 寄送驗證連結 email
        String frontendBaseUrl = configService.getString(ShopConfigKey.PAYMENT_FRONTEND_URL);
        String verifyUrl = frontendBaseUrl + "/verify-email?selector=" + selector + "&token=" + validator;
        int expireHours = expireMinutes / 60;
        mailService.sendEmailVerification(email, verifyUrl, expireHours);

        log.info("Email 驗證連結已產生：email={}, selector={}, 有效期={}分鐘, IP={}",
                email, selector, expireMinutes, ipAddress);
    }

    /**
     * 驗證 Email
     * <p>
     * 驗證成功後：
     * <ul>
     *   <li>更新會員：email_verified = true, status = ACTIVE</li>
     *   <li>標記 token 為已使用</li>
     *   <li>產生 JWT token 供前端自動登入</li>
     *   <li>寄送歡迎信</li>
     * </ul>
     *
     * @param selector  選擇器
     * @param validator 驗證器（原始值）
     * @return 驗證結果（含 JWT token 和會員資料）
     */
    public VerifyResult verifyEmail(String selector, String validator) {
        if (StringUtils.isEmpty(selector) || StringUtils.isEmpty(validator)) {
            throw new ServiceException("驗證連結無效");
        }

        // 1. DB 查 selector
        ShopEmailVerification record = verificationMapper.selectBySelector(selector);
        if (record == null) {
            throw new ServiceException("驗證連結無效或已過期");
        }

        // 2. 檢查是否已使用
        if (Boolean.TRUE.equals(record.getUsed())) {
            throw new ServiceException("此驗證連結已使用");
        }

        // 3. 檢查是否已過期
        if (record.getExpiresAt().before(new Date())) {
            throw new ServiceException("驗證連結已過期，請重新申請");
        }

        // 4. 常數時間比對 hashed token
        String hashedValidator = sha256Hex(validator);
        if (!MessageDigest.isEqual(
                hashedValidator.getBytes(StandardCharsets.UTF_8),
                record.getHashedToken().getBytes(StandardCharsets.UTF_8))) {
            log.warn("Email 驗證 token 比對失敗：selector={}", selector);
            throw new ServiceException("驗證連結無效");
        }

        // 5. 查詢會員
        ShopMember member = memberService.selectMemberById(record.getMemberId());
        if (member == null) {
            throw new ServiceException("會員資料異常，請聯繫客服");
        }

        // 6. 若已驗證，不重複處理
        if (Boolean.TRUE.equals(member.getEmailVerified())) {
            // 標記 token 已使用，但仍讓使用者登入
            verificationMapper.markAsUsed(record.getId(), new Date());
            ShopMemberLoginUser loginUser = new ShopMemberLoginUser(member);
            String jwtToken = memberTokenService.createToken(loginUser);
            return new VerifyResult(jwtToken, member);
        }

        // 7. 更新會員：email_verified = true + status = ACTIVE
        ShopMember updateMember = new ShopMember();
        updateMember.setMemberId(member.getMemberId());
        updateMember.setEmailVerified(true);
        updateMember.setStatus(MemberStatus.ACTIVE.getCode());
        memberService.updateMember(updateMember);

        // 更新本地物件以反映最新狀態
        member.setEmailVerified(true);
        member.setStatus(MemberStatus.ACTIVE.getCode());

        // 8. 標記 token 已使用
        verificationMapper.markAsUsed(record.getId(), new Date());

        // 9. 產生 JWT token（讓前端自動登入）
        ShopMemberLoginUser loginUser = new ShopMemberLoginUser(member);
        String jwtToken = memberTokenService.createToken(loginUser);

        // 10. 寄送歡迎信
        try {
            mailService.sendWelcomeEmail(record.getEmail(), member.getNickname());
        } catch (Exception e) {
            log.warn("寄送歡迎信失敗（不影響驗證流程）：{}", e.getMessage());
        }

        log.info("會員 {} (ID:{}) Email 驗證成功", record.getEmail(), member.getMemberId());

        return new VerifyResult(jwtToken, member);
    }

    // ==================== 內部類別 ====================

    /**
     * Email 驗證結果
     */
    public record VerifyResult(String token, ShopMember member) {
    }

    // ==================== 私有方法 ====================

    /**
     * 計算 SHA-256 雜湊（hex 格式）
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 演算法不可用", e);
        }
    }
}
