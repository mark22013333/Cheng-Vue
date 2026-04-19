# 標籤模組規格（依現行專案架構調整版）

## 1. 目標與範疇
- 以「LINE」為唯一落地平台（現行資料表已存在 `line_user_tag*`）；保留跨平台擴充位但不再假設多表前置。
- 聚焦「使用者標籤」的管理、關聯與後續行銷（訊息發送）查詢。
- 與既有 LINE 模組（rich menu、template、message log）一致：資料表位於 `cheng-admin` migration，類別於 `cheng-line`。

## 2. 核心概念
- **LINE 使用者 (line_user)**：LINE userId 為主鍵。
- **標籤 (line_user_tag)**：行銷或分眾屬性，含代碼、顏色、描述、狀態、排序。
- **標籤關聯 (line_user_tag_relation)**：使用者與標籤的多對多關係。
- **計數欄位 (user_count)**：標籤底下的使用者數；需要在批次或事件更新。

## 3. 資料表（依 db-migration）
- `line_user_tag`（來源：V10__init_line_module.sql，V32 將 `sys_` 移除）
  - `tag_id` PK, auto increment
  - `tag_name`(100), `tag_code`(100, unique), `tag_color`(20, nullable), `tag_description`(500)
  - `user_count` int default 0
  - `status` tinyint default 1（1=啟用, 0=停用）
  - `sort_order` int default 0
  - 審計欄位：`create_by`, `create_time`, `update_by`, `update_time`
  - 索引：`uk_tag_code`, `idx_status`
- `line_user_tag_relation`
  - `id` PK
  - `line_user_id` varchar(255) not null（對應 `line_user.line_user_id`）
  - `tag_id` bigint not null（對應 `line_user_tag.tag_id`）
  - `create_by`, `create_time`
  - 索引：`uk_user_tag(line_user_id, tag_id)`, `idx_line_user_id`, `idx_tag_id`
- （已存在但本規格不改動）`line_user`, `line_message_log`, `line_conversation_log`…保持兼容。

## 4. 權責與邏輯分層
- **資料層**：MyBatis Mapper 對應上述表；命名遵循 `LineUserTagMapper`、`LineUserTagRelationMapper`。
- **服務層**：`LineUserTagService` 提供 CRUD、狀態啟停、排序；`LineUserTagRelationService` 提供批次新增/刪除、計數回寫。
- **介面層**：Controller 於 `cheng-line`/`cheng-admin`（依現行 LINE 模組放置）暴露 REST API，沿用統一回傳格式 `AjaxResult`。
- **任務/事件**：若需大量回寫 `user_count`，以排程或事件驅動批次刷新。

## 5. API 需求（建議）
- 標籤管理
  - 新增/修改/刪除標籤（防重複 `tag_code`）。
  - 啟用/停用標籤。
  - 調整排序。
  - 查詢列表（分頁、狀態篩選、名稱/代碼模糊）。
- 標籤關聯
  - 依使用者批次新增/移除標籤（多個 tagId）。
  - 依標籤批次移除使用者。
  - 查詢：指定使用者的標籤清單；指定標籤的使用者（需分頁）。
- 事件貼標（互動驅動）
  - 訊息回覆貼標：LINE webhook 收到使用者回覆後，根據規則（關鍵字/場景）呼叫 `LineUserTagRelationService.bindTags(lineUserId, tagCodes)`。
  - 點擊後台產生連結貼標：在短鏈接/追蹤連結跳轉記錄中識別 lineUserId 或狀態碼，觸發貼標；須在後台建立「連結 → tagCode 列表」對照。
  - 保證冪等：同一 `(line_user_id, tag_id)` 插入應命中唯一鍵，不應拋出錯誤。
  - 異常處理：line_user 不存在則記錄警告並略過，必要時先建 user 再貼標（看產品需求）。
- 計數維護
  - 批次重算 `user_count`（全量）。
  - 事件式增減（新增關聯 +1，刪除關聯 -1；需防重複/不存在）。

## 6. 邏輯規則
- `tag_code` 唯一且不可更名後重複；重名檢查走 DB 唯一鍵。
- 停用標籤：保留關聯記錄但不再參與後續行銷圈選；前端需明顯標示。
- 刪除標籤：如系統允許，需先移除關聯（或採軟刪除）；預設建議「不可刪除、僅停用」避免遺失歷史。
- 批次關聯：同一 `(line_user_id, tag_id)` 重複請求應冪等；以唯一鍵約束 + 服務層判斷。
- `user_count` 正確性優先：事件式更新失敗時需有重算排程兜底。

## 7. 批次/排程建議
- **重算使用者計數**
  - 來源：`line_user_tag_relation`
  - 輸出：更新 `line_user_tag.user_count`
  - 步驟：按 `tag_id` group by 計數 → 批次更新。
  - 時機：每日定時，或大量匯入後手動觸發。
