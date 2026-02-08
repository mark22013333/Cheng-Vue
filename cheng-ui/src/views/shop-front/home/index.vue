<template>
  <div class="shop-home">
    <!-- Hero Banner Section -->
    <section class="hero-section">
      <div class="hero-carousel" v-if="banners.length">
        <el-carousel
          :interval="6000"
          height="520px"
          arrow="hover"
          indicator-position="outside"
          :pause-on-hover="true"
        >
          <el-carousel-item v-for="(banner, index) in banners" :key="banner.bannerId">
            <div
              class="hero-slide"
              :style="{ '--delay': index * 0.1 + 's' }"
              @click="handleBannerClick(banner)"
            >
              <div class="hero-image-wrapper">
                <img :src="getImageUrl(banner.imageUrl)" :alt="banner.title" class="hero-image" />
                <div class="hero-overlay"></div>
              </div>
              <div class="hero-content" v-if="banner.title">
                <span class="hero-eyebrow">精選推薦</span>
                <h1 class="hero-title">{{ banner.title }}</h1>
                <p class="hero-subtitle" v-if="banner.subTitle">{{ banner.subTitle }}</p>
                <button class="hero-cta">
                  立即選購
                  <span class="cta-arrow">→</span>
                </button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <div v-else class="hero-placeholder">
        <div class="placeholder-content">
          <span class="placeholder-icon">✦</span>
          <h2>探索精選商品</h2>
          <p>發現屬於你的完美選擇</p>
        </div>
      </div>
    </section>

    <!-- Features Strip -->
    <section class="features-strip">
      <div class="feature-item" v-for="(feature, i) in features" :key="i" :style="{ '--i': i }">
        <span class="feature-icon">{{ feature.icon }}</span>
        <div class="feature-text">
          <strong>{{ feature.title }}</strong>
          <span>{{ feature.desc }}</span>
        </div>
      </div>
    </section>

    <!-- Hot Products - Editorial Grid -->
    <section class="section hot-section">
      <div class="section-header">
        <div class="section-title-group">
          <span class="section-tag">HOT</span>
          <h2 class="section-title">熱門精選</h2>
        </div>
        <p class="section-desc">當季最受歡迎的人氣商品</p>
        <router-link to="/products?isHot=true" class="section-link">
          探索更多 <span class="link-arrow">→</span>
        </router-link>
      </div>

      <div class="editorial-grid" v-loading="loading.hot">
        <div
          v-for="(product, index) in hotProducts.slice(0, 5)"
          :key="product.productId"
          class="editorial-item"
          :class="getEditorialClass(index)"
          :style="{ '--delay': index * 0.08 + 's' }"
          @click="goProductDetail(product.productId)"
        >
          <div class="editorial-image">
            <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
            <div class="editorial-overlay">
              <span class="quick-view">快速瀏覽</span>
            </div>
            <div class="editorial-badge" v-if="product.isHot">
              <span>熱賣</span>
            </div>
          </div>
          <div class="editorial-info">
            <h3 class="editorial-title">{{ product.title }}</h3>
            <div class="editorial-price">
              <span class="price-current">NT$ {{ formatPrice(product.price) }}</span>
              <span class="price-original" v-if="product.originalPrice && product.originalPrice > product.price">
                NT$ {{ formatPrice(product.originalPrice) }}
              </span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading.hot && hotProducts.length === 0" description="暫無熱門商品" />
      </div>
    </section>

    <!-- New Products - Magazine Layout -->
    <section class="section new-section">
      <div class="new-section-wrapper">
        <div class="new-section-sidebar">
          <span class="vertical-tag">NEW ARRIVALS</span>
          <h2 class="sidebar-title">新品<br/>上市</h2>
          <p class="sidebar-desc">搶先體驗最新商品，掌握潮流脈動</p>
          <router-link to="/products?isNew=true" class="sidebar-link">
            查看全部新品
            <span class="link-icon">→</span>
          </router-link>
        </div>

        <div class="new-products-grid" v-loading="loading.new">
          <div
            v-for="(product, index) in newProducts.slice(0, 6)"
            :key="product.productId"
            class="new-product-card"
            :style="{ '--delay': index * 0.06 + 's' }"
            @click="goProductDetail(product.productId)"
          >
            <div class="new-card-image">
              <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
              <div class="new-tag">NEW</div>
              <div class="card-actions">
                <button class="action-btn" @click.stop="addToCart(product)">
                  <span>加入購物車</span>
                </button>
              </div>
            </div>
            <div class="new-card-info">
              <h4 class="new-card-title">{{ product.title }}</h4>
              <p class="new-card-subtitle" v-if="product.subTitle">{{ product.subTitle }}</p>
              <div class="new-card-footer">
                <span class="new-price">NT$ {{ formatPrice(product.price) }}</span>
                <span class="new-sales">已售 {{ product.salesCount || 0 }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!loading.new && newProducts.length === 0" description="暫無新品" class="empty-state" />
        </div>
      </div>
    </section>

    <!-- Recommend Products - Showcase -->
    <section class="section recommend-section">
      <div class="recommend-header">
        <div class="recommend-title-area">
          <span class="diamond-icon">◆</span>
          <h2 class="recommend-title">精選推薦</h2>
          <span class="diamond-icon">◆</span>
        </div>
        <p class="recommend-subtitle">為您挑選的優質好物</p>
      </div>

      <div class="recommend-carousel" v-loading="loading.recommend">
        <div class="recommend-track">
          <div
            v-for="(product, index) in recommendProducts"
            :key="product.productId"
            class="recommend-card"
            :style="{ '--delay': index * 0.05 + 's' }"
            @click="goProductDetail(product.productId)"
          >
            <div class="recommend-card-inner">
              <div class="recommend-image">
                <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
                <div class="recommend-badge" v-if="product.isRecommend">
                  <span>推薦</span>
                </div>
              </div>
              <div class="recommend-info">
                <h4 class="recommend-name">{{ product.title }}</h4>
                <div class="recommend-meta">
                  <span class="recommend-price">NT$ {{ formatPrice(product.price) }}</span>
                  <span class="recommend-divider">|</span>
                  <span class="recommend-sales">銷量 {{ product.salesCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading.recommend && recommendProducts.length === 0" description="暫無推薦商品" />
      </div>

      <div class="recommend-footer">
        <router-link to="/products?isRecommend=true" class="explore-all-btn">
          探索所有推薦商品
          <span class="btn-arrow">→</span>
        </router-link>
      </div>
    </section>

    <!-- Articles Section -->
    <section class="section article-section" v-if="latestArticles.length > 0">
      <div class="section-header">
        <div class="section-title-group">
          <span class="section-tag" style="background: linear-gradient(135deg, #38b2ac 0%, #4299e1 100%);">BLOG</span>
          <h2 class="section-title">文章精選</h2>
        </div>
        <p class="section-desc">探索最新的商品資訊與生活提案</p>
        <router-link to="/articles" class="section-link">
          查看全部 <span class="link-arrow">→</span>
        </router-link>
      </div>

      <div class="article-grid" v-loading="loading.article">
        <div
          v-for="(article, index) in latestArticles"
          :key="article.articleId"
          class="article-card"
          :style="{ '--delay': index * 0.08 + 's' }"
          @click="goArticleDetail(article.articleId)"
        >
          <div class="article-cover">
            <img v-if="article.coverImage" :src="getImageUrl(article.coverImage)" :alt="article.title" />
            <div class="article-cover-placeholder" v-else>
              <span>文章</span>
            </div>
          </div>
          <div class="article-info">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-summary" v-if="article.summary">{{ article.summary }}</p>
            <div class="article-meta">
              <span>{{ formatArticleDate(article.createTime) }}</span>
              <span>{{ article.viewCount || 0 }} 次瀏覽</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Bottom CTA -->
    <section class="cta-section">
      <div class="cta-content">
        <h3 class="cta-title">加入會員享專屬優惠</h3>
        <p class="cta-desc">立即註冊，搶先獲得新品資訊與限定折扣</p>
        <router-link to="/register" class="cta-button">
          立即加入
        </router-link>
      </div>
      <div class="cta-decoration">
        <span class="deco-circle"></span>
        <span class="deco-circle"></span>
        <span class="deco-circle"></span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listFrontBanners, listHotProducts, listNewProducts, listRecommendProducts, listLatestArticles, getProductSkus } from '@/api/shop/front'
import { addToCart as addToCartApi } from '@/api/shop/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()

const banners = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const recommendProducts = ref([])
const latestArticles = ref([])

const loading = ref({
  banner: false,
  hot: false,
  new: false,
  recommend: false,
  article: false
})

const features = [
  { icon: '🚚', title: '快速配送', desc: '全台宅配到府' },
  { icon: '🔒', title: '安全付款', desc: '多元支付保障' },
  { icon: '↩️', title: '輕鬆退換', desc: '7天鑑賞期' },
  { icon: '💬', title: '專業客服', desc: '即時線上支援' }
]

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  // 圖片路徑由 Nginx 處理，不需要加 API 前綴
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}

function formatPrice(price) {
  return Number(price || 0).toLocaleString('zh-TW')
}

function getEditorialClass(index) {
  const classes = ['featured', 'normal', 'normal', 'wide', 'normal']
  return classes[index] || 'normal'
}

function handleBannerClick(banner) {
  if (banner.linkValue) {
    if (banner.linkType === 'PRODUCT') {
      router.push(`/product/${banner.linkValue}`)
    } else if (banner.linkType === 'CATEGORY') {
      router.push(`/products?categoryId=${banner.linkValue}`)
    } else if (banner.linkType === 'URL') {
      window.open(banner.linkValue, '_blank')
    }
  }
}

function goProductDetail(productId) {
  router.push(`/product/${productId}`)
}

function goArticleDetail(articleId) {
  router.push(`/article/${articleId}`)
}

function formatArticleDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

async function addToCart(product) {
  try {
    const res = await getProductSkus(product.productId)
    const skus = res.data || []
    if (skus.length === 0) {
      ElMessage.warning('此商品暫無可購買的規格')
      return
    }
    if (skus.length === 1) {
      await addToCartApi({ skuId: skus[0].skuId, quantity: 1 })
      ElMessage.success(`已將「${product.title}」加入購物車`)
    } else {
      ElMessage.info('此商品有多種規格，請至商品頁面選擇')
      router.push(`/product/${product.productId}`)
    }
  } catch (error) {
    if (error?.response?.status === 401) {
      ElMessage.warning('請先登入後再加入購物車')
      router.push('/login')
    } else {
      ElMessage.error(error?.message || '加入購物車失敗')
    }
  }
}

async function loadBanners() {
  loading.value.banner = true
  try {
    const res = await listFrontBanners('HOME_TOP')
    banners.value = res.data || []
  } catch (error) {
    console.error('載入輪播圖失敗', error)
  } finally {
    loading.value.banner = false
  }
}

async function loadHotProducts() {
  loading.value.hot = true
  try {
    const res = await listHotProducts(8)
    hotProducts.value = res.data || []
  } catch (error) {
    console.error('載入熱門商品失敗', error)
  } finally {
    loading.value.hot = false
  }
}

async function loadNewProducts() {
  loading.value.new = true
  try {
    const res = await listNewProducts(8)
    newProducts.value = res.data || []
  } catch (error) {
    console.error('載入新品失敗', error)
  } finally {
    loading.value.new = false
  }
}

async function loadRecommendProducts() {
  loading.value.recommend = true
  try {
    const res = await listRecommendProducts(8)
    recommendProducts.value = res.data || []
  } catch (error) {
    console.error('載入推薦商品失敗', error)
  } finally {
    loading.value.recommend = false
  }
}

async function loadLatestArticles() {
  loading.value.article = true
  try {
    const res = await listLatestArticles(6)
    latestArticles.value = res.data || []
  } catch (error) {
    console.error('載入文章失敗', error)
  } finally {
    loading.value.article = false
  }
}

onMounted(() => {
  loadBanners()
  loadHotProducts()
  loadNewProducts()
  loadRecommendProducts()
  loadLatestArticles()
})
</script>

<style scoped>
/* ===== Base & Variables ===== */
.shop-home {
  --home-accent: var(--mall-primary, #5a67d8);
  --home-accent-light: var(--mall-primary-light, #ebf4ff);
  --home-text: var(--mall-text-primary, #1a202c);
  --home-text-secondary: #718096;
  --home-card-bg: var(--mall-card-bg, #ffffff);
  --home-border: #e2e8f0;
  --home-price: #c53030;
  --home-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --home-shadow-lg: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --home-radius: 16px;
  --home-radius-sm: 8px;
  --transition-base: 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  display: flex;
  flex-direction: column;
  gap: 48px;
}

/* ===== Hero Section ===== */
.hero-section {
  border-radius: var(--home-radius);
  overflow: hidden;
  box-shadow: var(--home-shadow-lg);
}

.hero-carousel :deep(.el-carousel__container) {
  border-radius: var(--home-radius);
}

.hero-carousel :deep(.el-carousel__indicators--outside) {
  margin-top: 16px;
}

.hero-carousel :deep(.el-carousel__indicator) {
  padding: 12px 6px;
}

.hero-carousel :deep(.el-carousel__button) {
  width: 32px;
  height: 4px;
  border-radius: 2px;
  background: var(--home-border);
  opacity: 1;
}

.hero-carousel :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: var(--home-accent);
  width: 48px;
}

.hero-slide {
  position: relative;
  height: 100%;
  cursor: pointer;
  animation: slideIn 0.6s ease-out backwards;
  animation-delay: var(--delay);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(1.02);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.hero-image-wrapper {
  position: relative;
  height: 100%;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s ease;
}

.hero-slide:hover .hero-image {
  transform: scale(1.03);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(0, 0, 0, 0.5) 0%,
    rgba(0, 0, 0, 0.2) 50%,
    transparent 100%
  );
}

.hero-content {
  position: absolute;
  bottom: 60px;
  left: 60px;
  max-width: 500px;
  color: white;
}

.hero-eyebrow {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  line-height: 1.2;
  margin: 0 0 12px;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 18px;
  margin: 0 0 24px;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: white;
  color: var(--home-text);
  border: none;
  border-radius: 30px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.hero-cta:hover {
  background: var(--home-accent);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.cta-arrow {
  transition: transform var(--transition-base);
}

.hero-cta:hover .cta-arrow {
  transform: translateX(4px);
}

.hero-placeholder {
  height: 400px;
  background: var(--mall-header-bg, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--home-radius);
}

.placeholder-content {
  text-align: center;
  color: white;
}

.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.1); }
}

.placeholder-content h2 {
  font-size: 28px;
  margin: 0 0 8px;
}

.placeholder-content p {
  opacity: 0.8;
}

/* ===== Features Strip ===== */
.features-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 24px;
  background: var(--home-card-bg);
  border-radius: var(--home-radius);
  box-shadow: var(--home-shadow);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  border-radius: var(--home-radius-sm);
  transition: all var(--transition-base);
  animation: fadeUp 0.5s ease-out backwards;
  animation-delay: calc(var(--i) * 0.1s);
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-item:hover {
  background: var(--home-accent-light);
}

.feature-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-text strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--home-text);
}

.feature-text span {
  font-size: 12px;
  color: var(--home-text-secondary);
}

/* ===== Section Common ===== */
.section {
  padding: 40px;
  background: var(--home-card-bg);
  border-radius: var(--home-radius);
  box-shadow: var(--home-shadow);
}

.section-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 32px;
  position: relative;
}

