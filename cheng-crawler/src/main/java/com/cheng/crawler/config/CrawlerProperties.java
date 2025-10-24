package com.cheng.crawler.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 爬蟲配置屬性
 * 從 application.yml 中讀取 crawler.selenium 配置
 * 使用 @Value 注入，不需要 @ConfigurationProperties 依賴
 *
 * @author cheng
 * @since 2025-10-24
 */
@Getter
@Component
public class CrawlerProperties {

    /**
     * Selenium 模式
     * - remote: 使用遠端 Docker Selenium (適用於正式環境)
     * - local: 使用本地 ChromeDriver (適用於開發環境)
     */
    @Value("${crawler.selenium.mode:local}")
    private String mode;

    /**
     * 遠端 Selenium URL (remote 模式使用)
     * 例如: <a href="http://localhost:9515">http://localhost:9515</a>
     */
    @Value("${crawler.selenium.remote-url:http://localhost:9515}")
    private String remoteUrl;

    /**
     * 本地 ChromeDriver 路徑 (local 模式使用)
     * 例如: /usr/bin/chromedriver 或 /usr/local/bin/chromedriver
     */
    @Value("${crawler.selenium.chrome-driver-path:/usr/bin/chromedriver}")
    private String chromeDriverPath;

    /**
     * 判斷是否使用遠端模式
     *
     * @return true 如果模式為 remote
     */
    public boolean isRemoteMode() {
        return "remote".equalsIgnoreCase(mode);
    }

    /**
     * 判斷是否使用本地模式
     *
     * @return true 如果模式為 local
     */
    public boolean isLocalMode() {
        return "local".equalsIgnoreCase(mode);
    }
}
