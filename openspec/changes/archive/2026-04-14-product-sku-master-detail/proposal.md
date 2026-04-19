## Why

`product-edit-redesign` 上線後，SKU 區塊（②規格與庫存）雖已改為垂直卡片式，但在實際使用中浮現兩個新問題：

1. **多 SKU 商品卡片過長** — 一個商品若有 5+ 個 SKU，每張 SkuCard 展開後佔據約 400px 高度，整個 Accordion 區塊縱向拉到螢幕外，使用者必須滾動才能掃過所有規格，失去「一眼看全部」的能力；同時當下在編哪一個 SKU 也容易迷失焦點。
2. **已匯入 SKU 仍顯示「關聯庫存物品」下拉** — 從庫存匯入的 SKU 在 `SkuCard` 中依然渲染可搜尋的 `el-select`，使用者會誤以為可以「重新換一個庫存物品」，但實際上換掉會清空剛帶入的價格與庫存邏輯。下拉對已關聯的 SKU 是純雜訊。

此外，業務端累積的教訓需要在系統層面固化：

3. **從庫存匯入的售價可能造成上架虧損** — 庫存物品的 `currentPrice` 欄位是採購端的「參考價」，部分品項被手動設為 0 或極低數字（例如試賣價 $1、測試資料 $30）。使用者匯入時若未檢查，就會把 $1 的售價直接上架。這不是使用者操作失誤，而是系統沒有在關鍵路徑上做防呆。

## What Changes

### UI：SKU 區塊改為 Master-Detail 佈局

- **左側清單**（固定寬度 ~280px，可滾動）— 每個 SKU 一行，顯示 `#N`、規格名稱、售價、啟用開關、刪除按鈕。點擊任一行即選中，右側隨之切換。
- **右側編輯區**（flex 1）— 只顯示目前選中的 SKU 的 `SkuCard`，未選時顯示空狀態「請從左側選擇規格進行編輯」。
- **頂部工具列**保持不變（「從庫存匯入」/「手動新增」兩顆按鈕）。
- 新增 SKU 時自動選中新項，刪除當前選中項時自動選擇相鄰項，避免右側空白。
- SKU 卡片在 master-detail 模式下應**隱藏 header 的啟用開關與刪除按鈕**（這兩個動作已移到左側清單列），避免重複。

### UI：SkuCard 關聯庫存區塊改為狀態感知

當 `stockMode === 'LINKED'`：

- `invItemId` 已設定 → 顯示唯讀的 `已關聯：{invItemName}（{invItemCode}）` 文字 + 一顆「解除關聯」小按鈕，**不再渲染下拉**。
- `invItemId` 未設定 → 保留原本的可搜尋 `el-select`（通常是手動新增 SKU 後還沒關聯庫存的情境）。
- 解除關聯按鈕會清空 `invItemId`、`invItemName`、`invItemCode`、`refPrice`、`refCost`，讓使用者回到「重新選一個」的流程，且意圖明確。

### 業務規則：庫存匯入售價防呆

新增共用工具 `priceSafeguard.js`，導出函式 `safeSkuPriceFromInventory(current, purchase)`：

- `current >= 100` → 直接信任，回傳 `current`
- `current < 100`（含 0、空值）→ 採用採購成本加成：
  - `purchase > 0` → 回傳 `Math.round(purchase * 1.2)`（加成 20%）
  - `purchase <= 0` 或未設定 → 回傳 `0`（讓使用者在後續驗證步驟被強制手動輸入）

此防呆函式**套用於三處**：

1. `InventoryImportDialog.goToPreview()` — 初次進入預覽時的 `editPrice` 預設值
2. `InventoryImportDialog` 的 `importOptions.syncPrice` watcher — 切換勾選時的重算
3. `useProductForm.handleInvItemChange()` — 在 SkuCard 中直接下拉關聯庫存時的 `sku.price` 預填（此路徑現在雖被 UI 縮小範圍，但仍保留入口給手動新增後關聯的情境）

此外 `useProductForm.importFromInventory()` 不再重算價格，直接採用 Dialog 已套用防呆後的 `item.currentPrice`（由 Dialog 的 `handleConfirm` 寫回），保持防呆邏輯單一來源。

### UI：防呆提示可視化

Preview 卡片上當某個品項的 `editPrice` 來自防呆（亦即 `current < 100` 且 `editPrice != current`），在售價右側顯示 `⚠ 已套用防呆（原庫存現價 $X 過低）` 提示，讓使用者知道系統做了自動調整，可手動覆寫。

## Impact

### New Capabilities

- `sku-master-detail-layout`：SKU 區塊 Master-Detail 佈局，左清單右編輯表單
- `inv-item-display-mode`：SkuCard 中已關聯庫存的展示模式切換（唯讀 vs 可選）
- `inventory-price-safeguard`：庫存匯入售價防呆規則

### Modified Capabilities

- `inventory-import-dialog`：Preview 階段的 `editPrice` 預設值改為經過防呆函式處理，並新增防呆提示 UI
- `inv-item-change-handler`（`useProductForm.handleInvItemChange`）：`sku.price` 預填改為經過防呆函式處理
- `sku-card-layout`：新增 `hideActions` prop，新增「已關聯」唯讀狀態

### 檔案異動

**前端（cheng-ui）：**

- **新增** `src/views/shop/product/composables/priceSafeguard.js`（約 30 行，純函式 + 常數）
- **新增** `src/views/shop/product/components/SkuListItem.vue`（約 120 行，左側清單列元件）
- **修改** `src/views/shop/product/components/SkuCard.vue`：
  - 新增 `hideActions: Boolean` prop
  - 關聯庫存區塊改為狀態感知（唯讀/可選切換）
  - 新增「解除關聯」emit：`unlink-inv-item`
- **修改** `src/views/shop/product/components/ProductSkuSection.vue`：
  - 重寫 template 為 master-detail 佈局
  - 管理 `selectedIndex` 本地狀態
  - 新增 SKU 時自動選中、刪除時 shift 選擇
- **修改** `src/views/shop/product/components/InventoryImportDialog.vue`：
  - `goToPreview()` 與 `importOptions.syncPrice` watcher 改用 `safeSkuPriceFromInventory`
  - Preview 卡片新增防呆提示 UI
- **修改** `src/views/shop/product/composables/useProductForm.js`：
  - `handleInvItemChange` 改用 `safeSkuPriceFromInventory`
  - 新增 `handleUnlinkInvItem(index)` 方法（解除關聯）
  - `importFromInventory` 保持不變（Dialog 已套用防呆）

**後端：** 無異動（防呆規則純前端表現層，不影響 API 合約與 DB schema）

### 相容性

- SKU 儲存格式不變，後端 API 合約不變
- 已存在的商品編輯進入頁面後直接套用新 UI，無需資料遷移
- 防呆規則只影響「新匯入」與「新關聯」的 SKU，已存在的 SKU 其既有 `price` 欄位不會被回溯調整
