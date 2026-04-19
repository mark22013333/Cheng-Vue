# Design — 商品編輯頁面重新設計

## 架構決策

### D1: 單一 Composable 中樞（useProductForm）

所有子元件不擁有自己的狀態，全部從 `useProductForm` 取得 reactive refs。

**理由**：
- `form` 和 `skuList` 存在跨區塊依賴（pricing section 需要 skuList 的 price，sku section 需要 form.price 作為新增 SKU 的預設值）
- 避免 prop drilling 和事件冒泡的複雜性
- `create.vue` 和 `edit.vue` 共用同一個 composable，確保業務邏輯一致

### D2: Steps 新增 vs Accordion 編輯

**新增** = 引導式（Steps），因為使用者需要從零開始，步驟感降低認知負擔。
**編輯** = 直接式（Accordion），因為使用者通常只想改某一個欄位，不需重走四步。

兩者共用相同的子元件（ProductBasicSection 等），只是**容器佈局**不同。

### D3: 商品主價格 = 自動推算 + 可覆寫

- `priceAutoSync = true`（預設）：`form.price` 透過 `watch` 即時跟隨 `min(skuList.*.price)`
- 使用者手動修改 `form.price` → 切換為 `priceAutoSync = false`
- 點「恢復自動」→ 重新計算並切回 `true`
- 新增欄位 `shop_product.price_auto_sync TINYINT(1) DEFAULT 1`

**原價 / 特惠價**維持手動設定，不自動推算。

### D4: 庫存匯入帶入價格

選擇庫存物品時：
- `sku.price` ← `invItem.currentPrice`（如果 SKU price 為空或為 0）
- `sku.costPrice` ← `invItem.purchasePrice`（如果 SKU costPrice 為空）
- `sku.refPrice` ← `invItem.currentPrice`（永遠更新，唯讀參考用）
- `sku.refCost` ← `invItem.purchasePrice`（永遠更新，唯讀參考用）

InventoryImportDialog 另提供勾選項：「使用庫存現價作為售價」/「使用採購成本作為成本價」。

### D5: 單次 API 送出

現行前端分兩次呼叫（`updateProduct` + `batchSaveSku`），改為將 `skuList` 嵌入 `product` payload 一次送出。後端 `ShopProductServiceImpl.updateProduct()` 已有 `syncPriceFromSkus` + `batchInsertSku` 邏輯，不需修改。

### D6: SKU 卡片式取代水平表格

水平表格 8 欄在行動裝置不可用。改為垂直卡片，每個 SKU 一張卡片，關聯庫存的下拉可用整行寬度。

---

## 元件架構

```
views/shop/product/
├── create.vue                      ← 新增商品（Steps 容器）
├── edit.vue                        ← 編輯商品（Accordion 容器）[改寫現有]
├── composables/
│   └── useProductForm.js           ← 核心 composable
└── components/
    ├── ProductBasicSection.vue     ← ❶ 基本資訊
    ├── ProductSkuSection.vue       ← ❷ 規格與庫存
    ├── ProductPricingSection.vue   ← ❸ 定價與促銷
    ├── ProductDetailSection.vue    ← ❹ 詳情與展示
    ├── SkuCard.vue                 ← 單一 SKU 卡片
    ├── InventoryImportDialog.vue   ← 庫存匯入 Dialog
    ├── ProductSummaryBar.vue       ← 摘要列（編輯模式頂部）
    └── AccordionSection.vue        ← 通用折疊區塊容器
```

---

## 資料流

```
                    useProductForm(productId?)
                    ══════════════════════════
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
     form (ref)     skuList (ref)   invItemOptions (ref)
         │               │               │
         ├──▶ BasicSection (v-model:form)  │
         ├──▶ PricingSection (v-model:form + v-model:skuList)
         ├──▶ DetailSection (v-model:form) │
         │               │               │
         │    SkuSection (v-model:skuList) │
         │      └── SkuCard × N           │
         │               │               │
         │    ImportDialog ── confirm ──▶ importFromInventory()
         │                                │
         │    watch(skuList.*.price) ──▶ computedMinPrice
         │         │                      │
         │         └── if priceAutoSync ──▶ form.price = min
         │
         │    saveProduct() ──▶ { ...form, skuList } ──▶ API
         │
         └── validate() ──▶ formRules + validateSkuList()
                              │
                              └── firstErrorSection → Accordion 自動展開
```

---

## 型別定義

### ProductForm

