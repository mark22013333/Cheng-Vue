# 爬蟲參數化與 Quartz 排程整合

## 📋 更新資訊
- **更新日期**: 2025-03-28
- **功能**: 支援參數化執行與 Quartz 排程整合
- **影響範圍**: CrawlerHandler、所有 Handler、Quartz 任務

## 🎯 設計目標

### 問題
- 爬蟲執行無法從外部傳入參數
- Quartz 排程無法動態調整爬蟲行為
- 後台介面無法靈活控制爬蟲

### 解決方案
使用**方法多載**，同時支援：
1. ✅ **無參數執行** - 簡單場景，使用預設值
2. ✅ **帶參數執行** - 靈活場景，從 Quartz/後台傳參

---

## 🔧 核心設計

### 1. CrawlerParams DTO

```java
@Data
@Builder
public class CrawlerParams {
    // 基本參數
    private Boolean enabled;          // 是否啟用
    private String mode;              // 模式（today-only, full-sync 等）
    private String startDate;         // 開始日期
    private String endDate;           // 結束日期
    private Integer batchSize;        // 批次大小
    private Long timeout;             // 逾時時間
    
    // 可擴充的自訂參數
    private Map<String, Object> customParams;
    
    // 便利方法
    public static CrawlerParams empty() {
        return CrawlerParams.builder().build();
    }
}
```

### 2. CrawlerHandler 方法多載

```java
public abstract class CrawlerHandler<R, P> {
    
    /**
     * 無參數版本 - 簡單場景
     */
    public final CrawlerResult execute() {
        return execute(CrawlerParams.empty());
    }
    
    /**
     * 帶參數版本 - 靈活場景
     */
    public final CrawlerResult execute(CrawlerParams params) {
        // 實際執行邏輯
        beforeCrawl(params);
        List<R> rawData = crawlWebsiteFetchData(driver, params);
        List<P> processedData = processData(rawData, params);
        saveData(processedData);
        afterCrawl(params, true);
    }
    
    // 子類實作的方法都帶參數
    protected abstract List<R> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params);
    protected abstract List<P> processData(List<R> rawData, CrawlerParams params);
    protected void beforeCrawl(CrawlerParams params) { }
    protected void afterCrawl(CrawlerParams params, boolean success) { }
}
```

---

## 🚀 使用範例

### 範例 1：簡單執行（無參數）

```java
// 使用預設參數
CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA102);
CrawlerResult result = handler.execute();  // ← 簡單！
```

### 範例 2：帶參數執行

```java
// 設定模式
CrawlerParams params = CrawlerParams.builder()
    .mode("today-only")
    .build();

CrawlerResult result = handler.execute(params);
```

### 範例 3：完整參數

```java
CrawlerParams params = CrawlerParams.builder()
    .mode("full-sync")
    .startDate("2025-03-01")
    .endDate("2025-03-28")
    .batchSize(500)
    .timeout(60000L)
    .build();

CrawlerResult result = handler.execute(params);
```

### 範例 4：自訂參數

```java
CrawlerParams params = CrawlerParams.builder()
    .mode("custom")
    .build();

// 加入自訂參數
params.setCustomParam("company", "2330");
params.setCustomParam("depth", 3);

CrawlerResult result = handler.execute(params);

// 在 Handler 中使用
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) {
    String company = params.getCustomParamAsString("company");
    Integer depth = params.getCustomParamAsInteger("depth");
    
    // 根據參數調整爬取邏輯
    // ...
}
```

---

## 📅 Quartz 排程整合

### 1. 建立 Quartz 任務

