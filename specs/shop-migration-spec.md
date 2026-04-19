# 商城系統遷移規格書

> **來源專案**: Choice Code PHP 電商平台
> **目標專案**: CoolApps (cheng-shop 模組)
> **建立日期**: 2026-01-29
> **狀態標記**: ✅ 已完成 | 🔧 部分完成 | ❌ 未開始

---

## 一、功能總覽與遷移進度

| # | 功能模組 | PHP 狀態 | Java 遷移狀態 | 備註 |
|---|---------|---------|--------------|------|
| 1 | 商品管理 | ✅ | ✅ 已完成 | SKU 架構已升級 |
| 2 | 商品分類 | ✅ | ✅ 已完成 | 改為樹狀結構 |
| 3 | 購物車 | ✅ | ✅ 已完成 | 新增選中機制 |
| 4 | 訂單系統 | ✅ | ✅ 已完成 | 含 ECPay 金流回調、出貨事務保護 |
| 5 | 結帳流程 | ✅ | ✅ 已完成 | 含折扣整合、可配置運費、ECPay 付款 |
| 6 | 會員系統 | ✅ | 🔧 部分完成 | 缺少第三方登入實作 |
| 7 | 金流整合 (ECPay) | ✅ | ✅ 已完成 | EcpayService + ShopPaymentController |
| 8 | 物流整合 | ✅ | ❌ 未開始 | 超商取貨、宅配 |
| 9 | 折扣系統 | ✅ | ✅ 已完成 | 特價 + 全站折扣 (mode 0/1/2) |
| 10 | 滿額禮物 | ✅ | ✅ 已完成 | 禮物 CRUD + 結帳整合 + 出貨扣庫存 |
| 11 | 熱門/特價商品 | ✅ | ✅ 已完成 | 標記管理 (is_hot/is_new/is_recommend) + 單筆切換 + 批量設定 |
| 12 | 輪播管理 | ✅ | ✅ 已完成 | |
| 13 | 頁面區塊 | ✅ | ✅ 已完成 | |
| 14 | 部落格/文章 | ✅ | ✅ 已完成 | 文章 CRUD + 前台列表/詳情 + 首頁精選區塊 |
| 15 | 多語言 | ✅ | ❌ 未開始 | PHP 使用 DB 多語言 |
| 16 | 電子報 | ✅ | ❌ 未開始 | |
| 17 | 簡訊通知 | ✅ | ❌ 未開始 | 訂單/出貨通知 |
| 18 | Facebook 登入 | ✅ | ❌ 未開始 | 枚舉已定義 |
| 19 | LINE 登入 | ✅ | ❌ 未開始 | 枚舉已定義 |
| 20 | 資料匯出 (Excel) | ✅ | ❌ 未開始 | 訂單/商品/會員匯出 |
| 21 | 資料匯入 (Excel) | ✅ | ❌ 未開始 | 商品批量匯入 |
| 22 | 統計報表 | ✅ | ❌ 未開始 | 營收/訂單/訪客統計 |
| 23 | 系統設定 | ✅ | ❌ 未開始 | 商城專屬設定 |
| 24 | 訪客追蹤 | ✅ | ❌ 未開始 | IP 訪客記錄 |
| 25 | SEO 關鍵字 | ✅ | ❌ 未開始 | |

---

## 二、已完成功能詳細規格

### 2.1 商品管理 ✅

**資料表**: `shop_product`, `shop_product_sku`

| 欄位 | 型別 | 說明 | PHP 對應 |
|------|------|------|---------|
| product_id | BIGINT PK | 商品 ID | products.id |
| category_id | BIGINT | 分類 ID | products.main_id + sub_id |
| title | VARCHAR(200) | 商品名稱 | products.pname |
| main_image | VARCHAR(500) | 主圖 | products.images (第一張) |
| slider_images | TEXT (JSON) | 輪播圖 | products.images (逗號分隔) |
| video_url | VARCHAR(500) | 影片 | products.youtube |
| description | LONGTEXT | 商品描述 (HTML) | products.info |
| price | DECIMAL(10,2) | 售價 | products.price |
| original_price | DECIMAL(10,2) | 原價 | products.price (折扣前) |
| status | VARCHAR(20) | 狀態 | products.status |
| is_hot | TINYINT | 熱銷 | hot_products 關聯 |
| is_new | TINYINT | 新品 | 無 (新增) |
| is_recommend | TINYINT | 推薦 | discount_products 關聯 |
| sales_count | INT | 銷量 | products.volume |
| view_count | INT | 瀏覽數 | products.views |
| sale_price | DECIMAL(10,2) | 特惠價 | products.sprice (P0 新增) |
| sale_end_date | DATETIME | 特價結束日期 | products.sdate (P0 新增) |

**SKU 規格** (PHP 為 JSON opts，Java 已升級為獨立表):

