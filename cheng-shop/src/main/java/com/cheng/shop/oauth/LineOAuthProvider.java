package com.cheng.shop.oauth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.exception.ServiceException;
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
 * LINE Login OAuth2 策略實作
 * <p>
 * LINE Login API v2.1：
 * <ul>
 *   <li>授權：https://access.line.me/oauth2/v2.1/authorize</li>
 *   <li>Token：https://api.line.me/oauth2/v2.1/token</li>
 *   <li>Profile：https://api.line.me/v2/profile</li>
 * </ul>
 * </p>
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LineOAuthProvider implements OAuthProviderStrategy {

    private static final String AUTHORIZE_URL = "https://access.line.me/oauth2/v2.1/authorize";
    private static final String TOKEN_URL = "https://api.line.me/oauth2/v2.1/token";
    private static final String PROFILE_URL = "https://api.line.me/v2/profile";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ShopConfigService configService;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.LINE;
    }

    @Override
    public String buildAuthorizeUrl(String state, String redirectUri) {
        String channelId = configService.getRequiredString(ShopConfigKey.OAUTH_LINE_CHANNEL_ID);

        return AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + encode(channelId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state)
                + "&scope=profile%20openid";
    }

    @Override
    public OAuthTokenResponse exchangeToken(String code, String redirectUri) {
        String channelId = configService.getRequiredString(ShopConfigKey.OAUTH_LINE_CHANNEL_ID);
        String channelSecret = configService.getRequiredString(ShopConfigKey.OAUTH_LINE_CHANNEL_SECRET);

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("client_id", channelId)
                .add("client_secret", channelSecret)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("LINE Token 交換失敗：status={}, body={}", response.code(), body);
                throw new ServiceException("LINE 登入失敗，請稍後再試");
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
            log.error("LINE Token 交換異常", e);
            throw new ServiceException("LINE 登入連線失敗，請稍後再試");
        }
    }

    @Override
    public OAuthUserProfile getUserProfile(String accessToken) {
        Request request = new Request.Builder()
                .url(PROFILE_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("LINE Profile 取得失敗：status={}, body={}", response.code(), body);
                throw new ServiceException("LINE 用戶資料取得失敗");
            }

            JSONObject json = JSON.parseObject(body);
            return OAuthUserProfile.builder()
                    .providerId(json.getString("userId"))
                    .nickname(json.getString("displayName"))
                    .avatarUrl(json.getString("pictureUrl"))
                    .build();
        } catch (IOException e) {
            log.error("LINE Profile 取得異常", e);
            throw new ServiceException("LINE 用戶資料連線失敗");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
