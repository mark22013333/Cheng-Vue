# 05. API 設計規格

---

## 5.1 前台 API（消費者用）

> 路徑前綴：`/api/shop`

### 5.1.1 首頁

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| GET | `/home/banners` | 取得輪播列表 | ❌ |
| GET | `/home/blocks` | 取得首頁區塊 | ❌ |
| GET | `/home/hot-products` | 取得熱門商品 | ❌ |
| GET | `/home/new-products` | 取得新品 | ❌ |
| GET | `/home/recommend-products` | 取得推薦商品 | ❌ |

### 5.1.2 分類

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| GET | `/categories` | 取得分類樹 | ❌ |
| GET | `/categories/{id}` | 取得分類詳情 | ❌ |
| GET | `/categories/{id}/products` | 取得分類商品 | ❌ |

### 5.1.3 商品

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| GET | `/products` | 商品列表（分頁） | ❌ |
| GET | `/products/{id}` | 商品詳情 | ❌ |
| GET | `/products/{id}/skus` | 商品 SKU 列表 | ❌ |
| GET | `/products/search` | 商品搜尋 | ❌ |

### 5.1.4 購物車

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| GET | `/cart` | 取得購物車 | ✅ |
| POST | `/cart` | 加入購物車 | ✅ |
| PUT | `/cart/{id}` | 更新數量 | ✅ |
| DELETE | `/cart/{id}` | 移除商品 | ✅ |
| DELETE | `/cart` | 清空購物車 | ✅ |
| PUT | `/cart/select` | 批次選取/取消選取 | ✅ |

### 5.1.5 訂單

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| POST | `/orders` | 建立訂單 | ✅ |
| GET | `/orders` | 訂單列表 | ✅ |
| GET | `/orders/{id}` | 訂單詳情 | ✅ |
| POST | `/orders/{id}/cancel` | 取消訂單 | ✅ |
| POST | `/orders/{id}/confirm` | 確認收貨 | ✅ |

### 5.1.6 支付

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| POST | `/payment/create` | 發起支付 | ✅ |
| GET | `/payment/methods` | 取得可用付款方式 | ❌ |
| POST | `/payment/callback/{method}` | 支付回調 | ❌ |

### 5.1.7 會員認證

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| POST | `/auth/login` | 帳密登入 | ❌ |
| POST | `/auth/register` | 註冊 | ❌ |
| POST | `/auth/logout` | 登出 | ✅ |
| GET | `/auth/social/{provider}` | 第三方登入入口 | ❌ |
| GET | `/auth/social/{provider}/callback` | 第三方回調 | ❌ |
| POST | `/auth/refresh` | 重新整理 Token | ✅ |

### 5.1.8 會員中心

| 方法 | 路徑 | 說明 | 認證 |
|-----|-----|-----|-----|
| GET | `/member/info` | 會員資料 | ✅ |
| PUT | `/member/info` | 更新資料 | ✅ |
| PUT | `/member/password` | 修改密碼 | ✅ |
| GET | `/member/points` | 點數紀錄 | ✅ |

---

## 5.2 後台 API（管理員用）

> 路徑前綴：`/shop/admin`

### 5.2.1 輪播管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/banner/list` | 輪播列表 | `shop:banner:list` |
| GET | `/banner/{id}` | 輪播詳情 | `shop:banner:query` |
| POST | `/banner` | 新增輪播 | `shop:banner:add` |
| PUT | `/banner` | 更新輪播 | `shop:banner:edit` |
| DELETE | `/banner/{ids}` | 刪除輪播 | `shop:banner:remove` |

### 5.2.2 區塊管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/block/list` | 區塊列表 | `shop:block:list` |
| POST | `/block` | 儲存區塊 | `shop:block:edit` |

### 5.2.3 分類管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/category/list` | 分類列表 | `shop:category:list` |
| GET | `/category/{id}` | 分類詳情 | `shop:category:query` |
| POST | `/category` | 新增分類 | `shop:category:add` |
| PUT | `/category` | 更新分類 | `shop:category:edit` |
| DELETE | `/category/{id}` | 刪除分類 | `shop:category:remove` |

### 5.2.4 商品管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/product/list` | 商品列表 | `shop:product:list` |
| GET | `/product/{id}` | 商品詳情 | `shop:product:query` |
| POST | `/product` | 新增商品 | `shop:product:add` |
| PUT | `/product` | 更新商品 | `shop:product:edit` |
| PUT | `/product/{id}/status` | 更新狀態 | `shop:product:edit` |
| DELETE | `/product/{ids}` | 刪除商品 | `shop:product:remove` |

### 5.2.5 SKU 管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/sku/list/{productId}` | SKU 列表 | `shop:product:query` |
| POST | `/sku` | 新增 SKU | `shop:product:edit` |
| PUT | `/sku` | 更新 SKU | `shop:product:edit` |
| DELETE | `/sku/{id}` | 刪除 SKU | `shop:product:edit` |

### 5.2.6 訂單管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/order/list` | 訂單列表 | `shop:order:list` |
| GET | `/order/{id}` | 訂單詳情 | `shop:order:query` |
| PUT | `/order/{id}/status` | 更新狀態 | `shop:order:edit` |
| POST | `/order/{id}/ship` | 出貨 | `shop:order:ship` |
| POST | `/order/{id}/refund` | 退款 | `shop:order:refund` |
| GET | `/order/export` | 匯出訂單 | `shop:order:export` |

### 5.2.7 會員管理

| 方法 | 路徑 | 說明 | 權限 |
|-----|-----|-----|-----|
| GET | `/member/list` | 會員列表 | `shop:member:list` |
| GET | `/member/{id}` | 會員詳情 | `shop:member:query` |
| PUT | `/member/{id}/status` | 更新狀態 | `shop:member:edit` |
| GET | `/member/{id}/orders` | 會員訂單 | `shop:member:query` |

---

## 5.3 API 回應格式

### 5.3.1 成功回應

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 5.3.2 分頁回應

```json
{
  "code": 200,
  "msg": "查詢成功",
  "total": 100,
  "rows": [ ... ]
}
```

### 5.3.3 錯誤回應

```json
{
  "code": 500,
  "msg": "商品庫存不足"
}
```

---

## 5.4 請求範例

### 5.4.1 建立訂單

**Request**

```http
POST /api/shop/orders
Content-Type: application/json
Authorization: Bearer {token}

{
  "cartIds": [1, 2, 3],
  "receiverName": "王小明",
  "receiverMobile": "0912345678",
  "receiverAddress": "台北市信義區...",
  "receiverZip": "110",
  "paymentMethod": "LINE_PAY",
  "shippingMethod": "HOME_DELIVERY",
  "buyerRemark": "請送到管理室"
}
```

**Response**

```json
{
  "code": 200,
  "msg": "訂單建立成功",
  "data": {
    "orderId": 12345,
    "orderNo": "SO20260117001",
    "totalAmount": 1500.00,
    "paymentUrl": "https://pay.line.me/..."
  }
}
```

### 5.4.2 第三方登入

**Step 1: 取得授權 URL**

```http
GET /api/shop/auth/social/LINE
```

**Response**

```json
{
  "code": 200,
  "data": {
    "authUrl": "https://access.line.me/oauth2/v2.1/authorize?..."
  }
}
```

**Step 2: 回調處理**

```http
GET /api/shop/auth/social/LINE/callback?code=xxx&state=yyy
```

**Response**

```json
{
  "code": 200,
  "msg": "登入成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "memberInfo": {
      "memberId": 1001,
      "nickname": "王小明",
      "avatar": "https://..."
    }
  }
}
```
