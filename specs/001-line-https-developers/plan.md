# 實作計畫 (Implementation Plan): LINE Imagemap 訊息範本

**分支 (Branch)**: `feature/line-imagemap-template` | **日期 (Date)**: 2025-12-23
**輸入 (Input)**: 來自 `/specs/001-line-https-developers/spec.md` 的功能規格

## 摘要 (Summary)

實作 LINE Imagemap 訊息範本的後端管理功能，包含 CRUD API、圖片自動裁切縮放處理 (產生 240/300/460/700/1040 尺寸)，並將完整的 Imagemap JSON 儲存於 `line_message_template` 表中。

## 技術上下文 (Technical Context)

**語言/版本**: Java 17, JavaScript (Vue 3.5.24)
**主要依賴 (Backend)**: 
- Spring Boot 3.5.4
- Thumbnails (或 Java ImageIO) 用於圖片處理
- MyBatis (資料存取)
- Jackson (JSON 處理)
**主要依賴 (Frontend)**: 
- Vue 3 + Composition API
- Element Plus 3+
- HTML5 Canvas (預覽與區域繪製)
**資料庫**: MySQL (使用現有 `line_message_template` 表)
**儲存**: 本機檔案系統 (透過 `cheng.profile` 配置路徑)

## 憲法檢查 (Constitution Check)

- [x] **語言**: 輸出為繁體中文
- [x] **技術棧**: 使用 Spring Boot + Vue 3
- [x] **Enum**: 需定義 ImagemapActionType Enum
- [x] **日誌**: 需使用 `@Log` 記錄範本操作
- [x] **圖片**: 需使用相對路徑並存於 `cheng.profile` 下

## 專案結構 (Project Structure)

### 文件
```text
specs/001-line-https-developers/
├── plan.md              # 本檔案
├── spec.md              # 規格書
└── tasks.md             # 任務列表
```

### 原始碼

```text
# 後端
cheng-admin/src/main/java/com/cheng/web/controller/line/LineTemplateController.java (修改)
cheng-system/src/main/java/com/cheng/line/service/LineTemplateService.java (修改)
cheng-system/src/main/java/com/cheng/line/service/impl/LineTemplateServiceImpl.java (修改)
cheng-common/src/main/java/com/cheng/common/utils/ImageUtils.java (新增/修改)

# 前端
cheng-ui/src/views/line/template/components/ImagemapEditor.vue (新增)
cheng-ui/src/api/line/template.js (修改)
```