.section-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.section-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  background: linear-gradient(135deg, #f56565 0%, #ed8936 100%);
  color: white;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  border-radius: 4px;
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--home-text);
  margin: 0;
}

.section-desc {
  font-size: 14px;
  color: var(--home-text-secondary);
  margin: 0 0 4px;
}

.section-link {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--home-accent);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all var(--transition-base);
}

.section-link:hover {
  background: var(--home-accent);
  color: white;
}

.link-arrow {
  transition: transform var(--transition-base);
}

.section-link:hover .link-arrow {
  transform: translateX(4px);
}

/* ===== Hot Products - Editorial Grid ===== */
.editorial-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: auto auto;
  gap: 20px;
  min-height: 400px;
}

.editorial-item {
  position: relative;
  border-radius: var(--home-radius-sm);
  overflow: hidden;
  cursor: pointer;
  animation: fadeUp 0.5s ease-out backwards;
  animation-delay: var(--delay);
}

.editorial-item.featured {
  grid-column: span 2;
  grid-row: span 2;
}

.editorial-item.wide {
  grid-column: span 2;
}

.editorial-image {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 220px;
  overflow: hidden;
}

.editorial-item.featured .editorial-image {
  min-height: 460px;
}

.editorial-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.editorial-item:hover .editorial-image img {
  transform: scale(1.08);
}

