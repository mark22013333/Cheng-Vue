package com.cheng.shop.oauth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.service.ILineConfigService;
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
 * 從 LINE 頻道設定（sys_line_config）讀取 Login Channel ID/Secret，
 * 不再需要在系統參數（sys_config）中重複設定。
 * </p>
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

    private final ILineConfigService lineConfigService;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.LINE;
    }

    @Override
    public String buildAuthorizeUrl(String state, String redirectUri) {
        String channelId = getLoginChannelId();

        return AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + encode(channelId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state)
                + "&scope=profile%20openid";
    }

    @Override
    public OAuthTokenResponse exchangeToken(String code, String redirectUri) {
        String channelId = getLoginChannelId();
        String channelSecret = getLoginChannelSecret();

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

    /**
     * 檢查 LINE Login 是否已設定（預設頻道有 Login Channel ID 和 Secret）
     *
     * @return true=已設定可用，false=未設定
     */
    public boolean isLoginConfigured() {
        LineConfig config = lineConfigService.selectDefaultLineConfig();
        return config != null
                && StringUtils.isNotEmpty(config.getLoginChannelId())
                && StringUtils.isNotEmpty(config.getLoginChannelSecret());
    }

    // ==================== 私有方法 ====================

    /**
     * 從預設 LINE 頻道設定取得 Login Channel ID
     */
    private String getLoginChannelId() {
        LineConfig config = getDefaultLineConfig();
        String loginChannelId = config.getLoginChannelId();
        if (StringUtils.isEmpty(loginChannelId)) {
            throw new ServiceException("LINE 頻道設定中未填寫 Login Channel ID，請至「行銷管理 → LINE 設定」配置");
        }
        return loginChannelId;
    }

    /**
     * 從預設 LINE 頻道設定取得 Login Channel Secret
     */
    private String getLoginChannelSecret() {
        LineConfig config = getDefaultLineConfig();
        String loginChannelSecret = config.getLoginChannelSecret();
        if (StringUtils.isEmpty(loginChannelSecret)) {
            throw new ServiceException("LINE 頻道設定中未填寫 Login Channel Secret，請至「行銷管理 → LINE 設定」配置");
        }
        return loginChannelSecret;
    }

    /**
     * 取得預設 LINE 頻道設定，若無預設頻道則拋出異常
     */
    private LineConfig getDefaultLineConfig() {
        LineConfig config = lineConfigService.selectDefaultLineConfig();
        if (config == null) {
            throw new ServiceException("尚未設定預設 LINE 頻道，請至「行銷管理 → LINE 設定」配置");
        }
        return config;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
