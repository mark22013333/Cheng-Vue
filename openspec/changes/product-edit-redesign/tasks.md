# Tasks — 商品編輯頁面重新設計

## Phase 1: 基礎重構（不改 UI 行為）

### T1: 建立 useProductForm composable
- [x] 建立 `cheng-ui/src/views/shop/product/composables/useProductForm.js`
- [x] 從現有 `edit.vue` 抽取所有狀態：`form`, `skuList`, `categoryOptions`, `invItemOptions`, `invItemLoading`
- [x] 從現有 `edit.vue` 抽取所有方法：`loadProduct`, `loadSkuList`, `ensureLinkedItemsLoaded`, `getCategoryTree`, `loadInvItems`, `remoteSearchInvItems`, `handleInvSelectFocus`, `addSku`, `removeSku`, `handleStockModeChange`, `handleInvItemChange`, `submitForm`
- [x] 新增 `priceAutoSync` 欄位到 form 預設值（`default: true`）
- [x] 新增 `computedMinPrice` computed（`min(skuList.*.price)`）
- [x] 新增 `computedMinOriginalPrice` computed
- [x] 新增 `watch(skuList.*.price)` → 自動同步 `form.price` if `priceAutoSync`
- [x] 新增 `toggleAutoPrice()` 方法
- [x] 新增 `importFromInventory(items, options)` 方法
- [x] 新增 `formRules` 物件（按 `specs/validation-rules.md`）
- [x] 新增 `validateSkuList()` 函式
- [x] 新增 `validate()` 統一入口
- [x] 新增 `validateStep(step)` 逐步驗證
- [x] 新增 `mapFieldToSection(fieldName)` 映射
- [x] 新增 `summary` computed（`SummaryData` 各區塊摘要文字）
- [x] 新增 `isDirty` + `sectionDirty` computed（快照比對）
- [x] 新增 `totalStock`, `linkedSkuCount` computed
- [x] `loadInvItems` 的 map 增加 `purchasePrice: item.purchasePrice`
- [x] `handleInvItemChange` 增加 price/costPrice/refPrice/refCost 帶入邏輯
- [x] `saveProduct()` 改為將 skuList 嵌入 product payload 一次送出

### T2: 改寫 edit.vue 使用 useProductForm
- [x] `edit.vue` 改為 `import { useProductForm } from './composables/useProductForm'`
- [x] 移除 `edit.vue` 內所有已抽取的狀態和方法
- [x] 使用 composable 回傳的 refs 和方法
- [x] `el-form ref` 綁定到 composable 的 `productFormRef`
- [x] 確認所有現有功能不變（新增、編輯、儲存、SKU 操作）
- [x] 測試：新增商品 → 加 SKU → 儲存 → 回列表
- [x] 測試：編輯商品 → 修改 SKU → 儲存 → 回列表

### T3: Flyway migration
- [x] 建立 `V50__product_price_auto_sync.sql`
- [x] `ALTER TABLE shop_product ADD COLUMN price_auto_sync TINYINT(1) DEFAULT 1`
- [x] `ShopProduct.java` 新增 `private Boolean priceAutoSync` 欄位
- [x] `ShopProductMapper.xml` 的 insert/update/select 加入 `price_auto_sync`

---

## Phase 2: 元件拆分

### T4: 建立 AccordionSection.vue
- [x] 建立 `cheng-ui/src/views/shop/product/components/AccordionSection.vue`
- [x] Props: `collapsed(v-model)`, `title`, `description`, `sectionKey`, `hasError`, `isDirty`
- [x] Slots: `default`, `#summary`
- [x] 使用 `el-collapse-transition` 做展開/收合動畫
- [x] 鍵盤支援：Enter/Space toggle
- [x] 無障礙：`role="button"`, `aria-expanded`, `aria-controls`, `tabindex="0"`
- [x] 完整 CSS 按 `specs/accordion-css.md` 實作
- [x] `@media (prefers-reduced-motion: reduce)` 禁用動畫