.editorial-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, transparent 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.editorial-item:hover .editorial-overlay {
  opacity: 1;
}

.quick-view {
  padding: 10px 24px;
  background: white;
  color: var(--home-text);
  font-size: 13px;
  font-weight: 600;
  border-radius: 20px;
  transform: translateY(20px);
  transition: transform var(--transition-base);
}

.editorial-item:hover .quick-view {
  transform: translateY(0);
}

.editorial-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #f56565 0%, #ed8936 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 4px;
}

.editorial-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
}

.editorial-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  line-height: 1.4;
}

.editorial-item.featured .editorial-title {
  font-size: 20px;
}

.editorial-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-current {
  font-size: 18px;
  font-weight: 700;
}

.editorial-item.featured .price-current {
  font-size: 22px;
}

.price-original {
  font-size: 13px;
  text-decoration: line-through;
  opacity: 0.7;
}

/* ===== New Products Section ===== */
.new-section {
  background: linear-gradient(135deg, var(--home-accent-light) 0%, var(--home-card-bg) 100%);
}

.new-section-wrapper {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 40px;
}

.new-section-sidebar {
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.vertical-tag {
  writing-mode: vertical-lr;
  text-orientation: mixed;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--home-accent);
  position: absolute;
  left: -20px;
  top: 0;
  opacity: 0.6;
}

