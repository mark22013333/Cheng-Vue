<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <Collection style="width: 16px; height: 16px;" />
              <span>暫存列表</span>
            </div>
            <el-button link type="primary" icon="Refresh" @click="refreshCacheNames()" />
          </div>
          <div class="monitor-panel__body" style="padding: 0;">
            <el-table
              v-loading="loading"
              :data="cacheNames"
              :height="tableHeight"
              highlight-current-row
              @row-click="getCacheKeys"
              class="custom-table panel-table"
            >
              <el-table-column label="序號" width="60" type="index" />
              <el-table-column label="暫存名稱" align="center" prop="cacheName" :show-overflow-tooltip="true" :formatter="nameFormatter" />
              <el-table-column label="備註" align="center" prop="remark" :show-overflow-tooltip="true" />
              <el-table-column label="操作" width="60" align="center" class-name="small-padding fixed-width" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" icon="Delete" @click="handleClearCacheName(scope.row)" />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <Key style="width: 16px; height: 16px;" />
              <span>鍵名列表</span>
            </div>
            <el-button link type="primary" icon="Refresh" @click="refreshCacheKeys()" />
          </div>
          <div class="monitor-panel__body" style="padding: 0;">
            <el-table
              v-loading="subLoading"
              :data="cacheKeys"
              :height="tableHeight"
              highlight-current-row
              @row-click="handleCacheValue"
              class="custom-table panel-table"
            >
              <el-table-column label="序號" width="60" type="index" />
              <el-table-column label="暫存鍵名" align="center" :show-overflow-tooltip="true" :formatter="keyFormatter" />
              <el-table-column label="操作" width="60" align="center" class-name="small-padding fixed-width" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" icon="Delete" @click="handleClearCacheKey(scope.row)" />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="monitor-panel">
          <div class="monitor-panel__header">
            <div class="monitor-panel__title">
              <Document style="width: 16px; height: 16px;" />
              <span>暫存内容</span>
            </div>
            <el-button link type="primary" icon="Refresh" @click="handleClearCacheAll()">清理全部</el-button>
          </div>
          <div class="monitor-panel__body">
            <el-form :model="cacheForm" label-position="top">
              <el-form-item label="暫存名稱" prop="cacheName">
                <el-input v-model="cacheForm.cacheName" :readOnly="true" />
              </el-form-item>
              <el-form-item label="暫存鍵名" prop="cacheKey">
                <el-input v-model="cacheForm.cacheKey" :readOnly="true" />
              </el-form-item>
              <el-form-item label="暫存内容" prop="cacheValue">
                <el-input v-model="cacheForm.cacheValue" type="textarea" :rows="8" :readOnly="true" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CacheList">
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache"

const { proxy } = getCurrentInstance()

const cacheNames = ref([])
const cacheKeys = ref([])
const cacheForm = ref({})
const loading = ref(true)
const subLoading = ref(false)
const nowCacheName = ref("")
const tableHeight = ref(window.innerHeight - 240)

/** 查詢暫存名稱列表 */
function getCacheNames() {
  loading.value = true
  listCacheName().then(response => {
    cacheNames.value = response.data
    loading.value = false
  })
}

/** 重新整理暫存名稱列表 */
function refreshCacheNames() {
  getCacheNames()
  proxy.$modal.msgSuccess("重新整理暫存列表成功")
}

/** 清理指定名稱暫存 */
function handleClearCacheName(row) {
  clearCacheName(row.cacheName).then(response => {
    proxy.$modal.msgSuccess("清理暫存名稱[" + row.cacheName + "]成功")
    getCacheKeys()
  })
}

/** 查詢暫存鍵名列表 */
function getCacheKeys(row) {
  const cacheName = row !== undefined ? row.cacheName : nowCacheName.value
  if (cacheName === "") {
    return
  }
  subLoading.value = true
  listCacheKey(cacheName).then(response => {
    cacheKeys.value = response.data
    subLoading.value = false
    nowCacheName.value = cacheName
  })
}

/** 重新整理暫存鍵名列表 */
function refreshCacheKeys() {
  getCacheKeys()
  proxy.$modal.msgSuccess("重新整理鍵名列表成功")
}

/** 清理指定鍵名暫存 */
function handleClearCacheKey(cacheKey) {
  clearCacheKey(cacheKey).then(response => {
    proxy.$modal.msgSuccess("清理暫存鍵名[" + cacheKey + "]成功")
    getCacheKeys()
  })
}

/** 列表前缀去除 */
function nameFormatter(row) {
  return row.cacheName.replace(":", "")
}

/** 鍵名前缀去除 */
function keyFormatter(cacheKey) {
  return cacheKey.replace(nowCacheName.value, "")
}

/** 查詢暫存内容詳細 */
function handleCacheValue(cacheKey) {
  getCacheValue(nowCacheName.value, cacheKey).then(response => {
    cacheForm.value = response.data
  })
}

/** 清理全部暫存 */
function handleClearCacheAll() {
  clearCacheAll().then(response => {
    proxy.$modal.msgSuccess("清理全部暫存成功")
  })
}

getCacheNames()
</script>

<style lang="scss" scoped>
.monitor-panel {
  background: var(--el-bg-color, #ffffff);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  height: calc(100vh - 125px);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__body {
    flex: 1;
    overflow: auto;
    padding: 16px 20px;
  }
}

/* 面板內嵌表格移除外層邊框和圓角 */
:deep(.panel-table) {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

html.dark .monitor-panel {
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: none;

  .monitor-panel__header {
    border-bottom-color: rgba(255, 255, 255, 0.06);
  }
}
</style>
