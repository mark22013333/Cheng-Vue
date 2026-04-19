## 設計決策

### D1: product.salePrice 保留 vs 刪除

**選項 A（採用）：保留為 derived 欄位**

- `shop_product.sale_price` 改由 `syncPriceFromSkus()` 自動推導為 `min(active SKU salePrice)`
- `shop_product.sale_end_date` 改為 `max(active SKU saleEndDate)`
- 列表頁 API 向前相容，不需改查詢
- 商品卡片組件可直接讀 `product.salePrice`
- admin `index.vue` 和 SEO 結構化資料不用動

**選項 B（不採用）：刪除 column**

- DROP `shop_product.sale_price` / `sale_end_date`
- 列表頁需改為 subquery 或 join 取 min SKU salePrice
- `detail.vue` `effectiveSalePrice` 需移除 product fallback
- `ShopCart` 需移除 `productSalePrice` / `productSaleEndDate`
- 語意最乾淨，但破壞性大，工時倍增

**理由**：語意上 product.salePrice 與 product.price 角色相同 — 都是「從 SKU 推導的代表值」，保留 derived 欄位不違反 SRP，且大幅降低遷移風險。如果未來要刪除可以開獨立 change。

---

### D2: SKU saleEndDate 策略

**選項 A（採用）：每個 SKU 獨立 saleEndDate**

- `shop_product_sku` 新增 `sale_end_date` 欄位
- `ShopPriceService.calculateSkuPrice()` 改用 `sku.getSaleEndDate()`（不再讀 product）
- 支持「大杯延長促銷 3 天」這類情境
- product.saleEndDate 由 max(SKU saleEndDate) 推導

**選項 B（不採用）：共用 product.saleEndDate**

- 不動 DB，SKU 所有特價共用一個到期時間
- 簡單但不夠靈活
- 和「特惠價已到 SKU 級」的語意不一致

**理由**：既然特惠價已遷到 SKU 級，到期時間也應跟著走，確保一致性。且 DB 變更只是 ADD COLUMN 一行，成本極低。

---

### D3: 驗證規則設計

**商品層**

移除 `salePriceValidator`（`useProductForm.js:163-175`），因為 product.salePrice 不再由使用者手動輸入。

**SKU 層**

在 `validateSkuList()` 新增規則：

| 規則 | 條件 | 錯誤訊息 |
|------|------|---------|
| S-SP1 | `sku.salePrice != null && sku.salePrice < 0` | 特惠價不可為負數 |
| S-SP2 | `sku.salePrice != null && sku.price != null && sku.salePrice >= sku.price` | 特惠價須低於該規格售價 |
| S-SP3 | `sku.saleEndDate != null && sku.salePrice == null` | 設定特價結束時間前須先填寫特惠價 |

**為什麼不需要 S-SP4（salePrice > costPrice）？** 有意低於成本的「虧損促銷」是合法商業行為（出清庫存），不應系統攔截。

---

### D4: 後端 fallback 統一策略

**現狀不一致**：

```
ShopPriceService.calculateSkuPrice():
  SKU 有 salePrice → 用 SKU salePrice + product.saleEndDate
  SKU 無 salePrice → calculatePrice(sku.price, null, null) → 只套全站折扣

ShopCheckoutServiceImpl.calculateCartItemPrice():
  SKU 有 skuSalePrice → 用 skuSalePrice + product.saleEndDate
  SKU 無 skuSalePrice → calculatePrice(sku.price, product.salePrice, product.saleEndDate) → 套商品特價
```

**統一方案（採用）**：

重構後 product.salePrice 是 derived，不應在結帳邏輯中被當作 "備用特價" 使用。統一為：

```
calculateSkuPrice():
  SKU 有 salePrice → 用 SKU salePrice + SKU saleEndDate
  SKU 無 salePrice → calculatePrice(sku.price, null, null) → 只套全站折扣
```

同步重構 `ShopCheckoutServiceImpl.calculateCartItemPrice()` 為呼叫 `ShopPriceService.calculateSkuPrice()`，消除重複邏輯。

---

### D5: V50 資料遷移策略

```sql
-- 1. 新增 SKU 級 sale_end_date 欄位
ALTER TABLE shop_product_sku
  ADD COLUMN sale_end_date DATETIME DEFAULT NULL
  COMMENT '特價結束時間（NULL 表示長期）' AFTER sale_price;

-- 2. 下推：product 有特惠價、SKU 沒有 → 複製到所有 ENABLED SKU
UPDATE shop_product_sku sku
  INNER JOIN shop_product p ON sku.product_id = p.product_id
SET sku.sale_price    = p.sale_price,
    sku.sale_end_date = p.sale_end_date
WHERE p.sale_price IS NOT NULL
  AND p.sale_price > 0
  AND sku.sale_price IS NULL
  AND sku.status IN ('ENABLED', '1');

-- 3. 為 SKU saleEndDate 加索引（加速過期批次查詢）
ALTER TABLE shop_product_sku
  ADD INDEX idx_sku_sale_end_date (sale_end_date);
```

**為什麼用 UPDATE JOIN 而非逐筆**：一次性遷移比 cursor 快且原子，且只在 SKU 自身沒有 salePrice 時才寫入。

**為什麼不清空 product.sale_price**：保留 derived 值；後續使用者在 admin 存檔時，`syncPriceFromSkus()` 會重新推導。

---

### D6: UI 呈現位置

**SkuCard（右側編輯區）**：在價格區塊（showPrice 時）新增 salePrice + saleEndDate 欄位。

```
售價 | 原價 | 成本價       ← 現有
特惠價 | 特價結束時間      ← 新增
```

**ProductPricingSection SKU 表格**：新增 salePrice 欄位（只讀展示，編輯入口在 SkuCard）。

**商品層促銷區塊**：移除 salePrice / saleEndDate 輸入框，改為唯讀摘要：
- 有 SKU 設定特惠價 → 顯示「特惠價範圍 $X ~ $Y」+ 連結到 SKU 編輯
- 所有 SKU 無特惠價 → 顯示「尚未設定 SKU 特惠價」

---

### D7: 前端 Shop 商店 detail.vue 影響評估

**現有邏輯**（`effectiveSalePrice` computed）：

```js
if (selectedSku.value?.salePrice != null) {
  return Number(selectedSku.value.salePrice || 0)
}
return Number(product.value?.salePrice || 0)
```

**遷移後**：
- SKU 有 salePrice → 走第一個 branch ✓
- SKU 無 salePrice → fallback 到 product.salePrice（derived = min SKU salePrice）
  - 若所有 SKU 都沒特惠 → product.salePrice = null → 0 → 不顯示 ✓
  - 若部分 SKU 有特惠 → product.salePrice = min → 但「此 SKU 沒特惠卻顯示別人的特惠價」→ ⚠ 語意錯誤

**修正**：移除 product fallback，因為 derived product.salePrice 只用於列表頁概覽：

```js
const effectiveSalePrice = computed(() => {
  return Number(selectedSku.value?.salePrice || 0)
})
```

**倒數計時**：改為 `selectedSku.value?.saleEndDate`。
