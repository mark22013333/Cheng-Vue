# 06. 前端頁面設計（消費者端）

---

## 6.1 頁面結構

```
cheng-shop-ui/
└── src/views/
    ├── home/                      # 首頁
    │   └── index.vue
    ├── category/                  # 分類
    │   ├── index.vue              # 分類列表
    │   └── products.vue           # 分類商品
    ├── product/                   # 商品
    │   ├── list.vue               # 商品列表
    │   ├── detail.vue             # 商品詳情
    │   └── search.vue             # 搜尋結果
    ├── cart/                      # 購物車
    │   └── index.vue
    ├── checkout/                  # 結帳
    │   ├── index.vue              # 結帳頁
    │   └── result.vue             # 結帳結果
    ├── order/                     # 訂單
    │   ├── list.vue               # 訂單列表
    │   └── detail.vue             # 訂單詳情
    ├── member/                    # 會員中心
    │   ├── index.vue              # 會員首頁
    │   ├── profile.vue            # 個人資料
    │   ├── password.vue           # 修改密碼
    │   └── points.vue             # 點數紀錄
    └── auth/                      # 認證
        ├── login.vue              # 登入
        ├── register.vue           # 註冊
        └── callback.vue           # 第三方回調
```

---

## 6.2 頁面規格

### 6.2.1 首頁 (home/index.vue)

**區塊結構**

| 區塊 | 說明 | API |
|-----|-----|-----|
| Header | 導航列、搜尋、購物車圖示 | - |
| Banner | 輪播圖 | `GET /home/banners` |
| 形象區 | 品牌介紹區塊 | `GET /home/blocks` |
| 熱門商品 | 熱銷商品展示 | `GET /home/hot-products` |
| 新品上架 | 最新商品 | `GET /home/new-products` |
| 推薦商品 | 精選推薦 | `GET /home/recommend-products` |
| Footer | 聯絡資訊、社群連結 | - |

**輪播組件規格**

```vue
<template>
  <el-carousel :interval="5000" height="400px">
    <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
      <img 
        :src="banner.imageUrl" 
        :alt="banner.title"
        @click="handleBannerClick(banner)"
      />
    </el-carousel-item>
  </el-carousel>
</template>
```

---

### 6.2.2 商品列表頁 (product/list.vue)

**功能**

- 分類篩選
- 排序（銷量、價格、上架時間）
- 分頁載入
- 列表/卡片切換

**篩選參數**

```typescript
interface ProductQuery {
  categoryId?: number;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  sortField?: 'sales' | 'price' | 'createTime';
  sortOrder?: 'asc' | 'desc';
  pageNum: number;
  pageSize: number;
}
```

---

### 6.2.3 商品詳情頁 (product/detail.vue)

**區塊結構**

| 區塊 | 說明 |
|-----|-----|
| 商品圖片 | 主圖 + 縮圖輪播 |
| 商品資訊 | 標題、價格、銷量 |
| SKU 選擇 | 規格選擇（如顏色、尺寸） |
| 數量選擇 | 購買數量 +/- |
| 操作按鈕 | 加入購物車、立即購買 |
| 商品描述 | 富文本詳情 |

**SKU 選擇器**

```vue
<template>
  <div class="sku-selector">
    <div v-for="sku in skuList" :key="sku.skuId" class="sku-item">
      <el-radio-group v-model="selectedSkuId">
        <el-radio :label="sku.skuId">
          {{ sku.skuName }} - NT$ {{ sku.price }}
        </el-radio>
      </el-radio-group>
    </div>
  </div>
</template>
```

---

### 6.2.4 購物車頁 (cart/index.vue)

**功能**

- 商品列表（圖片、名稱、SKU、單價、數量、小計）
- 全選/單選
- 修改數量
- 刪除商品
- 金額統計
- 前往結帳

**資料結構**

```typescript
interface CartItem {
  cartId: number;
  skuId: number;
  productName: string;
  skuName: string;
  skuImage: string;
  price: number;
  quantity: number;
  isSelected: boolean;
  stock: number;  // 用於驗證庫存
}
```

---

### 6.2.5 結帳頁 (checkout/index.vue)

**區塊結構**

| 區塊 | 說明 |
|-----|-----|
| 收件資訊 | 姓名、電話、地址 |
| 商品清單 | 結帳商品列表 |
| 付款方式 | 選擇付款方式 |
| 物流方式 | 選擇配送方式 |
| 買家備註 | 備註輸入框 |
| 金額明細 | 商品金額、運費、折扣、總計 |
| 提交按鈕 | 提交訂單 |

**表單驗證**

```javascript
const rules = {
  receiverName: [
    { required: true, message: '請輸入收件人姓名', trigger: 'blur' }
  ],
  receiverMobile: [
    { required: true, message: '請輸入收件人電話', trigger: 'blur' },
    { pattern: /^09\d{8}$/, message: '請輸入正確的手機號碼', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '請輸入收件地址', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '請選擇付款方式', trigger: 'change' }
  ],
  shippingMethod: [
    { required: true, message: '請選擇配送方式', trigger: 'change' }
  ]
}
```

---

### 6.2.6 訂單列表頁 (order/list.vue)

**功能**

- 訂單狀態篩選（全部、待付款、待出貨、待收貨、已完成、已取消）
- 訂單卡片展示
- 分頁載入

**訂單狀態標籤**

