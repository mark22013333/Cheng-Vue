# CA102 爬蟲實作說明文件

## 📋 專案資訊
- **爬蟲名稱**: CA102 - 臺灣證券交易所即時重大訊息爬蟲
- **資料來源**: 公開資訊觀測站 (https://mops.twse.com.tw)
- **實作日期**: 2025-03-28
- **資料表名稱**: CAT102 (T代表Table)

## 🎯 功能概述

### 主要功能
1. **爬取公司重大訊息**
   - 從民國105年8月25日開始爬取至今日
   - 可透過參數設定僅爬取今日資料
   - 支援逐日爬取並即時儲存

2. **資料處理流程**
   - 呼叫列表 API 取得當日所有重大訊息
   - 針對每筆訊息呼叫詳細資料 API
   - 動態產生 HTML 內容
   - 批次寫入資料庫

3. **資料庫相容性**
   - 自動偵測資料庫類型 (MySQL/MSSQL/Oracle/PostgreSQL)
   - 動態產生對應的 SQL 語法
   - 目前使用 MySQL

## 📁 檔案結構

```
cheng-crawler/
├── src/main/java/com/cheng/crawler/
│   ├── handler/
│   │   └── CA102WHandler.java          # 爬蟲主要邏輯
│   ├── dto/
│   │   └── CompanyNewsDTO.java         # 公司新聞資料物件
│   ├── utils/
│   │   ├── JacksonUtil.java            # JSON 處理工具
│   │   ├── JdbcSqlTemplate.java        # JDBC 工具（含資料庫類型偵測）
│   │   ├── TimeUtil.java               # 時間轉換工具（民國年↔西元年）
│   │   └── MapUtils.java               # Map 工具
│   └── enums/
│       └── CrawlerType.java            # 爬蟲類型列舉
└── sql/
    └── create_cat102_table.sql         # 資料表建立腳本
```

## 🔧 核心技術實作

### 1. Repository 註冊機制

**CA102WHandler.java** 初始化時註冊 SQL：
```java
@Override
protected void init() {
    // 1. 建立動態 SQL（適配不同資料庫）
    String timestampFunc = jdbcSqlTemplate.getCurrentTimestampFunction();
    this.SQL = String.format(
        "INSERT INTO CAT102 (COMPANY_NAME, COMPANY_NO, TITLE, CONTENT, PUBLISH_DATE, EXTRACT_DATE) " +
        "VALUES (?, ?, ?, ?, ?, %s)",
        timestampFunc
    );
    
    // 2. 註冊 SQL 和資料轉換器到 Repository
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
}
```

### 2. 資料流轉架構

```
crawlWebsiteFetchData()  →  processData()  →  Repository.batchSave()
      (String[])              (CompanyNewsDTO)      (JDBC 批次寫入)
```

**職責分離**：
- ✅ Handler 負責爬取和處理
- ✅ Repository 負責儲存
- ✅ JdbcSqlTemplate 負責 JDBC 操作

### 3. 批次寫入策略

Repository 自動處理批次寫入：
- **批次大小**: 1000 筆/批次
- **錯誤處理**: 單筆失敗不影響其他資料
- **效能優化**: 批次間延遲 500ms
- **統一管理**: 所有爬蟲共用相同的批次邏輯

## 📊 資料表結構

### CAT102 欄位定義

| 欄位名稱 | 資料類型 | 說明 | 索引 |
|---------|---------|------|------|
| ID | BIGINT | 主鍵 (自動遞增) | PK |
| COMPANY_NAME | VARCHAR(200) | 公司名稱 | - |
| COMPANY_NO | VARCHAR(50) | 公司代號 | YES |
| TITLE | VARCHAR(500) | 訊息標題 | - |
| CONTENT | TEXT | HTML 內容 | - |
| PUBLISH_DATE | VARCHAR(50) | 發布日期 | YES |
| EXTRACT_DATE | DATETIME | 資料擷取時間 | YES |

### 建立資料表
```bash
mysql -u root -p < sql/create_cat102_table.sql
```

## ⚙️ 設定參數

### application.yml
```yaml
crawler:
  ca102:
    today-only: false  # true: 只爬取今日, false: 從 2016-08-25 開始
```

## 🚀 使用方式

### 1. 執行爬蟲
```java
@Autowired
private CA102WHandler ca102Handler;

public void runCrawler() {
    CrawledData crawledData = new CrawledData();
    ca102Handler.execute(crawledData);
}
```

### 2. 查詢資料
```sql
-- 查詢最新重大訊息
SELECT * FROM CAT102 
ORDER BY EXTRACT_DATE DESC 
LIMIT 10;

-- 查詢特定公司的訊息
SELECT * FROM CAT102 
WHERE COMPANY_NO = '2330' 
ORDER BY PUBLISH_DATE DESC;
```

## 📈 效能指標

- **爬取速度**: 約 1 筆/秒 (含 API 呼叫延遲)
- **批次寫入**: 1000 筆/批次
- **記憶體使用**: 約 50-100MB
- **資料庫連線**: 使用 masterDataSource (可擴展為獨立資料源)

## 🔍 資料來源 API

### 1. 列表 API
```
POST https://mops.twse.com.tw/mops/api/t05st02
Body: {"year": "113", "month": "3", "day": "28"}
```

### 2. 詳細資料 API
```
POST https://mops.twse.com.tw/mops/api/t05st02_detail
Body: {
  "companyId": "2330",
  "marketKind": "STKL",
  "enterDate": "1130328",
  "serialNumber": "123"
}
```

## 🛠️ 故障排除

### 問題1: 資料庫連線失敗
**解決方式**: 檢查 `masterDataSource` 是否正確配置

### 問題2: SQL 語法錯誤
**解決方式**: 確認資料庫類型偵測是否正確，檢查 log 中的 SQL 語句

### 問題3: API 回應 429 (Too Many Requests)
**解決方式**: 增加 API 呼叫間隔時間

## 📝 更新日誌

### 2025-03-28
- ✅ 完成 CA102WHandler 實作
- ✅ 新增資料庫類型自動偵測功能
- ✅ 實作動態 SQL 語法產生
- ✅ 新增批次寫入功能
- ✅ 建立 CAT102 資料表
- ✅ 更新 commons-lang3 至 3.18.0

## 🔗 相關文件

- [WCS132WHandler 參考實作](../cheng-crawler/src/main/java/com/cheng/crawler/handler/WCS132WHandler.java)
- [公開資訊觀測站](https://mops.twse.com.tw)

## 👥 開發團隊

- **開發者**: Cheng
- **審核者**: -
- **測試者**: -
