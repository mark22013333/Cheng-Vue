<template>
  <div class="product-list-page">
    <div class="page-layout">
      <!-- 左側分類側邊欄 -->
      <aside class="category-sidebar">
        <div class="sidebar-header">
          <el-icon><Menu /></el-icon>
          <span>商品分類</span>
        </div>
        <el-tree
          ref="categoryTreeRef"
          :data="categoryTree"
          :props="treeProps"
          node-key="categoryId"
          highlight-current
          :expand-on-click-node="false"
          :default-expand-all="true"
          @node-click="handleCategoryClick"
          class="category-tree"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span class="node-label">{{ data.name }}</span>
              <span class="node-count" v-if="data.productCount">({{ data.productCount }})</span>
            </span>
          </template>
        </el-tree>
      </aside>

      <!-- 右側商品區 -->
      <div class="product-area">
        <!-- 篩選區 -->
        <div class="filter-section">
          <div class="filter-left">
            <div class="current-category" v-if="currentCategory">
              <span class="category-path">{{ currentCategory.name }}</span>
              <el-button text size="small" @click="clearCategory">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <span v-else class="all-products">全部商品</span>
            <span class="product-count">共 {{ total }} 件</span>
          </div>
          <div class="filter-right">
            <div class="sort-tabs">
              <button
                v-for="opt in sortOptions"
                :key="opt.value"
                class="sort-tab"
                :class="{ active: sortType === opt.value }"
                @click="sortType = opt.value; handleSort()"
              >
                {{ opt.label }}
              </button>
            </div>
            <div class="view-toggle">
              <button
                class="view-btn"
                :class="{ active: gridCols === 3 }"
                @click="gridCols = 3"
                title="3 欄"
              >
                <el-icon><Grid /></el-icon>
              </button>
              <button
                class="view-btn"
                :class="{ active: gridCols === 4 }"
                @click="gridCols = 4"
                title="4 欄"
              >
                <el-icon><Menu /></el-icon>
              </button>
            </div>
          </div>
        </div>

        <!-- 商品 Grid -->
        <div class="product-grid-container" ref="waterfallRef">
          <div
            class="product-grid"
            v-if="productList.length > 0"
            :style="{ gridTemplateColumns: `repeat(${gridCols}, 1fr)` }"
          >
            <ProductCard
              v-for="product in productList"
              :key="product.productId"
              :product="product"
              @click="goProductDetail(product.productId)"
            />
          </div>

          <!-- 載入中提示 -->
          <div class="loading-more" v-if="loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>載入中...</span>
          </div>

          <!-- 無更多數據 -->
          <div class="no-more" v-if="!loading && !hasMore && productList.length > 0">
            <span>— 已經到底了 —</span>
          </div>

          <!-- 空狀態 -->
          <el-empty v-if="!loading && productList.length === 0" description="暫無商品" />
        </div>

        <!-- 滾動觸發器 -->
        <div ref="loadMoreTrigger" class="load-more-trigger"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, Close, Loading, Grid } from '@element-plus/icons-vue'
import { listProducts, listCategories } from '@/api/shop/front'
import ProductCard from '@/views/shop-front/components/ProductCard.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const productList = ref([])
const categories = ref([])
const total = ref(0)
const hasMore = ref(true)
const sortType = ref('default')
const currentCategory = ref(null)
const categoryTreeRef = ref(null)
const waterfallRef = ref(null)
const loadMoreTrigger = ref(null)
const gridCols = ref(4)

let intersectionObserver = null

const treeProps = {
  children: 'children',
  label: 'name'
}

