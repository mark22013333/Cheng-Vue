import { createWebHistory, createRouter } from 'vue-router'

/**
 * 單一應用程式包含商城和後台管理
 * - 商城路由：/ 開頭
 * - 後台路由：/cadm 開頭
 *
 * 注意：Layout 使用延遲載入避免循環依賴
 */

// ============================================================================
// 判斷是否為後台路徑
// ============================================================================
export const isAdminPath = (path) => {
  const p = path || window.location.pathname
  return p.startsWith('/cadm')
}

// ============================================================================
// 後台管理路由（/cadm 前綴）
// ============================================================================
export const adminRoutes = [
  {
    path: '/cadm/redirect/:path(.*)',
    component: () => import('@/layout/index.vue'),
    hidden: true,
    children: [
      {
        path: '',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/cadm/login',
    component: () => import('@/views/login'),
    hidden: true,
    meta: { title: '後台登入' }
  },
  {
    path: '/cadm/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/cadm/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '/cadm',
    component: () => import('@/layout/index.vue'),
    redirect: '/cadm/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'AdminIndex',
        meta: { title: '首頁', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/cadm/user',
    component: () => import('@/layout/index.vue'),
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile/:activeTab?',
        component: () => import('@/views/system/user/profile/index'),
        name: 'AdminProfile',
        meta: { title: '個人中心', icon: 'user' }
      }
    ]
  },
]

// 後台動態路由（基於權限載入）
export const adminDynamicRoutes = [
  {
    path: '/cadm/system/user-auth',
    component: () => import('@/layout/index.vue'),
    hidden: true,
    permissions: ['system:user:edit'],
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
    component: () => import('@/layout/index.vue'),
    hidden: true,
    permissions: ['system:role:edit'],
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
    component: () => import('@/layout/index.vue'),
    hidden: true,
    permissions: ['system:dict:list'],
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
    component: () => import('@/layout/index.vue'),
    hidden: true,
    permissions: ['monitor:job:list'],
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
    component: () => import('@/layout/index.vue'),
    hidden: true,
    permissions: ['tool:gen:edit'],
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
    component: () => import('@/layout/index.vue'),
    hidden: true,
    permissions: ['shop:product:add', 'shop:product:edit'],
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

// ============================================================================
// 商城路由（根路徑）
// ============================================================================
export const shopRoutes = [
  {
    path: '/login',
    component: () => import('@/views/shop-front/auth/login.vue'),
    hidden: true,
    meta: { title: '會員登入' }
  },
  {
    path: '/register',
    component: () => import('@/views/shop-front/auth/register.vue'),
    hidden: true,
    meta: { title: '會員註冊' }
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/layout/ShopLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('@/views/shop-front/home/index.vue'),
        name: 'ShopHome',
        meta: { title: '商城首頁' }
      },
      {
        path: 'products',
        component: () => import('@/views/shop-front/product/list.vue'),
        name: 'ShopProducts',
        meta: { title: '商品列表' }
      },
      {
        path: 'product/:id',
        component: () => import('@/views/shop-front/product/detail.vue'),
        name: 'ShopProductDetail',
        meta: { title: '商品詳情' }
      },
      {
        path: 'category',
        component: () => import('@/views/shop-front/product/list.vue'),
        name: 'ShopCategory',
        meta: { title: '商品分類' }
      },
      {
        path: 'articles',
        component: () => import('@/views/shop-front/article/list.vue'),
        name: 'ShopArticles',
        meta: { title: '文章列表' }
      },
      {
        path: 'terms',
        component: () => import('@/views/shop-front/legal/terms.vue'),
        name: 'ShopTerms',
        meta: { title: '服務條款' }
      },
      {
        path: 'privacy',
        component: () => import('@/views/shop-front/legal/privacy.vue'),
        name: 'ShopPrivacy',
        meta: { title: '隱私政策' }
      },
      {
        path: 'article/:id',
        component: () => import('@/views/shop-front/article/detail.vue'),
        name: 'ShopArticleDetail',
        meta: { title: '文章詳情' }
      },
      {
        path: 'cart',
        component: () => import('@/views/shop-front/cart/index.vue'),
        name: 'ShopCart',
        meta: { title: '購物車' }
      },
      {
        path: 'checkout',
        component: () => import('@/views/shop-front/checkout/index.vue'),
        name: 'ShopCheckout',
        meta: { title: '結帳' }
      },
      {
        path: 'order-success/:orderNo',
        component: () => import('@/views/shop-front/order/success.vue'),
        name: 'ShopOrderSuccess',
        meta: { title: '訂單完成' }
      },
      {
        path: 'payment-result/:orderNo',
        component: () => import('@/views/shop-front/order/payment-result.vue'),
        name: 'ShopPaymentResult',
        meta: { title: '付款結果' }
      },
      {
        path: 'member',
        component: () => import('@/views/shop-front/member/index.vue'),
        name: 'ShopMember',
        meta: { title: '會員中心' },
        children: [
          {
            path: 'orders',
            component: () => import('@/views/shop-front/member/orders.vue'),
            name: 'ShopMemberOrders',
            meta: { title: '我的訂單' }
          },
          {
            path: 'order/:orderNo',
            component: () => import('@/views/shop-front/member/order-detail.vue'),
            name: 'ShopMemberOrderDetail',
            meta: { title: '訂單詳情' }
          },
          {
            path: 'address',
            component: () => import('@/views/shop-front/member/address.vue'),
            name: 'ShopMemberAddress',
            meta: { title: '收貨地址' }
          },
          {
            path: 'profile',
            component: () => import('@/views/shop-front/member/profile.vue'),
            name: 'ShopMemberProfile',
            meta: { title: '個人資料' }
          }
        ]
      }
    ]
  }
]

// ============================================================================
// 404 路由（必須放在最後）
// ============================================================================
export const notFoundRoute = {
  path: '/:pathMatch(.*)*',
  component: () => import('@/views/error/404'),
  hidden: true
}

// ============================================================================
// 合併所有路由
// ============================================================================
const routes = [
  ...shopRoutes,
  ...adminRoutes,
  notFoundRoute
]

const router = createRouter({
  history: createWebHistory('/'),
  routes: routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

export default router
