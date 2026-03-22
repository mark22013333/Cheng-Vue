<template>
  <div class="shop-layout">
    <!-- 頂部公告欄（滑動跑馬燈） -->
    <div v-if="showAnnouncement && announcements.length" class="announcement-bar">
      <div class="announcement-marquee">
        <div class="announcement-track">
          <template v-for="n in 10" :key="n">
            <template v-for="(item, i) in announcements" :key="`${n}-${i}`">
              <a
                v-if="item.linkType && item.linkType !== 'NONE'"
                class="announcement-item announcement-link"
                :href="getAnnouncementHref(item)"
                @click.prevent="handleAnnouncementClick(item)"
              >
                {{ item.text }}
                <svg class="announcement-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
              </a>
              <span v-else class="announcement-item">{{ item.text }}</span>
            </template>
            <span class="announcement-sep">✦</span>
          </template>
        </div>
      </div>
      <button class="announcement-close" @click="showAnnouncement = false" aria-label="關閉公告">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
      </button>
    </div>

    <!-- 頂部導航 -->
    <header class="shop-header" :class="{ 'light-header': themeStore.isLightHeader }">
      <div class="header-container">
        <div class="logo" @click="goHome">
          <img src="@/assets/logo/logo.png" alt="Logo" class="logo-img" />
          <span class="logo-text">CoolApps</span>
        </div>

        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首頁</router-link>
          <router-link to="/products" class="nav-item">全部商品</router-link>
          <div
            class="nav-item category-nav"
            @mouseenter="showCategoryDropdown = true"
            @mouseleave="showCategoryDropdown = false"
          >
            <span>商品分類</span>
            <svg class="caret-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>
            <transition name="dropdown-fade">
              <div v-show="showCategoryDropdown" class="category-dropdown">
                <div class="category-dropdown-inner">
                  <router-link
                    v-for="cat in categories"
                    :key="cat.categoryId"
                    :to="{ path: '/products', query: { categoryId: cat.categoryId } }"
                    class="category-link"
                    @click="showCategoryDropdown = false"
                  >
                    <span class="category-link-name">{{ cat.name }}</span>
                    <span v-if="cat.children && cat.children.length" class="category-link-count">
                      {{ cat.children.length }} 子分類
                    </span>
                  </router-link>
                  <div v-if="!categories.length" class="category-empty">暫無分類</div>
                </div>
              </div>
            </transition>
          </div>
          <router-link to="/articles" class="nav-item">文章專區</router-link>
        </nav>

        <div class="header-right">
          <div class="search-box">
            <svg class="search-box-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            <input
              v-model="searchKeyword"
              class="search-native-input"
              placeholder="搜尋商品..."
              @keyup.enter="handleSearch"
            />
          </div>
          <button id="shop-cart-icon" class="header-icon-btn cart-btn" @click="goCart" aria-label="購物車">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
          </button>

          <!-- 登入/使用者入口 -->
          <div v-if="isLoggedIn" class="user-menu">
            <el-dropdown trigger="click">
              <div class="user-avatar" :class="{ 'has-avatar': memberAvatar }">
                <img v-if="memberAvatar" :src="memberAvatar" alt="頭像" class="avatar-img" />
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goMember">
                    <el-icon><User /></el-icon>
                    會員中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="goOrders">
                    <el-icon><Document /></el-icon>
                    我的訂單
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    登出
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else class="auth-buttons">
            <button class="auth-login-btn" @click="goLogin">登入</button>
            <button class="auth-register-btn" @click="goRegister">註冊</button>
          </div>

          <el-popover :visible="showThemePicker" placement="bottom" :width="320" trigger="click">
            <template #reference>
              <button class="header-icon-btn theme-toggle" @click="showThemePicker = !showThemePicker" aria-label="主題">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
              </button>
            </template>
            <div class="theme-picker">
              <div class="theme-picker-title">選擇主題</div>
              <div class="theme-grid">
                <div
                  v-for="(theme, key) in PRESET_THEMES"
                  :key="key"
                  class="theme-item"
                  :class="{ active: themeStore.currentTheme === key }"
                  @click="selectTheme(key)"
                >
                  <div class="theme-preview" :style="{ background: theme.headerBg }"></div>
                  <span class="theme-name">{{ theme.name }}</span>
                </div>
              </div>
            </div>
          </el-popover>

          <!-- 漢堡選單（手機） -->
          <button class="mobile-menu-btn" @click="showMobileMenu = true" aria-label="選單">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="4" y1="7" x2="20" y2="7"/><line x1="4" y1="12" x2="20" y2="12"/><line x1="4" y1="17" x2="20" y2="17"/></svg>
          </button>
        </div>
      </div>
    </header>

    <!-- 手機側滑選單 -->
    <transition name="drawer-fade">
      <div v-if="showMobileMenu" class="mobile-overlay" @click="showMobileMenu = false"></div>
    </transition>
    <transition name="drawer-slide">
      <nav v-if="showMobileMenu" class="mobile-drawer">
        <div class="drawer-header">
          <div class="drawer-logo">
            <img src="@/assets/logo/logo.png" alt="Logo" class="drawer-logo-img" />
            <span>CoolApps</span>
          </div>
          <button class="drawer-close" @click="showMobileMenu = false" aria-label="關閉">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="drawer-search">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          <input v-model="searchKeyword" placeholder="搜尋商品..." @keyup.enter="handleSearch(); showMobileMenu = false" />
        </div>
        <div class="drawer-nav">
          <router-link to="/" class="drawer-nav-item" @click="showMobileMenu = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9,22 9,12 15,12 15,22"/></svg>
            首頁
          </router-link>
          <router-link to="/products" class="drawer-nav-item" @click="showMobileMenu = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="m7.5 4.27 9 5.15"/><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5M12 22V12"/></svg>
            全部商品
          </router-link>
          <router-link to="/articles" class="drawer-nav-item" @click="showMobileMenu = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2"/><path d="M18 14h-8M15 18h-5M10 6h8"/></svg>
            文章專區
          </router-link>
          <router-link to="/cart" class="drawer-nav-item" @click="showMobileMenu = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
            購物車
            <span v-if="cartCount > 0" class="drawer-badge">{{ cartCount }}</span>
          </router-link>
        </div>
        <div v-if="categories.length" class="drawer-categories">
          <div class="drawer-section-label">商品分類</div>
          <div class="drawer-category-list">
            <router-link
              v-for="cat in categories"
              :key="cat.categoryId"
              :to="{ path: '/products', query: { categoryId: cat.categoryId } }"
              class="drawer-category-chip"
              @click="showMobileMenu = false"
            >{{ cat.name }}</router-link>
          </div>
        </div>
        <div class="drawer-footer">
          <template v-if="isLoggedIn">
            <button class="drawer-member-btn" @click="goMember(); showMobileMenu = false">
              <div class="drawer-avatar" :class="{ 'has-avatar': memberAvatar }">
                <img v-if="memberAvatar" :src="memberAvatar" alt="" />
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </div>
              會員中心
            </button>
            <button class="drawer-logout-btn" @click="handleLogout(); showMobileMenu = false">登出</button>
          </template>
          <template v-else>
            <button class="drawer-login-btn" @click="goLogin(); showMobileMenu = false">登入帳號</button>
            <button class="drawer-register-btn" @click="goRegister(); showMobileMenu = false">免費註冊</button>
          </template>
        </div>
      </nav>
    </transition>

    <!-- 主要內容區 -->
    <main class="shop-main" :class="{ 'is-fullwidth': isHomePage }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 -->
    <footer class="shop-footer">
      <div class="footer-container">
        <div class="footer-section footer-about">
          <div class="footer-logo">
            <img src="@/assets/logo/logo.png" alt="Logo" class="footer-logo-img" />
            <span>CoolApps</span>
          </div>
          <p>提供優質商品與貼心服務，讓每一次購物都是美好體驗。</p>
        </div>
        <div class="footer-section">
          <h4>顧客服務</h4>
          <ul class="footer-links">
            <li><router-link to="/articles">常見問題</router-link></li>
            <li><router-link to="/legal/terms">服務條款</router-link></li>
            <li><router-link to="/legal/privacy">隱私政策</router-link></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4>聯絡我們</h4>
          <ul class="footer-links">
            <li>Email: service@coolapps.com</li>
            <li>電話: 02-1234-5678</li>
            <li>週一至週五 9:00 - 18:00</li>
          </ul>
        </div>
        <div class="footer-section">
          <h4>關注我們</h4>
          <div class="footer-social">
            <a href="#" class="social-link" title="Facebook">FB</a>
            <a href="#" class="social-link" title="Instagram">IG</a>
            <a href="#" class="social-link" title="LINE">LINE</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2025 CoolApps. All rights reserved.</p>
      </div>
    </footer>

    <!-- Cookie 同意提示 -->
    <CookieConsent />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Document, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMallThemeStore, PRESET_THEMES } from '@/store/modules/mallTheme'
