<template>
    <div :class="{'has-logo':showLogo}" :style="{ backgroundColor: settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="showLogo" :collapse="isCollapse" />
        <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground"
                :text-color="settings.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
                :unique-opened="true"
                :active-text-color="settings.theme"
                :collapse-transition="false"
                mode="vertical"
            >
                <sidebar-item
                    v-for="(route, index) in sidebarRouters"
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
            :title="'拖曳調整選單寬度 (目前: ' + sidebar.width + 'px)'"
        ></div>
    </div>
</template>

<script>
import {mapGetters, mapState} from "vuex"
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"

export default {
    components: { SidebarItem, Logo },
    data() {
        return {
            isResizing: false
        }
    },
    computed: {
        ...mapState(["settings"]),
        ...mapGetters(["sidebarRouters", "sidebar"]),
        activeMenu() {
            const route = this.$route
            const { meta, path } = route
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu
            }
            return path
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo
        },
        variables() {
            // Vite 不支援 import SCSS 文件，直接定義顏色變數
            return {
                menuBackground: '#304156',
                menuLightBackground: '#ffffff',
                menuColor: '#bfcbd9',
                menuLightColor: '#303133'
            }
        },
        isCollapse() {
            return !this.sidebar.opened
        }
    },
    methods: {
        startResize(e) {
            this.isResizing = true
            const startX = e.clientX
            const startWidth = this.sidebar.width

            const handleMouseMove = (e) => {
                if (!this.isResizing) return

                const deltaX = e.clientX - startX
                let newWidth = startWidth + deltaX

                // 限制最小和最大寬度
                newWidth = Math.max(180, Math.min(400, newWidth))

                // 更新 Vuex 狀態
                this.$store.dispatch('app/setSidebarWidth', newWidth)
            }

            const handleMouseUp = () => {
                this.isResizing = false
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
    }
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
