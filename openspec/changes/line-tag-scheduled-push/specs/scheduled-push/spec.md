## ADDED Requirements

### Requirement: Create scheduled push via Quartz
系統 SHALL 允許建立排程推播任務，整合現有 Quartz SysJob 系統，使用 ScheduledTaskType.LINE_TAG_PUSH 模板。

#### Scenario: Create one-time schedule
- **WHEN** 使用者在前端選擇範本、標籤、頻道，設定一次性排程「2026-04-01 14:00」
- **THEN** 系統建立 SysJob，invokeTarget 為 `lineScheduledPushTask.execute(json)`，cronExpression 為對應的一次性 cron（如 `0 0 14 1 4 ? 2026`）

#### Scenario: Create recurring schedule
- **WHEN** 使用者設定重複排程「每週一早上 9 點」
- **THEN** 系統建立 SysJob，cronExpression 為 `0 0 9 ? * MON`

### Requirement: LineScheduledPushTask execution
`lineScheduledPushTask.execute(paramsJson)` SHALL 解析參數、解析標籤目標、執行逐人 PUSH，並記錄 LineMessageLog。

#### Scenario: Scheduled task executes successfully
- **WHEN** Quartz 觸發 LineScheduledPushTask，paramsJson 包含 configId=1, templateId=5, tagIds="1,2"
- **THEN** 解析 tag 1 和 tag 2 的使用者，使用 configId=1 的頻道和 templateId=5 的範本，執行逐人 PUSH，記錄 LineMessageLog

#### Scenario: Scheduled task with tag group
- **WHEN** paramsJson 包含 tagGroupIds="3"
- **THEN** 使用標籤群組 3 的 AND/OR 邏輯解析目標使用者

### Requirement: Schedule management from message page
前端推播訊息頁面的「排程列表」Tab SHALL 顯示所有 LINE 推播排程，並支援暫停、恢復、刪除操作。

#### Scenario: View scheduled pushes
- **WHEN** 使用者切換到「排程列表」Tab
- **THEN** 列出所有 jobGroup="LINE_PUSH" 的 SysJob，顯示排程名稱、範本、標籤、cron 說明、狀態、下次執行時間

#### Scenario: Pause a schedule
- **WHEN** 使用者點擊暫停按鈕
- **THEN** 呼叫 PUT /monitor/job/changeStatus 將 job 狀態改為 PAUSE

#### Scenario: Delete a schedule
- **WHEN** 使用者點擊刪除按鈕並確認
- **THEN** 呼叫 DELETE /monitor/job/{jobId} 刪除排程

### Requirement: Schedule creation dialog in message page
前端推播頁面 SHALL 提供排程設定 Dialog，整合範本選擇和標籤選擇。

#### Scenario: Open schedule dialog
- **WHEN** 使用者在發送訊息 Tab 點擊「排程發送」按鈕
- **THEN** 彈出 Dialog，預填已選的範本和標籤，使用者只需設定排程時間

#### Scenario: One-time mode UI
- **WHEN** 使用者選擇「一次性」模式
- **THEN** 顯示日期選擇器 + 時間選擇器，前端自動轉換為 cron expression

#### Scenario: Recurring mode UI
- **WHEN** 使用者選擇「重複」模式
- **THEN** 顯示 cron 輸入框 + 「產生器」按鈕 + 人類可讀的 cron 說明