.sidebar-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--home-text);
  line-height: 1.2;
  margin: 0 0 16px;
}

.sidebar-desc {
  font-size: 14px;
  color: var(--home-text-secondary);
  line-height: 1.8;
  margin: 0 0 24px;
}

.sidebar-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--home-accent);
  color: white;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  transition: all var(--transition-base);
  width: fit-content;
}

.sidebar-link:hover {
  background: var(--home-text);
  transform: translateX(4px);
}

.link-icon {
  transition: transform var(--transition-base);
}

.sidebar-link:hover .link-icon {
  transform: translateX(4px);
}

.new-products-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 300px;
}

.new-product-card {
  background: var(--home-card-bg);
  border-radius: var(--home-radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  animation: fadeUp 0.5s ease-out backwards;
  animation-delay: var(--delay);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.new-product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--home-shadow-lg);
}

.new-card-image {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.new-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.new-product-card:hover .new-card-image img {
  transform: scale(1.08);
}

.new-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  background: linear-gradient(135deg, #4299e1 0%, #667eea 100%);
  color: white;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  border-radius: 4px;
}

.card-actions {
  position: absolute;
  bottom: 12px;
  left: 12px;
  right: 12px;
  opacity: 0;
  transform: translateY(10px);
  transition: all var(--transition-base);
}

.new-product-card:hover .card-actions {
  opacity: 1;
  transform: translateY(0);
}

.action-btn {
  width: 100%;
  padding: 10px;
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--home-text);
  cursor: pointer;
  transition: all var(--transition-base);
}

