package com.cheng.shop.service;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.domain.ShopPasswordReset;
import com.cheng.shop.mapper.ShopPasswordResetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 密碼重設服務
 * <p>
 * 採用 selector + hashed token 設計：
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
public class ShopPasswordResetService {

    private final ShopPasswordResetMapper passwordResetMapper;
    private final IShopMemberService memberService;
    private final ShopMailService mailService;
    private final ShopConfigService configService;
    private final RedisCache redisCache;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** selector 長度（bytes），Base64URL 編碼後約 27 字元 */
    private static final int SELECTOR_LENGTH = 20;

    /** validator 長度（bytes），256-bit 熵 */
    private static final int VALIDATOR_LENGTH = 32;

    /** 會員密碼變更 Redis key TTL（天），等同 JWT 最長壽命 */
    private static final int PWD_CHANGED_TTL_DAYS = 7;

    /** 密碼最大長度（字元） */
    private static final int MAX_PASSWORD_LENGTH = 50;

    // ==================== 公開方法 ====================

    /**
     * 發起密碼重設請求
     * <p>
     * 產生 selector + validator → 存 DB（hashed validator）→ 寄送重設連結 email
     *
     * @param email     收件人 Email
     * @param ipAddress 請求 IP
     * @param userAgent 請求 User-Agent
     */
    public void requestPasswordReset(String email, String ipAddress, String userAgent) {
        // 1. 查詢會員（不存在則模擬延遲後靜默返回，防止帳號列舉）
        ShopMember member = memberService.selectMemberByEmail(email);
        if (member == null) {
            simulateProcessingDelay();
            log.debug("密碼重設請求：email={} 不存在，靜默返回", email);
            return;
        }

        // 2. 使同一 email 的舊 token 全部失效
        passwordResetMapper.invalidateByEmail(email);

        // 3. 產生 selector + validator
        byte[] selectorBytes = new byte[SELECTOR_LENGTH];
        byte[] validatorBytes = new byte[VALIDATOR_LENGTH];
        SECURE_RANDOM.nextBytes(selectorBytes);
        SECURE_RANDOM.nextBytes(validatorBytes);

        String selector = Base64.getUrlEncoder().withoutPadding().encodeToString(selectorBytes);
        String validator = Base64.getUrlEncoder().withoutPadding().encodeToString(validatorBytes);
        String hashedToken = sha256Hex(validator);

        // 4. 計算過期時間
        int expireMinutes = configService.getInt(ShopConfigKey.PWD_RESET_TOKEN_EXPIRE);
        Date expiresAt = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L);

        // 5. 存入 DB
        ShopPasswordReset resetRecord = new ShopPasswordReset();
        resetRecord.setSelector(selector);
        resetRecord.setHashedToken(hashedToken);
        resetRecord.setEmail(email);
        resetRecord.setMemberId(member.getMemberId());
        resetRecord.setExpiresAt(expiresAt);
        resetRecord.setUsed(false);
        resetRecord.setIpAddress(ipAddress);
        resetRecord.setUserAgent(userAgent);
        resetRecord.setCreatedAt(new Date());
        passwordResetMapper.insert(resetRecord);

        // 6. 寄送重設連結 email
        String frontendBaseUrl = configService.getString(ShopConfigKey.PAYMENT_FRONTEND_URL);
        String resetUrl = frontendBaseUrl + "/reset-password#selector=" + selector + "&token=" + validator;
        mailService.sendResetLink(email, resetUrl, expireMinutes);

