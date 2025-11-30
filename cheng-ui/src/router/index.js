import { createWebHistory, createRouter } from 'vue-router'
/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置項
 *
 * hidden: true                     // 當設定 true 的時候該路由不會再側邊欄出現 如401，login等頁面，或者如一些編輯頁面/edit/1
 * alwaysShow: true                 // 當你一個路由下面的 children 聲明的路由大於1個時，自動會變成嵌套的模式--如元件頁面
 *                                  // 只有一個時，會將那個子路由當做根路由顯示在側邊欄--如引導頁面
 *                                  // 若你想不管路由下面的 children 聲明的個數都顯示你的根路由
 *                                  // 你可以設定 alwaysShow: true，这樣它就會忽略之前定義的規則，一直顯示根路由
 * redirect: noRedirect             // 當設定 noRedirect 的時候該路由在麵包屑導航中不可被點擊
 * name:'router-name'               // 設定路由的名字，一定要填寫不然使用<keep-alive>時會出現各种問題
 * query: '{"id": 1, "name": "ry"}' // 訪問路由的預設傳遞參數
 * roles: ['admin', 'common']       // 訪問路由的角色權限
 * permissions: ['a:a:a', 'b:b:b']  // 訪問路由的選單權限
 * meta : {
    noCache: true                   // 如果設定為true，則不會被 <keep-alive> 暫存(預設 false)
    title: 'title'                  // 設定該路由在側邊欄和麵包屑中展示的名字
    icon: 'svg-name'                // 設定該路由的圖標，對應路徑src/assets/icons/svg
    breadcrumb: false               // 如果設定為false，則不會在breadcrumb麵包屑中顯示
    activeMenu: '/system/user'      // 當路由設定了該屬性，則會顯亮相對應的側邊欄。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首頁', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile/:activeTab?',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '個人中心', icon: 'user' }
      }
    ]
  }
]

// 動態路由，基於使用者權限動態去載入
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配使用者', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典資料', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '呼叫日誌', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改產生配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

export default router
