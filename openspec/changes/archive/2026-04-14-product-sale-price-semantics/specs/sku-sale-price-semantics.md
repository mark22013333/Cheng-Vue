# 規格書：SKU 級特惠價語意重構

## 目標

將特惠價（salePrice + saleEndDate）的「真相來源」從 `shop_product` 遷移至 `shop_product_sku`，使每個規格可獨立設定促銷價格與到期時間。

---

## 1. 資料模型變更

### 1.1 DB — V50 遷移

```sql
-- shop_product_sku 新增 sale_end_date
ALTER TABLE shop_product_sku
  ADD COLUMN sale_end_date DATETIME DEFAULT NULL
  COMMENT '特價結束時間（NULL 表示長期）' AFTER sale_price;

-- 資料下推（product → SKU）
UPDATE shop_product_sku sku
  INNER JOIN shop_product p ON sku.product_id = p.product_id
SET sku.sale_price    = p.sale_price,
    sku.sale_end_date = p.sale_end_date
WHERE p.sale_price IS NOT NULL
  AND p.sale_price > 0
  AND sku.sale_price IS NULL
  AND sku.status IN ('ENABLED', '1');

-- 索引
ALTER TABLE shop_product_sku
  ADD INDEX idx_sku_sale_end_date (sale_end_date);
```

### 1.2 Domain — ShopProductSku.java

新增欄位：

```java
/**
 * 特價結束時間（NULL 表示長期）
 */
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private Date saleEndDate;
```

### 1.3 Mapper — ShopProductSkuMapper.xml

| 位置 | 變更 |
|------|------|
| `resultMap` | 新增 `<result property="saleEndDate" column="sale_end_date"/>` |
| `selectSkuVo` | SQL 新增 `s.sale_end_date` |
| `insertSku` | 新增 `<if test="saleEndDate != null">sale_end_date,</if>` |
| `batchInsertSku` | values 新增 `#{item.saleEndDate}` |
| `updateSku` | 新增 `<if test="saleEndDate != null">sale_end_date = #{saleEndDate},</if>` |

---

## 2. 後端服務變更

### 2.1 ShopProductServiceImpl.syncPriceFromSkus()

```java
// 現有：推導 price / originalPrice
// 新增：推導 salePrice / saleEndDate

BigDecimal minSalePrice = skuList.stream()
    .filter(sku -> sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
    .filter(sku -> {
        // 只計算未過期的 SKU 特惠價
        return sku.getSaleEndDate() == null || sku.getSaleEndDate().after(new Date());
    })
    .map(ShopProductSku::getSalePrice)
    .min(BigDecimal::compareTo)
    .orElse(null);

Date maxSaleEndDate = skuList.stream()
    .filter(sku -> sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
    .map(ShopProductSku::getSaleEndDate)
    .filter(Objects::nonNull)
    .max(Date::compareTo)
    .orElse(null);

product.setSalePrice(minSalePrice);
product.setSaleEndDate(maxSaleEndDate);
```

### 2.2 ShopPriceService.calculateSkuPrice()

**Before**:
```java
public PriceResult calculateSkuPrice(ShopProductSku sku, ShopProduct product) {
    if (sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
        Date saleEndDate = product != null ? product.getSaleEndDate() : null;  // ← 讀 product
        ...
    }
    return calculatePrice(sku.getPrice(), null, null);
}
```

**After**:
```java
public PriceResult calculateSkuPrice(ShopProductSku sku) {
    if (sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
        boolean notExpired = sku.getSaleEndDate() == null || sku.getSaleEndDate().after(new Date());
        if (notExpired) {
            BigDecimal savings = sku.getPrice().subtract(sku.getSalePrice());
            if (savings.compareTo(BigDecimal.ZERO) > 0) {
                String label = "特價 -$" + savings.stripTrailingZeros().toPlainString();
                return PriceResult.of(sku.getSalePrice(), label, sku.getPrice());
            }
        }
    }
    return calculatePrice(sku.getPrice(), null, null);
}
```

**變更重點**：
- 方法簽名去掉 `ShopProduct product` 參數（SKU 自帶 saleEndDate）
- 使用 `sku.getSaleEndDate()` 而非 `product.getSaleEndDate()`
- 需同步更新 `enrichSkuPrices()` 的呼叫方

### 2.3 ShopPriceService.enrichSkuPrices()

```java
// Before: enrichSkuPrices(List<ShopProductSku> skus, ShopProduct product)
// After:  enrichSkuPrices(List<ShopProductSku> skus)
public void enrichSkuPrices(List<ShopProductSku> skus) {
    if (skus == null || skus.isEmpty()) return;
    for (ShopProductSku sku : skus) {
        PriceResult result = calculateSkuPrice(sku);
        sku.setFinalPrice(result.getFinalPrice());
        sku.setDiscountLabel(result.getDiscountLabel());
        sku.setOriginalDisplayPrice(result.getOriginalDisplayPrice());
    }
}
```

