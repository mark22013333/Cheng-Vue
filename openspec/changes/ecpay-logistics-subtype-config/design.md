## Context

綠界（ECPay）物流 API 的超商取貨分為兩種合約型態：

- **B2C 大宗寄貨**：適合商品寄放綠界物流中心、出貨量大的賣家；對應子類型代碼 `UNIMART` / `FAMI` / `HILIFE` / `OKMART`，並支援宅配（`TCAT`、`POST`）。
- **C2C 門市寄/取件**：賣家自行至超商寄件、出貨量小；對應子類型代碼 `UNIMARTC2C` / `FAMIC2C` / `HILIFEC2C` / `OKMARTC2C`，**不提供宅配**。

目前專案程式碼在 `ShippingMethod.getEcpayLogisticsSubType()` 寫死為 B2C 代碼，而 UAT 使用者實際開通的是 C2C，導致電子地圖 API 回「找不到加密金鑰」。

使用者本人明確表達未來出貨量長大可能改簽 B2C 合約，屆時不希望再改程式部署。因此此設計以**系統參數**為切換點。

### Current State

```
ShippingMethod.getEcpayLogisticsSubType()           // 硬寫死 B2C 代碼
  └── EcpayLogisticsGateway.generateMapFormHtml()   // 直接使用
  └── EcpayLogisticsGateway.createShipment()        // 直接使用
  └── ShopLogisticsServiceImpl.getAvailableMethods()// 不過濾，全數回傳
```

### Constraints

- 不可改 `/shop/logistics/methods` 的 API 合約（前端已依賴）。
- 不可讓前端讀 sub_type_mode（避免前後端雙寫邏輯）。
- sys_config 改值後的生效行為受既有 sys_config cache 機制管轄，本 change 不介入。
- 歷史訂單的 `logistics_sub_type` 欄位值不回溯更新。

## Goals / Non-Goals

**Goals:**

- 以 sys_config 參數 `shop.ecpay.logistics.sub_type_mode`（B2C / C2C）切換綠界子類型代碼對映。
- 預設 C2C，對齊目前綠界實際合約。
- C2C 模式下自動過濾掉不支援的宅配（`HOME_DELIVERY`）。
- 程式內部以型別安全的 enum（`LogisticsSubTypeMode`）代表模式，避免 magic string。
- 不支援組合在呼叫綠界前攔截，提供清楚的業務錯誤訊息。
- 單元測試覆蓋兩種模式的所有 ShippingMethod 對映與錯誤情境。

**Non-Goals:**

- 不同時支援 B2C + C2C 混合模式（ECPay 帳號通常二選一）。
- 不實作參數熱更新 listener。
- 不補 B2C 特有的額外欄位（Temperature、Specification 等目前已有，不再擴張）。
- 不提供管理介面 UI（走既有 sys_config 編輯功能）。
- 不改動金流（shop.ecpay.*）相關設定。

## Decisions

### 決策 1：模式參數放在 sys_config 而非 application.yml

**選擇**：sys_config（資料庫）

**理由**：
- 專案既有 pattern：所有商城的 ECPay 設定（merchant_id、hash_key、mode、server_reply_url）都在 sys_config。保持一致，避免設定分散。
- sys_config 可在執行中透過後台管理介面改值，不用重啟服務；改 application.yml 需重新打包部署。
- 不同環境（local / UAT / prod）可能用不同綠界帳號型態，放 sys_config 方便各環境獨立設定。

**被淘汰的替代方案**：
- application.yml + `@Value`：部署成本高、環境切換麻煩。
- 寫死程式碼（B2C 或 C2C 擇一）：就是現在的做法，已證明會踩坑。

### 決策 2：新增型別安全的 `LogisticsSubTypeMode` enum，而非直接用字串

**選擇**：新增 `cheng-shop/.../enums/LogisticsSubTypeMode.java`，值為 `B2C` / `C2C`，附 `fromConfigValue(String)` 容錯解析。

**理由**：
- 避免在多個地方散落 `"C2C".equals(xxx)` 這種 magic string 比對。
- `fromConfigValue()` 集中處理大小寫、null、非法值 fallback 邏輯。
- 全域 CLAUDE.md 規則：「專案禁止魔術數字，必須使用枚舉常數」，此處 magic string 等同 magic number 的概念延伸。

