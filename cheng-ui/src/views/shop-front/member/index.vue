<template>
  <div class="member-center">
    <div class="member-container">
      <!-- 側邊欄 -->
      <aside class="member-sidebar">
        <div class="sidebar-user">
          <div class="sidebar-avatar-wrap">
            <el-avatar :size="56" :src="userAvatar" class="sidebar-avatar" />
          </div>
          <div class="sidebar-name">{{ userName }}</div>
          <div class="sidebar-badge">一般會員</div>
        </div>

        <nav class="sidebar-nav">
          <button
            v-for="item in menuItems"
            :key="item.key"
            class="nav-link"
            :class="{ active: activeMenu === item.key }"
            @click="handleMenuSelect(item.key)"
          >
            <span class="nav-link-icon">
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
            </span>
            <span class="nav-link-text">{{ item.label }}</span>
            <span class="nav-link-arrow">
              <el-icon :size="12"><ArrowRight /></el-icon>
            </span>
          </button>
        </nav>
      </aside>

      <!-- 主內容區 -->
      <main class="member-content">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>

        <!-- 預設首頁內容 -->
        <div v-if="isIndexPage" class="member-dashboard">
          <!-- 歡迎區 -->
          <div class="welcome-hero">
            <div class="welcome-text">
              <p class="welcome-greeting">{{ greetingText }}</p>
              <h2 class="welcome-name">{{ userName }}</h2>
              <p class="welcome-sub">很高興見到你，看看今天有什麼新鮮事</p>
            </div>
            <div class="welcome-deco">
              <span class="deco-circle c1" />
              <span class="deco-circle c2" />
              <span class="deco-circle c3" />
            </div>
          </div>

          <!-- 訂單統計 -->
          <div class="section-label">
            <span class="section-dot" />
            訂單概況
          </div>
          <div class="stats-cards">
            <button
              v-for="stat in statCards"
              :key="stat.key"
              class="stat-card"
              :class="stat.key"
              @click="goTo('orders', stat.key)"
            >
              <div class="stat-icon-wrap">
                <el-icon :size="22"><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ orderStats[stat.key] }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </button>
          </div>

          <!-- 快捷入口 -->
          <div class="section-label">
            <span class="section-dot" />
            快捷功能
          </div>
          <div class="action-grid">
            <button class="action-item" @click="goTo('address')">
              <div class="action-icon">
                <el-icon :size="22"><Location /></el-icon>
              </div>
              <span class="action-text">管理地址</span>
              <span class="action-arrow">
                <el-icon :size="12"><ArrowRight /></el-icon>
              </span>
            </button>
            <button class="action-item" @click="goTo('profile')">
              <div class="action-icon">
                <el-icon :size="22"><User /></el-icon>
              </div>
              <span class="action-text">個人資料</span>
              <span class="action-arrow">
                <el-icon :size="12"><ArrowRight /></el-icon>
              </span>
            </button>
            <button class="action-item" @click="$router.push('/cart')">
              <div class="action-icon">
                <el-icon :size="22"><ShoppingCart /></el-icon>
              </div>
              <span class="action-text">購物車</span>
              <span class="action-arrow">
                <el-icon :size="12"><ArrowRight /></el-icon>
              </span>
            </button>
            <button class="action-item" @click="$router.push('/products')">
              <div class="action-icon">
                <el-icon :size="22"><Goods /></el-icon>
              </div>
              <span class="action-text">繼續購物</span>
              <span class="action-arrow">
                <el-icon :size="12"><ArrowRight /></el-icon>
              </span>
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import useMemberStore from '@/store/modules/member'
import { getOrderStats } from '@/api/shop/order'
import {
  Document,
  Location,
  User,
  Clock,
  Box,
  Van,
  CircleCheck,
  ShoppingCart,
  Goods,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()

const userName = computed(() => memberStore.nickname || '會員')
const userAvatar = computed(() => memberStore.avatar || '')

// 時間感知問候
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早安'
  if (hour < 14) return '午安'
  if (hour < 18) return '午後好'
  return '晚安'
})

const menuItems = [
  { key: 'orders', label: '我的訂單', icon: markRaw(Document) },
  { key: 'address', label: '收貨地址', icon: markRaw(Location) },
  { key: 'profile', label: '個人資料', icon: markRaw(User) }
]

