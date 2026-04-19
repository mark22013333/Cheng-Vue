## Context

商城後台 (`cheng-shop`) 目前商品需手動逐筆新增。現有 ~120 筆 PROSTAFF 汽車清潔用品資料在 Google Sheets 試算表中，供應商網站 ktunival.com.tw 有對應的商品圖片和分類。

現有基礎設施：
- `cheng-crawler` 模組：已有 `CrawlerHandler` 模板模式（支援批次和單筆模式）、`ImageDownloadUtil`、`JdbcSqlTemplate`
- `cheng-shop` 模組：`shop_product` + `shop_product_sku` 資料表、完整的 CRUD API
- Jsoup、OkHttp 已在 cheng-crawler 的 pom.xml 中
- 圖片路徑：`cheng.profile`（local: `/Users/cheng/uploadPath`）

ktunival.com.tw 特性：
- Big5 編碼的 ASP 網站，純靜態 HTML（不需 Selenium）
- 搜尋 URL: `show_Prdouct_3_1.asp?skeyword={barcode}&Submit22=%B0%e%A5%X`
- 詳情 URL: `product.asp?productid={id}`
- 圖片 URL: `product_images/PROSTAFF\{category}\{filename}.jpg`（需 URL encode）

## Goals / Non-Goals

**Goals:**
- 一次性批量匯入 ~120 筆商品到 shop_product + shop_product_sku
- 自動從供應商網站爬取商品圖片和分類
- 圖片下載到本地，路徑寫入商品主圖欄位
- 完整的匯入記錄（crawl_import_log）追蹤每筆狀態
- 自動建立不存在的商品分類（作為「汽車類」子分類）

**Non-Goals:**
- 不做持續同步（一次性匯入）
- 不做前端匯入 UI（透過後端 API 或排程觸發）
- 不處理庫存同步（商品匯入後庫存量由管理員手動管理）
- ~~不存入成本價（定價一）~~ → v2 改為：定價一寫入 costPrice

## Decisions

### 1. 不繼承 CrawlerHandler，獨立 Service

**選擇：** 建立獨立的 `ProductImportService`，不繼承 `CrawlerHandler<R, P>`

**原因：**
- `CrawlerHandler` 設計為 Selenium 驅動的爬蟲（模板中有 `createWebDriver()`），本次用 Jsoup + OkHttp 即可
- 匯入流程是「CSV 讀取 → 爬蟲補充 → 寫入商品」的複合流程，不是純爬蟲
- 但仍放在 `cheng-crawler` 模組，複用 `ImageDownloadUtil` 和 `JdbcSqlTemplate`

**替代方案：** 繼承 `CrawlerHandler` 並覆寫 `createWebDriver()` 返回 null → 不夠乾淨，語義不符

### 2. CSV 本地檔案匯入

**選擇：** 將 Google Sheets 匯出為 CSV 放在本地，程式讀取本地 CSV

**原因：**
- 一次性匯入不需要即時連接 Google API
- 避免 Google Sheets API 的 OAuth 複雜度
- CSV 路徑可透過 API 參數或配置指定

### 3. Jsoup 直連（不經 Selenium）

**選擇：** 用 Jsoup 的 `Jsoup.connect(url).get()` 直接取得 HTML

**原因：**
- 目標網站為純靜態 ASP 頁面，無 JavaScript 渲染
- Jsoup 支援指定 charset（Big5）
- 比 Selenium 快 10 倍以上，120 筆商品預估 5-10 分鐘完成

### 4. 匯入流程設計

