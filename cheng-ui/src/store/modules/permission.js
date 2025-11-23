import auth from '@/plugins/auth'
import router, {constantRoutes, dynamicRoutes} from '@/router'
import {getRouters} from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      state.defaultRoutes = constantRoutes.concat(routes)
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      state.topbarRouters = routes
    },
    SET_SIDEBAR_ROUTERS: (state, routes) => {
      state.sidebarRouters = routes
    },
  },
  actions: {
    // 產生路由
    GenerateRoutes({ commit }) {
      return new Promise(resolve => {
        // 向後端請求路由數據
        getRouters().then(res => {
          console.log('[Permission] 後端返回的路由數據:', res.data)
          
          // 檢查路由名稱是否有重複
          if (res.data && Array.isArray(res.data)) {
            const systemRoute = res.data.find(r => r.path === '/system')
            if (systemRoute && systemRoute.children) {
              console.log('[Permission] ========== 後端返回的 System 子路由 ==========')
              systemRoute.children.forEach((c, i) => {
                console.log(`  ${i+1}. path="${c.path}" name="${c.name}" component="${c.component}"`)
              })
            }
            
            // 檢查所有路由名稱
            const allNames = new Map()
            res.data.forEach(parent => {
              if (parent.children) {
                parent.children.forEach(child => {
                  if (child.name) {
                    if (allNames.has(child.name)) {
                      console.error(`[Permission] ❌ 路由名稱重複: "${child.name}" 出現在 ${allNames.get(child.name)} 和 ${parent.path}`)
                    } else {
                      allNames.set(child.name, parent.path)
                    }
                  }
                })
              }
            })
          }
          
          const sdata = JSON.parse(JSON.stringify(res.data))
          const rdata = JSON.parse(JSON.stringify(res.data))
          const sidebarRoutes = filterAsyncRouter(sdata)
          const rewriteRoutes = filterAsyncRouter(rdata, false, true)
          console.log('[Permission] 處理後的路由:', rewriteRoutes)
          const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
          
          // Vue Router 4: 使用 addRoute 代替 addRoutes
          asyncRoutes.forEach(route => {
            router.addRoute(route)
          })
          
          // Vue Router 4: 直接添加完整路由對象（含 children）
          rewriteRoutes.forEach(route => {
            console.log('[Permission] ========== 添加路由 ==========')
            console.log('[Permission] 路由 path:', route.path)
            console.log('[Permission] 路由 name:', route.name)
            console.log('[Permission] 路由 component:', route.component)
            console.log('[Permission] 子路由數量:', route.children ? route.children.length : 0)
            
            if (route.children && route.children.length > 0) {
              console.log('[Permission] 子路由詳情:')
              route.children.forEach((c, i) => {
                console.log(`  ${i+1}. path="${c.path}" name="${c.name}" component=${c.component ? '✓' : '✗'}`)
              })
            }
            
            // Vue Router 4: 直接添加整個路由對象（包括 children）
            router.addRoute(route)
            console.log('[Permission] ✅ 已添加路由:', route.path)
          })
          
          // ========== 最終驗證：檢查路由註冊 ==========
          console.log('[Permission] ========== 路由註冊完成 ==========')
          
          const allRoutes = router.getRoutes()
          console.log('[Permission] 總路由數:', allRoutes.length)
          
          // 檢查父路由
          const parentRoutes = allRoutes.filter(r => r.children && r.children.length > 0)
          console.log('[Permission] 帶子路由的父路由:', parentRoutes.map(r => ({
            path: r.path,
            name: r.name,
            childrenCount: r.children.length
          })))
          
          // 檢查 system 路由結構
          const systemRoute = allRoutes.find(r => r.path === '/system')
          if (systemRoute) {
            console.log('[Permission] ✅ System 路由已註冊:', {
              path: systemRoute.path,
              name: systemRoute.name,
              component: systemRoute.components?.default?.name || '未知',
              hasChildren: systemRoute.children?.length || 0
            })
          } else {
            console.error('[Permission] ❌ System 路由未找到')
          }
          
          // 檢查 router.getRoutes() 中是否有子路由的獨立記錄
          console.log('[Permission] ========== 檢查 router.getRoutes() 中的 System 子路由 ==========')
          const systemChildren = allRoutes.filter(r => r.name && ['User', 'Role', 'Dict', 'Config', 'Notice', 'Operlog', 'Logininfor'].includes(r.name))
          console.log('[Permission] 找到的子路由記錄:')
          systemChildren.forEach((r, i) => {
            console.log(`  ${i+1}. name="${r.name}" path="${r.path}" hasComponent=${!!r.components?.default}`)
          })
          console.log('[Permission] 總共找到', systemChildren.length, '個子路由記錄')
          
          // 測試路由解析
          console.log('[Permission] 測試路由解析:')
          const testPaths = ['/system/user', '/system/config', '/system/dict']
          testPaths.forEach(path => {
            const resolved = router.resolve(path)
            console.log(`  ${path} → matched:`, resolved.matched.length > 0 ? '✓' : '✗', 
              resolved.matched.length > 0 ? resolved.matched.map(r => r.name).join(' > ') : 'NOT FOUND')
          })
          
          // Vue Router 4: 通配符路由語法改變
          rewriteRoutes.push({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
          router.addRoute({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
          
          commit('SET_ROUTES', rewriteRoutes)
          commit('SET_SIDEBAR_ROUTERS', constantRoutes.concat(sidebarRoutes))
          commit('SET_DEFAULT_ROUTES', sidebarRoutes)
          commit('SET_TOPBAR_ROUTES', sidebarRoutes)
          resolve(rewriteRoutes)
        })
      })
    }
  }
}

// 遍歷後台傳來的路由字串，轉換為元件物件
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
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
      // Vue Router 4: 確保子路由名稱唯一，添加父路由前綴
      // 只為頂層路由的直接子路由添加前綴（避免過度嵌套）
      if (!lastRouter && route.name) {
        route.children.forEach(child => {
          if (child.name) {
            const originalName = child.name
            child.name = `${route.name}${child.name}`
            console.log(`[Permission] 重命名子路由: ${originalName} → ${child.name}`)
          }
        })
      }
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
  childrenMap.forEach(el => {
    // Vue Router 4: 對於三層嵌套路由（ParentView），需要拼接父路由 path
    if (el.children && el.children.length && el.component === 'ParentView') {
      // 深拷貝子路由，避免修改原始對象導致其他父路由的同名子路由被覆蓋
      const clonedChildren = el.children.map(child => {
        const cloned = { ...child }
        // 拼接路徑：log/operlog
        cloned.path = el.path + '/' + child.path
        return cloned
      })
      children = children.concat(filterChildren(clonedChildren, el))
    } else {
      children.push(el)
    }
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
    } else {
      // 沒有權限限制的路由也允許訪問（例如 inventory 相關路由）
      res.push(route)
    }
  })
  return res
}

export const loadView = (view) => {
  // Vite 環境使用 import.meta.glob 預編譯路徑
  // 後端返回的 view 格式如：'system/user/index' 或 'system/role/index'
  let componentPath = view
  if (!componentPath.endsWith('.vue')) {
    componentPath = `${view}.vue`
  }
  
  // Vite 要求使用絕對路徑
  const fullPath = `/src/views/${componentPath}`
  
  console.log('[loadView] 載入組件:', view, '→', fullPath)
  
  // 使用動態導入
  return () => import(/* @vite-ignore */ fullPath).catch(err => {
    console.error('[loadView] 組件載入失敗:', fullPath, err)
    throw err
  })
}

export default permission
