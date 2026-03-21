package com.cheng.shop.oauth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.dto.OAuthTokenResponse;
import com.cheng.shop.domain.dto.OAuthUserProfile;
import com.cheng.shop.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Google OAuth 2.0 策略實作
 * <p>
 * 使用標準 Authorization Code Flow，透過 OkHttp 呼叫 Google API。
 * 配置從系統參數表（sys_config）讀取。
 * </p>
 * <ul>
 *   <li>授權：<a href="https://accounts.google.com/o/oauth2/v2/auth">授權</a></li>
 *   <li>Token：https://oauth2.googleapis.com/token</li>
 *   <li>UserInfo：https://www.googleapis.com/oauth2/v3/userinfo</li>
 * </ul>
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthProvider implements OAuthProviderStrategy {

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ShopConfigService shopConfigService;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String buildAuthorizeUrl(String state, String redirectUri) {
        ensureEnabled();
        String clientId = getClientId();

        return AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state)
                + "&scope=" + encode("openid profile email")
                + "&access_type=offline"
                + "&prompt=select_account";
    }

    @Override
    public OAuthTokenResponse exchangeToken(String code, String redirectUri) {
        String clientId = getClientId();
        String clientSecret = getClientSecret();

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("Google Token 交換失敗：status={}, body={}", response.code(), body);
                throw new ServiceException("Google 登入失敗，請稍後再試");
            }

            JSONObject json = JSON.parseObject(body);
            return OAuthTokenResponse.builder()
                    .accessToken(json.getString("access_token"))
                    .refreshToken(json.getString("refresh_token"))
                    .expiresIn(json.getInteger("expires_in"))
                    .idToken(json.getString("id_token"))
                    .scope(json.getString("scope"))
                    .build();
        } catch (IOException e) {
            log.error("Google Token 交換異常", e);
            throw new ServiceException("Google 登入連線失敗，請稍後再試");
        }
    }

    @Override
    public OAuthUserProfile getUserProfile(String accessToken) {
        Request request = new Request.Builder()
                .url(USERINFO_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("Google UserInfo 取得失敗：status={}, body={}", response.code(), body);
                throw new ServiceException("Google 用戶資料取得失敗");
            }

            JSONObject json = JSON.parseObject(body);
            return OAuthUserProfile.builder()
                    .providerId(json.getString("sub"))
                    .nickname(json.getString("name"))
                    .avatarUrl(json.getString("picture"))
                    .email(json.getString("email"))
                    .build();
        } catch (IOException e) {
            log.error("Google UserInfo 取得異常", e);
            throw new ServiceException("Google 用戶資料連線失敗");
        }
    }

    // ==================== 私有方法 ====================

    private void ensureEnabled() {
        if (!shopConfigService.getBoolean(ShopConfigKey.OAUTH_GOOGLE_ENABLED)) {
            throw new ServiceException("Google 登入尚未啟用");
        }
    }

    private String getClientId() {
        String clientId = shopConfigService.getString(ShopConfigKey.OAUTH_GOOGLE_CLIENT_ID);
        if (StringUtils.isEmpty(clientId)) {
            throw new ServiceException("Google OAuth Client ID 未配置，請至系統參數設定");
        }
        return clientId;
    }

    private String getClientSecret() {
        String clientSecret = shopConfigService.getString(ShopConfigKey.OAUTH_GOOGLE_CLIENT_SECRET);
        if (StringUtils.isEmpty(clientSecret)) {
            throw new ServiceException("Google OAuth Client Secret 未配置，請至系統參數設定");
        }
        return clientSecret;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
