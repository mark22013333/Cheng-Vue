package com.cheng.crawler.handler;

import com.cheng.crawler.config.CrawlerProperties;
import com.cheng.crawler.dto.CrawlerParams;
import com.cheng.crawler.dto.CrawlerResult;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.repository.CrawlerDataRepository;
import com.cheng.crawler.utils.JdbcSqlTemplate;
import com.cheng.crawler.utils.SeleniumUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 爬蟲處理器抽象類別（模板模式 + 泛型）
 * 定義爬蟲的執行流程，子類別實作具體的爬取和資料處理邏輯
 * <p>
 * 執行流程：
 * 1. beforeCrawl() - 爬取前的初始化
 * 2. crawlWebsiteFetchData() - 爬取資料（由子類實作）
 * 3. processData() - 處理爬取的資料（由子類實作）
 * 4. saveData() - 儲存資料（根據類型選擇不同的 Repository）
 * 5. afterCrawl() - 爬取後的清理工作
 *
 * @param <R> 原始資料類型（Raw Data Type）
 * @param <P> 處理後資料類型（Processed Data Type）
 * @author Cheng
 * @since 2025-10-02 00:06
 **/
@Slf4j
@Component
public abstract class CrawlerHandler<R, P> implements ApplicationContextAware {

    private static final Map<CrawlerType, CrawlerHandler<?, ?>> HANDLER_MAP = new HashMap<>();

    private static ApplicationContext applicationContext;

    /**
     * JDBC 模板工具（可選）
     * 如果爬蟲需要使用 JDBC 直接寫入資料庫，可以注入此工具
     */
    @Autowired
    protected JdbcSqlTemplate jdbcSqlTemplate;

    /**
     * 爬蟲配置屬性
     * 用於讀取 Selenium 相關配置（remote/local 模式）
     */
    @Autowired
    protected CrawlerProperties crawlerProperties;

    @PostConstruct
    public void handlerInit() {
        log.info("CrawlerHandler Initialized: {}", this.getClass().getSimpleName());
        CrawlerType crawlerType = this.getCrawlerType();
        if (crawlerType == null) {
            log.warn("crawlerType handler without type needs to be checked! {}", this.getClass().getName());
            return;
        }
        HANDLER_MAP.put(crawlerType, this);

        // 呼叫子類的初始化方法
        try {
            init();
        } catch (Exception e) {
            log.error("Handler 初始化失敗: {}", this.getClass().getSimpleName(), e);
        }
    }

