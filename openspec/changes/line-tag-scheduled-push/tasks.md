## 1. 資料庫與基礎設施

- [x] 1.1 建立 Flyway migration：新增 `line_push_detail` 表（detail_id, message_id, line_user_id, line_display_name, status, retry_count, error_message, send_time）
- [x] 1.2 建立 `LinePushDetail` Domain 類別（含 Lombok、枚舉 PushDetailStatus: SUCCESS/FAILED/BLOCKED）
- [x] 1.3 建立 `LinePushDetailMapper` 介面 + XML（insertBatch、selectByMessageId、countByStatus）
- [x] 1.4 建立 `SendProgressDTO`（taskId, status, total, sent, success, failed, startTime, estimatedRemainingSeconds, messageId）

## 2. 標籤目標解析

- [x] 2.1 建立 `ILineTagResolveService` 介面：resolveTargets(tagIds, tagGroupIds) → Set<String>、previewCount(tagIds, tagGroupIds) → TagPreviewDTO
- [x] 2.2 實作 `LineTagResolveServiceImpl`：個別標籤聯集解析
- [x] 2.3 實作標籤群組解析：根據 SysTagGroup 的 calcMode 和 SysTagGroupDetail 的 AND/OR 邏輯
- [x] 2.4 新增 GET /line/message/tag/preview API 端點，回傳預計人數和各標籤明細

## 3. 非同步逐人 PUSH 發送引擎

- [x] 3.1 在 `LineMessageSendServiceImpl` 新增 `sendPushToEach(client, userIds, message, messageId)` 方法：逐人 PUSH + 重試 3 次（指數退避 1s/2s/4s）+ 封鎖偵測
- [x] 3.2 實作進度追蹤：ConcurrentHashMap<String, SendProgressDTO> 暫存進度，任務完成後 30 分鐘自動清除（ScheduledExecutorService）
- [x] 3.3 在 `determineTargetType` switch 加入 TAG case：解析標籤 → 收集 userIds → 呼叫 sendPushToEach
- [x] 3.4 將 TAG 推播改為 @Async 非同步執行，POST /send 回傳 taskId
- [x] 3.5 新增 GET /line/message/send/progress/{taskId} API 端點
- [x] 3.6 實作同時推播任務數限制（最多 3 個）

## 4. SendMessageDTO 擴充

- [x] 4.1 `SendMessageDTO` 新增 `targetTagIds: List<Long>`、`targetTagGroupIds: List<Long>` 欄位
- [x] 4.2 更新前端 API 呼叫，TAG 模式傳送 targetTagIds/targetTagGroupIds

## 5. 排程推播

- [x] 5.1 `ScheduledTaskType` enum 新增 `LINE_TAG_PUSH`（code, beanName, methodName, parameters: configId/templateId/tagIds/tagGroupIds）
- [x] 5.2 建立 `LineScheduledPushTask` @Component：execute(paramsJson) → 解析參數 → resolveTargets → sendPushToEach → 記錄 LineMessageLog
- [x] 5.3 驗證現有 SysJob CRUD API 可正常建立 LINE_TAG_PUSH 類型任務

## 6. 前端：Tab 佈局重構

- [x] 6.1 index.vue 重構為 el-tabs 3 個 Tab（發送訊息 / 排程列表 / 發送記錄）
- [x] 6.2 發送訊息 Tab：新增第四個 radio「標籤推播」
- [x] 6.3 TAG 模式 UI：標籤多選器（帶人數顯示）+ 標籤群組選擇器 + 預計人數顯示（呼叫 preview API）
- [x] 6.4 新增「排程發送」按鈕，彈出排程設定 Dialog（一次性: 日期+時間選擇器 / 重複: cron 輸入）

## 7. 前端：進度追蹤

- [x] 7.1 建立推播進度 Dialog 元件：進度條 + 成功/失敗計數 + 預估剩餘時間
- [x] 7.2 實作 polling 機制：每 3 秒呼叫 GET /progress/{taskId}，status=DONE 時停止
- [x] 7.3 關閉 Dialog 不中斷發送，在發送記錄 Tab 可查看結果

## 8. 前端：排程列表 Tab

- [x] 8.1 建立排程列表表格：篩選 jobGroup="LINE_PUSH" 的 SysJob，顯示名稱/範本/標籤/頻率/狀態/下次執行
- [x] 8.2 實作暫停/恢復/刪除操作（呼叫現有 /monitor/job API）
- [x] 8.3 新增排程 Dialog（從發送訊息 Tab 的「排程發送」按鈕觸發，預填範本和標籤）

## 9. 前端：發送記錄 Tab

- [x] 9.1 將現有 timeline 改為 el-table 表格：時間/範本/方式/對象/成功/失敗/狀態
- [x] 9.2 新增「查看明細」按鈕，彈出 Dialog 顯示 line_push_detail 的逐人狀態
- [x] 9.3 支援篩選（日期範圍、發送方式、狀態）

## 10. 測試與驗證

- [ ] 10.1 標籤解析 Service 單元測試：聯集、交集、去重、空標籤
- [ ] 10.2 逐人 PUSH 單元測試：成功、重試、封鎖偵測、3 次失敗
- [ ] 10.3 進度追蹤單元測試：並發安全、自動清除、任務上限
- [ ] 10.4 排程推播整合測試：建立排程 → Quartz 觸發 → 發送執行
- [ ] 10.5 前端 QA：Tab 切換、標籤選擇、進度 Dialog、排程 CRUD、手機版 RWD
