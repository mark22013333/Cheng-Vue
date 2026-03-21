<template>
  <div class="product-detail-page" v-loading="loading">
    <!-- 麵包屑導覽列 -->
    <nav class="breadcrumb-section" v-if="product">
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
    </nav>

    <div class="product-main" v-if="product">
      <!-- 商品圖片區 -->
      <div class="product-gallery">
        <!-- 手機版：輪播切換 -->
        <div class="gallery-carousel">
          <el-carousel
            :autoplay="false"
            :initial-index="currentImageIndex"
            indicator-position="outside"
            height=""
            @change="onCarouselChange"
            ref="carouselRef"
          >
            <el-carousel-item v-for="(img, index) in allImages" :key="index">
              <div class="carousel-image-wrapper">
                <el-image
                  :src="getImageUrl(img)"
                  fit="contain"
                  :preview-src-list="previewImages"
                  :initial-index="index"
                  preview-teleported
                  hide-on-click-modal
                />
              </div>
            </el-carousel-item>
          </el-carousel>
          <div class="image-counter">{{ currentImageIndex + 1 }} / {{ allImages.length }}</div>
        </div>

        <!-- 桌面版：主圖 + 縮圖列 -->
        <div class="gallery-desktop">
          <div ref="mainImageRef" class="main-image">
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
              <img :src="getImageUrl(img)" :alt="`商品圖 ${index + 1}`" />
            </div>
          </div>
        </div>
      </div>

      <!-- 商品資訊區 -->
      <div class="product-info">
        <h1 class="product-title">{{ product.title }}</h1>
        <p class="product-subtitle" v-if="product.subTitle">{{ product.subTitle }}</p>

        <div class="price-section">
          <div class="price-row">
            <span class="price-symbol">NT$</span>
            <span class="current-price">{{ formatPriceNumber(currentPrice) }}</span>
            <span
              v-if="currentOriginalPrice && Number(currentOriginalPrice) > Number(currentPrice)"
              class="original-price"
            >
              {{ fmtCurrency(currentOriginalPrice) }}
            </span>
          </div>
          <span v-if="currentDiscountLabel" class="discount-tag">{{ currentDiscountLabel }}</span>
        </div>

        <div v-if="showSaleCountdown" class="sale-countdown">
          <el-icon><Timer /></el-icon>
          <span>限時特惠</span>
          <div class="countdown-blocks">
            <span class="countdown-num" v-for="(part, i) in countdownParts" :key="i">{{ part }}</span>
          </div>
        </div>

        <div class="meta-section">
          <div class="meta-item">
            <span class="meta-value">{{ product.salesCount || 0 }}</span>
            <span class="meta-label">已售</span>
          </div>
          <div class="meta-divider"></div>
          <div class="meta-item">
            <span class="meta-value">{{ product.viewCount || 0 }}</span>
            <span class="meta-label">瀏覽</span>
          </div>
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

        <!-- 桌面版操作按鈕 -->
        <div class="action-buttons desktop-actions">
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
      <div class="tab-header">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'detail' }"
          @click="activeTab = 'detail'"
        >
          商品詳情
        </button>
      </div>
      <div class="tab-content" v-show="activeTab === 'detail'">
        <div class="description-content" v-html="product.description"></div>
        <el-empty v-if="!product.description" description="暫無商品詳情" />
      </div>
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
      <!-- 手機版水平捲動推薦 -->
      <div class="recommend-scroll">
        <ProductCard
          v-for="item in recommendProducts"
          :key="'m-' + item.productId"
          :product="item"
          class="recommend-scroll-card"
          @click="goProductDetail(item.productId)"
        />
      </div>
    </div>

    <el-empty v-if="!loading && !product" description="商品不存在或已下架" />

    <!-- 手機版底部固定購買列 -->
    <div class="mobile-bottom-bar" v-if="product">
      <button class="mobile-btn-cart" @click="handleAddToCart">
        <el-icon><ShoppingCart /></el-icon>
        <span>購物車</span>
      </button>
      <button class="mobile-btn-buy" @click="handleBuyNow">
        立即購買
      </button>
    </div>
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
import { formatCurrency } from '@/utils/cheng'
import ProductCard from '@/views/shop-front/components/ProductCard.vue'
import { useFlyToCart } from '@/composables/useFlyToCart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const { flyToCart } = useFlyToCart()
const mainImageRef = ref(null)
const carouselRef = ref(null)

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

