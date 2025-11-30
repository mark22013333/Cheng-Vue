<template>
  <div :class="classObj" class="app-wrapper" :style="{ '--current-color': theme, '--sidebar-width': (sidebar.hide || device === 'mobile') ? '0px' : (sidebar.opened ? sidebar.width + 'px' : '54px') }">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <sidebar v-if="!sidebar.hide" class="sidebar-container" />
    <div 
      :class="{ hasTagsView: needTagsView, sidebarHide: sidebar.hide }" 
      class="main-container"
      :style="{ marginLeft: (sidebar.hide || device === 'mobile') ? '0px' : (sidebar.opened ? sidebar.width + 'px' : '54px') }"
    >
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar @setLayout="setLayout" />
        <tags-view v-if="needTagsView" />
      </div>
      <app-main />
      <settings ref="settingRef" />
    </div>
    <!-- 浮動掃描按鈕（手機端） -->
    <floating-scan-button />
    <!-- 掃描結果按鈕（手機端） -->
    <scan-result-button />
  </div>
</template>

<script>
export default {
  name: 'Layout'
}
</script>

<script setup>
import { ref, computed, watch, watchEffect } from 'vue'
import { useWindowSize } from '@vueuse/core'
import FloatingScanButton from '@/components/FloatingScanButton'
import ScanResultButton from '@/components/ScanResultButton'
import Sidebar from './components/Sidebar/index.vue'
import { AppMain, Navbar, Settings, TagsView } from './components'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()
const theme = computed(() => settingsStore.theme)
const sideTheme = computed(() => settingsStore.sideTheme)
const sidebar = computed(() => useAppStore().sidebar)
const device = computed(() => useAppStore().device)
const needTagsView = computed(() => settingsStore.tagsView)
const fixedHeader = computed(() => settingsStore.fixedHeader)

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

const { width, height } = useWindowSize()
const WIDTH = 992 // refer to Bootstrap's responsive design

watch(() => device.value, () => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    useAppStore().closeSideBar({ withoutAnimation: false })
  }
})

watchEffect(() => {
  if (width.value - 1 < WIDTH) {
    useAppStore().toggleDevice('mobile')
    useAppStore().closeSideBar({ withoutAnimation: true })
  } else {
    useAppStore().toggleDevice('desktop')
  }
})

function handleClickOutside() {
  useAppStore().closeSideBar({ withoutAnimation: false })
}

const settingRef = ref(null)
function setLayout() {
  settingRef.value.openSetting()
}
</script>

<style lang="scss" scoped>
@use "@/assets/styles/mixin.scss" as mix;
@use "@/assets/styles/variables.module.scss" as vars;

.app-wrapper {
  @include mix.clearfix;
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.main-container:has(.fixed-header) {
  height: 100vh;
  overflow: hidden;
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - var(--sidebar-width, #{vars.$base-sidebar-width}));
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.sidebarHide .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}

.main-container {
  min-height: 100vh;
  transition: margin-left 0.28s;
  margin-left: vars.$base-sidebar-width;
  position: relative;
}

.hideSidebar .main-container {
  margin-left: 54px;
}

.sidebarHide .main-container {
  margin-left: 0;
}

.mobile .main-container {
  margin-left: 0;
}
</style>
<style lang="scss">
/* AppMain 相關的全局樣式 */
.fixed-header + .app-main {
  overflow-y: auto;
  scrollbar-gutter: auto;
  height: calc(100vh - 50px);
  min-height: 0px;
  margin-top: 50px;
}

.hasTagsView .app-main {
  /* 84 = navbar + tags-view = 50 + 34 */
  min-height: calc(100vh - 84px);
}

.hasTagsView .fixed-header + .app-main {
  margin-top: 84px;
  height: calc(100vh - 84px);
  min-height: 0px;
}
</style>

/* Transition 動畫 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
