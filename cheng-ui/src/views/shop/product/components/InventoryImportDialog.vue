<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="dialogTitle"
    width="720px"
    :close-on-click-modal="false"
    destroy-on-close
    @open="handleDialogOpen"
    @closed="handleDialogClosed"
  >
    <!-- Step A：搜尋與多選 -->
    <template v-if="dialogStep === 'select'">
      <!-- 搜尋框 -->
      <el-input
        v-model="searchQuery"
        placeholder="輸入物品名稱、編碼或 #標籤搜尋"
        clearable
        :prefix-icon="Search"
        @input="handleSearch"
      />

      <!-- 篩選列 -->
      <div class="import-filter-bar">
        <span class="import-filter-label">篩選：庫存</span>
        <el-select v-model="filterStock" size="small" style="width: 100px" @change="handleFilterChange">
          <el-option label="全部" value="all" />
          <el-option label="有庫存" value="inStock" />
        </el-select>
      </div>

      <!-- 物品表格 -->
      <el-table
        ref="importTableRef"
        :data="tableData"
        v-loading="tableLoading"
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
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
    </template>

    <!-- Step B：預覽確認 -->
    <template v-if="dialogStep === 'preview'">
      <p class="preview-desc">以下庫存物品將建立為商品規格（SKU），可在匯入前微調。</p>

      <div class="preview-list">
        <div v-for="(item, index) in previewItems" :key="item.itemId" class="preview-card">
          <div class="preview-card__title">{{ index + 1 }}. {{ item.itemName }}</div>
          <el-form label-width="80px" size="small" class="preview-card__form">
            <el-form-item label="規格名稱">
              <el-input v-model="item.editSkuName" />
            </el-form-item>
            <el-form-item label="SKU 編碼">
              <el-input v-model="item.editSkuCode" />
            </el-form-item>
            <el-form-item label="售價">
              <div class="preview-price-row">
                <el-input-number v-model="item.editPrice" :min="0" :precision="0" controls-position="right" style="width: 140px" />
                <span v-if="isItemSafeguarded(item)" class="preview-price-ref preview-price-ref--safeguard">
                  <el-icon><WarningFilled /></el-icon>
                  已套用防呆（原庫存現價 ${{ item.currentPrice ?? 0 }} 過低）
                </span>
                <span v-else-if="item.currentPrice != null" class="preview-price-ref" :class="{ 'preview-price-ref--zero': !item.currentPrice }">
                  ← 庫存現價 ${{ item.currentPrice }}
                  <template v-if="!item.currentPrice">（尚未定價）</template>
                </span>
              </div>
            </el-form-item>
            <el-form-item label="成本價">
              <div class="preview-price-row">
                <el-input-number
                  v-model="item.editCostPrice"
                  :min="0"
                  :max="item.editPrice > 0 ? item.editPrice : undefined"
                  :precision="0"
                  controls-position="right"
                  style="width: 140px"
                  :class="{ 'preview-input--error': isCostExceeding(item) }"
                />
                <span v-if="item.purchasePrice != null" class="preview-price-ref" :class="{ 'preview-price-ref--zero': !item.purchasePrice }">
                  ← 採購成本 ${{ item.purchasePrice }}
                  <template v-if="!item.purchasePrice">（尚未設定）</template>
                </span>
                <span v-if="isCostExceeding(item)" class="preview-cost-error">
                  <el-icon><WarningFilled /></el-icon>
                  超過售價
                </span>
              </div>
            </el-form-item>
            <el-form-item label="庫存">
              <span class="preview-stock-readonly">{{ item.currentStock }}（自動同步，不可編輯）</span>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 勾選項 -->
      <div class="preview-options">
        <el-checkbox v-model="importOptions.syncPrice">使用庫存現價作為售價（取消勾選則售價留空，稍後設定）</el-checkbox>
        <div v-if="importOptions.syncPrice && safeguardCount > 0" class="preview-options__warn preview-options__warn--safeguard">
          <el-icon><WarningFilled /></el-icon>
          {{ safeguardCount }} 個物品的庫存現價低於 ${{ MIN_TRUST_PRICE }}，已自動以採購成本推算售價（加成 {{ markupPct }}%），可手動覆寫
        </div>
        <div v-if="zeroPriceEditCount > 0" class="preview-options__warn preview-options__warn--zero">
          <el-icon><WarningFilled /></el-icon>
          {{ zeroPriceEditCount }} 個物品的售價為 $0，匯入後仍需於儲存時確認才能建立為 0 元商品
        </div>
        <el-checkbox v-model="importOptions.syncCost">使用採購成本作為成本價</el-checkbox>
        <div v-if="costBlockerCount > 0" class="preview-options__warn preview-options__warn--error">
          <el-icon><WarningFilled /></el-icon>
          {{ costBlockerCount }} 個物品的成本價高於售價，請先調整才能匯入
        </div>
      </div>
    </template>

    <!-- 底部操作 -->
    <template #footer>
      <template v-if="dialogStep === 'select'">
        <span class="import-selected-count">已選 {{ selectedItems.length }} 項</span>
        <div class="import-footer-buttons">
          <el-button @click="$emit('update:visible', false)">取消</el-button>
          <el-button type="primary" :disabled="selectedItems.length === 0" @click="goToPreview">下一步</el-button>
        </div>
      </template>
      <template v-else>
        <div class="import-footer-buttons">
          <el-button @click="goBackToSelect">返回選擇</el-button>
          <el-button type="primary" :disabled="hasImportBlocker" @click="handleConfirm">確認匯入</el-button>
        </div>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { Search, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listItemWithStock } from '@/api/inventory/item'