### T5: 抽出 ProductBasicSection.vue
- [x] 建立 `components/ProductBasicSection.vue`
- [x] Props: `form(v-model)`, `categoryOptions`
- [x] 欄位：title, categoryId, subTitle, mainImage, sliderImages
- [x] 使用 `<image-upload>` 元件
- [x] 從 edit.vue 移出對應的 template + style

### T6: 抽出 ProductDetailSection.vue
- [x] 建立 `components/ProductDetailSection.vue`
- [x] Props: `form(v-model)`
- [x] 欄位：description(`<editor>`), isRecommend, isNew, isHot, sortOrder

### T7: 抽出 SkuCard.vue
- [x] 建立 `components/SkuCard.vue`
- [x] Props: `sku(v-model)`, `index`, `invItemOptions`, `invItemLoading`, `showPrice`, `errors`
- [x] 卡片式佈局（垂直排列），取代水平表格行
- [x] LINKED 模式：庫存物品下拉（整行寬度）、規格名稱、SKU 編碼、庫存（鎖定）
- [x] MANUAL 模式：規格名稱、SKU 編碼、庫存（可編輯）
- [x] `showPrice=true` 時顯示售價 + 成本價 + 參考價提示
- [x] 切換模式按鈕、刪除按鈕
- [x] 驗證錯誤欄位紅框：`el-form-item :error="fieldError(fieldName)"`

### T8: 抽出 ProductSkuSection.vue
- [x] 建立 `components/ProductSkuSection.vue`
- [x] Props: `skuList(v-model)`, `invItemOptions`, `invItemLoading`, `skuErrors`, `showPrice`
- [x] 頂部操作列：`[📦 從庫存匯入]` `[+ 手動新增]`
- [x] 內部 `v-for SkuCard`
- [x] 空狀態 `el-empty`

### T9: 抽出 ProductPricingSection.vue
- [x] 建立 `components/ProductPricingSection.vue`
- [x] Props: `form(v-model)`, `skuList(v-model)`, `computedMinPrice`, `isAutoPrice`, `skuErrors`
- [x] 商品主價格：自動/手動模式切換、狀態標籤
- [x] SKU 個別售價表格：規格名稱 | 參考價(唯讀) | 售價 | 原價 | 成本價
- [x] 促銷設定：特惠價 + 結束時間 + 折扣提示

### T10: 在 edit.vue 中組裝元件
- [x] 用 Section 元件取代 edit.vue 內的 inline template
- [x] 確認功能等同 T2 結束時的狀態
- [x] 確認 el-form 驗證仍然正常運作

---

## Phase 3: 新 UI 行為

### T11: edit.vue 改為 Accordion 模式
- [x] 用 `AccordionSection` 包裹 4 個 Section 元件
- [x] `sections` reactive 狀態（預設全部收合）
- [x] 摘要文字綁定 `summary` computed
- [x] 未儲存變更指示器（`sectionDirty` → `isDirty` prop）
- [x] 驗證錯誤自動展開（`firstErrorSection` → `hasError` prop + `scrollToSection`）
- [x] 全部展開/全部收合按鈕
- [x] `onBeforeRouteLeave` 離開確認

### T12: 建立 ProductSummaryBar.vue
- [x] 建立 `components/ProductSummaryBar.vue`
- [x] Props: `summary`, `hasUnsavedChanges`
- [x] 3 行摘要：價格、規格庫存、分類旗標
- [x] 點擊行 → emit `scroll-to(sectionKey)`
- [x] 未儲存提示

### T13: 建立 InventoryImportDialog.vue
- [x] 建立 `components/InventoryImportDialog.vue`
- [x] Step A：搜尋（300ms 防抖、#標籤）+ 多選（el-table selection）+ 庫存篩選
- [x] 已關聯提示：`isAlreadyLinked` → disabled checkbox + 「已加入此商品」
- [x] 零庫存提示：灰底 +「目前無庫存」
- [x] Step B：預覽確認（規格名稱/SKU編碼/售價/成本價 可編輯）
- [x] 勾選項：使用庫存現價作為售價 / 使用採購成本作為成本價
- [x] 勾選項 watch → 即時更新 previewItems 預設值
- [x] emit confirm → 由容器呼叫 `importFromInventory`
- [x] 詳細規格見 `specs/inventory-import-dialog.md`

