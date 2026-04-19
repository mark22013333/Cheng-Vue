# 爬蟲系統設計模式說明

## 📐 設計架構

本系統採用 **模板模式（Template Pattern）+ 策略模式（Strategy Pattern）** 的設計，實現靈活可擴展的爬蟲架構。

### 核心設計理念

```
CrawlerHandler (模板類)
定義爬蟲執行流程：初始化 → 爬取 → 處理 → 儲存 → 清理

├── CA101WHandler (書籍爬蟲)
│   └── BookInventoryRepository → inv_xxx 資料表
│
├── CA102Handler (通用爬蟲)
│   └── GenericCrawlerRepository → ca102_data 資料表
│
└── CA103Handler (可擴展...)
    └── 自訂 Repository → ca103_xxx 資料表
```

## 🎯 模板模式：統一的執行流程

### CrawlerHandler 抽象類別

定義了所有爬蟲的標準執行流程：

```java
public final CrawlerResult execute(CrawledData crawledData) {
    // 1. 初始化
    beforeCrawl(crawledData);
    
    // 2. 建立 WebDriver
    WebDriver driver = createWebDriver();
    
    // 3. 爬取原始資料
    List<String[]> rawData = crawlWebsiteFetchData(driver, crawledData);
    
    // 4. 處理資料
    List<Object> processedData = processData(rawData, crawledData);
    
    // 5. 儲存資料
    saveData(processedData, crawledData);
    
    // 6. 清理
    afterCrawl(crawledData, success);
}
```

### 子類必須實作的抽象方法

```java
// 1. 定義爬蟲類型
protected abstract CrawlerType getCrawlerType();

// 2. 指定資料儲存策略
protected abstract CrawlerDataRepository getRepository();

// 3. 爬取原始資料
protected abstract List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData);

// 4. 處理原始資料
protected abstract List<Object> processData(List<String[]> rawData, CrawledData crawledData);
```

### 可選的鉤子方法

```java
// 爬取前初始化（可覆寫）
protected void beforeCrawl(CrawledData crawledData);

// 爬取後清理（可覆寫）
protected void afterCrawl(CrawledData crawledData, boolean success);

// 自訂 WebDriver 配置（可覆寫）
protected WebDriver createWebDriver();
```

## 🎨 策略模式：靈活的資料儲存

### CrawlerDataRepository 介面

定義資料儲存的統一介面：

```java
public interface CrawlerDataRepository {
    int batchSave(List<Object> dataList, CrawledData crawledData);
    boolean save(Object data, CrawledData crawledData);
}
```

### 不同的儲存策略

#### 1. BookInventoryRepository（書籍儲存）
```java
@Repository
public class BookInventoryRepository implements CrawlerDataRepository {
    // 儲存到 inv_book_info、inv_item_stock 等表
    // 處理書籍特有的邏輯
}
```

#### 2. GenericCrawlerRepository（通用儲存）
```java
@Repository
public class GenericCrawlerRepository implements CrawlerDataRepository {
    // 根據 CrawlerType 動態決定資料表
    // CA102 -> ca102_data
    // CA103 -> ca103_data
}
```

## 📝 如何新增爬蟲

### Step 1: 新增 CrawlerType

```java
public enum CrawlerType {
    CA101("ISBN書籍查詢", "書籍庫存"),
    CA102("範例爬蟲", "通用資料"),
    CA103("商品爬蟲", "電商商品"),  // ← 新增
}
```

### Step 2: 建立 Handler

```java
@Component
public class CA103Handler extends CrawlerHandler {
    
    @Autowired
    private ProductRepository productRepository;  // 自訂 Repository
    
    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA103;
    }
    
    @Override
    protected CrawlerDataRepository getRepository() {
        return productRepository;
    }
    
    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
        // 實作爬取邏輯
        driver.get(crawledData.getTargetUrl());
        // ...
        return rawData;
    }
    
    @Override
    protected List<Object> processData(List<String[]> rawData, CrawledData crawledData) {
        // 將原始資料轉換為 Product 物件
        List<Object> products = new ArrayList<>();
        // ...
        return products;
    }
}
```

### Step 3: 建立 Repository（選擇性）

如果是書籍類型，使用 `BookInventoryRepository`；
如果是其他類型，使用 `GenericCrawlerRepository` 或建立自訂 Repository：

