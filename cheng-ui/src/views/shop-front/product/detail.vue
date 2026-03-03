<template>
  <div class="product-detail-page" v-loading="loading">
    <!-- 麵包屑導覽列 -->
    <div class="breadcrumb-section" v-if="product">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首頁</el-breadcrumb-item>
        <el-breadcrumb-item
          v-for="category in categoryPath"
          :key="category.categoryId"
          :to="{ path: '/products', query: { categoryId: category.categoryId } }"
        >
          {{ category.name }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="product-main" v-if="product">
      <!-- 商品圖片區 -->
      <div class="product-gallery">
        <div class="main-image">
          <el-image
            :src="getImageUrl(currentImage)"
            fit="contain"
            :preview-src-list="previewImages"
            :initial-index="currentImageIndex"
            preview-teleported
            hide-on-click-modal
          />
        </div>
        <div class="thumbnail-list" v-if="allImages.length > 1">
          <div
            v-for="(img, index) in allImages"
            :key="index"
            class="thumbnail"
            :class="{ active: currentImage === img }"
            @click="currentImage = img"
          >
            <img :src="getImageUrl(img)" />
          </div>
        </div>
      </div>

      <!-- 商品資訊區 -->
      <div class="product-info">
        <h1 class="product-title">{{ product.title }}</h1>
        <p class="product-subtitle" v-if="product.subTitle">{{ product.subTitle }}</p>

        <div class="price-section">
          <div class="price-row">
            <span class="current-price">NT$ {{ currentPrice }}</span>
            <span
              v-if="selectedSku?.originalPrice && Number(selectedSku.originalPrice) > Number(currentPrice)"
              class="original-price"
            >
              NT$ {{ selectedSku.originalPrice }}
            </span>
            <span
              v-else-if="product.originalDisplayPrice && Number(product.originalDisplayPrice) > Number(currentPrice)"
              class="original-price"
            >
              NT$ {{ product.originalDisplayPrice }}
            </span>
            <span
              v-else-if="product.originalPrice && Number(product.originalPrice) > Number(currentPrice)"
              class="original-price"
            >
              NT$ {{ product.originalPrice }}
            </span>
          </div>
          <span v-if="showDiscountTag" class="discount-tag">{{ product.discountLabel }}</span>
        </div>

        <div v-if="showSaleCountdown" class="sale-countdown">
          <el-icon><Timer /></el-icon>
          <span>特惠倒數：{{ saleCountdownText }}</span>
        </div>

        <div class="meta-section">
          <span>銷量 {{ product.salesCount || 0 }}</span>
          <span class="meta-divider"></span>
          <span>瀏覽 {{ product.viewCount || 0 }}</span>
        </div>

        <!-- SKU 選擇 -->
        <div class="sku-section" v-if="skuList.length > 0">
          <div class="section-label">規格選擇</div>
          <div class="sku-grid">
            <button
              v-for="sku in skuList"
              :key="sku.skuId"
              class="sku-btn"
              :class="{
                active: selectedSkuId === sku.skuId,
                disabled: sku.stockQuantity <= 0
              }"
              :disabled="sku.stockQuantity <= 0"
              @click="selectedSkuId = sku.skuId"
            >
              {{ sku.skuName }}
              <span v-if="sku.stockQuantity <= 0" class="out-of-stock">缺貨</span>
            </button>
          </div>
        </div>

        <!-- 數量選擇 -->
        <div class="quantity-section">
          <div class="section-label">購買數量</div>
          <div class="quantity-row">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="selectedSku?.stockQuantity || 99"
              size="large"
            />
            <span class="stock-info" v-if="selectedSku">
              庫存 {{ selectedSku.stockQuantity }} 件
            </span>
          </div>
        </div>

        <!-- 操作按鈕 -->
        <div class="action-buttons">
          <button class="btn-cart" @click="handleAddToCart">
            <el-icon><ShoppingCart /></el-icon>
            加入購物車
          </button>
          <button class="btn-buy" @click="handleBuyNow">
            立即購買
          </button>
        </div>
      </div>
    </div>

    <!-- 商品詳情 -->
    <div class="product-description" v-if="product">
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="商品詳情" name="detail">
          <div class="description-content" v-html="product.description"></div>
          <el-empty v-if="!product.description" description="暫無商品詳情" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 推薦商品 -->
    <div class="recommend-section" v-if="recommendProducts.length > 0">
      <div class="recommend-header">
        <h2 class="recommend-title">你可能也喜歡</h2>
      </div>
      <div class="recommend-grid">
        <ProductCard
          v-for="item in recommendProducts"
          :key="item.productId"
          :product="item"
          @click="goProductDetail(item.productId)"
        />
      </div>
    </div>

    <el-empty v-if="!loading && !product" description="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Timer } from '@element-plus/icons-vue'
