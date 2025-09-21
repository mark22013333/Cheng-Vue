<template>
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    @select="handleSelect"
  >
    <template v-for="(item, index) in topMenus">
      <el-menu-item :style="{'--theme': theme}" :index="item.path" :key="index" v-if="index < visibleNumber">
        <svg-icon
        v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
        :icon-class="item.meta.icon"/>
        {{ item.meta.title }}
      </el-menu-item>
    </template>

    <!-- 頂部選單超出數量折叠 -->
    <el-submenu :style="{'--theme': theme}" index="more" :key="visibleNumber" v-if="topMenus.length > visibleNumber">
      <template slot="title">更多選單</template>
      <template v-for="(item, index) in topMenus">
        <el-menu-item
          :index="item.path"
          :key="index"
          v-if="index >= visibleNumber">
          <svg-icon
            v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
            :icon-class="item.meta.icon"/>
          {{ item.meta.title }}
        </el-menu-item>
      </template>
    </el-submenu>
  </el-menu>
</template>

<script>
import {constantRoutes} from "@/router"
import {isHttp} from "@/utils/validate"

// 隱藏側邊欄路由
const hideList = ['/index', '/user/profile']

export default {
  data() {
    return {
      // 頂部欄初始數
      visibleNumber: 5,
      // 目前激活選單的 index
      currentIndex: undefined
    }
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme
    },
    // 頂部顯示選單
    topMenus() {
      let topMenus = []
      this.routers.map((menu) => {
        if (menu.hidden !== true) {
          // 兼容頂部欄一級選單内部跳轉
          if (menu.path === '/' && menu.children) {
            topMenus.push(menu.children[0])
          } else {
            topMenus.push(menu)
          }
        }
      })
      return topMenus
    },
    // 所有的路由訊息
    routers() {
      return this.$store.state.permission.topbarRouters
    },
    // 設定子路由
    childrenMenus() {
      var childrenMenus = []
      this.routers.map((router) => {
        for (var item in router.children) {
          if (router.children[item].parentPath === undefined) {
            if(router.path === "/") {
              router.children[item].path = "/" + router.children[item].path
            } else {
              if(!isHttp(router.children[item].path)) {
                router.children[item].path = router.path + "/" + router.children[item].path
              }
            }
            router.children[item].parentPath = router.path
          }
          childrenMenus.push(router.children[item])
        }
      })
      return constantRoutes.concat(childrenMenus)
    },
    // 預設激活的選單
    activeMenu() {
      const path = this.$route.path
      let activePath = path
      if (path !== undefined && path.lastIndexOf("/") > 0 && hideList.indexOf(path) === -1) {
        const tmpPath = path.substring(1, path.length)
        if (!this.$route.meta.link) {
          activePath = "/" + tmpPath.substring(0, tmpPath.indexOf("/"))
          this.$store.dispatch('app/toggleSideBarHide', false)
        }
      } else if(!this.$route.children) {
        activePath = path
        this.$store.dispatch('app/toggleSideBarHide', true)
      }
      this.activeRoutes(activePath)
      return activePath
    },
  },
  beforeMount() {
    window.addEventListener('resize', this.setVisibleNumber)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.setVisibleNumber)
  },
  mounted() {
    this.setVisibleNumber()
  },
  methods: {
    // 根據寬度計算設定顯示欄數
    setVisibleNumber() {
      const width = document.body.getBoundingClientRect().width / 3
      this.visibleNumber = parseInt(width / 85)
    },
    // 選單選擇事件
    handleSelect(key, keyPath) {
      this.currentIndex = key
      const route = this.routers.find(item => item.path === key)
      if (isHttp(key)) {
        // http(s):// 路徑新視窗打開
        window.open(key, "_blank")
      } else if (!route || !route.children) {
        // 沒有子路由路徑内部打開
        const routeMenu = this.childrenMenus.find(item => item.path === key)
        if (routeMenu && routeMenu.query) {
          let query = JSON.parse(routeMenu.query)
          this.$router.push({ path: key, query: query })
        } else {
          this.$router.push({ path: key })
        }
        this.$store.dispatch('app/toggleSideBarHide', true)
      } else {
        // 顯示左側聯動選單
        this.activeRoutes(key)
        this.$store.dispatch('app/toggleSideBarHide', false)
      }
    },
    // 目前激活的路由
    activeRoutes(key) {
      var routes = []
      if (this.childrenMenus && this.childrenMenus.length > 0) {
        this.childrenMenus.map((item) => {
          if (key == item.parentPath || (key == "index" && "" == item.path)) {
            routes.push(item)
          }
        })
      }
      if(routes.length > 0) {
        this.$store.commit("SET_SIDEBAR_ROUTERS", routes)
      } else {
        this.$store.dispatch('app/toggleSideBarHide', true)
      }
    }
  },
}
</script>

<style lang="scss">
.topmenu-container.el-menu--horizontal > .el-menu-item {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #999093 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}

.topmenu-container.el-menu--horizontal > .el-menu-item.is-active, .el-menu--horizontal > .el-submenu.is-active .el-submenu__title {
  border-bottom: 2px solid #{'var(--theme)'} !important;
  color: #303133;
}

/* submenu item */
.topmenu-container.el-menu--horizontal > .el-submenu .el-submenu__title {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #999093 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}
</style>