呼叫端 `ShopFrontController.getProductSkus()` 相應更新：
```java
// Before: priceService.enrichSkuPrices(list, product);
// After:  priceService.enrichSkuPrices(list);
```

### 2.4 ShopCheckoutServiceImpl.calculateCartItemPrice()

**消除重複邏輯**，改為委託 `ShopPriceService`：

```java
private BigDecimal calculateCartItemPrice(ShopCart cartItem) {
    ShopProductSku virtualSku = new ShopProductSku();
    virtualSku.setPrice(cartItem.getPrice());
    virtualSku.setSalePrice(cartItem.getSkuSalePrice());
    virtualSku.setSaleEndDate(cartItem.getSkuSaleEndDate());  // 新欄位

    PriceResult result = priceService.calculateSkuPrice(virtualSku);
    return result.getFinalPrice();
}
```

**注意**：`ShopCart` 的查詢需更新，JOIN `shop_product_sku.sale_end_date as sku_sale_end_date`。

### 2.5 ShopCart Domain + Mapper 變更

`ShopCart.java` 新增：
```java
private Date skuSaleEndDate;
```

`ShopCartMapper.xml` 更新：
- `resultMap` 新增 `<result property="skuSaleEndDate" column="sku_sale_end_date"/>`
- `selectCartVo` SQL 新增 `s.sale_end_date as sku_sale_end_date`
- 可移除 `productSalePrice` / `productSaleEndDate`（不再 fallback）

---

## 3. 前端 Admin 變更

### 3.1 useProductForm.js

#### 移除

- `salePriceValidator()` 函式（L163-175）
- `formRules.salePrice` 驗證規則（L195-197）
- `createDefaultForm()` 中的 `salePrice` / `saleEndDate`（L718-719）
- `sectionDirty.pricing` 中的 `salePrice` / `saleEndDate` 監聽
- `validateStep(2)` 中的 `salePrice` 欄位驗證

#### 新增

- `addSku()` 初始化 `salePrice: undefined` + `saleEndDate: undefined`
- `validateSkuList()` 新增 S-SP1 ~ S-SP3 驗證規則：

```js
// S-SP1: 特惠價非負
if (sku.salePrice != null && sku.salePrice < 0) {
  errors.push({ index, field: 'salePrice', message: '特惠價不可為負數' })
}
// S-SP2: 特惠價 < 售價
if (sku.salePrice != null && sku.price != null && sku.salePrice >= sku.price) {
  errors.push({ index, field: 'salePrice', message: '特惠價須低於該規格售價' })
}
// S-SP3: 有到期時間但沒特惠價
if (sku.saleEndDate != null && (sku.salePrice == null || sku.salePrice === '')) {
  errors.push({ index, field: 'saleEndDate', message: '設定特價結束時間前須先填寫特惠價' })
}
```

#### 更新

- `summary.pricing` 邏輯：

```js
// 改為 SKU 級特惠摘要
const salePrices = skuList.value
  .filter(s => s.salePrice != null && s.salePrice > 0)
  .map(s => Number(s.salePrice))
if (salePrices.length > 0) {
  const min = Math.min(...salePrices)
  const max = Math.max(...salePrices)
  pricing += min === max
    ? ` 特惠 $${min}`
    : ` 特惠 $${min}~$${max}`
}
```

- `sectionDirty.pricing` 新增 SKU salePrice / saleEndDate 變更偵測

### 3.2 ProductPricingSection.vue

#### 移除

```html
<!-- 移除 -->
<el-form-item label="特惠價" prop="salePrice">...</el-form-item>
<el-form-item label="特價結束時間" prop="saleEndDate">...</el-form-item>
<span v-if="form.salePrice > 0" class="sale-price-hint">...</span>
```

#### 新增

促銷摘要區塊（替代原有表單欄位）：

```html
<div class="pricing-section__promo">
  <h4 class="pricing-section__subtitle">促銷設定</h4>
  <div v-if="salePriceSummary" class="promo-summary">
    {{ salePriceSummary }}
  </div>
  <div v-else class="promo-summary promo-summary--empty">
    尚未設定 SKU 特惠價，請在「規格與庫存」步驟中為各規格設定特惠價
  </div>
</div>
```

新增 prop: `salePriceSummary: String`

#### SKU 表格新增 salePrice 欄位

在 el-table 中新增：

```html
<el-table-column prop="salePrice" label="特惠價" width="120">
  <template #default="scope">
    <span v-if="scope.row.salePrice > 0" class="price sale">${{ scope.row.salePrice }}</span>
    <span v-else class="price-empty">—</span>
  </template>
</el-table-column>
```

