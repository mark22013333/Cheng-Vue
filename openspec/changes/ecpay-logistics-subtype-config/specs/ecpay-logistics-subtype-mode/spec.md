## ADDED Requirements

### Requirement: 系統參數控制物流子類型模式

系統 SHALL 提供 `shop.ecpay.logistics.sub_type_mode` 系統參數，用於控制綠界物流子類型（`LogisticsSubType`）代碼對映。此參數值 MUST 僅接受 `B2C` 與 `C2C` 兩種值（大小寫不敏感），預設值 SHALL 為 `C2C`。

#### Scenario: 參數為 C2C 時，CVS_711 對映 UNIMARTC2C

- **WHEN** `shop.ecpay.logistics.sub_type_mode = C2C`，且 `ShippingMethod.CVS_711` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"UNIMARTC2C"`

#### Scenario: 參數為 C2C 時，CVS_FAMILY 對映 FAMIC2C

- **WHEN** `shop.ecpay.logistics.sub_type_mode = C2C`，且 `ShippingMethod.CVS_FAMILY` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"FAMIC2C"`

#### Scenario: 參數為 C2C 時，CVS_HILIFE 對映 HILIFEC2C

- **WHEN** `shop.ecpay.logistics.sub_type_mode = C2C`，且 `ShippingMethod.CVS_HILIFE` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"HILIFEC2C"`

#### Scenario: 參數為 B2C 時，CVS_711 對映 UNIMART

- **WHEN** `shop.ecpay.logistics.sub_type_mode = B2C`，且 `ShippingMethod.CVS_711` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"UNIMART"`

#### Scenario: 參數為 B2C 時，CVS_FAMILY 對映 FAMI

- **WHEN** `shop.ecpay.logistics.sub_type_mode = B2C`，且 `ShippingMethod.CVS_FAMILY` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"FAMI"`

#### Scenario: 參數為 B2C 時，CVS_HILIFE 對映 HILIFE

- **WHEN** `shop.ecpay.logistics.sub_type_mode = B2C`，且 `ShippingMethod.CVS_HILIFE` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"HILIFE"`

#### Scenario: 參數為 B2C 時，HOME_DELIVERY 對映 TCAT

- **WHEN** `shop.ecpay.logistics.sub_type_mode = B2C`，且 `ShippingMethod.HOME_DELIVERY` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `"TCAT"`

#### Scenario: 參數為 C2C 時，HOME_DELIVERY 視為不支援

- **WHEN** `shop.ecpay.logistics.sub_type_mode = C2C`，且 `ShippingMethod.HOME_DELIVERY` 呼叫 `getEcpayLogisticsSubType()`
- **THEN** 回傳 `null`，代表此物流方式在當前模式不可用

#### Scenario: 參數值大小寫不敏感

- **WHEN** sys_config 內設為 `c2c`、`C2c`、`b2c` 等任意大小寫
- **THEN** 系統正規化後以 `C2C` 或 `B2C` 處理，行為一致

#### Scenario: 參數值未設或為非法值時走預設

- **WHEN** sys_config 內 `shop.ecpay.logistics.sub_type_mode` 不存在、為空字串，或為 `"ABC"` 等非法值
- **THEN** 系統以預設 `C2C` 處理，不拋例外


### Requirement: 可用物流方式清單依模式過濾

`GET /shop/logistics/methods` API 回傳的可用物流方式清單 SHALL 依 `shop.ecpay.logistics.sub_type_mode` 自動排除不支援的物流方式。`shop.logistics.methods` 中列舉的方式若在當前模式下無對應綠界代碼，MUST 不出現在回傳結果中。

#### Scenario: C2C 模式下過濾掉 HOME_DELIVERY

- **GIVEN** `shop.logistics.methods = "HOME_DELIVERY,CVS_711,CVS_FAMILY,CVS_HILIFE"` 且 `shop.ecpay.logistics.sub_type_mode = C2C`
- **WHEN** 前端呼叫 `GET /shop/logistics/methods`
- **THEN** 回傳清單僅包含 `CVS_711`、`CVS_FAMILY`、`CVS_HILIFE`，不含 `HOME_DELIVERY`

#### Scenario: B2C 模式下保留 HOME_DELIVERY

- **GIVEN** `shop.logistics.methods = "HOME_DELIVERY,CVS_711"` 且 `shop.ecpay.logistics.sub_type_mode = B2C`
- **WHEN** 前端呼叫 `GET /shop/logistics/methods`
- **THEN** 回傳清單包含 `HOME_DELIVERY` 與 `CVS_711`

#### Scenario: 前端不需感知模式

- **WHEN** 前端只依 API 回傳的清單渲染物流選項
- **THEN** 無需讀取或判斷 `shop.ecpay.logistics.sub_type_mode`；行為隨後端設定自動調整


### Requirement: 不支援組合在呼叫綠界前攔截

當 `ShippingMethod` 在當前模式下無對應綠界代碼（例如 C2C + HOME_DELIVERY）時，`EcpayLogisticsGateway.generateMapFormHtml()` 與 `createShipment()` MUST 在呼叫綠界 API 之前拋出 `ServiceException`，錯誤訊息需明確指出「目前物流型態（X）不支援 Y」。

#### Scenario: C2C 模式下呼叫電子地圖表單帶 HOME_DELIVERY

- **GIVEN** `shop.ecpay.logistics.sub_type_mode = C2C`
- **WHEN** `generateMapFormHtml(storeKey, "HOME_DELIVERY", replyUrl)` 被呼叫（例如前端繞過 UI 過濾直接送請求）
- **THEN** 拋出 `ServiceException`，錯誤訊息包含「C2C」與「宅配」字樣，且不對綠界發出任何網路請求

#### Scenario: C2C 模式下建立宅配物流訂單

- **GIVEN** `shop.ecpay.logistics.sub_type_mode = C2C`，訂單 `shippingMethod = HOME_DELIVERY`
- **WHEN** `createShipment(order)` 被呼叫
- **THEN** 回傳 `LogisticsResult.fail(...)`，訊息包含「C2C」與「宅配」字樣，且不對綠界發出任何網路請求

#### Scenario: 任一模式下超商取貨正常流程不受影響

- **GIVEN** `shop.ecpay.logistics.sub_type_mode = C2C` 或 `B2C`
- **WHEN** `generateMapFormHtml(storeKey, "CVS_711", replyUrl)` 被呼叫
- **THEN** 系統組出含正確 `LogisticsSubType`（C2C 模式送 `UNIMARTC2C`、B2C 模式送 `UNIMART`）與對應 CheckMacValue 的 HTML form，並送達綠界電子地圖 URL


### Requirement: 模式正規化容器

系統 SHALL 提供 `LogisticsSubTypeMode` 列舉（`B2C`、`C2C`）作為型別安全的模式代表，並提供 `fromConfigValue(String)` 靜態方法用於從 sys_config 字串容錯解析。

#### Scenario: 解析合法值

- **WHEN** `LogisticsSubTypeMode.fromConfigValue("B2C")` 或 `fromConfigValue("c2c")`
- **THEN** 回傳對應的 `B2C` 或 `C2C` 列舉，大小寫不敏感

#### Scenario: 解析非法值回傳預設

- **WHEN** `LogisticsSubTypeMode.fromConfigValue("xyz")`、`fromConfigValue(null)` 或 `fromConfigValue("")`
- **THEN** 回傳 `C2C`（預設值），不拋例外

#### Scenario: 解析時去除前後空白

- **WHEN** `LogisticsSubTypeMode.fromConfigValue("  B2C  ")`
- **THEN** 回傳 `B2C`