import { getProduct, getProductSkus, listCategories, listProducts } from '@/api/shop/front'
import { useCartStore } from '@/store/modules/cart'
import { getMemberToken } from '@/utils/memberAuth'
import ProductCard from '@/views/shop-front/components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const product = ref(null)
const skuList = ref([])
const categories = ref([])
const selectedSkuId = ref(null)
const quantity = ref(1)
const currentImage = ref('')
const activeTab = ref('detail')
const recommendProducts = ref([])

// 計算分類路徑
const categoryPath = computed(() => {
  if (!product.value?.categoryId || categories.value.length === 0) {
    return []
  }

  const path = []
  let currentCatId = product.value.categoryId

  while (currentCatId) {
    const category = categories.value.find(c => c.categoryId === currentCatId)
    if (category) {
      path.unshift(category)
      currentCatId = category.parentId
    } else {
      break
    }
  }

  return path
})

const selectedSku = computed(() => {
  return skuList.value.find(sku => sku.skuId === selectedSkuId.value)
})

const nowTs = ref(Date.now())
let countdownTimer = null
let saleExpiredReloaded = false

function parseDateTimeString(value) {
  if (!value) return null
  try {
    const ts = new Date(String(value).replace(' ', 'T')).getTime()
    return Number.isFinite(ts) ? ts : null
  } catch (e) {
    return null
  }
}

const saleEndTs = computed(() => parseDateTimeString(product.value?.saleEndDate))

const saleRemainingMs = computed(() => {
  if (!saleEndTs.value) return 0
  return saleEndTs.value - nowTs.value
})

const effectiveSalePrice = computed(() => {
  if (selectedSku.value?.salePrice != null) {
    return Number(selectedSku.value.salePrice || 0)
  }
  return Number(product.value?.salePrice || 0)
})

const hasActiveSalePrice = computed(() => {
  const salePrice = Number(effectiveSalePrice.value || 0)
  if (!(salePrice > 0)) return false
  if (!saleEndTs.value) return true
  return saleRemainingMs.value > 0
})

const currentPrice = computed(() => {
  if (!product.value) return 0

  if (saleEndTs.value && saleRemainingMs.value <= 0) {
    return Number(selectedSku.value?.price || product.value.price || 0)
  }

  if (hasActiveSalePrice.value) {
    return Number(effectiveSalePrice.value || 0)
  }

  if (selectedSku.value?.price != null) {
    return Number(selectedSku.value.price || 0)
  }

  const finalPrice = product.value.finalPrice
  if (finalPrice != null) {
    return Number(finalPrice || 0)
  }

  return Number(product.value.price || 0)
})

const showDiscountTag = computed(() => {
  if (!product.value?.discountLabel) return false

  const original = Number(
    selectedSku.value?.originalPrice ?? product.value.originalDisplayPrice ?? product.value.originalPrice ?? 0
  )
  return original > Number(currentPrice.value)
})

const showSaleCountdown = computed(() => {
  return hasActiveSalePrice.value && !!product.value?.saleEndDate && Number(effectiveSalePrice.value || 0) > 0
})

function formatCountdown(ms) {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  const hh = String(hours).padStart(2, '0')
  const mm = String(minutes).padStart(2, '0')
  const ss = String(seconds).padStart(2, '0')

  if (days > 0) {
    return `${days}天 ${hh}:${mm}:${ss}`
  }
  return `${hh}:${mm}:${ss}`
}