```vue
<template>
  <el-tag :type="getStatusType(order.status)">
    {{ order.statusText }}
  </el-tag>
</template>

<script setup>
const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    PAID: 'primary',
    PROCESSING: 'primary',
    SHIPPED: 'success',
    DELIVERED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'info',
    REFUNDING: 'danger',
    REFUNDED: 'danger'
  }
  return map[status] || 'info'
}
</script>
```

---

### 6.2.7 訂單詳情頁 (order/detail.vue)

**區塊結構**

| 區塊 | 說明 |
|-----|-----|
| 訂單狀態 | 當前狀態、狀態流程圖 |
| 物流資訊 | 物流單號、物流軌跡 |
| 收件資訊 | 收件人、電話、地址 |
| 商品列表 | 訂單商品明細 |
| 金額明細 | 商品金額、運費、折扣、總計 |
| 操作按鈕 | 取消訂單、確認收貨、再次購買 |

---

### 6.2.8 會員中心 (member/index.vue)

**功能模組**

| 模組 | 說明 |
|-----|-----|
| 會員資訊卡 | 頭像、暱稱、等級、點數 |
| 訂單概覽 | 待付款、待出貨、待收貨數量 |
| 快捷入口 | 我的訂單、個人資料、修改密碼、登出 |

---

## 6.3 Pinia 狀態管理

### 6.3.1 會員狀態 (stores/member.js)

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMemberInfo, login, logout } from '@/api/member'

export const useMemberStore = defineStore('member', () => {
  const token = ref(localStorage.getItem('shop_token') || '')
  const memberInfo = ref(null)
  
  const isLoggedIn = computed(() => !!token.value)
  
  async function loginAction(credentials) {
    const res = await login(credentials)
    token.value = res.data.token
    memberInfo.value = res.data.memberInfo
    localStorage.setItem('shop_token', res.data.token)
    return res
  }
  
  async function logoutAction() {
    await logout()
    token.value = ''
    memberInfo.value = null
    localStorage.removeItem('shop_token')
  }
  
  async function fetchMemberInfo() {
    if (!token.value) return
    const res = await getMemberInfo()
    memberInfo.value = res.data
  }
  
  return { token, memberInfo, isLoggedIn, loginAction, logoutAction, fetchMemberInfo }
})
```

### 6.3.2 購物車狀態 (stores/cart.js)

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartItems, addToCart, updateCartQuantity, removeFromCart } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref([])
  
  const cartCount = computed(() => 
    cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  )
  
  const selectedItems = computed(() => 
    cartItems.value.filter(item => item.isSelected)
  )
  
  const totalAmount = computed(() => 
    selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )
  
  async function fetchCartItems() {
    const res = await getCartItems()
    cartItems.value = res.data
  }
  
  async function addItem(skuId, quantity) {
    await addToCart(skuId, quantity)
    await fetchCartItems()
  }
  
  async function updateQuantity(cartId, quantity) {
    await updateCartQuantity(cartId, quantity)
    const item = cartItems.value.find(i => i.cartId === cartId)
    if (item) item.quantity = quantity
  }
  
  async function removeItem(cartId) {
    await removeFromCart(cartId)
    cartItems.value = cartItems.value.filter(i => i.cartId !== cartId)
  }
  
  return { cartItems, cartCount, selectedItems, totalAmount, fetchCartItems, addItem, updateQuantity, removeItem }
})
```

---

## 6.4 路由配置

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useMemberStore } from '@/stores/member'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/home/index.vue') },
  { path: '/category', name: 'Category', component: () => import('@/views/category/index.vue') },
  { path: '/category/:id', name: 'CategoryProducts', component: () => import('@/views/category/products.vue') },
  { path: '/products', name: 'ProductList', component: () => import('@/views/product/list.vue') },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/product/detail.vue') },
  { path: '/search', name: 'Search', component: () => import('@/views/product/search.vue') },
  { path: '/cart', name: 'Cart', component: () => import('@/views/cart/index.vue'), meta: { requiresAuth: true } },
  { path: '/checkout', name: 'Checkout', component: () => import('@/views/checkout/index.vue'), meta: { requiresAuth: true } },
  { path: '/checkout/result', name: 'CheckoutResult', component: () => import('@/views/checkout/result.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'OrderList', component: () => import('@/views/order/list.vue'), meta: { requiresAuth: true } },
  { path: '/order/:id', name: 'OrderDetail', component: () => import('@/views/order/detail.vue'), meta: { requiresAuth: true } },
  { path: '/member', name: 'Member', component: () => import('@/views/member/index.vue'), meta: { requiresAuth: true } },
  { path: '/member/profile', name: 'Profile', component: () => import('@/views/member/profile.vue'), meta: { requiresAuth: true } },
  { path: '/member/password', name: 'Password', component: () => import('@/views/member/password.vue'), meta: { requiresAuth: true } },
  { path: '/login', name: 'Login', component: () => import('@/views/auth/login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/register.vue') },
  { path: '/auth/callback', name: 'AuthCallback', component: () => import('@/views/auth/callback.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const memberStore = useMemberStore()
  if (to.meta.requiresAuth && !memberStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
```

---

## 6.5 響應式設計

| 斷點 | 寬度 | 佈局 |
|-----|-----|-----|
| xs | < 768px | 單欄、隱藏側邊欄 |
| sm | 768px - 991px | 雙欄 |
| md | 992px - 1199px | 三欄 |
| lg | ≥ 1200px | 四欄 |

**商品卡片響應式**

```css
.product-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, 1fr);
}

@media (min-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
```