import { useCartStore } from '@/store/modules/cart'
import { getMemberToken } from '@/utils/memberAuth'
import useMemberStore from '@/store/modules/member'
import CookieConsent from '@/components/CookieConsent/index.vue'
import { useMarketingTracker } from '@/composables/useMarketingTracker'
import { listCategories, getAnnouncement } from '@/api/shop/front'

const router = useRouter()
const route = useRoute()
const themeStore = useMallThemeStore()
const cartStore = useCartStore()
const memberStore = useMemberStore()
const { trackSearch } = useMarketingTracker()
const searchKeyword = ref('')
const showThemePicker = ref(false)
const showAnnouncement = ref(true)
const announcements = ref([])
const showCategoryDropdown = ref(false)
const showMobileMenu = ref(false)
const categories = ref([])

// 路由變化時關閉手機選單
watch(() => route.path, () => { showMobileMenu.value = false })

// 首頁偵測：滿版佈局
const isHomePage = computed(() => route.path === '/')

// 購物車數量（從 store 取得）
const cartCount = computed(() => cartStore.totalQuantity)

// 是否已登入（使用 store 的響應式 token，而非直接讀 Cookie）
const isLoggedIn = computed(() => !!memberStore.token)

// 會員頭像
const memberAvatar = computed(() => memberStore.avatar)

