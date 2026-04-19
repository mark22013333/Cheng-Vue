## Why

目前商城後台的商品需要手動逐筆新增，效率低。現有一份 Google Sheets 試算表包含 ~120 筆 PROSTAFF 汽車清潔用品（含條碼、品名、規格、價格），且供應商網站 ktunival.com.tw 有對應的商品圖片和分類資訊。需要建立一次性的批量匯入流程，透過爬蟲自動取得圖片和分類後寫入商城。

## What Changes

- 新增 `shop_product_sku.barcode` 欄位，儲存國際條碼（EAN）
- 新建 `crawl_import_log` 資料表，記錄爬蟲匯入的每筆結果（成功/零筆/多筆/圖片失敗）
- 在 `cheng-crawler` 模組新增 ktunival.com.tw 爬蟲（OkHttp + Jsoup，Big5 編碼）
- 讀取 CSV 的條碼 → 搜尋供應商網站 → 解析詳情頁取得圖片和分類 → 下載圖片到本地 → 寫入 `shop_product` + `shop_product_sku`
- 自動建立商品分類：以爬蟲取得的「產品類別」作為「汽車類」的子分類
- 價格對映（v2）：定價一→成本價、零售價→原價（劃線價）、九折價→售價、特惠價不設
- 匯入商品預設狀態為 DRAFT

## Capabilities

### New Capabilities
- `product-csv-import`: 從 CSV 讀取試算表資料，解析條碼、品名、規格、品號、價格欄位
- `ktunival-crawler`: 用條碼搜尋 ktunival.com.tw，解析搜尋頁和詳情頁，取得商品圖片、分類、介紹
- `crawl-import-log`: 爬蟲匯入記錄表，追蹤每筆條碼的匯入狀態和錯誤原因
- `product-batch-writer`: 將 CSV + 爬蟲資料合併後批量寫入 shop_product、shop_product_sku，自動建立缺失分類

### Modified Capabilities

## Impact

- **DB**: `shop_product_sku` 新增欄位、新建 `crawl_import_log` 表（Flyway migration）
- **cheng-crawler 模組**: 新增爬蟲 Handler、Service、DTO
- **cheng-shop 模組**: 可能需要調整 SKU 相關的 domain/mapper 以支援 barcode 欄位
- **依賴**: 需要 Jsoup（HTML 解析），確認 pom.xml 是否已有
- **外部系統**: ktunival.com.tw（需控制請求頻率，每次間隔 1-2 秒）
- **檔案系統**: 圖片下載至 `cheng.profile` 路徑
