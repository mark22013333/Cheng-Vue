import router, { isAdminPath } from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { getMemberToken } from '@/utils/memberAuth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

// ============================================================================
// 白名單配置
// ============================================================================

// 後台管理白名單（不需登入）
const adminWhiteList = ['/cadm/login', '/cadm/register']

// 商城白名單（公開頁面）
const shopWhiteList = [
  '/',
  '/login',
  '/register',
  '/products',
  '/product/**',
  '/category',
  '/articles',
  '/article/**',
  '/cart',
  '/terms',
  '/privacy'
]

// 商城需要登入的路徑
const shopProtectedPaths = ['/checkout', '/member', '/order-success', '/payment-result']

// ============================================================================
// 輔助函數
// ============================================================================

const isInWhiteList = (path) => {
  if (isAdminPath(path)) {
    return adminWhiteList.includes(path)
  }
  return shopWhiteList.some(pattern => isPathMatch(pattern, path))
}

const isShopProtectedPath = (path) => {
  return shopProtectedPaths.some(p => path.startsWith(p))
}

// ============================================================================
// 路由守衛
// ============================================================================

router.beforeEach((to, from, next) => {
  NProgress.start()

  // 設定頁面標題
  if (to.meta.title) {
    useSettingsStore().setTitle(to.meta.title)
  }

  const targetPath = to.path

  // ========================================
  // 後台管理路由 (/cadm/*)
  // ========================================
  if (isAdminPath(targetPath)) {
    const adminToken = getToken()

    if (adminToken) {
      // 已登入
      if (targetPath === '/cadm/login') {
        // 已登入但訪問登入頁，跳轉到首頁
        next({ path: '/cadm/index' })
        NProgress.done()
      } else if (isInWhiteList(targetPath)) {
        next()
      } else {
        // 需要載入動態路由
        if (useUserStore().roles.length === 0) {
          isRelogin.show = true
          useUserStore().getInfo().then(() => {
            isRelogin.show = false
            usePermissionStore().generateRoutes().then(accessRoutes => {
              // 動態路由需要加上 /cadm 前綴
              accessRoutes.forEach(route => {
                if (!isHttp(route.path)) {
                  // 將原始路由路徑轉換為 /cadm 前綴
                  const adminRoute = {
                    ...route,
                    path: route.path.startsWith('/cadm') ? route.path : `/cadm${route.path}`
                  }
                  router.addRoute(adminRoute)
                }
              })
              next({ ...to, replace: true })
            })
          }).catch(err => {
            useUserStore().logOut().catch(() => {}).finally(() => {
              ElMessage.error(err)
              next({ path: '/cadm/login' })
            })
          })
        } else {
          next()
        }
      }
    } else {
      // 未登入
      if (isInWhiteList(targetPath)) {
        next()
      } else {
        next(`/cadm/login?redirect=${encodeURIComponent(to.fullPath)}`)
        NProgress.done()
      }
    }
    return
  }

  // ========================================
  // 商城路由 (非 /cadm/*)
  // ========================================
  const memberToken = getMemberToken()

  if (isShopProtectedPath(targetPath)) {
    // 需要登入的頁面
    if (memberToken) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  } else {
    // 公開頁面，直接放行
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})
