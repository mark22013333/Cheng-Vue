<template>
   <i-frame v-model:src="url"></i-frame>
</template>

<script setup>
import iFrame from '@/components/iFrame'

const baseApi = import.meta.env.VITE_APP_BASE_API
const isCadmPath = typeof window !== 'undefined' && window.location.pathname.startsWith('/cadm')
const isDev = import.meta.env.DEV
// Swagger 不應走 /prod-api，否則會變成 /cadm/prod-api/swagger-ui/... 並被後端視為 /cadm/swagger-ui...
// 在 /cadm 入口（UAT/PROD）固定走 /cadm/swagger-ui 與 /cadm/v3/api-docs；
// 本機 Vite 開發環境則走 baseApi（例如 /dev-api）由 proxy 轉發到後端。
const swaggerBase = isCadmPath && !isDev ? '/cadm' : baseApi
const configUrl = `${swaggerBase}/v3/api-docs/swagger-config`
const url = ref(`${swaggerBase}/swagger-ui/index.html?configUrl=${encodeURIComponent(configUrl)}`)
</script>