### T14: handleInvItemChange 增加價格帶入
- [x] `sku.price` ← `invItem.currentPrice`（如果 price 為空或為 0）
- [x] `sku.costPrice` ← `invItem.purchasePrice`（如果 costPrice 為空）
- [x] `sku.refPrice` ← `invItem.currentPrice`（永遠更新）
- [x] `sku.refCost` ← `invItem.purchasePrice`（永遠更新）
- [x] 保持 skuName / skuCode 的 `||` 優先保留邏輯不變

### T15: 商品主價格自動推算 UI
- [x] PricingSection 內：自動模式 → `form.price` disabled + 提示「= min(SKU 售價)」+ 🔄 標籤
- [x] 手動模式 → `form.price` 可編輯 + 「恢復自動」按鈕 + ✏️ 標籤
- [x] `toggleAutoPrice` 綁定

---

## Phase 4: 新增商品 Steps 模式

### T16: 建立 create.vue
- [x] 建立 `cheng-ui/src/views/shop/product/create.vue`
- [x] `el-steps` 4 步驟：基本資訊 / 規格與庫存 / 定價與促銷 / 詳情與發布
- [x] 使用 `useProductForm()`（不傳 productId）
- [x] `v-show` 切換 4 個 Section 元件
- [x] Step ② 的 SkuSection `showPrice=false`
- [x] `nextStep` → `validateStep(currentStep)` 逐步驗證
- [x] 最後一步「儲存商品」→ `saveProduct()`
- [x] 儲存成功後導航到列表頁

### T17: 路由調整
- [x] `router/index.js` 新增 `/cadm/shop/product-create` 路由
- [x] `permissions: [SHOP_PRODUCT_ADD]`
- [x] 商品列表 `index.vue` 的「新增」按鈕改為 `router.push('/cadm/shop/product-create/index')`

---

## Phase 5: 收尾

### T18: API 合併
- [x] 前端 `saveProduct()` 將 skuList 嵌入 product payload 一次送出
- [x] 確認後端 `ShopProductController` 的 `@RequestBody ShopProduct` 可反序列化 `skuList`
- [x] 移除前端 `batchSaveSku` 的獨立呼叫
- [x] 測試：新增商品（含 SKU）→ 確認 SKU 已入庫
- [x] 測試：編輯商品（改 SKU 價格）→ 確認 SKU 已更新

### T19: 清理
- [x] 移除 edit.vue 中不再使用的 inline template 和 style
- [x] 確認 `batchSaveSku` API 仍保留（可能其他地方使用）
- [x] 確認所有新元件的 scoped style 不污染全域

### T20: 完整測試
- [x] 新增商品（Steps 流程）：基本 → 庫存匯入 → 定價自動推算 → 詳情 → 儲存
- [x] 新增商品（Steps 流程）：基本 → 手動 SKU → 各別定價 → 儲存
- [x] 編輯商品（Accordion）：展開規格區 → 新增 SKU → 儲存
- [x] 編輯商品（Accordion）：展開定價區 → 改價格 → 儲存
- [x] 編輯商品（Accordion）：不修改直接離開 → 不應彈出確認
- [x] 編輯商品（Accordion）：修改後離開 → 應彈出「未儲存變更」確認
- [x] 驗證錯誤：空 SKU 名稱 → 自動展開 sku section + 紅點 + 紅框
- [x] 驗證錯誤：空 SKU 售價 → 自動展開 pricing section
- [x] 庫存匯入：搜尋 → 多選 → 預覽 → 確認 → SKU 已建立
- [x] 庫存匯入：已關聯物品 → disabled + 「已加入」提示
- [x] 庫存匯入：取消勾選「使用庫存現價」→ 售價為 0
- [x] 價格自動推算：改 SKU 價格 → 商品主價格跟著變
- [x] 價格手動覆寫：手動改主價格 → 切為手動模式 → SKU 價格變不影響
- [x] 價格恢復自動：點「恢復自動」→ 主價格重新計算
- [x] 行動裝置：768px 以下 Accordion 佈局正常
- [x] Reduced motion：`prefers-reduced-motion` 下所有動畫禁用
