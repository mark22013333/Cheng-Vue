# 爬蟲框架 Repository 架構重構

## 📋 重構資訊
- **重構日期**: 2025-03-28
- **重構類型**: 架構重構 - Repository 模式實作
- **影響範圍**: GenericCrawlerRepository、CA102WHandler

## 🎯 重構目標

### 原問題
1. ❌ Handler 直接在 `crawlWebsiteFetchData()` 中呼叫 `batchSaveToDatabase()`
2. ❌ 違反單一職責原則：Handler 既爬取又儲存
3. ❌ Repository 只是空殼，沒有實際儲存邏輯
4. ❌ 破壞了模板方法模式的流程

### 正確架構
```
CrawlerHandler.execute() 模板方法
  ↓
1. crawlWebsiteFetchData(driver, crawledData)
   → 返回 List<R> (原始資料，如 String[])
  ↓
2. processData(rawData, crawledData)  
   → 轉換為 List<P> (業務物件，如 CompanyNewsDTO)
  ↓
3. saveData(processedData, crawledData)
   → 呼叫 Repository.batchSave()
     ↓
     Repository 使用註冊的轉換器
     → 將業務物件轉回 String[]
     → 使用 JdbcSqlTemplate 批次寫入
```

## 🏗️ 新架構設計

### 核心概念：SQL 註冊機制

```java
// 1. 在 Handler 初始化時註冊 SQL 和轉換器
@Override
protected void init() {
    String sql = buildDynamicSql();
    
    // 註冊到 Repository
    genericCrawlerRepository.registerSql(
        getCrawlerType(),
        sql,
        dto -> convertToStringArray(dto)  // 資料轉換器
    );
}
```

### GenericCrawlerRepository 新功能

#### 1. SQL 註冊管理
```java
private final Map<CrawlerType, String> sqlRegistry = new ConcurrentHashMap<>();
private final Map<CrawlerType, DataConverter<Object>> converterRegistry = new ConcurrentHashMap<>();

public void registerSql(CrawlerType crawlerType, String sql, DataConverter<Object> converter) {
    sqlRegistry.put(crawlerType, sql);
    converterRegistry.put(crawlerType, converter);
}
```

#### 2. 資料轉換器介面
```java
@FunctionalInterface
public interface DataConverter<T> {
    String[] convert(T data);
}
```

#### 3. 批次儲存流程
```java
@Override
public int batchSave(List<Object> dataList, CrawledData crawledData) {
    // 1. 取得註冊的 SQL
    String sql = sqlRegistry.get(crawlerType);
    
    // 2. 轉換資料（使用註冊的轉換器）
    List<String[]> convertedData = convertData(crawlerType, dataList);
    
    // 3. JDBC 批次寫入
    boolean success = jdbcSqlTemplate.insertBatchSql(sql, convertedData);
    
    return success ? convertedData.size() : 0;
}
```

## 📝 CA102WHandler 完整範例

### 1. 初始化階段
```java
@Override
protected void init() {
    // 建立動態 SQL（適配不同資料庫）
    String timestampFunc = jdbcSqlTemplate.getCurrentTimestampFunction();
    this.SQL = String.format(
        "INSERT INTO CAT102 (COMPANY_NAME, COMPANY_NO, TITLE, CONTENT, PUBLISH_DATE, EXTRACT_DATE) " +
        "VALUES (?, ?, ?, ?, ?, %s)",
        timestampFunc
    );
    
    // 註冊 SQL 和資料轉換器
    genericCrawlerRepository.registerSql(
        CrawlerType.CA102,
        SQL,
        dto -> {
            CompanyNewsDTO news = (CompanyNewsDTO) dto;
            return new String[]{
                news.getCompanyName(),
                news.getCompanyId(),
                news.getSubject(),
                news.getContent(),
                news.getPublishDate()
            };
        }
    );
    
    log.info("初始化 CA102WHandler，已註冊 SQL 到 Repository");
}
```

### 2. 爬取階段
```java
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
    List<String[]> allData = new ArrayList<>();
    
    // 逐日爬取資料
    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
        List<String[]> dailyData = processDailyData(currentDate);
        allData.addAll(dailyData);  // 只負責爬取，不儲存
    }
    
    return allData;  // 返回原始資料
}
```