    /**
     * 子類可覆寫此方法進行初始化
     * 此方法會在 @PostConstruct 階段被自動呼叫
     */
    protected void init() {
        // 預設為空實作，子類可選擇性覆寫
    }

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        CrawlerHandler.applicationContext = context;
    }

    /**
     * 取得爬蟲類型（子類必須實作）
     */
    protected abstract CrawlerType getCrawlerType();

    /**
     * 取得資料儲存 Repository（子類必須實作）
     * 根據爬蟲類型返回對應的 Repository 實作
     */
    protected abstract CrawlerDataRepository<P> getRepository();

    // ==================== 公開方法 ====================

    /**
     * 單一查詢並儲存（模板方法 - final）
     * 適用於即時查詢場景，直接爬取並儲存單筆資料
     * <p>
     * 此方法為 final，統一處理類型轉換邏輯，
     * 子類別只需實作 {@link #crawlSingle(String)} 方法即可
     *
     * @param input      輸入內容（例如：ISBN、URL等）
     * @param resultType 預期回傳的結果類型
     * @param <T>        結果類型泛型
     * @return 爬取並儲存後的結果
     * @throws Exception 處理失敗時拋出例外
     */
    public final <T> T crawlSingleAndSave(String input, Class<T> resultType) throws Exception {
        // 1. 呼叫子類的核心爬取邏輯（回傳類型為 P）
        P result = crawlSingle(input);

        // 2. 統一的類型轉換邏輯
        if (resultType.isInstance(result)) {
            return resultType.cast(result);
        } else {
            throw new IllegalArgumentException(
                    String.format("[%s] 不支援的回傳類型: %s，實際類型: %s",
                            getCrawlerType().name(),
                            resultType.getName(),
                            result != null ? result.getClass().getName() : "null")
            );
        }
    }

    /**
     * 單一查詢核心邏輯（子類實作）
     * 回傳類型為泛型 P（處理後資料類型），提供完整的類型安全
     *
     * <p>子類別實作此方法時：
     * <ul>
     *   <li>直接回傳 P 類型的資料，不需要進行類型轉換</li>
     *   <li>專注於爬取邏輯，類型轉換由父類統一處理</li>
     *   <li>可以選擇是否進行資料儲存（依業務需求）</li>
     * </ul>
     *
     * @param input 輸入內容（例如：ISBN、URL等）
     * @return 爬取結果（類型為 P）
     * @throws Exception 處理失敗時拋出例外
     */
    protected P crawlSingle(String input) throws Exception {
        throw new UnsupportedOperationException(
                String.format("[%s] 此爬蟲不支援單一查詢模式，請實作 crawlSingle() 方法",
                        getCrawlerType().name())
        );
    }

    /**
     * 執行爬蟲任務（無參數版本）
     * <p>
     * 適用於簡單執行場景，使用預設參數
     *
     * @return 爬蟲執行結果
     */
    public final CrawlerResult execute() {
        return execute(CrawlerParams.empty());
    }

    /**
     * 執行爬蟲任務（帶參數版本）
     * <p>
     * 執行流程：
     * 1. beforeCrawl() - 初始化
     * 2. crawlWebsiteFetchData() - 爬取資料
     * 3. processData() - 處理資料
     * 4. saveData() - 儲存資料
     * 5. afterCrawl() - 清理
     * <p>
     * <b>適用於 Quartz 排程呼叫：</b>
     * <pre>
     * // 後台可以設定參數
     * CrawlerParams params = CrawlerParams.builder()
     *     .mode("today-only")
     *     .batchSize(500)
     *     .build();
     * handler.execute(params);
     * </pre>
     *
     * @param params 爬蟲執行參數（可為 null，為 null 時使用空參數）
     * @return 爬蟲執行結果
     */
    public final CrawlerResult execute(CrawlerParams params) {
        // 如果參數為 null，使用空參數
        CrawlerParams effectiveParams = params != null ? params : CrawlerParams.empty();

        WebDriver driver = null;
        CrawlerResult result = new CrawlerResult();
        result.setCrawlerType(getCrawlerType());
        result.setStartTime(System.currentTimeMillis());

        try {
            log.info("[{}] 開始執行爬蟲任務，參數: {}", getCrawlerType().name(), effectiveParams);

            // 1. 初始化
            beforeCrawl(effectiveParams);

            // 2. 建立 WebDriver
            driver = createWebDriver();

            // 3. 爬取資料
            log.info("[{}] 開始爬取資料", getCrawlerType().name());
            List<R> rawData = crawlWebsiteFetchData(driver, effectiveParams);
            result.setRawDataCount(rawData != null ? rawData.size() : 0);

            // 4. 處理資料
            log.info("[{}] 開始處理資料，共 {} 筆", getCrawlerType().name(), result.getRawDataCount());
            List<P> processedData = processData(rawData, effectiveParams);
            result.setProcessedDataCount(processedData != null ? processedData.size() : 0);

            // 5. 儲存資料
            if (processedData != null && !processedData.isEmpty()) {
                log.info("[{}] 開始儲存資料，共 {} 筆", getCrawlerType().name(), processedData.size());
                int savedCount = saveData(processedData);
                result.setSavedDataCount(savedCount);
            }

            // 6. 清理
            afterCrawl(effectiveParams, true);

            result.setSuccess(true);
            result.setMessage("爬蟲執行成功");
            log.info("[{}] 爬蟲任務完成 - 原始: {}, 處理: {}, 儲存: {}",
                    getCrawlerType().name(),
                    result.getRawDataCount(),
                    result.getProcessedDataCount(),
                    result.getSavedDataCount());

        } catch (Exception e) {
            log.error("[{}] 爬蟲執行失敗: {}", getCrawlerType().name(), e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("爬蟲執行失敗: " + e.getMessage());
            result.setError(e);
            afterCrawl(effectiveParams, false);
        } finally {
            // 確保 WebDriver 被關閉
            SeleniumUtil.safelyQuitWebDriver(driver);
            result.setEndTime(System.currentTimeMillis());
            result.setDurationMs(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    // ==================== 可選方法（子類可選擇性實作）====================

    /**
     * 爬取網站並取得原始資料（批次處理用）
     *
     * <p>此方法僅用於批次處理模式（execute()），子類別可以選擇：
     * <ul>
     *   <li>只實作 crawlSingleAndSave() - 適用於單一查詢場景</li>
     *   <li>只實作 crawlWebsiteFetchData() + processData() - 適用於批次處理場景</li>
     *   <li>兩者都實作 - 同時支援兩種模式</li>
     * </ul>
     *
     * @param driver WebDriver
     * @param params 爬蟲參數（可以根據參數調整爬取行為）
     * @return 原始資料列表（類型安全）
     * @throws UnsupportedOperationException 如果子類別不支援批次處理模式
     */
    protected List<R> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) throws Exception {
        throw new UnsupportedOperationException(
                String.format("[%s] 此爬蟲僅支援單一查詢模式，請使用 crawlSingleAndSave() 方法",
                        getCrawlerType().name())
        );
    }

    /**
     * 處理爬取的原始資料，轉換為業務物件（批次處理用）
     *
     * <p>此方法僅用於批次處理模式（execute()）
     * 如果子類別不支援批次處理，可以不實作此方法
     *
     * @param rawData 原始資料（類型安全）
     * @param params  爬蟲參數（可用於處理過程中的特殊邏輯）
     * @return 處理後的業務物件列表（類型安全）
     * @throws UnsupportedOperationException 如果子類別不支援批次處理模式
     */
    protected List<P> processData(List<R> rawData, CrawlerParams params) throws Exception {
        throw new UnsupportedOperationException(
                String.format("[%s] 此爬蟲僅支援單一查詢模式，請使用 crawlSingleAndSave() 方法",
                        getCrawlerType().name())
        );
    }


    // ==================== Hook Method（子類可選擇性覆寫）====================

    /**
     * 爬取前的初始化工作（Hook Method）
     * 子類可覆寫此方法進行自訂初始化
     *
     * @param params 爬蟲參數（可根據參數進行不同的初始化）
     */
    protected void beforeCrawl(CrawlerParams params) throws Exception {
        log.debug("[{}] 執行爬取前初始化", getCrawlerType().name());
    }

    /**
     * 爬取後的清理工作（Hook Method）
     * 子類可覆寫此方法進行自訂清理
     *
     * @param params  爬蟲參數
     * @param success 是否成功
     */
    protected void afterCrawl(CrawlerParams params, boolean success) {
        log.debug("[{}] 執行爬取後清理，成功: {}", getCrawlerType().name(), success);
    }

    /**
     * 建立 WebDriver（可覆寫以自訂配置）
     * 根據配置自動選擇 Remote 或 Local 模式
     */
    protected WebDriver createWebDriver() {
        if (crawlerProperties != null) {
            log.info("[{}] 使用 {} 模式建立 WebDriver", 
                getCrawlerType().name(), 
                crawlerProperties.getMode());
            return SeleniumUtil.createWebDriver(crawlerProperties);
        } else {
            log.warn("[{}] CrawlerProperties 未注入，使用預設本地模式", getCrawlerType().name());
            return SeleniumUtil.createWebDriver();
        }
    }

    /**
     * 儲存資料到資料庫
     * 使用子類提供的 Repository 進行儲存
     *
     * @param processedData 處理後的資料（類型安全）
     * @return 實際儲存的筆數
     */
    protected int saveData(List<P> processedData) throws Exception {
        CrawlerDataRepository<P> repository = getRepository();
        if (repository == null) {
            log.warn("[{}] 未設定 Repository，跳過資料儲存", getCrawlerType().name());
            return 0;
        }
        return repository.batchSave(processedData, getCrawlerType());
    }

    /**
     * 從 ApplicationContext 取得 bean 的方法
     *
     * @param beanClass bean 的類別
     * @return bean 實例，若取不到則返回 null
     */
    protected <T> T getBean(Class<T> beanClass) {
        if (applicationContext != null) {
            try {
                return applicationContext.getBean(beanClass);
            } catch (Exception e) {
                log.error("無法從 ApplicationContext 取得 bean: {}", beanClass.getName(), e);
            }
        }
        return null;
    }

    // ==================== JDBC 批次寫入工具方法 ====================

    /**
     * 通用批次寫入方法：將資料批次寫入資料庫
     * <p>
     * 此方法提供以下功能：
     * <ul>
     *   <li>自動分批處理大量資料（預設每批 1000 筆）</li>
     *   <li>錯誤處理：單一批次失敗不影響其他批次</li>
     *   <li>效能優化：批次間自動延遲，減輕資料庫負擔</li>
     *   <li>詳細日誌：記錄每批次的處理狀態</li>
     * </ul>
     *
     * @param sql  SQL 語句（使用 ? 作為參數佔位符）
     * @param data 要寫入的資料列表（每個 String[] 對應 SQL 中的參數）
     * @return 是否全部寫入成功
     * @throws IllegalStateException 如果 JdbcSqlTemplate 未注入
     */
    protected boolean batchSaveToDatabase(String sql, List<String[]> data) {
        return batchSaveToDatabase(sql, data, 1000, 500);
    }

    /**
     * 通用批次寫入方法（可自訂批次大小和延遲時間）
     *
     * @param sql       SQL 語句
     * @param data      要寫入的資料列表
     * @param batchSize 批次大小（每批處理幾筆）
     * @param delayMs   批次間延遲時間（毫秒）
     * @return 是否全部寫入成功
     * @throws IllegalStateException 如果 JdbcSqlTemplate 未注入
     */
    protected boolean batchSaveToDatabase(String sql, List<String[]> data, int batchSize, long delayMs) {
        if (jdbcSqlTemplate == null) {
            throw new IllegalStateException(
                    String.format("[%s] JdbcSqlTemplate 未注入，無法使用批次寫入功能",
                            getCrawlerType().name())
            );
        }

        if (data == null || data.isEmpty()) {
            log.warn("[{}] 沒有資料需要寫入資料庫", getCrawlerType().name());
            return true;
        }

        log.info("[{}] 準備批次寫入資料，共 {} 筆，批次大小: {}",
                getCrawlerType().name(), data.size(), batchSize);

        // 如果資料量小於批次大小，直接寫入
        if (data.size() <= batchSize) {
            try {
                boolean success = jdbcSqlTemplate.insertBatchSql(sql, data);
                if (success) {
                    log.info("[{}] 資料寫入成功，共 {} 筆", getCrawlerType().name(), data.size());
                } else {
                    log.error("[{}] 資料寫入失敗", getCrawlerType().name());
                }
                return success;
            } catch (Exception e) {
                log.error("[{}] 寫入資料到資料庫時發生錯誤", getCrawlerType().name(), e);
                return false;
            }
        }

        // 分批處理大量資料
        boolean allSuccess = true;
        int totalBatches = (int) Math.ceil((double) data.size() / batchSize);

        for (int i = 0; i < data.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, data.size());
            List<String[]> batch = data.subList(i, endIndex);
            int currentBatch = (i / batchSize) + 1;

            log.info("[{}] 處理批次 {}/{}, 筆數: {}",
                    getCrawlerType().name(), currentBatch, totalBatches, batch.size());

            try {
                boolean success = jdbcSqlTemplate.insertBatchSql(sql, batch);
                if (!success) {
                    log.error("[{}] 批次 {} 寫入失敗", getCrawlerType().name(), currentBatch);
                    allSuccess = false;
                }
            } catch (Exception e) {
                log.error("[{}] 批次 {} 寫入錯誤", getCrawlerType().name(), currentBatch, e);
                allSuccess = false;
                // 繼續處理下一批，而不是直接中斷
            }

            // 每個批次處理完後等待一下，減輕資料庫負擔
            if (delayMs > 0 && i + batchSize < data.size()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("[{}] 批次延遲被中斷", getCrawlerType().name());
                }
            }
        }

        if (allSuccess) {
            log.info("[{}] 所有批次寫入成功，共 {} 筆資料", getCrawlerType().name(), data.size());
        } else {
            log.warn("[{}] 部分批次寫入失敗，請檢查日誌", getCrawlerType().name());
        }

        return allSuccess;
    }

    /**
     * 根據類型取得對應的 Handler
     *
     * @param type 爬蟲類型
     * @return Handler 實例（使用萬用字元泛型）
     */
    public static CrawlerHandler<?, ?> getHandler(CrawlerType type) {
        return HANDLER_MAP.get(type);
    }
}
