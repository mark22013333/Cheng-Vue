package com.cheng.crawler.enums;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Cheng
 * @since 2025-10-02 00:26
 **/
@Slf4j
@Component
public class Constant {
    public static final String CHROME_DRIVER_PATH = "/usr/bin/chromedriver";
    public static final String DOWNLOAD_PATH = "/tmp/download";


    /**
     * 取得爬蟲使用的 ChromeDriver 路徑
     *
     * @return ChromeDriver 路徑
     */
    public static String getChromeDriverPath() {
        return CHROME_DRIVER_PATH;
    }
}
