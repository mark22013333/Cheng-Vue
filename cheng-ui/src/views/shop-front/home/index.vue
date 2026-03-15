<template>
  <div class="shop-home" ref="homeRef">
    <!-- ====== Hero ====== -->
    <section class="hero">
      <div class="hero-carousel" v-if="banners.length">
        <el-carousel
          :interval="6000"
          height="560px"
          arrow="hover"
          indicator-position="none"
          :pause-on-hover="true"
        >
          <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
            <div class="hero-slide" @click="handleBannerClick(banner)">
              <img :src="getImageUrl(banner.imageUrl)" :alt="banner.title" class="hero-bg" />
              <div class="hero-veil"></div>
              <div class="hero-body" v-if="banner.title">
                <span class="hero-label">精選推薦</span>
                <h1 class="hero-headline">{{ banner.title }}</h1>
                <p class="hero-sub" v-if="banner.subTitle">{{ banner.subTitle }}</p>
                <button class="hero-btn">
                  <span>立即選購</span>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                </button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <div v-else class="hero-empty">
        <div class="hero-empty-inner">
          <span class="hero-empty-icon">✦</span>
          <h2>探索精選商品</h2>
          <p>發現屬於你的完美選擇</p>
        </div>
      </div>
    </section>

    <!-- ====== Marquee ====== -->
    <section class="marquee">
      <div class="marquee-inner">
        <div class="marquee-track">
          <template v-for="n in 4" :key="n">
            <div class="marquee-item" v-for="(f, i) in features" :key="`${n}-${i}`">
              <span class="marquee-icon">{{ f.icon }}</span>
              <span class="marquee-label">{{ f.title }}</span>
              <span class="marquee-dot">·</span>
              <span class="marquee-desc">{{ f.desc }}</span>
            </div>
            <span class="marquee-sep">✦</span>
          </template>
        </div>
      </div>
    </section>

    <!-- ====== Hot Products ====== -->
    <section class="sec sec-hot" data-reveal>
      <div class="sec-head">
        <div class="sec-head-left">
          <span class="sec-badge hot">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 23c-3.6 0-8-3.09-8-9.14C4 7.93 9.34 2.73 11.47 1c.17-.14.42-.14.58.01C12.17 1.11 12.24 1.25 12.24 1.4c0 1.72.63 3.56 1.87 4.85C16.78 3.73 17.96 3.05 18.2 2.9c.22-.13.51-.06.64.17a.5.5 0 01.05.38c-.52 2.2-.08 4.18 1.32 5.89C21.84 11.4 22 13.8 21.3 16c-.78 2.44-2.64 4.67-5.3 5.93A10.7 10.7 0 0112 23z"/></svg>
            熱門精選
          </span>
          <p class="sec-subtitle">當季最受歡迎的人氣商品</p>
        </div>
        <router-link to="/products?isHot=true" class="sec-more">
          探索更多
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </router-link>
      </div>

      <div class="hot-grid" v-loading="loading.hot">
        <div
          v-for="product in hotProducts.slice(0, 5)"
          :key="product.productId"
          class="hot-card"
          @click="goProductDetail(product.productId)"
        >
          <div class="hot-card-img">
            <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
            <div class="hot-card-overlay">
              <span class="hot-card-view">快速瀏覽</span>
            </div>
            <span
              class="hot-card-rank"
              :class="`rank-${hotProducts.indexOf(product) + 1}`"
              v-if="hotProducts.indexOf(product) < 3"
            >
              <svg class="rank-crown" viewBox="0 0 24 24" fill="currentColor">
                <path d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5z"/>
              </svg>
              <span class="rank-num">{{ hotProducts.indexOf(product) + 1 }}</span>
            </span>
            <span class="hot-card-tag" v-if="product.originalPrice && product.originalPrice > product.price">
              {{ Math.round((1 - product.price / product.originalPrice) * 100) }}% OFF
            </span>
          </div>
          <div class="hot-card-body">
            <h4>{{ product.title }}</h4>
            <div class="hot-card-price">
              <span class="hot-price-now">NT$ {{ formatPrice(product.price) }}</span>
              <span class="hot-price-was" v-if="product.originalPrice && product.originalPrice > product.price">
                NT$ {{ formatPrice(product.originalPrice) }}
              </span>
            </div>
            <div class="hot-card-meta">
              <span v-if="product.salesCount">已售 {{ product.salesCount }}</span>
              <span v-if="product.viewCount">{{ product.viewCount }} 次瀏覽</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading.hot && hotProducts.length === 0" description="暫無熱門商品" />
      </div>
    </section>

    <!-- ====== New Products: Horizontal Scroll ====== -->
    <section class="sec sec-new" data-reveal>
      <div class="sec-head">
        <div class="sec-head-left">
          <span class="sec-badge new">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
            新品上市
          </span>
          <p class="sec-subtitle">搶先體驗最新商品，掌握潮流脈動</p>
        </div>
        <div class="sec-head-right">
          <div class="scroll-nav">
            <button class="scroll-arrow" @click="scrollNew(-1)" aria-label="往左">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
            </button>
            <button class="scroll-arrow" @click="scrollNew(1)" aria-label="往右">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </button>
          </div>
          <router-link to="/products?isNew=true" class="sec-more">
            查看全部
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </router-link>
        </div>
      </div>

      <div class="hscroll" ref="newScrollRef" v-loading="loading.new">
        <div class="hscroll-track">
          <div
            v-for="product in newProducts.slice(0, 8)"
            :key="product.productId"
            class="hscroll-card"
            @click="goProductDetail(product.productId)"
          >
            <div class="hscroll-card-img">
              <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
              <span class="hscroll-tag">NEW</span>
              <div class="hscroll-card-actions">
                <button class="hscroll-cart-btn" @click.stop="addToCart(product, $event)">
                  加入購物車
                </button>
              </div>
            </div>
            <div class="hscroll-card-body">
              <h4>{{ product.title }}</h4>
              <p class="hscroll-card-sub" v-if="product.subTitle">{{ product.subTitle }}</p>
              <div class="hscroll-card-meta">
                <span class="hscroll-price">NT$ {{ formatPrice(product.price) }}</span>
                <span class="hscroll-sales">已售 {{ product.salesCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading.new && newProducts.length === 0" description="暫無新品" />
      </div>
    </section>

    <!-- ====== Recommend: Grid ====== -->
    <section class="sec sec-recommend" data-reveal>
      <div class="sec-head centered">
        <h2 class="sec-title">
          <span class="sec-title-deco">◆</span>
          精選推薦
          <span class="sec-title-deco">◆</span>
        </h2>
        <p class="sec-subtitle">為您挑選的優質好物</p>
      </div>

      <div class="recommend-grid" v-loading="loading.recommend">
        <div
          v-for="product in recommendProducts"
          :key="product.productId"
          class="recommend-card"
          @click="goProductDetail(product.productId)"
        >
          <div class="recommend-card-img">
            <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
            <div class="recommend-card-badge" v-if="product.isRecommend">推薦</div>
          </div>
          <div class="recommend-card-body">
            <h4>{{ product.title }}</h4>
            <div class="recommend-card-meta">
              <span class="recommend-price">NT$ {{ formatPrice(product.price) }}</span>
              <span class="recommend-sep">·</span>
              <span class="recommend-sales">銷量 {{ product.salesCount || 0 }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading.recommend && recommendProducts.length === 0" description="暫無推薦商品" />
      </div>

      <div class="sec-footer">
        <router-link to="/products?isRecommend=true" class="outline-btn">
          探索所有推薦商品
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </router-link>
      </div>
    </section>

    <!-- ====== Articles: Featured ====== -->
    <section class="sec sec-articles" data-reveal v-if="latestArticles.length > 0">
      <div class="sec-head">
        <div class="sec-head-left">
          <span class="sec-badge blog">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M19 5v14H5V5h14m0-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
            文章精選
          </span>
          <p class="sec-subtitle">探索最新的商品資訊與生活提案</p>
        </div>
        <router-link to="/articles" class="sec-more">
          查看全部
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </router-link>
      </div>

      <div class="articles-layout" v-loading="loading.article">
        <div
          class="article-featured"
          v-if="latestArticles[0]"
          @click="goArticleDetail(latestArticles[0].articleId)"
        >
          <div class="article-featured-cover">
            <img v-if="latestArticles[0].coverImage" :src="getImageUrl(latestArticles[0].coverImage)" :alt="latestArticles[0].title" />
            <div class="article-placeholder" v-else><span>文章</span></div>
          </div>
          <div class="article-featured-body">
            <h3>{{ latestArticles[0].title }}</h3>
            <p v-if="latestArticles[0].summary">{{ latestArticles[0].summary }}</p>
            <div class="article-meta">
              <span>{{ formatArticleDate(latestArticles[0].createTime) }}</span>
              <span>{{ latestArticles[0].viewCount || 0 }} 次瀏覽</span>
            </div>
          </div>
        </div>
        <div class="articles-side">
          <div
            v-for="article in latestArticles.slice(1, 4)"
            :key="article.articleId"
            class="article-sm"
            @click="goArticleDetail(article.articleId)"
          >
            <div class="article-sm-cover">
              <img v-if="article.coverImage" :src="getImageUrl(article.coverImage)" :alt="article.title" />
              <div class="article-placeholder sm" v-else><span>文章</span></div>
            </div>
            <div class="article-sm-body">
              <h4>{{ article.title }}</h4>
              <p class="article-sm-summary" v-if="article.summary">{{ article.summary }}</p>
              <span class="article-sm-date">{{ formatArticleDate(article.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== CTA ====== -->
    <section class="sec-cta" data-reveal>
      <div class="cta-bg"></div>
      <div class="cta-inner">
        <h3 class="cta-title">加入會員享專屬優惠</h3>
        <p class="cta-desc">立即註冊，搶先獲得新品資訊與限定折扣</p>
        <router-link to="/register" class="cta-btn">
          立即加入
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listFrontBanners, listHotProducts, listNewProducts, listRecommendProducts, listLatestArticles, getProductSkus } from '@/api/shop/front'
import { addToCart as addToCartApi } from '@/api/shop/cart'
import { ElMessage } from 'element-plus'
import { useFlyToCart } from '@/composables/useFlyToCart'

const router = useRouter()
const { flyToCart } = useFlyToCart()
const homeRef = ref(null)
const newScrollRef = ref(null)

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
  {icon: '🏪', title: '超商取貨', desc: '便利商店輕鬆取'},
  {icon: '🔒', title: '安全付款', desc: '多元支付保障'},
  {icon: '✨', title: '精選商品', desc: '嚴選高品質好物'},
  {icon: '🎁', title: '限時優惠', desc: '立即領取折價券'}
]

/* ── Scroll ── */
function scrollNew(dir) {
  const el = newScrollRef.value
  if (!el) return
  el.scrollBy({ left: dir * 320, behavior: 'smooth' })
}

/* ── IntersectionObserver ── */
let observer = null

function initReveal() {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('revealed')
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.08, rootMargin: '0px 0px -40px 0px' }
  )
  const els = homeRef.value?.querySelectorAll('[data-reveal]') || []
  els.forEach((el) => observer.observe(el))
}

/* ── Helpers ── */
function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}

function formatPrice(price) {
  return Number(price || 0).toLocaleString('zh-TW')
}

function handleBannerClick(banner) {
  if (banner.linkValue) {
    if (banner.linkType === 'PRODUCT') router.push(`/product/${banner.linkValue}`)
    else if (banner.linkType === 'CATEGORY') router.push(`/products?categoryId=${banner.linkValue}`)
    else if (banner.linkType === 'URL') window.open(banner.linkValue, '_blank')
  }
}

function goProductDetail(id) {
  router.push(`/product/${id}`)
}

function goArticleDetail(id) {
  router.push(`/article/${id}`)
}

function formatArticleDate(d) {
  return d ? d.substring(0, 10) : ''
}

async function addToCart(product, event) {
  try {
    const res = await getProductSkus(product.productId)
    const skus = res.data || []
    if (skus.length === 0) { ElMessage.warning('此商品暫無可購買的規格'); return }
    if (skus.length === 1) {
      // 觸發飛入購物車動畫：找到最近的商品圖片元素
      if (event) {
        const cardEl = event.target.closest('.hscroll-card-img') || event.target.closest('.showcase-card')
        if (cardEl) {
          flyToCart(getImageUrl(product.mainImage), cardEl, { size: 56 })
        }
      }
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

/* ── Load Data ── */
async function loadBanners() {
  loading.value.banner = true
  try { banners.value = (await listFrontBanners('HOME_TOP')).data || [] }
  catch (e) { console.error('載入輪播圖失敗', e) }
  finally { loading.value.banner = false }
}

async function loadHotProducts() {
  loading.value.hot = true
  try { hotProducts.value = (await listHotProducts(8)).data || [] }
  catch (e) { console.error('載入熱門商品失敗', e) }
  finally { loading.value.hot = false }
}

async function loadNewProducts() {
  loading.value.new = true
  try { newProducts.value = (await listNewProducts(8)).data || [] }
  catch (e) { console.error('載入新品失敗', e) }
  finally { loading.value.new = false }
}

async function loadRecommendProducts() {
  loading.value.recommend = true
  try { recommendProducts.value = (await listRecommendProducts(8)).data || [] }
  catch (e) { console.error('載入推薦商品失敗', e) }
  finally { loading.value.recommend = false }
}

async function loadLatestArticles() {
  loading.value.article = true
  try { latestArticles.value = (await listLatestArticles(6)).data || [] }
  catch (e) { console.error('載入文章失敗', e) }
  finally { loading.value.article = false }
}

/* 當文章資料載入後，重新掃描新出現的 data-reveal 元素 */
watch(latestArticles, () => {
  nextTick(() => {
    const els = homeRef.value?.querySelectorAll('[data-reveal]:not(.revealed)') || []
    els.forEach((el) => observer?.observe(el))
  })
})

onMounted(() => {
  loadBanners()
  loadHotProducts()
  loadNewProducts()
  loadRecommendProducts()
  loadLatestArticles()
  nextTick(() => initReveal())
})

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<style scoped>
/* ═══════════════════════════════════════
   Design tokens
   ═══════════════════════════════════════ */
.shop-home {
  --accent: var(--mall-primary, #4A6B7C);
  --accent-end: var(--mall-primary-end, #5A8A9A);
  --highlight: var(--mall-accent, #A5635C);
  --text-1: var(--mall-text-primary, #303133);
  --text-2: var(--mall-text-secondary, #606266);
  --text-3: var(--mall-text-muted, #909399);
  --card: var(--mall-card-bg, #ffffff);
  --border: var(--mall-border-color, #E8E4DF);
  --page-bg: var(--mall-body-bg, #FAF8F5);
  --radius: 20px;
  --radius-sm: 12px;
  --radius-xs: 8px;
  --ease: cubic-bezier(0.4, 0, 0.2, 1);
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 20px 40px rgba(0, 0, 0, 0.1);

  display: flex;
  flex-direction: column;
  gap: 0;
}

/* ═══════════════════════════════════════
   Scroll Reveal
   ═══════════════════════════════════════ */
[data-reveal] {
  opacity: 0;
  transform: translateY(36px);
  transition: opacity 0.7s var(--ease), transform 0.7s var(--ease);
}

[data-reveal].revealed {
  opacity: 1;
  transform: translateY(0);
}

/* ═══════════════════════════════════════
   Hero
   ═══════════════════════════════════════ */
.hero {
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.hero-carousel :deep(.el-carousel__container) {
  border-radius: var(--radius);
}

.hero-carousel :deep(.el-carousel__arrow) {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 16px;
  transition: all 0.3s var(--ease);
}

.hero-carousel :deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 0.3);
}

.hero-slide {
  position: relative;
  height: 100%;
  cursor: pointer;
  overflow: hidden;
}

.hero-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 8s ease;
  animation: kenBurns 12s ease-in-out infinite alternate;
}

@keyframes kenBurns {
  from { transform: scale(1); }
  to { transform: scale(1.06); }
}

.hero-veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.65) 0%,
    rgba(0, 0, 0, 0.25) 40%,
    rgba(0, 0, 0, 0.05) 70%,
    transparent 100%
  );
}

.hero-body {
  position: absolute;
  bottom: 56px;
  left: 56px;
  max-width: 520px;
  color: white;
}

.hero-label {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
  padding: 6px 18px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 24px;
  margin-bottom: 20px;
}

.hero-headline {
  font-size: 40px;
  font-weight: 800;
  line-height: 1.15;
  margin: 0 0 14px;
  letter-spacing: -0.02em;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.3);
}

.hero-sub {
  font-size: 17px;
  margin: 0 0 28px;
  opacity: 0.9;
  line-height: 1.6;
  font-weight: 400;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 32px;
  background: white;
  color: var(--text-1);
  border: none;
  border-radius: 32px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s var(--ease);
  letter-spacing: 0.02em;
}

.hero-btn:hover {
  background: var(--accent);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
}

.hero-btn svg {
  transition: transform 0.3s var(--ease);
}

.hero-btn:hover svg {
  transform: translateX(4px);
}

.hero-empty {
  height: 400px;
  background: linear-gradient(135deg, var(--accent) 0%, var(--accent-end) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius);
  text-align: center;
  color: white;
}

.hero-empty-icon {
  font-size: 40px;
  display: block;
  margin-bottom: 16px;
  animation: pulse 2.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1) rotate(0deg); }
  50% { opacity: 0.7; transform: scale(1.15) rotate(15deg); }
}

.hero-empty h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.hero-empty p {
  opacity: 0.8;
  font-size: 15px;
}

/* ═══════════════════════════════════════
   Marquee
   ═══════════════════════════════════════ */
.marquee {
  padding: 16px 0;
  overflow: hidden;
  margin: 28px 0 20px;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}

.marquee-inner {
  overflow: hidden;
  mask-image: linear-gradient(to right, transparent, black 5%, black 95%, transparent);
  -webkit-mask-image: linear-gradient(to right, transparent, black 5%, black 95%, transparent);
}

.marquee-track {
  display: flex;
  align-items: center;
  gap: 32px;
  width: max-content;
  animation: marqueeScroll 40s linear infinite;
}

@keyframes marqueeScroll {
  from { transform: translateX(0); }
  to { transform: translateX(-25%); }
}

.marquee:hover .marquee-track {
  animation-play-state: paused;
}

.marquee-item {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.marquee-icon {
  font-size: 20px;
}

.marquee-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}

.marquee-dot {
  color: var(--text-3);
}

.marquee-desc {
  font-size: 13px;
  color: var(--text-2);
}

.marquee-sep {
  color: var(--accent);
  font-size: 10px;
  opacity: 0.5;
  flex-shrink: 0;
}

/* ═══════════════════════════════════════
   Section Common
   ═══════════════════════════════════════ */
.sec {
  padding: 56px 0;
}

.sec + .sec {
  border-top: 1px solid var(--border);
}

.sec-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  gap: 20px;
}

.sec-head.centered {
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.sec-head-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sec-head-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sec-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 15px;
  font-weight: 700;
  border-radius: var(--radius-xs);
  margin-bottom: 2px;
  letter-spacing: -0.01em;
}

.sec-badge svg {
  flex-shrink: 0;
}

.sec-badge.hot {
  background: linear-gradient(135deg, rgba(165, 99, 92, 0.12), rgba(165, 99, 92, 0.06));
  color: var(--highlight);
}

.sec-badge.new {
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.12), rgba(74, 107, 124, 0.06));
  color: var(--accent);
}

.sec-badge.blog {
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.12), rgba(74, 107, 124, 0.06));
  color: var(--accent);
}

