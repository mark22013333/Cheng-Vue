<template>
  <router-view />
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

onMounted(() => {
  nextTick(() => {
    // 初始化主題樣式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>

<style>
/* 全局圖片預覽器樣式 - 確保在最上層 */
.el-image-viewer__wrapper {
  z-index: 2147483647 !important;
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
}

.el-image-viewer__mask {
  z-index: 2147483646 !important;
  position: fixed !important;
}

.el-image-viewer__btn {
  z-index: 2147483647 !important;
}

.el-image-viewer__canvas {
  z-index: 2147483647 !important;
}

.el-image-viewer__actions {
  z-index: 2147483647 !important;
}

.el-image-viewer__close {
  z-index: 2147483647 !important;
}

/* 全局下拉選單樣式 - 確保可以看到選項 */
.el-select-dropdown {
  max-height: 400px !important;
}

.el-select-dropdown .el-select-dropdown__wrap {
  max-height: 400px !important;
}

.el-select-dropdown .el-scrollbar__view {
  max-height: 400px !important;
}

/* 多選下拉選單動態高度 */
.el-select__tags {
  max-height: 200px !important;
  overflow-y: auto !important;
}
</style>
