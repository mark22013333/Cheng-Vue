## Why

商品編輯頁面（`edit.vue`）目前是單一長表單，存在以下問題：

1. **價格定義模糊** — 商品主價格和 SKU 個別價格在兩個地方各自獨立維護，容易不一致
2. **庫存關聯不帶價格** — 庫存物品已設定 `currentPrice` / `purchasePrice`，選取後卻不帶入 SKU，使用者須重複輸入
3. **認知負擔重** — 所有欄位一次攤開（基本資訊、圖片、SKU 表格 8 欄、富文字），新手不知道哪些重要、哪些可選
4. **SKU 水平表格擠** — 8 個欄位水平排列，尤其「關聯庫存物品」下拉佔 320px，行動裝置完全無法使用
5. **無建立流程引導** — 沒有步驟感，使用者不知道正確的填寫順序

## What Changes

- **新增商品**改為 **4 步驟引導式**（Steps）：基本資訊 → 規格與庫存 → 定價與促銷 → 詳情與發布
- **編輯商品**改為 **Accordion 折疊式**：4 個可獨立展開的區塊，每區收合時顯示即時摘要
- 新增**從庫存匯入**功能：多選庫存物品 → 預覽確認 → 批次建立 SKU，自動帶入名稱、編碼、價格、成本、庫存
- **商品主價格自動推算**：`price = min(SKU prices)`，可手動覆寫，支援切換回自動模式
- SKU 從水平表格改為**垂直卡片式**排列，每個 SKU 一張卡片
- 抽取 `useProductForm` composable 作為核心資料中樞，`create.vue` 和 `edit.vue` 共用子元件
- 前端改為**單次 API 送出**：`skuList` 嵌入 `product` payload，不再分兩次呼叫

## Capabilities

### New Capabilities

- `product-step-create`: 步驟式商品建立流程，4 步驟引導，每步逐步驗證
- `inventory-import-dialog`: 庫存物品多選匯入 Dialog，含搜尋/標籤篩選、已關聯衝突提示、預覽確認、價格/成本帶入選項
- `price-auto-sync`: 商品主價格自動跟隨 SKU 最低售價，可手動覆寫並恢復自動
- `accordion-edit`: 編輯模式 Accordion 折疊區塊，含摘要預覽、未儲存變更追蹤、驗證錯誤自動展開
- `sku-card-layout`: SKU 垂直卡片式佈局，取代水平表格

### Modified Capabilities

- `product-edit`: 現有 `edit.vue` 重構為 Accordion 模式 + 共用子元件架構
- `inv-item-change-handler`: `handleInvItemChange` 增加 price / costPrice / refPrice / refCost 帶入邏輯
- `product-save`: 前端改為單次 API 送出（skuList 嵌入 product），後端已支援不需修改

## Impact

- **前端 `cheng-ui/src/views/shop/product/`**：新增 `create.vue`、重構 `edit.vue`、新增 `components/` 和 `composables/` 子目錄
- **前端元件**：新增 7 個元件（ProductBasicSection, ProductSkuSection, ProductPricingSection, ProductDetailSection, SkuCard, InventoryImportDialog, ProductSummaryBar）
- **前端 composable**：新增 `useProductForm.js`
- **路由**：新增 `/cadm/shop/product-create` → `create.vue`
- **後端 ShopProduct**：新增 `priceAutoSync` 欄位（Boolean）
- **資料庫**：Flyway `V50__product_price_auto_sync.sql` — `shop_product` 表新增 `price_auto_sync` 欄位
- **API 變更**：無新增端點，前端改為將 `skuList` 嵌入現有 `POST/PUT /shop/product` payload
