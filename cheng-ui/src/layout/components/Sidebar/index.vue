<template>
    <div :class="{'has-logo':showLogo}" :style="{ backgroundColor: settingsStore.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="showLogo" :collapse="isCollapse" />
        <el-scrollbar :class="settingsStore.sideTheme" wrap-class="scrollbar-wrapper">
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="settingsStore.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground"
                :text-color="settingsStore.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
                :unique-opened="true"
                :active-text-color="settingsStore.theme"
                :collapse-transition="false"
                mode="vertical"
            >
                <sidebar-item
                    v-for="(route, index) in permissionStore.sidebarRouters"
                    :key="route.path  + index"
                    :item="route"
                    :base-path="route.path"
                />
            </el-menu>
        </el-scrollbar>
        <!-- 可拖曳的分隔條 -->
        <div
            v-if="!isCollapse"
            class="sidebar-resizer"
            @mousedown="startResize"
            :title="'拖曳調整選單寬度 (目前: ' + appStore.sidebar.width + 'px)'"
        ></div>
    </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const isResizing = ref(false)

const activeMenu = computed(() => {
    const { meta, path } = route
    // if set a path, the sidebar will highlight the path you set
    if (meta.activeMenu) {
        return meta.activeMenu
    }
    return path
})

const showLogo = computed(() => {
    return settingsStore.sidebarLogo
})

const variables = computed(() => {
    // Vite 不支援 import SCSS 文件，直接定義顏色變數
    return {
        menuBackground: '#304156',
        menuLightBackground: '#ffffff',
        menuColor: '#bfcbd9',
        menuLightColor: 'rgba(0,0,0,.70)'  // 修正淺色主題文字顏色
    }
})

const isCollapse = computed(() => {
    return !appStore.sidebar.opened
})

function startResize(e) {
    isResizing.value = true
    const startX = e.clientX
    const startWidth = appStore.sidebar.width

    const handleMouseMove = (e) => {
        if (!isResizing.value) return

        const deltaX = e.clientX - startX
        let newWidth = startWidth + deltaX

        // 限制最小和最大寬度
        newWidth = Math.max(180, Math.min(400, newWidth))

        // 更新 Pinia 狀態
        appStore.setSidebarWidth(newWidth)
    }

    const handleMouseUp = () => {
        isResizing.value = false
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)
        document.body.style.cursor = ''
        document.body.style.userSelect = ''
    }

    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
    document.body.style.cursor = 'ew-resize'
    document.body.style.userSelect = 'none'
}
</script>

<style lang="scss" scoped>
.sidebar-resizer {
    position: absolute;
    top: 0;
    right: 0;
    width: 4px;
    height: 100%;
    cursor: ew-resize;
    background-color: transparent;
    z-index: 1002;
    transition: background-color 0.2s;

    &:hover {
        background-color: rgba(64, 158, 255, 0.5);
    }

    &:active {
        background-color: rgba(64, 158, 255, 0.8);
    }
}
</style>
