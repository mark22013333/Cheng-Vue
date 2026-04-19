## Why

LINE 推播訊息目前只能指定個別使用者或廣播全部，無法根據標籤精準推播給特定族群。管理員需要手動查找貼標使用者再逐一加入推播名單，效率低且容易遺漏。同時缺乏排程功能，所有推播都必須即時手動執行，無法預約在最佳時段發送。

## What Changes

- 新增**標籤推播**發送方式：選擇標籤或標籤群組，系統自動解析貼標使用者並逐人 PUSH 發送，追蹤每人送達狀態
- 新增**排程推播**功能：支援一次性和重複排程，整合現有 Quartz 排程系統
- 改為**非同步發送**架構：大量推播在背景執行，前端透過 polling（每 3 秒）追蹤進度
- 新增**逐人重試機制**：每人最多重試 3 次，失敗記錄具體原因，封鎖使用者自動標記
- 重新設計推播頁面為 **3 個 Tab**：發送訊息 / 排程列表 / 發送記錄

## Capabilities

### New Capabilities
- `tag-target-resolve`: 標籤目標解析，支援個別標籤（聯集）和標籤群組（AND/OR 邏輯），去重後回傳 LINE User ID 清單
- `async-push-send`: 非同步逐人 PUSH 發送引擎，含重試 3 次機制、封鎖偵測、進度追蹤 API
- `scheduled-push`: 排程推播，整合 Quartz ScheduledTaskType，支援一次性（cron 模擬）和重複排程
- `push-progress-tracking`: 推播進度追蹤，提供 taskId + polling API，回傳即時的 sent/success/failed 計數

### Modified Capabilities

## Impact

- **後端 cheng-line 模組**：新增標籤解析 Service、修改 LineMessageSendServiceImpl 加入 TAG 和非同步 PUSH 邏輯
- **後端 cheng-quartz 模組**：ScheduledTaskType enum 擴充、新增 LineScheduledPushTask
- **後端 cheng-admin 模組**：LineMessageSendController 新增 progress API
- **前端 index.vue**：重構為 3 Tab 佈局，新增標籤選擇器、進度 Dialog、排程管理列表
- **API 變更**：POST /line/message/send 改為回傳 taskId（非同步），新增 GET /line/message/send/progress/{taskId}
- **資料庫**：LineMessageLog 現有 targetTagId 欄位啟用，可能需新增 line_push_detail 表記錄逐人狀態
