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
          </div>
          <div class="filter-right">
            <el-radio-group v-model="sortType" @change="handleSort" size="small">
              <el-radio-button value="default">預設</el-radio-button>
              <el-radio-button value="sales">銷量</el-radio-button>
              <el-radio-button value="priceAsc">價格↑</el-radio-button>
              <el-radio-button value="priceDesc">價格↓</el-radio-button>
              <el-radio-button value="newest">最新</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 瀑布流商品列表 -->
        <div class="waterfall-container" ref="waterfallRef">
          <div class="waterfall-grid" v-if="productList.length > 0">
            <div
              v-for="product in productList"
              :key="product.productId"
              class="waterfall-item"
              @click="goProductDetail(product.productId)"
            >
              <div class="product-card">
                <div class="product-image-wrapper">
                  <img
                    :src="getImageUrl(product.mainImage)"
                    :alt="product.title"
                    class="product-image"
                    loading="lazy"
                  />
                  <div class="product-tags">
                    <span class="tag tag-new" v-if="product.isNew">新品</span>
                    <span class="tag tag-hot" v-if="product.isHot">熱門</span>
                  </div>
                </div>
                <div class="product-info">
                  <h3 class="product-title">{{ product.title }}</h3>
                  <p class="product-subtitle" v-if="product.subTitle">{{ product.subTitle }}</p>
                  <div class="product-price-row">
                    <span class="current-price">NT$ {{ formatPrice(product.price) }}</span>
                    <span class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
                      NT$ {{ formatPrice(product.originalPrice) }}
                    </span>
                  </div>
                  <div class="product-stats">
                    <span>銷量 {{ product.salesCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
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
import { Menu, Close, Loading } from '@element-plus/icons-vue'
import { listProducts, listCategories } from '@/api/shop/front'

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

let intersectionObserver = null

const treeProps = {
  children: 'children',
  label: 'name'
}

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
  // 過濾出根分類（parentId 為 null、0 或 undefined），並排除名為「全部商品」的重複分類
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

// 過濾根分類（parentId 為 null、0 或 undefined）
function isRootCategory(category) {
  return !category.parentId || category.parentId === 0
}

// 獲取指定分類及其所有子分類的 ID
function getAllDescendantIds(categoryId) {
  const ids = [categoryId]
  const children = categories.value.filter(c => c.parentId === categoryId)
  for (const child of children) {
    ids.push(...getAllDescendantIds(child.categoryId))
  }
  return ids
}

function getImageUrl(url) {
  if (!url) return '/placeholder-image.png'
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function formatPrice(price) {
  return Number(price || 0).toLocaleString('zh-TW')
}

function goProductDetail(productId) {
  router.push(`/mall/product/${productId}`)
}

function handleCategoryClick(data) {
  if (data.categoryId) {
    // 獲取該分類及所有子分類的 ID
    queryParams.categoryIds = getAllDescendantIds(data.categoryId).join(',')
    currentCategory.value = data
  } else {
    // 全部商品
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

    // 檢查是否還有更多數據
    hasMore.value = productList.value.length < total.value
    queryParams.pageNum++
  } catch (error) {
    console.error('載入商品失敗', error)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
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
    // 獲取該分類及所有子分類的 ID
    queryParams.categoryIds = getAllDescendantIds(catId).join(',')
    // 設置當前分類
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
  // 支援 keyword 或 title 參數
  if (keyword) queryParams.title = keyword
  if (title) queryParams.title = title
  if (isHot) queryParams.isHot = true
  if (isNew) queryParams.isNew = true
  if (isRecommend) queryParams.isRecommend = true
}

// 設置 Intersection Observer 用於無限滾動
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
  await loadCategories()
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

/* 左側分類側邊欄 */
.category-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  background: var(--mall-card-bg, white);
  z-index: 1;
}

.category-tree {
  padding: 8px 0;
}

.category-tree :deep(.el-tree-node__content) {
  height: 40px;
  padding: 0 12px;
}

.category-tree :deep(.el-tree-node__content:hover) {
  background: var(--mall-primary-light, #ecf5ff);
}

.category-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: var(--mall-primary-light, #ecf5ff);
  color: var(--mall-primary, #409eff);
  font-weight: 500;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.node-label {
  flex: 1;
}

.node-count {
  font-size: 12px;
  color: #909399;
}

/* 右側商品區 */
.product-area {
  flex: 1;
  min-width: 0;
}

/* 篩選區 */
.filter-section {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-category {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--mall-primary-light, #ecf5ff);
  border-radius: 20px;
  color: var(--mall-primary, #409eff);
  font-weight: 500;
}

.all-products {
  font-weight: 500;
  color: #303133;
}

/* 瀑布流佈局 */
.waterfall-container {
  min-height: 400px;
}

.waterfall-grid {
  column-count: 4;
  column-gap: 20px;
}

.waterfall-item {
  break-inside: avoid;
  margin-bottom: 20px;
}

.product-card {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.product-image-wrapper {
  position: relative;
  overflow: hidden;
}

.product-image {
  width: 100%;
  display: block;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 6px;
}

.tag {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  color: white;
}

.tag-new {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tag-hot {
  background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
}

.product-info {
  padding: 14px;
}

.product-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-subtitle {
  font-size: 12px;
  color: #909399;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.current-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.product-stats {
  font-size: 12px;
  color: #909399;
}

/* 載入更多 */
.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #909399;
  font-size: 14px;
}

.load-more-trigger {
  height: 1px;
}

/* 響應式 */
@media (max-width: 1400px) {
  .waterfall-grid {
    column-count: 3;
  }
}

@media (max-width: 1100px) {
  .waterfall-grid {
    column-count: 2;
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
    background: #f5f7fa;
    border-radius: 20px;
  }

  .category-tree :deep(.el-tree-node__expand-icon) {
    display: none;
  }

  .waterfall-grid {
    column-count: 2;
    column-gap: 12px;
  }

  .waterfall-item {
    margin-bottom: 12px;
  }
}

@media (max-width: 500px) {
  .waterfall-grid {
    column-count: 2;
    column-gap: 10px;
  }

  .product-info {
    padding: 10px;
  }

  .product-title {
    font-size: 13px;
  }

  .current-price {
    font-size: 14px;
  }
}
</style>
