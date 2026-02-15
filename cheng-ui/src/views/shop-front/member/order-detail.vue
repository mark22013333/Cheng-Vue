<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>訂單詳情</h2>
    </div>

    <template v-if="order">
      <!-- 訂單狀態 -->
      <div class="status-section">
        <div class="status-header">
          <el-tag :type="getStatusType(order.status)" size="large">
            {{ getStatusText(order.status) }}
          </el-tag>
          <span class="order-no">訂單編號：{{ order.orderNo }}</span>
        </div>
        <el-steps :active="getStepActive(order.status)" finish-status="success" class="status-steps">
          <el-step title="提交訂單" :description="formatDate(order.createTime)" />
          <el-step title="付款成功" :description="order.paidTime ? formatDate(order.paidTime) : ''" />
          <el-step title="商品出貨" :description="order.shippedTime ? formatDate(order.shippedTime) : ''" />
          <el-step title="確認收貨" :description="getReceivedTimeDescription(order)" />
        </el-steps>
      </div>

      <!-- 收貨資訊 -->
      <div class="info-section">
        <h3><el-icon><Location /></el-icon> 收貨資訊</h3>
        <div class="info-content">
          <div class="info-row">
            <span class="label">收貨人：</span>
            <span class="value">{{ order.receiverName }}</span>
          </div>
          <div class="info-row">
            <span class="label">聯繫電話：</span>
            <span class="value">{{ order.receiverMobile }}</span>
          </div>
          <!-- 超商取貨資訊 -->
          <template v-if="order.cvsStoreName">
            <div class="info-row">
              <span class="label">取貨門市：</span>
              <span class="value cvs-store">
                <el-tag type="primary" size="small">{{ getShippingMethodText(order.shippingMethod) }}</el-tag>
                {{ order.cvsStoreName }}
              </span>
            </div>
            <div class="info-row" v-if="order.cvsStoreId">
              <span class="label">門市代號：</span>
              <span class="value">{{ order.cvsStoreId }}</span>
            </div>
            <div class="info-row">
              <span class="label">門市地址：</span>
              <span class="value">{{ order.cvsStoreAddress || order.receiverAddress }}</span>
            </div>
          </template>
          <!-- 宅配地址 -->
          <template v-else>
            <div class="info-row">
              <span class="label">收貨地址：</span>
              <span class="value">{{ order.receiverAddress }}</span>
            </div>
          </template>
        </div>
      </div>

      <!-- 物流資訊（有物流單號時顯示） -->
      <div class="info-section" v-if="order.shippingNo || order.shippingMethod">
        <h3><el-icon><Van /></el-icon> 物流資訊</h3>
        <div class="info-content">
          <div class="info-row">
            <span class="label">配送方式：</span>
            <span class="value">{{ getShippingMethodText(order.shippingMethod) }}</span>
          </div>
          <div class="info-row" v-if="order.shippingNo">
            <span class="label">物流單號：</span>
            <span class="value shipping-no">
              {{ order.shippingNo }}
              <el-button text type="primary" size="small" @click="copyShippingNo">複製</el-button>
            </span>
          </div>
          <div class="info-row" v-else-if="order.status === 'PAID' && order.cvsStoreName">
            <span class="label">物流狀態：</span>
            <span class="value pending-logistics">物流單建立中，請稍候...</span>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="items-section">
        <h3><el-icon><ShoppingBag /></el-icon> 商品資訊</h3>
        <div class="items-list">
          <div v-for="item in order.orderItems" :key="item.itemId" class="order-item">
            <img :src="getImageUrl(item.skuImage)" class="item-image" />
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-sku" v-if="item.skuName">規格：{{ item.skuName }}</div>
            </div>
            <div class="item-price">${{ formatPrice(item.unitPrice) }}</div>
            <div class="item-quantity">x{{ item.quantity }}</div>
            <div class="item-subtotal">${{ formatPrice(item.totalPrice) }}</div>
          </div>
        </div>
      </div>

      <!-- 金額明細 -->
      <div class="amount-section">
        <h3><el-icon><Document /></el-icon> 金額明細</h3>
        <div class="amount-content">
          <div class="amount-row">
            <span>商品金額</span>
            <span>${{ formatPrice(order.productAmount) }}</span>
          </div>
          <div class="amount-row">
            <span>運費</span>
            <span v-if="order.shippingAmount > 0">${{ formatPrice(order.shippingAmount) }}</span>
            <span v-else class="free-shipping">免運費</span>
          </div>
          <div v-if="order.discountAmount > 0" class="amount-row discount">
            <span>折扣</span>
            <span>-${{ formatPrice(order.discountAmount) }}</span>
          </div>
          <div class="amount-row total">
            <span>實付金額</span>
            <span class="total-price">${{ formatPrice(order.totalAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 訂單資訊 -->
      <div class="info-section">
        <h3><el-icon><Tickets /></el-icon> 訂單資訊</h3>
        <div class="info-content">
          <div class="info-row">
            <span class="label">訂單編號：</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">下單時間：</span>
            <span class="value">{{ formatDate(order.createTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">付款方式：</span>
            <span class="value">{{ getPaymentMethodText(order.paymentMethod) }}</span>
          </div>
          <div class="info-row" v-if="order.buyerRemark">
            <span class="label">買家備註：</span>
            <span class="value">{{ order.buyerRemark }}</span>
          </div>
          <div class="info-row" v-if="order.sellerRemark">
            <span class="label">賣家備註：</span>
            <span class="value">{{ order.sellerRemark }}</span>
          </div>
        </div>
      </div>

      <!-- 操作按鈕 -->
      <div class="action-section">
        <el-button @click="$router.push('/member/orders')">返回訂單列表</el-button>
        <el-button
          v-if="order.status === 'PENDING' && order.paymentMethod === 'ECPAY'"
          type="primary"
          :loading="paying"
          @click="handlePayment"
        >
          立即付款
        </el-button>
        <el-button
          v-if="order.status === 'PENDING'"
          type="danger"
          plain
          @click="handleCancel"
        >
          取消訂單
        </el-button>
        <el-button
          v-if="order.status === 'SHIPPED'"
          type="success"
          @click="handleConfirm"
        >
          確認收貨
        </el-button>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="訂單不存在">
      <el-button type="primary" @click="$router.push('/member/orders')">返回訂單列表</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, ShoppingBag, Document, Tickets, Van } from '@element-plus/icons-vue'
import { getMyOrderDetail, memberCancelOrder, memberConfirmReceipt } from '@/api/shop/order'
import { createEcpayPayment } from '@/api/shop/payment'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo || ''

const loading = ref(false)
const order = ref(null)
const paying = ref(false)

onMounted(() => {
  if (orderNo) {
    fetchOrderDetail()
  }
})

async function fetchOrderDetail() {
  loading.value = true
  try {
    const response = await getMyOrderDetail(orderNo)
    if (response.code === 200) {
      order.value = response.data
    } else {
      ElMessage.error(response.msg || '載入訂單失敗')
    }
  } catch (error) {
    console.error('載入訂單詳情失敗', error)
    ElMessage.error('載入訂單詳情失敗')
  } finally {
    loading.value = false
  }
}

function getImageUrl(url) {
  if (!url) return '/placeholder-image.png'
  if (url.startsWith('http')) return url
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-TW')
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

function getStepActive(status) {
  // el-steps 的 active 值：索引 0 到 active-1 的步驟會顯示為完成（綠色打勾）
  // active 索引的步驟顯示為「進行中」，之後的步驟顯示為「待處理」
  const map = {
    PENDING: 1,     // 步驟 0（提交訂單）完成，步驟 1（付款）進行中
    PAID: 2,        // 步驟 0,1 完成，步驟 2（出貨）進行中
    PROCESSING: 2,  // 步驟 0,1 完成，步驟 2（出貨）進行中
    SHIPPED: 3,     // 步驟 0,1,2 完成，步驟 3（收貨）進行中
    DELIVERED: 4,   // 全部 4 個步驟完成
    COMPLETED: 4,   // 全部 4 個步驟完成
    CANCELLED: 0    // 訂單已取消，無進度
  }
  return map[status] || 0
}

// 取得確認收貨步驟的時間描述
function getReceivedTimeDescription(order) {
  // 優先顯示 receivedTime（已簽收時間），其次是 completeTime（完成時間）
  if (order.receivedTime) {
    return formatDate(order.receivedTime)
  }
  if (order.completeTime) {
    return formatDate(order.completeTime)
  }
  return ''
}

function getPaymentMethodText(method) {
  const map = {
    COD: '貨到付款',
    ECPAY: '綠界金流',
    CREDIT_CARD: '信用卡',
    LINE_PAY: 'LINE Pay',
    BANK_TRANSFER: '銀行轉帳'
  }
  return map[method] || method || '未指定'
}

function getShippingMethodText(method) {
  const map = {
    HOME_DELIVERY: '宅配到府',
    CVS_711: '7-ELEVEN 超取',
    CVS_FAMILY: '全家超取',
    CVS_HILIFE: '萊爾富超取',
    STORE_PICKUP: '門市自取'
  }
  return map[method] || method || '未指定'
}

function copyShippingNo() {
  if (order.value?.shippingNo) {
    navigator.clipboard.writeText(order.value.shippingNo)
      .then(() => ElMessage.success('已複製物流單號'))
      .catch(() => ElMessage.error('複製失敗'))
  }
}

async function handlePayment() {
  paying.value = true
  try {
    const res = await createEcpayPayment(order.value.orderNo)
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
    paying.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('確定要取消此訂單嗎？', '取消訂單', {
      confirmButtonText: '確定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })

    await memberCancelOrder(order.value.orderId)
    ElMessage.success('訂單已取消')
    await fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失敗')
    }
  }
}

async function handleConfirm() {
  try {
    await ElMessageBox.confirm('確認已收到商品嗎？', '確認收貨', {
      confirmButtonText: '確認收貨',
      cancelButtonText: '取消',
      type: 'info'
    })

    await memberConfirmReceipt(order.value.orderId)
    ElMessage.success('確認收貨成功')
    await fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失敗')
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

/* 狀態區塊 */
.status-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.order-no {
  font-size: 14px;
  color: #909399;
}

.status-steps {
  margin-top: 20px;
}

/* 資訊區塊 */
.info-section,
.items-section,
.amount-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.info-section h3,
.items-section h3,
.amount-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #303133;
  margin: 0 0 16px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-row .label {
  color: #909399;
  width: 100px;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
}

.info-row .value.cvs-store {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row .value.shipping-no {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.info-row .value.pending-logistics {
  color: #e6a23c;
  font-style: italic;
}

/* 商品列表 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
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
  width: 80px;
  text-align: center;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  width: 100px;
  text-align: right;
}

/* 金額明細 */
.amount-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.amount-row.discount {
  color: #f56c6c;
}

.amount-row.total {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
  margin-top: 8px;
  font-weight: 500;
}

.free-shipping {
  color: #67c23a;
}

.total-price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

/* 操作按鈕 */
.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
}

/* 響應式 */
@media (max-width: 768px) {
  .order-item {
    flex-wrap: wrap;
  }

  .item-price,
  .item-quantity,
  .item-subtotal {
    width: auto;
  }

  .status-steps {
    display: none;
  }

  .action-section {
    flex-direction: column;
  }

  .action-section .el-button {
    width: 100%;
  }
}
</style>
