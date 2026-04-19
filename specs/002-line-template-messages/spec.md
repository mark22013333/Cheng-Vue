# 功能規格書 (Feature Specification): LINE Template 訊息範本管理

**功能分支 (Feature Branch)**: `feature/line-template-message`
**建立日期 (Created)**: 2025-12-29
**狀態 (Status)**: Draft
**輸入 (Input)**: 依據 LINE Messaging API Template Messages 說明 (buttons/confirm/carousel/image-carousel) @docs/Line/LINE_API_DOCS.md

## 用戶場景與測試 (User Scenarios & Testing)

### 用戶故事 1 - 建立 Template 訊息範本 (優先級: P1)
管理員希望在後台建立 LINE Template 訊息 (Buttons/Confirm/Carousel/Image Carousel)，以便推播或自動回覆時重複使用。

**獨立測試**:
1. 透過範本建立 API 傳入 `type=template`、`altText`、`template` 物件，確認寫入 `line_message_template` 並 `msg_type='TEMPLATE'`。
2. 驗證 `content` JSON 的 `template.type` 必須為 buttons/confirm/carousel/image_carousel 之一。

**驗收場景**:
1. **Given** 管理員輸入 altText、選擇 Buttons 並設定 3 個 actions，**When** 儲存，**Then** 系統儲存完整 JSON 並能在前端預覽。
2. **Given** 缺少 altText 或 actions 超過 4 個，**When** 儲存，**Then** 系統提示錯誤且不建立範本。

---

### 用戶故事 2 - Carousel 多欄設定與驗證 (優先級: P1)
營運人員建立 Carousel 範本以一次展示多筆商品卡片，需確保欄位與圖片設定符合 LINE 規範。

**獨立測試**:
1. 建立 `template.type=carousel`，`columns` 介於 1~10，並含 actions<=3；驗證儲存成功。
2. 當 `columns`>10 或任一 column 缺少 text 時，驗證被阻擋並回傳錯誤訊息。

**驗收場景**:
1. **Given** 5 個商品卡片且每卡 3 個 actions，**When** 儲存，**Then** 能生成合法 JSON 並可推播成功 (HTTP 200)。
2. **Given** 任一 column text 超過 120 字或缺圖、缺 text，**When** 儲存，**Then** 伺服器回傳驗證錯誤。

---

### 用戶故事 3 - Image Carousel 點擊導向 (優先級: P2)
行銷人員建立 Image Carousel 以圖片導流到多個網址，需確保 HTTPS 及 action 限制。

**獨立測試**:
1. 設定 `template.type=image_carousel`，`columns` 介於 1~10，且每列 action.type=uri；驗證儲存成功。
2. 當出現非 HTTPS URL 或 actions 空白時，返回驗證錯誤。

**驗收場景**:
1. **Given** 3 張圖皆為 HTTPS，**When** 儲存並推播，**Then** LINE API 返回 200 且用戶可點擊開啟。
2. **Given** 任一 URL 非 HTTPS，**When** 儲存，**Then** 後端阻擋並提示「URL 必須為 HTTPS」。

## 需求 (Requirements)

### 功能需求 (Functional Requirements)
- **FR-001**: 後端必須支援 `TEMPLATE` 類型的範本 CRUD，`content` 儲存完整 LINE Template Message JSON (`type=template`, `altText`, `template`).
- **FR-002**: `altText` 必填，長度上限 1500 字元，缺少時不得儲存。
- **FR-003**: Buttons/Confirm/Carousel/Image Carousel 的 `template.type` 必須符合 API 列舉，欄位遵守 LINE 限制：
  - Buttons: `actions` 1~4；`text` 60/160 字限制 (依是否含 image/title)；`thumbnailImageUrl` 必須 HTTPS、寬度<=1024、JPEG/PNG。
  - Confirm: `actions` 必須 2 個；`text` 上限 240 字。
  - Carousel: `columns` 1~10；每 column `actions` 1~3；`text` 上限 120；圖片 `thumbnailImageUrl` 必須 HTTPS、寬度<=1024。
  - Image Carousel: `columns` 1~10；每 column 需 `imageUrl` (HTTPS, JPEG/PNG, <=1024px 寬) 且單一 `action`，類型限定 `uri`。
- **FR-004**: 驗證 `actions`：需符合 LINE action 物件格式；Buttons/Carousel 最大 4/3 actions；Image Carousel 僅允許 `uri` 且必須 HTTPS。
- **FR-005**: 後端驗證通過後，`messageCount` 設為 1；若多訊息封裝 (`messages` array) 則須符合 LINE Push API 1~5 筆限制。
- **FR-006**: 於操作日誌記錄範本名稱、type、校驗結果；返回 AjaxResult 時附上 `templateName` 與 `templateType` 供審計。
- **FR-007**: 前端模板編輯器需提供類型切換 (Buttons/Confirm/Carousel/Image Carousel)、圖片 URL 欄位提示 HTTPS、action 數量限制提示，並於送出前做前端校驗。

### 非功能需求 (Non-Functional Requirements)
- **NFR-001**: 驗證錯誤訊息需為繁體中文，清楚指出欄位與限制 (例："Buttons actions 需介於 1~4 筆")。
- **NFR-002**: 儲存/更新 API 響應時間 < 2 秒；驗證失敗不寫庫。
- **NFR-003**: 需紀錄操作日誌，包含人、物(範本)、時間；錯誤也需記錄原因。

### 關鍵實體 (Key Entities)
- **LineMessageTemplate** (table `line_message_template`):
  - `template_id`, `template_name`, `template_code`, `msg_type='TEMPLATE'`, `content` (完整 Template JSON), `alt_text`, `preview_img`, `category_id`, `status`, `message_count`, `use_count`, `last_used_at`。
  - JSON 需保留 `template` 內層物件欄位 (buttons/confirm/carousel/image_carousel) 以支援推播。

## 成功標準 (Success Criteria)
- **SC-001**: 針對每種 Template 類型提交不合法欄位時，後端返回明確錯誤並拒絕儲存。
- **SC-002**: 產出的 `content` 可直接呼叫 LINE `/v2/bot/message/push` 通過驗證且 HTTP 200。
- **SC-003**: 前端列表可顯示範本預覽 (使用 `preview_img` 或 altText)，並依 `status` 控制啟停。
