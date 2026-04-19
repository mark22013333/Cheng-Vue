<template>
  <div class="sku-section">
    <!-- 頂部操作列 -->
    <div class="sku-section__toolbar">
      <el-button type="primary" plain icon="Box" @click="emit('open-import-dialog')">
        從庫存匯入
      </el-button>
      <el-button icon="Plus" @click="emit('add-sku')">
        手動新增
      </el-button>
    </div>

    <!-- SKU 全域錯誤 -->
    <div v-if="globalError" class="sku-section__global-error">
      <el-alert :title="globalError" type="error" show-icon :closable="false" />
    </div>

    <!-- Master-Detail 主體 -->
    <div class="sku-section__body">
      <!-- 左側：SKU 清單 -->
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

      <!-- 右側：編輯表單 -->
      <div class="sku-section__detail">
        <SkuCard
          v-if="selectedSku"
          :key="`sku-card-${effectiveIndex}`"
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
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import SkuCard from './SkuCard.vue'
import SkuListItem from './SkuListItem.vue'

const props = defineProps({
  skuList: { type: Array, required: true },
  invItemOptions: { type: Array, default: () => [] },
  invItemLoading: { type: Boolean, default: false },
  skuErrors: { type: Array, default: () => [] },
  showPrice: { type: Boolean, default: false }
})

const emit = defineEmits([
  'update:skuList', 'add-sku', 'remove-sku',
  'stock-mode-change', 'inv-item-change', 'search-inv-items',
  'open-import-dialog', 'unlink-inv-item'
])

// === 本地選中狀態 ===
const selectedIndex = ref(0)

const effectiveIndex = computed(() => {
  if (props.skuList.length === 0) return -1
  if (selectedIndex.value < 0) return 0
  if (selectedIndex.value >= props.skuList.length) return props.skuList.length - 1
  return selectedIndex.value
})

const selectedSku = computed(() =>
  effectiveIndex.value >= 0 ? props.skuList[effectiveIndex.value] : null
)

// 新增 SKU 時自動選中新項
watch(
  () => props.skuList.length,
  (newLen, oldLen) => {
    if (newLen > oldLen) {
      selectedIndex.value = newLen - 1
    } else if (newLen < oldLen && selectedIndex.value >= newLen) {
      selectedIndex.value = Math.max(0, newLen - 1)
    }
  }
)

// === 錯誤處理 ===
/** 全域錯誤（index === -1 的錯誤） */
const globalError = computed(() => {
  const err = props.skuErrors.find(e => e.index === -1)
  return err?.message
})

/** 取得指定 SKU index 的錯誤 */
function skuErrorsForIndex(index) {
  return props.skuErrors.filter(e => e.index === index)
}

/** 該 SKU 是否有任何錯誤（左側清單紅色邊線標示） */
function indexHasError(index) {
  return props.skuErrors.some(e => e.index === index)
}

// === SKU 操作 ===
/** 更新單一 SKU */
function handleSkuUpdate(index, val) {
  const newList = [...props.skuList]
  newList[index] = val
  emit('update:skuList', newList)
}

/** 刪除 SKU（處理選中 shift） */
function handleRemove(index) {
  const currentSelected = selectedIndex.value
  emit('remove-sku', index)
  // 刪除的 index 在當前選中之前 → 選中要往前挪一格保持指向同一個 SKU
  if (index < currentSelected) {
    selectedIndex.value = currentSelected - 1
  }
  // 其他情況（刪除目前選中或更後面）由 length watcher 負責處理
}
</script>

<style scoped>
.sku-section__toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f0f6ff;
  border: 1px solid #d6e4ff;
  border-radius: var(--radius-md, 10px);
}
.sku-section__global-error {
  margin-bottom: 12px;
}

/* === Master-Detail 主體 === */
.sku-section__body {
  display: flex;
  gap: 0;
  min-height: 480px;
  border: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px);
  background: var(--color-bg-1, #FFFFFF);
  overflow: hidden;
}

/* === 左側 Master 清單 === */
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

/* === 右側 Detail 編輯區 === */
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
  min-height: 400px;
  color: var(--color-text-3, #86909C);
}

/* 右側的 SkuCard 佔滿寬度且去除原本的 margin-bottom */
.sku-section__detail :deep(.sku-card) {
  margin-bottom: 0;
  border: none;
}
.sku-section__detail :deep(.sku-card__header) {
  padding-top: 0;
}

/* === 響應式：窄螢幕切為上下堆疊 === */
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
</style>