function fmtCurrency(value) {
  return formatCurrency(value)
}

function formatPriceNumber(value) {
  const num = Number(value || 0)
  return num.toLocaleString('zh-TW')
}

const currentPrice = computed(() => {
  if (!product.value) return 0

  // 特惠到期：回歸基礎價格
  if (saleEndTs.value && saleRemainingMs.value <= 0) {
    return Number(selectedSku.value?.price || product.value.price || 0)
  }

  // 有效特惠價優先
  if (hasActiveSalePrice.value) {
    return Number(effectiveSalePrice.value || 0)
  }

  // 選中 SKU 時，優先使用後端計算的 finalPrice（含全站折扣）
  if (selectedSku.value) {
    return Number(selectedSku.value.finalPrice || selectedSku.value.price || 0)
  }

  // 無 SKU 時，使用商品層級的 finalPrice
  const finalPrice = product.value.finalPrice
  if (finalPrice != null) {
    return Number(finalPrice || 0)
  }

  return Number(product.value.price || 0)
})

/**
 * 目前的劃線原價
 */
const currentOriginalPrice = computed(() => {
  if (selectedSku.value?.originalDisplayPrice) {
    return Number(selectedSku.value.originalDisplayPrice)
  }
  if (selectedSku.value?.originalPrice) {
    return Number(selectedSku.value.originalPrice)
  }
  if (product.value?.originalDisplayPrice) {
    return Number(product.value.originalDisplayPrice)
  }
  if (product.value?.originalPrice) {
    return Number(product.value.originalPrice)
  }
  return 0
})

/**
 * 目前的折扣標籤
 */