const saleCountdownText = computed(() => formatCountdown(saleRemainingMs.value))

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function startCountdown() {
  stopCountdown()
  nowTs.value = Date.now()
  if (!showSaleCountdown.value) return
  countdownTimer = setInterval(() => {
    nowTs.value = Date.now()
    if (saleEndTs.value && saleRemainingMs.value <= 0 && !saleExpiredReloaded) {
      saleExpiredReloaded = true
      loadProduct()
    }
  }, 1000)
}

const allImages = computed(() => {
  const images = []
  if (product.value?.mainImage) {
    images.push(product.value.mainImage)
  }
  if (product.value?.sliderImages) {
    try {
      const sliders = JSON.parse(product.value.sliderImages)
      images.push(...sliders)
    } catch (e) {
      console.error('解析輪播圖失敗', e)
    }
  }
  return images
})

const previewImages = computed(() => {
  return allImages.value.map(img => getImageUrl(img))
})

const currentImageIndex = computed(() => {
  const index = allImages.value.indexOf(currentImage.value)
  return index >= 0 ? index : 0
})

function getImageUrl(url) {
  if (!url) return 'https://via.placeholder.com/600x600?text=No+Image'
  if (url.startsWith('http')) return url
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}

function goProductDetail(productId) {
  router.push(`/product/${productId}`)
}

async function handleAddToCart() {
  if (!selectedSkuId.value && skuList.value.length > 0) {
    ElMessage.warning('請選擇商品規格')
    return
  }

  const sku = selectedSku.value || skuList.value[0]
  if (!sku) {
    ElMessage.warning('此商品暫無可購買規格')
    return
  }

  const skuInfo = {
    skuId: sku.skuId,
    productId: product.value.productId,
    productTitle: product.value.title,
    skuName: sku.skuName,
    mainImage: product.value.mainImage,
    price: sku.price
  }

  try {
    await cartStore.addToCart(sku.skuId, quantity.value, skuInfo)
    ElMessage.success('已加入購物車')
  } catch (error) {
    ElMessage.error(error.message || '加入購物車失敗')
  }
}

