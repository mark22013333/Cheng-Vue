<template>
   <i-frame v-model:src="url"></i-frame>
</template>

<script setup>
import iFrame from '@/components/iFrame'

const baseApi = import.meta.env.VITE_APP_BASE_API
const configUrl = `${baseApi}/v3/api-docs/swagger-config`
const url = ref(`${baseApi}/swagger-ui/index.html?configUrl=${encodeURIComponent(configUrl)}`)
</script>
