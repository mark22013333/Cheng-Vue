package com.cheng.shop.domain.dto;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth Token 交換回應
 *
 * @author cheng
 */
@Data
@Builder
public class OAuthTokenResponse {

    /** Access Token */
    private String accessToken;

    /** Refresh Token */
    private String refreshToken;

    /** Token 有效期（秒） */
    private Integer expiresIn;

    /** ID Token（OIDC，LINE/Google 有） */
    private String idToken;

    /** 授權範圍 */
    private String scope;
}