| 欄位 | 型別 | 說明 |
|------|------|------|
| sku_id | BIGINT PK | 規格 ID |
| product_id | BIGINT FK | 所屬商品 |
| sku_name | VARCHAR(200) | 規格名稱 |
| price | DECIMAL(10,2) | 規格售價 |
| original_price | DECIMAL(10,2) | 規格原價 |
| sale_price | DECIMAL(10,2) | 規格特惠價 (P0 新增) |
| cost_price | DECIMAL(10,2) | 成本價 |
| stock_quantity | INT | 庫存數量 |
| inv_item_id | BIGINT | 庫存模組關聯 |

**已實作 API**:
- `GET /shop/front/products` - 商品列表 (分頁、分類篩選)
- `GET /shop/front/product/{id}` - 商品詳情
- `GET /shop/front/product/{id}/skus` - SKU 列表
- `GET /shop/front/products/hot` - 熱銷商品
- `GET /shop/front/products/new` - 新品
- `GET /shop/front/products/recommend` - 推薦商品
- `POST/PUT/DELETE /shop/product/*` - 後台 CRUD

---

### 2.2 商品分類 ✅

**資料表**: `shop_category`

| 欄位 | 型別 | 說明 | PHP 對應 |
|------|------|------|---------|
| category_id | BIGINT PK | 分類 ID | main_class.id / sub_class.id |
| parent_id | BIGINT | 父分類 | sub_class.mid |
| name | VARCHAR(100) | 名稱 | main_class.cname |
| icon | VARCHAR(500) | 圖示 | 無 (新增) |
| banner_image | VARCHAR(500) | 橫幅圖 | 無 (新增) |
| sort_order | INT | 排序 | main_class.seq |
| status | VARCHAR(20) | 狀態 | 無 (新增) |

**升級**: PHP 為二層固定結構 (main_class → sub_class)，Java 改為無限層級樹狀結構。

---

### 2.3 購物車 ✅

**資料表**: `shop_cart`

| 欄位 | 型別 | 說明 | PHP 對應 |
|------|------|------|---------|
| cart_id | BIGINT PK | 購物車項目 ID | 無 (PHP 為 Session) |
| member_id | BIGINT FK | 會員 ID | $_SESSION['bee_member'] |
| sku_id | BIGINT FK | 商品規格 ID | 無 (PHP 無 SKU) |
| quantity | INT | 數量 | cart JSON 的 cnt |
| is_selected | TINYINT | 是否選中 | 無 (新增) |

**升級**: PHP 為 Session/JSON 前端管理，Java 改為 DB 持久化 + 選中機制。

**已實作 API**:
- `GET /shop/member/cart` - 取得購物車
- `GET /shop/member/cart/count` - 數量統計
- `POST /shop/member/cart/add` - 加入
- `PUT /shop/member/cart/update` - 更新數量
- `PUT /shop/member/cart/select/{id}` - 切換選中
- `PUT /shop/member/cart/selectAll` - 全選
- `DELETE /shop/member/cart/remove/{id}` - 刪除
- `DELETE /shop/member/cart/clear` - 清空
- `POST /shop/member/cart/merge` - 合併訪客購物車

---

### 2.4 訂單系統 ✅

**資料表**: `shop_order`, `shop_order_item`

**shop_order**:

| 欄位 | 型別 | 說明 | PHP 對應 |
|------|------|------|---------|
| order_id | BIGINT PK | 訂單 ID | orders.id |
| order_no | VARCHAR(32) UNIQUE | 訂單編號 | orders.oid (P+timestamp) |
| member_id | BIGINT FK | 會員 ID | orders.mid |
| product_amount | DECIMAL(10,2) | 商品金額 | 從 content JSON 計算 |
| shipping_amount | DECIMAL(10,2) | 運費 | 無 (PHP 在系統設定) |
| discount_amount | DECIMAL(10,2) | 折扣金額 | 無 (新增) |
| total_amount | DECIMAL(10,2) | 總金額 | orders.total |
| status | VARCHAR(20) | 訂單狀態 | 簡化版 rtncode |
| pay_status | VARCHAR(20) | 付款狀態 | orders.rtncode |
| ship_status | VARCHAR(20) | 物流狀態 | orders.pdate (有=已出貨) |
| payment_method | VARCHAR(20) | 付款方式 | orders.pay_method (1-4) |
| shipping_method | VARCHAR(20) | 物流方式 | orders.pay_method 推算 |
| receiver_name | VARCHAR(50) | 收件人 | orders.oname |
| receiver_phone | VARCHAR(20) | 收件電話 | orders.ophone |
| receiver_address | VARCHAR(200) | 收件地址 | orders.addr |
| member_memo | VARCHAR(500) | 會員備註 | orders.memo |
| ecpay_trade_no | VARCHAR(64) | ECPay 交易編號 | 無 (P0 新增) |
| ecpay_info | JSON | ECPay 回調資訊 | 無 (P0 新增) |

**shop_order_item**:

