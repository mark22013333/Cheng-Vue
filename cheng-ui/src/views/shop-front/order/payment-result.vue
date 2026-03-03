<template>
  <div class="payment-result-page">
    <el-result
      :icon="isSuccess ? 'success' : 'error'"
      :title="isSuccess ? '付款成功' : '付款失敗'"
      :sub-title="subTitle"
    >
      <template #extra>
        <el-button type="primary" @click="goToOrderDetail">
          查看訂單
        </el-button>
        <el-button @click="$router.push('/products')">繼續購物</el-button>
        <el-button v-if="!isSuccess" type="warning" @click="retryPayment">
          重新付款
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createEcpayPayment } from '@/api/shop/payment'
import { getMemberToken } from '@/utils/memberAuth'

const route = useRoute()
const router = useRouter()

const orderNo = route.params.orderNo || ''
const status = route.query.status || 'success'

const isSuccess = computed(() => status === 'success')

const subTitle = computed(() => {
  if (isSuccess.value) {
    return `訂單編號：${orderNo}，付款已完成`
  }
  return `訂單編號：${orderNo}，付款未完成，請重新嘗試`
})

/**
 * 檢查會員 Token 是否存在，若不存在則引導登入
 * @returns {boolean} Token 是否有效
 */
function ensureAuthenticated() {
  if (getMemberToken()) {
    return true
  }
  ElMessageBox.confirm(
    '登入已過期，需要重新登入後才能繼續操作。',
    '請重新登入',
    { confirmButtonText: '前往登入', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    // 帶 redirect 回當前頁面，登入後可直接返回
    const currentPath = route.fullPath
    router.push(`/login?redirect=${encodeURIComponent(currentPath)}`)
  }).catch(() => {})
  return false
}

/** 查看訂單詳情（需要登入） */
function goToOrderDetail() {
  if (!ensureAuthenticated()) return
  router.push(`/member/order/${orderNo}`)
}

/** 重新付款（需要登入） */
async function retryPayment() {
  if (!ensureAuthenticated()) return
  try {
    const res = await createEcpayPayment(orderNo)
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
    ElMessage.error('建立付款失敗，請稍後再試')
  }
}
</script>

<style scoped>
.payment-result-page {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
