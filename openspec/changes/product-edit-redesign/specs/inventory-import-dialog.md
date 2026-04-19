# Spec — InventoryImportDialog 庫存匯入

## 概述

提供從庫存系統多選物品、預覽確認、批次匯入為 SKU 的完整流程。

---

## Props / Emits

```js
// Props
{
  visible: { type: Boolean, default: false },          // v-model
  existingInvItemIds: { type: Array, default: () => [] } // 已關聯的 invItemId（衝突提示）
}

// Emits
{
  'update:visible': (val: boolean) => true,
  'confirm': (items: InvItemOption[], options: ImportOptions) => true
}
```

---

## 內部狀態

```js
const dialogStep = ref('select')  // 'select' | 'preview'

// Step A: 搜尋與多選
const searchQuery = ref('')
const searchTimer = ref(null)            // 300ms 防抖
const tableData = ref([])                // InvItemOption[]
const tableLoading = ref(false)
const selectedItems = ref([])            // el-table selection 勾選的項目
const filterStock = ref('all')           // 'all' | 'inStock' | 'outOfStock'

// Step B: 預覽確認
const previewItems = ref([])             // 從 selectedItems 複製，可微調
const importOptions = reactive({
  syncPrice: true,                       // 預設勾選
  syncCost: true                         // 預設勾選
})
```

---

## Step A：搜尋與多選

### 佈局

```
┌──────────────────────────────────────────────────────────┐
│  從庫存匯入                                    [✕ 關閉]   │
│                                                          │
│  ┌─────────────────────────────────────────────────┐     │
│  │ 🔍 輸入物品名稱、編碼或 #標籤搜尋                │     │
│  └─────────────────────────────────────────────────┘     │
│                                                          │
│  篩選：庫存 [全部 ▾]                                      │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │ ☑│ 物品名稱             │ 編碼    │庫存│ 現價 │成本│  │
│  ├──┼──────────────────────┼────────┼────┼──────┼────┤  │
│  │☑ │ Fisher-Price 費雪     │ FP-001 │ 15 │ $145 │ $90│  │
│  │☑ │ 蟲蟲掰掰 178*53*53mm │ PA-03  │ 10 │ $145 │ $80│  │
│  │──│──────────────────────│────────│────│──────│────│  │
│  │☐ │ 兒童積木組 A          │ BK-001 │  5 │ $299 │$150│  │
│  │disabled│ 兒童積木組 B     │ BK-002 │  3 │ $399 │$200│  │
│  │  │ └─ 🏷️ 已加入此商品    │        │    │      │    │  │
│  │──│──────────────────────│────────│────│──────│────│  │
│  │☐ │ 辦公椅 高背           │ CH-001 │  0 │ $899 │$450│  │
│  │  │ └─ 💤 目前無庫存      │        │    │      │    │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  已選 2 項                         [取消]  [下一步 ▸]    │
└──────────────────────────────────────────────────────────┘
```

### 搜尋邏輯

複用現有 `listItemWithStock` API，300ms 防抖。

```js
function handleSearch(query) {
  if (searchTimer.value) clearTimeout(searchTimer.value)

  if (!query || query.trim() === '') {
    loadItems()
    return
  }

  searchTimer.value = setTimeout(() => {
    const params = {}
    if (query.startsWith('#')) {
      const tagName = query.substring(1).trim()
      if (tagName) params.tagName = tagName
    } else {
      params.keyword = query.trim()
    }
    loadItems(params)
  }, 300)
}

function loadItems(params = {}) {
  tableLoading.value = true
  const query = { pageNum: 1, pageSize: 50, ...params }

  // 庫存篩選
  if (filterStock.value === 'inStock') {
    query.stockStatus = '0'  // 有庫存
  }

  listItemWithStock(query)
    .then(response => {
      tableData.value = (response.rows || []).map(item => ({
        itemId: item.itemId,
        itemCode: item.itemCode,
        itemName: item.itemName,
        barcode: item.barcode,
        currentStock: item.availableQty || 0,
        stockQuantity: item.totalQuantity || 0,
        currentPrice: item.currentPrice,
        purchasePrice: item.purchasePrice,
        tags: item.tags || [],
        // 衝突狀態
        isAlreadyLinked: props.existingInvItemIds.includes(item.itemId)
      }))
    })
    .finally(() => { tableLoading.value = false })
}
```

