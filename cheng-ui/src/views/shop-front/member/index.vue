<template>
  <div class="member-center">
    <div class="member-container">
      <!-- 側邊欄 -->
      <aside class="member-sidebar">
        <div class="user-info">
          <el-avatar :size="64" :src="userAvatar" />
          <div class="user-name">{{ userName }}</div>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="member-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的訂單</span>
          </el-menu-item>
          <el-menu-item index="address">
            <el-icon><Location /></el-icon>
            <span>收貨地址</span>
          </el-menu-item>
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>個人資料</span>
          </el-menu-item>
        </el-menu>
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
          <h2>會員中心</h2>

          <!-- 訂單統計 -->
          <div class="stats-cards">
            <div class="stat-card" @click="goTo('orders', 'pending')">
              <div class="stat-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ orderStats.pending }}</div>
                <div class="stat-label">待付款</div>
              </div>
            </div>
            <div class="stat-card" @click="goTo('orders', 'paid')">
              <div class="stat-icon paid">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ orderStats.paid }}</div>
                <div class="stat-label">待出貨</div>
              </div>
            </div>
            <div class="stat-card" @click="goTo('orders', 'shipped')">
              <div class="stat-icon shipped">
                <el-icon><Van /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ orderStats.shipped }}</div>
                <div class="stat-label">待收貨</div>
              </div>
            </div>
            <div class="stat-card" @click="goTo('orders', 'completed')">
              <div class="stat-icon completed">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ orderStats.completed }}</div>
                <div class="stat-label">已完成</div>
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
              <div class="action-item" @click="$router.push('/mall/cart')">
                <el-icon><ShoppingCart /></el-icon>
                <span>購物車</span>
              </div>
              <div class="action-item" @click="$router.push('/mall/products')">
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
import { ref, computed, onMounted } from 'vue'
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
  Goods
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()

const userName = computed(() => memberStore.nickname || '會員')
const userAvatar = computed(() => memberStore.avatar || '')

const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/orders')) return 'orders'
  if (path.includes('/address')) return 'address'
  if (path.includes('/profile')) return 'profile'
  return ''
})

const isIndexPage = computed(() => {
  return route.path === '/mall/member' || route.path === '/mall/member/'
})

// 訂單統計（模擬數據，之後接 API）
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
  router.push(`/mall/member/${index}`)
}

function goTo(page, status) {
  if (status) {
    router.push({ path: `/mall/member/${page}`, query: { status } })
  } else {
    router.push(`/mall/member/${page}`)
  }
}

onMounted(() => {
  loadOrderStats()
})
</script>

<style scoped>
.member-center {
  min-height: calc(100vh - 120px);
  background: #f5f7fa;
  padding: 24px;
}

.member-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 24px;
}

/* 側邊欄 */
.member-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 24px 0;
  height: fit-content;
}

.user-info {
  text-align: center;
  padding: 0 24px 24px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.user-name {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.member-menu {
  border-right: none;
}

.member-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
}

.member-menu .el-menu-item.is-active {
  background: var(--mall-primary-light, #ecf5ff);
  color: var(--mall-primary, #409eff);
}

/* 主內容區 */
.member-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  min-height: 500px;
}

.member-dashboard h2 {
  margin: 0 0 24px;
  font-size: 20px;
  color: #303133;
}

/* 統計卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.pending {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-icon.paid {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-icon.shipped {
  background: #ecf5ff;
  color: #409eff;
}

.stat-icon.completed {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 快捷功能 */
.quick-actions h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: var(--mall-primary-light, #ecf5ff);
  color: var(--mall-primary, #409eff);
}

.action-item .el-icon {
  font-size: 28px;
}

.action-item span {
  font-size: 14px;
}

/* 響應式 */
@media (max-width: 768px) {
  .member-container {
    flex-direction: column;
  }

  .member-sidebar {
    width: 100%;
  }

  .stats-cards,
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