```js
/**
 * @typedef {Object} ProductForm
 * @property {number|undefined} productId
 * @property {number|undefined} categoryId
 * @property {string|undefined} title
 * @property {string|undefined} subTitle
 * @property {string|undefined} mainImage
 * @property {string|undefined} sliderImages
 * @property {number} price              - 商品主價格
 * @property {number} originalPrice      - 原價（劃線價）
 * @property {number|undefined} salePrice - 特惠價
 * @property {string|undefined} saleEndDate
 * @property {string|undefined} description
 * @property {number} sortOrder
 * @property {boolean} isRecommend
 * @property {boolean} isNew
 * @property {boolean} isHot
 * @property {boolean} priceAutoSync     - 價格是否自動跟隨 SKU 最低價
 */
```

### SkuRow

```js
/**
 * @typedef {Object} SkuRow
 * @property {number|undefined} skuId
 * @property {number|undefined} productId
 * @property {string} skuCode
 * @property {string} skuName
 * @property {number} price               - SKU 售價
 * @property {number|undefined} originalPrice
 * @property {number|undefined} costPrice  - 成本價
 * @property {number} stockQuantity
 * @property {number|undefined} invItemId
 * @property {'LINKED'|'MANUAL'} stockMode - 前端用，不送後端
 * @property {'ENABLED'|'DISABLED'} status
 * @property {number|undefined} refPrice   - 前端用：庫存 currentPrice（唯讀參考）
 * @property {number|undefined} refCost    - 前端用：庫存 purchasePrice（唯讀參考）
 * @property {string|undefined} invItemName
 * @property {string|undefined} invItemCode
 */
```

### InvItemOption

```js
/**
 * @typedef {Object} InvItemOption
 * @property {number} itemId
 * @property {string} itemCode
 * @property {string} itemName
 * @property {string|undefined} barcode
 * @property {number} currentStock       - availableQty
 * @property {number} stockQuantity      - totalQuantity
 * @property {number|undefined} currentPrice
 * @property {number|undefined} purchasePrice
 * @property {SysTag[]} tags
 */
```

### ImportOptions

```js
/**
 * @typedef {Object} ImportOptions
 * @property {boolean} syncPrice  - 是否帶入庫存現價作為售價
 * @property {boolean} syncCost   - 是否帶入採購成本作為成本價
 */
```

### SummaryData

```js
/**
 * @typedef {Object} SummaryData
 * @property {string} basic    - "蟲蟲掰掰 / 家庭用品"
 * @property {string} sku      - "2 SKU：Fisher-Price($145)... — 庫存 25"
 * @property {string} pricing  - "$145 起（自動） 原價 $195 特惠 $131（省$14）"
 * @property {string} detail   - "推薦 ✗ / 新品 ✓ / 熱門 ✓ / 排序 0"
 */
```

---

## useProductForm composable 介面

```js
/**
 * @param {Ref<number>|undefined} productId - 編輯模式傳入
 * @returns {Object}
 */
export function useProductForm(productId) {
  // ===== 狀態 =====
  const form = ref(/* ProductForm */)
  const skuList = ref(/* SkuRow[] */)
  const categoryOptions = ref([])
  const invItemOptions = ref([])
  const invItemLoading = ref(false)
  const loading = ref(false)
  const skuValidationErrors = ref([])
  const firstErrorSection = ref(null)
  const productFormRef = ref(null)     // el-form ref，由容器頁面綁定

  // ===== 計算屬性 =====
  const computedMinPrice = computed(/* min(skuList.*.price) */)
  const computedMinOriginalPrice = computed(/* min(skuList.*.originalPrice) */)
  const isAutoPrice = computed(() => form.value.priceAutoSync)
  const totalStock = computed(/* sum(skuList.*.stockQuantity) */)
  const linkedSkuCount = computed(/* skuList.filter(LINKED).length */)
  const summary = computed(/* SummaryData */)
  const isDirty = computed(/* JSON snapshot comparison */)
  const sectionDirty = computed(/* per-section dirty flags */)

  // ===== 方法 =====
  async function loadProduct()
  async function saveProduct()
  function addSku()
  function removeSku(index)
  function importFromInventory(items, options)
  function toggleAutoPrice()
  function searchInvItems(query)
  function handleInvItemChange(index, invItemId)
  function handleStockModeChange(index, mode)
  async function validate()
  async function validateStep(step)
  function getFirstErrorSection()

  // ===== watch =====
  // watch(skuList.*.price) → if priceAutoSync → form.price = min
  // watch(skuList, deep) → clear skuValidationErrors

  return {
    form, skuList, categoryOptions, invItemOptions, invItemLoading,
    loading, skuValidationErrors, firstErrorSection, productFormRef,
    computedMinPrice, computedMinOriginalPrice, isAutoPrice,
    totalStock, linkedSkuCount, summary, isDirty, sectionDirty,
    loadProduct, saveProduct, addSku, removeSku,
    importFromInventory, toggleAutoPrice,
    searchInvItems, handleInvItemChange, handleStockModeChange,
    validate, validateStep, getFirstErrorSection,
    formRules  // el-form rules object
  }
}
```

