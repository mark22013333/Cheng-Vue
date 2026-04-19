## ADDED Requirements

### Requirement: 合併 API
系統 SHALL 提供 `POST /shop/member/tracking/merge` 端點（需登入），接收 `{ guestId: "uuid-xxx" }`。後端將 `shop_browsing_logs` 和 `shop_search_logs` 中所有 `guest_id = guestId` 的記錄的 `member_id` 更新為當前登入會員 ID，並將 `guest_id` 設為 NULL。

#### Scenario: 合併訪客記錄
- **WHEN** 會員登入後呼叫 POST `/shop/member/tracking/merge` 帶 `{ guestId: "abc-123" }`
- **THEN** 系統將兩張表中 `guest_id = "abc-123"` 的所有記錄的 `member_id` 更新為該會員 ID，`guest_id` 設為 NULL

#### Scenario: 無匹配記錄
- **WHEN** 呼叫合併 API 但該 guestId 在 DB 中無任何記錄
- **THEN** 系統回傳 200（靜默成功，不報錯）

#### Scenario: 未登入呼叫合併
- **WHEN** 未登入使用者呼叫 POST `/shop/member/tracking/merge`
- **THEN** 系統回傳 401

### Requirement: 前端登入後自動觸發合併
前端在會員登入成功後（收到 token 回應後），SHALL 自動檢查是否有 `guest_tracking_id` Cookie。若有，則：
1. 呼叫合併 API 帶上 guestId（fire-and-forget）
2. 清除 `guest_tracking_id` Cookie

#### Scenario: 登入後自動合併
- **WHEN** 使用者以帳號密碼登入成功，且有 `guest_tracking_id` Cookie 值為 `"abc-123"`
- **THEN** 前端自動呼叫 `POST /shop/member/tracking/merge` 帶 `{ guestId: "abc-123" }`，然後清除 Cookie

#### Scenario: OAuth 登入後自動合併
- **WHEN** 使用者透過 LINE/Google OAuth 登入成功，且有 `guest_tracking_id` Cookie
- **THEN** 同樣觸發合併流程

#### Scenario: 登入但無訪客 Cookie
- **WHEN** 使用者登入成功但無 `guest_tracking_id` Cookie
- **THEN** 不呼叫合併 API
