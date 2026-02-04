<template>
  <div class="product-detail-page" v-loading="loading">
    <!-- 麵包屑導覽列 -->
    <div class="breadcrumb-section" v-if="product">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/mall' }">首頁</el-breadcrumb-item>
        <el-breadcrumb-item
          v-for="category in categoryPath"
          :key="category.categoryId"
          :to="{ path: '/mall/products', query: { categoryId: category.categoryId } }"
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
          <el-image :src="getImageUrl(currentImage)" fit="contain" :preview-src-list="allImages" preview-teleported />
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
          <span v-if="showDiscountTag" class="discount-tag">{{ product.discountLabel }}</span>
        </div>

        <div v-if="showSaleCountdown" class="sale-countdown">特惠價倒數：{{ saleCountdownText }}</div>

        <div class="meta-section">
          <span>銷量：{{ product.salesCount || 0 }}</span>
          <span>瀏覽：{{ product.viewCount || 0 }}</span>
        </div>

        <!-- SKU 選擇 -->
        <div class="sku-section" v-if="skuList.length > 0">
          <div class="section-label">規格選擇</div>
          <el-radio-group v-model="selectedSkuId" class="sku-list">
            <el-radio-button 
              v-for="sku in skuList" 
              :key="sku.skuId" 
              :value="sku.skuId"
              :disabled="sku.stockQuantity <= 0"
            >
              {{ sku.skuName }}
              <span v-if="sku.stockQuantity <= 0" class="out-of-stock">（缺貨）</span>
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 數量選擇 -->
        <div class="quantity-section">
          <div class="section-label">購買數量</div>
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

        <!-- 操作按鈕 -->
        <div class="action-buttons">
          <el-button type="warning" size="large" icon="ShoppingCart" @click="handleAddToCart">
            加入購物車
          </el-button>
          <el-button type="primary" size="large" @click="handleBuyNow">
            立即購買
          </el-button>
        </div>
      </div>
    </div>

    <!-- 商品詳情 -->
    <div class="product-description" v-if="product">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品詳情" name="detail">
          <div class="description-content" v-html="product.description"></div>
          <el-empty v-if="!product.description" description="暫無商品詳情" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-empty v-if="!loading && !product" description="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct, getProductSkus, listCategories } from '@/api/shop/front'
import { useCartStore } from '@/store/modules/cart'
import { getMemberToken } from '@/utils/memberAuth'

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

// 計算分類路徑（從根到當前分類）
const categoryPath = computed(() => {
  if (!product.value?.categoryId || categories.value.length === 0) {
    return []
  }

  const path = []
  let currentCatId = product.value.categoryId

  // 從當前分類向上追溯到根分類
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

  // 若商品特惠價已過期，但後端回傳的 finalPrice 仍可能是舊資料，先強制回到商品價格
  if (saleEndTs.value && saleRemainingMs.value <= 0) {
    return Number(selectedSku.value?.price || product.value.price || 0)
  }

  // 優先顯示特惠價（有效期間內）
  if (hasActiveSalePrice.value) {
    return Number(effectiveSalePrice.value || 0)
  }

  // 有選擇 SKU 時，價格以 SKU 為準
  if (selectedSku.value?.price != null) {
    return Number(selectedSku.value.price || 0)
  }

  // 其次使用後端計算後的最終售價（例如全站折扣）
  const finalPrice = product.value.finalPrice
  if (finalPrice != null) {
    return Number(finalPrice || 0)
  }

  // 最後才退回商品價格
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

function getImageUrl(url) {
  if (!url) return 'https://via.placeholder.com/600x600?text=No+Image'
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

async function handleAddToCart() {
  if (!selectedSkuId.value && skuList.value.length > 0) {
    ElMessage.warning('請選擇商品規格')
    return
  }

  // 如果沒有 SKU 但商品存在，使用第一個 SKU 或拒絕
  const sku = selectedSku.value || skuList.value[0]
  if (!sku) {
    ElMessage.warning('此商品暫無可購買規格')
    return
  }

  // 組裝 skuInfo（用於訪客購物車）
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

  // 組裝 skuInfo（用於訪客購物車）
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
      router.push('/mall/login?redirect=/mall/checkout')
      return
    }
    // 先加入購物車並選中
    await cartStore.addToCart(sku.skuId, quantity.value, skuInfo)
    // 跳轉到結帳頁面
    router.push('/mall/checkout')
  } catch (error) {
    ElMessage.error(error.message || '操作失敗')
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

async function loadProduct() {
  const productId = route.params.id
  if (!productId) return

  loading.value = true
  try {
    const res = await getProduct(productId)
    product.value = res.data
    currentImage.value = res.data?.mainImage || ''
    saleExpiredReloaded = false

    // 載入 SKU
    const skuRes = await getProductSkus(productId)
    skuList.value = skuRes.data || []
    if (skuList.value.length > 0) {
      const availableSku = skuList.value.find(s => s.stockQuantity > 0)
      if (availableSku) {
        selectedSkuId.value = availableSku.skuId
      }
    }
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
  loadCategories()
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

.breadcrumb-section {
  background: var(--mall-card-bg, white);
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.breadcrumb-section :deep(.el-breadcrumb__item) {
  font-size: 14px;
}

.breadcrumb-section :deep(.el-breadcrumb__inner.is-link) {
  color: #606266;
  font-weight: normal;
}

.breadcrumb-section :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--mall-primary, #409eff);
}

.product-main {
  background: var(--mall-card-bg, white);
  border-radius: 16px;
  padding: 32px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.product-gallery {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.main-image {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
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
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;
}

.thumbnail.active {
  border-color: var(--mall-primary, #667eea);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--mall-text-primary, #303133);
  margin: 0;
}

.product-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.price-section {
  background: #fff7e6;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--mall-accent, #f56c6c);
}

.original-price {
  font-size: 16px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.discount-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 12px;
  color: white;
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  border-radius: 4px;
  font-weight: 500;
}

.sale-countdown {
  font-size: 13px;
  color: #e6a23c;
  margin-top: 8px;
}

.meta-section {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #909399;
}

.section-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.sku-section {
  padding-top: 8px;
}

.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.out-of-stock {
  color: #c0c4cc;
  font-size: 12px;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stock-info {
  font-size: 14px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.action-buttons .el-button {
  flex: 1;
  height: 48px;
  font-size: 16px;
}

.product-description {
  background: var(--mall-card-bg, white);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.description-content {
  line-height: 1.8;
  color: #606266;
}

.description-content :deep(img) {
  max-width: 100%;
  height: auto;
}

@media (max-width: 900px) {
  .product-main {
    grid-template-columns: 1fr;
  }
}
</style>
