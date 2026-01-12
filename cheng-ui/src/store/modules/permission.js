import { defineStore } from 'pinia'
import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

// 匹配views裡面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      routes: [],
      addRoutes: [],
      defaultRoutes: [],
      topbarRouters: [],
      sidebarRouters: []
    }),
    actions: {
      setRoutes(routes) {
        this.addRoutes = routes
        this.routes = constantRoutes.concat(routes)
      },
      setDefaultRoutes(routes) {
        this.defaultRoutes = constantRoutes.concat(routes)
      },
      setTopbarRoutes(routes) {
        this.topbarRouters = routes
      },
      setSidebarRouters(routes) {
        this.sidebarRouters = routes
      },
      generateRoutes(roles) {
        return new Promise(resolve => {
          // 向後端請求路由資料
          getRouters().then(res => {
            console.log('[generateRoutes] 📥 Backend routes:', JSON.stringify(res.data, null, 2))

            const sdata = JSON.parse(JSON.stringify(res.data))
            const rdata = JSON.parse(JSON.stringify(res.data))
            const defaultData = JSON.parse(JSON.stringify(res.data))
            const sidebarRoutes = filterAsyncRouter(sdata)
            const rewriteRoutes = filterAsyncRouter(rdata, false, true)
            const defaultRoutes = filterAsyncRouter(defaultData)
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)

            // console.log('[路由調試] 處理後的 rewriteRoutes:', JSON.stringify(rewriteRoutes, null, 2))
            // console.log('[路由調試] 處理後的 sidebarRoutes:', JSON.stringify(sidebarRoutes, null, 2))

            // 新增動態路由
            asyncRoutes.forEach(route => {
              // console.log('[路由調試] 新增動態路由:', route.path)
              router.addRoute(route)
            })

            // 新增後端返回的路由
            rewriteRoutes.forEach(route => {
              console.log('[路由調試] 🚀 新增後端路由:', route.path, 'name:', route.name)
              if (route.children && route.children.length > 0) {
                route.children.forEach(child => {
                  console.log('[路由調試]   └─ 子路由:', child.path, 'name:', child.name, 'component:', typeof child.component === 'function' ? '✅' : child.component)
                })
              }
              router.addRoute(route)
            })

            console.log('[路由調試] 🗂️ 所有已註冊的路由:')
            router.getRoutes().forEach(r => {
              if (r.path.includes('template') || r.path.includes('imagemap') || r.path.includes('list')) {
                console.log('[路由調試]   📍', r.path, 'name:', r.name)
              }
            })

            this.setRoutes(rewriteRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(defaultRoutes)
            resolve(rewriteRoutes)
          })
        })
      }
    }
  })

// 遍歷後台傳來的路由字串，轉換為元件物件
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    // 跳過外部連結（以 http 開頭的路徑不是 Vue 路由）
    if (route.path && route.path.startsWith('http')) {
      console.log('[filterAsyncRouter] 🚫 Skipping external link:', route.path)
      return false
    }
    
    // 修正路由名稱重複問題：Marketing -> User 與 System -> User 名稱衝突
    // 同時配合組件名稱 LineUser 以確保 keep-alive 生效
    if (route.path === 'line/user' && route.name === 'User') {
      route.name = 'LineUser'
    }

    // 修正路由名稱重複問題：LINE Config 與 System Config 名稱衝突
    if (route.path === 'line/config' && route.name === 'Config') {
      route.name = 'LineConfig'
      console.log('[filterAsyncRouter] 🔧 Renamed line/config route from "Config" to "LineConfig"')
    }

    // 修正路由名稱：LINE Imagemap 需要唯一名稱（組件名稱：LineImagemap）
    if (route.component === 'line/imagemap/index') {
      route.name = 'LineImagemap'
      console.log('[filterAsyncRouter] 🔧 Renamed imagemap route to "LineImagemap"')
    }

    // 修正路由名稱：訊息範本列表（組件名稱：LineTemplate）
    if (route.component === 'line/template/index') {
      route.name = 'LineTemplate'
      console.log('[filterAsyncRouter] 🔧 Renamed template list route to "LineTemplate"')
    }

    // 修正路由名稱：標籤模組 - LINE 標籤列表（避免與其他 List 衝突）
    if (route.component === 'tag/line/list/index') {
      route.name = 'LineTagList'
      console.log('[filterAsyncRouter] 🔧 Renamed tag/line/list route to "LineTagList"')
    }

    // 修正路由名稱：標籤模組 - LINE 使用者貼標
    if (route.component === 'tag/line/bindUser/index') {
      route.name = 'LineTagBindUser'
      console.log('[filterAsyncRouter] 🔧 Renamed tag/line/bindUser route to "LineTagBindUser"')
    }

    // 修正路由名稱：標籤模組 - 庫存標籤列表
    if (route.component === 'tag/inventory/list/index') {
      route.name = 'InvTagList'
      console.log('[filterAsyncRouter] 🔧 Renamed tag/inventory/list route to "InvTagList"')
    }

    // 修正路由名稱：標籤模組 - 庫存物品貼標
    if (route.component === 'tag/inventory/bindItem/index') {
      route.name = 'InvTagBindItem'
      console.log('[filterAsyncRouter] 🔧 Renamed tag/inventory/bindItem route to "InvTagBindItem"')
    }

    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 元件特殊處理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach((el, index) => {
    console.log('[filterChildren] 📂 Processing child:', el.name, 'path:', el.path, 'component:', el.component, 'lastRouter:', lastRouter?.path)
    if (el.children && el.children.length) {
      // 計算當前元素的完整路徑
      const currentFullPath = lastRouter ? (lastRouter.path + '/' + el.path) : el.path
      // 建立一個帶有完整路徑的虛擬路由物件
      const virtualRouter = { path: currentFullPath }
      // 遞歸處理子路由
      children = children.concat(filterChildren(el.children, virtualRouter))
      return
    }
    if (lastRouter) {
      el.path = lastRouter.path + '/' + el.path
      console.log('[filterChildren] 📝 Modified path to:', el.path)
    }
    children.push(el)
  })
  return children
}

// 動態路由遍歷，驗證是否具備權限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  let res
  console.log('[loadView] 🔍 Loading view:', view)

  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]

    // 精確匹配
    if (dir === view) {
      console.log('[loadView] ✅ Exact match:', view, '->', path)
      res = modules[path]  // 直接返回 modules[path]，它本身就是返回 Promise 的函數
      break
    }

    // 容錯匹配：後端返回 system/user，前端檔案 system/user/index.vue
    if (dir === view + '/index') {
      console.log('[loadView] ✅ Index fallback:', view, '->', path)
      res = modules[path]
      break
    }

    // 容錯匹配：後端返回 system/user/index，前端檔案 system/user.vue (較少見但可能)
    if (dir + '/index' === view) {
      console.log('[loadView] ✅ Reverse fallback:', view, '->', path)
      res = modules[path]
      break
    }
  }

  if (!res) {
    console.error(`[loadView] ❌ FAILED to find component for view: "${view}"`)
    console.error('[loadView] Available modules (first 15):', Object.keys(modules).slice(0, 15).map(k => k.split('views/')[1]))
  }

  return res
}

export default usePermissionStore