**被淘汰的替代方案**：
- 直接在 `ShopConfigService.getEcpayLogisticsSubTypeMode()` 回字串：呼叫端要自己比對，容易出錯。
- boolean `isB2c()` / `isC2c()`：未來若有第三種模式（如 B2B）需大改。

### 決策 3：`ShippingMethod.getEcpayLogisticsSubType()` 接受 mode 參數

**選擇**：改成 `getEcpayLogisticsSubType(LogisticsSubTypeMode mode)`；保留無參版本 `@Deprecated` 並預設以 C2C 呼叫。

**理由**：
- `ShippingMethod` 是 pure enum，不宜注入 Spring bean；由呼叫端讀 mode 再傳入，職責分離。
- 保留無參版本避免未來若有遺漏的呼叫點造成 build error，但透過 `@Deprecated` 警告和預設值（C2C）讓行為向前相容。
- 呼叫端只有 `EcpayLogisticsGateway` 與 `ShopLogisticsServiceImpl` 兩處，改造成本低。

**被淘汰的替代方案**：
- 讓 `ShippingMethod` 成為 Spring bean：違反 enum 的語意，也不符合既有 codebase 風格。
- 在 `EcpayLogisticsGateway` 內做 mode → code 的對映表：會把業務邏輯從 enum 拉出來，破壞 cohesion。

### 決策 4：C2C 模式的 `HOME_DELIVERY` 回傳 null，呼叫端判 null 拋例外

**選擇**：`getEcpayLogisticsSubType(C2C)` 對 `HOME_DELIVERY` 回傳 `null`；`generateMapFormHtml` / `createShipment` 檢查 null 後拋 `ServiceException`。

**理由**：
- `null` 語意清晰：「此組合不存在」。比拋例外直接在 enum 內更靈活，呼叫端可選擇如何處理（`getAvailableMethods()` 用 null 判斷要不要過濾掉；gateway 用 null 判斷要不要拋錯）。
- 維持 enum 不耦合到 `ServiceException`（enum 層不該知道業務例外）。

**被淘汰的替代方案**：
- 在 enum 內拋 `UnsupportedOperationException`：破壞 `getAvailableMethods()` 的迭代 — 每跑一個不支援的 method 就炸例外，邏輯複雜。
- 回傳空字串：讓綠界那邊失敗，使用者看到誤導性錯誤，跟目前 bug 一樣。

### 決策 5：`getAvailableMethods()` 過濾不支援方式，前端不感知 mode

**選擇**：後端 `getAvailableMethods()` 依 mode 過濾，前端 `checkout/index.vue` 用原 API 合約渲染。

**理由**：
- 前端不需要理解 B2C / C2C 概念，關注點只是「這個賣家目前能用什麼物流方式」。
- 避免前後端雙寫：若前端也要判斷，B2C / C2C 分支邏輯會同時出現在兩處，容易失同步。
- API 合約不變，向後相容：前端不用改 code。

**被淘汰的替代方案**：
- API 多回一個 `subTypeMode` 欄位，前端自己過濾：耦合增加、對相容性不利。
- 前端單獨呼叫 `/shop/config` 取 mode：多一次 roundtrip，沒必要。

### 決策 6：Flyway migration 用 `INSERT ... ON DUPLICATE KEY UPDATE` 寫入預設值

**選擇**：
```sql
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('綠界物流型態', 'shop.ecpay.logistics.sub_type_mode', 'C2C', 'Y', 'admin', NOW(),
        'B2C=大宗寄貨（含宅配） / C2C=門市寄取件（無宅配），預設 C2C')
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    remark       = VALUES(remark);
```

**理由**：
- 本專案 Flyway migration 已有 `INSERT INTO sys_config ... VALUES ...` pattern（如 V41）。
- `ON DUPLICATE KEY UPDATE` 確保若 UAT 已有此 key（例如手動先建過）也能覆寫為預設值，方便一致性驗證。
- `config_type='Y'`（系統內建）對齊 V41 其他 ecpay.logistics.* 設定。

**被淘汰的替代方案**：
- `INSERT IGNORE`：若既有值錯誤（如 `"test"`），不會被修正，除錯時容易誤判。

## Risks / Trade-offs

- **[風險] UAT 使用者改了綠界合約但忘了改 sys_config** → 系統仍送舊模式代碼 → 綠界回「找不到加密金鑰」，症狀跟目前一樣。
  - **緩解**：migration 註解寫清楚，上線時一併發布 release note 提醒；未來可補 health check 比對 sys_config 與綠界實際合約（超出本 change 範圍）。

