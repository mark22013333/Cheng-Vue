---
description: "LINE Imagemap 實作任務列表"
---

# 任務列表 (Tasks): LINE Imagemap 訊息範本

**輸入 (Input)**: `/specs/001-line-https-developers/spec.md`
**先決條件**: `V30__add_line_template_tables.sql` (已存在)

## Phase 1: 基礎建設與圖片處理 (Foundational)

- [x] T001 [P] 檢查並引入圖片處理依賴 (如 Thumbnails) 於 `cheng-common/pom.xml`
- [x] T002 [P] 實作圖片處理工具類 `ImagemapUtils`，支援將圖片裁切縮放為 LINE 指定的 5 種寬度 (240, 300, 460, 700, 1040)
- [x] T003 [P] 定義 Imagemap 相關 DTO 與 Enum (`ImagemapActionType`, `ImagemapMessageDto`)

## Phase 2: 用戶故事 2 - Imagemap 圖片處理 (Priority: P1)

**目標**: 上傳並產生符合 LINE 規格的圖片資源

- [x] T004 實作圖片上傳 API (`/line/template/upload/imagemap`)，接收圖片並呼叫 `ImagemapUtils` 處理
- [x] T005 將處理後的圖片儲存至 `{cheng.profile}/imagemap/{tempId}/` 目錄
- [x] T006 返回預覽圖 URL 與圖片 Base URL 給前端

## Phase 3: 用戶故事 1 - 建立與編輯 Imagemap 範本 (Priority: P1)

**目標**: 儲存完整的 Imagemap 訊息設定

- [x] T007 [P] 修改 `LineTemplateController` 新增/更新接口，支援 `IMAGEMAP` 類型校驗
- [x] T008 [P] 實作 `LineTemplateService` 邏輯，組裝標準 LINE Imagemap JSON 物件
- [x] T009 [P] 加上 `@Log` 註解記錄操作日誌 (需包含 template_name, type)
- [ ] T010 [前端] 實作 `ImagemapEditor.vue` 組件，支援圖片上傳與熱區 (Hotspot) 繪製
- [ ] T011 [前端] 整合至 `line/template/index` 頁面

## Phase 4: 測試與潤飾

- [ ] T012 測試圖片產生的品質與檔案大小
- [ ] T013 驗證產生的 JSON 是否可通過 LINE API 傳送
- [ ] T014 檢查 Nginx/Tomcat 靜態資源映射是否包含 imagemap 目錄
