<template>
  <div class="order-success-page">
    <el-result icon="success" title="訂單提交成功" :sub-title="`訂單編號：${orderNo}`">
      <template #extra>
        <el-button type="primary" @click="$router.push('/mall/member/orders')">查看訂單</el-button>
        <el-button @click="$router.push('/mall/products')">繼續購物</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const orderNo = route.params.orderNo || ''
</script>

<style scoped>
.order-success-page {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
