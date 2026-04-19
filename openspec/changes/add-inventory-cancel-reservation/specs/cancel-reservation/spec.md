## ADDED Requirements

### Requirement: 使用者取消待審核預約
系統 SHALL 提供 API 端點，允許使用者取消自己的待審核預約（`reserve_status=1`），將狀態更新為已取消（`reserve_status=4`），並恢復庫存數量。

#### Scenario: 成功取消預約
- **WHEN** 使用者呼叫 `POST /inventory/management/cancel-reserve` 傳入 `borrowId`，且該預約為自己建立的待審核預約
- **THEN** 系統將預約狀態更新為 CANCELLED(4)，`reserved_qty` 減少對應數量，`available_qty` 增加對應數量，回傳成功

#### Scenario: 非待審核狀態無法取消
- **WHEN** 使用者嘗試取消已通過（APPROVED）、已拒絕（REJECTED）或已取消（CANCELLED）的預約
- **THEN** 系統回傳錯誤訊息「僅待審核的預約可以取消」

#### Scenario: 非本人預約無法取消
- **WHEN** 使用者嘗試取消他人建立的預約
- **THEN** 系統回傳錯誤訊息「只能取消自己的預約」

#### Scenario: 預約記錄不存在
- **WHEN** 使用者傳入不存在的 `borrowId`
- **THEN** 系統回傳錯誤訊息「預約記錄不存在」

### Requirement: 取消預約後通知審核人員
系統 SHALL 在預約取消後，自動建立系統通知（`SysNotice`），發送給具有 `inventory:borrow:approve` 權限的所有使用者。

#### Scenario: 通知內容正確
- **WHEN** 使用者成功取消預約
- **THEN** 系統建立 `SysNotice` 記錄，noticeType=1（通知），標題包含物品名稱與取消者姓名，內容包含預約詳細資訊（物品名、預約數量、預約日期）

#### Scenario: 無審核權限使用者時不建立通知
- **WHEN** 系統中沒有任何使用者具有 `inventory:borrow:approve` 權限
- **THEN** 系統正常完成取消操作，不建立通知（不影響取消流程）

### Requirement: 取消預約後發布 SSE 事件
系統 SHALL 在預約取消後發布 `ReservationEvent`（eventType="cancelled"），讓前端透過 SSE 即時更新庫存顯示。

#### Scenario: SSE 事件廣播
- **WHEN** 使用者成功取消預約
- **THEN** 系統發布 `ReservationEvent`，包含 itemId、最新 availableQuantity 和 reservedQuantity

### Requirement: 前端取消預約按鈕
前端庫存管理頁面 SHALL 在預約記錄中顯示「取消預約」按鈕，僅對自己的待審核預約可見。

#### Scenario: 按鈕顯示條件
- **WHEN** 預約記錄的 `reserve_status=1`（待審核）且 `create_by` 為當前登入使用者
- **THEN** 顯示「取消預約」按鈕

#### Scenario: 點擊取消預約
- **WHEN** 使用者點擊「取消預約」按鈕並確認
- **THEN** 前端呼叫取消 API，成功後重新載入列表並顯示成功訊息

#### Scenario: 取消前確認
- **WHEN** 使用者點擊「取消預約」按鈕
- **THEN** 系統顯示確認對話框，使用者確認後才執行取消