### 3.3 SkuCard.vue

在 `showPrice` 區塊中新增特惠價欄位：

```html
<!-- 現有 -->
<el-row :gutter="12">
  <el-col :span="8">售價</el-col>
  <el-col :span="8">原價</el-col>
  <el-col :span="8">成本價</el-col>
</el-row>

<!-- 新增 -->
<el-row :gutter="12">
  <el-col :span="12">
    <el-form-item label="特惠價" :error="fieldError('salePrice')">
      <el-input-number v-model="sku.salePrice" :min="0" :precision="0"
        controls-position="right" style="width: 100%" placeholder="選填" />
      <div v-if="sku.salePrice > 0 && sku.price > 0" class="sku-card__ref">
        省 ${{ sku.price - sku.salePrice }}
      </div>
    </el-form-item>
  </el-col>
  <el-col :span="12">
    <el-form-item label="特價結束時間" :error="fieldError('saleEndDate')">
      <el-date-picker v-model="sku.saleEndDate" type="datetime"
        placeholder="留空表示長期" style="width: 100%"
        value-format="YYYY-MM-DD HH:mm:ss" />
    </el-form-item>
  </el-col>
</el-row>
```

**注意**：此區塊在 create.vue 的 Step 1（規格與庫存）中 `showPrice=false` 時不顯示，在 Step 2（定價與促銷）和 edit.vue 中 `showPrice=true` 時顯示。

### 3.4 SkuListItem.vue

在 `__row2` 新增特惠價標籤（顯示在售價旁邊）：

```html
<span v-if="sku.salePrice > 0" class="sku-list-item__sale-tag">
  特 ${{ sku.salePrice }}
</span>
```

---

## 4. 前端 Shop 商店變更

### 4.1 detail.vue

```js
// Before
const effectiveSalePrice = computed(() => {
  if (selectedSku.value?.salePrice != null) {
    return Number(selectedSku.value.salePrice || 0)
  }
  return Number(product.value?.salePrice || 0)
})

// After — 移除 product fallback
const effectiveSalePrice = computed(() => {
  return Number(selectedSku.value?.salePrice || 0)
})

// Before
const saleEndTs = computed(() => parseDateTimeString(product.value?.saleEndDate))

// After — 使用 SKU 自身的 saleEndDate
const saleEndTs = computed(() =>
  parseDateTimeString(selectedSku.value?.saleEndDate || product.value?.saleEndDate)
)
```

**說明**：`saleEndTs` 保留 product fallback 作為安全網（應對遷移期間少數 SKU 尚未被使用者存檔重新推導的情境），但 `effectiveSalePrice` 不 fallback，確保語意正確。

---

## 5. 驗證案例表

| 場景 | SKU A | SKU B | SKU C | 期望結果 |
|------|-------|-------|-------|---------|
| 1: 全部有特惠 | price=145, salePrice=120 | price=220, salePrice=180 | price=120, salePrice=99 | ✓ product.salePrice=99 |
| 2: 部分有特惠 | price=145, salePrice=120 | price=220, salePrice=null | price=120, salePrice=null | ✓ product.salePrice=120 |
| 3: 無特惠 | salePrice=null | salePrice=null | salePrice=null | ✓ product.salePrice=null |
| 4: 特惠 >= 售價 | price=120, salePrice=120 | — | — | ✗ 驗證失敗 S-SP2 |
| 5: 負數特惠 | salePrice=-10 | — | — | ✗ 驗證失敗 S-SP1 |
| 6: 有結束時間無特惠 | salePrice=null, saleEndDate=明天 | — | — | ✗ 驗證失敗 S-SP3 |
| 7: 特惠已過期 | salePrice=100, saleEndDate=昨天 | — | — | ✓ 儲存成功，但詳情頁不顯示特惠（notExpired=false） |
| 8: 長期特惠 | salePrice=100, saleEndDate=null | — | — | ✓ 長期有效 |

---

## 6. 不動的檔案

以下已確認不需修改：

| 檔案 | 原因 |
|------|------|
| `ShopProduct.java` | salePrice / saleEndDate 欄位保留（derived） |
| `ShopProductMapper.xml` | insert/update 的 salePrice/saleEndDate 繼續存在（由 syncPriceFromSkus 填值後寫入） |
| `admin/index.vue` 商品列表 | `isSaleActive()` 和 `row.salePrice` 讀取 product 層 derived 值，不影響 |
| `InventoryImportDialog.vue` | 匯入不涉及 salePrice 設定 |
| `priceSafeguard.js` | 只處理售價防呆，不涉及特惠價 |
