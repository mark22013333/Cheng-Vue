package com.cheng.shop.oauth;

import com.cheng.shop.domain.dto.OAuthTokenResponse;
import com.cheng.shop.domain.dto.OAuthUserProfile;
import com.cheng.shop.enums.SocialProvider;

/**
 * OAuth 第三方登入策略介面
 * <p>
 * 每個第三方平台（LINE、Google、Facebook 等）實作此介面，
 * 由 {@link OAuthProviderFactory} 自動收集並根據 {@link SocialProvider} 路由。
 * </p>
 *
 * @author cheng
 */
public interface OAuthProviderStrategy {

    /**
     * 此實作對應的平台
     */
    SocialProvider getProvider();

    /**
     * 組裝授權 URL（含 state 參數）
     *
     * @param state       CSRF 防護用的隨機字串
     * @param redirectUri OAuth 回調 URI
     * @return 完整的授權 URL
     */
    String buildAuthorizeUrl(String state, String redirectUri);

    /**
     * 用 authorization code 換取 access token
     *
     * @param code        授權碼
     * @param redirectUri OAuth 回調 URI（必須與授權時一致）
     * @return Token 回應
     */
    OAuthTokenResponse exchangeToken(String code, String redirectUri);

    /**
     * 用 access token 取得用戶基本資料
     *
     * @param accessToken OAuth access token
     * @return 用戶資料
     */
    OAuthUserProfile getUserProfile(String accessToken);
}
