<template>
   <i-frame v-model:src="url"></i-frame>
</template>

<script setup>
import iFrame from '@/components/iFrame'

const baseApi = import.meta.env.VITE_APP_BASE_API
const isCadmPath = typeof window !== 'undefined' && window.location.pathname.startsWith('/cadm')
const isDev = import.meta.env.DEV
// 本機 Vite 開發環境要走 /dev-api（不能加 /cadm 前綴）
// UAT/PROD 在 /cadm 入口且 baseApi=/prod-api 時才加 /cadm 前綴
const apiBase = (isCadmPath && !isDev && baseApi === '/prod-api')
  ? `/cadm${baseApi}`
  : baseApi
const configUrl = `${apiBase}/v3/api-docs/swagger-config`
const url = ref(`${apiBase}/swagger-ui/index.html?configUrl=${encodeURIComponent(configUrl)}`)
</script>