| 欄位 | 型別 | 說明 | PHP 對應 |
|------|------|------|---------|
| item_id | BIGINT PK | 明細 ID | 無 (PHP 存 JSON) |
| order_id | BIGINT FK | 訂單 ID | - |
| product_id | BIGINT FK | 商品 ID | content.prods[].pid |
| sku_id | BIGINT FK | 規格 ID | 無 (PHP 無 SKU) |
| product_title | VARCHAR(200) | 商品名稱 | content.prods[].pname |
| sku_name | VARCHAR(200) | 規格名稱 | content.prods[].opts |
| product_image | VARCHAR(500) | 商品圖片 | content.prods[].pic |
| unit_price | DECIMAL(10,2) | 單價 | content.prods[].price |
| quantity | INT | 數量 | content.prods[].cnt |
| total_price | DECIMAL(10,2) | 小計 | content.prods[].mcnt |

**已完成**:
- 訂單建立、列表查詢、詳情查看
- 訂單狀態機 (PENDING → PAID → PROCESSING → SHIPPED → DELIVERED → COMPLETED)
- 訂單取消、確認收貨
- 運費計算 (可配置：sys_config shop.shipping.* 設定)
- 結帳預覽與提交
- 庫存扣減 (事務保護)
- ✅ ECPay 金流回調處理 (Server Callback + CheckMacValue 驗證)
- ✅ 出貨流程 (標記出貨 + @Transactional 事務保護)
- ✅ ECPay 付款欄位 (ecpay_trade_no, ecpay_info)

**缺少**:
- ❌ 出貨簡訊通知
- ❌ 訂單匯出 (Excel)
- ❌ 訂單列印
- ❌ 訂單統計 (日/月/年報表)

---

### 2.5 會員系統 🔧

**資料表**: `shop_member`, `shop_member_address`, `shop_member_social`

**已完成**:
- 會員 CRUD、狀態管理
- 多收貨地址管理 (新增/編輯/刪除/設為預設)
- 會員等級欄位 (NORMAL/VIP/SVIP)
- 點數欄位
- 第三方登入表結構 (shop_member_social)

**缺少**:
- ❌ 第三方登入實作 (Facebook OAuth, LINE Login)
- ❌ 會員密碼重設
- ❌ 會員匯出 (Excel)

---

## 三、未開始功能詳細規格

### 3.1 金流整合 (ECPay) ✅ 已完成

**PHP 原始實作**:

#### 3.1.1 支付方式

| 代碼 | 名稱 | 說明 |
|------|------|------|
| ALL | 全部 | 不指定，顯示所有付款選項 ✅ 已實作 |
| Credit | 信用卡 | 線上刷卡 |
| WebATM | 網路 ATM | 網路銀行轉帳 |
| ATM | 自動櫃員機 | 虛擬帳號轉帳 |
| CVS | 超商代碼 | 超商繳費代碼 |
| BARCODE | 超商條碼 | 超商條碼繳費 |
| GooglePay | Google Pay | 行動支付 |

#### 3.1.2 支付流程

```
前端提交訂單
  → 後端建立訂單記錄 (status=PENDING)                    ✅
  → 前端呼叫 /shop/payment/ecpay/create                  ✅
  → 後端組裝 ECPay 參數，回傳自動提交 HTML Form           ✅
  → 前端插入 Form 並自動 submit 到 ECPay                  ✅
  → 用戶在 ECPay 完成付款                                  ✅
  → ECPay Server 回調 POST /shop/payment/ecpay/callback   ✅
     → 驗證 CheckMacValue (SHA256)                         ✅
     → 冪等性檢查 (已付款不重複處理)                         ✅
     → 更新訂單狀態 (pay_status=PAID, status=PAID)          ✅
     → 記錄 ecpay_trade_no + ecpay_info                    ✅
  → 用戶返回 POST /shop/payment/ecpay/return               ✅
     → 302 重導至前端 /mall/payment-result/:orderNo         ✅
```

#### 3.1.3 ECPay 參數規格