### 3. 處理階段
```java
@Override
protected List<CompanyNewsDTO> processData(List<String[]> rawData, CrawledData crawledData) {
    List<CompanyNewsDTO> processedData = new ArrayList<>();
    
    for (String[] data : rawData) {
        // 將原始資料轉換為業務物件
        CompanyNewsDTO dto = new CompanyNewsDTO();
        dto.setCompanyName(data[0]);
        dto.setCompanyId(data[1]);
        dto.setSubject(data[2]);
        dto.setContent(data[3]);
        dto.setPublishDate(data[4]);
        
        processedData.add(dto);
    }
    
    return processedData;  // 返回業務物件
}
```

### 4. 儲存階段（自動執行）
```java
// CrawlerHandler 基類自動呼叫
protected int saveData(List<P> processedData, CrawledData crawledData) {
    CrawlerDataRepository<P> repository = getRepository();
    return repository.batchSave(processedData, crawledData);
}
```

## 🔄 資料流轉示意圖

```
┌─────────────────────────────────────────────────────────────────┐
│                    CA102WHandler.execute()                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 1. crawlWebsiteFetchData()                                      │
│    - 呼叫 API 取得資料                                           │
│    - 回傳 List<String[]>                                        │
│      [["台積電", "2330", "標題", "內容", "2025-03-28"], ...]     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. processData()                                                │
│    - 將 String[] 轉換為 CompanyNewsDTO                          │
│    - 回傳 List<CompanyNewsDTO>                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. saveData() → GenericCrawlerRepository.batchSave()            │
│    ┌─────────────────────────────────────────────────┐          │
│    │ (1) 取得註冊的 SQL 和轉換器                      │          │
│    │ (2) 使用轉換器將 DTO → String[]                 │          │
│    │ (3) 呼叫 JdbcSqlTemplate.insertBatchSql()      │          │
│    │     - 批次寫入（1000筆/批）                     │          │
│    │     - 錯誤處理                                  │          │
│    │     - 效能優化                                  │          │
│    └─────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                        ✅ 儲存完成
```

## 🚀 未來新增爬蟲的標準流程

### 步驟 1：建立 Handler 類別
```java
@Component
public class CA103WHandler extends CrawlerHandler<String[], ProductDTO> {
    
    @Autowired
    private GenericCrawlerRepository genericCrawlerRepository;
    
    private String SQL;
    
    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA103;
    }
    
    @Override
    protected CrawlerDataRepository<ProductDTO> getRepository() {
        return (CrawlerDataRepository<ProductDTO>) (CrawlerDataRepository<?>) genericCrawlerRepository;
    }
}
```

### 步驟 2：在 init() 中註冊 SQL
```java
@Override
protected void init() {
    // 建立 SQL
    String timestampFunc = jdbcSqlTemplate.getCurrentTimestampFunction();
    this.SQL = String.format(
        "INSERT INTO CA103T (NAME, PRICE, CATEGORY, CREATE_TIME) VALUES (?, ?, ?, %s)",
        timestampFunc
    );
    
    // 註冊到 Repository
    genericCrawlerRepository.registerSql(
        CrawlerType.CA103,
        SQL,
        dto -> {
            ProductDTO product = (ProductDTO) dto;
            return new String[]{
                product.getName(),
                product.getPrice(),
                product.getCategory()
            };
        }
    );
}
```

### 步驟 3：實作爬取方法
```java
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
    List<String[]> data = new ArrayList<>();
    
    // 爬取邏輯
    // ...
    
    return data;  // 只負責爬取，不儲存
}
```

### 步驟 4：實作處理方法
```java
@Override
protected List<ProductDTO> processData(List<String[]> rawData, CrawledData crawledData) {
    return rawData.stream()
        .map(data -> {
            ProductDTO dto = new ProductDTO();
            dto.setName(data[0]);
            dto.setPrice(data[1]);
            dto.setCategory(data[2]);
            return dto;
        })
        .collect(Collectors.toList());
}
```

### 步驟 5：執行（儲存自動完成）
```java
// 基類會自動呼叫 saveData() → Repository.batchSave()
CrawlerResult result = handler.execute(crawledData);
```