const statCards = [
  { key: 'pending', label: '待付款', icon: markRaw(Clock) },
  { key: 'paid', label: '待出貨', icon: markRaw(Box) },
  { key: 'shipped', label: '待收貨', icon: markRaw(Van) },
  { key: 'completed', label: '已完成', icon: markRaw(CircleCheck) }
]

const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/orders')) return 'orders'
  if (path.includes('/address')) return 'address'
  if (path.includes('/profile')) return 'profile'
  return ''
})

const isIndexPage = computed(() => {
  return route.path === '/member' || route.path === '/member/'
})

const orderStats = ref({
  pending: 0,
  paid: 0,
  shipped: 0,
  completed: 0
})

async function loadOrderStats() {
  try {
    const res = await getOrderStats()
    const stats = res.data || {}
    orderStats.value = {
      pending: stats.PENDING || 0,
      paid: stats.PAID || 0,
      shipped: stats.SHIPPED || 0,
      completed: stats.COMPLETED || 0
    }
  } catch (error) {
    console.error('載入訂單統計失敗', error)
  }
}

function handleMenuSelect(index) {
  router.push(`/member/${index}`)
}

function goTo(page, status) {
  if (status) {
    router.push({ path: `/member/${page}`, query: { status } })
  } else {
    router.push(`/member/${page}`)
  }
}

onMounted(() => {
  loadOrderStats()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500;600;700&family=Noto+Serif+TC:wght@500;600;700&family=Noto+Sans+Symbols+2&family=Noto+Color+Emoji&display=swap');

.member-center {
  min-height: calc(100vh - 120px);
  padding: 0;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}

.member-container {
  max-width: 1060px;
  margin: 0 auto;
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

/* ====== 側邊欄 ====== */
.member-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #FDFCFA;
  border: 1px solid #EDE8E2;
  border-radius: 18px;
  overflow: hidden;
  position: sticky;
  top: 100px;
}

.sidebar-user {
  text-align: center;
  padding: 28px 20px 22px;
  background: linear-gradient(160deg, rgba(74, 107, 124, 0.06) 0%, rgba(196, 168, 130, 0.06) 100%);
  border-bottom: 1px solid #F0EBE5;
}

.sidebar-avatar-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  padding: 3px;
  border-radius: 50%;
  aspect-ratio: 1 / 1;
  background: conic-gradient(from 45deg, #4A6B7C, #7BA3B5, #C4A882, #A5635C, #4A6B7C);
}

.sidebar-avatar-wrap :deep(.sidebar-avatar) {
  width: 100% !important;
  height: 100% !important;
  border: 2.5px solid #FDFCFA;
}

.sidebar-name {
  margin-top: 14px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 16px;
  font-weight: 600;
  color: #3D2B1F;
}

.sidebar-badge {
  display: inline-block;
  margin-top: 6px;
  padding: 2px 12px;
  border-radius: 99px;
  background: rgba(74, 107, 124, 0.08);
  border: 1px solid rgba(74, 107, 124, 0.15);
  font-size: 11px;
  font-weight: 600;
  color: #4A6B7C;
  letter-spacing: 0.5px;
}

/* 導航 */
.sidebar-nav {
  padding: 10px 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: none;
  border-radius: 12px;
  background: transparent;
  cursor: pointer;
  font-family: 'Noto Sans TC', 'Noto Sans Symbols 2', system-ui, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 14px;
  font-weight: 500;
  color: #7A6B5D;
  transition: all 0.25s ease;
  text-align: left;
}

.nav-link:hover {
  background: rgba(74, 107, 124, 0.05);
  color: #4A6B7C;
}

.nav-link.active {
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.10), rgba(90, 138, 154, 0.08));
  color: #4A6B7C;
  font-weight: 700;
}

.nav-link-icon {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: #F5F0EB;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9A8B7D;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.nav-link.active .nav-link-icon {
  background: linear-gradient(135deg, #4A6B7C, #5A8A9A);
  color: #fff;
  box-shadow: 0 2px 8px rgba(74, 107, 124, 0.2);
}

.nav-link:hover .nav-link-icon {
  color: #4A6B7C;
}

.nav-link-text {
  flex: 1;
}

.nav-link-arrow {
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.25s ease;
  color: #B0A090;
}

.nav-link:hover .nav-link-arrow,
.nav-link.active .nav-link-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* ====== 主內容區 ====== */
.member-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 1px solid #EDE8E2;
  border-radius: 18px;
  padding: 32px;
  min-height: 480px;
}

