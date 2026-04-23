## Why

UAT 結帳時點選「選擇門市」，綠界彈窗回「找不到加密金鑰，請確認是否有申請開通此物流方式!」。深入追查後確認：

1. sys_config 內 `shop.ecpay.logistics.*`（MerchantID、HashKey/HashIV、mode=prod）全部正確，後端 log 也顯示成功產生電子地圖表單。
2. 錯誤來源是綠界端：我們送 `LogisticsSubType=UNIMART`（B2C 代碼），但該 `MerchantID=3300766` 只開通 **C2C 門市寄/取件**（出貨量小、自行至超商寄件）。綠界找不到此帳號在 B2C 的設定，於是回這個誤導性的錯誤訊息。
3. 目前 `ShippingMethod.getEcpayLogisticsSubType()`（`cheng-shop/.../enums/ShippingMethod.java:46`）把 CVS_711/FAMILY/HILIFE 寫死成 B2C 代碼（`UNIMART` / `FAMI` / `HILIFE`），完全不支援 C2C 代碼（`UNIMARTC2C` / `FAMIC2C` / `HILIFEC2C`）。

未來出貨量成長後使用者可能會改簽 B2C 合約，此時需要能切回 B2C 代碼而不用改程式碼重新部署。**需以系統參數控制 B2C / C2C 模式**，避免下次又卡在同一個坑。

## What Changes

### 核心規則

1. **新增 sys_config 參數** `shop.ecpay.logistics.sub_type_mode`，值為 `B2C` 或 `C2C`，**預設 `C2C`**（對齊目前綠界實際開通的型態）。
2. **`ShippingMethod.getEcpayLogisticsSubType()` 簽名維持不變，但實作改為依 mode 回傳對應代碼**：
   - C2C mode：CVS_711 → `UNIMARTC2C`、CVS_FAMILY → `FAMIC2C`、CVS_HILIFE → `HILIFEC2C`、HOME_DELIVERY → `null`（C2C 沒有宅配）
   - B2C mode：CVS_711 → `UNIMART`、CVS_FAMILY → `FAMI`、CVS_HILIFE → `HILIFE`、HOME_DELIVERY → `TCAT`
3. **宅配在 C2C 模式下被判定為「不支援」**：`ShopLogisticsService.getAvailableMethods()` 在 C2C 模式時自動過濾掉 `HOME_DELIVERY`，無論 `shop.logistics.methods` 是否列出。
4. **錯誤處理**：若後台勉強送來 C2C + HOME_DELIVERY 組合，`EcpayLogisticsGateway.generateMapFormHtml()` / `createShipment()` 先於呼叫綠界前丟 `ServiceException`「目前物流型態（C2C）不支援宅配」。
5. **保留 `isCvs()` / `isHomeDelivery()` 等判斷邏輯**（與 mode 無關，仍由 ShippingMethod 自身判斷）。

### 後端

1. **`ShopConfigKey`** 新增列舉 `ECPAY_LOGISTICS_SUB_TYPE_MODE("shop.ecpay.logistics.sub_type_mode", "綠界物流型態（B2C/C2C）", "C2C")`。
2. **`ShopConfigService`** 新增 `getEcpayLogisticsSubTypeMode()`（回傳正規化後的 "B2C" / "C2C"，預設 C2C）與 `isEcpayLogisticsC2cMode()` 便捷方法。
3. **新增 enum `LogisticsSubTypeMode`**（`cheng-shop/.../enums/LogisticsSubTypeMode.java`），值為 `B2C` / `C2C`，附 `fromConfigValue(String)` 容錯解析（接受大小寫、未知值走 C2C）。
4. **`ShippingMethod.getEcpayLogisticsSubType(LogisticsSubTypeMode mode)`**：改成接受 mode 參數的版本；保留無參版本但標註 `@Deprecated`（用以避免外部呼叫失效），預設以 C2C 呼叫。
5. **`EcpayLogisticsGateway`** 在 `generateMapFormHtml()` 與 `createShipment()` 內讀取 mode → 呼叫帶參版本的 `getEcpayLogisticsSubType(mode)`；取得 `null` 時拋出 `ServiceException` 並帶上描述性訊息。
6. **`ShopLogisticsServiceImpl.getAvailableMethods()`** 依 mode 過濾可用物流方式：C2C 模式下排除 `HOME_DELIVERY`，回傳給前端的清單不含宅配選項。
7. **Flyway migration**（下一個版本號）寫入 `shop.ecpay.logistics.sub_type_mode = C2C` 到 `sys_config`，並附註釋。
8. **單元測試**：
   - `ShippingMethodTest` 覆蓋 B2C / C2C 兩個模式下每個 ShippingMethod 的對應代碼。
   - `EcpayLogisticsGatewayTest`（若尚無則新增）mock `ShopConfigService`，驗證 C2C + HOME_DELIVERY 會拋例外；C2C + CVS_711 會送出 `UNIMARTC2C`。
   - `LogisticsSubTypeModeTest` 驗證 `fromConfigValue()` 的大小寫相容與 fallback 行為。

