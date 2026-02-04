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

// 免登入白名單
// 商城前台：首頁、商品列表、商品詳情、分類、購物車允許未登入訪問
// 結帳和會員中心需要登入
const whiteList = [
  '/login',
  '/register',
  '/mall',
  '/mall/login',
  '/mall/register',
  '/mall/products',
  '/mall/product/**',
  '/mall/category',
  '/mall/articles',
  '/mall/article/**',
  '/mall/cart'
]

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

// 商城：僅結帳與會員中心需要登入，其餘商城路徑直接放行
const isMallPublicPath = (path) => {
  if (!path.startsWith('/mall')) return false
  if (path.startsWith('/mall/checkout')) return false
  if (path.startsWith('/mall/member')) return false
  return true
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  const isMallPath = to.path.startsWith('/mall')
  const memberToken = getMemberToken()
  const adminToken = getToken()

  if (to.meta.title) {
    useSettingsStore().setTitle(to.meta.title)
  }

  if (isMallPath) {
    if (memberToken || isWhiteList(to.path) || isMallPublicPath(to.path)) {
      next()
    } else {
      next(`/mall/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
    return
  }

  if (adminToken) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (to.path.startsWith('/mall')) {
      // 商城路徑不需載入後台權限路由
      next()
    } else if (isWhiteList(to.path) || isMallPublicPath(to.path)) {
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
