# 10. 開發檢查清單

---

## 10.1 開發前檢查

### 10.1.1 環境準備

- [ ] 確認 Java 17 已安裝
- [ ] 確認 Node.js 20+ 已安裝
- [ ] 確認 MySQL 8.0 已啟動
- [ ] 確認 Redis 已啟動
- [ ] 檢查 Migration 版本號，避免衝突

### 10.1.2 模組建立

- [ ] 建立 `cheng-shop` 後端模組
- [ ] 在 `pom.xml` 中新增模組依賴
- [ ] 建立 `cheng-shop-ui` 前端專案
- [ ] 配置 Vite 開發環境

---

## 10.2 後端開發檢查

### 10.2.1 核心規範（必須遵守）

- [ ] **強制使用 Enum**：所有狀態必須定義 Enum，禁止魔術數字
- [ ] **策略模式**：支付、物流、折扣、第三方登入使用策略模式
- [ ] **冗餘欄位**：訂單明細儲存商品快照資訊
- [ ] **介面導向**：所有 Service 必須有介面定義
- [ ] **事務管理**：多表操作必須加 `@Transactional`

### 10.2.2 Enum 定義清單

- [ ] `ProductStatus`：草稿/預覽/上架中/已下架
- [ ] `OrderStatus`：待付款/已付款/處理中/已出貨/已完成/已取消/退款中/已退款
- [ ] `PayStatus`：未付款/付款中/已付款/退款中/已退款/付款失敗
- [ ] `ShipStatus`：未出貨/備貨中/已出貨/運送中/已送達/已退回
- [ ] `MemberStatus`：正常/停用/凍結
- [ ] `SocialProvider`：LINE/GOOGLE/FACEBOOK
- [ ] `PaymentMethod`：LINE_PAY/CREDIT_CARD/ATM/CVS/COD
- [ ] `ShippingMethod`：HOME_DELIVERY/CVS_711/CVS_FAMILY/CVS_HILIFE/STORE_PICKUP
- [ ] `BannerLinkType`：NONE/PRODUCT/CATEGORY/URL
- [ ] `BlockType`：TEXT/IMAGE/HTML/PRODUCT_LIST/CATEGORY_LIST
- [ ] `CommonStatus`：ENABLED/DISABLED

### 10.2.3 策略模式實現

- [ ] `PaymentStrategy` 介面 + 工廠
- [ ] `LinePayStrategy` 實現
- [ ] `CreditCardStrategy` 實現
- [ ] `ShippingStrategy` 介面 + 工廠
- [ ] `HomeDeliveryStrategy` 實現
- [ ] `CvsPickupStrategy` 實現
- [ ] `SocialAuthStrategy` 介面 + 工廠
- [ ] `LineAuthStrategy` 實現
- [ ] `GoogleAuthStrategy` 實現

### 10.2.4 Service 層

- [ ] `IShopProductService` + 實現
- [ ] `IShopOrderService` + 實現
- [ ] `IShopMemberService` + 實現
- [ ] `IShopCartService` + 實現
- [ ] `IShopContentService` + 實現
- [ ] `InventoryIntegrationService`
- [ ] `StockRedisService`

### 10.2.5 Controller 層

**前台 API**
- [ ] `ShopHomeController`：首頁相關
- [ ] `ShopProductController`：商品相關
- [ ] `ShopCartController`：購物車
- [ ] `ShopOrderController`：訂單
- [ ] `ShopMemberController`：會員
- [ ] `ShopAuthController`：認證

**後台 API**
- [ ] `AdminBannerController`：輪播管理
- [ ] `AdminBlockController`：區塊管理
- [ ] `AdminCategoryController`：分類管理
- [ ] `AdminProductController`：商品管理
- [ ] `AdminOrderController`：訂單管理
- [ ] `AdminMemberController`：會員管理

### 10.2.6 安全與認證