### 前端

1. **不改 `/shop/logistics/methods` API 回傳型別**，只是回傳內容在 C2C 模式下自動少了 `HOME_DELIVERY`，前端 `checkout/index.vue` 用原本的清單渲染即可。
2. **不在前端暴露 mode**：前端不需要知道目前是 B2C 或 C2C，只看後端回傳的可用物流方式清單。這避免前後端雙寫 mode 邏輯。

### 管理端（admin）

1. 不新增管理介面 UI，透過「系統管理 → 參數設定」搜 `shop.ecpay.logistics.sub_type_mode` 直接改值即可（既有 sys_config 管理功能就夠）。
2. migration 寫入時 `config_type='Y'`（系統內建、不可刪）、`remark` 說明「B2C=大宗寄貨/代送黑貓 / C2C=門市寄取件/無宅配」。

### 不做的事

- **不改動現有金流模組**（`shop.ecpay.mode`、`shop.ecpay.hash_key` 等），本次只聚焦物流。
- **不實作 Runtime hot-reload**：改 sys_config 後需等 cache 失效（既有 sys_config 機制自帶）；不額外寫 listener 去監聽參數變化。
- **不支援「同時開通 B2C + C2C」的混合模式**：ECPay 帳號一般只會選一種型態。若日後真有混合需求，再開新 change。
- **不補 B2C 特有欄位**（如 B2C Create API 的 `Temperature`、`Specification` 等），目前 `createShipment()` 已為宅配填寫，超商共用不受影響；若日後 B2C 上線發現參數不足，另開 change。
- **不改 V41 migration 既有註解**（「綠界物流 MerchantID (B2C)」等）：歷史註解保留，新 migration 補充更精確的說明即可。

## Capabilities

### New Capabilities

- `ecpay-logistics-subtype-mode`：以系統參數切換綠界物流子類型（B2C vs C2C），統一處理「現用 C2C」與「未來可能切 B2C」的代碼對映、可用物流方式過濾、以及不支援組合的錯誤處理。

### Modified Capabilities

（無既有 spec 變更，本次是純新增 capability）

## Impact

### 影響的程式碼

- `cheng-shop/src/main/java/com/cheng/shop/config/ShopConfigKey.java` — 新增 enum 值
- `cheng-shop/src/main/java/com/cheng/shop/config/ShopConfigService.java` — 新增 getter
- `cheng-shop/src/main/java/com/cheng/shop/enums/ShippingMethod.java` — 方法簽名調整
- `cheng-shop/src/main/java/com/cheng/shop/enums/LogisticsSubTypeMode.java` — 新增
- `cheng-shop/src/main/java/com/cheng/shop/logistics/EcpayLogisticsGateway.java` — 讀 mode + 呼叫帶參版本
- `cheng-shop/src/main/java/com/cheng/shop/logistics/impl/ShopLogisticsServiceImpl.java` — `getAvailableMethods()` 過濾
- `cheng-admin/src/main/resources/db/migration/V{next}__shop_logistics_sub_type_mode.sql` — 新增 sys_config 紀錄

### 影響的 API

- `GET /shop/logistics/methods`：回傳內容在 C2C 模式下不含 `HOME_DELIVERY`（型別不變）。
- `POST /shop/logistics/map/url`：若前端硬塞 `HOME_DELIVERY` + C2C，回 `500` + 業務錯誤訊息（現況下是打到綠界才失敗，改為先於呼叫前攔截）。

### 影響的資料

- `sys_config` 新增一筆 `shop.ecpay.logistics.sub_type_mode = C2C`。
- 既有訂單的 `logistics_sub_type` 欄位不變（歷史訂單可能有 `UNIMART` / `FAMI` / `HILIFE`，那些是建單當下的值，不回溯改寫）。

### 部署

- Flyway migration 自動執行，無須手動步驟。
- 若 UAT 已有既存的 `shop.ecpay.logistics.sub_type_mode` 設定（不應該有），migration 的 `INSERT IGNORE` 或 `ON DUPLICATE KEY UPDATE` 策略需明確（建議 `INSERT ... ON DUPLICATE KEY UPDATE config_value=VALUES(config_value)` 以覆寫，方便從預設值 bootstrap）。

### 風險與回滾

- 風險：migration 啟用後，若使用者綠界帳號實際是 B2C（不符合目前 C2C 預設），下單會被前端過濾掉宅配 / 超商代碼打到綠界會是 C2C 代碼，仍然失敗。**緩解**：部署後第一件事確認綠界帳號型態 → 若為 B2C 則手動 UPDATE sys_config 設為 B2C。
- 回滾：改 sys_config 的值不等於回滾，此 change 的程式碼改動需以反向 PR 回滾（新增一個 migration 把 sub_type_mode 刪掉並不會讓程式行為回到舊版，仍會有 default = C2C 的行為）。若要完全回到舊行為，需 revert 程式碼 commit。
