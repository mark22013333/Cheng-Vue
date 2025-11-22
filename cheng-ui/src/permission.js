import router from './router'
import store from './store'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        // 判斷目前使用者是否已拉取完user_info訊息
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            // 根據roles權限產生可訪問的路由表
            accessRoutes.forEach(route => {
              router.addRoute(route) // 動態新增可訪問路由表
            })
            next({...to, replace: true}) // hack方法 確認addRoute已完成
          })
        }).catch(err => {
            store.dispatch('LogOut').then(() => {
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
    if (isWhiteList(to.path)) {
      // 在免登入白名單，直接進入
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`) // 否則全部重定向到登入頁
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
