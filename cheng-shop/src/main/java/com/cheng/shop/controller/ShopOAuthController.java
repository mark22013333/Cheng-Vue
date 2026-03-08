package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
import com.cheng.common.constant.Constants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.shop.domain.dto.OAuthCallbackBody;
import com.cheng.shop.enums.SocialProvider;
import com.cheng.shop.security.ShopMemberLoginUser;
import com.cheng.shop.service.SocialLoginService;
import com.cheng.shop.utils.ShopMemberSecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.cheng.common.utils.ServletUtils.getRequest;

/**
 * 商城 OAuth 第三方登入控制器
 *
 * @author cheng
 */
@Slf4j
@Anonymous
@PublicApi
@RestController
@RequestMapping("/shop/auth/oauth")
@RequiredArgsConstructor
public class ShopOAuthController extends BaseController {

    private final SocialLoginService socialLoginService;

    /**
     * 取得第三方登入授權 URL
     *
     * @param provider    第三方平台（LINE/GOOGLE）
     * @param redirectUri 前端回調 URL
     */
    @GetMapping("/authorize-url")
    public AjaxResult getAuthorizeUrl(
            @RequestParam String provider,
            @RequestParam String redirectUri) {
        SocialProvider socialProvider = SocialProvider.fromCode(provider.toUpperCase());
        String authorizeUrl = socialLoginService.getAuthorizeUrl(socialProvider, redirectUri);

        AjaxResult result = AjaxResult.success();
        result.put("authorizeUrl", authorizeUrl);
        return result;
    }

    /**
     * 處理 OAuth 回調（用 authorization code 換取 JWT）
     */
    @PostMapping("/callback")
    public AjaxResult callback(@Valid @RequestBody OAuthCallbackBody body) {
        SocialProvider provider = SocialProvider.fromCode(body.getProvider().toUpperCase());

        // 從前端設定中取得 redirectUri，此處由前端傳入以保持一致
        HttpServletRequest request = getRequest();

        String token = socialLoginService.handleCallback(
                provider, body.getCode(), body.getState(),
                request.getHeader("X-OAuth-Redirect-Uri"),
                request);

        AjaxResult result = AjaxResult.success("登入成功");
        result.put(Constants.TOKEN, token);
        return result;
    }

    /**
     * 綁定社交帳號到已登入會員
     */
    @PostMapping("/link")
    public AjaxResult link(@Valid @RequestBody LinkBody body) {
        ShopMemberLoginUser loginUser = ShopMemberSecurityUtils.getLoginUser();
        SocialProvider provider = SocialProvider.fromCode(body.getProvider().toUpperCase());

        socialLoginService.linkAccount(
                loginUser.getMemberId(), provider,
                body.getCode(), body.getRedirectUri());

        return success("綁定成功");
    }

    /**
     * 解除社交帳號綁定
     */
    @PostMapping("/unlink")
    public AjaxResult unlink(@Valid @RequestBody UnlinkBody body) {
        ShopMemberLoginUser loginUser = ShopMemberSecurityUtils.getLoginUser();
        SocialProvider provider = SocialProvider.fromCode(body.getProvider().toUpperCase());

        socialLoginService.unlinkAccount(loginUser.getMemberId(), provider);
        return success("解綁成功");
    }

    /**
     * 查詢會員的社交帳號綁定狀態
     */
    @GetMapping("/bindings")
    public AjaxResult bindings() {
        ShopMemberLoginUser loginUser = ShopMemberSecurityUtils.getLoginUser();
        return success(socialLoginService.getBindings(loginUser.getMemberId()));
    }

    // ==================== 請求體 ====================

    @Data
    public static class LinkBody {
        @NotBlank(message = "provider 不可為空")
        private String provider;
        @NotBlank(message = "code 不可為空")
        private String code;
        @NotBlank(message = "redirectUri 不可為空")
        private String redirectUri;
    }

    @Data
    public static class UnlinkBody {
        @NotBlank(message = "provider 不可為空")
        private String provider;
    }
}