```java
@Repository
public class ProductRepository implements CrawlerDataRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public int batchSave(List<Object> dataList, CrawledData crawledData) {
        // 批次儲存到 ca103_products 表
        String sql = "INSERT INTO ca103_products (name, price, url) VALUES (?, ?, ?)";
        // ...
    }
}
```

### Step 4: 執行爬蟲

```java
// 取得 Handler
CrawlerHandler handler = CrawlerHandler.getHandler(CrawlerType.CA103);

// 準備參數
CrawledData crawledData = new CrawledData();
crawledData.setTargetUrl("https://example.com");
crawledData.setCrawlerType(CrawlerType.CA103);

// 執行爬蟲
CrawlerResult result = handler.execute(crawledData);

// 檢查結果
if (result.isSuccess()) {
    log.info("爬蟲成功，儲存 {} 筆資料", result.getSavedDataCount());
} else {
    log.error("爬蟲失敗：{}", result.getMessage());
}
```

## 🎭 設計模式優勢

### 1. 模板模式優勢
- ✅ **統一流程**：所有爬蟲遵循相同的執行步驟
- ✅ **易於維護**：流程邏輯集中在父類
- ✅ **防止錯誤**：`execute()` 是 final 方法，無法被覆寫
- ✅ **靈活擴展**：子類只需實作特定邏輯

### 2. 策略模式優勢
- ✅ **解耦合**：爬蟲邏輯與儲存邏輯分離
- ✅ **可替換**：輕鬆切換不同的儲存策略
- ✅ **易測試**：可以 Mock Repository 進行單元測試

### 3. 整體優勢
- ✅ **開放封閉原則**：對擴展開放，對修改封閉
- ✅ **單一職責原則**：每個類別只負責一項職責
- ✅ **依賴倒置原則**：依賴抽象而非具體實作

## 📊 資料表設計規範

### 書籍類型（CA101）
```sql
-- 寫入 inv_xxx 系列表
inv_book_info       -- 書籍基本資訊
inv_item_stock      -- 庫存數量
inv_book_item       -- 書籍物品關聯
```

### 其他類型（CA102, CA103, ...）
```sql
-- 寫入 CAxxx 命名的表
ca102_data          -- CA102 通用資料
ca103_products      -- CA103 商品資料
ca104_news          -- CA104 新聞資料
```

## 🔧 進階應用

### 1. 自訂 WebDriver 配置

```java
@Override
protected WebDriver createWebDriver() {
    // 使用自訂配置
    return SeleniumUtil.createWebDriver(
        true,   // headless
        true,   // randomUserAgent
        false   // antiDetection
    );
}
```

### 2. 實作批次處理

```java
@Override
protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
    List<String[]> allData = new ArrayList<>();
    
    // 分頁爬取
    for (int page = 1; page <= 10; page++) {
        driver.get(baseUrl + "?page=" + page);
        List<String[]> pageData = parseCurrentPage(driver);
        allData.addAll(pageData);
        
        // 避免過於頻繁
        Thread.sleep(1000);
    }
    
    return allData;
}
```

### 3. 錯誤處理與重試

```java
@Override
protected void afterCrawl(CrawledData crawledData, boolean success) {
    if (!success) {
        // 記錄失敗任務
        failedTaskRepository.save(crawledData);
        
        // 發送告警
        alertService.send("爬蟲失敗：" + getCrawlerType());
    }
}
```

## 📈 執行結果範例

```java
CrawlerResult {
    crawlerType: CA101,
    success: true,
    message: "爬蟲執行成功",
    startTime: 1697712000000,
    endTime: 1697712015000,
    durationMs: 15000,
    rawDataCount: 100,
    processedDataCount: 95,
    savedDataCount: 90
}
```

## 🎓 總結

這個設計模式架構提供了：

1. **清晰的流程控制**：模板模式確保執行順序
2. **靈活的擴展性**：新增爬蟲只需實作幾個方法
3. **統一的資料儲存**：策略模式處理不同的儲存需求
4. **完整的生命週期管理**：自動處理 WebDriver 的建立和關閉
5. **詳細的執行記錄**：CrawlerResult 記錄完整的執行資訊

無論是書籍爬蟲還是其他類型的爬蟲，都可以輕鬆整合到這個系統中！