.sec-badge.recommend {
  background: none;
  font-size: 16px;
  padding: 0;
  color: var(--accent);
}

.sec-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-1);
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.sec-title-deco {
  color: var(--accent);
  font-size: 14px;
  margin: 0 10px;
  opacity: 0.6;
}

.sec-subtitle {
  font-size: 14px;
  color: var(--text-2);
  margin: 0;
  line-height: 1.5;
}

.sec-more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--accent);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 24px;
  border: 1.5px solid var(--accent);
  transition: all 0.25s var(--ease);
  white-space: nowrap;
  flex-shrink: 0;
}

.sec-more:hover {
  background: var(--accent);
  color: white;
}

.sec-more svg {
  transition: transform 0.25s var(--ease);
}

.sec-more:hover svg {
  transform: translateX(3px);
}

.sec-footer {
  text-align: center;
  margin-top: 36px;
}

.outline-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 36px;
  border: 2px solid var(--accent);
  color: var(--accent);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  border-radius: 32px;
  transition: all 0.3s var(--ease);
}

.outline-btn:hover {
  background: var(--accent);
  color: white;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.outline-btn svg {
  transition: transform 0.25s var(--ease);
}

.outline-btn:hover svg {
  transform: translateX(4px);
}

/* ═══════════════════════════════════════
   Hot Products Grid
   ═══════════════════════════════════════ */
.hot-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px;
}