| 參數 | 說明 | 對應 Java 欄位 |
|------|------|---------------|
| MerchantID | 特店編號 | sys_config: shop.ecpay.merchant_id |
| MerchantTradeNo | 交易編號 | order_no |
| MerchantTradeDate | 交易日期 | create_time (yyyy/MM/dd HH:mm:ss) |
| TotalAmount | 交易金額 (整數) | total_amount.intValue() |
| TradeDesc | 交易描述 | "CoolApps 商城" |
| ItemName | 商品名稱 | 訂單商品名稱串接 (#分隔) |
| ReturnURL | Server 回調 | /shop/payment/ecpay/callback |
| OrderResultURL | 用戶返回 | /shop/payment/ecpay/return |
| ChoosePayment | 付款方式 | ALL |
| EncryptType | 加密類型 | 1 (SHA256) |
| CheckMacValue | 檢查碼 | SHA256 雜湊計算 |

#### 3.1.4 測試/生產環境

| 環境 | ServiceURL | MerchantID | HashKey | HashIV |
|------|-----------|------------|---------|--------|
| 測試 | payment-stage.ecpay.com.tw | 3002607 | pwFHCqoQZGmho4w6 | EkRm7iFT261dpevs |
| 生產 | payment.ecpay.com.tw | sys_config | sys_config | sys_config |

> 環境切換由 `sys_config: shop.ecpay.mode` 控制 (test/production)

#### 3.1.5 Java 實作 ✅

**已建立**:
- `EcpayService` - ECPay 金流核心服務 (無外部 SDK，純 HTTP 表單 + SHA256)
  - `createPayment()` - 組裝參數、產生 CheckMacValue、回傳自動提交 HTML
  - `verifyCallback()` - 驗證回調 CheckMacValue
  - `handleCallback()` - 冪等處理回調，更新訂單狀態
  - `generateCheckMacValue()` - SHA256 演算法實作
- `ShopPaymentController` - 金流 API 控制器
  - `POST /shop/payment/ecpay/create` - 建立支付 (需登入)
  - `POST /shop/payment/ecpay/callback` - Server 回調 (@Anonymous)
  - `POST /shop/payment/ecpay/return` - 用戶返回 (@Anonymous)
- `payment.js` - 前端 API
- `payment-result.vue` - 付款結果頁面 (成功/失敗 + 重新付款)
- `checkout/index.vue` - 結帳頁增加 ECPay 選項
- `PaymentMethod.ECPAY` - 枚舉新增
- `ShopOrder` + Mapper - 增加 ecpay_trade_no, ecpay_info 欄位

**設定 (sys_config)**:
| config_key | 說明 |
|------------|------|
| shop.ecpay.merchant_id | 特店編號 |
| shop.ecpay.hash_key | HashKey |
| shop.ecpay.hash_iv | HashIV |
| shop.ecpay.mode | 環境模式 (test/production) |

---

### 3.2 物流整合 ❌

**PHP 原始實作**:

#### 3.2.1 配送方式

| 代碼 | pay_method | 說明 | Java 對應 |
|------|-----------|------|----------|
| 1 | 宅配貨到付款 | 家中配送，貨到付款 | HOME_DELIVERY + COD |
| 2 | 宅配線上付款 | 家中配送，先線上付款 | HOME_DELIVERY + CREDIT_CARD |
| 3 | 超商取貨付款 | CVS 取貨，到店付款 | CVS_PICKUP + COD |
| 4 | 超商取貨線上付款 | CVS 取貨，先線上付款 | CVS_PICKUP + CREDIT_CARD |

#### 3.2.2 超商取貨流程

```
用戶選擇超商取貨
  → 呼叫 ECPay 物流 API 開啟門市選擇地圖
  → 用戶選擇門市 (7-11/全家/萊爾富/OK)
  → 回傳門市資訊: 【門市名稱】【門市代號】地址
  → 儲存至訂單 receiver_address
```

#### 3.2.3 支援超商

| 超商 | 代碼 | 說明 |
|------|------|------|
| 7-11 | UNIMART | 統一超商 |
| 全家 | FAMI | 全家便利商店 |
| 萊爾富 | HILIFE | 萊爾富 |
| OK | OKMART | OK 便利商店 |

#### 3.2.4 建議 Java 實作

**需建立**:
- `ShopShippingConfig` - 物流設定
- `EcpayLogisticsService` - ECPay 物流 SDK 整合
- `ShopShippingController` - 物流 API
  - `POST /shop/shipping/cvs/map` - 開啟超商地圖
  - `POST /shop/shipping/cvs/callback` - 門市選擇回調
  - `POST /shop/shipping/create` - 建立物流單
  - `POST /shop/shipping/status/callback` - 物流狀態回調

---

### 3.3 折扣系統 ✅ 已完成 (方案 A)

**PHP 原始實作**:

#### 3.3.1 折扣模式

| dismode | 名稱 | 計算公式 | 範例 | Java 實作 |
|---------|------|---------|------|-----------|
| 0 | 無折扣 | 原價 | - | ✅ |
| 1 | 加價折扣 | `originalDisplayPrice = round(price/100)*rate + price` | discount=10, 售價 90 → 顯示原價 100 | ✅ |
| 2 | 原價折扣 | `finalPrice = price - (price/100) * discount` | discount=10, 原價 100 → 售價 90 | ✅ |

#### 3.3.2 商品特價

| 欄位 | 說明 | Java 欄位 |
|------|------|-----------|
| sprice | 特惠價 (>0 時啟用) | sale_price |
| sdate | 特惠到期日 | sale_end_date |

**優先順序**: 商品特價 (sale_price 未過期) > 全站折扣 (mode 1/2) > 原價

#### 3.3.3 Java 實作 ✅

**已建立**:
- `ShopPriceService` - 統一價格計算服務
  - `calculatePrice(price, salePrice, saleEndDate)` - 核心計算
  - `enrichProductPrices(List<ShopProduct>)` - 批次填充 finalPrice/discountLabel/originalDisplayPrice
  - `calculateSkuPrice(skuPrice, skuSalePrice, productSalePrice, saleEndDate)` - SKU 價格計算
- `PriceResult` - 價格計算結果 VO (finalPrice, discountLabel, originalDisplayPrice)
- `ShopProduct` 新增欄位: salePrice, saleEndDate, finalPrice(transient), discountLabel(transient), originalDisplayPrice(transient)
- `ShopProductSku` 新增欄位: salePrice
- `ShopFrontController` - 所有商品列表/詳情端點整合 priceService.enrichProductPrices()
- `ShopCheckoutServiceImpl` - 結帳金額計算整合折扣價格

**前端**:
- `product/edit.vue` - 後台商品編輯增加特惠價、特價結束時間欄位
- `ProductCard.vue` - 商品卡片顯示 finalPrice + 劃線原價 + 折扣標籤
- `product/detail.vue` - 商品詳情頁顯示折扣資訊

**設定 (sys_config)**:
| config_key | 說明 |
|------------|------|
| shop.discount.mode | 折扣模式 (0=無/1=加價/2=原價) |
| shop.discount.rate | 折扣百分比 |

**方案 B - 進階模式** (未來擴展，尚未實作):
- 建立 `shop_promotion` 促銷活動表
- 建立 `shop_coupon` 優惠券表
- 建立 `shop_coupon_usage` 使用記錄表
- 支援多種優惠類型 (滿減、折扣、優惠券)

---

### 3.4 滿額禮物 ❌

**PHP 原始實作**:

#### 3.4.1 資料表: `gifts`

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | INT PK | 禮物 ID |
| gname | VARCHAR | 禮物名稱 |
| pic | LONGTEXT | 圖片 (Base64) |
| price | INT | 消費門檻金額 |
| stock | INT | 庫存數量 |

#### 3.4.2 業務規則

1. 系統設定 `system.gift = 1` 時啟用
2. 每筆訂單最多選擇 **1 個** 禮物（不累計）
3. 禮物篩選: `WHERE price <= 訂單金額 AND stock > 0 ORDER BY price DESC`
4. 庫存扣除時機: **訂單標記出貨時** (非下單時)
5. 庫存為 0 的禮物不顯示
6. 用戶可選擇符合資格的任何禮物

#### 3.4.3 建議 Java 實作

**資料表**: `shop_gift`

| 欄位 | 型別 | 說明 |
|------|------|------|
| gift_id | BIGINT PK | 禮物 ID |
| name | VARCHAR(100) | 禮物名稱 |
| image_url | VARCHAR(500) | 圖片 URL (改用檔案上傳) |
| threshold_amount | DECIMAL(10,2) | 消費門檻金額 |
| stock_quantity | INT | 庫存數量 |
| status | VARCHAR(20) | 狀態 |
| create_time | DATETIME | 建立時間 |

**需建立**:
- `ShopGift` 實體
- `ShopGiftMapper` + XML
- `IShopGiftService` + Impl
- `ShopGiftController` (後台 CRUD)
- 結帳流程增加禮物選擇邏輯
- `shop_order` 增加 `gift_id` 欄位

---

### 3.5 運費設定 ✅ 已完成

**PHP 原始實作**:

#### 3.5.1 運費規則

| 設定 | 說明 |
|------|------|
| free_shipping | 免運門檻金額 |
| shipping_fee | 本島運費 |
| shipping_fee_island | 離島運費 |
| shipping_fee_overseas | 海外運費 |

**計算邏輯**:
```
if (訂單金額 >= free_shipping) → 免運
else → 依地區計算運費
```

#### 3.5.2 Java 實作 ✅

已從硬編碼改為 sys_config 可配置化:

| 設定鍵 (sys_config) | 預設值 | 說明 |
|---------------------|--------|------|
| shop.shipping.free_threshold | 1000 | 免運門檻 |
| shop.shipping.domestic_fee | 60 | 本島運費 |
| shop.shipping.island_fee | 150 | 離島運費 |

**離島判斷邏輯**: 收件地址包含「金門、馬祖、澎湖、蘭嶼、綠島、小琉球、連江」等關鍵字
**實作位置**: `ShopCheckoutServiceImpl.calculateShippingFee()`

---

### 3.6 部落格/文章系統 ❌

**PHP 原始實作**:

#### 3.6.1 資料表: `blog`

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | INT PK | 文章 ID |
| title | VARCHAR | 標題 |
| content | LONGTEXT | HTML 內容 |
| pic | TEXT | 縮圖 |
| related_product | VARCHAR | 關聯商品 ID |
| status | TINYINT | 0=隱藏, 1=顯示 |
| cdate | DATETIME | 建立時間 |
| udate | DATETIME | 更新時間 |

#### 3.6.2 功能清單

- 文章 CRUD
- 富文本編輯器 (Trumbowyg)
- 文章與商品關聯
- 前台分頁列表、詳情頁
- 最新 6 篇展示於首頁

#### 3.6.3 建議 Java 實作

**資料表**: `shop_article`

| 欄位 | 型別 | 說明 |
|------|------|------|
| article_id | BIGINT PK | 文章 ID |
| title | VARCHAR(200) | 標題 |
| summary | VARCHAR(500) | 摘要 |
| content | LONGTEXT | HTML 內容 |
| cover_image | VARCHAR(500) | 封面圖 |
| product_id | BIGINT | 關聯商品 (可選) |
| status | VARCHAR(20) | DRAFT / PUBLISHED |
| sort_order | INT | 排序 |
| view_count | INT | 瀏覽數 |
| create_time | DATETIME | 建立時間 |
| update_time | DATETIME | 更新時間 |

---

### 3.7 電子報 ❌

**PHP 原始實作**:

#### 3.7.1 資料表: `newsletters`

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | INT PK | ID |
| email | VARCHAR | 訂閱者 Email |
| cdate | DATETIME | 訂閱時間 |

#### 3.7.2 功能

- 訂閱/取消訂閱
- 群發郵件 (PHPMailer + SMTP)
- 後台管理訂閱者名單

#### 3.7.3 建議 Java 實作

使用現有 Spring Boot Mail 配合 `shop_newsletter` 表實作。

---

### 3.8 簡訊通知 ❌

**PHP 原始實作**:

#### 3.8.1 通知場景

| 場景 | 觸發時機 | 接收者 |
|------|---------|--------|
| 新訂單通知 | 訂單建立 | 管理員 (最多 5 組電話) |
| 出貨通知 | 標記出貨 | 會員 |
| 驗證碼 | 會員註冊 | 會員 |

#### 3.8.2 建議 Java 實作

可整合 LINE Notify 或 SMS 服務商 API，透過事件機制觸發。

---

### 3.9 資料匯出/匯入 ❌

**PHP 原始實作**:

#### 3.9.1 匯出功能

| 資料類型 | 格式 | 篩選條件 |
|---------|------|---------|
| 訂單 | Excel (XLS) | 日期範圍 |
| 商品 | Excel (XLS) | 全量 |
| 會員 | Excel (XLS) | 全量 |
| 資料庫備份 | SQL | 指定表 |

#### 3.9.2 匯入功能

| 資料類型 | 格式 | 規則 |
|---------|------|------|
| 商品批量匯入 | Excel | 20 欄固定結構、至少 3 行 |
| Yahoo 商品匯入 | 爬蟲 | 賣家 ID + 分類 |

#### 3.9.3 商品 Excel 匯入欄位 (20 欄)

1. 商品編號 (pnum)
2. 商品名稱 (pname)
3. 原價 (price)
4. 特惠價 (sprice)
5. 成本 (cost)
6. 庫存 (stock)
7. 購買限制 (buy_limited)
8. 主圖 URL (images)
9. 詳細圖 URL (info_images)
10. 商品描述 (info)
11. YouTube URL (youtube)
12. 規格 1 名稱
13. 規格 1 選項 (逗號分隔)
14. 規格 2 名稱
15. 規格 2 選項 (逗號分隔)
16. 主分類 ID
17. 次分類 ID
18. 關聯商品 ID (逗號分隔)
19. 規格對應圖片
20. 特惠到期日

#### 3.9.4 建議 Java 實作

使用 Apache POI 或 EasyExcel 實作匯出/匯入。

---

### 3.10 統計報表 ❌

**PHP 原始實作**:

#### 3.10.1 報表類型

| 報表 | 說明 | 篩選 |
|------|------|------|
| 獲利統計 | 訪客數 vs 訂單數折線圖 | 日期範圍 |
| 月營收 | 月度營收統計 | 年份 |
| 年營收 | 年度營收統計 | - |
| 訂單統計 | 已出貨/未出貨/未付款數量 | - |

#### 3.10.2 計算規則

- 排除未出貨及金流未完成的訂單
- 訪客以 IP 計算唯一數
- 營收 = Σ 已出貨訂單的 total

---

### 3.11 系統設定 (商城專屬) ❌

**PHP 原始實作中的商城相關設定**:

| 設定鍵 | 說明 | 型別 |
|--------|------|------|
| shop_name | 商店名稱 | VARCHAR |
| shop_url | 商店網址 | VARCHAR |
| shop_logo | Logo URL | VARCHAR |
| dismode | 折扣模式 (0/1/2) | TINYINT |
| discount | 折扣百分比 | INT |
| gift | 滿額禮開關 | TINYINT |
| need_reg | 強制註冊購物 | TINYINT |
| show_stock | 顯示庫存 | TINYINT |
| free_shipping | 免運門檻 | INT |
| shipping_fee | 本島運費 | INT |
| gstatus | 金流環境 (0=測試/1=正式) | TINYINT |
| hashkey | ECPay HashKey | VARCHAR |
| hashiv | ECPay HashIV | VARCHAR |
| merchantid | ECPay 特店編號 | VARCHAR |
| pay_methods | 啟用的付款方式 | VARCHAR (JSON) |
| cvs_list | 啟用的超商 | VARCHAR (JSON) |
| sms_account | 簡訊帳號 | VARCHAR |
| sms_password | 簡訊密碼 | VARCHAR |
| sms_phones | 通知電話 (分號分隔) | VARCHAR |
| mail_smtp | SMTP 伺服器 | VARCHAR |
| mail_port | 郵件埠號 | INT |
| mail_account | 郵件帳號 | VARCHAR |
| mail_password | 郵件密碼 | VARCHAR |

#### 3.11.1 建議 Java 實作

**方案 A**: 使用現有 `sys_config` 表，以 `shop.` 前綴存放
**方案 B**: 建立獨立 `shop_config` 表

---

### 3.12 社交登入 ❌

**PHP 原始實作**:

#### 3.12.1 Facebook OAuth

```
流程:
1. 前端觸發 Facebook Login SDK
2. 取得 access_token
3. 呼叫 Graph API 取得用戶資訊 (fb_id, name)
4. 檢查 fb_id 是否已存在 members 表
5. 已存在 → 自動登入
6. 不存在 → 自動建立會員 (password=NULL)
```

#### 3.12.2 LINE Login

```
流程:
1. 重導向至 LINE Login 授權頁
2. 用戶同意後回調 (line_callback.php)
3. 使用 authorization code 換取 access_token
4. 呼叫 Profile API 取得 line_id, displayName
5. 檢查 line_id 是否已存在
6. 已存在 → 自動登入
7. 不存在 → 導向註冊頁 (line_reg.php) 預填資訊
```

#### 3.12.3 Java 現況

- `shop_member_social` 表已建立 (provider, provider_id, member_id)
- `SocialProvider` 枚舉已定義 (LINE, GOOGLE, FACEBOOK)
- 實際 OAuth 流程 **尚未實作**

---

## 四、PHP → Java 欄位對照表

### 4.1 付款方式對照

| PHP pay_method | 說明 | Java PaymentMethod | Java ShippingMethod |
|---------------|------|-------------------|-------------------|
| 1 | 宅配貨到付款 | COD | HOME_DELIVERY |
| 2 | 宅配線上付款 | CREDIT_CARD | HOME_DELIVERY |
| 3 | 超商取貨付款 | COD | CVS_PICKUP |
| 4 | 超商取貨線上付款 | CREDIT_CARD | CVS_PICKUP |

### 4.2 訂單狀態對照

| PHP 狀態 | 判斷條件 | Java OrderStatus |
|---------|---------|-----------------|
| 金流未完成 | rtncode = 0 or NULL | PENDING |
| 已付款 | rtncode = 1 | PAID |
| 處理中 | - | PROCESSING |
| 已出貨 | pdate IS NOT NULL | SHIPPED |
| 已送達 | - | DELIVERED |
| 已完成 | - | COMPLETED |
| 已取消 | - | CANCELLED |

### 4.3 商品狀態對照

| PHP status | 說明 | Java ProductStatus |
|-----------|------|-------------------|
| 0 | 正常顯示 | ON_SALE |
| 1 | 隱藏 | OFF_SALE |
| - | - | DRAFT (新增) |
| - | - | PREVIEW (新增) |

---

## 五、建議遷移優先順序

### 第一階段 - 核心交易閉環 ✅ 全部完成

| # | 功能 | 優先級 | 狀態 | 備註 |
|---|------|--------|------|------|
| 1 | 折扣系統 (商品特價 + 全站折扣) | P0 | ✅ | ShopPriceService, mode 0/1/2 |
| 2 | 運費設定 (可配置化) | P0 | ✅ | sys_config + 離島判斷 |
| 3 | 金流整合 (ECPay) | P0 | ✅ | EcpayService + SHA256 CheckMacValue |
| 4 | 出貨流程 (標記出貨 + 事務保護) | P0 | ✅ | @Transactional 補強 |

**Flyway 遷移**: `V38__shop_mall_p0_p1_ecpay_marketing.sql`
**建置驗證**: 前後端均通過編譯 (2026-01-29)

### 第二階段 - 行銷與促銷

| # | 功能 | 優先級 | 依賴 |
|---|------|--------|------|
| 5 | 滿額禮物 | P1 | 結帳流程 |
| 6 | 熱門/特價商品管理頁 | P1 | 商品管理 |
| 7 | 部落格/文章系統 | P1 | 無 |

### 第三階段 - 營運工具

| # | 功能 | 優先級 | 依賴 |
|---|------|--------|------|
| 8 | 資料匯出 (Excel) | P2 | 訂單/商品/會員 |
| 9 | 資料匯入 (Excel) | P2 | 商品管理 |
| 10 | 統計報表 | P2 | 訂單系統 |
| 11 | 系統設定 (商城) | P2 | 無 |

### 第四階段 - 用戶體驗

| # | 功能 | 優先級 | 依賴 |
|---|------|--------|------|
| 12 | 社交登入 (LINE + Facebook) | P3 | 會員系統 |
| 13 | 物流整合 (超商取貨) | P3 | 金流整合 |
| 14 | 簡訊/通知 | P3 | 訂單系統 |
| 15 | 電子報 | P3 | 會員系統 |

### 第五階段 - 延伸功能

| # | 功能 | 優先級 | 依賴 |
|---|------|--------|------|
| 16 | 多語言支援 | P4 | 系統設定 |
| 17 | 訪客追蹤 | P4 | 無 |
| 18 | SEO 關鍵字 | P4 | 無 |

---

## 六、資料庫遷移清單

以下 Flyway 遷移腳本需建立:

| 遷移檔案 | 內容 | 狀態 |
|---------|------|------|
| `V38__shop_mall_p0_p1_ecpay_marketing.sql` | 商品特價欄位 + SKU 特價 + ECPay 欄位 + sys_config 設定 (折扣/運費/金流) | ✅ 已建立 |
| `V{N}__shop_add_gift.sql` | 滿額禮物表 | ❌ |
| `V{N}__shop_add_article.sql` | 文章/部落格表 | ❌ |
| `V{N}__shop_add_newsletter.sql` | 電子報表 | ❌ |
| `V{N}__shop_add_statistics.sql` | 統計相關表/視圖 | ❌ |
| `V{N}__shop_order_add_gift.sql` | 訂單增加 gift_id 欄位 | ❌ |

---

## 七、技術升級對照

| 項目 | PHP 原始方案 | Java 升級方案 |
|------|------------|-------------|
| 密碼雜湊 | MD5 | BCrypt (Spring Security) |
| Session | PHP Session | JWT Token |
| 購物車 | Session + JSON | DB 持久化 + 選中機制 ✅ |
| 商品規格 | JSON 欄位 | 獨立 SKU 表 ✅ |
| 分類 | 二層固定 | 無限層級樹狀 ✅ |
| 圖片存儲 | Base64/Google Drive | 檔案上傳服務 |
| 模板 | Twig | Vue 3 SPA ✅ |
| ORM | 自製 MysqliDb | MyBatis ✅ |
| 匯出 | 原生 XLS 輸出 | Apache POI / EasyExcel |
| 金流 | ECPay PHP SDK | ECPay HTTP 表單 + SHA256 ✅ |
| 通知 | SMS API | LINE Notify + SMS |

---

## 八、附錄 - PHP 原始檔案索引

### 前台核心檔案

| 檔案 | 功能 | 遷移目標 |
|------|------|---------|
| index.php | 首頁 | views/shop-front/home ✅ |
| product.php | 商品詳情 | views/shop-front/product/detail ✅ |
| cart.php | 購物車 | views/shop-front/cart ✅ |
| order.php | 結帳頁 | views/shop-front/checkout ✅ |
| order_submit.php | 訂單提交 | ShopCheckoutController ✅ |
| order_list.php | 訂單列表 | views/shop-front/member/orders ✅ |
| login.php | 登入 | views/shop-front/auth/login ✅ |
| register.php | 註冊 | views/shop-front/auth/register ✅ |
| get_product.php | 商品 API | ShopFrontController ✅ |
| get_discount.php | 折扣 API | ✅ ShopPriceService |
| get_gifts.php | 禮物 API | ❌ |
| ECPay.Payment.Integration.php | 金流 SDK | ✅ EcpayService |
| Ecpay.Logistic.Integration.php | 物流 SDK | ❌ |
| fb_login.php | FB 登入 | ❌ |
| line_callback.php | LINE 回調 | ❌ |
| search.php | 搜尋 | ❌ |
| blogs.php | 部落格 | ❌ |

### 後台管理檔案

| 檔案 | 功能 | 遷移目標 |
|------|------|---------|
| admin/orders.php | 訂單管理 | views/shop/order ✅ |
| admin/products.php | 商品管理 | views/shop/product ✅ |
| admin/members.php | 會員管理 | views/shop/member ✅ |
| admin/system.php | 系統設定 | ❌ |
| admin/blog.php | 文章管理 | ❌ |
| admin/excel_export.php | Excel 匯出 | ❌ |
| admin/excel_products.php | 商品匯出 | ❌ |
| admin/gift_update.php | 禮物管理 | ❌ |
| admin/new_discount.php | 特價管理 | ❌ |
| admin/get_chart.php | 統計圖表 | ❌ |
| admin/pay_month.php | 月報 | ❌ |
| admin/pay_year.php | 年報 | ❌ |
| admin/print_orders.php | 列印訂單 | ❌ |

---

> **使用方式**: 開發時對照此規格書，完成一項功能後將狀態改為 ✅，即可追蹤進度。
