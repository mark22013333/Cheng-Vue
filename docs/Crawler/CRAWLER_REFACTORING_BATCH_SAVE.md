# 爬蟲框架重構：批次儲存方法提升至基類

## 📋 重構資訊
- **重構日期**: 2025-03-28
- **重構類型**: 方法提升 (Extract to Superclass)
- **影響範圍**: CrawlerHandler 基類與所有子類爬蟲

## 🎯 重構目的

### 問題分析
在實作 CA102WHandler 時，發現 `privateSaveToDatabase()` 方法是一個通用的批次寫入邏輯：
- ✅ 邏輯完全獨立於業務
- ✅ 所有使用 JDBC 直接寫入的爬蟲都需要相同功能
- ✅ 重複程式碼會增加維護成本

### 解決方案
將批次寫入邏輯提升到 `CrawlerHandler` 基類，讓所有子類爬蟲都能重用。

## 🔧 重構內容

### 1. CrawlerHandler 基類新增功能

#### 1.1 注入 JdbcSqlTemplate
```java
@Autowired(required = false)
protected JdbcSqlTemplate jdbcSqlTemplate;
```
- 使用 `required = false`，不需要 JDBC 的爬蟲不會報錯
- 子類可直接使用，無需重複注入

#### 1.2 新增通用批次寫入方法
```java
// 簡化版本（使用預設參數）
protected boolean batchSaveToDatabase(String sql, List<String[]> data)

// 完整版本（可自訂參數）
protected boolean batchSaveToDatabase(String sql, List<String[]> data, 
                                     int batchSize, long delayMs)
```

**功能特性**：
- ✅ 自動分批處理（預設 1000 筆/批）
- ✅ 錯誤處理：單批失敗不影響其他批
- ✅ 效能優化：批次間延遲（預設 500ms）
- ✅ 詳細日誌：記錄處理進度
- ✅ 空值檢查：自動處理 null 或空列表
- ✅ 狀態檢查：確保 JdbcSqlTemplate 已注入

### 2. CA102WHandler 簡化

#### 2.1 移除重複程式碼
**重構前** (61 行):
```java
@Autowired
private JdbcSqlTemplate jdbcSqlTemplate;

private boolean privateSaveToDatabase(List<String[]> data, JdbcSqlTemplate jdbcSqlTemplate) {
    // ... 61 行的批次處理邏輯
}
```

**重構後** (1 行):
```java
boolean success = batchSaveToDatabase(SQL, dailyData);
```

#### 2.2 程式碼減少量
- **刪除**: 61 行 (privateSaveToDatabase 方法 + JdbcSqlTemplate 注入)
- **新增**: 1 行 (呼叫基類方法)
- **淨減少**: 60 行 (減少 98.4%)

## 📊 重構效益

### 1. 程式碼重用性
| 項目 | 重構前 | 重構後 | 改善 |
|-----|-------|-------|------|
| CA102WHandler 行數 | 581 | 521 | -60 (-10.3%) |
| 重複程式碼 | 每個爬蟲 61 行 | 0 | -100% |
| 未來新爬蟲成本 | 61 行/個 | 1 行/個 | -98.4% |

### 2. 維護性提升
- **集中管理**: 批次邏輯只在基類一處維護
- **統一行為**: 所有爬蟲的批次處理行為一致
- **易於優化**: 修改基類即可讓所有爬蟲受益

### 3. 擴展性增強
```java
// 未來可輕鬆新增功能
protected boolean batchSaveToDatabase(String sql, List<String[]> data) {
    // 可加入重試機制
    // 可加入進度Callback
    // 可加入效能監控
    return batchSaveToDatabase(sql, data, 1000, 500);
}
```

## 🚀 使用方式

### 基本使用（預設參數）
```java
@Component
public class MyNewCrawlerHandler extends CrawlerHandler<String[], MyDTO> {
    
    private String SQL;
    
    @Override
    protected void init() {
        // 初始化 SQL（會自動適配資料庫類型）
        String timestampFunc = jdbcSqlTemplate.getCurrentTimestampFunction();
        this.SQL = String.format(
            "INSERT INTO MY_TABLE (COL1, COL2, CREATE_TIME) VALUES (?, ?, %s)",
            timestampFunc
        );
    }
    
    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) {
        List<String[]> data = new ArrayList<>();
        // ... 爬取資料
        
        // 直接呼叫基類的批次寫入方法
        boolean success = batchSaveToDatabase(SQL, data);
        
        return data;
    }
}
```

### 進階使用（自訂參數）
```java
// 自訂批次大小和延遲
boolean success = batchSaveToDatabase(SQL, data, 500, 1000);
// 每批 500 筆，批次間延遲 1000ms
```

## ✅ 重構驗證

### 功能驗證
- [x] CA102WHandler 正常運作
- [x] 批次寫入功能正常
- [x] 錯誤處理正常
- [x] 日誌輸出正確
- [x] 不使用 JDBC 的爬蟲不受影響

### 程式碼品質
- [x] 遵循 DRY 原則 (Don't Repeat Yourself)
- [x] 符合單一職責原則
- [x] 提升程式碼可讀性
- [x] 增強可維護性
- [x] 提供完整文件

## 📝 遷移指南

### 對於現有爬蟲 (如 WCS132WHandler)
如果有其他爬蟲也使用類似的 `privateSaveToDatabase` 方法：

1. **移除 JdbcSqlTemplate 注入**
   ```java
   // 刪除這行
   @Autowired
   private JdbcSqlTemplate jdbcSqlTemplate;
   ```

2. **替換方法呼叫**
   ```java
   // 舊寫法
   privateSaveToDatabase(data, jdbcSqlTemplate);
   
   // 新寫法
   batchSaveToDatabase(SQL, data);
   ```

3. **刪除 privateSaveToDatabase 方法**
   完整刪除該方法的定義

### 對於新爬蟲
直接使用基類的 `batchSaveToDatabase()` 方法，無需重新實作。

## 🎓 設計模式應用

### 模板方法模式 (Template Method)
```
CrawlerHandler (抽象類別)
├── 定義執行流程
├── 提供通用工具方法 ← batchSaveToDatabase
└── 子類實作特定邏輯
    ├── CA102WHandler
    ├── WCS132WHandler
    └── ... (未來的爬蟲)
```

### 策略模式 (Strategy)
```java
// 批次大小策略
batchSaveToDatabase(sql, data, 1000, 500);  // 高頻率小批次
batchSaveToDatabase(sql, data, 5000, 2000); // 低頻率大批次
```

## 📈 效能考量

### 預設參數選擇理由
- **批次大小 1000**: 
  - MySQL 預設 `max_allowed_packet` 可容納
  - JDBC PreparedStatement 批次效率最佳點
  - 記憶體使用合理

- **延遲 500ms**:
  - 避免資料庫連線池耗盡
  - 給資料庫喘息時間
  - 對爬蟲總時間影響小

### 可調整場景
```java
// 高效能場景（資料庫負載低）
batchSaveToDatabase(sql, data, 5000, 0);

// 謹慎場景（資料庫負載高）
batchSaveToDatabase(sql, data, 100, 2000);

// 大量資料場景
batchSaveToDatabase(sql, data, 10000, 1000);
```

## 🔗 相關文件
- [CA102 爬蟲實作說明](./CA102_CRAWLER_IMPLEMENTATION.md)
- [CrawlerHandler API 文件](../cheng-crawler/src/main/java/com/cheng/crawler/CrawlerHandler.java)

## 👥 變更記錄
- **2025-03-28**: 完成重構，將批次儲存方法提升至基類
- **作者**: Cheng
- **審核**: -
