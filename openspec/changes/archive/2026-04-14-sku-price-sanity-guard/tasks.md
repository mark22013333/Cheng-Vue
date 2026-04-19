# Tasks — SKU 售價 / 成本合理性防呆

## Phase 1：前端工具函式與常數

- [x] T1.1 `priceSafeguard.js` 新增 `recommendedPriceFromCost(cost)` 匯出
- [x] T1.2 JSDoc 註解：成本 <= 0 回傳 null，否則 `round(cost × MARKUP_RATIO)`

## Phase 2：validateSkuList 擴充

- [x] T2.1 新增 S4b 規則：`costPrice > price` → errors
- [x] T2.2 新增 `isSkuDirtyForPriceCheck(sku, index)` 輔助函式（比對 price / costPrice 是否與 originalSkuList 不同）
- [x] T2.3 收集 `needsConfirm` 陣列（僅收集 dirty && price <= 0 的 SKU）
- [x] T2.4 validateSkuList 回傳擴充為 `{ valid, errors, needsConfirm }`
- [x] T2.5 既有呼叫端（validate、validateStep 2、dirty 偵測等）相容性檢查

## Phase 3：saveProduct 確認對話

- [x] T3.1 saveProduct 在 validate 通過後檢查 `needsConfirm`
- [x] T3.2 使用 ElMessageBox.confirm 列出 SKU 名稱與警告文字
- [x] T3.3 使用者取消 → return，不送出 API，不清表單
- [x] T3.4 使用者確定 → 繼續原本的送出流程

## Phase 4：SkuCard 視覺警示

- [x] T4.1 售價輸入欄：`price <= 0 && dirty` 顯示橘色邊框與提示文字
- [x] T4.2 新增「建議售價 $X」按鈕（`price <= 0 && costPrice > 0` 時顯示）
- [x] T4.3 按鈕點擊 → 填入 recommendedPriceFromCost(costPrice)
- [x] T4.4 成本價輸入欄：`costPrice > price` 顯示紅色邊框與錯誤文字
- [x] T4.5 新增樣式 `.sku-input--warn` / `.sku-input--error` / `.sku-warn` / `.sku-error`

## Phase 5：SkuListItem 警告圖示

- [x] T5.1 `price === 0` 時在第二排顯示 WarningFilled icon 與 tooltip「此規格為 0 元商品」
- [x] T5.2 `costPrice > price` 時顯示紅色警告

## Phase 6：InventoryImportDialog 調整

- [x] T6.1 移除「$0 匯入後手動設定售價」暗示性提示
- [x] T6.2 移除「$0 採購成本請確認」暗示性提示
- [x] T6.3 新增「$0 匯入後仍需儲存時確認」明確警告
- [x] T6.4 新增「cost > price 請先調整」警告（以 costBlockerCount 驅動）
- [x] T6.5 editCostPrice 加上 `:max="item.editPrice"` 動態上限
- [x] T6.6 preview 卡片內，editCostPrice > editPrice 時顯示「超過售價」紅字
- [x] T6.7 「確認匯入」按鈕依 `hasImportBlocker` computed disabled
- [x] T6.8 新增 `costBlockerCount` / `hasImportBlocker` 兩個 computed

## Phase 7：後端常數與校驗

- [x] T7.1 新增（或擴充）`ShopPriceConstants.java`，定義 `MARKUP_RATIO = 1.2` 與 `MIN_TRUST_PRICE`
- [x] T7.2 `ShopProductServiceImpl` 新增 `validateAndEnrichSkuPricing(List<ShopProductSku>)` 私有方法
- [x] T7.3 R1 實作：`cost > price` → `throw new ServiceException`，錯誤訊息帶 SKU 名稱與金額
- [x] T7.4 R4 實作：`price == null && cost > 0` → 自動填入推薦售價
- [x] T7.5 `insertProduct` 在儲存 SKU 前呼叫 `validateAndEnrichSkuPricing`
- [x] T7.6 `updateProduct` 在儲存 SKU 前呼叫 `validateAndEnrichSkuPricing`
- [x] T7.7 既有 productImportService / crawler import 路徑確認是否經過此校驗：crawler 流程未設定 costPrice，無 cost>price 風險；0 元商品防呆主要於後台 UI 保障

## Phase 8：驗證與測試

- [x] T8.1 前端 `pnpm run build` 確認無錯誤
- [x] T8.2 後端 `mvn compile` 確認無錯誤
- [x] T8.3 手動測試：新增 0 元 SKU → 跳確認對話 → 確定送出成功
- [x] T8.4 手動測試：新增 0 元 SKU → 跳確認對話 → 取消保留表單
- [x] T8.5 手動測試：cost > price → 儲存擋下，顯示欄位錯誤
- [x] T8.6 手動測試：既有 0 元商品編輯其他欄位後儲存 → 不跳確認（未動 price/cost）
- [x] T8.7 手動測試：既有 0 元商品改價後儲存 → 跳確認對話
- [x] T8.8 手動測試：cost=100 price=null → 建議售價按鈕顯示 $120，點擊填入
- [x] T8.9 手動測試：匯入 Dialog 的 cost > price 警告與按鈕 disabled
- [x] T8.10 後端測試：API 直呼傳 cost > price → 回傳業務錯誤
- [x] T8.11 後端測試：API 直呼傳 price = null, cost = 100 → 儲存後 price = 120

## Phase 9：文件與歸檔

- [ ] T9.1 執行 `openspec validate sku-price-sanity-guard`
- [x] T9.2 於相關 CLAUDE.md / 程式註解交叉引用本 spec
- [x] T9.3 完工後 `openspec archive sku-price-sanity-guard`