.hot-card {
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--card);
  cursor: pointer;
  transition: all 0.3s var(--ease);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
}

.hot-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.hot-card-img {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  flex-shrink: 0;
}

.hot-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.hot-card:hover .hot-card-img img {
  transform: scale(1.06);
}

.hot-card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s var(--ease);
}

.hot-card:hover .hot-card-overlay {
  opacity: 1;
}

.hot-card-view {
  padding: 8px 20px;
  background: white;
  color: var(--text-1);
  font-size: 13px;
  font-weight: 600;
  border-radius: 20px;
  transform: translateY(8px);
  transition: transform 0.3s var(--ease);
}

.hot-card:hover .hot-card-view {
  transform: translateY(0);
}

.hot-card-rank {
  position: absolute;
  top: -4px;
  left: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 3;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}
.rank-crown {
  width: 22px;
  height: 22px;
  margin-bottom: -6px;
  position: relative;
  z-index: 1;
}
.rank-num {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 900;
  color: white;
  border-radius: 50%;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
}
/* 金牌 */
.rank-1 .rank-crown { color: #F59E0B; }
.rank-1 .rank-num {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  box-shadow: 0 0 0 2.5px #FDE68A, 0 2px 8px rgba(245, 158, 11, 0.4);
}
/* 銀牌 */
.rank-2 .rank-crown { color: #94A3B8; }
.rank-2 .rank-num {
  background: linear-gradient(135deg, #94A3B8 0%, #64748B 100%);
  box-shadow: 0 0 0 2.5px #CBD5E1, 0 2px 8px rgba(148, 163, 184, 0.4);
}
/* 銅牌 */
.rank-3 .rank-crown { color: #D97757; }
.rank-3 .rank-num {
  background: linear-gradient(135deg, #D97757 0%, #B45B3E 100%);
  box-shadow: 0 0 0 2.5px #FECACA, 0 2px 8px rgba(217, 119, 87, 0.4);
}

.hot-card-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 8px;
  background: var(--highlight);
  color: white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 6px;
  letter-spacing: 0.5px;
}

.hot-card-body {
  padding: 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hot-card-body h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-card-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: auto;
}

.hot-price-now {
  font-size: 16px;
  font-weight: 700;
  color: var(--highlight);
  font-variant-numeric: tabular-nums;
}

.hot-price-was {
  font-size: 12px;
  color: var(--text-3);
  text-decoration: line-through;
}

.hot-card-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-3);
}

.hot-card-meta span {
  white-space: nowrap;
}

/* ═══════════════════════════════════════
   Horizontal Scroll (New Products)
   ═══════════════════════════════════════ */
.scroll-nav {
  display: flex;
  gap: 8px;
}

.scroll-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1.5px solid var(--border);
  background: var(--card);
  color: var(--text-2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s var(--ease);
}

.scroll-arrow:hover {
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: var(--shadow-sm);
}

.hscroll {
  overflow: hidden;
  margin: 0 -20px;
  padding: 0 20px;
  min-height: 300px;
}

.hscroll-track {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  padding: 8px 0 16px;
  scroll-behavior: smooth;
}

.hscroll-track::-webkit-scrollbar {
  display: none;
}

.hscroll-card {
  flex: 0 0 280px;
  scroll-snap-align: start;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--card);
  cursor: pointer;
  transition: all 0.3s var(--ease);
  box-shadow: var(--shadow-sm);
}

.hscroll-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.hscroll-card-img {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.hscroll-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.hscroll-card:hover .hscroll-card-img img {
  transform: scale(1.06);
}

.hscroll-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  background: var(--accent);
  color: white;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1px;
  border-radius: 4px;
}

.hscroll-card-actions {
  position: absolute;
  bottom: 10px;
  left: 10px;
  right: 10px;
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.3s var(--ease);
}

.hscroll-card:hover .hscroll-card-actions {
  opacity: 1;
  transform: translateY(0);
}

.hscroll-cart-btn {
  width: 100%;
  padding: 10px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border: none;
  border-radius: var(--radius-xs);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  cursor: pointer;
  transition: all 0.25s var(--ease);
}

.hscroll-cart-btn:hover {
  background: var(--accent);
  color: white;
}

.hscroll-card-body {
  padding: 16px;
}

.hscroll-card-body h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hscroll-card-sub {
  font-size: 12px;
  color: var(--text-3);
  margin: 0 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hscroll-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hscroll-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--highlight);
  font-variant-numeric: tabular-nums;
}