async function handleBuyNow() {
  if (!selectedSkuId.value && skuList.value.length > 0) {
    ElMessage.warning('請選擇商品規格')
    return
  }

  const sku = selectedSku.value || skuList.value[0]
  if (!sku) {
    ElMessage.warning('此商品暫無可購買規格')
    return
  }

  const skuInfo = {
    skuId: sku.skuId,
    productId: product.value.productId,
    productTitle: product.value.title,
    skuName: sku.skuName,
    mainImage: product.value.mainImage,
    price: sku.price
  }

  try {
    if (!getMemberToken()) {
      await cartStore.addToCart(sku.skuId, quantity.value, skuInfo)
      router.push('/login?redirect=/checkout')
      return
    }
    await cartStore.addToCart(sku.skuId, quantity.value, skuInfo)
    router.push('/checkout')
  } catch (error) {
    ElMessage.error(error.message || '操作失敗')
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

async function loadRecommendProducts() {
  if (!product.value?.categoryId) return
  try {
    const res = await listProducts({
      categoryIds: String(product.value.categoryId),
      pageNum: 1,
      pageSize: 4
    })
    recommendProducts.value = (res.rows || []).filter(p => p.productId !== product.value.productId).slice(0, 4)
  } catch (e) {
    recommendProducts.value = []
  }
}

async function loadProduct() {
  const productId = route.params.id
  if (!productId) return

  loading.value = true
  try {
    const res = await getProduct(productId)
    product.value = res.data
    currentImage.value = res.data?.mainImage || ''
    saleExpiredReloaded = false

    const skuRes = await getProductSkus(productId)
    skuList.value = skuRes.data || []
    if (skuList.value.length > 0) {
      const availableSku = skuList.value.find(s => s.stockQuantity > 0)
      if (availableSku) {
        selectedSkuId.value = availableSku.skuId
      }
    }

    loadRecommendProducts()
  } catch (error) {
    console.error('載入商品失敗', error)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, () => {
  loadProduct()
})

watch(showSaleCountdown, () => {
  startCountdown()
})

watch(selectedSkuId, () => {
  startCountdown()
})

onMounted(() => {
  loadCategoriesData()
  loadProduct()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
.product-detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* === 麵包屑 === */
.breadcrumb-section {
  padding: 12px 0;
}

.breadcrumb-section :deep(.el-breadcrumb__item) {
  font-size: 13px;
}

.breadcrumb-section :deep(.el-breadcrumb__inner.is-link) {
  color: var(--mall-text-muted, #909399);
  font-weight: normal;
}

.breadcrumb-section :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--mall-primary, #4A6B7C);
}

/* === 商品主區域 === */
.product-main {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  padding: 32px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

/* === 圖片區 === */
.product-gallery {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.main-image {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f6f3;
}

.main-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.thumbnail {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.thumbnail:hover {
  border-color: var(--mall-border-color, #E8E4DF);
}

.thumbnail.active {
  border-color: var(--mall-primary, #4A6B7C);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* === 商品資訊 === */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--mall-text-primary, #303133);
  margin: 0;
  line-height: 1.4;
  letter-spacing: 0.5px;
}

.product-subtitle {
  font-size: 14px;
  color: var(--mall-text-muted, #909399);
  margin: 0;
  line-height: 1.6;
}

/* === 價格 === */
.price-section {
  background: var(--mall-body-bg, #FAF8F5);
  padding: 18px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--mall-accent, #A5635C);
}

.original-price {
  font-size: 15px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.discount-tag {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  color: var(--mall-accent, #A5635C);
  background: rgba(165, 99, 92, 0.1);
  border: 1px solid rgba(165, 99, 92, 0.2);
  border-radius: 4px;
  font-weight: 600;
}

.sale-countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--mall-accent, #A5635C);
  padding: 8px 12px;
  background: rgba(165, 99, 92, 0.06);
  border-radius: 6px;
}

/* === 統計 === */
.meta-section {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--mall-text-muted, #909399);
}

.meta-divider {
  width: 1px;
  height: 12px;
  background: var(--mall-border-color, #E8E4DF);
}

/* === SKU === */
.section-label {
  font-size: 14px;
  color: var(--mall-text-secondary, #606266);
  margin-bottom: 10px;
  font-weight: 500;
}

.sku-section {
  padding-top: 4px;
}

.sku-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sku-btn {
  padding: 8px 20px;
  border: 1px solid var(--mall-border-color, #E8E4DF);
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 13px;
  color: var(--mall-text-primary, #303133);
  transition: all 0.2s;
}

.sku-btn:hover:not(.disabled) {
  border-color: var(--mall-primary, #4A6B7C);
  color: var(--mall-primary, #4A6B7C);
}

.sku-btn.active {
  border-color: var(--mall-primary, #4A6B7C);
  background: var(--mall-primary, #4A6B7C);
  color: white;
}

.sku-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f5f5;
}

.out-of-stock {
  font-size: 11px;
  color: #c0c4cc;
  margin-left: 4px;
}

/* === 數量 === */
.quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-info {
  font-size: 13px;
  color: var(--mall-text-muted, #909399);
}

/* === 操作按鈕 === */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.btn-cart,
.btn-buy {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  letter-spacing: 1px;
}

.btn-cart {
  background: var(--mall-primary, #4A6B7C);
  color: white;
}

.btn-cart:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-buy {
  background: var(--mall-accent, #A5635C);
  color: white;
}

.btn-buy:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* === 商品詳情 Tab === */
.product-description {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  padding: 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: var(--mall-text-secondary, #606266);
}

.detail-tabs :deep(.el-tabs__item.is-active) {
  color: var(--mall-primary, #4A6B7C);
}

.detail-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--mall-primary, #4A6B7C);
}

.description-content {
  line-height: 1.8;
  color: var(--mall-text-secondary, #606266);
  font-size: 14px;
}

.description-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

/* === 推薦商品 === */
.recommend-section {
  margin-top: 8px;
}

.recommend-header {
  margin-bottom: 20px;
}

.recommend-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--mall-text-primary, #303133);
  margin: 0;
  position: relative;
  padding-left: 16px;
}

.recommend-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background: var(--mall-accent, #A5635C);
  border-radius: 2px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

/* === RWD === */
@media (max-width: 900px) {
  .product-main {
    grid-template-columns: 1fr;
    padding: 20px;
    gap: 24px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

@media (max-width: 500px) {
  .product-title {
    font-size: 20px;
  }

  .current-price {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