const sortOptions = [
  { label: '預設', value: 'default' },
  { label: '銷量', value: 'sales' },
  { label: '價格低→高', value: 'priceAsc' },
  { label: '價格高→低', value: 'priceDesc' },
  { label: '最新', value: 'newest' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  categoryIds: undefined,
  title: undefined,
  isHot: undefined,
  isNew: undefined,
  isRecommend: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

// 將分類列表轉換為樹狀結構
const categoryTree = computed(() => {
  const allNode = { categoryId: null, name: '全部商品', children: [] }
  const rootCategories = categories.value.filter(c => isRootCategory(c) && c.name !== '全部商品')
  const tree = rootCategories.map(item => ({
    ...item,
    children: buildTree(categories.value, item.categoryId)
  })).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return [allNode, ...tree]
})

function buildTree(items, parentId) {
  return items
    .filter(item => item.parentId === parentId)
    .map(item => ({
      ...item,
      children: buildTree(items, item.categoryId)
    }))
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

function isRootCategory(category) {
  return !category.parentId || category.parentId === 0
}

function getAllDescendantIds(categoryId) {
  const ids = [categoryId]
  const children = categories.value.filter(c => c.parentId === categoryId)
  for (const child of children) {
    ids.push(...getAllDescendantIds(child.categoryId))
  }
  return ids
}

function goProductDetail(productId) {
  router.push(`/product/${productId}`)
}

function handleCategoryClick(data) {
  if (data.categoryId) {
    queryParams.categoryIds = getAllDescendantIds(data.categoryId).join(',')
    currentCategory.value = data
  } else {
    queryParams.categoryIds = undefined
    currentCategory.value = null
  }
  resetAndLoad()
}

function clearCategory() {
  queryParams.categoryIds = undefined
  currentCategory.value = null
  if (categoryTreeRef.value) {
    categoryTreeRef.value.setCurrentKey(null)
  }
  resetAndLoad()
}

function handleSort() {
  switch (sortType.value) {
    case 'sales':
      queryParams.orderByColumn = 'salesCount'
      queryParams.isAsc = 'desc'
      break
    case 'priceAsc':
      queryParams.orderByColumn = 'price'
      queryParams.isAsc = 'asc'
      break
    case 'priceDesc':
      queryParams.orderByColumn = 'price'
      queryParams.isAsc = 'desc'
      break
    case 'newest':
      queryParams.orderByColumn = 'createTime'
      queryParams.isAsc = 'desc'
      break
    default:
      queryParams.orderByColumn = undefined
      queryParams.isAsc = undefined
  }
  resetAndLoad()
}

function resetAndLoad() {
  queryParams.pageNum = 1
  productList.value = []
  hasMore.value = true
  loadProducts()
}

async function loadProducts() {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const res = await listProducts(queryParams)
    const newProducts = res.rows || []
    total.value = res.total || 0

    if (queryParams.pageNum === 1) {
      productList.value = newProducts
    } else {
      productList.value = [...productList.value, ...newProducts]
    }

    hasMore.value = productList.value.length < total.value
    queryParams.pageNum++
  } catch (error) {
    console.error('載入商品失敗', error)
  } finally {
    loading.value = false
  }
}

async function loadCategoriesData() {
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('載入分類失敗', error)
  }
}

function initQueryFromRoute() {
  const { categoryId, keyword, title, isHot, isNew, isRecommend } = route.query
  if (categoryId) {
    const catId = Number(categoryId)
    queryParams.categoryIds = getAllDescendantIds(catId).join(',')
    nextTick(() => {
      const cat = categories.value.find(c => c.categoryId === catId)
      if (cat) {
        currentCategory.value = cat
        if (categoryTreeRef.value) {
          categoryTreeRef.value.setCurrentKey(catId)
        }
      }
    })
  }
  if (keyword) queryParams.title = keyword
  if (title) queryParams.title = title
  if (isHot) queryParams.isHot = true
  if (isNew) queryParams.isNew = true
  if (isRecommend) queryParams.isRecommend = true
}

function setupIntersectionObserver() {
  if (intersectionObserver) {
    intersectionObserver.disconnect()
  }

  intersectionObserver = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !loading.value && hasMore.value) {
        loadProducts()
      }
    },
    {
      root: null,
      rootMargin: '100px',
      threshold: 0.1
    }
  )

  if (loadMoreTrigger.value) {
    intersectionObserver.observe(loadMoreTrigger.value)
  }
}

watch(() => route.query, () => {
  initQueryFromRoute()
  resetAndLoad()
}, { deep: true })

onMounted(async () => {
  await loadCategoriesData()
  initQueryFromRoute()
  await loadProducts()
  setupIntersectionObserver()
})