- [ ] 前台 API 使用 JWT 認證
- [ ] 後台 API 使用現有 Spring Security
- [ ] 公開 API 加上 `@Anonymous` 註解
- [ ] SSE 端點加上 `@Anonymous` 註解
- [ ] 防重複提交加上 `@RepeatSubmit`
- [ ] 重要操作加上 `@Log`

### 10.2.7 效能優化

- [ ] 列表 API 必須分頁
- [ ] 熱點資料使用 Redis 快取
- [ ] 庫存扣減使用 Redis Lua Script
- [ ] 避免 N+1 查詢

---

## 10.3 前端開發檢查

### 10.3.1 消費者端 (cheng-shop-ui)

**技術棧**
- [ ] Vue 3 + Composition API
- [ ] Vite 5
- [ ] Pinia 狀態管理
- [ ] Vue Router 4
- [ ] Element Plus（繁體中文）

**頁面清單**
- [ ] 首頁 (`home/index.vue`)
- [ ] 分類頁 (`category/index.vue`, `category/products.vue`)
- [ ] 商品列表 (`product/list.vue`)
- [ ] 商品詳情 (`product/detail.vue`)
- [ ] 搜尋結果 (`product/search.vue`)
- [ ] 購物車 (`cart/index.vue`)
- [ ] 結帳頁 (`checkout/index.vue`, `checkout/result.vue`)
- [ ] 訂單列表 (`order/list.vue`)
- [ ] 訂單詳情 (`order/detail.vue`)
- [ ] 會員中心 (`member/index.vue`, `member/profile.vue`, `member/password.vue`)
- [ ] 登入 (`auth/login.vue`)
- [ ] 註冊 (`auth/register.vue`)
- [ ] 第三方回調 (`auth/callback.vue`)

**Pinia Store**
- [ ] `useMemberStore`：會員狀態
- [ ] `useCartStore`：購物車狀態
- [ ] `useProductStore`：商品快取

**響應式設計**
- [ ] 手機版適配 (< 768px)
- [ ] 平板版適配 (768px - 1199px)
- [ ] 桌面版適配 (≥ 1200px)

### 10.3.2 後台管理 (cheng-ui)

**頁面清單**
- [ ] 輪播管理 (`shop/banner/index.vue`)
- [ ] 區塊管理 (`shop/block/index.vue`)
- [ ] 分類管理 (`shop/category/index.vue`)
- [ ] 商品列表 (`shop/product/index.vue`)
- [ ] 商品編輯 (`shop/product/edit.vue`)
- [ ] 訂單列表 (`shop/order/index.vue`)
- [ ] 訂單詳情 (`shop/order/detail.vue`)
- [ ] 會員列表 (`shop/member/index.vue`)

**API 定義**
- [ ] `api/shop/banner.js`
- [ ] `api/shop/block.js`
- [ ] `api/shop/category.js`
- [ ] `api/shop/product.js`
- [ ] `api/shop/order.js`
- [ ] `api/shop/member.js`

### 10.3.3 前端規範

- [ ] 環境變數使用 `import.meta.env`
- [ ] 路由名稱必須唯一（使用模組前綴）
- [ ] Element Plus 設定繁體中文
- [ ] el-radio-group label 為字串
- [ ] 狀態欄位提交使用 xxxCode

---

## 10.4 資料庫檢查

### 10.4.1 表結構

- [ ] `shop_category`：商品分類
- [ ] `shop_product`：商品主表
- [ ] `shop_product_sku`：商品 SKU
- [ ] `shop_member`：會員
- [ ] `shop_member_social`：第三方登入
- [ ] `shop_order`：訂單主表
- [ ] `shop_order_item`：訂單明細
- [ ] `shop_cart`：購物車
- [ ] `shop_banner`：輪播
- [ ] `shop_page_block`：頁面區塊

### 10.4.2 索引檢查

- [ ] 外鍵欄位有索引
- [ ] 狀態欄位有索引
- [ ] 時間欄位有索引（用於排序）
- [ ] 唯一約束正確設定

