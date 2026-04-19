# Spec — 庫存匯入售價防呆

## 概述

防止使用者將庫存中售價異常偏低（或未定價）的物品直接帶入為 SKU 售價，造成上架後虧損。規則以純前端工具函式實現，套用於所有「從庫存帶入售價」的路徑。

---

## 工具模組：`priceSafeguard.js`

### 位置

`cheng-ui/src/views/shop/product/composables/priceSafeguard.js`

### 常數

```js
/**
 * 視為「可信任」的最低售價。低於此門檻的庫存售價會被視為異常
 * （試賣價、未定價、測試資料），觸發防呆。
 */
export const MIN_TRUST_PRICE = 100

/**
 * 當觸發防呆時，由採購成本推算售價的加成比例。
 * 20% 為零售業保守毛利率起點，確保至少不虧本。
 */
export const MARKUP_RATIO = 1.2
```

### 主要函式

```js
/**
 * 從庫存物品帶入 SKU 售價的防呆計算。
 *
 * 規則：
 * 1. 若庫存現價 >= MIN_TRUST_PRICE，直接信任
 * 2. 否則，若採購成本 > 0，回傳 round(purchase × MARKUP_RATIO)
 * 3. 否則，回傳 0（讓後續驗證強制使用者手動輸入）
 *
 * @param {number|null|undefined} current  庫存物品的 currentPrice
 * @param {number|null|undefined} purchase 庫存物品的 purchasePrice
 * @returns {number} 防呆後的 SKU 售價
 */
export function safeSkuPriceFromInventory(current, purchase) {
  const c = Number(current) || 0
  const p = Number(purchase) || 0

  if (c >= MIN_TRUST_PRICE) return c
  if (p > 0) return Math.round(p * MARKUP_RATIO)
  return 0
}

/**
 * 判斷 safeSkuPriceFromInventory 的回傳是否經過調整（用於 UI 提示）。
 *
 * @param {number|null|undefined} current
 * @param {number|null|undefined} purchase
 * @returns {boolean}
 */
export function isPriceSafeguardApplied(current, purchase) {
  const c = Number(current) || 0
  return c < MIN_TRUST_PRICE
}
```

---

## 套用路徑 1：`InventoryImportDialog.goToPreview()`

### 前

```js
editPrice: importOptions.syncPrice ? (item.currentPrice || 0) : 0,
```

### 後

```js
import { safeSkuPriceFromInventory } from '../composables/priceSafeguard'

// ...

editPrice: importOptions.syncPrice
  ? safeSkuPriceFromInventory(item.currentPrice, item.purchasePrice)
  : 0,
```

---

## 套用路徑 2：`InventoryImportDialog` 的 `syncPrice` watcher

### 前

```js
watch(() => importOptions.syncPrice, (sync) => {
  previewItems.value.forEach(item => {
    item.editPrice = sync ? (item.currentPrice || 0) : 0
  })
})
```

### 後

```js
watch(() => importOptions.syncPrice, (sync) => {
  previewItems.value.forEach(item => {
    item.editPrice = sync
      ? safeSkuPriceFromInventory(item.currentPrice, item.purchasePrice)
      : 0
  })
})
```

---

## 套用路徑 3：`useProductForm.handleInvItemChange()`

此路徑在使用者於 SkuCard 內直接選取庫存物品時觸發。Master-Detail 重構後，此路徑僅剩「手動新增 SKU → 在右側表單選關聯庫存」的情境，但仍需防呆。

### 前

```js
if (!sku.price || sku.price === 0) {
  sku.price = invItem.currentPrice || 0
}
```

### 後

```js
import { safeSkuPriceFromInventory } from './priceSafeguard'

// ...

if (!sku.price || sku.price === 0) {
  sku.price = safeSkuPriceFromInventory(invItem.currentPrice, invItem.purchasePrice)
}
```

---

## 套用路徑 4：`importFromInventory` 不重複套用

Dialog 在 `handleConfirm` 時已經把 `editPrice` 寫回 `item.currentPrice`：

```js
// InventoryImportDialog.vue → handleConfirm
const items = previewItems.value.map(item => ({
  ...item,
  itemName: item.editSkuName,
  itemCode: item.editSkuCode,
  currentPrice: item.editPrice,   // ← 這裡是已套用防呆後的值
  purchasePrice: item.editCostPrice
}))
emit('confirm', items, { ...importOptions })
```

所以 `useProductForm.importFromInventory` 拿到的 `item.currentPrice` 已經是防呆後的值。**此函式不需改動**，保持：

```js
price: importOpts.syncPrice ? (item.currentPrice || 0) : 0,
```