```
CSV 讀取 (120 筆)
    │
    ▼ for each row
┌─────────────────────────────┐
│ 1. 用條碼搜尋 ktunival      │ GET show_Prdouct_3_1.asp?skeyword={barcode}
│    解析搜尋結果頁            │ 
│    ├── 0 筆 → LOG (NOT_FOUND)│
│    ├── 1 筆 → 取得 productid │
│    └── 多筆 → LOG (MULTIPLE) │
├─────────────────────────────┤
│ 2. 爬取商品詳情頁            │ GET product.asp?productid={id}
│    解析：產品名稱、類別、     │ Jsoup parse, charset=Big5
│          圖片URL、介紹       │
├─────────────────────────────┤
│ 3. 下載商品圖片              │ ImageDownloadUtil → uploadPath/product/{timestamp}.jpg
│    ├── 成功 → 記錄本地路徑   │
│    └── 失敗 → LOG (IMG_FAIL) │
├─────────────────────────────┤
│ 4. 查找/建立分類             │ shop_category: 汽車類 > {爬蟲類別}
├─────────────────────────────┤
│ 5. 寫入 shop_product         │ title, main_image, price, original_price, sale_price
│    寫入 shop_product_sku     │ barcode, sku_code, sku_name, price
│    LOG (SUCCESS)             │
└─────────────────────────────┘
    │
    ▼ sleep 1-2s
    下一筆
```

### 5. crawl_import_log 表設計

| 欄位 | 類型 | 說明 |
|------|------|------|
| log_id | BIGINT PK | 主鍵 |
| batch_id | VARCHAR(50) | 批次 ID（同一次匯入共用） |
| barcode | VARCHAR(50) | 條碼 |
| product_name | VARCHAR(200) | 品名（來自 CSV）|
| status | VARCHAR(20) | SUCCESS / NOT_FOUND / MULTIPLE / IMG_FAIL / ERROR |
| product_id | BIGINT | 成功時關聯的商品 ID |
| detail_url | VARCHAR(500) | 爬取的詳情頁 URL |
| error_message | VARCHAR(500) | 錯誤訊息 |
| create_time | DATETIME | 記錄時間 |

### 6. 價格對映（v2 — 2026-04-16 更新）

> 商品新增流程的價格結構已調整，定價一為成本價、零售價為原價、九折價為售價。

| CSV 欄位 | 目標 | 說明 |
|----------|------|------|
| 定價一 | `shop_product_sku.cost_price` | 成本價（進貨成本） |
| 零售價 | `shop_product_sku.original_price` | 原價（劃線價） |
| 九折價 | `shop_product_sku.price` | 售價（消費者實際付款價） |
| —      | `shop_product_sku.sale_price` | 不設定（null），未來可手動設定特惠價 |
| 定價一 | `shop_product.cost_price`（不存在，僅 SKU 層級） | — |
| 零售價 | `shop_product.original_price` | 同步自 SKU |
| 九折價 | `shop_product.price` | 同步自 SKU |
| —      | `shop_product.sale_price` | 不設定（null） |
| 品號   | `shop_product_sku.sku_code` | 直接寫入 |
| 條碼   | `shop_product_sku.barcode`（新欄位）| 直接寫入 |
| 規格   | `shop_product_sku.sku_name` | 直接寫入 |

#### 價格防呆（沿用既有規則）

- `costPrice > price` → 硬阻擋（成本高於售價）
- `MARKUP_RATIO = 1.2`：若 `price` 為 null，以 `costPrice × 1.2` 推算

#### v1 → v2 差異

| 欄位 | v1（舊） | v2（新） |
|------|---------|---------|
| `cost_price` | 不存入 | 定價一 |
| `price` | 零售價 | 九折價 |
| `original_price` | 零售價 + 50（硬編碼） | 零售價（直接寫入，不加成） |
| `sale_price` | 九折價 | null（方案 A：不設特惠價） |

## Risks / Trade-offs

- **[網站不穩定]** → ktunival.com.tw 是老舊 ASP 網站，可能不穩定 → 加入重試機制（最多 3 次）和錯誤記錄
- **[Big5 編碼亂碼]** → Jsoup 指定 charset("Big5")，圖片 URL 中的中文路徑需要正確 encode → 測試時優先驗證
- **[圖片 URL 含反斜線]** → `product_images/PROSTAFF\化工用品類\0013.jpg` 中的 `\` 需轉為 `/` 或正確 URL encode
- **[請求頻率過高]** → 每次請求間隔 1-2 秒，120 筆約 5-10 分鐘 → 可接受
- **[重複匯入]** → 用 barcode 做唯一性檢查，已存在的跳過並記錄 → 防止重複執行
