import { defineStore } from 'pinia'
import auth from '@/plugins/auth'
import router from '@/router'
import { getRouters } from '@/api/menu'
import {
  MONITOR_JOB_LIST,
  SHOP_PRODUCT_ADD,
  SHOP_PRODUCT_EDIT,
  SYSTEM_DICT_LIST,
  SYSTEM_ROLE_EDIT,
  SYSTEM_USER_EDIT,
  TOOL_GEN_EDIT
} from '@/constants/permissions'

// 匹配views裡面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

// 延遲載入 Layout 組件，避免循環依賴
const layoutModules = {
  Layout: () => import('@/layout/index'),
  ParentView: () => import('@/components/ParentView'),
  InnerLink: () => import('@/layout/components/InnerLink')
}

// 後台管理基礎路由（用於側邊欄顯示）
const getAdminBaseRoutes = () => {
  return [
    {
      path: '/cadm',
      redirect: '/cadm/index',
      children: [
        {
          path: 'index',
          name: 'AdminIndex',
          meta: { title: '首頁', icon: 'dashboard', affix: true }
        }
      ]
    }
  ]
}

// 後台動態路由配置（使用延遲載入避免循環依賴）
const adminDynamicRoutes = [
  {
    path: '/cadm/system/user-auth',
    component: layoutModules.Layout,
    hidden: true,
    permissions: [SYSTEM_USER_EDIT],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/cadm/system/user' }
      }
    ]
  },
  {
    path: '/cadm/system/role-auth',
    component: layoutModules.Layout,
    hidden: true,
    permissions: [SYSTEM_ROLE_EDIT],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配使用者', activeMenu: '/cadm/system/role' }
      }
    ]
  },
  {
    path: '/cadm/system/dict-data',
    component: layoutModules.Layout,
    hidden: true,
    permissions: [SYSTEM_DICT_LIST],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典資料', activeMenu: '/cadm/system/dict' }
      }
    ]
  },
  {
    path: '/cadm/monitor/job-log',
    component: layoutModules.Layout,
    hidden: true,
    permissions: [MONITOR_JOB_LIST],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '呼叫日誌', activeMenu: '/cadm/monitor/job' }
      }
    ]
  },
  {
    path: '/cadm/tool/gen-edit',
    component: layoutModules.Layout,
    hidden: true,
    permissions: [TOOL_GEN_EDIT],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改產生配置', activeMenu: '/cadm/tool/gen' }
      }
    ]
  },
  {
    path: '/cadm/shop/product-edit',
    component: layoutModules.Layout,
    hidden: true,
    permissions: [SHOP_PRODUCT_ADD, SHOP_PRODUCT_EDIT],
    children: [
      {
        path: 'index/:productId?',
        component: () => import('@/views/shop/product/edit'),
        name: 'ShopProductEdit',
        meta: { title: '編輯商品', activeMenu: '/cadm/shop/product' }
      }
    ]
  }
]

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
        this.routes = getAdminBaseRoutes().concat(routes)
      },
      setDefaultRoutes(routes) {
        this.defaultRoutes = getAdminBaseRoutes().concat(routes)
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

            // 為後端路由加上 /cadm 前綴
            const addCadmPrefix = (routes) => {
              return routes.map(route => {
                const newRoute = { ...route }
                if (newRoute.path && !newRoute.path.startsWith('/cadm') && !newRoute.path.startsWith('http')) {
                  newRoute.path = newRoute.path.startsWith('/') ? `/cadm${newRoute.path}` : `/cadm/${newRoute.path}`
                }
                if (newRoute.redirect && !newRoute.redirect.startsWith('/cadm')) {
                  newRoute.redirect = newRoute.redirect.startsWith('/') ? `/cadm${newRoute.redirect}` : `/cadm/${newRoute.redirect}`
                }
                return newRoute
              })
            }

            const sidebarRoutes = filterAsyncRouter(addCadmPrefix(sdata))
            const rewriteRoutes = filterAsyncRouter(addCadmPrefix(rdata), false, true)
            const defaultRoutes = filterAsyncRouter(addCadmPrefix(defaultData))
            const asyncRoutes = filterDynamicRoutes(adminDynamicRoutes)

            // 新增動態路由
            asyncRoutes.forEach(route => {
              router.addRoute(route)
            })

            // 新增後端返回的路由
            rewriteRoutes.forEach(route => {
              console.log('[路由調試] 🚀 新增後端路由:', route.path, 'name:', route.name)
              router.addRoute(route)
            })

            this.setRoutes(rewriteRoutes)
            this.setSidebarRouters(getAdminBaseRoutes().concat(sidebarRoutes))
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
    // 跳過外部連結
    if (route.path && route.path.startsWith('http')) {
      return false
    }

    // 修正路由名稱重複問題
    if (route.path === 'line/user' && route.name === 'User') {
      route.name = 'LineUser'
    }
    if (route.path === 'line/config' && route.name === 'Config') {
      route.name = 'LineConfig'
    }
    if (route.component === 'line/imagemap/index') {
      route.name = 'LineImagemap'
    }
    if (route.component === 'line/template/index') {
      route.name = 'LineTemplate'
    }
    if (route.component === 'tag/line/list/index') {
      route.name = 'LineTagList'
    }
    if (route.component === 'tag/line/bindUser/index') {
      route.name = 'LineTagBindUser'
    }
    if (route.component === 'tag/inventory/list/index') {
      route.name = 'InvTagList'
    }
    if (route.component === 'tag/inventory/bindItem/index') {
      route.name = 'InvTagBindItem'
    }
    if (route.component === 'tag/group/line/index') {
      route.name = 'LineTagGroup'
    }
    if (route.component === 'tag/group/inventory/index') {
      route.name = 'InvTagGroup'
    }

    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      if (route.component === 'Layout') {
        route.component = layoutModules.Layout
      } else if (route.component === 'ParentView') {
        route.component = layoutModules.ParentView
      } else if (route.component === 'InnerLink') {
        route.component = layoutModules.InnerLink
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
    if (el.children && el.children.length) {
      const currentFullPath = lastRouter ? (lastRouter.path + '/' + el.path) : el.path
      const virtualRouter = { path: currentFullPath }
      children = children.concat(filterChildren(el.children, virtualRouter))
      return
    }
    if (lastRouter) {
      el.path = lastRouter.path + '/' + el.path
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
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]
    if (dir === view) {
      res = modules[path]
      break
    }
    if (dir === view + '/index') {
      res = modules[path]
      break
    }
    if (dir + '/index' === view) {
      res = modules[path]
      break
    }
  }
  return res
}

export default usePermissionStore