import {
  safeSkuPriceFromInventory,
  isPriceSafeguardApplied,
  MIN_TRUST_PRICE,
  MARKUP_RATIO
} from '../composables/priceSafeguard'

const props = defineProps({
  visible: { type: Boolean, default: false },
  existingInvItemIds: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:visible', 'confirm'])

// === 內部狀態 ===
const dialogStep = ref('select')  // 'select' | 'preview'

// Step A：搜尋與多選
const searchQuery = ref('')
const searchTimer = ref(null)
const tableData = ref([])
const tableLoading = ref(false)
const selectedItems = ref([])
const filterStock = ref('all')
const importTableRef = ref(null)

// Step B：預覽確認
const previewItems = ref([])
const importOptions = reactive({ syncPrice: true, syncCost: true })

// === 計算屬性 ===
const dialogTitle = computed(() =>
  dialogStep.value === 'select'
    ? '從庫存匯入'
    : `確認匯入 — ${previewItems.value.length} 個品項`
)

/** 防呆觸發的品項數（用於頂部批次提示） */
const safeguardCount = computed(() =>
  previewItems.value.filter(item =>
    isPriceSafeguardApplied(item.currentPrice, item.purchasePrice)
  ).length
)

/** 防呆加成百分比（例如 20） */
const markupPct = Math.round((MARKUP_RATIO - 1) * 100)

/** 判斷單一品項目前是否顯示「已套用防呆」狀態（使用者手動覆寫回原價時隱藏） */
function isItemSafeguarded(item) {
  if (!importOptions.syncPrice) return false
  if (!isPriceSafeguardApplied(item.currentPrice, item.purchasePrice)) return false
  return Number(item.editPrice) !== (Number(item.currentPrice) || 0)
}

/** 判斷單一品項成本價是否高於售價（驗證 blocker） */
function isCostExceeding(item) {
  const price = Number(item.editPrice)
  const cost = Number(item.editCostPrice)
  if (!Number.isFinite(cost) || cost <= 0) return false
  if (!Number.isFinite(price)) return false
  return cost > price
}

/** 目前預覽中售價為 $0 的品項數（匯入後於儲存時需再次確認） */
const zeroPriceEditCount = computed(() =>
  previewItems.value.filter(item => {
    const p = Number(item.editPrice)
    return !Number.isFinite(p) || p <= 0
  }).length
)

/** 成本價高於售價的品項數（硬阻擋，不可匯入） */
const costBlockerCount = computed(() =>
  previewItems.value.filter(isCostExceeding).length
)

/** 是否存在阻擋匯入的錯誤 */
const hasImportBlocker = computed(() => costBlockerCount.value > 0)

// === 搜尋邏輯 ===
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
    query.stockStatus = '0'
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
        isAlreadyLinked: props.existingInvItemIds.includes(item.itemId)
      }))
    })
    .finally(() => { tableLoading.value = false })
}

function handleFilterChange() {
  const params = {}
  if (searchQuery.value?.trim()) {
    if (searchQuery.value.startsWith('#')) {
      const tagName = searchQuery.value.substring(1).trim()
      if (tagName) params.tagName = tagName
    } else {
      params.keyword = searchQuery.value.trim()
    }
  }
  loadItems(params)
}

function handleSelectionChange(selection) {
  selectedItems.value = selection
}

function tableRowClassName({ row }) {
  if (row.isAlreadyLinked) return 'is-linked'
  if (row.currentStock === 0) return 'is-empty-stock'
  return ''
}

