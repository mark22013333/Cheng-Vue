## 1. 準備分支與研讀現況

- [x] 1.1 從 main 開新分支 `fix/ecpay-logistics-c2c-subtype`（對齊 `main 受保護，必須先開分支再 PR` 的專案規則）
- [x] 1.2 `grep -rn "getEcpayLogisticsSubType\|getEcpayLogisticsType" --include="*.java"` 盤點所有呼叫點，確認範圍只有 `EcpayLogisticsGateway` 與 `ShopLogisticsServiceImpl`
- [x] 1.3 再次確認 `cheng-shop/.../enums/ShippingMethod.java:46-53`、`EcpayLogisticsGateway.java:74`、`EcpayLogisticsGateway.java:117`、`ShopLogisticsServiceImpl.getAvailableMethods()` 的現行邏輯（免得變更時漏接）

## 2. 新增模式 enum

- [x] 2.1 建立 `cheng-shop/src/main/java/com/cheng/shop/enums/LogisticsSubTypeMode.java`，值為 `B2C`、`C2C`
- [x] 2.2 新增 `public static LogisticsSubTypeMode fromConfigValue(String raw)` 靜態方法：
  - `null`、空字串、未知值 → 回傳 `C2C`
  - 去除前後空白、大小寫不敏感比對
- [x] 2.3 新增單元測試 `LogisticsSubTypeModeTest`，覆蓋 spec 的三個 scenario（合法值、非法值 fallback、trim）— 共 16 個測試通過

## 3. 擴充 sys_config 設定

- [x] 3.1 在 `ShopConfigKey.java` 的「ECPay 物流設定」區塊新增 `ECPAY_LOGISTICS_SUB_TYPE_MODE("shop.ecpay.logistics.sub_type_mode", "綠界物流型態（B2C/C2C）", "C2C")`
- [x] 3.2 在 `ShopConfigService.java` 新增兩個便捷方法：
  - `public LogisticsSubTypeMode getEcpayLogisticsSubTypeMode()`：讀 sys_config → 呼叫 `LogisticsSubTypeMode.fromConfigValue(...)` 回傳
  - `public boolean isEcpayLogisticsC2cMode()`：是否為 C2C 模式（便於 if 判斷）
- [x] 3.3 新建 Flyway migration `cheng-admin/src/main/resources/db/migration/V50__shop_logistics_sub_type_mode.sql`（確認 V49 已存在，下一版為 V50）
  - 使用 `INSERT INTO sys_config ... ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), remark = VALUES(remark)` 語法
  - 預設值 `C2C`，remark 寫清楚「B2C=大宗寄貨（含宅配） / C2C=門市寄取件（無宅配），預設 C2C」

## 4. 調整 ShippingMethod

- [x] 4.1 `ShippingMethod.getEcpayLogisticsSubType()` 改為接受 `LogisticsSubTypeMode mode` 參數
- [x] 4.2 保留無參版本 `@Deprecated` 標註，內部呼叫 `getEcpayLogisticsSubType(LogisticsSubTypeMode.DEFAULT)`（即 C2C）
- [x] 4.3 `getEcpayLogisticsType()` 維持不變
- [x] 4.4 新增 `ShippingMethodTest`，覆蓋 12 個測試（每個方式 × 兩個 mode）

## 5. 調整 EcpayLogisticsGateway

- [x] 5.1 `createShipment()` 讀 mode（`generateMapFormHtml()` 的 mode 判斷下放到 service 層，gateway 收到的 String 即已 mapped，維持原簽名減少耦合）
- [x] 5.2 service 層 `ShopLogisticsServiceImpl.generateMapFormHtml()` 先於呼叫 gateway 前做 mode 檢查 → 拋 `ServiceException`
- [x] 5.3 `createShipment()` null 時回 `LogisticsResult.fail(...)`，不進入任何 HTTP 呼叫
- [x] 5.4 `EcpayLogisticsGatewayTest` 以 Mockito mock `ShopConfigService`：
  - C2C + HOME_DELIVERY → fail，訊息含「C2C」、「宅配到府」
  - C2C + CVS_711 無 storeId → fail 但訊息是「門市代號未設定」（代表 mode 檢查已通過）
  - 未設 shipping method → fail with「物流方式未設定」
  - 共 3 個測試通過

## 6. 調整可用物流方式清單

- [x] 6.1 `ShopLogisticsServiceImpl.getAvailableMethods()` 依 mode 過濾，STORE_PICKUP 豁免
- [x] 6.2 `ShopLogisticsServiceImplTest` 新增 6 個測試：
  - C2C 模式過濾 HOME_DELIVERY，保留 3 個超商
  - B2C 模式保留 4 項
  - C2C 模式 + 僅 HOME_DELIVERY → 空清單
  - STORE_PICKUP 兩種模式都不過濾
  - generateMapFormHtml HOME_DELIVERY 拋 ServiceException 且 verifyNoInteractions(gateway)
  - generateMapFormHtml CVS_711 C2C 會傳 "UNIMARTC2C" 給 gateway

## 7. 文件與提醒

- [x] 7.1 CLAUDE.md 新增「綠界物流型態（B2C / C2C）」段落，含 sys_config key、切換步驟與程式內取法
- [x] 7.2 （與 7.1 合併完成）

## 8. 本地驗證

- [x] 8.1 `mvn -pl cheng-shop -am test` → 37/37 測試通過
- [x] 8.2 `mvn -pl cheng-admin -am package -DskipTests` → WAR 126M 建置成功
- [x] 8.3 `PathHandlingArchitectureTest` → 2/2 通過（無回歸）
- [ ] 8.4 本地啟動後端實機驗證 sys_config 值與 `/shop/logistics/methods` 回傳（視需要）— 留到 UAT 實機驗證階段一併

## 9. 送 PR 與部署 UAT

- [ ] 9.1 Conventional commit（繁體中文）：`fix(shop): 綠界物流子類型以系統參數切換 B2C/C2C，預設 C2C`，body 列出關鍵變更
- [ ] 9.2 推分支、開 PR，summary 引用 `openspec/changes/ecpay-logistics-subtype-config/proposal.md`，test plan 條列部署後驗證項
- [ ] 9.3 merge 到 main → CI 觸發部署
- [ ] 9.4 SSH 到 UAT 確認 Flyway 執行成功：`tail catalina.out` 找 `Migrating schema ... to version 50`

## 10. UAT 實機驗證

- [ ] 10.1 瀏覽器開 `https://cheng.tplinkdns.com/checkout`（登入後）
- [ ] 10.2 確認物流選項只顯示 3 個超商（無宅配）
- [ ] 10.3 點「選擇門市」→ 彈窗顯示綠界電子地圖（不再是錯誤頁）
- [ ] 10.4 抓 `tail -100 ~/tomcat10/logs/catalina.out | grep EcpayLogisticsGateway`，確認 log 顯示 `logisticsSubType=UNIMARTC2C`
- [ ] 10.5 完成一筆測試訂單（含取貨門市、付款）→ 確認 `createShipment` log 顯示成功 `1|AllPayLogisticsID=...`
- [ ] 10.6 若 10.5 成功，archive change：`openspec archive ecpay-logistics-subtype-config`
- [ ] 10.7 若 10.3 仍出現「找不到加密金鑰」，收集 Chrome DevTools Network popup response 並回報 — 可能需要再排查綠界帳號其他設定
