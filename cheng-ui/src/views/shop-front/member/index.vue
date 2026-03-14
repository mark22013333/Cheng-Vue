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
          <div class="dash-header">
            <h2>會員中心</h2>
            <p>歡迎回來，{{ userName }}</p>
          </div>

          <!-- 訂單統計 -->
          <div class="stats-cards">
            <div
              v-for="stat in statCards"
              :key="stat.key"
              class="stat-card"
              @click="goTo('orders', stat.key)"
            >
              <div class="stat-icon" :class="stat.key">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ orderStats[stat.key] }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </div>

          <!-- 快捷入口 -->
          <div class="quick-actions">
            <h3>快捷功能</h3>
            <div class="action-grid">
              <div class="action-item" @click="goTo('address')">
                <el-icon><Location /></el-icon>
                <span>管理地址</span>
              </div>
              <div class="action-item" @click="goTo('profile')">
                <el-icon><User /></el-icon>
                <span>個人資料</span>
              </div>
              <div class="action-item" @click="$router.push('/cart')">
                <el-icon><ShoppingCart /></el-icon>
                <span>購物車</span>
              </div>
              <div class="action-item" @click="$router.push('/products')">
                <el-icon><Goods /></el-icon>
                <span>繼續購物</span>
              </div>
            </div>
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
.dash-header {
  margin-bottom: 28px;
}

.dash-header h2 {
  margin: 0;
  font-family: 'Noto Serif TC', 'Noto Sans Symbols 2', system-ui, serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 22px;
  font-weight: 600;
  color: #3D2B1F;
}

.dash-header p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #A09585;
}

/* 統計卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 32px;
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
}

.stat-card:hover {
  border-color: #D8CFC5;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-icon.pending {
  background: #FEF3F0;
  color: #D4594B;
}

.stat-icon.paid {
  background: #FEF6EC;
  color: #C4882E;
}

.stat-icon.shipped {
  background: #EFF6FC;
  color: #4A6B7C;
}

.stat-icon.completed {
  background: #F0F7EB;
  color: #5F9E3C;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #3D2B1F;
  line-height: 1.2;
}

.stat-label {
  font-size: 12.5px;
  color: #A09585;
  margin-top: 2px;
}

/* 快捷功能 */
.quick-actions h3 {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 700;
  color: #3D2B1F;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 22px 14px;
  background: #FDFCFA;
  border: 1px solid #F0EBE5;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #7A6B5D;
}

.action-item:hover {
  border-color: #4A6B7C;
  color: #4A6B7C;
  background: rgba(74, 107, 124, 0.03);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(74, 107, 124, 0.08);
}

.action-item .el-icon {
  font-size: 26px;
}

.action-item span {
  font-size: 13px;
  font-weight: 500;
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

  .stats-cards,
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
