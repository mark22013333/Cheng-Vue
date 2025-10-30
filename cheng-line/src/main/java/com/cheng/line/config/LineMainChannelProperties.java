package com.cheng.line.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LINE 主頻道配置屬性
 * 從 application-xx.yml 中讀取 line.bot.* 配置（整合版）
 * 
 * 配置結構：
 * - line.bot.channel-token: LINE SDK 必要參數（Access Token）
 * - line.bot.channel-secret: LINE SDK 必要參數（Channel Secret）
 * - line.bot.enabled: 是否啟用主頻道自動載入（擴充參數）
 * - line.bot.channel-name: 頻道名稱（擴充參數）
 * - line.bot.channel-id: Channel ID（擴充參數）
 * - line.bot.bot-basic-id: Bot Basic ID（擴充參數）
 *
 * @author cheng
 */
@Data
@Component
@ConfigurationProperties(prefix = "line.bot")
public class LineMainChannelProperties {

    /**
     * 是否啟用主頻道自動載入（擴充參數）
     */
    private Boolean enabled = false;

    /**
     * 頻道名稱（擴充參數）
     */
    private String channelName;

    /**
     * Channel ID（擴充參數）
     */
    private String channelId;

    /**
     * Bot Basic ID（擴充參數，例如：@322okyxf）
     */
    private String botBasicId;

    /**
     * Channel Secret（LINE SDK 必要參數，加密儲存）
     */
    private String channelSecret;

    /**
     * Channel Access Token（LINE SDK 必要參數，加密儲存）
     * 對應 LINE SDK 的 channel-token 參數
     */
    private String channelToken;

    /**
     * 檢查配置是否完整
     *
     * @return 是否完整
     */
    public boolean isValid() {
        return enabled != null && enabled
                && channelName != null && !channelName.trim().isEmpty()
                && channelId != null && !channelId.trim().isEmpty()
                && botBasicId != null && !botBasicId.trim().isEmpty()
                && channelSecret != null && !channelSecret.trim().isEmpty()
                && channelToken != null && !channelToken.trim().isEmpty();
    }
    
    /**
     * 取得 Access Token（相容舊有 API）
     *
     * @return Access Token
     */
    public String getAccessToken() {
        return channelToken;
    }
    
    /**
     * 設定 Access Token（相容舊有 API）
     *
     * @param accessToken Access Token
     */
    public void setAccessToken(String accessToken) {
        this.channelToken = accessToken;
    }
}