.hscroll-sales {
  font-size: 12px;
  color: var(--text-3);
}

/* ═══════════════════════════════════════
   Recommend Grid
   ═══════════════════════════════════════ */
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 200px;
}

.recommend-card {
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--card);
  cursor: pointer;
  transition: all 0.3s var(--ease);
  box-shadow: var(--shadow-sm);
}

.recommend-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.recommend-card-img {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
}

.recommend-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.recommend-card:hover .recommend-card-img img {
  transform: scale(1.05);
}

.recommend-card-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  background: var(--highlight);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 4px;
}

.recommend-card-body {
  padding: 14px;
}

.recommend-card-body h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  margin: 0 0 8px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommend-card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.recommend-price {
  font-weight: 700;
  color: var(--highlight);
  font-variant-numeric: tabular-nums;
}

.recommend-sep {
  color: var(--border);
}

.recommend-sales {
  color: var(--text-3);
}

/* ═══════════════════════════════════════
   Articles: Featured Layout
   ═══════════════════════════════════════ */
.articles-layout {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
  min-height: 300px;
}

.article-featured {
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--card);
  cursor: pointer;
  transition: all 0.3s var(--ease);
  box-shadow: var(--shadow-sm);
}

.article-featured:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.article-featured-cover {
  aspect-ratio: 2 / 1;
  overflow: hidden;
}

