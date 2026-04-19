# 功能規格書 (Feature Specification): LINE Imagemap 訊息範本管理

**功能分支 (Feature Branch)**: `feature/line-imagemap-template`
**建立日期 (Created)**: 2025-12-23
**狀態 (Status)**: Draft
**輸入 (Input)**: 用戶描述: "在後台建立 LINE 的圖文訊息 (Imagemap Message)，參考 LINE Messaging API 文件"

## 用戶場景與測試 (User Scenarios & Testing)

### 用戶故事 1 - 建立與編輯 Imagemap 範本 (優先級: P1)

管理員希望能在後台介面透過表單建立 LINE Imagemap 訊息範本，以便在推播或自動回覆時使用。

**優先級理由**: 這是 Imagemap 功能的核心，沒有建立功能則無法使用。

**獨立測試**:
1. 呼叫建立 API，帶入 Imagemap 參數 (BaseUrl, AltText, BaseSize, Actions)。
2. 驗證資料庫 `line_message_template` 表中是否新增一筆 `msg_type='IMAGEMAP'` 的記錄。
3. 驗證 `content` 欄位中的 JSON 是否符合 LINE API 格式。

**驗收場景**:

1. **Given** 管理員上傳了一張圖片並設定了兩個點擊區域 (一個連結，一個文字訊息), **When** 點擊儲存, **Then** 系統應產生對應的圖片資源並儲存範本設定。
2. **Given** 圖片處理失敗, **When** 儲存, **Then** 系統應回傳錯誤訊息且不建立無效的範本。

---

### 用戶故事 2 - Imagemap 圖片自動處理 (優先級: P1)

管理員上傳一張高解析度圖片 (寬度 >= 1040px)，系統自動裁切並縮放成 LINE 要求的規格 (240px, 300px, 460px, 700px, 1040px)。

**優先級理由**: LINE Imagemap 嚴格要求 `baseUrl` 指向的端點必須能回應特定寬度的圖片，人工處理過於繁瑣。

**獨立測試**:
1. 上傳一張寬度 2000px 的圖片。
2. 檢查伺服器儲存路徑下是否產生了 `240`, `300`, `460`, `700`, `1040` 五個檔案 (無副檔名或對應路徑)。

**驗收場景**:

1. **Given** 一張 1040x1040 的圖片, **When** 上傳處理, **Then** 系統產生 5 種尺寸的圖片檔案。
2. **Given** 一張寬度小於 1040 的圖片, **When** 上傳, **Then** 系統應提示錯誤或嘗試放大 (視需求定義，此處建議報錯)。

---

## 需求 (Requirements)

### 功能需求 (Functional Requirements)

- **FR-001**: 系統**必須**提供 API 支援 `IMAGEMAP` 類型的訊息範本 CRUD。
- **FR-002**: 系統**必須**實作圖片處理服務，將上傳的原始圖片轉換為 LINE Imagemap 規格：
    - 格式：JPEG 或 PNG
    - 尺寸：寬度必須支援 240, 300, 460, 700, 1040 像素
    - 檔案結構：`{cheng.profile}/imagemap/{templateId}/{size}` (需配合 Nginx 或靜態資源映射)
- **FR-003**: 系統**必須**驗證 Imagemap 的 `actions` 區域設定：
    - 區域 (area) 不可超出圖片範圍。
    - 支援 `uri` (開啟連結) 和 `message` (發送文字) 兩種類型。
- **FR-004**: 系統**必須**儲存完整的 LINE Message JSON 物件至 `line_message_template.content` 欄位。
- **FR-005**: 圖片儲存路徑應位於 `cheng.profile` 配置的路徑下，並可透過靜態資源存取。

### 關鍵實體 (Key Entities)

- **LineMessageTemplate** (對應表 `line_message_template`):
    - `template_id`: ID
    - `msg_type`: 'IMAGEMAP'
    - `content`: JSON String (儲存完整的 Imagemap Message Object)
    - `preview_img`: 儲存 240px 或 300px 的縮圖路徑供後台列表顯示
    - `alt_text`: 替代文字

## 成功標準 (Success Criteria)

### 可衡量的結果 (Measurable Outcomes)

- **SC-001**: 上傳一張圖片後，系統能在 5 秒內完成所有尺寸的產生。
- **SC-002**: 建立的 Imagemap 範本可以直接被 LINE Messaging API 的 `/push` 或 `/reply` 接口使用而不報錯 (400 Bad Request)。
- **SC-003**: 前端能正確顯示已建立的 Imagemap 預覽圖。
