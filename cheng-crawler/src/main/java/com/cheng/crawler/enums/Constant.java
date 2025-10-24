package com.cheng.crawler.enums;

import com.cheng.crawler.config.CrawlerProperties;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 爬蟲常數類別
 *
 * @author Cheng
 * @since 2025-10-02 00:26
 **/
@Slf4j
@Component
public class Constant {
    /**
     * 預設的 ChromeDriver 路徑（當配置未載入時使用）
     */
    public static final String DEFAULT_CHROME_DRIVER_PATH = "/usr/bin/chromedriver";

    /**
     * 下載路徑
     */
    public static final String DOWNLOAD_PATH = "/tmp/download";

    private static CrawlerProperties crawlerProperties;

    /**
     * 注入 CrawlerProperties
     * 使用 @Autowired 讓 Spring 自動注入配置
     */
    @Autowired
    public void setCrawlerProperties(CrawlerProperties properties) {
        Constant.crawlerProperties = properties;
    }

    /**
     * 取得爬蟲使用的 ChromeDriver 路徑
     * 優先從配置檔案讀取，若未設定則使用預設值
     *
     * @return ChromeDriver 路徑
     */
    public static String getChromeDriverPath() {
        if (crawlerProperties != null && !StringUtils.isEmpty(crawlerProperties.getChromeDriverPath())) {
            return crawlerProperties.getChromeDriverPath();
        }
        log.warn("CrawlerProperties 未載入，使用預設 ChromeDriver 路徑: {}", DEFAULT_CHROME_DRIVER_PATH);
        return DEFAULT_CHROME_DRIVER_PATH;
    }
}
