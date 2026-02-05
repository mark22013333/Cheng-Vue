<template>
  <div class="payment-result-page">
    <el-result
      :icon="isSuccess ? 'success' : 'error'"
      :title="isSuccess ? '付款成功' : '付款失敗'"
      :sub-title="subTitle"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push(`/member/order/${orderNo}`)">
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
import { ElMessage } from 'element-plus'
import { createEcpayPayment } from '@/api/shop/payment'

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

async function retryPayment() {
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