onUnmounted(() => {
  if (intersectionObserver) {
    intersectionObserver.disconnect()
  }
})
</script>

<style scoped>
.product-list-page {
  min-height: calc(100vh - 200px);
}

.page-layout {
  display: flex;
  gap: 24px;
}

/* === 側邊欄 === */
.category-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  height: fit-content;
  position: sticky;
  top: 94px;
  max-height: calc(100vh - 118px);
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--mall-text-primary, #303133);
  border-bottom: 1px solid var(--mall-border-color, #E8E4DF);
  position: sticky;
  top: 0;
  background: var(--mall-card-bg, white);
  z-index: 1;
  letter-spacing: 0.5px;
}

.category-tree {
  padding: 8px 0;
  --el-tree-node-hover-bg-color: transparent;
}

.category-tree :deep(.el-tree-node__content) {
  height: 40px;
  padding: 0 12px;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}

.category-tree :deep(.el-tree-node__content:hover) {
  background: var(--mall-body-bg, #FAF8F5);
}

.category-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: var(--mall-body-bg, #FAF8F5);
  border-left-color: var(--mall-primary, #4A6B7C);
  color: var(--mall-primary, #4A6B7C);
  font-weight: 600;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.node-label {
  flex: 1;
  font-size: 13px;
}

.node-count {
  font-size: 12px;
  color: var(--mall-text-muted, #909399);
}

/* === 篩選區 === */
.product-area {
  flex: 1;
  min-width: 0;
}

.filter-section {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-category {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 14px;
  background: var(--mall-body-bg, #FAF8F5);
  border-radius: 20px;
  color: var(--mall-primary, #4A6B7C);
  font-weight: 500;
  font-size: 13px;
}

.all-products {
  font-weight: 600;
  color: var(--mall-text-primary, #303133);
  font-size: 15px;
}

.product-count {
  font-size: 13px;
  color: var(--mall-text-muted, #909399);
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sort-tabs {
  display: flex;
  gap: 4px;
}

.sort-tab {
  background: none;
  border: none;
  padding: 6px 14px;
  font-size: 13px;
  color: var(--mall-text-secondary, #606266);
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
  border-radius: 4px;
}

.sort-tab:hover {
  color: var(--mall-primary, #4A6B7C);
}

.sort-tab.active {
  color: var(--mall-primary, #4A6B7C);
  font-weight: 600;
  background: var(--mall-body-bg, #FAF8F5);
}

.view-toggle {
  display: flex;
  gap: 4px;
  border-left: 1px solid var(--mall-border-color, #E8E4DF);
  padding-left: 12px;
}

.view-btn {
  background: none;
  border: 1px solid transparent;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 4px;
  color: var(--mall-text-muted, #909399);
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.view-btn.active {
  color: var(--mall-primary, #4A6B7C);
  border-color: var(--mall-border-color, #E8E4DF);
  background: var(--mall-body-bg, #FAF8F5);
}

/* === 商品 Grid === */
.product-grid-container {
  min-height: 400px;
}

.product-grid {
  display: grid;
  gap: 20px;
  transition: grid-template-columns 0.3s;
}

/* === 載入更多 === */
.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px;
  color: var(--mall-text-muted, #909399);
  font-size: 13px;
}

.load-more-trigger {
  height: 1px;
}

/* === RWD === */
@media (max-width: 1100px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr) !important;
  }
}

@media (max-width: 900px) {
  .page-layout {
    flex-direction: column;
  }

  .category-sidebar {
    width: 100%;
    position: static;
    max-height: none;
  }

  .category-tree {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px;
  }

  .category-tree :deep(.el-tree-node) {
    display: inline-block;
  }

  .category-tree :deep(.el-tree-node__content) {
    height: auto;
    padding: 8px 16px;
    background: var(--mall-body-bg, #FAF8F5);
    border-radius: 20px;
    border-left: none;
  }

  .category-tree :deep(.el-tree-node__expand-icon) {
    display: none;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 12px;
  }

  .sort-tabs {
    flex-wrap: wrap;
  }

  .view-toggle {
    display: none;
  }
}

@media (max-width: 500px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 10px;
  }

  .filter-section {
    padding: 12px 14px;
  }
}
</style>