- **清理孤兒關聯**（可選）
  - 刪除不存在 `line_user` 或 `line_user_tag` 的關聯列。

## 8. 前端（cheng-ui）互動要求
- 路由命名遵守唯一性規則；使用既有權限與按鈕組件。
- 列表分頁、篩選（名稱/代碼/狀態）、排序。
- 標籤顏色欄位用於前端標籤顯示；可提供預設色盤。
- 批次指派：支援多選使用者後批次貼標；操作需顯示成功/失敗明細。
- 防呆：`tag_code` 輸入時即時檢查、停用標籤操作需確認。

## 9. 擴充到其他平台的最小約定
- 新平台需自備三張表（命名示例，保持與 LINE 對齊欄位結構）：
  - `<platform>_user`
  - `<platform>_user_tag`
  - `<platform>_user_tag_relation`
- 介面與服務可抽象為 `TagService<T extends User>` 等泛型；目前先以 LINE 為實作，後續平台可透過 Factory/Strategy 註冊。
- 若未新增新平台前，不建立跨平台共用 staging 或 group 匯出表。

## 10. 標籤群組（Tag Group）運算概念與落地指引
- 目的：支援「標籤群組」圈選邏輯，便於後續訊息發送或報表。現階段尚未建立群組表，需新增 migration 才能啟用。
- 建議表結構（待新增 migration）
  - `line_tag_group_list`：`tag_group_id` PK、`group_name`、`calc_mode`（NULL=LEFT_TO_RIGHT）、`count_uid`、審計欄位。
  - `line_tag_group_detail`：`id` PK、`tag_group_id`、`group_index`、`tag_id`、`operator`（AND/OR，首筆可空）。
- 運算模式（CALC_MODE）
  - `LEFT_TO_RIGHT`（預設）：依 `group_index` 從左到右 fold，不套用布林優先權。
    - 範例 `A AND B OR C AND D` 解析為 `((((A AND B) OR C) AND D) ...)`。
  - `OR_OF_AND`：以 OR 切段，段內為 AND，段與段做 OR（DNF）。
    - 範例 `A AND B OR C AND D` 解析為 `(A AND B) OR (C AND D)`。
  - CALC_MODE 為 NULL 或空字串時一律視為 `LEFT_TO_RIGHT`，保持舊資料行為。
- 批次流程（LINE 單平台簡化版）
  1. 讀取群組規則：`line_tag_group_detail` + 對應 `calc_mode`。
  2. 分頁拉取 `line_user_tag_relation`（Keyset Pagination；Safety Cutoff 確保同一 `line_user_id` 不跨頁）。
  3. 依 `line_user_id` 彙總 tag set，套用群組運算模式，找出符合群組的使用者。
  4. 產出群組匯出結果（可寫入 `line_tag_group_export` 暫存/目標表，需另建表），批次寫入 staging 後 merge 目標。
  5. 批次結束後 group by 回寫 `count_uid`，清理 staging（依 `batch_id`）。
- 屬性優先權
  - 單平台 LINE 不需要 BU/PUBLIC 合併；若未來引入跨表來源，再加上「平台資料優先於公共資料」的屬性選擇邏輯。
- 重要守則
  - Keyset Pagination + Safety Cutoff，避免 OFFSET 慢與切斷同 UID。
  - 需索引：`line_user_tag_relation` 上 `line_user_id`、`tag_id`。
  - 事件式增減與排程重算並存：事件更新 `user_count`，每日排程全量重算兜底。

## 11. 監控與審計
- 操作日誌（@Log）需記錄：標籤名稱/代碼、影響的使用者數、操作者。
- 批次重算：記錄開始/結束時間、成功/失敗標籤數、錯誤摘要。
- 關聯異常：唯一鍵衝突與缺少對應使用者/標籤需在日誌中記錄並回傳明確訊息。

## 12. 安全與資料品質
- 服務層參數校驗：`tag_id`、`tag_code`、`line_user_id` 不可空；批次輸入須檢查長度與重複。
- 防重複寫入：依賴 DB 唯一鍵與服務層冪等判斷。
- 停用標籤在前端圈選時需過濾（僅顯示啟用），但歷史紀錄保留。

## 13. 待辦與落地路線
- 建立/確認 Mapper、Service、Controller（若尚未實作）並串接現有權限菜單。
- 補充排程：`user_count` 重算 + 孤兒關聯清理。
- 前端頁面：標籤管理、使用者貼標、批次貼標；遵循路由唯一與 Pinia 狀態管理規範（若使用）。
- 若未來加入其他平台，再行新增對應表與 Platform/Strategy 註冊點。