.action-btn:hover {
  background: var(--home-accent);
  color: white;
}

.new-card-info {
  padding: 16px;
}

.new-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--home-text);
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.new-card-subtitle {
  font-size: 12px;
  color: var(--home-text-secondary);
  margin: 0 0 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.new-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.new-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--home-price);
}

.new-sales {
  font-size: 12px;
  color: var(--home-text-secondary);
}

/* ===== Recommend Section ===== */
.recommend-section {
  text-align: center;
}

.recommend-header {
  margin-bottom: 32px;
}

.recommend-title-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 8px;
}

.diamond-icon {
  color: var(--home-accent);
  font-size: 14px;
}

.recommend-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--home-text);
  margin: 0;
}

.recommend-subtitle {
  font-size: 14px;
  color: var(--home-text-secondary);
  margin: 0;
}

.recommend-carousel {
  overflow: hidden;
  padding: 10px 0;
}

.recommend-track {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  padding: 10px 0;
}

.recommend-track::-webkit-scrollbar {
  display: none;
}

.recommend-card {
  flex: 0 0 calc(25% - 15px);
  min-width: 220px;
  scroll-snap-align: start;
  animation: fadeUp 0.5s ease-out backwards;
  animation-delay: var(--delay);
}

.recommend-card-inner {
  background: var(--home-card-bg);
  border-radius: var(--home-radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid var(--home-border);
}

.recommend-card-inner:hover {
  transform: translateY(-6px);
  box-shadow: var(--home-shadow-lg);
  border-color: transparent;
}

.recommend-image {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.recommend-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.recommend-card-inner:hover .recommend-image img {
  transform: scale(1.05);
}

.recommend-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  background: linear-gradient(135deg, #9f7aea 0%, #667eea 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 4px;
}

.recommend-info {
  padding: 14px;
  text-align: left;
}

.recommend-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--home-text);
  margin: 0 0 8px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommend-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.recommend-price {
  font-weight: 700;
  color: var(--home-price);
}

.recommend-divider {
  color: var(--home-border);
}

.recommend-sales {
  color: var(--home-text-secondary);
}

.recommend-footer {
  margin-top: 32px;
}

.explore-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: transparent;
  color: var(--home-accent);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  border: 2px solid var(--home-accent);
  border-radius: 30px;
  transition: all var(--transition-base);
}

