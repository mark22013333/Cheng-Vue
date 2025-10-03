package com.cheng.crawler.utils;

import com.cheng.crawler.enums.Constant;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Selenium 相關操作的工具類，提供統一的 WebDriver 建立、配置和管理方法
 *
 * @author cheng
 * @since 2025/05/27 00:17
 **/
@Slf4j
public class SeleniumUtil {

    /**
     * 預設的 User-Agent 列表，用於隨機選擇以避免被反爬蟲機制偵測
     */
    private static final String[] DEFAULT_USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.7103.114 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.7103.114 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15"
    };

    /**
     * 建立預設的 ChromeOptions 配置，包含無頭模式、反爬蟲設定和隨機 User-Agent
     *
     * @return 配置好的 ChromeOptions
     */
    public static ChromeOptions createChromeOptions() {
        // 使用預設值呼叫參數化版本：啟用無頭模式、啟用隨機User-Agent、啟用反偵測
        return createChromeOptions(true, true, true);
    }

    /**
     * 建立自訂的 ChromeOptions 配置
     *
     * @param headless        是否使用無頭模式
     * @param randomUserAgent 是否使用隨機 User-Agent
     * @param antiDetection   是否啟用反偵測功能
     * @return 配置好的 ChromeOptions
     */
    public static ChromeOptions createChromeOptions(boolean headless, boolean randomUserAgent, boolean antiDetection) {
        System.setProperty("webdriver.chrome.driver", Constant.getChromeDriverPath());
        ChromeOptions options = new ChromeOptions();

        // 基本配置
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");  // 允許遠端連線，解決 CDP 相關問題
        options.addArguments("--disable-infobars");

        // 新增穩定性設定，減少連線重置問題
        options.addArguments("--disable-web-security");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-setuid-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-features=NetworkService");
        options.addArguments("--dns-prefetch-disable");

        // 增加 WebDriver 連線穩定性的設定
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.setPageLoadTimeout(Duration.ofSeconds(60));

        // 反爬蟲配置
        if (antiDetection) {
            options.addArguments("--disable-blink-features=AutomationControlled");
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
            options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
        }

        // 設定 User-Agent
        if (randomUserAgent) {
            options.addArguments("--user-agent=" + getRandomUserAgent());
        } else {
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.7103.114 Safari/537.36");
        }

        return options;
    }

    /**
     * 從預設 User-Agent 列表中隨機選擇一個
     *
     * @return 隨機選擇的 User-Agent 字串
     */
    public static String getRandomUserAgent() {
        return DEFAULT_USER_AGENTS[new Random().nextInt(DEFAULT_USER_AGENTS.length)];
    }

    /**
     * 建立 WebDriver 實例，使用預設配置
     *
     * @return WebDriver 實例
     */
    public static WebDriver createWebDriver() {
        return new ChromeDriver(createChromeOptions());
    }

    /**
     * 建立 WebDriver 實例，使用自訂配置
     *
     * @param headless        是否使用無頭模式
     * @param randomUserAgent 是否使用隨機 User-Agent
     * @param antiDetection   是否啟用反偵測功能
     * @return WebDriver 實例
     */
    public static WebDriver createWebDriver(boolean headless, boolean randomUserAgent, boolean antiDetection) {
        return new ChromeDriver(createChromeOptions(headless, randomUserAgent, antiDetection));
    }

    /**
     * 建立 WebDriverWait 實例
     *
     * @param driver         WebDriver
     * @param timeoutSeconds 等待逾時秒數
     * @return WebDriverWait 實例
     */
    public static WebDriverWait createWebDriverWait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 安全關閉 WebDriver
     *
     * @param driver 要關閉的 WebDriver
     */
    public static void safelyQuitWebDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error("關閉 WebDriver 時發生錯誤: {}", e.getMessage());
            }
        }
    }

    /**
     * 安全關閉執行緒池
     *
     * @param executor       執行緒池
     * @param timeoutMinutes 等待逾時分鐘數
     */
    public static void shutdownExecutorService(ExecutorService executor, int timeoutMinutes) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(timeoutMinutes, TimeUnit.MINUTES)) {
                    log.warn("執行緒池等待逾時，強制關閉未完成的任務");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("等待執行緒池關閉時被中斷: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
