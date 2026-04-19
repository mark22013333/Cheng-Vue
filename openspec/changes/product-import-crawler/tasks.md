## 1. DB Migration

- [x] 1.1 建立 Flyway migration: `shop_product_sku` 新增 `barcode VARCHAR(50)` 欄位（位於 `sku_code` 之後），含索引 `idx_sku_barcode`
- [x] 1.2 建立 Flyway migration: 新建 `crawl_import_log` 表（log_id, batch_id, barcode, product_name, status, product_id, detail_url, error_message, create_time）

## 2. CSV 讀取

- [x] 2.1 建立 `ProductCsvRow` DTO（barcode, productName, spec, productCode, retailPrice, discountPrice）
- [x] 2.2 建立 `ProductCsvReader` 工具類，讀取 CSV 並解析為 `List<ProductCsvRow>`，處理含逗號的價格格式，僅保留「正常品」

## 3. 爬蟲核心

- [x] 3.1 建立 `KtunivalCrawlerService`，實作條碼搜尋方法：用 Jsoup 連接搜尋 URL（charset=Big5），解析結果數量和 productid
- [x] 3.2 實作商品詳情頁解析：從 `product.asp?productid={id}` 取得產品名稱、類別、圖片 URL、介紹
- [x] 3.3 實作圖片下載：利用 `ImageDownloadUtil` 下載圖片到 `{cheng.profile}/upload/product/` 目錄，處理 URL 中的反斜線和 Big5 中文路徑
- [x] 3.4 加入請求頻率控制（1-2 秒間隔）和失敗重試機制（最多 3 次，間隔 2 秒）

## 4. 商品寫入

- [x] 4.1 建立 `ProductImportService`，實作分類查找/建立邏輯：查詢「汽車類」category_id，按爬蟲類別查找或新建子分類
- [x] 4.2 實作 `shop_product` 寫入：title=品名, main_image=本地圖片路徑, category_id, status=DRAFT, is_new=true
- [x] 4.3 實作 `shop_product_sku` 寫入：barcode, sku_code=品號, sku_name=規格, price=零售價, original_price=零售價+50, sale_price=九折價
- [x] 4.4 實作重複匯入防護：匯入前檢查 barcode 是否已存在於 shop_product_sku

## 5. 匯入記錄

- [x] 5.1 建立 `CrawlImportLog` domain 和 `CrawlImportLogMapper`（MyBatis），支援新增和批次查詢
- [x] 5.2 在匯入流程各節點寫入 log（SUCCESS, NOT_FOUND, MULTIPLE, IMG_FAIL, ERROR, SKIPPED）

## 6. 整合與觸發

- [x] 6.1 建立 `ProductImportController` REST API（POST /api/crawler/product-import），接收 CSV 路徑參數，觸發匯入
- [x] 6.2 整合完整流程：CSV 讀取 → 逐筆爬蟲 → 圖片下載 → 分類建立 → 商品寫入 → 記錄 log
- [x] 6.3 準備本地 CSV 檔案（從 Google Sheets 匯出），執行匯入並驗證結果

## 7. SKU 欄位支援

- [x] 7.1 更新 `ShopProductSku` domain 新增 barcode 欄位
- [x] 7.2 更新 `ShopProductSkuMapper.xml` 的 insert/update/select SQL 包含 barcode

## 8. 價格對映 v2（2026-04-16）

- [x] 8.1 `ProductCsvRow` 新增 `costPrice` 欄位（BigDecimal）
- [x] 8.2 `ProductCsvReader` 讀取 `fields[5]`（定價一）到 `costPrice`
- [x] 8.3 `ProductImportServiceImpl` — 新增商品時的價格對映更新：
  - `sku.setCostPrice(row.getCostPrice())` ← 定價一
  - `sku.setPrice(row.getDiscountPrice())` ← 九折價（售價）
  - `sku.setOriginalPrice(row.getRetailPrice())` ← 零售價（原價）
  - `sku.setSalePrice(null)` ← 不設特惠價
  - 移除 `retailPrice.add(new BigDecimal("50"))` 硬編碼
  - product 層同步上述對映
- [x] 8.4 `ProductImportServiceImpl` — 更新已存在商品時（updateExistingProduct）同步修改價格對映
- [x] 8.5 價格防呆：costPrice > price 時記錄 ERROR 並跳過該筆
