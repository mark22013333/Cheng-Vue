<template>
  <div class="orders-page">
    <div class="page-header">
      <h2>我的訂單</h2>
    </div>

    <!-- 狀態篩選 -->
    <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane name="PENDING">
        <template #label>
          待付款
          <el-badge v-if="stats.PENDING" :value="stats.PENDING" :max="99" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="PAID">
        <template #label>
          待出貨
          <el-badge v-if="stats.PAID" :value="stats.PAID" :max="99" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="SHIPPED">
        <template #label>
          待收貨
          <el-badge v-if="stats.SHIPPED" :value="stats.SHIPPED" :max="99" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="已完成" name="COMPLETED" />
    </el-tabs>

    <!-- 訂單列表 -->
    <div class="orders-list" v-loading="loading">
      <el-empty v-if="orderList.length === 0" description="暫無訂單" />

      <div v-for="order in orderList" :key="order.orderId" class="order-card">
        <div class="order-header">
          <span class="order-no">訂單編號：{{ order.orderNo }}</span>
          <span class="order-time">{{ formatDate(order.createTime) }}</span>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        <div class="order-items">
          <div v-for="item in order.orderItems" :key="item.itemId" class="order-item">
            <img :src="getImageUrl(item.skuImage)" class="item-image" />
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-sku" v-if="item.skuName">規格：{{ item.skuName }}</div>
            </div>
            <div class="item-price">${{ formatPrice(item.unitPrice) }}</div>
            <div class="item-quantity">x{{ item.quantity }}</div>
          </div>
        </div>
        <div class="order-footer">
          <div class="order-total">
            共 {{ getTotalQuantity(order) }} 件商品，
            合計：<span class="price">${{ formatPrice(order.totalAmount) }}</span>
            <span class="shipping" v-if="order.shippingAmount > 0">
              （含運費 ${{ formatPrice(order.shippingAmount) }}）
            </span>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="viewDetail(order.orderNo)">查看詳情</el-button>
            <el-button
              v-if="order.status === 'PENDING' && order.paymentMethod === 'ECPAY'"
              size="small"
              type="primary"
              :loading="payingOrderNo === order.orderNo"
              @click="handlePayment(order)"
            >
              去付款
            </el-button>
            <el-button
              v-if="order.status === 'PENDING'"
              size="small"
              type="danger"
              plain
              @click="handleCancel(order)"
            >
              取消訂單
            </el-button>
            <el-button
              v-if="order.status === 'SHIPPED'"
              size="small"
              type="success"
              @click="handleConfirm(order)"
            >
              確認收貨
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, getOrderStats, memberCancelOrder, memberConfirmReceipt } from '@/api/shop/order'
import { createEcpayPayment } from '@/api/shop/payment'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const activeStatus = ref(route.query.status || 'all')
const orderList = ref([])
const stats = ref({})
const payingOrderNo = ref(null)

onMounted(() => {
  fetchStats()
  fetchOrders()
})

function handleStatusChange() {
  fetchOrders()
}

async function fetchStats() {
  try {
    const response = await getOrderStats()
    if (response.code === 200) {
      stats.value = response.data || {}
    }
  } catch (error) {
    console.error('載入統計失敗', error)
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const status = activeStatus.value === 'all' ? null : activeStatus.value
    const response = await getMyOrders(status)
    if (response.code === 200) {
      orderList.value = response.data || []
    }
  } catch (error) {
    console.error('載入訂單失敗', error)
  } finally {
    loading.value = false
  }
}

function getImageUrl(url) {
  if (!url) return '/placeholder-image.png'
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-TW')
}

function getTotalQuantity(order) {
  if (!order.orderItems) return 0
  return order.orderItems.reduce((sum, item) => sum + item.quantity, 0)
}

function viewDetail(orderNo) {
  router.push(`/mall/member/order/${orderNo}`)
}

function getStatusType(status) {
  const map = {
    PENDING: 'warning',
    PAID: 'info',
    PROCESSING: 'info',
    SHIPPED: 'primary',
    DELIVERED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    PENDING: '待付款',
    PAID: '待出貨',
    PROCESSING: '處理中',
    SHIPPED: '待收貨',
    DELIVERED: '已送達',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

async function handlePayment(order) {
  payingOrderNo.value = order.orderNo
  try {
    const res = await createEcpayPayment(order.orderNo)
    if (res.code === 200 && res.data?.formHtml) {
      const container = document.createElement('div')
      container.innerHTML = res.data.formHtml
      document.body.appendChild(container)
      const form = container.querySelector('form')
      if (form) {
        form.submit()
      }
    } else {
      ElMessage.error(res.msg || '建立付款失敗')
    }
  } catch (error) {
    ElMessage.error(error.message || '建立付款失敗')
  } finally {
    payingOrderNo.value = null
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('確定要取消此訂單嗎？', '取消訂單', {
      confirmButtonText: '確定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })

    await memberCancelOrder(order.orderId)
    ElMessage.success('訂單已取消')
    await fetchOrders()
    await fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失敗')
    }
  }
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm('確認已收到商品嗎？', '確認收貨', {
      confirmButtonText: '確認收貨',
      cancelButtonText: '取消',
      type: 'info'
    })

    await memberConfirmReceipt(order.orderId)
    ElMessage.success('確認收貨成功')
    await fetchOrders()
    await fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失敗')
    }
  }
}
</script>

<style scoped>
.orders-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.order-card {
  background: #fafafa;
  border-radius: 12px;
  overflow: hidden;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #f0f2f5;
}

.order-no {
  font-size: 14px;
  color: #303133;
}

.order-time {
  font-size: 12px;
  color: #909399;
  flex: 1;
}

.order-items {
  padding: 16px 20px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.order-item + .order-item {
  border-top: 1px solid #ebeef5;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  color: #303133;
}

.item-sku {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.item-price,
.item-quantity {
  font-size: 14px;
  color: #606266;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.order-total {
  font-size: 14px;
  color: #606266;
}

.order-total .price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.order-total .shipping {
  font-size: 12px;
  color: #909399;
}

.order-actions {
  display: flex;
  gap: 8px;
}

/* 響應式 */
@media (max-width: 768px) {
  .order-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .order-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
