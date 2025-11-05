package com.cheng.crawler.utils;

import com.cheng.crawler.config.CrawlerProperties;
import com.cheng.crawler.enums.Constant;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
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
     * 使用本地模式（需要設定 ChromeDriver 路徑）
     *
     * @return 配置好的 ChromeOptions
     */
    public static ChromeOptions createChromeOptions() {
        // 使用預設值呼叫參數化版本：啟用無頭模式、啟用隨機User-Agent、啟用反偵測、本地模式
        return createChromeOptions(true, true, true, false);
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
        return createChromeOptions(headless, randomUserAgent, antiDetection, false);
    }

    /**
     * 建立自訂的 ChromeOptions 配置（支援 Remote/Local 模式）
     *
     * @param headless        是否使用無頭模式
     * @param randomUserAgent 是否使用隨機 User-Agent
     * @param antiDetection   是否啟用反偵測功能
     * @param isRemote        是否為遠端模式（Docker Selenium）
     * @return 配置好的 ChromeOptions
     */
    public static ChromeOptions createChromeOptions(boolean headless, boolean randomUserAgent, boolean antiDetection, boolean isRemote) {
        // 只有本地模式且使用預設路徑時才需要設定 ChromeDriver 路徑
        // 如果透過 createLocalWebDriver() 建立，路徑設定會在該方法中處理
        if (!isRemote) {
            String chromeDriverPath = Constant.getChromeDriverPath();
            if ("auto".equalsIgnoreCase(chromeDriverPath) || chromeDriverPath == null || chromeDriverPath.trim().isEmpty()) {
                // 使用 WebDriverManager 自動管理
                WebDriverManager.chromedriver().setup();
            } else {
                // 使用指定路徑
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            }
        }
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
     * 建立 WebDriver 實例，使用預設配置（本地模式）
     *
     * @return WebDriver 實例
     */
    public static WebDriver createWebDriver() {
        return new ChromeDriver(createChromeOptions());
    }

    /**
     * 根據配置建立 WebDriver 實例
     * 自動判斷使用 Remote 或 Local 模式
     *
     * @param properties 爬蟲配置屬性
     * @return WebDriver 實例
     */
    public static WebDriver createWebDriver(CrawlerProperties properties) {
        return createWebDriver(properties, properties.isHeadless(), true, true);
    }

    /**
     * 根據配置建立 WebDriver 實例（完整參數版本）
     *
     * @param properties      爬蟲配置屬性
     * @param headless        是否使用無頭模式
     * @param randomUserAgent 是否使用隨機 User-Agent
     * @param antiDetection   是否啟用反偵測功能
     * @return WebDriver 實例
     */
    public static WebDriver createWebDriver(CrawlerProperties properties, boolean headless, boolean randomUserAgent, boolean antiDetection) {
        if (properties.isRemoteMode()) {
            return createRemoteWebDriver(properties.getRemoteUrl(), headless, randomUserAgent, antiDetection);
        } else {
            return createLocalWebDriver(properties.getChromeDriverPath(), headless, randomUserAgent, antiDetection);
        }
    }

    /**
     * 建立遠端 WebDriver（Docker Selenium）
     *
     * @param remoteUrl       遠端 Selenium URL
     * @param headless        是否使用無頭模式
     * @param randomUserAgent 是否使用隨機 User-Agent
     * @param antiDetection   是否啟用反偵測功能
     * @return RemoteWebDriver 實例
     */
    public static WebDriver createRemoteWebDriver(String remoteUrl, boolean headless, boolean randomUserAgent, boolean antiDetection) {
        try {
            ChromeOptions options = createChromeOptions(headless, randomUserAgent, antiDetection, true);
            log.info("建立遠端 WebDriver，URL: {}", remoteUrl);
            return new RemoteWebDriver(new URL(remoteUrl), options);
        } catch (Exception e) {
            log.error("建立遠端 WebDriver 失敗: {}", e.getMessage(), e);
            throw new RuntimeException("無法連接到遠端 Selenium: " + remoteUrl, e);
        }
    }

    /**
     * 建立本地 WebDriver
     *
     * @param chromeDriverPath ChromeDriver 路徑（如果為 "auto" 則使用 WebDriverManager 自動下載）
     * @param headless         是否使用無頭模式
     * @param randomUserAgent  是否使用隨機 User-Agent
     * @param antiDetection    是否啟用反偵測功能
     * @return ChromeDriver 實例
     */
    public static WebDriver createLocalWebDriver(String chromeDriverPath, boolean headless, boolean randomUserAgent, boolean antiDetection) {
        // 如果路徑設定為 "auto" 或空值，使用 WebDriverManager 自動下載最新的 ChromeDriver
        if ("auto".equalsIgnoreCase(chromeDriverPath) || chromeDriverPath == null || chromeDriverPath.trim().isEmpty()) {
            log.info("使用 WebDriverManager 自動下載最新的 ChromeDriver");
            WebDriverManager.chromedriver().setup();
        } else {
            log.info("使用指定路徑的 ChromeDriver: {}", chromeDriverPath);
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        }
        
        ChromeOptions options = createChromeOptions(headless, randomUserAgent, antiDetection, false);
        return new ChromeDriver(options);
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
