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
  console.log('[Permission] beforeEach - from:', from.path, 'to:', to.path);
  NProgress.start()
  if (getToken()) {
    console.log('[Permission] Has token');
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      console.log('[Permission] Already logged in, redirect to /');
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      console.log('[Permission] In whitelist, allow');
      next()
    } else {
      if (store.getters.roles.length === 0) {
        console.log('[Permission] No roles, fetching user info...');
        isRelogin.show = true
        // 判斷目前使用者是否已拉取完user_info訊息
        store.dispatch('GetInfo').then(() => {
          console.log('[Permission] GetInfo success, generating routes...');
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            console.log('[Permission] GenerateRoutes success, routes:', accessRoutes.length);
            // 路由已在 store 中添加完成，直接重新導航
            next({...to, replace: true}) // hack方法 確認addRoute已完成
          }).catch(err => {
            console.error('[Permission] GenerateRoutes error:', err);
            isRelogin.show = false
            NProgress.done()
          })
        }).catch(err => {
          console.error('[Permission] GetInfo error:', err);
          isRelogin.show = false
          store.dispatch('LogOut').then(() => {
            ElMessage.error(err)
            next({ path: '/' })
            NProgress.done()
          })
        })
      } else {
        console.log('[Permission] Has roles, allow');
        next()
      }
    }
  } else {
    console.log('[Permission] No token');
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