function goHome() {
  router.push('/')
}

function goCart() {
  router.push('/cart')
}

/** 排除認證頁面作為 redirect 目標，避免 redirect 參數不斷累積 */
const AUTH_PATHS = ['/login', '/register', '/forgot-password', '/reset-password']

function getRedirectTarget() {
  const current = route.path
  return AUTH_PATHS.some(p => current.startsWith(p)) ? '/' : route.fullPath
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: getRedirectTarget() } })
}

function goRegister() {
  router.push({ path: '/register', query: { redirect: getRedirectTarget() } })
}

function goMember() {
  router.push('/member')
}

function goOrders() {
  router.push('/member/orders')
}

/** 公告連結點擊 */
function getAnnouncementHref(item) {
  if (item.linkType === 'PRODUCT') return `/product/${item.linkValue}`
  if (item.linkType === 'ARTICLE') return `/article/${item.linkValue}`
  if (item.linkType === 'CATEGORY') return `/products?categoryId=${item.linkValue}`
  if (item.linkType === 'URL') return item.linkValue
  return '#'
}

function handleAnnouncementClick(item) {
  const href = getAnnouncementHref(item)
  if (item.linkType === 'URL' && href.startsWith('http')) {
    window.open(href, '_blank')
  } else {
    router.push(href)
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('確定要登出嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await memberStore.logOut()
    cartStore.loadGuestCart()
    ElMessage.success('已登出')
    router.push('/login')
  } catch (e) {
    // 取消登出
  }
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    trackSearch(searchKeyword.value.trim())
    router.push({ path: '/products', query: { title: searchKeyword.value.trim() } })
    searchKeyword.value = ''
  }
}

function selectTheme(key) {
  themeStore.setTheme(key)
  showThemePicker.value = false
}

async function loadCategories() {
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } catch (e) {
    categories.value = []
  }
}

/** 每項格式: { text, linkType, linkValue } */
const FALLBACK_ANNOUNCEMENTS = [
  { text: '全館滿 $999 免運費', linkType: 'NONE', linkValue: '' },
  { text: '新會員首單享 9 折優惠', linkType: 'NONE', linkValue: '' }
]

async function loadAnnouncements() {
  try {
    const res = await getAnnouncement()
    const data = res?.data
    let parsed = []
    if (typeof data === 'string' && data.startsWith('[')) {
      parsed = JSON.parse(data)
    } else if (Array.isArray(data)) {
      parsed = data
    }
    // 相容舊格式（純字串陣列）→ 轉換為物件格式
    if (parsed.length > 0 && typeof parsed[0] === 'string') {
      parsed = parsed.map(t => ({ text: t, linkType: 'NONE', linkValue: '' }))
    }
    announcements.value = parsed.length > 0 ? parsed : FALLBACK_ANNOUNCEMENTS
  } catch {
    announcements.value = FALLBACK_ANNOUNCEMENTS
  }
}

onMounted(() => {
  themeStore.init()
  cartStore.init()
  loadCategories()
  loadAnnouncements()
  // 僅在 store 缺少會員資料時才呼叫（登入後 store 已有完整資料，不需重複取得）
  if (getMemberToken() && !memberStore.id) {
    memberStore.getProfile().catch((err) => {
      console.warn('[ShopLayout] getProfile 失敗', err)
    })
  }
})
</script>

<style scoped>
.shop-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--mall-body-bg, #FAF8F5);
}

/* === 公告欄（滑動跑馬燈） === */
.announcement-bar {
  background: var(--mall-primary, #4A6B7C);
  color: white;
  padding: 9px 0;
  font-size: 13px;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1001;
  display: flex;
  align-items: center;
}

.announcement-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  margin-right: 12px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
  z-index: 2;
}

.announcement-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.announcement-marquee {
  overflow: hidden;
  flex: 1;
  min-width: 0;
  mask-image: linear-gradient(to right, transparent, black 2%, black 98%, transparent);
  -webkit-mask-image: linear-gradient(to right, transparent, black 2%, black 98%, transparent);
}

.announcement-track {
  display: flex;
  align-items: center;
  gap: 36px;
  width: max-content;
  animation: announcementScroll 30s linear infinite;
}

.announcement-bar:hover .announcement-track {
  animation-play-state: paused;
}

