# 綠界物流整合可執行規格書

**版本**：1.0.0
**日期**：2026-02-08
**作者**：cheng
**狀態**：草稿

---

## 目錄

1. [需求概述](#1-需求概述)
2. [系統架構](#2-系統架構)
3. [資料模型](#3-資料模型)
4. [API 設計](#4-api-設計)
5. [前端實作](#5-前端實作)
6. [綠界整合](#6-綠界整合)
7. [防呆與異常處理](#7-防呆與異常處理)
8. [測試計畫](#8-測試計畫)
9. [實作順序](#9-實作順序)

---

## 1. 需求概述

### 1.1 業務背景

現有商城結帳流程（`ShopCheckoutServiceImpl`）已支援地址管理，但 `ShippingMethod` 枚舉中的超商取貨選項（CVS_711、CVS_FAMILY、CVS_HILIFE）尚未實際串接綠界物流。使用者在結帳時選擇超商取貨後，必須透過綠界電子地圖選取門市，系統才能在付款後自動建立物流訂單。

### 1.2 範疇

| 項目 | 是否包含 |
|------|---------|
| 超商取貨（7-11、全家、萊爾富）B2C 大宗寄倉 | 是 |
| 宅配到府（黑貓）| 是（呼叫建立物流訂單，不含電子地圖） |
| 超商 C2C 店到店 | 否（後續版本） |
| 物流狀態追蹤 Webhook | 是 |
| 退貨逆物流 | 否（後續版本） |

### 1.3 核心流程

```
使用者選擇超商取貨
    → 開啟綠界電子地圖 (iframe/popup)
    → 使用者選取門市
    → 綠界回調帶回門市資訊
    → 儲存至暫存表
    → 結帳提交（關聯門市資訊）
    → 付款成功事件觸發
    → 自動建立物流訂單
    → 取得物流單號，更新 shop_order.shipping_no
```

### 1.4 金流物流組合規則

| 付款方式 | 物流方式 | 備註 |
|---------|---------|------|
| ECPAY（信用卡、ATM） | 宅配 / 超商取貨 | 正常支援 |
| ECPAY（超商代碼付款 CVS） | 超商取貨 | 同一超商的限制見第 6 節 |
| COD（貨到付款） | 僅宅配 | 超商取貨不支援代收 |

---

## 2. 系統架構

### 2.1 物流選擇流程圖

```
┌─────────────────────────────────────────────────────────────────┐
│                          結帳流程                                │
└─────────────────────────────────────────────────────────────────┘

[前端 checkout/index.vue]
        │
        ├─ 選擇物流方式
        │       │
        │       ├─ HOME_DELIVERY (宅配)
        │       │       └─ 填寫收貨地址 → 正常結帳
        │       │
        │       └─ CVS_711 / CVS_FAMILY / CVS_HILIFE (超商取貨)
        │               └─ 點擊「選擇門市」按鈕
        │                       │
        │                       ▼
        │               [開啟電子地圖視窗]
        │                       │
        │                       ▼
        │               [ECPay 電子地圖頁面]
        │               https://logistics-stage.ecpay.com.tw/Express/map
        │                       │
        │               使用者選取門市後，ECPay POST 回 ServerReplyURL
        │                       │
        │                       ▼
        │               [後端 /shop/logistics/cvs/store/callback]
        │                       │
        │               儲存門市資訊至 shop_cvs_store_temp
        │                       │
        │               透過 WebSocket 或 Polling 通知前端
        │                       │
        │                       ▼
        │               [前端顯示已選門市]
        │                       │
        ▼                       ▼
[使用者點擊提交訂單]
        │
        ▼
[POST /shop/checkout/submit（含 cvsStoreKey）]
        │
        ▼
[ShopCheckoutServiceImpl.submit]
        │
        ├─ 驗證門市暫存資料有效
        ├─ 建立訂單（含超商門市資訊冗餘）
        └─ 清除暫存資料
                │
                ▼
        [付款成功事件 PaymentSuccessEvent]
                │
                ▼
        [OrderEventListener]
                │
                ▼
        [EcpayLogisticsGateway.createShipment]
                │
                ▼
        POST https://logistics-stage.ecpay.com.tw/Express/Create
                │
                ▼
        取得 AllPayLogisticsID → 更新 shop_order.shipping_no
```

### 2.2 組件交互圖

```
┌──────────────────────────────────────────────────────────────────┐
│                        cheng-shop 模組                           │
│                                                                  │
│  ┌─────────────────┐     ┌───────────────────────────────────┐  │
│  │ ShopPaymentCtrl │     │ ShopLogisticsController (新增)    │  │
│  │  (既有)         │     │  POST /shop/logistics/map/url     │  │
│  │  ECPay 金流回調 │     │  POST /shop/logistics/cvs/store/  │  │
│  └────────┬────────┘     │       callback                    │  │
│           │              └────────────────┬──────────────────┘  │
│           │                               │                      │
│           ▼                               ▼                      │
│  ┌─────────────────┐     ┌───────────────────────────────────┐  │
│  │ PaymentSuccess  │     │ IShopLogisticsService (新增)      │  │
│  │    Event        │     │  + generateMapUrl()               │  │
│  └────────┬────────┘     │  + saveCvsStore()                 │  │
│           │              │  + getCvsStore()                  │  │
│           ▼              └────────────────┬──────────────────┘  │
│  ┌─────────────────┐                      │                      │
│  │ OrderEvent      │     ┌────────────────▼──────────────────┐  │
│  │  Listener       │     │ ShopCvsStoreTempMapper (新增)     │  │
│  │  (既有，擴充)   │     │  + insert / selectByKey / delete  │  │
│  └────────┬────────┘     └───────────────────────────────────┘  │
│           │                                                      │
│           ▼                                                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ EcpayLogisticsGateway (新增)                            │    │
│  │  + createShipment(ShopOrder) : LogisticsResult          │    │
│  │  + generateMapUrl(params) : String                      │    │
│  │  + verifyCallback(params) : boolean                     │    │
│  │  + generateCheckMacValue(params, key, iv) : String      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### 2.3 與現有程式的關係

| 現有類別 | 變更類型 | 說明 |
|---------|---------|------|
| `ShopCheckoutServiceImpl` | 修改 | 新增物流方式驗證、超商門市關聯 |
| `CheckoutSubmitRequest` | 修改 | 新增 `shippingMethod`、`cvsStoreKey` 欄位 |
| `OrderEventListener` | 修改 | 付款成功後觸發物流訂單建立 |
| `ShopOrder` | 修改 | 新增超商門市相關欄位 |
| `EcpayPaymentGateway` | 不變 | 金流邏輯維持現有 |

---

## 3. 資料模型

### 3.1 超商門市暫存表

```sql
-- Flyway: V41__shop_logistics_cvs_store_temp.sql

CREATE TABLE shop_cvs_store_temp
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    store_key     VARCHAR(64)  NOT NULL COMMENT '前端識別 key（UUID），避免 memberId 碰撞',
    member_id     BIGINT       NOT NULL COMMENT '會員ID',
    logistics_sub VARCHAR(20)  NOT NULL COMMENT '物流子類型：FAMI/UNIMART/HILIFE',
    store_id      VARCHAR(20)  NOT NULL COMMENT '門市代號',
    store_name    VARCHAR(100) NOT NULL COMMENT '門市名稱',
    store_address VARCHAR(200)          DEFAULT NULL COMMENT '門市地址',
    store_tel     VARCHAR(20)           DEFAULT NULL COMMENT '門市電話',
    cvs_outside   VARCHAR(1)            DEFAULT NULL COMMENT '是否離島（1=是）',
    expire_time   DATETIME     NOT NULL COMMENT '過期時間（建立後 30 分鐘）',
    create_time   DATETIME              DEFAULT NULL COMMENT '建立時間',
    PRIMARY KEY (id),
    UNIQUE KEY uk_store_key (store_key),
    KEY idx_member_id (member_id),
    KEY idx_expire_time (expire_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='超商門市選取暫存表';
```

### 3.2 shop_order 表新增欄位

```sql
-- Flyway: V41__shop_logistics_cvs_store_temp.sql（同一遷移檔）

ALTER TABLE shop_order
    ADD COLUMN cvs_store_id      VARCHAR(20)  DEFAULT NULL COMMENT '超商門市代號'     AFTER shipping_method,
    ADD COLUMN cvs_store_name    VARCHAR(100) DEFAULT NULL COMMENT '超商門市名稱'     AFTER cvs_store_id,
    ADD COLUMN cvs_store_address VARCHAR(200) DEFAULT NULL COMMENT '超商門市地址'     AFTER cvs_store_name,
    ADD COLUMN logistics_sub_type VARCHAR(20) DEFAULT NULL COMMENT '物流子類型'       AFTER cvs_store_address;
```

### 3.3 ShippingMethod 枚舉確認

現有枚舉已足夠，無需新增：

```java
// 現有，不需修改
public enum ShippingMethod implements CodedEnum<String> {
    HOME_DELIVERY("HOME_DELIVERY", "宅配到府"),
    CVS_711("CVS_711", "7-11 超取"),
    CVS_FAMILY("CVS_FAMILY", "全家超取"),
    CVS_HILIFE("CVS_HILIFE", "萊爾富超取"),
    STORE_PICKUP("STORE_PICKUP", "門市自取");
    // ...
    public boolean isCvs() { ... }  // 已存在
}
```

新增輔助方法以對應綠界 LogisticsSubType：

```java
// 新增方法：取得綠界 LogisticsSubType
public String getEcpayLogisticsSubType() {
    return switch (this) {
        case CVS_711    -> "UNIMART";
        case CVS_FAMILY -> "FAMI";
        case CVS_HILIFE -> "HILIFE";
        default         -> null;
    };
}
```

### 3.4 sys_config 設定項

```sql
-- Flyway: V41__shop_logistics_cvs_store_temp.sql（同一遷移檔）

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES
    -- 綠界物流 B2C 帳號
    ('綠界物流 MerchantID (B2C)',    'shop.ecpay.logistics.merchant_id', '2000132',           'Y', 'admin', NOW(), '綠界物流 B2C 商店代號（測試用 2000132）'),
    ('綠界物流 HashKey (B2C)',       'shop.ecpay.logistics.hash_key',    '5294y06JbISpM5x9',  'Y', 'admin', NOW(), '綠界物流 B2C HashKey'),
    ('綠界物流 HashIV (B2C)',        'shop.ecpay.logistics.hash_iv',     'v77hoKGq4kWxNNIS',  'Y', 'admin', NOW(), '綠界物流 B2C HashIV'),
    ('綠界物流模式',                  'shop.ecpay.logistics.mode',        'test',              'Y', 'admin', NOW(), 'test=測試環境, prod=正式環境'),
    -- 電子地圖回調 URL 設定
    ('物流電子地圖回調BaseURL',       'shop.logistics.callback_base_url', '',                  'Y', 'admin', NOW(), '公開可存取的後端 URL（含 ngrok），供綠界回調超商選取結果'),
    -- 宅配設定
    ('宅配寄件人姓名',                'shop.logistics.sender_name',       '',                  'Y', 'admin', NOW(), '宅配寄件人姓名'),
    ('宅配寄件人電話',                'shop.logistics.sender_phone',      '',                  'Y', 'admin', NOW(), '宅配寄件人電話'),
    ('宅配寄件人地址',                'shop.logistics.sender_address',    '',                  'Y', 'admin', NOW(), '宅配寄件人完整地址'),
    ('宅配寄件人郵遞區號',            'shop.logistics.sender_zip',        '',                  'Y', 'admin', NOW(), '宅配寄件人郵遞區號（3 碼）'),
    -- 超商門市暫存過期分鐘數
    ('門市暫存過期分鐘',              'shop.logistics.cvs_store_expire_minutes', '30',          'Y', 'admin', NOW(), '超商門市暫存有效分鐘數');
```

---

## 4. API 設計

### 4.1 後端 API 端點清單

| 方法 | 路徑 | 說明 | 認證 |
|------|------|------|------|
| GET  | `/shop/logistics/methods` | 查詢可用物流方式 | 無 |
| POST | `/shop/logistics/map/url` | 取得電子地圖 URL | 會員 JWT |
| POST | `/shop/logistics/cvs/store/callback` | 電子地圖回調（接收門市） | 無 |
| GET  | `/shop/logistics/cvs/store/{storeKey}` | 查詢暫存門市資訊 | 會員 JWT |
| POST | `/shop/logistics/shipment/create` | 手動補建物流訂單 | 後台權限 |

### 4.2 GET /shop/logistics/methods

**用途**：結帳頁載入可用物流方式列表（含運費）

**請求**：
```http
GET /shop/logistics/methods?productAmount=1500
Authorization: Bearer {memberToken}
```

**回應**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "code": "HOME_DELIVERY",
      "name": "宅配到府",
      "description": "黑貓宅配，3-5 個工作天",
      "fee": 0,
      "freeThreshold": 1000,
      "requireAddress": true,
      "requireCvsStore": false
    },
    {
      "code": "CVS_711",
      "name": "7-11 超取",
      "description": "7-11 超商取貨，2-3 個工作天",
      "fee": 60,
      "freeThreshold": 0,
      "requireAddress": false,
      "requireCvsStore": true
    },
    {
      "code": "CVS_FAMILY",
      "name": "全家超取",
      "description": "全家便利商店取貨，2-3 個工作天",
      "fee": 60,
      "freeThreshold": 0,
      "requireAddress": false,
      "requireCvsStore": true
    },
    {
      "code": "CVS_HILIFE",
      "name": "萊爾富超取",
      "description": "萊爾富便利商店取貨，2-3 個工作天",
      "fee": 60,
      "freeThreshold": 0,
      "requireAddress": false,
      "requireCvsStore": true
    }
  ]
}
```

### 4.3 POST /shop/logistics/map/url

**用途**：前端請求綠界電子地圖 URL，後端組裝含 CheckMacValue 的表單

**請求**：
```http
POST /shop/logistics/map/url
Authorization: Bearer {memberToken}
Content-Type: application/json

{
  "shippingMethod": "CVS_711",
  "storeKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**欄位說明**：
- `shippingMethod`：對應 ShippingMethod 枚舉代碼，必須是 CVS 類型
- `storeKey`：前端產生的 UUID，作為此次選店的識別碼，也用於後續查詢暫存結果

**回應**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "formHtml": "<form method='post' action='https://logistics-stage.ecpay.com.tw/Express/map'>...</form>"
  }
}
```

**說明**：前端收到 `formHtml` 後，將其注入頁面並執行 `form.submit()`，瀏覽器跳轉至綠界電子地圖頁面。選店完成後，綠界呼叫 `ServerReplyURL` 帶回門市資訊。

### 4.4 POST /shop/logistics/cvs/store/callback

**用途**：接收綠界電子地圖選店回調

**請求**（綠界 POST，form-encoded）：
```
MerchantID=2000132
MerchantTradeNo={storeKey}
CVSStoreID=131386
CVSStoreName=台大門市
CVSAddress=台北市大安區羅斯福路四段1號
CVSTelephone=02-23620303
CVSOutSide=0
ExtraData=
```

**回應**：
```
1|OK
```

**處理邏輯**：
1. 驗證 MerchantID 是否匹配
2. 以 `MerchantTradeNo`（即 storeKey）從資料庫查詢是否有對應的暫存記錄
3. 將門市資訊寫入 `shop_cvs_store_temp`（若已存在則更新）
4. 回傳 `1|OK` 告知綠界接收成功

**注意**：此端點必須標記 `@Anonymous`，不需要認證，因為是綠界伺服器呼叫。

### 4.5 GET /shop/logistics/cvs/store/{storeKey}

**用途**：前端輪詢查詢門市選取結果

**請求**：
```http
GET /shop/logistics/cvs/store/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Authorization: Bearer {memberToken}
```

**回應（已選取）**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "storeKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "storeId": "131386",
    "storeName": "台大門市",
    "storeAddress": "台北市大安區羅斯福路四段1號",
    "storeTel": "02-23620303",
    "logisticsSubType": "UNIMART",
    "expireTime": "2026-02-08T14:30:00"
  }
}
```

**回應（尚未選取）**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 4.6 結帳提交 API 變更

現有 `CheckoutSubmitRequest` 需新增物流相關欄位：

```java
// 修改後的 CheckoutSubmitRequest.java
@Data
public class CheckoutSubmitRequest {

    @NotNull(message = "請選擇收貨地址")
    private Long addressId;

    private String remark;

    private String paymentMethod = "COD";

    private Long giftId;

    // 新增欄位 ---

    /** 物流方式（對應 ShippingMethod 枚舉） */
    @NotBlank(message = "請選擇物流方式")
    private String shippingMethod;

    /** 超商門市暫存 key（選擇超商取貨時必填） */
    private String cvsStoreKey;
}
```

**POST /shop/checkout/submit 請求範例（超商取貨）**：
```json
{
  "addressId": null,
  "remark": "",
  "paymentMethod": "ECPAY",
  "shippingMethod": "CVS_711",
  "cvsStoreKey": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**POST /shop/checkout/submit 請求範例（宅配）**：
```json
{
  "addressId": 42,
  "remark": "請放門口",
  "paymentMethod": "ECPAY",
  "shippingMethod": "HOME_DELIVERY"
}
```

### 4.7 錯誤碼定義

| HTTP 狀態碼 | 業務錯誤碼 | 訊息 | 說明 |
|------------|-----------|------|------|
| 400 | LOGISTICS_001 | 物流方式不支援 | shippingMethod 不合法 |
| 400 | LOGISTICS_002 | 超商門市尚未選取 | cvsStoreKey 對應的暫存不存在 |
| 400 | LOGISTICS_003 | 超商門市選取已過期 | 暫存 expireTime 已過 |
| 400 | LOGISTICS_004 | 付款方式與物流方式不相容 | 例如 COD + 超商取貨 |
| 500 | LOGISTICS_005 | 物流訂單建立失敗 | 呼叫綠界 API 失敗 |
| 500 | LOGISTICS_006 | 電子地圖 URL 產生失敗 | 設定缺失或簽章錯誤 |

---

## 5. 前端實作

### 5.1 結帳頁物流選擇 UI 設計

在現有 `checkout/index.vue` 中，「付款方式」區塊上方新增「物流方式」區塊：

```html
<!-- 物流方式（新增於付款方式區塊前） -->
<div class="checkout-section">
  <h2>物流方式</h2>
  <el-radio-group v-model="shippingMethod" class="shipping-group" @change="handleShippingChange">
    <el-radio
      v-for="method in availableShippingMethods"
      :key="method.code"
      :value="method.code"
      size="large"
      class="shipping-radio"
    >
      <div class="shipping-option">
        <span class="shipping-name">{{ method.name }}</span>
        <span class="shipping-desc">{{ method.description }}</span>
        <span class="shipping-fee" v-if="method.fee > 0">+${{ method.fee }}</span>
        <span class="shipping-fee free" v-else>免運費</span>
      </div>
    </el-radio>
  </el-radio-group>

  <!-- 超商門市選取區塊（僅在選擇超商取貨時顯示） -->
  <div v-if="isSelectedCvs" class="cvs-store-section">
    <div v-if="selectedStore" class="store-info">
      <el-icon><Location /></el-icon>
      <span class="store-name">{{ selectedStore.storeName }}</span>
      <span class="store-address">{{ selectedStore.storeAddress }}</span>
      <el-button text type="primary" @click="openCvsMap">重新選取</el-button>
    </div>
    <el-button v-else type="warning" @click="openCvsMap" :loading="loadingStore">
      <el-icon><Location /></el-icon>
      選擇取貨門市
    </el-button>
    <div v-if="storeError" class="store-error">{{ storeError }}</div>
  </div>
</div>
```

### 5.2 超商地圖選店流程

```javascript
// checkout/index.vue <script setup> 新增邏輯

import { ref, computed } from 'vue'
import { getLogisticsMethods, getMapUrl, getCvsStore } from '@/api/shop/logistics'
import { v4 as uuidv4 } from 'uuid'  // 需安裝：pnpm add uuid

const shippingMethod = ref('HOME_DELIVERY')
const availableShippingMethods = ref([])
const selectedStore = ref(null)
const storeKey = ref(null)
const loadingStore = ref(false)
const storeError = ref('')
let pollTimer = null

// 是否選擇超商
const isSelectedCvs = computed(() => {
  return ['CVS_711', 'CVS_FAMILY', 'CVS_HILIFE'].includes(shippingMethod.value)
})

// 載入物流方式
async function fetchShippingMethods() {
  try {
    const res = await getLogisticsMethods({ productAmount: checkoutData.productAmount })
    if (res.code === 200) {
      availableShippingMethods.value = res.data
      if (res.data.length > 0) {
        shippingMethod.value = res.data[0].code
      }
    }
  } catch (e) {
    console.error('載入物流方式失敗', e)
  }
}

// 物流方式切換
function handleShippingChange(val) {
  // 切換物流方式時清除已選門市
  if (!isSelectedCvs.value) {
    clearCvsStore()
  }
  // 重新計算運費
  fetchCheckoutPreview(selectedAddress.value?.addressId)
}

// 開啟電子地圖
async function openCvsMap() {
  storeError.value = ''
  loadingStore.value = true

  try {
    // 產生新的 storeKey（每次選店都產生新的，避免衝突）
    storeKey.value = uuidv4()

    const res = await getMapUrl({
      shippingMethod: shippingMethod.value,
      storeKey: storeKey.value
    })

    if (res.code === 200 && res.data?.formHtml) {
      // 開啟新視窗，注入表單並提交
      const mapWindow = window.open('', 'cvs_map', 'width=1024,height=768')
      mapWindow.document.open()
      mapWindow.document.write(res.data.formHtml)
      mapWindow.document.close()

      // 開始輪詢，等待回調結果（最多 10 分鐘）
      startPolling()
    } else {
      storeError.value = '無法開啟門市地圖，請稍後再試'
    }
  } catch (e) {
    storeError.value = '開啟門市地圖失敗'
  } finally {
    loadingStore.value = false
  }
}

// 輪詢查詢門市選取結果
function startPolling() {
  let attempts = 0
  const maxAttempts = 60  // 最多輪詢 60 次 × 10 秒 = 10 分鐘

  pollTimer = setInterval(async () => {
    attempts++
    if (attempts > maxAttempts) {
      clearPolling()
      storeError.value = '門市選取逾時，請重新選取'
      return
    }

    try {
      const res = await getCvsStore(storeKey.value)
      if (res.code === 200 && res.data) {
        selectedStore.value = res.data
        clearPolling()
        ElMessage.success(`已選取門市：${res.data.storeName}`)
      }
    } catch (e) {
      // 忽略輪詢錯誤，繼續輪詢
    }
  }, 10000)  // 每 10 秒查詢一次
}

function clearPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function clearCvsStore() {
  selectedStore.value = null
  storeKey.value = null
  clearPolling()
}

// 提交訂單前驗證物流
function validateShipping() {
  if (isSelectedCvs.value && !selectedStore.value) {
    ElMessage.warning('請選擇取貨門市')
    return false
  }
  return true
}

// 修改 handleSubmit，加入物流驗證和 shippingMethod 欄位
async function handleSubmit() {
  if (!selectedAddress.value && !isSelectedCvs.value) {
    ElMessage.warning('請選擇收貨地址')
    return
  }
  if (!validateShipping()) {
    return
  }

  // ... 確認 dialog ...

  const submitData = {
    addressId: isSelectedCvs.value ? null : selectedAddress.value.addressId,
    remark: remark.value,
    paymentMethod: paymentMethod.value,
    shippingMethod: shippingMethod.value,
    cvsStoreKey: isSelectedCvs.value ? storeKey.value : undefined
  }
  // ... 其餘流程不變 ...
}

// 組件卸載時清除輪詢
import { onUnmounted } from 'vue'
onUnmounted(() => {
  clearPolling()
})
```

### 5.3 表單驗證規則

| 欄位 | 規則 | 錯誤訊息 |
|------|------|---------|
| shippingMethod | 必填，必須是有效的 ShippingMethod 代碼 | 請選擇物流方式 |
| cvsStoreKey | 選擇超商取貨時必填 | 請選擇取貨門市 |
| addressId | 選擇宅配時必填 | 請選擇收貨地址 |
| paymentMethod + shippingMethod | COD 不得搭配超商取貨 | 貨到付款僅支援宅配 |

### 5.4 API 呼叫模組

新增 `cheng-ui/src/api/shop/logistics.js`：

```javascript
import requestShop from '@/utils/requestShop'

/**
 * 查詢可用物流方式
 */
export function getLogisticsMethods(params) {
  return requestShop({
    url: '/shop/logistics/methods',
    method: 'get',
    params
  })
}

/**
 * 取得電子地圖表單 HTML
 */
export function getMapUrl(data) {
  return requestShop({
    url: '/shop/logistics/map/url',
    method: 'post',
    data
  })
}

/**
 * 查詢超商門市暫存結果
 */
export function getCvsStore(storeKey) {
  return requestShop({
    url: `/shop/logistics/cvs/store/${storeKey}`,
    method: 'get'
  })
}
```

---

## 6. 綠界整合

### 6.1 EcpayLogisticsGateway 類別設計

**位置**：`cheng-shop/src/main/java/com/cheng/shop/logistics/EcpayLogisticsGateway.java`

```java
package com.cheng.shop.logistics;

/**
 * 綠界物流閘道
 * <p>
 * 負責：
 * <ul>
 *   <li>產生電子地圖 URL（含 CheckMacValue）</li>
 *   <li>驗證電子地圖回調簽章</li>
 *   <li>建立物流訂單（Express/Create）</li>
 * </ul>
 *
 * CheckMacValue 演算法與 EcpayPaymentGateway 相同（SHA256）
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcpayLogisticsGateway {

    private final ISysConfigService configService;

    // --- 測試環境 ---
    private static final String TEST_MAP_URL     = "https://logistics-stage.ecpay.com.tw/Express/map";
    private static final String TEST_CREATE_URL  = "https://logistics-stage.ecpay.com.tw/Express/Create";

    // --- 正式環境 ---
    private static final String PROD_MAP_URL     = "https://logistics.ecpay.com.tw/Express/map";
    private static final String PROD_CREATE_URL  = "https://logistics.ecpay.com.tw/Express/Create";

    /**
     * 產生電子地圖自動提交表單
     *
     * @param storeKey          前端識別 key（作為 MerchantTradeNo）
     * @param logisticsSubType  FAMI / UNIMART / HILIFE
     * @param serverReplyUrl    回調 URL（公開可存取）
     * @return 自動提交 HTML form
     */
    public String generateMapFormHtml(String storeKey, String logisticsSubType, String serverReplyUrl) {
        // 實作細節見 6.2
    }

    /**
     * 建立物流訂單
     *
     * @param order 訂單（需包含超商門市資訊）
     * @return 物流結果
     */
    public LogisticsResult createShipment(ShopOrder order) {
        // 實作細節見 6.3
    }

    /**
     * 產生 CheckMacValue（與金流相同演算法）
     */
    String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        // 複用 EcpayPaymentGateway 的邏輯（提取為共用工具類）
    }
}
```

### 6.2 電子地圖表單參數組裝

```java
public String generateMapFormHtml(String storeKey, String logisticsSubType, String serverReplyUrl) {
    String merchantId = getConfig("shop.ecpay.logistics.merchant_id");
    String hashKey    = getConfig("shop.ecpay.logistics.hash_key");
    String hashIv     = getConfig("shop.ecpay.logistics.hash_iv");
    String mode       = getConfig("shop.ecpay.logistics.mode");
    String mapUrl     = "prod".equals(mode) ? PROD_MAP_URL : TEST_MAP_URL;

    TreeMap<String, String> params = new TreeMap<>();
    params.put("MerchantID",       merchantId);
    params.put("MerchantTradeNo",  storeKey);          // 長度限制 20 碼
    params.put("LogisticsType",    "CVS");
    params.put("LogisticsSubType", logisticsSubType);   // FAMI/UNIMART/HILIFE
    params.put("IsCollection",     "N");               // N=不代收貨款
    params.put("ServerReplyURL",   serverReplyUrl);
    params.put("Device",           "0");               // 0=PC, 1=Mobile

    String checkMac = generateCheckMacValue(params, hashKey, hashIv);
    params.put("CheckMacValue", checkMac);

    return buildAutoSubmitForm(mapUrl, params);
}
```

**MerchantTradeNo 限制**：最長 20 碼。UUID 長度為 36 碼（含連字號），需截取或使用其他格式。
建議格式：`L` + 時間戳 `yyMMddHHmmss` (12碼) + UUID 前 6 碼大寫 = 19 碼。

### 6.3 建立物流訂單參數

```java
public LogisticsResult createShipment(ShopOrder order) {
    String merchantId = getConfig("shop.ecpay.logistics.merchant_id");
    String hashKey    = getConfig("shop.ecpay.logistics.hash_key");
    String hashIv     = getConfig("shop.ecpay.logistics.hash_iv");
    String mode       = getConfig("shop.ecpay.logistics.mode");
    String createUrl  = "prod".equals(mode) ? PROD_CREATE_URL : TEST_CREATE_URL;

    boolean isCvs = ShippingMethod.fromCode(order.getShippingMethod()).isCvs();

    TreeMap<String, String> params = new TreeMap<>();
    params.put("MerchantID",           merchantId);
    params.put("MerchantTradeNo",      generateLogisticTradeNo(order.getOrderNo()));
    params.put("MerchantTradeDate",    formatDate(new Date()));
    params.put("LogisticsType",        isCvs ? "CVS" : "HOME");
    params.put("LogisticsSubType",     resolveLogisticsSubType(order));
    params.put("GoodsAmount",          String.valueOf(order.getTotalAmount().intValue()));
    params.put("GoodsName",            truncate(buildGoodsName(order), 50));
    params.put("SenderName",           getConfig("shop.logistics.sender_name"));
    params.put("SenderPhone",          getConfig("shop.logistics.sender_phone"));
    params.put("SenderZipCode",        getConfig("shop.logistics.sender_zip"));
    params.put("SenderAddress",        getConfig("shop.logistics.sender_address"));
    params.put("ReceiverName",         order.getReceiverName());
    params.put("ReceiverCellPhone",    order.getReceiverMobile());

    if (isCvs) {
        params.put("ReceiverStoreID",  order.getCvsStoreId());
        params.put("IsCollection",     "N");
    } else {
        params.put("ReceiverZipCode",  order.getReceiverZip());
        params.put("ReceiverAddress",  order.getReceiverAddress());
    }

    String checkMac = generateCheckMacValue(params, hashKey, hashIv);
    params.put("CheckMacValue", checkMac);

    // 呼叫綠界 API
    String response = httpPost(createUrl, params);
    return parseLogisticsResponse(response);
}

/**
 * 解析建立物流回應
 * 成功回應格式：1|OK|AllPayLogisticsID|CVSPaymentNo|CVSValidationNo
 */
private LogisticsResult parseLogisticsResponse(String response) {
    if (response == null || !response.startsWith("1|")) {
        log.error("建立物流訂單失敗，回應：{}", response);
        return LogisticsResult.fail(response);
    }
    String[] parts = response.split("\\|");
    return LogisticsResult.builder()
        .success(true)
        .logisticsId(parts.length > 2 ? parts[2] : null)
        .build();
}
```

### 6.4 電子地圖回調處理

`ServerReplyURL` 格式：`{callbackBaseUrl}/shop/logistics/cvs/store/callback`

```java
// ShopLogisticsController 中

@Anonymous
@PostMapping("/cvs/store/callback")
@ResponseBody
public String cvsStoreCallback(HttpServletRequest request) {
    Map<String, String> params = extractParams(request);
    log.info("收到電子地圖回調：{}", params);

    try {
        // 1. 驗證 MerchantID
        String merchantId = params.get("MerchantID");
        String configMerchantId = configService.selectConfigByKey("shop.ecpay.logistics.merchant_id");
        if (!configMerchantId.equals(merchantId)) {
            log.warn("電子地圖回調 MerchantID 不匹配：{}", merchantId);
            return "0|Error";
        }

        // 2. 儲存門市資訊
        String storeKey = params.get("MerchantTradeNo");
        logisticsService.saveCvsStore(storeKey, params);

        return "1|OK";
    } catch (Exception e) {
        log.error("電子地圖回調處理異常", e);
        return "0|Error";
    }
}
```

### 6.5 付款成功後觸發物流訂單

在現有 `OrderEventListener` 新增處理邏輯：

```java
// OrderEventListener.java（修改）

@EventListener
public void handlePaymentSuccess(PaymentSuccessEvent event) {
    ShopOrder order = event.getOrder();
    log.info("付款成功事件，訂單：{}，物流方式：{}", order.getOrderNo(), order.getShippingMethod());

    // 若有設定物流方式，自動建立物流訂單
    if (order.getShippingMethod() != null) {
        try {
            LogisticsResult result = logisticsGateway.createShipment(order);
            if (result.isSuccess()) {
                orderMapper.updateShippingNo(order.getOrderId(), result.getLogisticsId());
                log.info("物流訂單建立成功：orderNo={}，logisticsId={}",
                        order.getOrderNo(), result.getLogisticsId());
            } else {
                log.error("物流訂單建立失敗：orderNo={}，原因：{}",
                        order.getOrderNo(), result.getErrorMessage());
                // 不拋出例外，付款成功仍需完成；物流可由後台手動補建
            }
        } catch (Exception e) {
            log.error("物流訂單建立異常：orderNo={}", order.getOrderNo(), e);
        }
    }
}
```

### 6.6 LogisticsResult DTO

**位置**：`cheng-shop/src/main/java/com/cheng/shop/logistics/LogisticsResult.java`

```java
package com.cheng.shop.logistics;

@Data
@Builder
public class LogisticsResult {
    private boolean success;
    private String logisticsId;     // AllPayLogisticsID
    private String errorMessage;
    private String rawResponse;

    public static LogisticsResult fail(String response) {
        return LogisticsResult.builder()
            .success(false)
            .rawResponse(response)
            .build();
    }
}
```

### 6.7 LogisticsSubType 對應表

| ShippingMethod | LogisticsType | LogisticsSubType |
|---------------|---------------|-----------------|
| CVS_711       | CVS           | UNIMART          |
| CVS_FAMILY    | CVS           | FAMI             |
| CVS_HILIFE    | CVS           | HILIFE           |
| HOME_DELIVERY | HOME          | TCAT             |

### 6.8 EcpayCheckMacValue 工具類提取

由於金流與物流的 CheckMacValue 演算法完全相同，建議提取為共用工具類：

**位置**：`cheng-shop/src/main/java/com/cheng/shop/payment/EcpaySignatureUtils.java`

```java
package com.cheng.shop.payment;

/**
 * 綠界簽章工具類（金流與物流共用）
 */
public final class EcpaySignatureUtils {

    private EcpaySignatureUtils() {}

    /**
     * 產生 CheckMacValue（SHA256）
     */
    public static String generateCheckMacValue(Map<String, String> params,
                                                String hashKey, String hashIv) {
        TreeMap<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(params);

        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey).append("&");
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("HashIV=").append(hashIv);

        String encoded = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8).toLowerCase();
        encoded = encoded.replace("%2d", "-").replace("%5f", "_").replace("%2e", ".")
                         .replace("%21", "!").replace("%2a", "*").replace("%28", "(")
                         .replace("%29", ")").replace("%20", "+");

        return sha256(encoded).toUpperCase();
    }
}
```

---

## 7. 防呆與異常處理

### 7.1 輸入驗證規則

**後端驗證（Service 層）**：

```java
// ShopCheckoutServiceImpl.submit 新增驗證

private void validateLogistics(CheckoutSubmitRequest request) {
    ShippingMethod method = ShippingMethod.fromCode(request.getShippingMethod());

    // 1. 超商取貨必須提供門市 key
    if (method.isCvs()) {
        if (request.getCvsStoreKey() == null || request.getCvsStoreKey().isBlank()) {
            throw new BusinessException(LOGISTICS_002, "超商門市尚未選取");
        }

        // 2. 查詢暫存門市
        ShopCvsStoreTemp store = cvsStoreTempMapper.selectByKey(request.getCvsStoreKey());
        if (store == null) {
            throw new BusinessException(LOGISTICS_002, "超商門市尚未選取");
        }
        if (store.getExpireTime().before(new Date())) {
            throw new BusinessException(LOGISTICS_003, "超商門市選取已過期，請重新選取");
        }
    }

    // 3. 宅配必須提供地址
    if (method == ShippingMethod.HOME_DELIVERY && request.getAddressId() == null) {
        throw new BusinessException(LOGISTICS_001, "宅配需提供收貨地址");
    }

    // 4. COD 不支援超商取貨
    if ("COD".equals(request.getPaymentMethod()) && method.isCvs()) {
        throw new BusinessException(LOGISTICS_004, "貨到付款僅支援宅配");
    }
}
```

### 7.2 API 錯誤處理

**綠界 API 呼叫失敗處理**：

```java
// EcpayLogisticsGateway 內部

private String httpPost(String url, Map<String, String> params) {
    int maxRetry = 3;
    int retryDelay = 1000; // ms

    for (int attempt = 1; attempt <= maxRetry; attempt++) {
        try {
            // OkHttp 呼叫
            return doPost(url, params);
        } catch (Exception e) {
            log.warn("呼叫綠界物流 API 失敗，第 {} 次，URL={}", attempt, url, e);
            if (attempt < maxRetry) {
                Thread.sleep(retryDelay * attempt); // 指數退避
            }
        }
    }
    throw new ServiceException("物流 API 呼叫失敗，已重試 " + maxRetry + " 次");
}
```

### 7.3 回調失敗重試機制

**電子地圖回調（選門市）**：
- 綠界不會重試電子地圖回調
- 若回調失敗（後端異常），前端輪詢將在 10 分鐘後逾時
- 使用者需重新選取門市
- 日誌記錄所有回調請求，方便排查

**物流訂單建立失敗**：
- 付款成功後物流建立失敗不影響訂單狀態
- 後台新增「手動補建物流訂單」功能（`POST /shop/logistics/shipment/create`）
- 建議：設定定時任務，每小時掃描已付款但無物流單號的訂單並重試

```java
// 建議：Quartz 排程（可選）
// @DisallowConcurrentExecution
// public class LogisticsRetryJob implements Job {
//     // 掃描狀態為 PAID 且 shipping_no 為 null 的訂單，重試建立物流
// }
```

### 7.4 門市暫存清理

```java
// 建議：Quartz 排程，每天清除過期門市暫存
// DELETE FROM shop_cvs_store_temp WHERE expire_time < NOW()
```

### 7.5 前端防呆

| 情境 | 防呆措施 |
|------|---------|
| 使用者未選門市就提交 | 提交前驗證，顯示「請選擇取貨門市」 |
| 電子地圖視窗被關閉 | 輪詢仍繼續，若 30 分鐘未完成顯示提示 |
| 頁面刷新後門市資訊遺失 | storeKey 暫存至 sessionStorage，重新查詢 |
| 超商門市與付款方式不符 | 即時警告，禁用提交按鈕 |

```javascript
// sessionStorage 暫存 storeKey
watch(storeKey, (val) => {
  if (val) {
    sessionStorage.setItem(`cvs_store_key_${shippingMethod.value}`, val)
  }
})

// 頁面載入時恢復
onMounted(async () => {
  // ...
  const savedKey = sessionStorage.getItem(`cvs_store_key_${shippingMethod.value}`)
  if (savedKey) {
    const res = await getCvsStore(savedKey)
    if (res.code === 200 && res.data) {
      storeKey.value = savedKey
      selectedStore.value = res.data
    }
  }
})
```

---

## 8. 測試計畫

### 8.1 單元測試案例

**位置**：`cheng-shop/src/test/java/com/cheng/shop/logistics/`

| 測試類別 | 測試方法 | 驗證重點 |
|---------|---------|---------|
| `EcpayLogisticsGatewayTest` | `generateMapFormHtml_shouldContainRequiredParams` | 表單含必填欄位 |
| `EcpayLogisticsGatewayTest` | `generateMapFormHtml_shouldHaveValidCheckMacValue` | CheckMacValue 正確 |
| `EcpayLogisticsGatewayTest` | `createShipment_cvs_shouldBuildCorrectParams` | 超商取貨參數正確 |
| `EcpayLogisticsGatewayTest` | `createShipment_home_shouldBuildCorrectParams` | 宅配參數正確 |
| `ShopCheckoutServiceImplTest` | `submit_cvs_withoutStore_shouldThrowException` | 未選門市拋出例外 |
| `ShopCheckoutServiceImplTest` | `submit_cvs_withExpiredStore_shouldThrowException` | 過期門市拋出例外 |
| `ShopCheckoutServiceImplTest` | `submit_cod_withCvs_shouldThrowException` | COD + 超商拋出例外 |
| `EcpaySignatureUtilsTest` | `generateCheckMacValue_shouldMatchReference` | 與既有金流簽章一致 |

**範例測試**：
```java
@ExtendWith(MockitoExtension.class)
class EcpayLogisticsGatewayTest {

    @Mock
    private ISysConfigService configService;

    @InjectMocks
    private EcpayLogisticsGateway gateway;

    @BeforeEach
    void setUp() {
        when(configService.selectConfigByKey("shop.ecpay.logistics.merchant_id")).thenReturn("2000132");
        when(configService.selectConfigByKey("shop.ecpay.logistics.hash_key")).thenReturn("5294y06JbISpM5x9");
        when(configService.selectConfigByKey("shop.ecpay.logistics.hash_iv")).thenReturn("v77hoKGq4kWxNNIS");
        when(configService.selectConfigByKey("shop.ecpay.logistics.mode")).thenReturn("test");
    }

    @Test
    void generateMapFormHtml_shouldContainRequiredParams() {
        String html = gateway.generateMapFormHtml(
            "L2602081234ABCDEF",
            "UNIMART",
            "https://example.com/shop/logistics/cvs/store/callback"
        );

        assertThat(html).contains("MerchantID");
        assertThat(html).contains("LogisticsType");
        assertThat(html).contains("UNIMART");
        assertThat(html).contains("CheckMacValue");
    }
}
```

### 8.2 整合測試案例

**位置**：`cheng-shop/src/test/java/com/cheng/shop/logistics/EcpayLogisticsIT.java`

| 測試方法 | 說明 |
|---------|------|
| `createShipment_711_IT` | 呼叫測試環境建立 7-11 物流訂單 |
| `createShipment_family_IT` | 呼叫測試環境建立全家物流訂單 |
| `createShipment_home_IT` | 呼叫測試環境建立宅配物流訂單 |

**整合測試需要**：
- 設定測試用的帳號（B2C：MerchantID=2000132）
- 需要可公開存取的 URL 才能測試電子地圖回調（建議用 ngrok）
- 使用測試環境 API

### 8.3 測試環境配置

**application-test.yml**（測試環境設定）：
```yaml
# 綠界物流測試設定（透過 sys_config 管理，此處僅供參考）
# shop.ecpay.logistics.merchant_id: 2000132
# shop.ecpay.logistics.hash_key: 5294y06JbISpM5x9
# shop.ecpay.logistics.hash_iv: v77hoKGq4kWxNNIS
# shop.ecpay.logistics.mode: test
```

**手動測試步驟**：
1. 啟動後端（profile=local），確認 ngrok 正常運作
2. 設定 `shop.logistics.callback_base_url` 為 ngrok URL
3. 前端進入結帳頁，選擇超商取貨
4. 點擊「選擇取貨門市」，確認電子地圖正常開啟
5. 在電子地圖選取門市，確認前端顯示門市資訊
6. 提交訂單，確認訂單含門市資訊
7. 付款完成，確認物流訂單自動建立（`shipping_no` 有值）

---

## 9. 實作順序

### 第一階段：基礎設施（預估 1 天）

**目標**：建立資料庫與設定，無需任何現有功能

1. **建立 Flyway 遷移檔** `V41__shop_logistics_cvs_store_temp.sql`
   - 建立 `shop_cvs_store_temp` 表
   - 新增 `shop_order` 欄位（cvs_store_id、cvs_store_name、cvs_store_address、logistics_sub_type）
   - 插入 sys_config 設定

2. **提取 `EcpaySignatureUtils`**
   - 從 `EcpayPaymentGateway` 提取 CheckMacValue 計算邏輯
   - 撰寫單元測試驗證與現有邏輯一致

**可交付成果**：資料庫遷移完成，工具類可用

---

### 第二階段：物流核心（預估 2 天）

**目標**：後端物流核心邏輯完整可用

3. **建立 `EcpayLogisticsGateway`**
   - `generateMapFormHtml()`
   - `createShipment()`
   - `generateCheckMacValue()`（使用 `EcpaySignatureUtils`）

4. **建立 `LogisticsResult` DTO**

5. **建立 `ShopCvsStoreTemp` 實體與 Mapper**
   - `ShopCvsStoreTemp.java`
   - `ShopCvsStoreTempMapper.java`
   - `ShopCvsStoreTempMapper.xml`

6. **建立 `IShopLogisticsService` 與 `ShopLogisticsServiceImpl`**
   - `generateMapUrl(shippingMethod, storeKey)`
   - `saveCvsStore(storeKey, params)`
   - `getCvsStore(storeKey, memberId)`
   - `getAvailableMethods(productAmount)`

7. **撰寫 `EcpayLogisticsGatewayTest`**（覆蓋率 80%+）

**可交付成果**：物流服務可獨立測試

---

### 第三階段：API 層（預估 1 天）

**目標**：後端 API 完整可用（含 Postman 測試）

8. **建立 `ShopLogisticsController`**
   - `GET /shop/logistics/methods`
   - `POST /shop/logistics/map/url`
   - `POST /shop/logistics/cvs/store/callback`（@Anonymous）
   - `GET /shop/logistics/cvs/store/{storeKey}`
   - `POST /shop/logistics/shipment/create`（後台）

9. **更新 `ShopSecurityConfig`** 新增物流 API 白名單（callback 端點）

10. **修改 `CheckoutSubmitRequest`** 新增 `shippingMethod`、`cvsStoreKey`

11. **修改 `ShopCheckoutServiceImpl`**
    - 新增 `validateLogistics()` 驗證
    - 超商取貨時從暫存表讀取門市資訊並寫入訂單

**可交付成果**：後端 API 可用 Postman 完整測試

---

### 第四階段：事件整合（預估 0.5 天）

**目標**：付款成功後自動建立物流訂單

12. **修改 `OrderEventListener`**
    - 在 `handlePaymentSuccess` 中呼叫 `EcpayLogisticsGateway.createShipment()`
    - 成功後更新 `shipping_no`

13. **修改 `ShopOrderMapper`**
    - 新增 `updateShippingNo(orderId, shippingNo)` 方法

**可交付成果**：端到端流程（付款→物流）可測試

---

### 第五階段：前端實作（預估 2 天）

**目標**：前端結帳頁完整可用

14. **新增 `cheng-ui/src/api/shop/logistics.js`**

15. **修改 `checkout/index.vue`**
    - 新增物流方式選擇 UI
    - 新增超商門市選取區塊
    - 實作電子地圖視窗開啟
    - 實作輪詢查詢門市結果
    - 修改提交邏輯（含物流驗證）
    - sessionStorage 暫存恢復

16. **調整 `addressForm`**
    - 宅配時顯示地址欄位
    - 超商取貨時隱藏地址欄位

**可交付成果**：前端端到端流程完整可測試

---

### 第六階段：完整測試與修復（預估 1 天）

**目標**：所有場景正常運作

17. 手動端到端測試（見 8.3 手動測試步驟）
18. 修復發現的問題
19. 補充整合測試
20. 更新後台訂單管理頁（顯示超商門市資訊）

**可交付成果**：功能完整上線

---

### 時程總覽

| 階段 | 工作 | 預估天數 |
|------|------|---------|
| 第一階段 | 基礎設施 | 1 天 |
| 第二階段 | 物流核心 | 2 天 |
| 第三階段 | API 層 | 1 天 |
| 第四階段 | 事件整合 | 0.5 天 |
| 第五階段 | 前端實作 | 2 天 |
| 第六階段 | 測試修復 | 1 天 |
| **合計** | | **7.5 天** |

---

## 附錄

### A. 綠界測試帳號

| 用途 | MerchantID | HashKey | HashIV |
|------|-----------|---------|--------|
| B2C 大宗寄倉（超商/宅配） | 2000132 | 5294y06JbISpM5x9 | v77hoKGq4kWxNNIS |
| C2C 店到店（不在此次範疇） | 2000933 | XBERn1YOvpM9nfZc | h1ONHk4P4yqbl5LK |

### B. 相關檔案清單

**新增檔案**：
- `cheng-admin/src/main/resources/db/migration/V41__shop_logistics_cvs_store_temp.sql`
- `cheng-shop/src/main/java/com/cheng/shop/logistics/EcpayLogisticsGateway.java`
- `cheng-shop/src/main/java/com/cheng/shop/logistics/LogisticsResult.java`
- `cheng-shop/src/main/java/com/cheng/shop/logistics/ShopCvsStoreTemp.java`（domain）
- `cheng-shop/src/main/java/com/cheng/shop/logistics/ShopCvsStoreTempMapper.java`
- `cheng-shop/src/main/resources/mapper/shop/ShopCvsStoreTempMapper.xml`
- `cheng-shop/src/main/java/com/cheng/shop/logistics/IShopLogisticsService.java`
- `cheng-shop/src/main/java/com/cheng/shop/logistics/impl/ShopLogisticsServiceImpl.java`
- `cheng-shop/src/main/java/com/cheng/shop/controller/ShopLogisticsController.java`
- `cheng-shop/src/main/java/com/cheng/shop/payment/EcpaySignatureUtils.java`
- `cheng-ui/src/api/shop/logistics.js`
- `cheng-shop/src/test/java/com/cheng/shop/logistics/EcpayLogisticsGatewayTest.java`

**修改檔案**：
- `cheng-shop/src/main/java/com/cheng/shop/domain/ShopOrder.java`（新增超商門市欄位）
- `cheng-shop/src/main/java/com/cheng/shop/domain/dto/CheckoutSubmitRequest.java`（新增 shippingMethod、cvsStoreKey）
- `cheng-shop/src/main/java/com/cheng/shop/enums/ShippingMethod.java`（新增 getEcpayLogisticsSubType）
- `cheng-shop/src/main/java/com/cheng/shop/service/impl/ShopCheckoutServiceImpl.java`（新增物流驗證）
- `cheng-shop/src/main/java/com/cheng/shop/listener/OrderEventListener.java`（新增物流建立）
- `cheng-shop/src/main/java/com/cheng/shop/payment/EcpayPaymentGateway.java`（使用 EcpaySignatureUtils）
- `cheng-shop/src/main/java/com/cheng/shop/config/ShopSecurityConfig.java`（新增白名單）
- `cheng-ui/src/views/shop-front/checkout/index.vue`（物流選擇 UI）

### C. 潛在問題與注意事項

1. **MerchantTradeNo 長度限制**：電子地圖 API 的 MerchantTradeNo 限制 20 碼，不能直接使用 UUID（36 碼），需要自定義格式。

2. **電子地圖 ServerReplyURL 必須公開可存取**：本地開發需搭配 ngrok，設定 `shop.logistics.callback_base_url`。

3. **超商付款 + 超商取貨同一超商限制**：綠界規定，若使用超商代碼付款（CVS），取貨門市必須是同一家超商。此驗證邏輯需在提交訂單時加入。

4. **GoodsAmount 精度**：綠界 GoodsAmount 為整數，需使用 `intValue()`，但四捨五入可能造成金額差異，建議商品金額設計為整數。

5. **宅配尺寸規格**：建立宅配物流訂單可能需要提供包裹尺寸（長、寬、高、重量），視商品設定而定。若商品系統未管理尺寸，可使用固定預設值。

6. **跨域問題**：電子地圖在新視窗開啟，回調由後端接收後前端輪詢，不涉及跨域。但若改用 iframe 嵌入，需注意 CSP 設定。
