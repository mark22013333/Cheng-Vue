## Why

`product-sku-master-detail` 的 `inventory-price-safeguard` 只解決「從庫存帶入時的建議售價」，但沒處理兩個業務底線：

1. **SKU 售價允許儲存為 $0** — `validateSkuList()` 的 S3 只擋空值與負數，`price === 0` 能通過。`safeSkuPriceFromInventory` 在「雙零」情境回傳 0，spec 裡寫「後續驗證擋下」，但後續驗證其實沒擋。
2. **成本價沒與售價比對** — S4 只擋 `costPrice < 0`，使用者可以建立一個「成本 $200、售價 $150」的虧本商品。
3. **匯入 Dialog UI 暗示可接受 $0 匯入** — 提示文字「$0 匯入後手動設定售價」實際鼓勵繞過防呆。

這三點是業務層的資料完整性問題，不是 UX 偏好。必須前後端都守住。

## What Changes

### 核心規則

1. **售價 = $0 屬例外狀況**：本次編輯異動過的 SKU 若 `price <= 0`，儲存時需跳出確認對話：「建立 0 元商品（{SKU 名稱}）屬例外情境（贈品 / 試用品），確定要儲存嗎？」使用者按確定才放行。
2. **成本價不得高於售價（寬鬆版）**：`costPrice > price` 直接擋下，`costPrice === price` 允許（0 毛利合理，例如成本轉嫁、保本商品）。
3. **既有 SKU 的歷史髒資料相容**：載入時若 `price === 0`，標示警告，但只要本次沒編輯該 SKU，允許儲存通過（不回溯強制修正）。
4. **系統推薦售價**：當 SKU 的 `price` 為空或 0，且 `costPrice > 0` 時，前端表單顯示「建議售價 ${cost × 1.2}」按鈕，點擊自動填入；後端 save 時若 `price` 為 null，以 `round(cost × 1.2)` 自動填補作為 fallback 推薦值（使用者仍可手動設 0 經確認對話）。
5. **後端驗證層**：`ShopProductServiceImpl` 儲存前校驗 `costPrice > price`，違反則回傳業務錯誤。

### 前端

1. **`useProductForm.validateSkuList()` 擴充**：
   - 新增 S3b：`price <= 0` 且該 SKU 於本次編輯中被 dirty → 標記為「需確認」（非錯誤）
   - 新增 S4b：`costPrice > price` → 錯誤「成本價不可高於售價」
2. **`useProductForm.saveProduct()` 儲存流程**：在呼叫 API 前，若存在「需確認」的零元 SKU，`ElMessageBox.confirm()` 列出 SKU 名稱，使用者確定後才送出。
3. **新增「本次編輯過」追蹤**：依現有 `sectionDirty` / snapshot 比對機制，標記 `sku._dirtyThisSession` 或以 `skuList vs. originalSkuList` 比對判斷。
4. **`SkuCard.vue`**：
   - 售價輸入欄：`price === 0` 時外框顯示橘色警告邊 + 提示「0 元商品需於儲存時確認」
   - 成本價輸入欄：`costPrice > price` 時紅色錯誤邊 + 錯誤文字
   - 新增「建議售價」按鈕：`price` 為空/0 且 `costPrice > 0` 時顯示，點擊填入 `round(costPrice × 1.2)`
5. **`InventoryImportDialog.vue`**：
   - 移除「$0 匯入後手動設定售價」、「$0 匯入後請確認成本價」兩則暗示性提示
   - 改為：「X 個物品將以 $0 匯入，匯入後仍需確認才能儲存」的明確警告
   - `editCostPrice` 欄位加上 `:max="item.editPrice"` 動態上限（costPrice 不得超過 editPrice）
   - Preview 卡片的 costPrice 欄若超過 editPrice，顯示紅色錯誤提示
6. **`priceSafeguard.js`**：匯出新常數 `RECOMMENDED_PRICE(cost) = Math.round(cost * MARKUP_RATIO)`，前後端同用。

### 後端

1. **`ShopProductServiceImpl.saveProduct()` / `updateProduct()`**：
   - 逐 SKU 校驗 `costPrice > price` → 拋 `ServiceException("規格「{name}」成本價（${cost}）不可高於售價（${price}）")`
   - 若 `price === null` 且 `costPrice > 0`，自動填入 `Math.round(cost * 1.2)` 作為推薦值（不擋 `price === 0`，由前端確認對話把關）
2. **新增常數 `ShopPriceConstants.MARKUP_RATIO = 1.2`**（或複用既有）供後端使用。

### 不做的事

- 不回溯修復資料庫中既存的 `price = 0` 或 `costPrice > price` 髒資料（歷史相容）
- 不修改 `originalPrice` / `salePrice` 的既有驗證規則（由 `product-sale-price-semantics` 管轄）
- 不提供「從成本反推全部 SKU 售價」的批次工具（超出範圍）

## Impact

### Modified Capabilities

- `sku-validation`：validateSkuList 新增 S3b / S4b 規則與確認對話流程
- `inventory-import-ux`：匯入 Dialog 移除 $0 暗示性提示，加上 costPrice 上限
- `sku-save-backend`：後端 save 路徑新增業務規則校驗

### New Capabilities

- `sku-recommended-price`：基於成本 × 1.2 的推薦售價 UI 與後端 fallback

### 檔案異動（預估）

**前端**：
- `cheng-ui/src/views/shop/product/composables/useProductForm.js`（validateSkuList / saveProduct / dirty tracking）
- `cheng-ui/src/views/shop/product/composables/priceSafeguard.js`（匯出 `recommendedPriceFromCost`）
- `cheng-ui/src/views/shop/product/components/SkuCard.vue`（視覺警示、建議售價按鈕）
- `cheng-ui/src/views/shop/product/components/InventoryImportDialog.vue`（提示文字、costPrice max）

**後端**：
- `cheng-shop/src/main/java/com/cheng/shop/service/impl/ShopProductServiceImpl.java`（儲存前校驗）
- `cheng-shop/src/main/java/com/cheng/shop/service/ShopPriceService.java`（如需新增 suggest 方法）

### 相容性

- 既有資料不動，僅新編輯路徑受限
- API 格式不變，僅錯誤訊息新增
- 前端確認對話為額外步驟，不改變儲存 API 簽名
