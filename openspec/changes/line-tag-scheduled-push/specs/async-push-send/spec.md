## ADDED Requirements

### Requirement: Asynchronous push send with taskId
POST /line/message/send 在 targetType=TAG 時 SHALL 立即回傳 taskId，在背景非同步執行逐人 PUSH。

#### Scenario: Tag push returns taskId immediately
- **WHEN** POST /line/message/send 帶 targetType=TAG, tagIds=[1,2]
- **THEN** 回傳 HTTP 200 `{ "code": 200, "data": { "taskId": "uuid-xxx" } }` 且發送在背景開始

#### Scenario: Non-tag push remains synchronous
- **WHEN** POST /line/message/send 帶 targetType=SINGLE 或 ALL
- **THEN** 維持現有同步行為，不回傳 taskId

### Requirement: Per-user push with individual status tracking
系統 SHALL 對每位目標使用者獨立呼叫 `client.pushMessage()`，並記錄每人的送達狀態到 `line_push_detail` 表。

#### Scenario: Successful push to user
- **WHEN** 對 userId="U123" 執行 pushMessage 且成功
- **THEN** line_push_detail 記錄 status=SUCCESS, retry_count=0

#### Scenario: Push fails and retries succeed
- **WHEN** 對 userId="U456" 首次 push 失敗（暫時性錯誤），第 2 次重試成功
- **THEN** line_push_detail 記錄 status=SUCCESS, retry_count=1

#### Scenario: Push fails after 3 retries
- **WHEN** 對 userId="U789" 連續 3 次 push 都失敗（暫時性錯誤）
- **THEN** line_push_detail 記錄 status=FAILED, retry_count=3, error_message 包含最後一次錯誤原因

### Requirement: Blocked user detection and marking
系統 SHALL 偵測使用者封鎖情況，不重試封鎖錯誤，並自動標記使用者。

#### Scenario: User has blocked the bot
- **WHEN** pushMessage 回傳 HTTP 400 且錯誤訊息包含 "block" 或 "can't send messages"
- **THEN** line_push_detail 記錄 status=BLOCKED, retry_count=0（不重試），且更新 line_user 表標記該使用者已封鎖

### Requirement: Retry with exponential backoff
重試間隔 SHALL 使用指數退避：第 1 次等 1 秒，第 2 次等 2 秒，第 3 次等 4 秒。

#### Scenario: Retry timing
- **WHEN** 首次 push 失敗（非封鎖錯誤）
- **THEN** 等待 1 秒後第 1 次重試，若仍失敗等 2 秒後第 2 次重試，若仍失敗等 4 秒後第 3 次重試

### Requirement: Aggregate statistics update
逐人 PUSH 完成後，系統 SHALL 更新 `line_message_log` 的聚合統計：successCount、failCount、sendStatus。

#### Scenario: All users sent successfully
- **WHEN** 1000 人全部 push 成功
- **THEN** line_message_log.successCount=1000, failCount=0, sendStatus=SUCCESS

#### Scenario: Partial failure
- **WHEN** 1000 人中 950 人成功、50 人失敗
- **THEN** line_message_log.successCount=950, failCount=50, sendStatus=SUCCESS（部分成功視為成功）

### Requirement: Concurrent task limit
系統 SHALL 限制同時進行的非同步推播任務最多 3 個。

#### Scenario: Exceed concurrent limit
- **WHEN** 已有 3 個推播任務進行中，使用者嘗試發起第 4 個
- **THEN** 回傳 HTTP 429 `{ "code": 429, "msg": "推播任務已滿，請稍後再試" }`
