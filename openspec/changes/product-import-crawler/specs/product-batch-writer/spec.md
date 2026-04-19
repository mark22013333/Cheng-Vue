## ADDED Requirements

### Requirement: 自動建立商品分類
系統 SHALL 根據爬蟲取得的「產品類別」名稱，查找或建立 `shop_category`。所有新分類 SHALL 建立為「汽車類」的子分類。

#### Scenario: 分類已存在
- **WHEN** 爬蟲取得類別 "化工用品類" 且 shop_category 已存在此名稱（在「汽車類」下）
- **THEN** 使用現有分類的 category_id

#### Scenario: 分類不存在
- **WHEN** 爬蟲取得類別 "洗車工具類" 且 shop_category 不存在此名稱
- **THEN** 系統在「汽車類」下新建子分類 "洗車工具類"，使用其 category_id

### Requirement: 寫入商品主表
系統 SHALL 將合併後的資料寫入 `shop_product`，欄位對映如下：
- `title`: CSV 品名
- `main_image`: 下載後的本地圖片路徑（爬蟲取得）
- `description`: 爬蟲取得的產品介紹
- `category_id`: 查找/建立的分類 ID
- `status`: DRAFT（預設）
- `is_new`: true

#### Scenario: 成功建立商品
- **WHEN** CSV + 爬蟲資料完整
- **THEN** 建立 shop_product 記錄，狀態為 DRAFT，is_new=true

#### Scenario: 圖片缺失仍建立商品
- **WHEN** 圖片下載失敗但其他資料完整
- **THEN** 建立 shop_product 記錄，main_image 為空字串

### Requirement: 寫入商品 SKU（v2 — 2026-04-16 更新）
系統 SHALL 為每筆商品建立一個 `shop_product_sku` 記錄，欄位對映如下：
- `barcode`: CSV 條碼編號
- `sku_code`: CSV 品號
- `sku_name`: CSV 規格
- `cost_price`: CSV 定價一（成本價）
- `price`: CSV 九折價（售價）
- `original_price`: CSV 零售價（原價/劃線價）
- `sale_price`: null（不設特惠價，方案 A）

#### Scenario: 成功建立 SKU
- **WHEN** 商品主表建立成功
- **THEN** 建立對應的 shop_product_sku，costPrice=定價一、price=九折價、originalPrice=零售價、salePrice=null

#### Scenario: 價格防呆 — costPrice > price
- **WHEN** 定價一（成本價）大於九折價（售價）
- **THEN** 系統記錄 ERROR，跳過該筆商品，不寫入

### Requirement: 重複匯入防護
系統 SHALL 在匯入前檢查 `shop_product_sku.barcode` 是否已存在，已存在的商品跳過匯入。

#### Scenario: 條碼已存在（Upsert 更新）
- **WHEN** barcode "4975163325016" 已存在於 shop_product_sku
- **THEN** 更新已存在商品的價格（costPrice、price、originalPrice），不重新爬蟲

#### Scenario: 條碼不存在
- **WHEN** barcode 不存在於 shop_product_sku
- **THEN** 正常執行匯入流程