### 10.4.3 冗餘欄位

- [ ] `shop_order.member_nickname`
- [ ] `shop_order_item.product_name`
- [ ] `shop_order_item.sku_name`
- [ ] `shop_order_item.sku_image`
- [ ] `shop_order_item.unit_price`
- [ ] `shop_order_item.inv_item_id`
- [ ] `shop_order_item.inv_item_name`

---

## 10.5 庫存整合檢查

- [ ] SKU 可關聯 `inv_item`
- [ ] 未關聯時使用獨立庫存
- [ ] Redis 庫存預扣實現
- [ ] 資料庫庫存扣減（防超賣 WHERE 條件）
- [ ] 庫存回滾機制
- [ ] 定時同步庫存到 Redis
- [ ] 訂單超時自動取消

---

## 10.6 測試檢查

### 10.6.1 單元測試

- [ ] Service 層業務邏輯測試
- [ ] 策略模式各策略測試
- [ ] Enum 方法測試

### 10.6.2 整合測試

- [ ] 下單流程測試（正常流程）
- [ ] 下單流程測試（庫存不足）
- [ ] 下單流程測試（併發扣庫存）
- [ ] 支付回調測試
- [ ] 訂單取消庫存回滾測試

### 10.6.3 前端測試

- [ ] 首頁載入測試
- [ ] 商品瀏覽流程測試
- [ ] 購物車操作測試
- [ ] 結帳流程測試
- [ ] 會員登入/註冊測試
- [ ] 第三方登入測試
- [ ] 響應式佈局測試

---

## 10.7 部署檢查

### 10.7.1 配置檔案

- [ ] `application-local.yml`：本地環境
- [ ] `application-dev.yml`：開發環境
- [ ] `application-prod.yml`：生產環境
- [ ] LINE Login 配置
- [ ] Google Login 配置
- [ ] LINE Pay 配置
- [ ] Redis 配置

### 10.7.2 安全檢查

- [ ] 敏感配置使用 Jasypt 加密
- [ ] API Key 不得硬編碼
- [ ] 第三方回調 URL 配置正確
- [ ] CORS 配置正確

### 10.7.3 監控

- [ ] 操作日誌記錄完整
- [ ] 異常日誌記錄完整
- [ ] Redis 連線監控
- [ ] 資料庫連線監控

---

## 10.8 文件檢查

- [ ] API 文件（Swagger / 手動維護）
- [ ] 部署文件
- [ ] 配置說明文件
- [ ] 第三方服務申請指南

---

## 10.9 常見陷阱提醒

### 後端

```java
// ❌ 禁止：使用魔術字串
if (status.equals("ON_SALE")) { ... }

// ✅ 必須：使用 Enum
if (product.getStatusEnum() == ProductStatus.ON_SALE) { ... }
```

```java
// ❌ 禁止：忘記事務
public void createOrder() {
    orderMapper.insert(order);
    stockMapper.decrease(itemId, quantity);  // 可能部分失敗
}

// ✅ 必須：加事務
@Transactional(rollbackFor = Exception.class)
public void createOrder() { ... }
```

```java
// ❌ 禁止：庫存扣減無防超賣
UPDATE stock SET quantity = quantity - #{qty}

// ✅ 必須：加 WHERE 條件
UPDATE stock SET quantity = quantity - #{qty} WHERE quantity >= #{qty}
```

### 前端

```javascript
// ❌ 錯誤：Vue 2 環境變數
process.env.VUE_APP_BASE_API

// ✅ 正確：Vue 3 + Vite
import.meta.env.VITE_APP_BASE_API
```

```javascript
// ❌ 錯誤：路由名稱重複
{ name: 'Product', path: '/shop/product' }
{ name: 'Product', path: '/admin/product' }  // 衝突！

// ✅ 正確：使用模組前綴
{ name: 'ShopProduct', path: '/shop/product' }
{ name: 'AdminProduct', path: '/admin/product' }
```
