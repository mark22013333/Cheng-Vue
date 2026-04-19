# Spec — SKU Master-Detail 佈局

## 概述

`ProductSkuSection` 重構為左清單右編輯的 master-detail 佈局，取代原本的垂直卡片堆疊。

---

## 元件結構

```
ProductSkuSection (容器)
├── toolbar（頂部「從庫存匯入 / 手動新增」按鈕列）— 不變
├── globalError 區塊 — 不變
├── body（master-detail 主體）
│   ├── 左側 master pane
│   │   ├── SkuListItem × N（v-for skuList）
│   │   └── 空狀態（skuList 空時）
│   └── 右側 detail pane
│       ├── SkuCard（hideActions，顯示 selectedSku）
│       └── 空狀態（未選中時）
```

---

## ProductSkuSection Props / Emits

Props 與 Emits **完全不變**（與 `product-edit-redesign` 階段保持一致的對外合約）：

```js
// Props
{
  skuList: { type: Array, required: true },
  invItemOptions: { type: Array, default: () => [] },
  invItemLoading: { type: Boolean, default: false },
  skuErrors: { type: Array, default: () => [] },
  showPrice: { type: Boolean, default: false }
}

// Emits
[
  'update:skuList',
  'add-sku',
  'remove-sku',
  'stock-mode-change',
  'inv-item-change',
  'search-inv-items',
  'open-import-dialog',
  'unlink-inv-item'   // 新增：解除關聯
]
```

新增一個 emit `unlink-inv-item(index)` 提供給 SkuCard 的「解除關聯」按鈕使用。

---

## 內部狀態（ProductSkuSection 本地）

```js
import { ref, computed, watch } from 'vue'

const selectedIndex = ref(0)

// 確保選中 index 永遠有效
const effectiveIndex = computed(() => {
  if (props.skuList.length === 0) return -1
  if (selectedIndex.value < 0) return 0
  if (selectedIndex.value >= props.skuList.length) return props.skuList.length - 1
  return selectedIndex.value
})

const selectedSku = computed(() =>
  effectiveIndex.value >= 0 ? props.skuList[effectiveIndex.value] : null
)
```

---

## 新增 SKU 時自動選中

```js
// 監聽 skuList 長度變化，新增時選中最後一項
watch(
  () => props.skuList.length,
  (newLen, oldLen) => {
    if (newLen > oldLen) {
      selectedIndex.value = newLen - 1
    } else if (newLen < oldLen && selectedIndex.value >= newLen) {
      // 刪除後若選中超出範圍，移到相鄰項
      selectedIndex.value = Math.max(0, newLen - 1)
    }
  }
)
```

說明：使用 length watcher 而非 deep watch，避免在 SKU 內容編輯時重置選中。新增 SKU 不論來源（手動新增 / 從庫存匯入）皆會觸發 length 增加，統一透過此路徑選中新項。

---

## 刪除 SKU 的選擇 shift

```js
function handleRemove(index) {
  emit('remove-sku', index)
  // 不需手動調整 selectedIndex，watch length 會自動處理
}
```

當刪除的是當前選中項（`index === selectedIndex.value`）：

- 若後面還有 SKU → `selectedIndex` 不變（因為後一項會遞補到同一個 index）
- 若刪掉的是最後一項 → watch 會把 `selectedIndex` 設為 `newLen - 1`

當刪除的是非選中項（`index !== selectedIndex.value`）：

- 若 `index < selectedIndex.value` → `selectedIndex` 應減 1（保持選中同一個 SKU）
- 若 `index > selectedIndex.value` → `selectedIndex` 不變

因為 watch 無法區分「刪了哪個 index」，需在刪除前後顯式處理：

```js
function handleRemove(index) {
  const currentSelected = selectedIndex.value
  emit('remove-sku', index)
  if (index < currentSelected) {
    selectedIndex.value = currentSelected - 1
  }
  // 其他情況由 length watcher 接手
}
```

---

## 佈局與樣式

### Template 結構

