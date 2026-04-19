## Context

CoolApps 的 LINE 推播系統目前支援三種發送方式：單人 PUSH、多人 MULTICAST、全體 BROADCAST。後端已有完整的標籤系統（`sys_tag` + `line_user_tag_relation` + `sys_tag_group`），`TargetType.TAG` 枚舉已定義但未實作。Quartz 排程系統已有完整的 Job 管理 CRUD 和前端 UI。

核心限制：LINE Multicast API 不回傳逐人送達狀態，封鎖使用者會靜默失敗。LINE Push API 有速率限制約 100 req/s。

## Goals / Non-Goals

**Goals:**
- 管理員可透過選擇標籤/標籤群組精準推播給特定族群
- 每位使用者有獨立的送達狀態追蹤
- 支援一次性和重複排程推播
- 大量推播在背景非同步執行，前端即時顯示進度

**Non-Goals:**
- 不實作 A/B 測試或分群測試
- 不實作推播效果分析（開封率等，LINE API 不支援）
- 不修改現有 PUSH/MULTICAST/BROADCAST 邏輯（保持向後相容）
- 不實作推播訊息內容的動態變數替換（如 {{userName}}）

## Decisions

### D1: 逐人 PUSH 取代 MULTICAST 做標籤推播

**選擇**：標籤推播使用逐人 `client.pushMessage()` 而非 `client.multicast()`

**替代方案**：用 MULTICAST 分批（每批 500 人）發送

**理由**：MULTICAST 回傳 202 Accepted 不含逐人狀態，被封鎖的使用者靜默失敗。逐人 PUSH 可追蹤每人送達結果，自動偵測封鎖。代價是速度較慢（1000 人 ~10 秒 vs MULTICAST 瞬間），但標籤推播的精準度需求高於速度需求。

### D2: 非同步發送 + Polling 進度追蹤

**選擇**：POST /send 立即回傳 taskId，背景 @Async 執行，前端每 3 秒 polling GET /send/progress/{taskId}

**替代方案**：WebSocket 即時推送進度

**理由**：Polling 實作簡單、不需要新增 WebSocket 基礎設施、3 秒間隔對使用者體驗已足夠。進度狀態用 ConcurrentHashMap 暫存在記憶體（不需要 Redis），任務完成後保留 30 分鐘再清除。

### D3: 重試 3 次 + 指數退避

**選擇**：每人 PUSH 失敗時重試最多 3 次，間隔 1s → 2s → 4s（指數退避）

**理由**：區分暫時性錯誤（網路抖動）和永久性錯誤（使用者封鎖）。封鎖錯誤（HTTP 400, error code: "You can't send messages to this user"）不重試，直接標記。

### D4: 整合現有 Quartz 系統做排程

**選擇**：新增 `ScheduledTaskType.LINE_TAG_PUSH` + `LineScheduledPushTask` @Component，用現有 `sys_job` 表和排程管理 UI

**替代方案**：建立獨立的 `line_scheduled_message` 表 + 專用 Quartz Job 輪詢

**理由**：零新 DB 表、零新管理 UI，利用現有完整的排程 CRUD、暫停/恢復、執行日誌功能。一次性排程用特定日期的 cron 表達式模擬（例如 `0 0 14 30 3 ? 2026`）。

### D5: 逐人發送明細表 line_push_detail

**選擇**：新增 `line_push_detail` 表記錄每人的發送狀態

**結構**：
```
line_push_detail
├── detail_id (PK)
├── message_id (FK → line_message_log)
├── line_user_id
├── line_display_name（快照）
├── status (SUCCESS / FAILED / BLOCKED)
├── retry_count
├── error_message
├── send_time
```

**理由**：`line_message_log` 只有聚合統計（successCount/failCount），無法知道「誰」沒收到。明細表讓管理員能查看失敗名單並手動重發。

### D6: 前端重構為 3 Tab 佈局

**選擇**：推播訊息頁面從左右分欄改為 Tab 導航：發送訊息 / 排程列表 / 發送記錄

**理由**：新增排程功能和發送記錄改為表格後，單頁面放不下。Tab 佈局讓每個功能有獨立空間，也更適合手機版。

## Risks / Trade-offs

- **[速率限制]** 逐人 PUSH 受 LINE rate limit 約束。5000+ 人的推播需要 ~50 秒。→ 緩解：非同步背景執行 + 進度追蹤，使用者不需等待。
- **[記憶體壓力]** 進度狀態存 ConcurrentHashMap。如果同時有多個大量推播任務，記憶體佔用增加。→ 緩解：限制同時進行的推播任務數量（建議最多 3 個）。
- **[排程推播的標籤變動]** 排程建立時的標籤人數和實際執行時可能不同（期間有新用戶貼標或解標）。→ 這是預期行為，每次執行時即時解析標籤。
- **[一次性排程的 cron 模擬]** 用特定日期 cron 模擬一次性排程不夠直觀。→ 緩解：前端排程 Dialog 提供日期選擇器，自動轉換為 cron。