        log.info("密碼重設連結已產生：email={}, selectorPrefix={}, 有效期={}分鐘, IP={}",
                maskEmail(email), selector.substring(0, Math.min(selector.length(), 8)), expireMinutes, ipAddress);
    }

    /**
     * 驗證重設 token 有效性（前端開啟重設密碼頁面時呼叫）
     *
     * @param selector  選擇器
     * @param validator 驗證器（原始值）
     * @return true=有效，false=無效或過期
     */
    public boolean validateResetToken(String selector, String validator) {
        if (StringUtils.isEmpty(selector) || StringUtils.isEmpty(validator)) {
            return false;
        }

        ShopPasswordReset resetRecord = passwordResetMapper.selectBySelector(selector);
        if (resetRecord == null) {
            return false;
        }
        if (Boolean.TRUE.equals(resetRecord.getUsed())) {
            return false;
        }
        if (resetRecord.getExpiresAt().before(new Date())) {
            return false;
        }

        // 常數時間比對 hashed token
        String hashedValidator = sha256Hex(validator);
        return MessageDigest.isEqual(
                hashedValidator.getBytes(StandardCharsets.UTF_8),
                resetRecord.getHashedToken().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 執行密碼重設
     *
     * @param selector    選擇器
     * @param validator   驗證器（原始值）
     * @param newPassword 新密碼（明文）
     * @param ipAddress   請求 IP
     * @param userAgent   請求 User-Agent
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String selector, String validator, String newPassword,
                              String ipAddress, String userAgent) {
        int minPasswordLength = getMinPasswordLength();
        if (StringUtils.isEmpty(newPassword) || newPassword.length() < minPasswordLength
                || newPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new ServiceException("密碼長度須為 " + minPasswordLength + "-" + MAX_PASSWORD_LENGTH + " 字元");
        }

        // 1. 透過 selector 查詢 DB
        ShopPasswordReset resetRecord = passwordResetMapper.selectBySelector(selector);
        if (resetRecord == null) {
            throw new ServiceException("重設連結無效或已過期，請重新操作");
        }

        // 2. 常數時間比對 hashed token
        String hashedValidator = sha256Hex(validator);
        // 3. 查詢會員
        ShopMember member = memberService.selectMemberByEmail(resetRecord.getEmail());
        if (member == null) {
            throw new ServiceException("會員資料異常，請聯繫客服");
        }

        // 4. 檢查新密碼不能與現有密碼相同
        if (SecurityUtils.matchesPassword(newPassword, member.getPassword())) {
            throw new ServiceException("新密碼不能與目前密碼相同");
        }

        // 5. 原子消耗 token（防重放）
        Date now = new Date();
        int consumed = passwordResetMapper.consumeToken(resetRecord.getId(), hashedValidator, now);
        if (consumed <= 0) {
            log.warn("密碼重設 token 消耗失敗：selectorPrefix={}, IP={}",
                    selector.substring(0, Math.min(selector.length(), 8)), ipAddress);
            throw new ServiceException("重設連結無效或已過期，請重新操作");
        }

        // 6. 更新密碼 + password_changed_at
        ShopMember updateMember = new ShopMember();
        updateMember.setMemberId(member.getMemberId());
        updateMember.setPassword(SecurityUtils.encryptPassword(newPassword));
        updateMember.setPasswordChangedAt(now);
        memberService.updateMember(updateMember);

        // 7. 寫入 Redis，供 JWT 失效檢查（TTL = token 最長壽命 7 天）
        String pwdChangedKey = CacheConstants.MEMBER_PWD_CHANGED_KEY + member.getMemberId();
        redisCache.setCacheObject(pwdChangedKey, now.getTime(), PWD_CHANGED_TTL_DAYS, TimeUnit.DAYS);

        // 8. 寄送密碼變更通知 email
        mailService.sendPasswordChangedNotification(resetRecord.getEmail(), ipAddress, userAgent);

        log.info("會員 {} (ID:{}) 密碼重設成功，IP: {}",
                maskEmail(resetRecord.getEmail()), member.getMemberId(), ipAddress);
    }

    /**
     * 密碼重設連結有效期（分鐘）
     */
    public int getTokenExpireMinutes() {
        return Math.max(configService.getInt(ShopConfigKey.PWD_RESET_TOKEN_EXPIRE), 1);
    }

    /**
     * 重發冷卻秒數
     */
    public int getResendCooldownSeconds() {
        return Math.max(configService.getInt(ShopConfigKey.PWD_RESET_RESEND_COOLDOWN_SECONDS), 1);
    }

    /**
     * 重設密碼最小長度
     */
    public int getMinPasswordLength() {
        return Math.max(configService.getInt(ShopConfigKey.PWD_RESET_MIN_LENGTH), 8);
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

    /**
     * 模擬處理延遲，防止時間側信道攻擊
     * <p>
     * email 不存在時呼叫，使回應時間與存在時接近
     */
    private void simulateProcessingDelay() {
        try {
            Thread.sleep(150 + SECURE_RANDOM.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 脫敏 email，避免 log 顯示完整地址
     */
    private String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.isEmpty()) {
            return "***@" + domain;
        }
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.substring(0, 1) + "***" + local.substring(local.length() - 1) + "@" + domain;
    }
}