- **[風險] 呼叫 `ShippingMethod.getEcpayLogisticsSubType()` 無參版本的遺留程式碼使用預設 C2C，但系統設定為 B2C** → 行為不一致。
  - **緩解**：編譯時 `@Deprecated` 警告；本 change 將所有已知呼叫點（grep 搜出的 `EcpayLogisticsGateway` 兩處、`ShippingMethodVO` 可能的用途）一併改為帶參版本，任務列表中明確列出。

- **[風險] sys_config cache 未刷新，改值後舊行為持續**
  - **緩解**：沿用既有 `ISysConfigService.selectConfigByKey()` 的 cache 機制，不另外做；release note 提醒「改值後若必要請呼叫 refresh cache endpoint 或重啟服務」。

- **[Trade-off] 保留無參 `getEcpayLogisticsSubType()`** → 程式碼面多一個要維護的方法。
  - **理由**：避免呼叫點外洩時的破壞性改動；加註 `@Deprecated` 讓 IDE 顯示警告。

- **[Trade-off] 預設 C2C 違反「測試/開發環境用 B2C 綠界公用測試帳號」的既有 V41 migration 預設值（`merchant_id=2000132` 是 B2C 測試帳號）**
  - **影響**：本地開發啟動、mode 默認 test 時，`merchant_id=2000132` + `sub_type_mode=C2C` 會送 `UNIMARTC2C` 到測試端 → 測試端對於該 MerchantID 也許不支援 C2C。
  - **緩解**：釐清後發現綠界測試環境 `2000132` 帳號同時支援 B2C 與 C2C，送 `UNIMARTC2C` 不會炸；若測試發現不行，補 migration 或文件指示本地開發環境改 sub_type_mode 為 B2C。本任務包含驗證步驟。

## Migration Plan

### 部署順序

1. 開新分支 `fix/ecpay-logistics-c2c-subtype`（從 main）。
2. 實作程式碼變更 + 單元測試。
3. 寫 Flyway migration `V{next}__shop_logistics_sub_type_mode.sql`。
4. 本地執行 `mvn -T 1C test`，確認測試全綠。
5. 推 PR、merge 到 main。
6. CI build WAR、部署到 UAT。
7. **驗證步驟**：UAT 重跑結帳流程 → 選超商取貨 → 確認能正常開啟門市地圖。
8. 觀察 catalina.out 有無新錯誤。

### 回滾

- **情境 A（功能壞 UAT 上不了）**：revert PR commit → 重新部署前一版 WAR。sys_config 的 sub_type_mode 欄位可保留（未來用得上）。
- **情境 B（C2C 代碼送錯、變回 B2C 反而好）**：線上 SQL `UPDATE sys_config SET config_value='B2C' WHERE config_key='shop.ecpay.logistics.sub_type_mode'`，不用回滾程式碼。
- **情境 C（綠界合約從 C2C 升為 B2C）**：同情境 B，改 sys_config 即可。

### 驗證 Checklist（部署後）

- [ ] 結帳頁載入，物流方式選項只有 3 個超商（C2C 模式過濾掉宅配）。
- [ ] 點「選擇門市」→ 彈窗能顯示綠界電子地圖（不再出現「找不到加密金鑰」）。
- [ ] 完成門市選取 → 回到結帳頁顯示門市名稱。
- [ ] 送出訂單 → 跳綠界金流付款頁。
- [ ] 模擬測試付款完成 → OrderEventListener 應能呼叫 `createShipment` 送 C2C 代碼成功。

## Open Questions

- **綠界測試環境（`MerchantID=2000132`）送 C2C 代碼是否能成功？**
  - 若不行，本地開發切換 sub_type_mode 為 B2C；若可行，維持預設 C2C。本任務的單元測試涵蓋對映邏輯，真實 API 互動以 UAT 驗證為準。

- **前端 `checkout/index.vue` 的 `requireCvsStore` 欄位是否因移除 HOME_DELIVERY 而需要調整？**
  - 初步審視不需要：`requireCvsStore` 依 `ShippingMethodVO.requireCvsStore` 欄位，HOME_DELIVERY 本來就是 false；C2C 模式直接不回傳 HOME_DELIVERY，前端連看都看不到。但實作時需確認 `ShippingMethodVO` 的 build 邏輯沒有硬寫 4 個方式。