const currentDiscountLabel = computed(() => {
  if (selectedSku.value?.discountLabel) {
    return selectedSku.value.discountLabel
  }
  return product.value?.discountLabel || null
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

const countdownParts = computed(() => {
  const ms = saleRemainingMs.value
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  const parts = []
  if (days > 0) parts.push(`${days}天`)
  parts.push(String(hours).padStart(2, '0'))
  parts.push(String(minutes).padStart(2, '0'))
  parts.push(String(seconds).padStart(2, '0'))
  return parts
})

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

function onCarouselChange(index) {
  if (allImages.value[index]) {
    currentImage.value = allImages.value[index]
  }
}

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
    // 觸發飛入購物車動畫
    if (mainImageRef.value) {
      flyToCart(getImageUrl(product.value.mainImage), mainImageRef.value)
    }
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
  padding-bottom: 24px;
}

/* === 麵包屑 === */
.breadcrumb-section {
  padding: 8px 0;
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
  border-radius: 16px;
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

/* 手機版輪播 — 桌面隱藏 */
.gallery-carousel {
  display: none;
  position: relative;
}

.gallery-carousel :deep(.el-carousel) {
  border-radius: 12px;
  overflow: hidden;
}

.gallery-carousel :deep(.el-carousel__container) {
  height: 0 !important;
  padding-bottom: 100%;
}

.gallery-carousel :deep(.el-carousel__item) {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.carousel-image-wrapper {
  width: 100%;
  height: 100%;
  background: #f8f6f3;
}

.carousel-image-wrapper :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.image-counter {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  z-index: 2;
  pointer-events: none;
}

.gallery-carousel :deep(.el-carousel__indicators--outside) {
  display: none;
}

/* 桌面版圖片 */
.gallery-desktop {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.main-image {
  aspect-ratio: 1;
  border-radius: 12px;
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
  scrollbar-width: none;
}

.thumbnail-list::-webkit-scrollbar {
  display: none;
}

.thumbnail {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;
  transition: all 0.2s;
}

.thumbnail:hover {
  border-color: var(--mall-border-color, #E8E4DF);
}

.thumbnail.active {
  border-color: var(--mall-primary, #4A6B7C);
  box-shadow: 0 0 0 1px var(--mall-primary, #4A6B7C);
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
  gap: 20px;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--mall-text-primary, #303133);
  margin: 0;
  line-height: 1.4;
  letter-spacing: 0.3px;
}

.product-subtitle {
  font-size: 14px;
  color: var(--mall-text-muted, #909399);
  margin: -8px 0 0 0;
  line-height: 1.6;
}

/* === 價格 === */
.price-section {
  background: linear-gradient(135deg, rgba(165, 99, 92, 0.06), rgba(165, 99, 92, 0.02));
  padding: 20px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  font-size: 16px;
  font-weight: 700;
  color: var(--mall-accent, #A5635C);
}

.current-price {
  font-size: 32px;
  font-weight: 800;
  color: var(--mall-accent, #A5635C);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
  line-height: 1;
}

.original-price {
  font-size: 14px;
  color: #c0c4cc;
  text-decoration: line-through;
  margin-left: 8px;
}

.discount-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  font-size: 12px;
  color: white;
  background: var(--mall-accent, #A5635C);
  border-radius: 6px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.sale-countdown {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--mall-accent, #A5635C);
  padding: 10px 16px;
  background: rgba(165, 99, 92, 0.06);
  border-radius: 8px;
  border: 1px solid rgba(165, 99, 92, 0.12);
}

.countdown-blocks {
  display: flex;
  gap: 4px;
  margin-left: auto;
}

.countdown-num {
  background: var(--mall-accent, #A5635C);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  min-width: 28px;
  text-align: center;
}

/* === 統計 === */
.meta-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.meta-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--mall-text-primary, #303133);
}

.meta-label {
  font-size: 13px;
  color: var(--mall-text-muted, #909399);
}

.meta-divider {
  width: 1px;
  height: 16px;
  background: var(--mall-border-color, #E8E4DF);
}

/* === SKU === */
.section-label {
  font-size: 14px;
  color: var(--mall-text-secondary, #606266);
  margin-bottom: 12px;
  font-weight: 600;
}

.sku-section {
  padding-top: 4px;
}

.sku-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sku-btn {
  padding: 10px 24px;
  border: 1.5px solid var(--mall-border-color, #E8E4DF);
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  color: var(--mall-text-primary, #303133);
  transition: all 0.2s;
  min-height: 44px;
}

.sku-btn:hover:not(.disabled) {
  border-color: var(--mall-primary, #4A6B7C);
  color: var(--mall-primary, #4A6B7C);
  background: rgba(74, 107, 124, 0.04);
}

.sku-btn.active {
  border-color: var(--mall-primary, #4A6B7C);
  background: var(--mall-primary, #4A6B7C);
  color: white;
}

.sku-btn.disabled {
  opacity: 0.4;
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
  gap: 16px;
}

.stock-info {
  font-size: 13px;
  color: var(--mall-text-muted, #909399);
}

/* === 操作按鈕（桌面） === */
.desktop-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.btn-cart,
.btn-buy {
  flex: 1;
  height: 52px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.25s ease;
  letter-spacing: 1px;
}

.btn-cart {
  background: var(--mall-primary, #4A6B7C);
  color: white;
}

.btn-cart:hover {
  background: color-mix(in srgb, var(--mall-primary, #4A6B7C) 85%, black);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 107, 124, 0.3);
}

.btn-buy {
  background: var(--mall-accent, #A5635C);
  color: white;
}

.btn-buy:hover {
  background: color-mix(in srgb, var(--mall-accent, #A5635C) 85%, black);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(165, 99, 92, 0.3);
}

/* === 商品詳情 Tab === */
.product-description {
  background: var(--mall-card-bg, white);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.tab-header {
  display: flex;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 0 28px;
}

.tab-btn {
  padding: 16px 4px;
  font-size: 15px;
  font-weight: 600;
  color: var(--mall-text-secondary, #606266);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  color: var(--mall-primary, #4A6B7C);
  border-bottom-color: var(--mall-primary, #4A6B7C);
}

.tab-content {
  padding: 28px;
}

.description-content {
  line-height: 1.8;
  color: var(--mall-text-secondary, #606266);
  font-size: 14px;
  word-break: break-word;
  overflow-wrap: break-word;
}

.description-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  display: block;
  margin: 12px auto;
}

.description-content :deep(table) {
  max-width: 100%;
  overflow-x: auto;
  display: block;
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
  font-weight: 700;
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

/* 手機版水平捲動推薦 — 桌面隱藏 */
.recommend-scroll {
  display: none;
}

/* === 手機版底部固定購買列 — 桌面隱藏 === */
.mobile-bottom-bar {
  display: none;
}

/* =================================================================
   RWD 響應式設計
   ================================================================= */

/* --- 平板 (≤ 1024px) --- */
@media (max-width: 1024px) {
  .product-main {
    gap: 32px;
    padding: 24px;
  }

  .recommend-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}

/* --- 手機版 (≤ 768px) --- */
@media (max-width: 768px) {
  .product-detail-page {
    gap: 16px;
    padding-bottom: 80px; /* 為底部購買列留空間 */
  }

  /* 麵包屑：單行截斷 */
  .breadcrumb-section {
    padding: 4px 0;
    overflow: hidden;
  }

  .breadcrumb-section :deep(.el-breadcrumb) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    display: block;
  }

  .breadcrumb-section :deep(.el-breadcrumb__item) {
    font-size: 12px;
    display: inline;
  }

  /* 主區域：單欄全寬 */
  .product-main {
    grid-template-columns: 1fr;
    padding: 0;
    gap: 0;
    border-radius: 16px;
    overflow: hidden;
  }

  /* 圖片：切換為輪播 */
  .gallery-desktop {
    display: none;
  }

  .gallery-carousel {
    display: block;
  }

  .product-gallery {
    gap: 0;
  }

  /* 商品資訊 */
  .product-info {
    padding: 20px 16px;
    gap: 16px;
  }

  .product-title {
    font-size: 18px;
    line-height: 1.5;
  }

  .product-subtitle {
    margin-top: -4px;
    font-size: 13px;
  }

  /* 價格區 */
  .price-section {
    padding: 14px 16px;
    border-radius: 10px;
  }

  .current-price {
    font-size: 28px;
  }

  /* 倒數計時 */
  .sale-countdown {
    flex-wrap: wrap;
    padding: 8px 12px;
    font-size: 12px;
  }

  .countdown-blocks {
    margin-left: 0;
    flex-basis: 100%;
    margin-top: 6px;
  }

  /* 統計 */
  .meta-section {
    gap: 16px;
  }

  .meta-value {
    font-size: 14px;
  }

  /* SKU */
  .sku-grid {
    gap: 8px;
  }

  .sku-btn {
    padding: 10px 18px;
    font-size: 13px;
    min-height: 44px;
  }

  /* 桌面版操作按鈕：隱藏 */
  .desktop-actions {
    display: none;
  }

  /* 手機底部固定購買列：顯示 */
  .mobile-bottom-bar {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 100;
    background: var(--mall-card-bg, white);
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
    padding: 8px 12px;
    padding-bottom: max(8px, env(safe-area-inset-bottom));
    gap: 10px;
  }

  .mobile-btn-cart {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    width: 64px;
    flex-shrink: 0;
    background: none;
    border: 1.5px solid var(--mall-primary, #4A6B7C);
    border-radius: 10px;
    color: var(--mall-primary, #4A6B7C);
    font-size: 11px;
    font-weight: 600;
    cursor: pointer;
    padding: 6px;
  }

  .mobile-btn-cart :deep(.el-icon) {
    font-size: 20px;
  }

  .mobile-btn-buy {
    flex: 1;
    height: 48px;
    border: none;
    border-radius: 10px;
    background: var(--mall-accent, #A5635C);
    color: white;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    letter-spacing: 2px;
    transition: background 0.2s;
  }

  .mobile-btn-buy:active {
    background: color-mix(in srgb, var(--mall-accent, #A5635C) 80%, black);
  }

  /* 商品詳情 */
  .product-description {
    border-radius: 12px;
  }

  .tab-header {
    padding: 0 16px;
  }

  .tab-btn {
    padding: 14px 4px;
    font-size: 14px;
  }

  .tab-content {
    padding: 16px;
  }

  .description-content {
    font-size: 14px;
    line-height: 1.8;
  }

  /* 推薦商品：水平捲動 */
  .recommend-grid {
    display: none;
  }

  .recommend-scroll {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    scroll-snap-type: x mandatory;
    -webkit-overflow-scrolling: touch;
    padding-bottom: 4px;
    scrollbar-width: none;
  }

  .recommend-scroll::-webkit-scrollbar {
    display: none;
  }

  .recommend-scroll-card {
    flex: 0 0 160px;
    scroll-snap-align: start;
  }
}

/* --- 超小螢幕 (≤ 375px) --- */
@media (max-width: 375px) {
  .product-title {
    font-size: 16px;
  }

  .current-price {
    font-size: 24px;
  }

  .price-symbol {
    font-size: 14px;
  }

  .sku-btn {
    padding: 8px 14px;
    font-size: 12px;
  }

  .recommend-scroll-card {
    flex: 0 0 140px;
  }
}
</style>
