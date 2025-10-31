package com.cheng.line.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LINE 模組配置屬性
 *
 * @author cheng
 */
@Data
@Component
@ConfigurationProperties(prefix = "line")
public class LineProperties {

    /**
     * Webhook 設定
     */
    private Webhook webhook = new Webhook();

    /**
     * Webhook 設定
     */
    @Data
    public static class Webhook {
        /**
         * Webhook Base URL（LINE 平台 Callback 使用）
         * 必須是公開可存取的網域
         * 範例：<a href="https://api.ap-domain.com">api.ap-domain.com</a>
         */
        private String baseUrl = "http://localhost:8080";
    }

    /**
     * 取得 Webhook Base URL
     *
     * @return Webhook Base URL
     */
    public String getWebhookBaseUrl() {
        return webhook.getBaseUrl();
    }
}