## ✅ 架構優勢

### 1. 職責分離
| 元件 | 職責 | 不負責 |
|-----|------|-------|
| **Handler** | 爬取、處理資料 | ❌ 不直接儲存 |
| **Repository** | 儲存資料 | ❌ 不處理業務邏輯 |
| **JdbcSqlTemplate** | JDBC 批次寫入 | ❌ 不管業務物件 |

### 2. 可重用性
- ✅ Repository 可被所有爬蟲重用
- ✅ 註冊機制讓每個爬蟲自訂 SQL 和轉換邏輯
- ✅ JDBC 批次寫入邏輯統一管理

### 3. 可維護性
- ✅ 每個元件職責清晰
- ✅ 修改儲存邏輯只需改 Repository
- ✅ 新增爬蟲只需註冊 SQL 和轉換器

### 4. 可測試性
```java
// 可以單獨測試 Handler 的爬取邏輯
List<String[]> rawData = handler.crawlWebsiteFetchData(driver, crawledData);

// 可以單獨測試 Repository 的儲存邏輯
repository.registerSql(type, sql, converter);
repository.batchSave(dataList, crawledData);
```

### 5. 靈活性
```java
// 可以註冊不同的轉換器
repository.registerSql(type, sql, dto -> {
    // 自訂轉換邏輯
    return new String[]{...};
});

// 可以動態變更 SQL
repository.registerSql(type, newSql);
```

## 📊 效能優化

### 批次寫入
- Repository 使用 `JdbcSqlTemplate.insertBatchSql()`
- 預設 1000 筆/批次
- 批次間延遲 500ms

### 記憶體優化
- 使用 Stream API 處理大量資料
- 避免一次載入所有資料到記憶體

### 錯誤處理
- 單筆轉換失敗不影響其他資料
- 批次寫入失敗有詳細日誌

## 🔍 除錯指南

### 1. SQL 未註冊錯誤
```
[CA102] 未註冊 SQL 語句，無法儲存。請在 Handler 的 init() 方法中呼叫 registerSql()
```
**解決**：在 Handler 的 `init()` 方法中呼叫 `registerSql()`

### 2. 資料轉換失敗
```
[CA102] 資料轉換失敗: CompanyNewsDTO{...}
```
**解決**：檢查轉換器邏輯，確保欄位順序與 SQL 一致

### 3. 批次寫入失敗
```
[CA102] JDBC 批次寫入失敗
```
**解決**：檢查 SQL 語法、資料庫連線、欄位數量是否匹配

## 🎓 設計模式應用

### 1. 模板方法模式 (Template Method)
- **CrawlerHandler** 定義執行流程
- 子類別實作具體步驟

### 2. 策略模式 (Strategy)
- **DataConverter** 作為策略
- 每個爬蟲註冊自己的轉換策略

### 3. 註冊模式 (Registry)
- **sqlRegistry** 管理 SQL
- **converterRegistry** 管理轉換器

### 4. Repository 模式
- **GenericCrawlerRepository** 抽象資料存取
- 隔離業務邏輯與資料儲存

## 🔗 相關文件
- [爬蟲框架批次儲存重構](./CRAWLER_REFACTORING_BATCH_SAVE.md)
- [CA102 爬蟲實作說明](./CA102_CRAWLER_IMPLEMENTATION.md)
- [JDBC 工具類 JPA 移除](./JDBC_TEMPLATE_JPA_REMOVAL.md)

## 👥 變更記錄
- **2025-03-28**: 完成 Repository 架構重構，實作 SQL 註冊機制
- **作者**: Cheng
- **審核**: -

---

## 💡 總結

這次重構實現了：
1. ✅ **職責分離** - Handler 負責爬取，Repository 負責儲存
2. ✅ **架構清晰** - 遵循模板方法模式的完整流程
3. ✅ **易於擴展** - 新爬蟲只需註冊 SQL 和轉換器
4. ✅ **統一管理** - 所有爬蟲的儲存邏輯集中在 Repository

**未來新增爬蟲只需三步**：
1. 建立 Handler 繼承 CrawlerHandler
2. 在 init() 中註冊 SQL 和轉換器
3. 實作 crawl 和 process 方法

**儲存會自動完成！** ✨
