package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
import com.cheng.common.annotation.RateLimiter;
import com.cheng.common.enums.LimitType;
import com.cheng.common.constant.CacheConstants;
import com.cheng.common.constant.Constants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.exception.user.CaptchaException;
import com.cheng.common.exception.user.CaptchaExpireException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.enums.MemberStatus;
import com.cheng.shop.security.MemberTokenService;
import com.cheng.shop.security.ShopMemberLoginUser;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.service.IShopMemberService;
import com.cheng.shop.service.ShopEmailVerificationService;
import com.cheng.shop.service.ShopPasswordPolicyService;
import com.cheng.shop.service.ShopPasswordResetService;
import com.cheng.shop.utils.ShopMemberSecurityUtils;
import com.cheng.system.service.ISysConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.cheng.common.utils.ServletUtils.getRequest;

/**
 * 商城會員登入/註冊
 *
 * @author cheng
 */
@Slf4j
@Anonymous
@PublicApi
@RestController
@RequestMapping("/shop/auth")
@RequiredArgsConstructor
public class ShopMemberAuthController extends BaseController {

    private final IShopMemberService memberService;
    private final MemberTokenService memberTokenService;
    private final RedisCache redisCache;
    private final ISysConfigService configService;
    private final ShopPasswordResetService passwordResetService;
    private final ShopPasswordPolicyService passwordPolicyService;
    private final ShopEmailVerificationService emailVerificationService;
    private final ShopConfigService shopConfigService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody MemberLoginBody body) {
        ShopMember member = findMemberByAccount(body.getAccount());
        if (member == null) {
            throw new ServiceException("帳號或密碼錯誤");
        }

        // 密碼驗證
        if (StringUtils.isEmpty(member.getPassword())
                || !SecurityUtils.matchesPassword(body.getPassword(), member.getPassword())) {
            throw new ServiceException("帳號或密碼錯誤");
        }

        // Email 驗證檢查
        if (!Boolean.TRUE.equals(member.getEmailVerified())) {
            AjaxResult result = AjaxResult.error("您的 Email 尚未驗證，請先至信箱點擊驗證連結");
            result.put("needVerify", true);
            result.put("email", member.getEmail());
            return result;
        }

        // 狀態檢查
        MemberStatus status = member.getStatusEnum();
        if (status == null || !status.canLogin()) {
            throw new ServiceException("此帳號已停用或凍結");
        }

        memberService.updateLoginInfo(member.getMemberId(), IpUtils.getIpAddr());
        ShopMemberLoginUser loginUser = new ShopMemberLoginUser(member);
        String token = memberTokenService.createToken(loginUser);

        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        ajax.put("member", member);
        return ajax;
    }

    @PostMapping("/register")
    public AjaxResult register(@Valid @RequestBody MemberRegisterBody body) {
        validateCaptcha("register", body.getCode(), body.getUuid());

        // 動態密碼政策驗證
        passwordPolicyService.validate(body.getPassword());

        String email = body.getEmail().trim().toLowerCase();
        if (!isEmail(email)) {
            throw new ServiceException("Email 格式不正確");
        }

        // 暱稱：優先使用前端傳入值，未提供則取 Email @ 前綴
        String nickname = StringUtils.isNotEmpty(body.getNickname())
                ? body.getNickname().trim()
                : email.split("@")[0];

        ShopMember member = new ShopMember();
        member.setNickname(nickname);
        member.setEmail(email);
        member.setPassword(body.getPassword());

        if (!memberService.checkEmailUnique(member)) {
            throw new ServiceException("此 Email 已被註冊");
        }

        int result = memberService.registerMember(member);
        if (result <= 0) {
            throw new ServiceException("註冊失敗，請稍後再試");
        }

        // 寄送 Email 驗證信（不發 JWT，需驗證後才能登入）
        String ipAddress = IpUtils.getIpAddr();
        String userAgent = getRequest().getHeader("User-Agent");
        emailVerificationService.requestEmailVerification(email, member.getMemberId(), ipAddress, userAgent);

        return AjaxResult.success("註冊成功，驗證信已發送至您的信箱");
    }

    @PostMapping("/logout")
    public AjaxResult logout() {
        ShopMemberLoginUser loginUser = memberTokenService.getLoginUser(getRequest());
        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getToken())) {
            memberTokenService.delLoginUser(loginUser.getToken());
        }
        return success();
    }

    @GetMapping("/profile")
    public AjaxResult profile() {
        ShopMemberLoginUser loginUser = ShopMemberSecurityUtils.getLoginUser();
        return success(loginUser.getMember());
    }

    @PutMapping("/profile/avatar")
    public AjaxResult updateAvatar(@RequestBody AvatarBody body) {
        ShopMemberLoginUser loginUser = ShopMemberSecurityUtils.getLoginUser();
        ShopMember member = loginUser.getMember();

        // 更新頭像
        ShopMember updateMember = new ShopMember();
        updateMember.setMemberId(member.getMemberId());
        updateMember.setAvatar(body.getAvatar());

        int result = memberService.updateMember(updateMember);
        if (result > 0) {
            member.setAvatar(body.getAvatar());
            memberTokenService.refreshToken(loginUser);
            return success(member);
        }
        return error("更新失敗");
    }

    @PutMapping("/profile")
    public AjaxResult updateProfile(@RequestBody MemberProfileBody body) {
        ShopMemberLoginUser loginUser = ShopMemberSecurityUtils.getLoginUser();
        ShopMember member = loginUser.getMember();

        // 更新會員資料（空字串轉 null，避免唯一鍵衝突）
        ShopMember updateMember = new ShopMember();
        updateMember.setMemberId(member.getMemberId());
        updateMember.setNickname(body.getNickname());
        updateMember.setMobile(StringUtils.isEmpty(body.getMobile()) ? null : body.getMobile());
        updateMember.setEmail(StringUtils.isEmpty(body.getEmail()) ? null : body.getEmail());
        updateMember.setGender(body.getGender());

        // 檢查手機唯一性（如果有變更）
        if (StringUtils.isNotEmpty(body.getMobile()) && !body.getMobile().equals(member.getMobile())) {
            if (!memberService.checkMobileUnique(updateMember)) {
                throw new ServiceException("手機號碼已被使用");
            }
        }
        // 檢查 Email 唯一性（如果有變更）
        if (StringUtils.isNotEmpty(body.getEmail()) && !body.getEmail().equals(member.getEmail())) {
            if (!memberService.checkEmailUnique(updateMember)) {
                throw new ServiceException("Email 已被使用");
            }
        }

        int result = memberService.updateMember(updateMember);
        if (result > 0) {
            // 更新 LoginUser 中的會員資訊
            member.setNickname(body.getNickname());
            if (StringUtils.isNotEmpty(body.getMobile())) {
                member.setMobile(body.getMobile());
            }
            if (StringUtils.isNotEmpty(body.getEmail())) {
                member.setEmail(body.getEmail());
            }
            member.setGender(body.getGender());
            memberTokenService.refreshToken(loginUser);
            return success(member);
        }
        return error("更新失敗");
    }

    private ShopMember findMemberByAccount(String account) {
        if (StringUtils.isEmpty(account)) {
            return null;
        }
        if (isEmail(account)) {
            return memberService.selectMemberByEmail(account);
        }
        return memberService.selectMemberByMobile(account);
    }

    private boolean isEmail(String account) {
        return EMAIL_PATTERN.matcher(account).matches();
    }

    /**
     * 模擬處理延遲，防止時間側信道攻擊。
     * email 不存在時呼叫，使回應時間與存在時接近。
     */
    private void simulateProcessingDelay() {
        try {
            Thread.sleep(150 + SECURE_RANDOM.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 累加每日發送次數，TTL 設定到當天 23:59:59
     */
    private void incrementDailyCount(String dailyKey, Integer currentCount) {
        int newCount = (currentCount == null ? 0 : currentCount) + 1;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long ttl = Math.max(Duration.between(now, endOfDay).getSeconds(), 1);
        redisCache.setCacheObject(dailyKey, newCount, (int) ttl, TimeUnit.SECONDS);
    }

    private void validateCaptcha(String username, String code, String uuid) {
        if (!configService.selectCaptchaEnabled()) {
            return;
        }
        if (StringUtils.isEmpty(code)) {
            throw new CaptchaException();
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        redisCache.deleteObject(verifyKey);
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }

    // ========== Email 驗證 ==========

    /**
     * 驗證 Email（使用者點擊驗證連結後到達）
     */
    @GetMapping("/verify-email")
    public AjaxResult verifyEmail(
            @RequestParam String selector,
            @RequestParam String token) {
        ShopEmailVerificationService.VerifyResult result =
                emailVerificationService.verifyEmail(selector, token);

        AjaxResult ajax = AjaxResult.success("Email 驗證成功");
        ajax.put(Constants.TOKEN, result.token());
        ajax.put("member", result.member());
        return ajax;
    }

    /**
     * 重寄 Email 驗證信
     */
    @PostMapping("/resend-verification")
    @RateLimiter(time = 3600, count = 10, limitType = LimitType.IP)
    public AjaxResult resendVerification(@Valid @RequestBody ResendVerificationBody body) {
        // 圖形驗證碼校驗
        validateCaptcha("resend-verification", body.getCode(), body.getUuid());

        String email = body.getEmail().trim().toLowerCase();
        if (!isEmail(email)) {
            throw new ServiceException("Email 格式不正確");
        }

        // 頻率限制
        checkEmailVerifyRateLimit(email);

        // 統一回應訊息（防止帳號列舉攻擊）
        String safeMsg = "若此信箱已註冊且尚未驗證，驗證信將寄至您的信箱";

        // 查詢會員
        ShopMember member = memberService.selectMemberByEmail(email);
        if (member == null) {
            simulateProcessingDelay();
            return AjaxResult.success(safeMsg);
        }

        // 已驗證的不需重寄
        if (Boolean.TRUE.equals(member.getEmailVerified())) {
            simulateProcessingDelay();
            return AjaxResult.success(safeMsg);
        }

        String ipAddress = IpUtils.getIpAddr();
        String userAgent = getRequest().getHeader("User-Agent");
        emailVerificationService.requestEmailVerification(email, member.getMemberId(), ipAddress, userAgent);

        return AjaxResult.success(safeMsg);
    }

    /**
     * Email 驗證頻率限制檢查
     */
    private void checkEmailVerifyRateLimit(String email) {
        // 同一 email 60 秒內不可重複發送
        String limitKey = CacheConstants.EMAIL_VERIFY_LIMIT_KEY + email;
        if (redisCache.getCacheObject(limitKey) != null) {
            throw new ServiceException("驗證信已發送，請稍後再試");
        }

        // 每小時限制
        String hourlyKey = CacheConstants.EMAIL_VERIFY_HOURLY_KEY + email;
        Integer hourlyCount = redisCache.getCacheObject(hourlyKey);
        int maxHourly = shopConfigService.getInt(ShopConfigKey.EMAIL_VERIFY_EMAIL_HOURLY_LIMIT);
        if (hourlyCount != null && hourlyCount >= maxHourly) {
            throw new ServiceException("發送次數過多，請稍後再試");
        }

        // 每日限制
        String dailyKey = CacheConstants.EMAIL_VERIFY_DAILY_KEY + email;
        Integer dailyCount = redisCache.getCacheObject(dailyKey);
        int maxDaily = shopConfigService.getInt(ShopConfigKey.EMAIL_VERIFY_EMAIL_DAILY_LIMIT);
        if (dailyCount != null && dailyCount >= maxDaily) {
            throw new ServiceException("今日發送次數已達上限，請明天再試");
        }

        // 設定 60 秒頻率限制
        redisCache.setCacheObject(limitKey, "1", 60, TimeUnit.SECONDS);

        // 累加每小時計數
        int newHourly = (hourlyCount == null ? 0 : hourlyCount) + 1;
        redisCache.setCacheObject(hourlyKey, newHourly, 1, TimeUnit.HOURS);

        // 累加每日計數
        incrementDailyCount(dailyKey, dailyCount);
    }

    // ========== 忘記密碼（重設連結方式） ==========

    /**
     * 步驟一：發送密碼重設連結
     */
    @PostMapping("/forgot-password")
    @RateLimiter(time = 3600, count = 10, limitType = LimitType.IP)
    public AjaxResult forgotPassword(@Valid @RequestBody ForgotPasswordBody body) {
        // 圖形驗證碼校驗
        validateCaptcha("forgot-password", body.getCode(), body.getUuid());

        String email = body.getEmail().trim().toLowerCase();
        if (!isEmail(email)) {
            throw new ServiceException("Email 格式不正確");
        }

        // Email 頻率限制
        checkEmailRateLimit(email);

        // 統一回應訊息（防止帳號列舉攻擊）
        String safeMsg = "若此信箱已註冊，重設連結將寄至您的信箱";

        // 委託 service 處理（內部處理 email 不存在的情況）
        String ipAddress = IpUtils.getIpAddr();
        String userAgent = getRequest().getHeader("User-Agent");
        passwordResetService.requestPasswordReset(email, ipAddress, userAgent);

        return AjaxResult.success(safeMsg);
    }

    /**
     * 驗證重設連結有效性（前端開啟重設密碼頁面時呼叫）
     *
     * @deprecated 改用 POST /shop/auth/validate-reset-token，將於後續版本移除
     */
    @Deprecated(since = "2026-03")
    @GetMapping("/validate-reset-token")
    public AjaxResult validateResetToken(
            @RequestParam String selector,
            @RequestParam String token) {
        boolean valid = passwordResetService.validateResetToken(selector, token);
        if (!valid) {
            return AjaxResult.error("重設連結無效或已過期");
        }
        return AjaxResult.success("連結有效");
    }

    /**
     * 驗證重設連結有效性（POST body 版本）
     */
    @PostMapping("/validate-reset-token")
    public AjaxResult validateResetTokenPost(@Valid @RequestBody ValidateResetTokenBody body) {
        boolean valid = passwordResetService.validateResetToken(body.getSelector(), body.getToken());
        if (!valid) {
            return AjaxResult.error("重設連結無效或已過期");
        }
        return AjaxResult.success("連結有效");
    }

    /**
     * 密碼政策（註冊 + 重設共用）
     */
    @GetMapping("/password-policy")
    public AjaxResult getPasswordPolicy() {
        return AjaxResult.success(passwordPolicyService.getPolicyMap());
    }

    /**
     * 密碼重設政策（前端顯示用）
     */
    @GetMapping("/password-reset-policy")
    public AjaxResult getPasswordResetPolicy() {
        Map<String, Object> data = new HashMap<>(8);
        data.put("expireMinutes", passwordResetService.getTokenExpireMinutes());
        data.put("resendCooldownSeconds", passwordResetService.getResendCooldownSeconds());
        // 合併密碼政策（包含 minLength, maxLength, requireUppercase 等）
        data.putAll(passwordPolicyService.getPolicyMap());
        // 向後相容
        data.put("minPasswordLength", passwordPolicyService.getMinLength());
        return AjaxResult.success(data);
    }

    /**
     * 步驟二：重設密碼
     */
    @PostMapping("/reset-password")
    public AjaxResult resetPassword(@Valid @RequestBody ResetPasswordBody body) {
        String ipAddress = IpUtils.getIpAddr();
        String userAgent = getRequest().getHeader("User-Agent");

        passwordResetService.resetPassword(
                body.getSelector(), body.getToken(),
                body.getNewPassword(), ipAddress, userAgent
        );

        return AjaxResult.success("密碼重設成功");
    }

    /**
     * Email 頻率限制檢查（每小時 + 每日）
     */
    private void checkEmailRateLimit(String email) {
        int resendCooldownSeconds = passwordResetService.getResendCooldownSeconds();

        // 同一 email 冷卻期間內不可重複發送
        String limitKey = CacheConstants.PWD_RESET_LIMIT_KEY + email;
        if (redisCache.getCacheObject(limitKey) != null) {
            throw new ServiceException("重設連結已發送，請稍後再試");
        }

        // 每小時限制
        String hourlyKey = CacheConstants.PWD_RESET_HOURLY_KEY + email;
        Integer hourlyCount = redisCache.getCacheObject(hourlyKey);
        int maxHourly = shopConfigService.getInt(ShopConfigKey.PWD_RESET_EMAIL_HOURLY_LIMIT);
        if (hourlyCount != null && hourlyCount >= maxHourly) {
            throw new ServiceException("發送次數過多，請稍後再試");
        }

        // 每日限制
        String dailyKey = CacheConstants.PWD_RESET_DAILY_KEY + email;
        Integer dailyCount = redisCache.getCacheObject(dailyKey);
        int maxDaily = shopConfigService.getInt(ShopConfigKey.PWD_RESET_EMAIL_DAILY_LIMIT);
        if (dailyCount != null && dailyCount >= maxDaily) {
            throw new ServiceException("今日發送次數已達上限，請明天再試");
        }

        // 設定重發冷卻秒數
        redisCache.setCacheObject(limitKey, "1", resendCooldownSeconds, TimeUnit.SECONDS);

        // 累加每小時計數
        int newHourly = (hourlyCount == null ? 0 : hourlyCount) + 1;
        redisCache.setCacheObject(hourlyKey, newHourly, 1, TimeUnit.HOURS);

        // 累加每日計數
        incrementDailyCount(dailyKey, dailyCount);
    }

    @Data
    public static class MemberLoginBody {
        @NotBlank(message = "請輸入帳號")
        private String account;
        @NotBlank(message = "請輸入密碼")
        private String password;
        private String code;
        private String uuid;
    }

    @Data
    public static class MemberRegisterBody {
        /** 暱稱（選填，未提供時以 Email 前綴作為預設值） */
        private String nickname;
        @NotBlank(message = "請輸入 Email")
        private String email;
        @NotBlank(message = "請輸入密碼")
        private String password;
        private String code;
        private String uuid;
    }

    @Data
    public static class ResendVerificationBody {
        @NotBlank(message = "請輸入 Email")
        private String email;
        /** 圖形驗證碼 */
        private String code;
        /** 圖形驗證碼 uuid */
        private String uuid;
    }

    @Data
    public static class MemberProfileBody {
        @NotBlank(message = "請輸入暱稱")
        private String nickname;
        private String mobile;
        private String email;
        private String gender;
    }

    @Data
    public static class AvatarBody {
        @NotBlank(message = "請選擇頭像")
        private String avatar;
    }

    @Data
    public static class ForgotPasswordBody {
        @NotBlank(message = "請輸入 Email")
        private String email;
        /** 圖形驗證碼 */
        private String code;
        /** 圖形驗證碼 uuid */
        private String uuid;
    }

    @Data
    public static class ResetPasswordBody {
        @NotBlank(message = "Selector 不可為空")
        private String selector;
        @NotBlank(message = "Token 不可為空")
        private String token;
        @NotBlank(message = "請輸入新密碼")
        private String newPassword;
    }

    @Data
    public static class ValidateResetTokenBody {
        @NotBlank(message = "Selector 不可為空")
        private String selector;
        @NotBlank(message = "Token 不可為空")
        private String token;
    }
}
