# Design — SKU 售價 / 成本合理性防呆

## 決策背景

使用者在檢視 `InventoryImportDialog` 時指出：
- 售價 $0 能被儲存，等於上架「免費商品」——不合理
- 成本 > 售價沒人擋，等於上架「保證虧本商品」——完全不合理

經討論確認以下邊界（使用者親自定奪）：

| 決策點 | 選擇 | 理由 |
|--------|------|------|
| `costPrice === price` 是否擋 | **不擋（寬鬆版）** | 允許保本 / 0 毛利（成本轉嫁、贈送前的庫存出清） |
| `price === 0` 是否擋 | **軟擋：跳確認對話** | 例外情境（贈品、試用品）確實存在，但要使用者明確承擔 |
| 既有資料庫髒資料 | **載入警告、不強制修正** | 避免既有商品因歷史定價問題無法儲存 |
| 後端是否防守 | **是** | 業務底線不應只在前端，API 直呼 / 爬蟲匯入仍需擋 |

## 技術抉擇

### 1. 「本次編輯過」的判定機制

目前 `useProductForm.js` 已有 `sectionDirty.pricing` 與 `orig` snapshot 比對邏輯（見 152-168 行）。本 change 不引入新狀態，直接**復用 snapshot 比對**：

```js
function isSkuDirtyForPriceCheck(sku, index) {
  const orig = originalSkuList.value?.[index]
  if (!orig) return true // 本次新增 → 必 dirty
  return orig.price !== sku.price || orig.costPrice !== sku.costPrice
}
```

**關鍵**：只比對價格相關欄位，避免使用者改了 SKU 名稱就觸發定價確認。

### 2. 確認對話的位置

有兩個可能的插入點：

```
[A] validateSkuList() 回傳中帶 needsConfirm 陣列
    → saveProduct() 依 needsConfirm 決定是否跳對話
[B] saveProduct() 直接內聯 confirm 邏輯
```

選 **[A]**：validateSkuList 仍然是純判定，對話 UX 由 saveProduct 控制，保持職責分離。

```js
// validateSkuList() 回傳格式擴充
{ valid: boolean, errors: [...], needsConfirm: [{ index, skuName, reason }] }
```

### 3. 推薦售價的常數位置

前後端同用 `MARKUP_RATIO = 1.2`：
- 前端：`priceSafeguard.js` 既有匯出，新增 `recommendedPriceFromCost(cost)` 便利方法
- 後端：新增 `ShopPriceConstants.MARKUP_RATIO`（不要 hardcode 在 ServiceImpl 裡）

兩邊「剛好」用 1.2 是約定，不是強制同步機制。若未來要參數化，改為 `sys_dict` 讀取時兩邊一起改。

### 4. 匯入 Dialog 的 costPrice 上限

目前 `editCostPrice` 無上限，可輸入超過 `editPrice` 的值。加上 `:max="item.editPrice || undefined"`：
- 當 editPrice 為 0 或未設時，不限制（因為 0 元商品走確認流程）
- 當 editPrice > 0 時，costPrice 不得超過

**但** `el-input-number` 的 `:max` 在使用者輸入後會自動收斂，不會給錯誤訊息。要搭配視覺提示：

```
[editCostPrice 輸入] ─ if > editPrice ─▶ 紅框 + 「超過售價」提示
                    └ 確認匯入按鈕 disabled
```

### 5. 後端 validation 的位置

`ShopProductServiceImpl` 的 `saveProduct` / `updateProduct` 內部，在呼叫 Mapper 前加：

```java
private void validateSkuPricing(List<ShopProductSku> skuList) {
    for (ShopProductSku sku : skuList) {
        BigDecimal price = sku.getPrice();
        BigDecimal cost = sku.getCostPrice();
        if (cost != null && price != null && cost.compareTo(price) > 0) {
            throw new ServiceException(
                String.format("規格「%s」成本價（$%s）不可高於售價（$%s）",
                    sku.getSkuName(), cost, price));
        }
        // price == null 時自動推薦
        if ((price == null || price.compareTo(BigDecimal.ZERO) == 0)
                && cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            sku.setPrice(cost.multiply(BigDecimal.valueOf(1.2))
                .setScale(0, RoundingMode.HALF_UP));
        }
    }
}
```

**注意**：後端「自動填推薦售價」只處理 `price == null`（API 未提供），不覆蓋前端明確傳來的 `price = 0`。前端的確認對話才是 0 元商品的真正閘門。

## 風險與邊界

### 誤擋風險

- **既有 SKU 本來就 price = 0**：使用者打開編輯頁，什麼都沒改、按儲存——不應跳確認（由 dirty 比對保障）
- **使用者把 price 從 0 改成 100、又改回 0**：會觸發確認對話（dirty 比對為 true）——這是對的，使用者確實在「決定」0 元

### 後端 API 直呼

有其他模組（例如 `product-import-crawler`）會直接呼叫 Service 建立商品。這些路徑不會經過前端確認對話，所以後端必須至少擋 `cost > price`。`price = 0` 情境在後端**不擋**（允許 admin 在前端明確確認後送進來），由呼叫端自行負責。

### `BigDecimal` vs. Number

前端用 `Number`，後端用 `BigDecimal`。序列化邊界要小心：
- 前端送 0 → Jackson 轉 `BigDecimal(0)` → `compareTo(ZERO) == 0` ✅
- 前端送 null → `price == null` 分支 ✅

### 與 `inventory-price-safeguard` 的關係

`inventory-price-safeguard` 處理「建議值填入」，本 change 處理「儲存時的硬性/軟性規則」。兩者不衝突，是同一業務線的不同層次：

```
  匯入 Dialog         SkuCard 編輯          儲存前
     │                    │                    │
     ▼                    ▼                    ▼
[inventory-price-safeguard]                   │
  建議值（MIN_TRUST_PRICE、MARKUP_RATIO）      │
                                               ▼
                                    [sku-price-sanity-guard]
                                       硬擋 cost > price
                                       軟擋 price = 0（確認對話）
                                       後端複查
```

## 未來可能擴充

- 推薦售價的加成比例參數化（sys_dict → `shop.sku.recommendation.markup`）
- 0 元商品的審計日誌（誰在什麼時候確認了 0 元商品）
- 不同商品分類用不同的 MARKUP_RATIO