**CrawlerTask.java**：
```java
@Slf4j
@Component("crawlerTask")
public class CrawlerTask {
    
    /**
     * 無參數執行
     * 
     * Quartz 設定：
     * - Bean名稱: crawlerTask
     * - 方法名稱: runCA102
     * - 參數: (無)
     */
    public void runCA102() {
        CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA102);
        CrawlerResult result = handler.execute();  // 無參數
        logResult(result);
    }
    
    /**
     * 帶模式參數
     * 
     * Quartz 設定：
     * - Bean名稱: crawlerTask
     * - 方法名稱: runCA102WithParams
     * - 參數: today-only
     */
    public void runCA102WithParams(String mode) {
        CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA102);
        
        CrawlerParams params = CrawlerParams.builder()
                .mode(mode)
                .build();
        
        CrawlerResult result = handler.execute(params);
        logResult(result);
    }
    
    /**
     * 多參數執行
     * 
     * Quartz 設定：
     * - Bean名稱: crawlerTask
     * - 方法名稱: runCA102Advanced
     * - 參數: today-only, 500, 60000
     */
    public void runCA102Advanced(String mode, Integer batchSize, Long timeout) {
        CrawlerParams params = CrawlerParams.builder()
                .mode(mode)
                .batchSize(batchSize)
                .timeout(timeout)
                .build();
        
        CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA102);
        CrawlerResult result = handler.execute(params);
        logResult(result);
    }
    
    /**
     * 日期範圍執行
     * 
     * Quartz 設定：
     * - Bean名稱: crawlerTask
     * - 方法名稱: runCA102DateRange
     * - 參數: 2025-03-01, 2025-03-28
     */
    public void runCA102DateRange(String startDate, String endDate) {
        CrawlerParams params = CrawlerParams.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA102);
        CrawlerResult result = handler.execute(params);
        logResult(result);
    }
}
```

### 2. 後台介面設定

#### 設定方式 A：簡單任務（無參數）
```
任務名稱：爬取台股重大訊息
Bean名稱：crawlerTask
方法名稱：runCA102
參數：(空)
Cron表達式：0 0 1 * * ?  （每天凌晨1點執行）
```

#### 設定方式 B：帶模式參數
```
任務名稱：爬取今日台股重大訊息
Bean名稱：crawlerTask
方法名稱：runCA102WithParams
參數：today-only
Cron表達式：0 0 9,12,15 * * ?  （每天9點、12點、15點執行）
```

#### 設定方式 C：完整參數
```
任務名稱：爬取台股重大訊息（高效能）
Bean名稱：crawlerTask
方法名稱：runCA102Advanced
參數：full-sync, 1000, 120000
Cron表達式：0 0 2 * * ?  （每天凌晨2點執行）
```

#### 設定方式 D：日期範圍
```
任務名稱：爬取台股重大訊息（指定日期）
Bean名稱：crawlerTask
方法名稱：runCA102DateRange
參數：2025-03-01, 2025-03-28
Cron表達式：手動觸發
```

---

## 📝 實作指南

### 步驟 1：在 Handler 中使用參數

```java
@Component
public class CA102WHandler extends CrawlerHandler<String[], CompanyNewsDTO> {
    
    @Value("${crawler.ca102.today-only:false}")
    private boolean todayOnly;
    
    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) {
        // 優先使用傳入的參數，其次使用配置
        String mode = params.getMode();
        boolean isTodayOnly = "today-only".equals(mode) || todayOnly;
        
        // 日期範圍
        LocalDate startDate;
        if (params.getStartDate() != null) {
            startDate = LocalDate.parse(params.getStartDate());
        } else {
            startDate = isTodayOnly ? LocalDate.now() : LocalDate.of(2016, 8, 25);
        }
        
        LocalDate endDate;
        if (params.getEndDate() != null) {
            endDate = LocalDate.parse(params.getEndDate());
        } else {
            endDate = LocalDate.now();
        }
        
        log.info("爬取日期範圍: {} 至 {}", startDate, endDate);
        
        // 爬取邏輯
        // ...
    }
    
    @Override
    protected void beforeCrawl(CrawlerParams params) {
        log.info("初始化爬蟲環境，參數: {}", params);
        
        // 根據參數進行不同的初始化
        if ("full-sync".equals(params.getMode())) {
            log.info("使用完整同步模式");
        }
    }
}
```

### 步驟 2：建立 Quartz 任務類別

已提供完整範例：`CrawlerTask.java`

### 步驟 3：在後台新增定時任務

1. 進入「系統管理」→「定時任務」
2. 點選「新增」
3. 填寫任務資訊（如上方設定範例）
4. 儲存並啟動

---

## 💡 進階用法

### 1. 條件執行

```java
@Override
protected void beforeCrawl(CrawlerParams params) {
    // 檢查是否啟用
    if (params.getEnabled() != null && !params.getEnabled()) {
        throw new RuntimeException("爬蟲已停用");
    }
}
```

### 2. 逾時控制

```java
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) {
    Long timeout = params.getTimeout();
    if (timeout != null) {
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
    }
    
    // 爬取邏輯
    // ...
}
```

### 3. 批次大小控制