### el-table 配置

```vue
<el-table
  ref="importTableRef"
  :data="tableData"
  v-loading="tableLoading"
  @selection-change="handleSelectionChange"
  border
  size="small"
  max-height="400"
>
  <el-table-column
    type="selection"
    width="45"
    :selectable="(row) => !row.isAlreadyLinked"
  />
  <el-table-column prop="itemName" label="物品名稱" min-width="180">
    <template #default="{ row }">
      <div>{{ row.itemName }}</div>
      <div v-if="row.isAlreadyLinked" class="import-hint import-hint--linked">
        已加入此商品
      </div>
      <div v-else-if="row.currentStock === 0" class="import-hint import-hint--empty">
        目前無庫存
      </div>
      <div v-if="row.tags?.length" class="import-tags">
        <el-tag v-for="tag in row.tags.slice(0, 3)" :key="tag.tagId" size="small" type="info">
          {{ tag.tagName }}
        </el-tag>
      </div>
    </template>
  </el-table-column>
  <el-table-column prop="itemCode" label="編碼" width="100" />
  <el-table-column prop="currentStock" label="庫存" width="70" align="right">
    <template #default="{ row }">
      <span :class="{ 'text-danger': row.currentStock === 0 }">{{ row.currentStock }}</span>
    </template>
  </el-table-column>
  <el-table-column prop="currentPrice" label="現價" width="80" align="right">
    <template #default="{ row }">
      {{ row.currentPrice != null ? '$' + row.currentPrice : '—' }}
    </template>
  </el-table-column>
  <el-table-column prop="purchasePrice" label="成本" width="80" align="right">
    <template #default="{ row }">
      {{ row.purchasePrice != null ? '$' + row.purchasePrice : '—' }}
    </template>
  </el-table-column>
</el-table>
```

### 行樣式

```css
/* 已關聯的行：禁用 + 灰底 */
.el-table__row.is-linked {
  background: var(--color-bg-3);
  opacity: 0.6;
}

/* 零庫存的行：淡灰底 */
.el-table__row.is-empty-stock {
  background: var(--color-bg-2);
}

.import-hint {
  font-size: 11px;
  margin-top: 2px;
}
.import-hint--linked {
  color: var(--color-primary);
}
.import-hint--empty {
  color: var(--color-text-4);
}
.import-tags {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}
```

### 行樣式回調

```js
function tableRowClassName({ row }) {
  if (row.isAlreadyLinked) return 'is-linked'
  if (row.currentStock === 0) return 'is-empty-stock'
  return ''
}
```

---

## Step B：預覽確認

### 佈局

```
┌──────────────────────────────────────────────────────────┐
│  確認匯入 — 2 個品項                           [✕ 關閉]   │
│                                                          │
│  以下庫存物品將建立為商品規格（SKU），可在匯入前微調。       │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │  ① Fisher-Price 費雪滑動學習智慧型手機               │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │ 規格名稱  [Fisher-Price 費雪滑動學習智慧型手機]│  │  │
│  │  │ SKU 編碼  [FP-001                           ]│  │  │
│  │  │ 售價      [$145    ]  ← 庫存現價 $145        │  │  │
│  │  │ 成本價    [$90     ]  ← 採購成本 $90         │  │  │
│  │  │ 庫存      15（自動同步，不可編輯）              │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                    │  │
│  │  ② 蟲蟲掰掰 178*53*53mm(24入)                     │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │ 規格名稱  [178*53*53mm(24入)                ]│  │  │
│  │  │ SKU 編碼  [PA-03                            ]│  │  │
│  │  │ 售價      [$145    ]  ← 庫存現價 $145        │  │  │
│  │  │ 成本價    [$80     ]  ← 採購成本 $80         │  │  │
│  │  │ 庫存      10（自動同步，不可編輯）              │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  ☑ 使用庫存現價作為售價（取消勾選則售價留空，稍後設定）      │
│  ☑ 使用採購成本作為成本價                                  │
│                                                          │
│                            [◂ 返回選擇]  [確認匯入 ✓]    │
└──────────────────────────────────────────────────────────┘
```