.article-featured-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease);
}

.article-featured:hover .article-featured-cover img {
  transform: scale(1.04);
}

.article-featured-body {
  padding: 24px;
}

.article-featured-body h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0 0 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-featured-body p {
  font-size: 14px;
  color: var(--text-2);
  margin: 0 0 16px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-3);
}

.articles-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-sm {
  display: flex;
  gap: 14px;
  padding: 12px;
  border-radius: var(--radius-xs);
  cursor: pointer;
  transition: all 0.25s var(--ease);
  background: var(--card);
  box-shadow: var(--shadow-sm);
}

.article-sm:hover {
  background: var(--page-bg);
  box-shadow: var(--shadow-md);
  transform: translateX(4px);
}

.article-sm-cover {
  width: 100px;
  height: 80px;
  border-radius: var(--radius-xs);
  overflow: hidden;
  flex-shrink: 0;
}

.article-sm-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-sm-body {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.article-sm-body h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-sm-summary {
  font-size: 12px;
  color: var(--text-2);
  margin: 0 0 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-sm-date {
  font-size: 12px;
  color: var(--text-3);
}

.article-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--accent), var(--accent-end));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.article-placeholder.sm {
  font-size: 13px;
}

/* ═══════════════════════════════════════
   CTA
   ═══════════════════════════════════════ */