```vue
<div class="sku-section__body">
  <!-- 左側清單 -->
  <div class="sku-section__master">
    <div v-if="skuList.length === 0" class="sku-section__master-empty">
      <el-empty description="暫無規格" :image-size="64" />
    </div>
    <div v-else class="sku-section__master-list">
      <SkuListItem
        v-for="(sku, index) in skuList"
        :key="sku.skuId || `new-${index}`"
        :sku="sku"
        :index="index"
        :active="index === effectiveIndex"
        :has-error="indexHasError(index)"
        @click="selectedIndex = index"
        @remove="handleRemove(index)"
      />
    </div>
  </div>

  <!-- 右側編輯區 -->
  <div class="sku-section__detail">
    <SkuCard
      v-if="selectedSku"
      :sku="selectedSku"
      :index="effectiveIndex"
      :inv-item-options="invItemOptions"
      :inv-item-loading="invItemLoading"
      :show-price="showPrice"
      :errors="skuErrorsForIndex(effectiveIndex)"
      hide-actions
      @update:sku="(val) => handleSkuUpdate(effectiveIndex, val)"
      @stock-mode-change="(mode) => emit('stock-mode-change', effectiveIndex, mode)"
      @inv-item-change="(itemId) => emit('inv-item-change', effectiveIndex, itemId)"
      @search-inv-items="(query) => emit('search-inv-items', query)"
      @unlink-inv-item="emit('unlink-inv-item', effectiveIndex)"
    />
    <div v-else class="sku-section__detail-empty">
      <el-empty description="請從左側選擇規格進行編輯" :image-size="100" />
    </div>
  </div>
</div>
```

### CSS

```css
.sku-section__body {
  display: flex;
  gap: 16px;
  min-height: 480px;
  border: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px);
  background: var(--color-bg-1, #FFFFFF);
  overflow: hidden;
}

.sku-section__master {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--color-border, #E5E6EB);
  background: var(--color-bg-2, #F7F8FA);
  overflow-y: auto;
  max-height: 600px;
}

.sku-section__master-list {
  display: flex;
  flex-direction: column;
}

.sku-section__master-empty {
  padding: 32px 16px;
  text-align: center;
}

.sku-section__detail {
  flex: 1;
  min-width: 0;
  padding: 16px;
  overflow-y: auto;
  max-height: 600px;
}

.sku-section__detail-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-3, #86909C);
}

/* 響應式：窄螢幕時縱向堆疊 */
@media (max-width: 960px) {
  .sku-section__body {
    flex-direction: column;
  }
  .sku-section__master {
    width: 100%;
    max-height: 240px;
    border-right: none;
    border-bottom: 1px solid var(--color-border, #E5E6EB);
  }
}
```

---

## 錯誤標記輔助函式

```js
function indexHasError(index) {
  return props.skuErrors.some(e => e.index === index)
}

function skuErrorsForIndex(index) {
  return props.skuErrors.filter(e => e.index === index)
}
```

---

## SkuListItem 元件（新增）

### Props / Emits

```js
// Props
{
  sku: { type: Object, required: true },
  index: { type: Number, required: true },
  active: { type: Boolean, default: false },
  hasError: { type: Boolean, default: false }
}

// Emits
['click', 'remove']
```

### Template

```vue
<template>
  <div
    class="sku-list-item"
    :class="{
      'sku-list-item--active': active,
      'sku-list-item--error': hasError,
      'sku-list-item--disabled': sku.status === 'DISABLED'
    }"
    @click="$emit('click')"
  >
    <div class="sku-list-item__main">
      <div class="sku-list-item__row1">
        <span class="sku-list-item__index">#{{ index + 1 }}</span>
        <span class="sku-list-item__name" :title="sku.skuName || '未命名規格'">
          {{ sku.skuName || '未命名規格' }}
        </span>
        <el-icon v-if="hasError" class="sku-list-item__error-icon" color="#F53F3F">
          <WarningFilled />
        </el-icon>
      </div>
      <div class="sku-list-item__row2">
        <span class="sku-list-item__price">
          ${{ sku.price ?? 0 }}
        </span>
        <span v-if="sku.invItemId" class="sku-list-item__linked-tag">
          <el-icon><Link /></el-icon> 庫存
        </span>
        <span class="sku-list-item__stock">庫存 {{ sku.stockQuantity ?? 0 }}</span>
      </div>
    </div>
    <div class="sku-list-item__actions" @click.stop>
      <el-switch
        v-model="sku.status"
        active-value="ENABLED"
        inactive-value="DISABLED"
        size="small"
      />
      <el-button
        link
        type="danger"
        icon="Delete"
        size="small"
        @click="$emit('remove')"
      />
    </div>
  </div>
</template>
```

### CSS

