<template>
  <div class="shop-layout">
    <!-- 頂部導航 -->
    <header class="shop-header">
      <div class="header-container">
        <div class="logo" @click="goHome">
          <img src="@/assets/logo/logo.png" alt="Logo" class="logo-img" />
          <span class="logo-text">CoolApps 商城</span>
        </div>
        
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首頁</router-link>
          <router-link to="/products" class="nav-item">全部商品</router-link>
          <router-link to="/category" class="nav-item">商品分類</router-link>
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
              <el-icon :size="24"><ShoppingCart /></el-icon>
            </el-badge>
          </div>
          
          <!-- 登入/使用者入口 -->
          <div v-if="isLoggedIn" class="user-menu">
            <el-dropdown trigger="click">
              <div class="user-avatar">
                <el-icon :size="20"><User /></el-icon>
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
          
          <el-popover :visible="showThemePicker" placement="bottom" :width="280" trigger="click">
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
        <div class="footer-section">
          <h4>關於我們</h4>
          <p>CoolApps 商城提供優質商品與服務</p>
        </div>
        <div class="footer-section">
          <h4>聯絡方式</h4>
          <p>Email: service@coolapps.com</p>
          <p>電話: 02-1234-5678</p>
        </div>
        <div class="footer-section">
          <h4>客服時間</h4>
          <p>週一至週五 9:00 - 18:00</p>
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
import { ShoppingCart, Brush, Search, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMallThemeStore, PRESET_THEMES } from '@/store/modules/mallTheme'
import { useCartStore } from '@/store/modules/cart'
import { getMemberToken } from '@/utils/memberAuth'
import useMemberStore from '@/store/modules/member'

const router = useRouter()
const route = useRoute()
const themeStore = useMallThemeStore()
const cartStore = useCartStore()
const memberStore = useMemberStore()
const searchKeyword = ref('')
const showThemePicker = ref(false)

// 購物車數量（從 store 取得）
const cartCount = computed(() => cartStore.totalQuantity)

// 是否已登入
const isLoggedIn = computed(() => !!getMemberToken())

function goHome() {
  router.push('/')
}

function goCart() {
  router.push('/cart')
}

function goLogin() {
  router.push(`/login?redirect=${route.fullPath}`)
}

function goRegister() {
  router.push(`/register?redirect=${route.fullPath}`)
}

function goMember() {
  router.push('/member')
}

function goOrders() {
  router.push('/member/orders')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('確定要登出嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await memberStore.logOut()
    // 登出後重新載入訪客購物車
    cartStore.loadGuestCart()
    ElMessage.success('已登出')
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


onMounted(() => {
  themeStore.init()
  // 初始化購物車
  cartStore.init()
  if (getMemberToken()) {
    memberStore.getProfile().catch(() => {})
  }
})
</script>

<style scoped>
.shop-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--mall-body-bg, #f5f5f5);
}

.shop-header {
  background: var(--mall-header-bg, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: white;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 12px;
}

.logo-img {
  height: 40px;
  filter: brightness(0) invert(1);
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}

.nav-menu {
  display: flex;
  gap: 40px;
}

.nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: all 0.3s;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: white;
  transition: width 0.3s;
}

.nav-item:hover::after,
.nav-item.router-link-active::after {
  width: 100%;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: white;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-box :deep(.search-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
}

.search-btn {
  background: rgba(255, 255, 255, 0.95) !important;
  border: none !important;
  color: #606266 !important;
  transition: all 0.3s;
}

.search-btn:hover {
  background: rgba(255, 255, 255, 1) !important;
  transform: scale(1.05);
}

.cart-icon,
.theme-toggle,
.user-avatar {
  cursor: pointer;
  padding: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-icon:hover,
.theme-toggle:hover,
.user-avatar:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-btn {
  color: white !important;
  font-weight: 500;
}

.login-btn:hover {
  color: rgba(255, 255, 255, 0.8) !important;
}

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
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.theme-item {
  cursor: pointer;
  text-align: center;
  padding: 8px;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.theme-item:hover {
  background: #f5f5f5;
}

.theme-item.active {
  border-color: var(--mall-primary, #667eea);
  background: #f0f0ff;
}

.theme-preview {
  width: 100%;
  height: 32px;
  border-radius: 6px;
  margin-bottom: 6px;
}

.theme-name {
  font-size: 12px;
  color: #606266;
}

.shop-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 20px;
}

.shop-footer {
  background: var(--mall-footer-bg, #2c3e50);
  color: #ecf0f1;
  padding: 48px 20px 24px;
  margin-top: auto;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 48px;
}

.footer-section h4 {
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: white;
  position: relative;
  padding-bottom: 12px;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background: var(--mall-primary, #667eea);
}

.footer-section p {
  margin: 10px 0;
  font-size: 14px;
  color: #bdc3c7;
  line-height: 1.6;
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

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
}
</style>