// === Step 切換 ===
function goToPreview() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('請至少選擇一個物品')
    return
  }

  previewItems.value = selectedItems.value.map(item => ({
    ...item,
    editSkuName: item.itemName,
    editSkuCode: item.itemCode,
    editPrice: importOptions.syncPrice
      ? safeSkuPriceFromInventory(item.currentPrice, item.purchasePrice)
      : 0,
    editCostPrice: importOptions.syncCost ? (item.purchasePrice || undefined) : undefined
  }))

  dialogStep.value = 'preview'
}

function goBackToSelect() {
  dialogStep.value = 'select'
}

// === 勾選項 watch：即時更新 previewItems 預設值 ===
watch(() => importOptions.syncPrice, (sync) => {
  previewItems.value.forEach(item => {
    item.editPrice = sync
      ? safeSkuPriceFromInventory(item.currentPrice, item.purchasePrice)
      : 0
  })
})

watch(() => importOptions.syncCost, (sync) => {
  previewItems.value.forEach(item => {
    item.editCostPrice = sync ? (item.purchasePrice || undefined) : undefined
  })
})

// === 確認匯入 ===
function handleConfirm() {
  if (hasImportBlocker.value) {
    ElMessage.error(`有 ${costBlockerCount.value} 個物品的成本價高於售價，請先調整`)
    return
  }
  const items = previewItems.value.map(item => ({
    ...item,
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

// === Dialog 生命週期 ===
function handleDialogOpen() {
  dialogStep.value = 'select'
  selectedItems.value = []
  previewItems.value = []
  importOptions.syncPrice = true
  importOptions.syncCost = true
  loadItems()
}

function handleDialogClosed() {
  searchQuery.value = ''
  tableData.value = []
  filterStock.value = 'all'
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
    searchTimer.value = null
  }
}
</script>

<style scoped>
/* === 篩選列 === */
.import-filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0;
}
.import-filter-label {
  font-size: 13px;
  color: var(--color-text-2, #4E5969);
}

/* === 表格行狀態 === */
:deep(.el-table__row.is-linked) {
  background: var(--color-bg-3, #F2F3F5);
  opacity: 0.6;
}
:deep(.el-table__row.is-empty-stock) {
  background: var(--color-bg-2, #F7F8FA);
}

/* === 提示文字 === */
.import-hint {
  font-size: 11px;
  margin-top: 2px;
}
.import-hint--linked {
  color: var(--color-primary, #409EFF);
}
.import-hint--empty {
  color: var(--color-text-4, #C9CDD4);
}
.import-tags {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}
.text-danger {
  color: var(--color-error, #F53F3F);
}

/* === 底部操作列 === */
:deep(.el-dialog__footer) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.import-selected-count {
  font-size: 13px;
  color: var(--color-text-2, #4E5969);
  font-weight: 500;
}
.import-footer-buttons {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

/* === Step B 預覽 === */
.preview-desc {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--color-text-3, #86909C);
  line-height: 1.6;
}
.preview-list {
  max-height: 420px;
  overflow-y: auto;
}
.preview-card {
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px);
  background: var(--color-bg-1, #FFFFFF);
}
.preview-card:last-child {
  margin-bottom: 0;
}
.preview-card__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  margin-bottom: 12px;
}
.preview-card__form {
  margin-bottom: 0;
}
.preview-card__form :deep(.el-form-item) {
  margin-bottom: 10px;
}
.preview-card__form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
.preview-price-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.preview-price-ref {
  font-size: 12px;
  color: var(--color-text-3, #86909C);
  white-space: nowrap;
}
.preview-price-ref--zero {
  color: var(--color-warning, #FF7D00);
}
.preview-price-ref--safeguard {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-warning, #FF7D00);
  font-weight: 500;
}
.preview-options__warn--safeguard {
  color: var(--color-warning, #FF7D00);
  font-weight: 500;
}
.preview-stock-readonly {
  font-size: 13px;
  color: var(--color-text-3, #86909C);
}

/* === 勾選項 === */
.preview-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #E5E6EB);
}
.preview-options__warn {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 24px;
  font-size: 12px;
  color: var(--color-warning, #FF7D00);
  line-height: 1.4;
}
.preview-options__warn--zero {
  color: var(--color-warning, #FF7D00);
}
.preview-options__warn--error {
  color: var(--color-error, #F53F3F);
  font-weight: 500;
}
.preview-cost-error {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-error, #F53F3F);
  font-weight: 500;
  white-space: nowrap;
}
.preview-input--error :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--color-error, #F53F3F) inset;
}
</style>
