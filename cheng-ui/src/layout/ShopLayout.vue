<template>
  <div class="shop-layout">
    <!-- 頂部公告欄 -->
    <div v-if="showAnnouncement" class="announcement-bar">
      <div class="announcement-content">
        <span>全館滿 $999 免運費 | 新會員首單享 9 折優惠</span>
      </div>
      <button class="announcement-close" @click="closeAnnouncement">
        <el-icon :size="14"><Close /></el-icon>
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
            <el-icon :size="12" class="caret-icon"><ArrowDown /></el-icon>
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
            <el-input
              v-model="searchKeyword"
              placeholder="搜尋商品..."
              @keyup.enter="handleSearch"
              style="width: 200px"
              class="search-input"
            />
            <el-button class="search-btn" circle @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </div>
          <div class="cart-icon" @click="goCart">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
          </div>

          <!-- 登入/使用者入口 -->
          <div v-if="isLoggedIn" class="user-menu">
            <el-dropdown trigger="click">
              <div class="user-avatar" :class="{ 'has-avatar': memberAvatar }">
                <img v-if="memberAvatar" :src="memberAvatar" alt="頭像" class="avatar-img" />
                <el-icon v-else :size="20"><User /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goMember">會員中心</el-dropdown-item>
                  <el-dropdown-item @click="goOrders">我的訂單</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">登出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else class="auth-buttons">
            <el-button text class="login-btn" @click="goLogin">登入</el-button>
            <el-button type="primary" round size="small" @click="goRegister">註冊</el-button>
          </div>

          <el-popover :visible="showThemePicker" placement="bottom" :width="320" trigger="click">
            <template #reference>
              <div class="theme-toggle" @click="showThemePicker = !showThemePicker">
                <el-icon :size="20"><Brush /></el-icon>
              </div>
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
        </div>
      </div>
    </header>

    <!-- 主要內容區 -->
    <main class="shop-main">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShoppingCart, Brush, Search, User, Close, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMallThemeStore, PRESET_THEMES } from '@/store/modules/mallTheme'
import { useCartStore } from '@/store/modules/cart'
import { getMemberToken } from '@/utils/memberAuth'
import useMemberStore from '@/store/modules/member'
import { listCategories } from '@/api/shop/front'

const router = useRouter()
const route = useRoute()
const themeStore = useMallThemeStore()
const cartStore = useCartStore()
const memberStore = useMemberStore()
const searchKeyword = ref('')
const showThemePicker = ref(false)
const showAnnouncement = ref(true)
const showCategoryDropdown = ref(false)
const categories = ref([])

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

function closeAnnouncement() {
  showAnnouncement.value = false
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

onMounted(() => {
  themeStore.init()
  cartStore.init()
  loadCategories()
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

/* === 公告欄 === */
.announcement-bar {
  background: var(--mall-primary, #4A6B7C);
  color: white;
  text-align: center;
  padding: 8px 40px;
  font-size: 13px;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1001;
}

.announcement-content {
  max-width: 1400px;
  margin: 0 auto;
}

.announcement-close {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
}

.announcement-close:hover {
  color: white;
}

/* === Header === */
.shop-header {
  background: var(--mall-header-bg, #FFFFFF);
  color: var(--mall-header-text, #303133);
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid var(--mall-border-color, #E8E4DF);
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
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}

.logo-img {
  height: 36px;
  transition: filter 0.3s;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--mall-header-text, #303133);
}

.shop-header:not(.light-header) .logo-text {
  color: white;
}

/* === 導航 === */
.nav-menu {
  display: flex;
  gap: 36px;
  align-items: center;
}

.nav-item {
  color: var(--mall-header-text-secondary, #606266);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: color 0.3s;
  letter-spacing: 0.5px;
}

.shop-header:not(.light-header) .nav-item {
  color: rgba(255, 255, 255, 0.9);
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--mall-primary, #4A6B7C);
  transition: width 0.3s;
}

.shop-header:not(.light-header) .nav-item::after {
  background: white;
}

.nav-item:hover::after,
.nav-item.router-link-active::after {
  width: 100%;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: var(--mall-primary, #4A6B7C);
}

.shop-header:not(.light-header) .nav-item:hover,
.shop-header:not(.light-header) .nav-item.router-link-active {
  color: white;
}

/* === 分類下拉 === */
.category-nav {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.caret-icon {
  transition: transform 0.3s;
}

.category-nav:hover .caret-icon {
  transform: rotate(180deg);
}

.category-dropdown {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  padding-top: 12px;
  z-index: 100;
}

.category-dropdown-inner {
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid #f0ebe6;
  padding: 12px;
  min-width: 240px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px;
}

.category-link {
  display: flex;
  flex-direction: column;
  padding: 10px 14px;
  border-radius: 6px;
  text-decoration: none;
  transition: background 0.2s;
}

.category-link:hover {
  background: var(--mall-body-bg, #FAF8F5);
}

.category-link-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.category-link-count {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.category-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 20px;
  color: #909399;
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
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 6px;
}

.light-header .search-box :deep(.search-input .el-input__wrapper) {
  background: #f5f2ef;
  border-radius: 20px;
  box-shadow: none;
  border: 1px solid #e8e4df;
}

.shop-header:not(.light-header) .search-box :deep(.search-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
}

.light-header .search-btn {
  background: #f5f2ef !important;
  border: 1px solid #e8e4df !important;
  color: #606266 !important;
}

.shop-header:not(.light-header) .search-btn {
  background: rgba(255, 255, 255, 0.95) !important;
  border: none !important;
  color: #606266 !important;
}

.search-btn {
  transition: all 0.3s;
}

.search-btn:hover {
  transform: scale(1.05);
}

.cart-icon,
.theme-toggle,
.user-avatar {
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--mall-header-text, #303133);
}

.shop-header:not(.light-header) .cart-icon,
.shop-header:not(.light-header) .theme-toggle,
.shop-header:not(.light-header) .user-avatar {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.light-header .cart-icon,
.light-header .theme-toggle,
.light-header .user-avatar {
  background: rgba(0, 0, 0, 0.04);
}

.cart-icon:hover,
.theme-toggle:hover,
.user-avatar:hover {
  background: rgba(0, 0, 0, 0.08);
  transform: scale(1.05);
}

.shop-header:not(.light-header) .cart-icon:hover,
.shop-header:not(.light-header) .theme-toggle:hover,
.shop-header:not(.light-header) .user-avatar:hover {
  background: rgba(255, 255, 255, 0.2);
}

.user-avatar.has-avatar {
  padding: 0;
  overflow: hidden;
}

.user-avatar .avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.light-header .login-btn {
  color: var(--mall-primary, #4A6B7C) !important;
  font-weight: 500;
}

.shop-header:not(.light-header) .login-btn {
  color: white !important;
  font-weight: 500;
}

.login-btn:hover {
  opacity: 0.8;
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
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }

  .search-box {
    display: none;
  }

  .footer-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .announcement-bar {
    font-size: 12px;
    padding: 6px 32px;
  }
}
</style>
