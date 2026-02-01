<template>
  <div class="product-picker">
    <!-- Trigger area: shows selected product or prompt -->
    <div class="picker-trigger" @click="openDialog">
      <div v-if="selectedProduct" class="selected-preview">
        <el-image
          v-if="selectedProduct.mainImage"
          :src="getImageUrl(selectedProduct.mainImage)"
          class="preview-thumb"
          fit="cover"
        />
        <div v-else class="preview-thumb preview-thumb--empty">
          <el-icon :size="16"><Goods /></el-icon>
        </div>
        <div class="preview-info">
          <span class="preview-title">{{ selectedProduct.title }}</span>
          <span class="preview-price">NT$ {{ selectedProduct.price }}</span>
        </div>
        <el-icon class="preview-swap"><Switch /></el-icon>
      </div>
      <div v-else class="trigger-empty">
        <el-icon :size="16"><Search /></el-icon>
        <span>點擊搜尋並選擇商品</span>
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog
      :model-value="dialogVisible"
      title="選擇商品"
      width="800px"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
      class="picker-dialog"
      @update:model-value="handleDialogVisibleChange"
      @open="onDialogOpen"
    >
      <!-- Search bar -->
      <div class="picker-toolbar">
        <el-input
          ref="searchInputRef"
          v-model="searchTitle"
          placeholder="輸入商品名稱搜尋..."
          clearable
          class="toolbar-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-tree-select
          v-model="searchCategoryId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'categoryId', children: 'children' }"
          placeholder="全部分類"
          clearable
          check-strictly
          :render-after-expand="false"
          class="toolbar-category"
          @change="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜尋</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- Product table -->
      <el-table
        ref="tableRef"
        v-loading="tableLoading"
        :data="productList"
        highlight-current-row
        :row-class-name="rowClassName"
        class="picker-table"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column width="44" align="center">
          <template #default="{ row }">
            <el-radio
              :model-value="tempSelectedId"
              :value="row.productId"
              @update:model-value="handleRowClick(row)"
            >
              <template #default>&nbsp;</template>
            </el-radio>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                v-if="row.mainImage"
                :src="getImageUrl(row.mainImage)"
                class="product-cell__img"
                fit="cover"
                :preview-src-list="[getImageUrl(row.mainImage)]"
                preview-teleported
                @click.stop
              />
              <div v-else class="product-cell__img product-cell__img--empty">
                <el-icon :size="18" color="#c0c4cc"><Goods /></el-icon>
              </div>
              <div class="product-cell__info">
                <span class="product-cell__title">{{ row.title }}</span>
                <span v-if="row.subTitle" class="product-cell__subtitle">{{ row.subTitle }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="價格" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">NT$ {{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="狀態" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ON_SALE' ? 'success' : 'info'"
              size="small"
              effect="light"
            >
              {{ row.status === 'ON_SALE' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="銷量" prop="salesCount" width="80" align="center">
          <template #default="{ row }">
            <span class="meta-text">{{ row.salesCount || 0 }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="picker-footer-bar">
        <div class="footer-hint">
          <template v-if="tempSelectedProduct">
            <span class="hint-label">已選：</span>
            <span class="hint-value">{{ tempSelectedProduct.title }}</span>
          </template>
          <template v-else>
            <span class="hint-label">共 {{ total }} 筆，點擊列選擇商品</span>
          </template>
        </div>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          small
          background
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>

      <template #footer>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :disabled="!tempSelectedId" @click="handleConfirm">
          確定選擇
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { Search, Refresh, Goods, Switch } from '@element-plus/icons-vue'
import { listProduct, getProduct } from '@/api/shop/product'
import { treeCategory } from '@/api/shop/category'

const props = defineProps({
  modelValue: { type: [Number, String], default: undefined },
  visible: { type: Boolean, default: false },
  onlySale: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'update:visible', 'confirm'])

// --- State ---
const dialogVisible = ref(false)
const tableLoading = ref(false)
const productList = ref([])
const categoryTree = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchTitle = ref('')
const searchCategoryId = ref(undefined)
const searchInputRef = ref(null)

// 暫存選擇（Dialog 內部用，確認後才 emit）
const tempSelectedId = ref(undefined)
const tempSelectedProduct = ref(null)

// 顯示在觸發區的已確認商品
const selectedProduct = ref(null)

// --- Watchers ---
watch(() => props.visible, (val) => {
  dialogVisible.value = val
})

watch(() => props.modelValue, (val) => {
  if (val && !selectedProduct.value) {
    loadSelectedProduct(val)
  } else if (!val) {
    selectedProduct.value = null
  }
}, { immediate: true })

// --- Methods ---
function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function formatPrice(price) {
  return Number(price || 0).toLocaleString('zh-TW')
}

function openDialog() {
  dialogVisible.value = true
  emit('update:visible', true)
}

function handleDialogVisibleChange(val) {
  dialogVisible.value = val
  emit('update:visible', val)
}

function onDialogOpen() {
  tempSelectedId.value = props.modelValue || undefined
  tempSelectedProduct.value = selectedProduct.value ? { ...selectedProduct.value } : null
  loadCategories()
  loadProducts()
  nextTick(() => {
    searchInputRef.value?.focus()
  })
}

async function loadCategories() {
  try {
    const res = await treeCategory()
    categoryTree.value = res.data || []
  } catch {
    categoryTree.value = []
  }
}

/**
 * 從樹中找到指定節點，收集該節點及所有子孫節點的 categoryId
 */
function collectDescendantIds(tree, targetId) {
  for (const node of tree) {
    if (node.categoryId === targetId) {
      const ids = []
      const collect = (n) => {
        ids.push(n.categoryId)
        if (n.children) n.children.forEach(collect)
      }
      collect(node)
      return ids
    }
    if (node.children && node.children.length > 0) {
      const found = collectDescendantIds(node.children, targetId)
      if (found) return found
    }
  }
  return null
}

async function loadProducts() {
  tableLoading.value = true
  try {
    const query = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchTitle.value) query.title = searchTitle.value
    if (searchCategoryId.value) {
      // 收集選中分類及其所有子分類的 ID
      const ids = collectDescendantIds(categoryTree.value, searchCategoryId.value)
      if (ids && ids.length > 0) {
        query.categoryIds = ids.join(',')
      } else {
        query.categoryIds = String(searchCategoryId.value)
      }
    }
    if (props.onlySale) query.status = 'ON_SALE'
    const res = await listProduct(query)
    productList.value = res.rows || []
    total.value = res.total || 0
  } catch {
    productList.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

async function loadSelectedProduct(productId) {
  try {
    const res = await getProduct(productId)
    if (res.data) {
      selectedProduct.value = res.data
    }
  } catch {
    // 商品可能已被刪除
  }
}

function handleSearch() {
  pageNum.value = 1
  loadProducts()
}

function handleReset() {
  searchTitle.value = ''
  searchCategoryId.value = undefined
  pageNum.value = 1
  loadProducts()
}

function handleRowClick(row) {
  tempSelectedId.value = row.productId
  tempSelectedProduct.value = row
}

function handleRowDblClick(row) {
  tempSelectedId.value = row.productId
  tempSelectedProduct.value = row
  handleConfirm()
}

function rowClassName({ row }) {
  return row.productId === tempSelectedId.value ? 'picker-row--selected' : ''
}

function handleConfirm() {
  if (!tempSelectedProduct.value) return
  const product = tempSelectedProduct.value
  selectedProduct.value = { ...product }
  emit('update:modelValue', product.productId)
  emit('confirm', {
    productId: product.productId,
    title: product.title,
    mainImage: product.mainImage,
    price: product.price
  })
  dialogVisible.value = false
  emit('update:visible', false)
}

function handleCancel() {
  dialogVisible.value = false
  emit('update:visible', false)
}
</script>

<style scoped>
/* === Trigger === */
.picker-trigger {
  display: flex;
  align-items: center;
  min-height: 40px;
  padding: 6px 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}

.picker-trigger:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.selected-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.preview-thumb {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  flex-shrink: 0;
  overflow: hidden;
}

.preview-thumb--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #c0c4cc;
}

.preview-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.preview-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-price {
  font-size: 12px;
  color: #909399;
}

.preview-swap {
  flex-shrink: 0;
  color: #909399;
  transition: color 0.2s;
}

.picker-trigger:hover .preview-swap {
  color: #409eff;
}

.trigger-empty {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #a8abb2;
  font-size: 13px;
  padding: 2px 0;
}

/* === Dialog Toolbar === */
.picker-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.toolbar-search {
  flex: 1;
}

.toolbar-category {
  width: 200px;
}

/* === Table === */
.picker-table {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.picker-table) .el-table__row {
  cursor: pointer;
}

:deep(.picker-table) .picker-row--selected td {
  background-color: #ecf5ff !important;
}

:deep(.picker-table) .el-radio__label {
  display: none;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-cell__img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
  overflow: hidden;
}

.product-cell__img--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
}

.product-cell__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.product-cell__title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-cell__subtitle {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-text {
  font-size: 13px;
  font-weight: 600;
  color: #e6522c;
  font-variant-numeric: tabular-nums;
}

.meta-text {
  font-size: 12px;
  color: #909399;
}

/* === Footer bar === */
.picker-footer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.footer-hint {
  font-size: 12px;
  color: #909399;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 280px;
}

.hint-label {
  color: #909399;
}

.hint-value {
  color: #409eff;
  font-weight: 500;
}
</style>