---

## 元件 Props / Emits 規格

### AccordionSection.vue（通用折疊容器）

```
Props:
  collapsed: boolean (v-model)
  title: string (required)
  description: string
  sectionKey: string (required)    - 用於 DOM id 和 scroll-to
  hasError: boolean
  isDirty: boolean

Emits:
  update:collapsed(boolean)

Slots:
  default    - 展開時的表單內容
  #summary   - 收合時的摘要文字
```

### ProductBasicSection.vue

```
Props:
  form: ProductForm (v-model)
  categoryOptions: Tree[]

Emits:
  update:form(ProductForm)

欄位：title*, categoryId*, subTitle, mainImage, sliderImages
```

### ProductSkuSection.vue

```
Props:
  skuList: SkuRow[] (v-model)
  invItemOptions: InvItemOption[]
  invItemLoading: boolean
  skuErrors: SkuError[]          - 驗證錯誤
  showPrice: boolean             - false=Steps Step②, true=Accordion

Emits:
  update:skuList(SkuRow[])
  add-sku()
  remove-sku(index)
  stock-mode-change(index, mode)
  inv-item-change(index, invItemId)
  search-inv-items(query)
  open-import-dialog()
```

### SkuCard.vue

```
Props:
  sku: SkuRow (v-model)
  index: number
  invItemOptions: InvItemOption[]
  invItemLoading: boolean
  showPrice: boolean
  errors: SkuError[]             - 過濾後的此 SKU 錯誤

Emits:
  update:sku(SkuRow)
  remove()
  stock-mode-change(mode)
  inv-item-change(invItemId)
  search-inv-items(query)
```

### ProductPricingSection.vue

```
Props:
  form: ProductForm (v-model)
  skuList: SkuRow[] (v-model)
  computedMinPrice: number|null
  isAutoPrice: boolean
  skuErrors: SkuError[]          - 價格相關錯誤

Emits:
  update:form(ProductForm)
  update:skuList(SkuRow[])
  toggle-auto-price()
```

### ProductDetailSection.vue

```
Props:
  form: ProductForm (v-model)

Emits:
  update:form(ProductForm)

欄位：description, isRecommend, isNew, isHot, sortOrder
```

### InventoryImportDialog.vue

```
Props:
  visible: boolean (v-model)
  existingInvItemIds: number[]   - 已關聯的物品ID

Emits:
  update:visible(boolean)
  confirm(items: InvItemOption[], options: ImportOptions)

內部兩步驟：
  Step A: 搜尋 + 多選（el-table selection）
  Step B: 預覽確認 + 微調 + 勾選項
```

### ProductSummaryBar.vue

```
Props:
  summary: SummaryData
  hasUnsavedChanges: boolean

Emits:
  scroll-to(sectionKey: 'basic'|'sku'|'pricing'|'detail')
```

---

## 路由設計

```js
// 新增
{
  path: '/cadm/shop/product-create',
  component: () => import('@/layout/index.vue'),
  hidden: true,
  permissions: [SHOP_PRODUCT_ADD],
  children: [
    {
      path: 'index',
      component: () => import('@/views/shop/product/create'),
      name: 'ShopProductCreate',
      meta: { title: '新增商品', activeMenu: '/cadm/shop/product' }
    }
  ]
}

// 現有（不變）
{
  path: '/cadm/shop/product-edit',
  ...
  children: [
    {
      path: 'index/:productId?',
      component: () => import('@/views/shop/product/edit'),
      name: 'ShopProductEdit',
      meta: { title: '編輯商品', activeMenu: '/cadm/shop/product' }
    }
  ]
}
```

商品列表頁 `index.vue` 的「新增」按鈕改為導航到 `/cadm/shop/product-create/index`。

---

## 後端變更

### Flyway V50

```sql
ALTER TABLE shop_product
ADD COLUMN price_auto_sync TINYINT(1) DEFAULT 1
COMMENT '價格是否自動同步SKU最低價（1=自動, 0=手動覆寫）';
```

### ShopProduct.java

新增欄位：

```java
/**
 * 價格是否自動同步 SKU 最低價
 */
private Boolean priceAutoSync;
```

### ShopProductMapper.xml

`insertProduct`、`updateProduct`、`selectProductById` 加入 `price_auto_sync` 欄位。

### 前端 loadInvItems 修改

`loadInvItems` 的 map 增加 `purchasePrice: item.purchasePrice`。後端 `InvItemWithStockDTO` 已包含此欄位。
