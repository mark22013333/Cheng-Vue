# 任務清單

## Phase 1: DB 遷移與 Domain

- [x] T1: 建立 `V50__sku_sale_end_date.sql`
  - [x] `ALTER TABLE shop_product_sku ADD COLUMN sale_end_date`
  - [x] `UPDATE JOIN` 資料下推
  - [x] `ADD INDEX idx_sku_sale_end_date`
- [x] T2: `ShopProductSku.java` 新增 `saleEndDate` 欄位（含 `@JsonFormat`）
- [x] T3: `ShopProductSkuMapper.xml` 更新
  - [x] resultMap 新增 `sale_end_date`
  - [x] `selectSkuVo` SQL 新增 `s.sale_end_date`
  - [x] `insertSku` 新增 saleEndDate
  - [x] `batchInsertSku` 新增 saleEndDate
  - [x] `updateSku` 新增 saleEndDate
- [x] T4: `ShopCart.java` 新增 `skuSaleEndDate` 欄位
- [x] T5: `ShopCartMapper.xml` 更新
  - [x] resultMap 新增 `sku_sale_end_date`
  - [x] selectCartVo JOIN 新增 `s.sale_end_date as sku_sale_end_date`

## Phase 2: 後端服務重構

- [x] T6: `ShopPriceService.calculateSkuPrice()` 重構
  - [x] 移除 `ShopProduct product` 參數
  - [x] 改用 `sku.getSaleEndDate()`
- [x] T7: `ShopPriceService.enrichSkuPrices()` 更新簽名
  - [x] 移除 `ShopProduct product` 參數
  - [x] 更新 `ShopFrontController.getProductSkus()` 呼叫端
- [x] T8: `ShopCheckoutServiceImpl.calculateCartItemPrice()` 重構
  - [x] 移除 productSalePrice fallback
  - [x] 委託 `ShopPriceService.calculateSkuPrice()`
  - [x] 使用 `skuSaleEndDate` 取代 `productSaleEndDate`
- [x] T9: `ShopProductServiceImpl.syncPriceFromSkus()` 擴充
  - [x] 推導 `product.salePrice = min(active SKU salePrice)`
  - [x] 推導 `product.saleEndDate = max(active SKU saleEndDate)`
  - [x] 過期 SKU 特惠不納入推導

## Phase 3: 前端 — useProductForm.js 重構

- [x] T10: 移除商品層特惠價相關邏輯
  - [x] 移除 `salePriceValidator()`
  - [x] 移除 `formRules.salePrice`
  - [x] 移除 `createDefaultForm()` 中的 `salePrice` / `saleEndDate`
  - [x] 移除 `validateStep(2)` 中的 `salePrice` 欄位
  - [x] 清理 `sectionDirty.pricing` 中的 `salePrice` / `saleEndDate`
- [x] T11: 新增 SKU 層特惠價邏輯
  - [x] `addSku()` 初始化 `salePrice: undefined` / `saleEndDate: undefined`
  - [x] `validateSkuList()` 新增 S-SP1 ~ S-SP3 規則
  - [x] `mapFieldToSection()` 新增 `saleEndDate: 'pricing'`
- [x] T12: 更新 summary 顯示邏輯（SKU 特惠價範圍）
- [x] T13: 更新 `sectionDirty.pricing` 新增 SKU salePrice/saleEndDate 變更偵測

## Phase 4: 前端 — UI 元件修改

- [x] T14: `SkuCard.vue` 新增特惠價欄位
  - [x] 在 `showPrice` 區塊新增 salePrice + saleEndDate
  - [x] 省多少的計算提示
  - [x] fieldError 支援 `salePrice` / `saleEndDate`
- [x] T15: `SkuListItem.vue` 新增特惠價標籤
  - [x] `__row2` 新增特惠價 tag
- [x] T16: `ProductPricingSection.vue` 重構促銷區塊
  - [x] 移除 salePrice / saleEndDate 表單欄位
  - [x] 新增促銷摘要區塊（SKU 特惠價範圍）
  - [x] SKU 表格新增 salePrice 唯讀欄位
  - [x] 新增 `salePriceSummary` prop

## Phase 5: 前端 — Shop 商店頁面

- [x] T17: `detail.vue` 更新 effectiveSalePrice
  - [x] 移除 product.salePrice fallback
  - [x] saleEndTs 優先使用 SKU saleEndDate
- [x] T18: 確認商品卡片（列表頁）不受影響
  - [x] product.salePrice / finalPrice 仍由 enrichProductPrices 填充
  - [x] isSaleActive() 邏輯不動

## Phase 6: 驗證與建置

- [x] T19: 前端 `pnpm run build` 確認無錯誤
- [x] T20: 後端 `mvn compile` 確認無錯誤
- [x] T21: 手動測試驗證案例表（8 個場景）
  - [x] 場景 1: 全部有特惠 — product.salePrice = min(100,80,90) = 80 ✅
  - [x] 場景 2: 部分有特惠 — product.salePrice = 100（唯一有特惠的 SKU）✅
  - [x] 場景 3: 無特惠 — product.salePrice = null ✅（修正 MyBatis 條件更新 bug）
  - [x] 場景 4: 特惠 >= 售價 — S-SP2 前端驗證攔截 ✅
  - [x] 場景 5: 負數特惠 — S-SP1 前端驗證攔截 ✅
  - [x] 場景 6: 有結束時間無特惠 — S-SP3 前端驗證攔截 ✅
  - [x] 場景 7: 特惠已過期 — 儲存成功，product.salePrice = null（過期不計入）✅
  - [x] 場景 8: 長期特惠 — saleEndDate=null，product.salePrice = min(90,85) = 85 ✅