@keyframes announcementScroll {
  from { transform: translateX(0); }
  to { transform: translateX(-10%); }
}

.announcement-item {
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 500;
}

.announcement-link {
  color: white;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.announcement-link:hover {
  opacity: 0.85;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.announcement-arrow {
  flex-shrink: 0;
  opacity: 0.7;
}

.announcement-sep {
  flex-shrink: 0;
  opacity: 0.5;
  font-size: 10px;
}

/* === Header === */
.shop-header {
  background: var(--mall-header-bg, #FFFFFF);
  color: var(--mall-header-text, #303133);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid var(--mall-border-color, #EDE8E2);
  transition: background 0.3s, border-color 0.3s;
}

.light-header .logo-img {
  filter: none;
}

.shop-header:not(.light-header) .logo-img {
  filter: brightness(0) invert(1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
  flex-shrink: 0;
}

.logo-img {
  height: 32px;
  transition: filter 0.3s;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: var(--mall-header-text, #303133);
  font-family: 'Inter', 'Avenir', 'Helvetica Neue', Arial, sans-serif;
}

.shop-header:not(.light-header) .logo-text {
  color: white;
}

/* === 導航 === */
.nav-menu {
  display: flex;
  gap: 4px;
  align-items: center;
  flex: 1;
  justify-content: center;
}

.nav-item {
  color: var(--mall-header-text-secondary, #606266);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 7px 16px;
  border-radius: 8px;
  position: relative;
  transition: color 0.2s, background 0.2s;
  letter-spacing: 0.3px;
}

.shop-header:not(.light-header) .nav-item {
  color: rgba(255, 255, 255, 0.85);
}

.nav-item:hover {
  color: var(--mall-primary, #4A6B7C);
  background: rgba(74, 107, 124, 0.07);
}

.nav-item.router-link-active {
  color: var(--mall-primary, #4A6B7C);
  background: rgba(74, 107, 124, 0.1);
  font-weight: 600;
}

.shop-header:not(.light-header) .nav-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.shop-header:not(.light-header) .nav-item.router-link-active {
  color: white;
  background: rgba(255, 255, 255, 0.15);
}

/* === 分類下拉 === */
.category-nav {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 3px;
}

.caret-icon {
  transition: transform 0.25s ease;
  flex-shrink: 0;
}

.category-nav:hover .caret-icon {
  transform: rotate(180deg);
}

.category-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
}

.category-dropdown-inner {
  background: var(--mall-card-bg, #FFFFFF);
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--mall-border-color, #EDE8E2);
  padding: 10px;
  min-width: 240px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px;
}

.category-link {
  display: flex;
  flex-direction: column;
  padding: 10px 14px;
  border-radius: 10px;
  text-decoration: none;
  transition: background 0.2s;
}

.category-link:hover {
  background: var(--mall-body-bg, #FAF8F5);
}

.category-link-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--mall-header-text, #303133);
}

.category-link-count {
  font-size: 12px;
  color: #9A8B7D;
  margin-top: 2px;
}

.category-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 20px;
  color: #9A8B7D;
  font-size: 13px;
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

/* === Header 右側 === */
.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

/* 搜尋列 */
.search-box {
  display: flex;
  align-items: center;
  background: var(--mall-body-bg, #F5F2EF);
  border: 1px solid var(--mall-border-color, #EDE8E2);
  border-radius: 24px;
  padding: 0 14px;
  height: 36px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-box:focus-within {
  border-color: var(--mall-primary, #4A6B7C);
  box-shadow: 0 0 0 3px rgba(74, 107, 124, 0.1);
}

.shop-header:not(.light-header) .search-box {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.15);
}

.shop-header:not(.light-header) .search-box:focus-within {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.06);
}

.search-box-icon {
  color: #9A8B7D;
  flex-shrink: 0;
  margin-right: 8px;
}

.shop-header:not(.light-header) .search-box-icon {
  color: rgba(255, 255, 255, 0.6);
}

.search-native-input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--mall-header-text, #303133);
  width: 160px;
  font-family: inherit;
}

.search-native-input::placeholder {
  color: #B5A99A;
}

.shop-header:not(.light-header) .search-native-input {
  color: white;
}

.shop-header:not(.light-header) .search-native-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

/* Icon 按鈕 */
.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  color: var(--mall-header-text, #303133);
  transition: background 0.2s, color 0.2s, transform 0.15s;
  position: relative;
  flex-shrink: 0;
}

.header-icon-btn:hover {
  background: rgba(74, 107, 124, 0.07);
  transform: translateY(-1px);
}

.shop-header:not(.light-header) .header-icon-btn {
  color: rgba(255, 255, 255, 0.9);
}

.shop-header:not(.light-header) .header-icon-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 購物車徽章 */
.cart-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  background: var(--mall-accent, #A5635C);
  color: white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  line-height: 1;
  pointer-events: none;
}

/* 使用者頭像 */
.user-avatar {
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: var(--mall-header-text, #303133);
  background: rgba(0, 0, 0, 0.04);
}

.shop-header:not(.light-header) .user-avatar {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar:hover {
  background: rgba(0, 0, 0, 0.08);
  transform: scale(1.05);
}

.shop-header:not(.light-header) .user-avatar:hover {
  background: rgba(255, 255, 255, 0.18);
}

.user-avatar.has-avatar {
  padding: 0;
  overflow: hidden;
  background: transparent;
}

.user-avatar .avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

/* Auth 按鈕 */
.auth-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.auth-login-btn {
  background: none;
  border: none;
  color: var(--mall-primary, #4A6B7C);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  font-family: inherit;
}

.auth-login-btn:hover {
  background: rgba(74, 107, 124, 0.07);
}

.shop-header:not(.light-header) .auth-login-btn {
  color: rgba(255, 255, 255, 0.9);
}

.shop-header:not(.light-header) .auth-login-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.auth-register-btn {
  background: var(--mall-primary, #4A6B7C);
  color: white;
  border: none;
  font-size: 13px;
  font-weight: 600;
  padding: 7px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: filter 0.2s, transform 0.15s;
  font-family: inherit;
  letter-spacing: 0.3px;
}

.auth-register-btn:hover {
  filter: brightness(0.92);
  transform: translateY(-1px);
}

/* 漢堡選單按鈕（手機） */
.mobile-menu-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  color: var(--mall-header-text, #303133);
  transition: background 0.2s;
}

.mobile-menu-btn:hover {
  background: rgba(0, 0, 0, 0.06);
}

.shop-header:not(.light-header) .mobile-menu-btn {
  color: white;
}

.shop-header:not(.light-header) .mobile-menu-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* === 手機側滑抽屜 === */
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  z-index: 2000;
}

.mobile-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: min(320px, 85vw);
  background: var(--mall-card-bg, #FDFCFA);
  z-index: 2001;
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--mall-border-color, #EDE8E2);
}

.drawer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-logo-img {
  height: 28px;
}

.drawer-logo span {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--mall-header-text, #3D2B1F);
}

.drawer-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  color: #9A8B7D;
  transition: background 0.2s, color 0.2s;
}

.drawer-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #3D2B1F;
}

.drawer-search {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 16px 20px;
  padding: 0 14px;
  height: 42px;
  background: var(--mall-body-bg, #F5F2EF);
  border: 1px solid var(--mall-border-color, #EDE8E2);
  border-radius: 12px;
  transition: border-color 0.2s;
}

.drawer-search:focus-within {
  border-color: var(--mall-primary, #4A6B7C);
}

.drawer-search svg {
  color: #9A8B7D;
  flex-shrink: 0;
}

.drawer-search input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: var(--mall-header-text, #3D2B1F);
  flex: 1;
  font-family: inherit;
}

.drawer-search input::placeholder {
  color: #B5A99A;
}

.drawer-nav {
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.drawer-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--mall-header-text, #3D2B1F);
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s;
}

.drawer-nav-item svg {
  color: #9A8B7D;
  flex-shrink: 0;
}

.drawer-nav-item:hover,
.drawer-nav-item.router-link-active {
  background: rgba(74, 107, 124, 0.07);
}

.drawer-nav-item.router-link-active {
  color: var(--mall-primary, #4A6B7C);
  font-weight: 600;
}

.drawer-nav-item.router-link-active svg {
  color: var(--mall-primary, #4A6B7C);
}

.drawer-badge {
  margin-left: auto;
  min-width: 20px;
  height: 20px;
  background: var(--mall-accent, #A5635C);
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}

.drawer-categories {
  padding: 8px 20px 12px;
}

.drawer-section-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #9A8B7D;
  margin-bottom: 10px;
}

.drawer-category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.drawer-category-chip {
  padding: 6px 14px;
  background: var(--mall-body-bg, #F5F2EF);
  border: 1px solid var(--mall-border-color, #EDE8E2);
  border-radius: 20px;
  font-size: 13px;
  color: var(--mall-header-text, #3D2B1F);
  text-decoration: none;
  transition: background 0.2s, border-color 0.2s;
}

.drawer-category-chip:hover {
  background: rgba(74, 107, 124, 0.07);
  border-color: var(--mall-primary, #4A6B7C);
  color: var(--mall-primary, #4A6B7C);
}

.drawer-footer {
  margin-top: auto;
  padding: 16px 20px 24px;
  border-top: 1px solid var(--mall-border-color, #EDE8E2);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.drawer-member-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: transparent;
  border: 1px solid var(--mall-border-color, #EDE8E2);
  border-radius: 12px;
  cursor: pointer;
  color: var(--mall-header-text, #3D2B1F);
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s;
  font-family: inherit;
}

.drawer-member-btn:hover {
  background: rgba(74, 107, 124, 0.05);
}

.drawer-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(74, 107, 124, 0.08);
  color: #9A8B7D;
  overflow: hidden;
}

.drawer-avatar.has-avatar {
  background: transparent;
}

.drawer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.drawer-logout-btn {
  background: transparent;
  border: 1px solid rgba(165, 99, 92, 0.2);
  color: #A5635C;
  padding: 10px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  font-family: inherit;
}

.drawer-logout-btn:hover {
  background: rgba(165, 99, 92, 0.06);
}

.drawer-login-btn {
  background: var(--mall-primary, #4A6B7C);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.2s;
  font-family: inherit;
}

.drawer-login-btn:hover {
  filter: brightness(0.92);
}

.drawer-register-btn {
  background: transparent;
  border: 1px solid var(--mall-border-color, #EDE8E2);
  color: var(--mall-header-text, #3D2B1F);
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  font-family: inherit;
}

.drawer-register-btn:hover {
  background: rgba(0, 0, 0, 0.03);
}

/* 抽屜過渡動畫 */
.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.3s ease;
}

.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}

.drawer-slide-enter-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.drawer-slide-leave-active {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.6, 1);
}

.drawer-slide-enter-from,
.drawer-slide-leave-to {
  transform: translateX(100%);
}

/* === 主題選擇器 === */
.theme-picker {
  padding: 8px 0;
}

.theme-picker-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding: 0 4px;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.theme-item {
  cursor: pointer;
  text-align: center;
  padding: 8px 4px;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.theme-item:hover {
  background: #f5f5f5;
}

.theme-item.active {
  border-color: var(--mall-primary, #4A6B7C);
  background: #f5f2ef;
}

.theme-preview {
  width: 100%;
  height: 28px;
  border-radius: 6px;
  margin-bottom: 4px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.theme-name {
  font-size: 11px;
  color: #606266;
}

/* === 主要內容 === */
.shop-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 20px;
}

/* 首頁滿版佈局（兩側保留微量呼吸空間） */
.shop-main.is-fullwidth {
  max-width: 100%;
  padding: 0 clamp(16px, 2vw, 40px);
}

/* === Footer === */
.shop-footer {
  background: var(--mall-footer-bg, #3D3D3D);
  color: #ecf0f1;
  padding: 48px 20px 24px;
  margin-top: auto;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 48px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.footer-logo-img {
  height: 32px;
  filter: brightness(0) invert(1);
}

.footer-logo span {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: 2px;
}

.footer-about p {
  font-size: 14px;
  color: #bdc3c7;
  line-height: 1.8;
}

.footer-section h4 {
  margin-bottom: 20px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  position: relative;
  padding-bottom: 12px;
  letter-spacing: 1px;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 32px;
  height: 2px;
  background: var(--mall-accent, #A5635C);
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin: 10px 0;
  font-size: 13px;
  color: #bdc3c7;
  line-height: 1.6;
}

.footer-links a {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: white;
}

.footer-social {
  display: flex;
  gap: 10px;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #bdc3c7;
  text-decoration: none;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s;
}

.social-link:hover {
  background: var(--mall-accent, #A5635C);
  color: white;
}

.footer-bottom {
  max-width: 1400px;
  margin: 32px auto 0;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
  font-size: 13px;
  color: #95a5a6;
}

/* === 購物車彈跳動畫（商品飛入後觸發） === */
:global(.cart-bounce) {
  animation: cartBounce 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) !important;
}

@keyframes cartBounce {
  0%   { transform: scale(1); }
  20%  { transform: scale(1.3); }
  40%  { transform: scale(0.9); }
  60%  { transform: scale(1.15); }
  80%  { transform: scale(0.97); }
  100% { transform: scale(1); }
}

/* === 過渡動畫 === */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* === RWD === */
@media (max-width: 1024px) {
  .search-native-input {
    width: 120px;
  }

  .nav-menu {
    gap: 2px;
  }

  .nav-item {
    padding: 7px 10px;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .shop-header {
    padding: 0 16px;
  }

  .header-container {
    height: 56px;
    gap: 8px;
  }

  .logo-text {
    font-size: 16px;
  }

  .logo-img {
    height: 28px;
  }

  .nav-menu {
    display: none;
  }

  .search-box {
    display: none;
  }

  .theme-toggle {
    display: none !important;
  }

  .auth-buttons {
    display: none;
  }

  .user-menu {
    display: none;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .footer-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .announcement-bar {
    font-size: 12px;
    padding: 6px 0;
  }
}

@media (min-width: 769px) {
  .mobile-menu-btn {
    display: none !important;
  }
}
</style>

<!-- 商城提示框全域樣式覆寫（不可 scoped，因 ElMessage/ElMessageBox 掛載在 body） -->
<style>
/* ============================================================
   ElMessage — 商城友善提示條
   從冷硬的系統通知 → 溫暖的店員輕語
   ============================================================ */

/* 只在商城頁面生效：ShopLayout 存在時覆寫 */
.shop-layout ~ .el-message,
body:has(.shop-layout) .el-message {
  --shop-msg-radius: 14px;
  --shop-msg-shadow: 0 8px 32px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);

  border-radius: var(--shop-msg-radius) !important;
  border: none !important;
  box-shadow: var(--shop-msg-shadow) !important;
  padding: 14px 22px !important;
  min-width: 180px !important;
  gap: 10px !important;
  top: 28px !important;
  font-size: 14px !important;
  animation: shopMsgSlideIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

/* 成功 — 溫暖的薄荷綠 */
body:has(.shop-layout) .el-message--success {
  background: linear-gradient(135deg, #ecfdf5, #f0fdf4) !important;
  color: #166534 !important;
}

body:has(.shop-layout) .el-message--success .el-message__icon {
  color: #22c55e !important;
  font-size: 20px !important;
}

body:has(.shop-layout) .el-message--success .el-message__content {
  color: #166534 !important;
  font-weight: 500 !important;
}

/* 警告 — 柔和的暖琥珀 */
body:has(.shop-layout) .el-message--warning {
  background: linear-gradient(135deg, #fffbeb, #fef3c7) !important;
  color: #92400e !important;
}

body:has(.shop-layout) .el-message--warning .el-message__icon {
  color: #f59e0b !important;
  font-size: 20px !important;
}

body:has(.shop-layout) .el-message--warning .el-message__content {
  color: #92400e !important;
  font-weight: 500 !important;
}

/* 錯誤 — 溫柔的玫瑰紅（非刺眼的警報紅） */
body:has(.shop-layout) .el-message--error {
  background: linear-gradient(135deg, #fff1f2, #ffe4e6) !important;
  color: #9f1239 !important;
}

body:has(.shop-layout) .el-message--error .el-message__icon {
  color: #f43f5e !important;
  font-size: 20px !important;
}

body:has(.shop-layout) .el-message--error .el-message__content {
  color: #9f1239 !important;
  font-weight: 500 !important;
}

/* 資訊 — 柔和的天空藍 */
body:has(.shop-layout) .el-message--info {
  background: linear-gradient(135deg, #eff6ff, #dbeafe) !important;
  color: #1e40af !important;
}

body:has(.shop-layout) .el-message--info .el-message__icon {
  color: #3b82f6 !important;
  font-size: 20px !important;
}

body:has(.shop-layout) .el-message--info .el-message__content {
  color: #1e40af !important;
  font-weight: 500 !important;
}

/* 關閉按鈕 */
body:has(.shop-layout) .el-message .el-message__closeBtn {
  font-size: 14px !important;
  opacity: 0.4;
  transition: opacity 0.2s;
}

body:has(.shop-layout) .el-message .el-message__closeBtn:hover {
  opacity: 0.8;
}

@keyframes shopMsgSlideIn {
  from {
    opacity: 0;
    transform: translateY(-16px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ============================================================
   ElMessageBox — 商城友善確認對話框
   從冷硬的系統確認 → 溫暖的對話
   ============================================================ */
body:has(.shop-layout) .el-overlay-message-box .el-message-box {
  --shop-dialog-radius: 20px;

  border-radius: var(--shop-dialog-radius) !important;
  border: none !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12), 0 4px 16px rgba(0, 0, 0, 0.06) !important;
  padding: 32px 28px 24px !important;
  max-width: 380px !important;
  animation: shopDialogIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

/* 遮罩層 */
body:has(.shop-layout) .el-overlay-message-box {
  background: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(4px) !important;
  -webkit-backdrop-filter: blur(4px) !important;
}

/* 標題 */
body:has(.shop-layout) .el-message-box__header {
  padding: 0 0 16px 0 !important;
}

body:has(.shop-layout) .el-message-box__title {
  font-size: 18px !important;
  font-weight: 700 !important;
  color: #1f2937 !important;
  letter-spacing: 0.3px !important;
}

/* 隱藏預設 icon 容器 — 去掉冷硬的系統感 */
body:has(.shop-layout) .el-message-box__status {
  display: none !important;
}

/* 內容文字 */
body:has(.shop-layout) .el-message-box__content {
  padding: 0 0 24px 0 !important;
}

body:has(.shop-layout) .el-message-box__message p {
  font-size: 15px !important;
  line-height: 1.6 !important;
  color: #4b5563 !important;
}

/* 按鈕區 */
body:has(.shop-layout) .el-message-box__btns {
  padding: 0 !important;
  display: flex !important;
  gap: 10px !important;
}

/* 取消按鈕 — 輕柔的灰色外框 */
body:has(.shop-layout) .el-message-box__btns .el-button--default {
  flex: 1 !important;
  height: 44px !important;
  border-radius: 12px !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  border: 1.5px solid #e5e7eb !important;
  background: white !important;
  color: #6b7280 !important;
  transition: all 0.2s !important;
}

body:has(.shop-layout) .el-message-box__btns .el-button--default:hover {
  border-color: #d1d5db !important;
  background: #f9fafb !important;
  color: #374151 !important;
}

/* 確認按鈕 — 商城主色調 */
body:has(.shop-layout) .el-message-box__btns .el-button--primary {
  flex: 1 !important;
  height: 44px !important;
  border-radius: 12px !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  border: none !important;
  background: var(--mall-primary, #4A6B7C) !important;
  color: white !important;
  letter-spacing: 0.5px !important;
  transition: all 0.2s !important;
}

body:has(.shop-layout) .el-message-box__btns .el-button--primary:hover {
  filter: brightness(0.9) !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(74, 107, 124, 0.3) !important;
}

/* 關閉按鈕 */
body:has(.shop-layout) .el-message-box__headerbtn {
  top: 20px !important;
  right: 20px !important;
  width: 32px !important;
  height: 32px !important;
  border-radius: 50% !important;
  transition: background 0.2s !important;
}

body:has(.shop-layout) .el-message-box__headerbtn:hover {
  background: #f3f4f6 !important;
}

body:has(.shop-layout) .el-message-box__headerbtn .el-message-box__close {
  color: #9ca3af !important;
  font-size: 14px !important;
}

@keyframes shopDialogIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* ============================================================
   ElDropdown — 商城頭像下拉選單
   從冷硬的系統下拉 → 溫暖的個人選單卡片
   ============================================================ */
body:has(.shop-layout) .el-dropdown__popper.el-popper {
  --el-dropdown-menuItem-hover-fill: rgba(74, 107, 124, 0.06);
  --el-dropdown-menuItem-hover-color: #3D2B1F;

  border: 1px solid #EDE8E2 !important;
  border-radius: 16px !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  padding: 8px !important;
  min-width: 180px !important;
  overflow: hidden !important;
  animation: shopDropdownIn 0.25s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

/* 隱藏 popper 箭頭 */
body:has(.shop-layout) .el-dropdown__popper .el-popper__arrow {
  display: none !important;
}

/* 選單項目 */
body:has(.shop-layout) .el-dropdown-menu__item {
  padding: 11px 16px !important;
  border-radius: 10px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #5A4A3E !important;
  transition: all 0.2s ease !important;
  line-height: 1.5 !important;
  margin: 2px 0 !important;
}

body:has(.shop-layout) .el-dropdown-menu__item:hover,
body:has(.shop-layout) .el-dropdown-menu__item:focus {
  background: rgba(74, 107, 124, 0.06) !important;
  color: #4A6B7C !important;
}

/* 分隔線 — 用溫暖的邊框取代冷灰 */
body:has(.shop-layout) .el-dropdown-menu__item--divided {
  border-top-color: #F0EBE5 !important;
  margin-top: 6px !important;
  padding-top: 13px !important;
}

/* 選單 icon */
body:has(.shop-layout) .el-dropdown-menu__item .el-icon {
  margin-right: 8px !important;
  font-size: 16px !important;
  color: #9A8B7D !important;
  transition: color 0.2s ease !important;
}

body:has(.shop-layout) .el-dropdown-menu__item:hover .el-icon,
body:has(.shop-layout) .el-dropdown-menu__item:focus .el-icon {
  color: #4A6B7C !important;
}

/* 登出項目 — 溫暖的退出感 */
body:has(.shop-layout) .el-dropdown-menu__item--divided:hover,
body:has(.shop-layout) .el-dropdown-menu__item--divided:focus {
  background: rgba(165, 99, 92, 0.06) !important;
  color: #A5635C !important;
}

body:has(.shop-layout) .el-dropdown-menu__item--divided:hover .el-icon,
body:has(.shop-layout) .el-dropdown-menu__item--divided:focus .el-icon {
  color: #A5635C !important;
}

@keyframes shopDropdownIn {
  from {
    opacity: 0;
    transform: translateY(-6px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* === RWD：手機版提示框微調 === */
@media (max-width: 768px) {
  body:has(.shop-layout) .el-message {
    min-width: auto !important;
    max-width: calc(100vw - 32px) !important;
    padding: 12px 18px !important;
    font-size: 13px !important;
    border-radius: 12px !important;
  }

  body:has(.shop-layout) .el-overlay-message-box .el-message-box {
    max-width: calc(100vw - 40px) !important;
    padding: 24px 20px 20px !important;
    border-radius: 16px !important;
    margin: 0 20px !important;
  }

  body:has(.shop-layout) .el-message-box__title {
    font-size: 16px !important;
  }

  body:has(.shop-layout) .el-message-box__message p {
    font-size: 14px !important;
  }

  body:has(.shop-layout) .el-message-box__btns .el-button--default,
  body:has(.shop-layout) .el-message-box__btns .el-button--primary {
    height: 42px !important;
    border-radius: 10px !important;
  }
}
</style>
