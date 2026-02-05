<template>
   <i-frame v-model:src="url"></i-frame>
</template>

<script setup>
import iFrame from '@/components/iFrame'

const baseApi = import.meta.env.VITE_APP_BASE_API
const isCadmPath = typeof window !== 'undefined' && window.location.pathname.startsWith('/cadm')
const apiBase = isCadmPath ? `/cadm${baseApi}` : baseApi
const configUrl = `${apiBase}/v3/api-docs/swagger-config`
const url = ref(`${apiBase}/swagger-ui/index.html?configUrl=${encodeURIComponent(configUrl)}`)
</script>
