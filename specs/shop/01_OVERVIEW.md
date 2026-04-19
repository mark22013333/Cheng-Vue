# 01. 專案概述與技術架構

## 1.1 專案目標

建立一個具備以下功能的商城網站：

- **前台網站**：形象首頁、商品展示、購物車、結帳流程、會員中心
- **後台管理**：首頁輪播管理、形象區塊管理、商品管理、訂單管理、會員管理
- **庫存整合**：與現有 `inv_item` 庫存系統軟連結，實現庫存同步

---

## 1.2 系統架構圖

```
┌─────────────────────────────────────────────────────────────────┐
│                    消費者端 (cheng-shop-ui)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ 首頁形象  │  │ 商品瀏覽  │  │ 購物車   │  │ 會員中心  │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     後端 API (cheng-shop)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ 商品服務  │  │ 訂單服務  │  │ 會員服務  │  │ 內容服務  │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│                              │                                   │
│                    ┌─────────┴─────────┐                        │
│                    │  庫存整合服務      │                        │
│                    └─────────┬─────────┘                        │
└──────────────────────────────┼──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                  現有系統 (cheng-system)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                      │
│  │ inv_item │  │ inv_stock│  │ inv_xxx  │                      │
│  └──────────┘  └──────────┘  └──────────┘                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1.3 技術棧

| 層級 | 技術 | 說明 |
|-----|-----|-----|
| **前端（前台）** | Vue 3.5 + Vite 5 + Pinia | 獨立 SPA，消費者使用 |
| **前端（後台）** | 現有 cheng-ui | 整合到現有後台系統 |
| **後端** | Spring Boot 3.5.4 + Java 17 | 新增 cheng-shop 模組 |
| **ORM** | MyBatis | 與現有系統一致 |
| **資料庫** | MySQL 8.0 | 與現有系統共用 |
| **快取** | Redis | 庫存預扣、Session、熱點資料 |
| **認證** | JWT + OAuth2 | 前台會員獨立認證體系 |

---

## 1.4 模組結構

### 後端模組 (cheng-shop)

```
cheng-shop/
├── src/main/java/com/cheng/shop/
│   ├── config/                    # 配置類
│   │   ├── ShopSecurityConfig.java
│   │   └── ShopRedisConfig.java
│   ├── controller/                # 控制器
│   │   ├── api/                   # 前台 API（消費者用）
│   │   │   ├── ShopHomeController.java
│   │   │   ├── ShopProductController.java
│   │   │   ├── ShopCartController.java
│   │   │   ├── ShopOrderController.java
│   │   │   └── ShopMemberController.java
│   │   └── admin/                 # 後台管理 API
│   │       ├── AdminBannerController.java
│   │       ├── AdminProductController.java
│   │       ├── AdminOrderController.java
│   │       └── AdminMemberController.java
│   ├── domain/                    # 領域模型
│   │   ├── entity/                # 實體類
│   │   ├── dto/                   # 數據傳輸對象
│   │   ├── vo/                    # 視圖對象
│   │   └── enums/                 # 狀態列舉
│   ├── mapper/                    # MyBatis Mapper
│   ├── service/                   # 服務層
│   │   ├── IShopProductService.java
│   │   ├── IShopOrderService.java
│   │   ├── IShopMemberService.java
│   │   ├── IShopContentService.java
│   │   └── impl/
│   ├── strategy/                  # 策略模式
│   │   ├── payment/               # 支付策略
│   │   ├── shipping/              # 物流策略
│   │   ├── discount/              # 折扣策略
│   │   └── auth/                  # 登入策略
│   └── integration/               # 整合層
│       └── InventoryIntegrationService.java
└── src/main/resources/
    └── mapper/shop/               # Mapper XML
```

### 前端模組 (cheng-shop-ui)

```
cheng-shop-ui/
├── src/
│   ├── api/                       # API 請求
│   ├── assets/                    # 靜態資源
│   ├── components/                # 公用組件
│   │   ├── Banner/                # 輪播組件
│   │   ├── ProductCard/           # 商品卡片
│   │   └── CartDrawer/            # 購物車抽屜
│   ├── composables/               # 組合式函數
│   ├── layouts/                   # 佈局組件
│   ├── router/                    # 路由配置
│   ├── stores/                    # Pinia 狀態
│   │   ├── cart.js
│   │   ├── member.js
│   │   └── product.js
│   └── views/                     # 頁面組件
│       ├── home/                  # 首頁
│       ├── product/               # 商品相關
│       ├── cart/                  # 購物車
│       ├── checkout/              # 結帳
│       ├── order/                 # 訂單
│       └── member/                # 會員中心
├── package.json
└── vite.config.js
```

---

## 1.5 與現有系統的關係

### 庫存整合方式

商品上架結構 (`shop_product` + `shop_product_sku`) 與庫存系統 (`inv_item` + `inv_stock`) 採用**軟連結**方式：

```
shop_product_sku.inv_item_id → inv_item.item_id
```

**設計原因**：
- 庫存系統關注：成本、儲位、條碼
- 電商系統關注：標題、SEO、圖片輪播、影片
- 兩者職責不同，不應混用同一張表

**整合優點**：
- 一個 SKU 可對應一個庫存品
- 多個 SKU 可組合多個庫存品（組合商品）
- 前台賣的是 `shop_product_sku`，扣庫存時去扣對應的 `inv_item_id`

### 會員系統獨立

商城會員 (`shop_member`) 與後台使用者 (`sys_user`) 完全獨立：

- `sys_user`：給員工/管理員使用，Spring Security 認證
- `shop_member`：給消費者使用，JWT 認證，支援第三方登入
