# Tasks — Product SKU Master-Detail

## T1. 建立價格防呆工具模組

- [ ] T1.1 新增 `cheng-ui/src/views/shop/product/composables/priceSafeguard.js`
- [ ] T1.2 匯出常數 `MIN_TRUST_PRICE = 100`, `MARKUP_RATIO = 1.2`
- [ ] T1.3 匯出 `safeSkuPriceFromInventory(current, purchase)`
- [ ] T1.4 匯出 `isPriceSafeguardApplied(current, purchase)`
- [ ] T1.5 JSDoc 完整註解規則與邊界

## T2. `useProductForm.js` 接入防呆

- [ ] T2.1 import `safeSkuPriceFromInventory`
- [ ] T2.2 `handleInvItemChange` 的 `sku.price` 預填改用防呆函式
- [ ] T2.3 新增 `handleUnlinkInvItem(index)` 方法（清空 invItemId/invItemName/invItemCode/refPrice/refCost，保留 price/costPrice/stockQuantity/skuName/skuCode）
- [ ] T2.4 將 `handleUnlinkInvItem` 加入 return

## T3. `InventoryImportDialog.vue` 接入防呆 + 提示 UI

- [ ] T3.1 import `safeSkuPriceFromInventory`, `isPriceSafeguardApplied`, `MIN_TRUST_PRICE`, `MARKUP_RATIO`
- [ ] T3.2 `goToPreview()` 的 `editPrice` 預設值改用防呆函式
- [ ] T3.3 `importOptions.syncPrice` watcher 改用防呆函式
- [ ] T3.4 新增 `isItemSafeguarded(item)` 與 `safeguardCount` computed
- [ ] T3.5 Preview 卡片售價欄位新增防呆提示（⚠ 已套用防呆）
- [ ] T3.6 勾選項區塊新增批次統計提示
- [ ] T3.7 新增 `.preview-price-ref--safeguard` 樣式

## T4. 新增 `SkuListItem.vue` 元件

- [ ] T4.1 建立檔案 `cheng-ui/src/views/shop/product/components/SkuListItem.vue`
- [ ] T4.2 實作 Props（sku, index, active, hasError）
- [ ] T4.3 實作 Emits（click, remove）
- [ ] T4.4 Template：#N、規格名稱、售價、庫存標籤、啟用開關、刪除鈕
- [ ] T4.5 樣式：active 態（左側藍邊）、error 態（左側紅邊）、disabled 態（opacity 0.6）
- [ ] T4.6 `@click.stop` 包住 actions 區塊避免觸發列 click

## T5. 修改 `SkuCard.vue`

- [ ] T5.1 新增 `hideActions: Boolean` prop
- [ ] T5.2 Header 的 actions 區塊加 `v-if="!hideActions"`
- [ ] T5.3 關聯庫存 form-item 改為狀態感知（`sku.invItemId` 有值 → 唯讀區塊，否則 → 原下拉）
- [ ] T5.4 新增 emit `unlink-inv-item`
- [ ] T5.5 唯讀區塊：顯示 `invItemName + invItemCode`，加「解除關聯」按鈕 emit `unlink-inv-item`
- [ ] T5.6 新增 `.sku-card__linked-info` 等樣式

## T6. 重構 `ProductSkuSection.vue` 為 master-detail

- [ ] T6.1 新增本地 `selectedIndex` ref + `effectiveIndex` computed + `selectedSku` computed
- [ ] T6.2 watch `skuList.length` 處理新增/刪除時的選中 shift
- [ ] T6.3 新增 `handleRemove(index)` 處理刪除前的 selectedIndex 調整
- [ ] T6.4 新增 `indexHasError(index)` 輔助函式
- [ ] T6.5 Template 重寫為左右兩欄結構（master + detail）
- [ ] T6.6 左側 v-for 渲染 `SkuListItem`，傳 active/hasError
- [ ] T6.7 右側條件渲染 `SkuCard`（hideActions）或 empty state
- [ ] T6.8 新增 emit `unlink-inv-item` 並在 SkuCard 轉發
- [ ] T6.9 CSS：flex 佈局、280px master、響應式 < 960px 切換縱向

## T7. `edit.vue` / `create.vue` 接線

- [ ] T7.1 `edit.vue`：`ProductSkuSection` 新增 `@unlink-inv-item="handleUnlinkInvItem"`
- [ ] T7.2 `create.vue`：同上
- [ ] T7.3 確認 `useProductForm` 解構中有 `handleUnlinkInvItem`

## T8. 驗證與手動測試

- [ ] T8.1 建置前端（`pnpm run build`）確認無 lint / type 錯誤
- [ ] T8.2 手動測試：載入已有 5+ SKU 的商品，確認 master-detail 佈局正確
- [ ] T8.3 手動測試：點擊左側不同 SKU，右側正確切換
- [ ] T8.4 手動測試：手動新增 SKU 後自動選中新項
- [ ] T8.5 手動測試：刪除當前選中項，選中自動移到相鄰項
- [ ] T8.6 手動測試：刪除非選中項，當前選中的 SKU 保持不變
- [ ] T8.7 手動測試：匯入庫存 — 挑一個 `currentPrice < 100` 的物品，確認預覽顯示防呆提示且 `editPrice` 為加成後的值
- [ ] T8.8 手動測試：Preview 勾選項切換 syncPrice，確認 editPrice 重算正確
- [ ] T8.9 手動測試：已關聯庫存的 SKU 在右側顯示唯讀區塊而非下拉
- [ ] T8.10 手動測試：點擊「解除關聯」後 invItemId 清空，區塊切回下拉，price 保留
- [ ] T8.11 手動測試：驗證失敗時左側清單的出錯項顯示紅色邊線
- [ ] T8.12 手動測試：窄視窗（< 960px）切為上下堆疊

## T9. 文件

- [ ] T9.1 本 change 若通過驗收，可併入 `product-edit-redesign` 的 archive 或獨立存檔
- [ ] T9.2 若有調整 `MIN_TRUST_PRICE` / `MARKUP_RATIO`，更新 spec 內驗證案例表
