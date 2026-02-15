package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
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
import com.cheng.shop.service.IShopMemberService;
import com.cheng.shop.utils.ShopMemberSecurityUtils;
import com.cheng.system.service.ISysConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

import static com.cheng.common.utils.ServletUtils.getRequest;

/**
 * 商城會員登入/註冊
 *
 * @author cheng
 */
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

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody MemberLoginBody body) {
        validateCaptcha(body.getAccount(), body.getCode(), body.getUuid());

        ShopMember member = findMemberByAccount(body.getAccount());
        if (member == null) {
            throw new ServiceException("帳號或密碼錯誤");
        }
        MemberStatus status = member.getStatusEnum();
        if (status == null || !status.canLogin()) {
            throw new ServiceException("此帳號已停用或凍結");
        }
        if (StringUtils.isEmpty(member.getPassword())
                || !SecurityUtils.matchesPassword(body.getPassword(), member.getPassword())) {
            throw new ServiceException("帳號或密碼錯誤");
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
        validateCaptcha(body.getNickname(), body.getCode(), body.getUuid());

        if (StringUtils.isEmpty(body.getMobile()) && StringUtils.isEmpty(body.getEmail())) {
            throw new ServiceException("手機或 Email 必須擇一填寫");
        }
        if (StringUtils.isNotEmpty(body.getEmail()) && !isEmail(body.getEmail())) {
            throw new ServiceException("Email 格式不正確");
        }

        ShopMember member = new ShopMember();
        member.setNickname(body.getNickname());
        member.setMobile(StringUtils.isEmpty(body.getMobile()) ? null : body.getMobile());
        member.setEmail(StringUtils.isEmpty(body.getEmail()) ? null : body.getEmail());
        member.setPassword(body.getPassword());

        if (StringUtils.isNotEmpty(member.getMobile()) && !memberService.checkMobileUnique(member)) {
            throw new ServiceException("手機號碼已存在");
        }
        if (StringUtils.isNotEmpty(member.getEmail()) && !memberService.checkEmailUnique(member)) {
            throw new ServiceException("Email 已存在");
        }

        int result = memberService.registerMember(member);
        if (result <= 0) {
            throw new ServiceException("註冊失敗，請稍後再試");
        }

        ShopMemberLoginUser loginUser = new ShopMemberLoginUser(member);
        String token = memberTokenService.createToken(loginUser);

        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        ajax.put("member", member);
        return ajax;
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

        // 更新會員資料
        ShopMember updateMember = new ShopMember();
        updateMember.setMemberId(member.getMemberId());
        updateMember.setNickname(body.getNickname());
        updateMember.setMobile(body.getMobile());
        updateMember.setEmail(body.getEmail());
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
            member.setMobile(body.getMobile());
            member.setEmail(body.getEmail());
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
        @NotBlank(message = "請輸入暱稱")
        private String nickname;
        private String mobile;
        private String email;
        @NotBlank(message = "請輸入密碼")
        private String password;
        private String code;
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
}
