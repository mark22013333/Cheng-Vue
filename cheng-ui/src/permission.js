import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
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
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    /* has token*/
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
        // 判斷當前使用者是否已拉取完user_info訊息
        useUserStore().getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes().then(accessRoutes => {
            // 根據roles權限產生可訪問的路由表
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route) // 動態新增可訪問路由表
              }
            })
            next({ ...to, replace: true }) // hack方法 確保addRoutes已完成
          })
        }).catch(err => {
          useUserStore().logOut().then(() => {
            ElMessage.error(err)
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    // 沒有token
    if (isWhiteList(to.path) || isMallPublicPath(to.path)) {
      // 在免登入白名單，直接進入
      next()
    } else {
      // 判斷是否在商城頁面，若是則重定向到商城登入頁
      if (to.path.startsWith('/mall')) {
        next(`/mall/login?redirect=${to.fullPath}`)
      } else {
        next(`/login?redirect=${to.fullPath}`) // 否則全部重定向到後台登入頁
      }
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