.sec-cta {
  position: relative;
  padding: 72px 40px;
  border-radius: var(--radius);
  overflow: hidden;
  text-align: center;
  margin-top: 24px;
}

.cta-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--accent) 0%, var(--highlight) 50%, var(--accent-end) 100%);
  background-size: 200% 200%;
  animation: ctaGradient 8s ease infinite;
}

@keyframes ctaGradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.cta-inner {
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 30px;
  font-weight: 800;
  color: white;
  margin: 0 0 12px;
  letter-spacing: -0.02em;
}

.cta-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 28px;
  font-weight: 400;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 40px;
  background: white;
  color: var(--accent);
  text-decoration: none;
  font-size: 15px;
  font-weight: 700;
  border-radius: 32px;
  transition: all 0.3s var(--ease);
  letter-spacing: 0.02em;
}

.cta-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}

.cta-btn svg {
  transition: transform 0.25s var(--ease);
}

.cta-btn:hover svg {
  transform: translateX(4px);
}

/* ═══════════════════════════════════════
   Responsive
   ═══════════════════════════════════════ */
@media (max-width: 1200px) {
  .hot-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .hot-card:nth-child(5) {
    display: none;
  }

  .recommend-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .articles-layout {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 900px) {
  .sec {
    padding: 40px 0;
  }

  .hero-body {
    left: 32px;
    bottom: 32px;
    right: 32px;
  }

  .hero-headline {
    font-size: 30px;
  }

  .hero-sub {
    font-size: 15px;
  }

  .sec-title {
    font-size: 24px;
  }

  .hot-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .hot-card:nth-child(n+4) {
    display: none;
  }

  .hscroll-card {
    flex: 0 0 240px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .articles-layout {
    grid-template-columns: 1fr;
  }

  .sec-head {
    flex-direction: column;
    gap: 12px;
  }

  .sec-head-right {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 600px) {
  .sec {
    padding: 32px 0;
  }

  .hero-carousel :deep(.el-carousel__container) {
    height: 340px !important;
  }

  .hero-body {
    left: 20px;
    bottom: 24px;
    right: 20px;
  }

  .hero-headline {
    font-size: 24px;
  }

  .hero-sub {
    font-size: 14px;
    margin-bottom: 20px;
  }

  .hero-btn {
    padding: 12px 24px;
    font-size: 14px;
  }

  .marquee {
    margin: 16px 0 12px;
  }

  .sec-title {
    font-size: 20px;
  }

  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .hot-card:nth-child(n+3) {
    display: none;
  }

  .hscroll-card {
    flex: 0 0 200px;
  }

  .recommend-grid {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .article-sm-cover {
    width: 80px;
    height: 64px;
  }

  .sec-cta {
    padding: 48px 24px;
    margin-top: 16px;
  }

  .cta-title {
    font-size: 22px;
  }

  .cta-desc {
    font-size: 14px;
  }

  .scroll-nav {
    display: none;
  }
}
</style>