```css
.sku-list-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border, #E5E6EB);
  cursor: pointer;
  transition: background var(--transition-fast, 150ms ease-out);
}
.sku-list-item:hover {
  background: var(--color-bg-3, #F2F3F5);
}
.sku-list-item--active {
  background: var(--color-primary-lighter, #D9ECFF);
  border-left: 3px solid var(--color-primary, #409EFF);
  padding-left: 9px;
}
.sku-list-item--error {
  border-left: 3px solid var(--color-error, #F53F3F);
  padding-left: 9px;
}
.sku-list-item--disabled {
  opacity: 0.6;
}
.sku-list-item__main {
  flex: 1;
  min-width: 0;
}
.sku-list-item__row1 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}
.sku-list-item__index {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary, #409EFF);
  background: var(--color-bg-1, #FFFFFF);
  padding: 1px 6px;
  border-radius: var(--radius-sm, 6px);
  flex-shrink: 0;
}
.sku-list-item__name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sku-list-item__row2 {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: var(--color-text-3, #86909C);
}
.sku-list-item__price {
  font-weight: 600;
  color: var(--color-primary, #409EFF);
}
.sku-list-item__linked-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--color-success, #67C23A);
}
.sku-list-item__actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
```

---

## SkuCard 的改動

### 新增 Props

```js
{
  ...existing,
  hideActions: { type: Boolean, default: false }
}
```

### 新增 Emits

```js
['update:sku', 'remove', 'stock-mode-change', 'inv-item-change', 'search-inv-items', 'unlink-inv-item']
```

### Header 條件渲染

```vue
<div class="sku-card__header">
  <span class="sku-card__index">#{{ index + 1 }}</span>
  <span class="sku-card__name">{{ sku.skuName || '未命名規格' }}</span>
  <div v-if="!hideActions" class="sku-card__actions">
    <el-switch v-model="sku.status" active-value="ENABLED" inactive-value="DISABLED" size="small" />
    <el-button link type="danger" icon="Delete" size="small" @click="emit('remove')" />
  </div>
</div>
```

### 關聯庫存區塊改為狀態感知

```vue
<el-form-item
  v-if="sku.stockMode === 'LINKED'"
  label="關聯庫存物品"
  :error="fieldError('invItemId')"
>
  <!-- 已關聯：顯示唯讀資訊 + 解除關聯按鈕 -->
  <div v-if="sku.invItemId" class="sku-card__linked-info">
    <div class="sku-card__linked-text">
      <el-icon color="#67C23A"><Link /></el-icon>
      <span class="sku-card__linked-name">{{ sku.invItemName || '（未知名稱）' }}</span>
      <span v-if="sku.invItemCode" class="sku-card__linked-code">{{ sku.invItemCode }}</span>
    </div>
    <el-button
      link
      type="warning"
      size="small"
      @click="emit('unlink-inv-item')"
    >
      解除關聯
    </el-button>
  </div>

  <!-- 未關聯：保留原本的可搜尋下拉 -->
  <el-select
    v-else
    v-model="sku.invItemId"
    placeholder="輸入名稱、編碼或 #標籤搜尋"
    ...
  >
    ...
  </el-select>
</el-form-item>
```

### 新增樣式

```css
.sku-card__linked-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 12px;
  background: var(--color-success-light, #F0F9EB);
  border: 1px solid var(--color-success-border, #D0E9C6);
  border-radius: var(--radius-sm, 6px);
}
.sku-card__linked-text {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}
.sku-card__linked-name {
  font-weight: 500;
  color: var(--color-text-1, #1D2129);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sku-card__linked-code {
  font-size: 11px;
  color: var(--color-text-3, #86909C);
  background: #FFFFFF;
  padding: 1px 6px;
  border-radius: var(--radius-sm, 6px);
}
```

---

## 父層（edit.vue / create.vue）的處理

新增 `@unlink-inv-item` 事件監聽，呼叫 composable 新增的 `handleUnlinkInvItem`：

```vue
<ProductSkuSection
  :sku-list="skuList"
  ...
  @add-sku="addSku"
  @remove-sku="removeSku"
  @stock-mode-change="handleStockModeChange"
  @inv-item-change="handleInvItemChange"
  @unlink-inv-item="handleUnlinkInvItem"
  @search-inv-items="searchInvItems"
  @open-import-dialog="importDialogVisible = true"
/>
```

---

## 驗證

- `skuList` 空時左側顯示 `el-empty`，右側顯示空狀態
- 新增 SKU 後左側清單立即出現新項並自動選中
- 刪除當前選中 SKU 後，選中自動移到相鄰項
- 驗證錯誤的 SKU 在左側清單以紅色邊線標示
- 解除關聯後 `sku.invItemId` 變為 `undefined`，UI 切換回可搜尋下拉
- 窄螢幕（< 960px）時左右改為上下堆疊
