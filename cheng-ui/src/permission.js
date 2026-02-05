import router from './router'
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

// 建置時決定應用模式
const appMode = import.meta.env.VITE_APP_MODE || 'shop'
const isAdminMode = appMode === 'admin'

// 後台管理白名單
const adminWhiteList = ['/login', '/register']

// 商城白名單（公開頁面，不需登入）
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
const shopProtectedPaths = ['/checkout', '/member', '/order-success']

const isWhiteList = (path) => {
  const list = isAdminMode ? adminWhiteList : shopWhiteList
  return list.some(pattern => isPathMatch(pattern, path))
}

const isProtectedPath = (path) => {
  return shopProtectedPaths.some(p => path.startsWith(p))
}

router.beforeEach((to, from, next) => {
  NProgress.start()

  if (to.meta.title) {
    useSettingsStore().setTitle(to.meta.title)
  }

  // 商城模式
  if (!isAdminMode) {
    const memberToken = getMemberToken()
    // 需要登入的頁面
    if (isProtectedPath(to.path)) {
      if (memberToken) {
        next()
      } else {
        next(`/login?redirect=${to.fullPath}`)
        NProgress.done()
      }
    } else {
      // 公開頁面直接放行
      next()
    }
    return
  }

  // 後台管理模式
  const adminToken = getToken()

  if (adminToken) {
    if (to.path === '/login') {
      next({ path: '/index' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        useUserStore().getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes().then(accessRoutes => {
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route)
              }
            })
            next({ ...to, replace: true })
          })
        }).catch(err => {
          useUserStore().logOut().catch(() => {}).finally(() => {
            ElMessage.error(err)
            next({ path: '/login' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    if (isWhiteList(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
