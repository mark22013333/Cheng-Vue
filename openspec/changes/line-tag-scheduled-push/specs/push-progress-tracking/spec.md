## ADDED Requirements

### Requirement: Progress tracking API
系統 SHALL 提供 GET /line/message/send/progress/{taskId} API，回傳推播任務的即時進度。

#### Scenario: Task in progress
- **WHEN** GET /line/message/send/progress/uuid-xxx 且任務正在執行
- **THEN** 回傳 `{ "taskId": "uuid-xxx", "status": "SENDING", "total": 1000, "sent": 500, "success": 495, "failed": 5, "startTime": "...", "estimatedRemainingSeconds": 5 }`

#### Scenario: Task completed
- **WHEN** GET /line/message/send/progress/uuid-xxx 且任務已完成
- **THEN** 回傳 `{ "taskId": "uuid-xxx", "status": "DONE", "total": 1000, "sent": 1000, "success": 993, "failed": 7, "messageId": 123 }`，messageId 為對應的 LineMessageLog ID

#### Scenario: Task not found
- **WHEN** GET /line/message/send/progress/invalid-id
- **THEN** 回傳 HTTP 404 `{ "code": 404, "msg": "任務不存在或已過期" }`

### Requirement: In-memory progress storage
進度狀態 SHALL 儲存在 ConcurrentHashMap，任務完成後保留 30 分鐘再自動清除。

#### Scenario: Progress auto-cleanup
- **WHEN** 任務完成已超過 30 分鐘
- **THEN** 進度資料從記憶體清除，後續 polling 回傳 404

### Requirement: Frontend progress dialog
前端 SHALL 在標籤推播送出後彈出進度 Dialog，每 3 秒 polling 一次，顯示進度條和即時統計。

#### Scenario: Progress dialog during send
- **WHEN** 使用者發起標籤推播且後端回傳 taskId
- **THEN** 彈出 Dialog 顯示進度條（sent/total）、成功數、失敗數，每 3 秒更新

#### Scenario: Progress dialog on completion
- **WHEN** polling 回傳 status=DONE
- **THEN** Dialog 顯示最終結果，進度條為 100%，停止 polling，顯示「查看詳情」按鈕連結到發送記錄

#### Scenario: Close dialog during send
- **WHEN** 使用者在發送進行中關閉 Dialog
- **THEN** 發送繼續在背景執行（不中斷），使用者可在「發送記錄」Tab 查看結果
