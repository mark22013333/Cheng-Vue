## Why

`product-sku-master-detail` 完成後，使用者在 Step 2「定價與促銷」設定商品特惠價時遇到驗證誤報：

- 商品有 3 個 SKU（$145 / $220 / $120），自動模式下 `form.price = min(SKU) = $120`
- 使用者輸入特惠價 $131，意圖是讓 $145 和 $220 的 SKU 有折扣感
- 系統拒絕：「特惠價須低於商品售價」（因 $131 > $120）

這不是單純的驗證規則鬆緊問題，而是**語意層級錯置**：

1. **商品層特惠價對多規格商品無意義** — 每個 SKU 規格不同、成本不同、市場定位不同，共用一個特惠價既不靈活也會造成虧損（$120 的 SKU 被套上 $131 的「特價」反而漲價了）。
2. **前端 admin 從未暴露 SKU 級 salePrice** — 後端 Domain、Mapper、DB 早在 V38 就已支援 `shop_product_sku.sale_price`，但 `ProductPricingSection.vue` 只編輯 `form.salePrice`（商品層），SKU 表格中 salePrice 欄位完全缺席。
3. **後端兩條定價路徑不一致**：
   - `ShopPriceService.calculateSkuPrice()`：SKU 無 salePrice 時**不 fallback 商品特價**，只套全站折扣
   - `ShopCheckoutServiceImpl.calculateCartItemPrice()`：SKU 無 salePrice 時**fallback 商品特價**
   - 這導致商品詳情頁顯示的價格可能與結帳時不同

## What Changes

### 核心語意轉換

> **特惠價的編輯入口從商品層移到 SKU 層，product.salePrice 改為自動推導的計算欄位。**

每個 `ShopProductSku` 獨立擁有 `salePrice`（+ 可選 `saleEndDate`），使用者在 admin 逐 SKU 設定。`shop_product.sale_price` / `sale_end_date` 改由 `syncPriceFromSkus()` 自動推導為 min(active SKU salePrice) / max(active SKU saleEndDate)，保持列表頁和 SEO 顯示的向前相容。

### 前端 admin

1. **移除** `ProductPricingSection.vue` 中的「特惠價」、「特價結束時間」兩個商品層表單欄位
2. **新增** SKU 個別售價表格中的 `salePrice` 和 `saleEndDate` 欄位
3. **移除** `useProductForm.salePriceValidator`（商品層驗證）
4. **新增** SKU 層驗證：每個 SKU 的 `salePrice`（若設定）必須 < 該 SKU 的 `price`
5. **新增** `SkuCard.vue` 右側編輯區的 salePrice + saleEndDate 欄位
6. **更新** summary 顯示邏輯：改為顯示 SKU 特惠價範圍

### 後端

1. **擴充** `syncPriceFromSkus()`：從 SKU 推導 `product.salePrice = min(active SKU salePrice)` 和 `product.saleEndDate = max(active SKU saleEndDate)`
2. **統一** `ShopPriceService.calculateSkuPrice()` 和 `ShopCheckoutServiceImpl.calculateCartItemPrice()` 的 fallback 邏輯：SKU 無 salePrice → 不套特價（統一為不 fallback）
3. **移除** `ShopCheckoutServiceImpl` 對 `productSalePrice` 的 fallback 路徑

### DB 遷移（V50）

1. **新增** `shop_product_sku.sale_end_date DATETIME DEFAULT NULL`
2. **資料下推** 將 `shop_product` 上既有的 `sale_price` / `sale_end_date` 推到所有 enabled SKU（僅 SKU 自身 salePrice 為 NULL 時）

## Impact

### New Capabilities

- `sku-sale-price-edit`：SKU 個別特惠價編輯與驗證
- `sku-sale-end-date`：SKU 個別特價到期時間

### Modified Capabilities

- `product-sale-price-sync`：product.salePrice 改為由 SKU 推導的計算欄位
- `pricing-section-ui`：移除商品層特惠價表單欄位，新增 SKU 欄位
- `checkout-price-calculation`：統一 SKU 級優先、無 fallback 到商品層的計算邏輯

### 檔案異動

**前端（cheng-ui）：**

- **修改** `src/views/shop/product/components/ProductPricingSection.vue`：移除商品層 salePrice/saleEndDate 表單；SKU 表格新增 salePrice/saleEndDate 欄位
- **修改** `src/views/shop/product/components/SkuCard.vue`：新增 salePrice + saleEndDate 編輯欄位
- **修改** `src/views/shop/product/composables/useProductForm.js`：移除 salePriceValidator；新增 SKU 層 salePrice 驗證；addSku 初始化 salePrice/saleEndDate；更新 summary
- **修改** `src/views/shop/product/index.vue`：列表頁特惠價顯示邏輯微調（product.salePrice 仍由後端推導）

**後端（cheng-shop）：**

- **修改** `ShopProductSku.java`：新增 `saleEndDate` 欄位
- **修改** `ShopProductSkuMapper.xml`：select/insert/update 增加 `sale_end_date`
- **修改** `ShopProductServiceImpl.syncPriceFromSkus()`：推導 salePrice + saleEndDate
- **修改** `ShopPriceService.calculateSkuPrice()`：使用 SKU 自身的 saleEndDate
- **修改** `ShopCheckoutServiceImpl.calculateCartItemPrice()`：移除 productSalePrice fallback

**DB（cheng-admin）：**

- **新增** `db/migration/V50__sku_sale_end_date.sql`：新增欄位 + 資料下推

### 相容性

- `shop_product.sale_price` / `sale_end_date` 欄位保留（改為 derived），列表頁 API 向前相容
- 前端商店 `detail.vue` 的 `effectiveSalePrice` 邏輯（SKU 優先 → product fallback）自然相容
- 購物車 `ShopCart.skuSalePrice` 已存在，結帳路徑無需更動查詢
- 既有商品的 salePrice 透過 V50 遷移下推到 SKU，遷移後的第一次儲存會重新推導 product 層
