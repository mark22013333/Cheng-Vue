## ADDED Requirements

### Requirement: 匯入記錄表
系統 SHALL 建立 `crawl_import_log` 資料表，記錄每筆商品的匯入狀態。同一批次匯入共用一個 batch_id。

#### Scenario: 成功匯入記錄
- **WHEN** 商品成功匯入
- **THEN** 記錄 status=SUCCESS，包含 barcode、product_name、product_id、detail_url

#### Scenario: 搜尋無結果記錄
- **WHEN** 條碼搜尋不到商品
- **THEN** 記錄 status=NOT_FOUND，包含 barcode、product_name

#### Scenario: 搜尋多筆記錄
- **WHEN** 條碼搜尋到多筆商品
- **THEN** 記錄 status=MULTIPLE，包含 barcode、product_name、error_message 記錄搜尋到的數量

#### Scenario: 圖片下載失敗記錄
- **WHEN** 商品圖片下載失敗
- **THEN** 記錄 status=IMG_FAIL，包含 barcode、product_name、error_message

#### Scenario: 其他錯誤記錄
- **WHEN** 匯入過程中發生預期外的錯誤
- **THEN** 記錄 status=ERROR，包含 barcode、product_name、error_message

### Requirement: shop_product_sku 新增 barcode 欄位
系統 SHALL 在 `shop_product_sku` 表新增 `barcode VARCHAR(50)` 欄位，用於儲存國際條碼（EAN）。

#### Scenario: Flyway migration 新增欄位
- **WHEN** 執行 Flyway migration
- **THEN** `shop_product_sku` 表新增 `barcode` 欄位，位於 `sku_code` 之後，含索引 `idx_sku_barcode`