```java
@Override
protected List<CompanyNewsDTO> processData(List<String[]> rawData, CrawlerParams params) {
    Integer batchSize = params.getBatchSize();
    if (batchSize != null) {
        // 使用自訂批次大小處理
        return processByBatch(rawData, batchSize);
    }
    
    // 使用預設批次大小
    return processDefault(rawData);
}
```

### 4. 動態模式切換

```java
public enum CrawlerMode {
    TODAY_ONLY,      // 只爬今天
    FULL_SYNC,       // 完整同步
    DATE_RANGE,      // 日期範圍
    INCREMENTAL      // 增量更新
}

@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) {
    String modeStr = params.getMode();
    CrawlerMode mode = CrawlerMode.valueOf(modeStr.toUpperCase().replace("-", "_"));
    
    switch (mode) {
        case TODAY_ONLY:
            return crawlToday();
        case FULL_SYNC:
            return crawlFullSync();
        case DATE_RANGE:
            return crawlDateRange(params.getStartDate(), params.getEndDate());
        case INCREMENTAL:
            return crawlIncremental();
        default:
            throw new IllegalArgumentException("不支援的模式: " + modeStr);
    }
}
```

---

## 🎨 最佳實踐

### 1. 參數優先順序

```
Quartz 參數 > 配置檔參數 > 硬編碼預設值
```

```java
@Value("${crawler.ca102.batch-size:1000}")
private int defaultBatchSize;

protected int getBatchSize(CrawlerParams params) {
    // 1. 優先使用 Quartz 傳入的參數
    if (params.getBatchSize() != null) {
        return params.getBatchSize();
    }
    
    // 2. 其次使用配置檔
    return defaultBatchSize;
    
    // 3. 最後使用硬編碼預設值（已在 @Value 中設定）
}
```

### 2. 參數驗證

```java
@Override
protected void beforeCrawl(CrawlerParams params) {
    // 驗證日期範圍
    if (params.getStartDate() != null && params.getEndDate() != null) {
        LocalDate start = LocalDate.parse(params.getStartDate());
        LocalDate end = LocalDate.parse(params.getEndDate());
        
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
        
        if (start.isBefore(LocalDate.of(2016, 1, 1))) {
            throw new IllegalArgumentException("開始日期不能早於 2016-01-01");
        }
    }
    
    // 驗證批次大小
    if (params.getBatchSize() != null) {
        int batchSize = params.getBatchSize();
        if (batchSize < 10 || batchSize > 10000) {
            throw new IllegalArgumentException("批次大小必須在 10-10000 之間");
        }
    }
}
```

### 3. 日誌記錄

```java
@Override
protected void beforeCrawl(CrawlerParams params) {
    log.info("==================================================");
    log.info("開始執行爬蟲: {}", getCrawlerType().name());
    log.info("模式: {}", params.getMode() != null ? params.getMode() : "預設");
    log.info("日期範圍: {} - {}", 
            params.getStartDate() != null ? params.getStartDate() : "預設",
            params.getEndDate() != null ? params.getEndDate() : "預設");
    log.info("批次大小: {}", params.getBatchSize() != null ? params.getBatchSize() : "預設");
    log.info("==================================================");
}
```

---

## ✅ 優勢總結

### 1. 靈活性

| 場景 | 改前 | 改後 |
|-----|------|------|
| **簡單執行** | `execute()` | `execute()` ✅ 不變 |
| **Quartz 排程** | ❌ 無法傳參 | ✅ 可傳任意參數 |
| **後台控制** | ❌ 需改程式碼 | ✅ 後台直接設定 |
| **測試** | ❌ 需建立環境變數 | ✅ 直接傳參數 |

### 2. 向後相容

```java
// 舊程式碼完全不受影響
handler.execute();  // ← 仍然可用！

// 新功能
handler.execute(params);  // ← 新增支援
```

### 3. 可擴充性

```java
// 未來可輕鬆加入新參數
CrawlerParams params = CrawlerParams.builder()
    .mode("today-only")
    .customParam("newFeature", value)  // ← 擴充
    .build();
```

---

## 🔗 相關文件

- [爬蟲框架架構總結](./ARCHITECTURE_SUMMARY.md)
- [移除 CrawledData 參數重構](./REFACTOR_REMOVE_CRAWLEDDATA_PARAMETER.md)
- [Repository 架構設計](./CRAWLER_REPOSITORY_ARCHITECTURE.md)

---

**更新日期**: 2025-03-28  
**維護者**: Cheng  
**版本**: 5.0 (支援參數化與 Quartz 整合)
