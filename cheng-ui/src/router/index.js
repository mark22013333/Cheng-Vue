import { createWebHistory, createRouter } from 'vue-router'
/* Layout */
import Layout from '@/layout'
import ShopLayout from '@/layout/ShopLayout.vue'

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

const isAdminHost = typeof window !== 'undefined' && window.location.pathname.startsWith('/cadm')
const defaultHomePath = isAdminHost ? '/index' : '/mall'

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
    redirect: defaultHomePath,
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
  },
  {
    path: '/mall/login',
    component: () => import('@/views/shop-front/auth/login.vue'),
    hidden: true,
    meta: { title: '會員登入' }
  },
  {
    path: '/mall/register',
    component: () => import('@/views/shop-front/auth/register.vue'),
    hidden: true,
    meta: { title: '會員註冊' }
  },
  {
    path: '/mall',
    component: ShopLayout,
    hidden: true,
    children: [
      {
        path: '',
        component: () => import('@/views/shop-front/home/index.vue'),
        name: 'MallHome',
        meta: { title: '商城首頁' }
      },
      {
        path: 'products',
        component: () => import('@/views/shop-front/product/list.vue'),
        name: 'MallProducts',
        meta: { title: '商品列表' }
      },
      {
        path: 'product/:id',
        component: () => import('@/views/shop-front/product/detail.vue'),
        name: 'MallProductDetail',
        meta: { title: '商品詳情' }
      },
      {
        path: 'category',
        component: () => import('@/views/shop-front/product/list.vue'),
        name: 'MallCategory',
        meta: { title: '商品分類' }
      },
      {
        path: 'articles',
        component: () => import('@/views/shop-front/article/list.vue'),
        name: 'MallArticles',
        meta: { title: '文章列表' }
      },
      {
        path: 'terms',
        component: () => import('@/views/shop-front/legal/terms.vue'),
        name: 'MallTerms',
        meta: { title: '服務條款' }
      },
      {
        path: 'privacy',
        component: () => import('@/views/shop-front/legal/privacy.vue'),
        name: 'MallPrivacy',
        meta: { title: '隱私政策' }
      },
      {
        path: 'article/:id',
        component: () => import('@/views/shop-front/article/detail.vue'),
        name: 'MallArticleDetail',
        meta: { title: '文章詳情' }
      },
      {
        path: 'cart',
        component: () => import('@/views/shop-front/cart/index.vue'),
        name: 'MallCart',
        meta: { title: '購物車' }
      },
      {
        path: 'checkout',
        component: () => import('@/views/shop-front/checkout/index.vue'),
        name: 'MallCheckout',
        meta: { title: '結帳' }
      },
      {
        path: 'order-success/:orderNo',
        component: () => import('@/views/shop-front/order/success.vue'),
        name: 'MallOrderSuccess',
        meta: { title: '訂單完成' }
      },
      {
        path: 'payment-result/:orderNo',
        component: () => import('@/views/shop-front/order/payment-result.vue'),
        name: 'MallPaymentResult',
        meta: { title: '付款結果' }
      },
      {
        path: 'member',
        component: () => import('@/views/shop-front/member/index.vue'),
        name: 'MallMember',
        meta: { title: '會員中心' },
        children: [
          {
            path: 'orders',
            component: () => import('@/views/shop-front/member/orders.vue'),
            name: 'MallMemberOrders',
            meta: { title: '我的訂單' }
          },
          {
            path: 'order/:orderNo',
            component: () => import('@/views/shop-front/member/order-detail.vue'),
            name: 'MallMemberOrderDetail',
            meta: { title: '訂單詳情' }
          },
          {
            path: 'address',
            component: () => import('@/views/shop-front/member/address.vue'),
            name: 'MallMemberAddress',
            meta: { title: '收貨地址' }
          },
          {
            path: 'profile',
            component: () => import('@/views/shop-front/member/profile.vue'),
            name: 'MallMemberProfile',
            meta: { title: '個人資料' }
          }
        ]
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
  },
  {
    path: '/shop/product-edit',
    component: Layout,
    hidden: true,
    permissions: ['shop:product:add', 'shop:product:edit'],
    children: [
      {
        path: 'index/:productId?',
        component: () => import('@/views/shop/product/edit'),
        name: 'ShopProductEdit',
        meta: { title: '編輯商品', activeMenu: '/shop/product' }
      }
    ]
  }
]

const routerBase = (typeof window !== 'undefined' && window.location.pathname.startsWith('/cadm')) ? '/cadm' : '/'

const router = createRouter({
  history: createWebHistory(routerBase),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// 非 /cadm 環境：強制只允許 /mall 路徑，避免直接看到後台登入頁
router.beforeEach((to, from, next) => {
  const adminHost = typeof window !== 'undefined' && window.location.pathname.startsWith('/cadm')
  if (!adminHost) {
    const isMallPath = to.path.startsWith('/mall')
    const isAllowed = isMallPath || to.path === '/401' || to.path === '/404'
    if (!isAllowed) {
      return next('/mall')
    }
  } else {
    // /cadm 環境下避免誤進商城路由
    if (to.path.startsWith('/mall')) {
      return next('/index')
    }
  }
  next()
})

export default router