/* ====== Dashboard ====== */

/* 歡迎區 */
.welcome-hero {
  position: relative;
  padding: 28px 30px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(74, 107, 124, 0.06) 0%, rgba(196, 168, 130, 0.06) 50%, rgba(165, 99, 92, 0.04) 100%);
  border: 1px solid rgba(74, 107, 124, 0.08);
  margin-bottom: 28px;
  overflow: hidden;
}

.welcome-text {
  position: relative;
  z-index: 1;
}

.welcome-greeting {
  margin: 0 0 2px;
  font-size: 13px;
  font-weight: 500;
  color: #9A8B7D;
  letter-spacing: 0.3px;
}

.welcome-name {
  margin: 0 0 6px;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif;
  font-size: 24px;
  font-weight: 700;
  color: #3D2B1F;
  line-height: 1.3;
}

.welcome-sub {
  margin: 0;
  font-size: 13.5px;
  color: #9A8B7D;
}

/* 裝飾圓 */
.welcome-deco {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 200px;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.deco-circle.c1 {
  width: 120px;
  height: 120px;
  right: -20px;
  top: -30px;
  background: #4A6B7C;
}

.deco-circle.c2 {
  width: 80px;
  height: 80px;
  right: 40px;
  bottom: -20px;
  background: #C4A882;
}

.deco-circle.c3 {
  width: 50px;
  height: 50px;
  right: 80px;
  top: 10px;
  background: #A5635C;
}

/* 區塊標籤 */
.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  font-weight: 600;
  color: #9A8B7D;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 14px;
}

.section-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #C4A882;
}

/* 統計卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  background: #FDFCFA;
  border: 1px solid #F0EBE5;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
  font-family: inherit;
}

.stat-card:hover {
  border-color: #D8CFC5;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card.pending .stat-icon-wrap {
  background: #FEF3F0;
  color: #D4594B;
}

.stat-card.paid .stat-icon-wrap {
  background: #FEF6EC;
  color: #C4882E;
}

.stat-card.shipped .stat-icon-wrap {
  background: #EFF6FC;
  color: #4A6B7C;
}

.stat-card.completed .stat-icon-wrap {
  background: #F0F7EB;
  color: #5F9E3C;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #3D2B1F;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 12.5px;
  color: #A09585;
  margin-top: 2px;
}

/* 快捷功能 — 水平列表 */
.action-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 14px 18px;
  background: #FDFCFA;
  border: 1px solid #F0EBE5;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
  text-align: left;
}

.action-item:hover {
  border-color: rgba(74, 107, 124, 0.25);
  background: rgba(74, 107, 124, 0.02);
  box-shadow: 0 2px 12px rgba(74, 107, 124, 0.06);
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #F5F0EB;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9A8B7D;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.action-item:hover .action-icon {
  background: linear-gradient(135deg, #4A6B7C, #5A8A9A);
  color: #fff;
  box-shadow: 0 2px 8px rgba(74, 107, 124, 0.2);
}

.action-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #5A4A3E;
}

.action-arrow {
  color: #C4B5A6;
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.25s ease;
}

.action-item:hover .action-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* ====== RWD ====== */
@media (max-width: 768px) {
  .member-container {
    flex-direction: column;
  }

  .member-sidebar {
    width: 100%;
    position: static;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
    padding: 8px 10px;
    gap: 6px;
  }

  .nav-link {
    flex-direction: column;
    gap: 6px;
    min-width: fit-content;
    padding: 10px 16px;
    font-size: 12px;
    text-align: center;
  }

  .nav-link-arrow {
    display: none;
  }

  .nav-link-icon {
    width: 32px;
    height: 32px;
  }

  .sidebar-user {
    padding: 18px 20px 14px;
  }

  .member-content {
    padding: 20px 16px;
    border-radius: 16px;
  }

  .welcome-hero {
    padding: 22px 20px;
    border-radius: 14px;
  }

  .welcome-name {
    font-size: 20px;
  }

  .welcome-deco {
    display: none;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-card {
    padding: 14px 12px;
  }

  .stat-value {
    font-size: 18px;
  }

  .action-grid {
    gap: 4px;
  }

  .action-item {
    padding: 12px 14px;
    border-radius: 12px;
  }
}
</style>