.explore-all-btn:hover {
  background: var(--home-accent);
  color: white;
}

.btn-arrow {
  transition: transform var(--transition-base);
}

.explore-all-btn:hover .btn-arrow {
  transform: translateX(4px);
}

/* ===== Article Section ===== */
.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 200px;
}

.article-card {
  border-radius: var(--home-radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  animation: fadeUp 0.5s ease-out backwards;
  animation-delay: var(--delay);
  border: 1px solid var(--home-border);
}

.article-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--home-shadow-lg);
  border-color: transparent;
}

.article-cover {
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.article-cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #38b2ac, #4299e1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.article-info {
  padding: 16px;
}

.article-info .article-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--home-text);
  margin: 0 0 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  font-size: 13px;
  color: var(--home-text-secondary);
  margin: 0 0 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #a0aec0;
}

/* ===== CTA Section ===== */
.cta-section {
  position: relative;
  padding: 60px 40px;
  background: linear-gradient(135deg, var(--home-accent) 0%, #764ba2 100%);
  border-radius: var(--home-radius);
  overflow: hidden;
  text-align: center;
}

.cta-content {
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 12px;
}

.cta-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 24px;
}

.cta-button {
  display: inline-block;
  padding: 14px 40px;
  background: white;
  color: var(--home-accent);
  text-decoration: none;
  font-size: 15px;
  font-weight: 600;
  border-radius: 30px;
  transition: all var(--transition-base);
}

.cta-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.cta-decoration {
  position: absolute;
  top: -50px;
  right: -50px;
  display: flex;
  gap: 20px;
}

.deco-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.deco-circle:nth-child(2) {
  width: 100px;
  height: 100px;
  margin-top: 80px;
}

.deco-circle:nth-child(3) {
  width: 80px;
  height: 80px;
  margin-top: 40px;
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .editorial-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .editorial-item.featured {
    grid-column: span 2;
  }

  .editorial-item.wide {
    grid-column: span 1;
  }

  .new-products-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .recommend-card {
    flex: 0 0 calc(33.333% - 14px);
  }

  .article-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .shop-home {
    gap: 32px;
  }

  .section {
    padding: 28px;
  }

  .hero-content {
    left: 40px;
    bottom: 40px;
  }

  .hero-title {
    font-size: 32px;
  }

  .features-strip {
    grid-template-columns: repeat(2, 1fr);
  }

  .editorial-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .editorial-item.featured {
    grid-column: span 2;
    grid-row: span 1;
  }

  .editorial-item.featured .editorial-image {
    min-height: 280px;
  }

  .new-section-wrapper {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .new-section-sidebar {
    text-align: center;
  }

  .vertical-tag {
    display: none;
  }

  .sidebar-link {
    margin: 0 auto;
  }

  .recommend-card {
    flex: 0 0 calc(50% - 10px);
  }

  .section-link {
    position: static;
    transform: none;
    margin-top: 12px;
  }
}

@media (max-width: 600px) {
  .shop-home {
    gap: 24px;
  }

  .section {
    padding: 20px;
  }

  .hero-carousel :deep(.el-carousel__container) {
    height: 320px !important;
  }

  .hero-content {
    left: 24px;
    bottom: 24px;
    right: 24px;
  }

  .hero-title {
    font-size: 24px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .hero-cta {
    padding: 12px 24px;
    font-size: 14px;
  }

  .features-strip {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    padding: 16px;
  }

  .feature-item {
    padding: 8px;
  }

  .feature-icon {
    font-size: 22px;
  }

  .section-title {
    font-size: 20px;
  }

  .editorial-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .editorial-item.featured,
  .editorial-item.wide {
    grid-column: span 1;
    grid-row: span 1;
  }

  .editorial-item.featured .editorial-image,
  .editorial-image {
    min-height: 200px;
  }

  .article-grid {
    grid-template-columns: 1fr;
  }

  .new-products-grid {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .sidebar-title {
    font-size: 28px;
  }

  .recommend-card {
    flex: 0 0 calc(100% - 40px);
    min-width: 180px;
  }

  .cta-section {
    padding: 40px 24px;
  }

  .cta-title {
    font-size: 22px;
  }
}

/* ===== Empty States ===== */
.empty-state {
  grid-column: 1 / -1;
}
</style>