### 進入 Step B 時

```js
function goToPreview() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('請至少選擇一個物品')
    return
  }

  // 複製到 previewItems，帶入可編輯欄位
  previewItems.value = selectedItems.value.map(item => ({
    ...item,
    editSkuName: item.itemName,
    editSkuCode: item.itemCode,
    editPrice: importOptions.syncPrice ? (item.currentPrice || 0) : 0,
    editCostPrice: importOptions.syncCost ? (item.purchasePrice || undefined) : undefined
  }))

  dialogStep.value = 'preview'
}
```

### 勾選項 watch

```js
// 勾選項變化時即時更新 previewItems 的預設值
watch(() => importOptions.syncPrice, (sync) => {
  previewItems.value.forEach(item => {
    if (sync) {
      item.editPrice = item.currentPrice || 0
    } else {
      item.editPrice = 0
    }
  })
})

watch(() => importOptions.syncCost, (sync) => {
  previewItems.value.forEach(item => {
    if (sync) {
      item.editCostPrice = item.purchasePrice || undefined
    } else {
      item.editCostPrice = undefined
    }
  })
})
```

### 確認匯入

```js
function handleConfirm() {
  // 組裝回傳資料：把 edit 欄位寫回 item
  const items = previewItems.value.map(item => ({
    ...item,
    // 覆蓋為使用者微調後的值
    itemName: item.editSkuName,
    itemCode: item.editSkuCode,
    currentPrice: item.editPrice,
    purchasePrice: item.editCostPrice
  }))

  emit('confirm', items, { ...importOptions })
  emit('update:visible', false)

  // 重置
  dialogStep.value = 'select'
  selectedItems.value = []
  previewItems.value = []
}
```

---

## Dialog 配置

```vue
<el-dialog
  :model-value="visible"
  @update:model-value="$emit('update:visible', $event)"
  :title="dialogStep === 'select' ? '從庫存匯入' : `確認匯入 — ${previewItems.length} 個品項`"
  width="720px"
  :close-on-click-modal="false"
  destroy-on-close
  @open="handleDialogOpen"
  @closed="handleDialogClosed"
>
```

### 生命週期

```js
function handleDialogOpen() {
  dialogStep.value = 'select'
  selectedItems.value = []
  previewItems.value = []
  importOptions.syncPrice = true
  importOptions.syncCost = true
  loadItems()  // 載入初始資料
}

function handleDialogClosed() {
  searchQuery.value = ''
  tableData.value = []
}
```

---

## useProductForm 端：importFromInventory

Dialog emit `confirm` 後，由容器頁面呼叫 composable 的 `importFromInventory`：

```js
// 容器頁面
function handleImportConfirm(items, options) {
  importFromInventory(items, options)
}
```

```js
// useProductForm 內
function importFromInventory(items, options) {
  for (const item of items) {
    // 衝突檢測
    const existingIdx = skuList.value.findIndex(s => s.invItemId === item.itemId)

    if (existingIdx >= 0) {
      // 更新：刷新庫存、參考價，不覆蓋現有售價
      const existing = skuList.value[existingIdx]
      existing.stockQuantity = item.currentStock
      existing.refPrice = item.currentPrice
      existing.refCost = item.purchasePrice
      continue
    }

    // 新增 SKU
    skuList.value.push({
      skuId: undefined,
      productId: form.value.productId,
      skuCode: item.itemCode || '',
      skuName: item.itemName || '',
      price: options.syncPrice ? (item.currentPrice || 0) : 0,
      costPrice: options.syncCost ? (item.purchasePrice || undefined) : undefined,
      originalPrice: undefined,
      stockQuantity: item.currentStock || 0,
      invItemId: item.itemId,
      stockMode: 'LINKED',
      status: 'ENABLED',
      refPrice: item.currentPrice,
      refCost: item.purchasePrice,
      invItemName: item.itemName,
      invItemCode: item.itemCode
    })
  }
}
```
