package com.cheng.line.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
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
     * 應用程式的存取路徑（context-path）
     */
    @Value("${server.servlet.context-path:/}")
    private String contextPath;

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
     * 取得 Webhook Base URL（包含 context-path）
     *
     * @return Webhook Base URL
     */
    public String getWebhookBaseUrl() {
        String baseUrl = webhook.getBaseUrl();

        // 移除結尾的斜線
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 如果 context-path 不是根路徑，則加上 context-path
        if (contextPath != null && !contextPath.equals("/")) {
            // 確保 context-path 以 / 開頭
            String path = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
            // 移除結尾的斜線
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            return baseUrl + path;
        }

        return baseUrl;
    }
}