避免防呆規則被執行兩次（導致低價商品被加成到奇怪的數字）。

---

## 防呆提示 UI

### 位置

`InventoryImportDialog.vue` 的 Step B Preview 卡片，售價欄位 `preview-price-row` 內。

### 前

```vue
<span v-if="item.currentPrice != null" class="preview-price-ref" :class="{ 'preview-price-ref--zero': !item.currentPrice }">
  ← 庫存現價 ${{ item.currentPrice }}
  <template v-if="!item.currentPrice">（尚未定價）</template>
</span>
```

### 後

```vue
<span v-if="isItemSafeguarded(item)" class="preview-price-ref preview-price-ref--safeguard">
  <el-icon><WarningFilled /></el-icon>
  已套用防呆（原庫存現價 ${{ item.currentPrice ?? 0 }} 過低）
</span>
<span v-else-if="item.currentPrice != null" class="preview-price-ref" :class="{ 'preview-price-ref--zero': !item.currentPrice }">
  ← 庫存現價 ${{ item.currentPrice }}
  <template v-if="!item.currentPrice">（尚未定價）</template>
</span>
```

### 判斷函式

```js
import { isPriceSafeguardApplied } from '../composables/priceSafeguard'

function isItemSafeguarded(item) {
  if (!importOptions.syncPrice) return false
  if (!isPriceSafeguardApplied(item.currentPrice, item.purchasePrice)) return false
  // 使用者已手動覆寫為原本低價時，提示消失
  return item.editPrice !== (Number(item.currentPrice) || 0)
}
```

### 樣式

```css
.preview-price-ref--safeguard {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-warning, #FF7D00);
  font-weight: 500;
}
```

---

## 防呆觸發統計提示（頂部）

在 Preview 的勾選項區塊新增一行統計：

```vue
<div v-if="importOptions.syncPrice && safeguardCount > 0" class="preview-options__warn">
  <el-icon><WarningFilled /></el-icon>
  {{ safeguardCount }} 個物品的庫存現價低於 ${{ MIN_TRUST_PRICE }}，已自動以採購成本推算售價（加成 {{ Math.round((MARKUP_RATIO - 1) * 100) }}%）
</div>
```

```js
import { safeSkuPriceFromInventory, isPriceSafeguardApplied, MIN_TRUST_PRICE, MARKUP_RATIO } from '../composables/priceSafeguard'

const safeguardCount = computed(() =>
  previewItems.value.filter(item =>
    isPriceSafeguardApplied(item.currentPrice, item.purchasePrice)
  ).length
)
```

此統計讓使用者一眼看到「有幾個品項被調整」，降低批次匯入時的盲區。

---

## 驗證案例

| 情境 | currentPrice | purchasePrice | editPrice 期望 | 提示 |
|------|--------------|---------------|-----------------|------|
| 正常價格 | 150 | 90 | 150 | 庫存現價 $150 |
| 邊界正好 100 | 100 | 60 | 100 | 庫存現價 $100 |
| 試賣價 | 1 | 90 | 108（90×1.2） | ⚠ 已套用防呆 |
| 低於 100 | 88 | 50 | 60（50×1.2） | ⚠ 已套用防呆 |
| 零現價有成本 | 0 | 80 | 96 | ⚠ 已套用防呆 |
| 雙零 | 0 | 0 | 0 | ⚠ 已套用防呆（後續驗證擋下） |
| 現價低且成本負數 | 50 | -10 | 0 | ⚠ 已套用防呆 |
| 現價為 null | null | 80 | 96 | ⚠ 已套用防呆 |
| 使用者手動覆寫回 $50 | 50 | 80 | 50 | 庫存現價 $50（提示消失） |

---

## 範圍邊界

### 本規則「不」處理

1. **編輯已存在的 SKU** — 已儲存的 SKU 其 `price` 不會被回溯防呆
2. **後端送出時的再次校驗** — 後端 API 不加額外防呆，信任前端送來的 `price`，使用者最終有權覆寫決定
3. **原價 `originalPrice` / 成本價 `costPrice`** — 只處理 `price`（售價）欄位，不干涉其他欄位
4. **非「從庫存帶入」的路徑** — 使用者手動輸入售價 $50 時不會被擋下，使用者意圖明確

### 未來可能擴充

- 若需要把防呆規則參數化（不同商品分類不同閾值），可將 `MIN_TRUST_PRICE` / `MARKUP_RATIO` 改為從 `sys_dict` 讀取
- 若需要在儲存時二次提示，可在 `saveProduct` 中檢查 `sku.price < MIN_TRUST_PRICE && sku.refPrice != null` 並彈出確認對話
