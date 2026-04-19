# 圖文訊息（Imagemap）重構規格書

> **建立日期**: 2025-12-26  
> **狀態**: 規劃中

---

## 1. 背景與目標

### 1.1 現有架構
- 圖文訊息範本獨立管理於 `/line/imagemap` 頁面
- 訊息範本中使用 IMAGEMAP 類型，透過 `ImagemapEditor.vue` 編輯
- 範本內容儲存於 `line_message_template.content` 欄位（JSON 格式）

### 1.2 異動需求
當圖文訊息範本異動時，需要同步更新所有引用該範本的訊息範本。

### 1.3 目標
1. 建立圖文訊息範本與訊息範本的關聯機制
2. 當圖文範本異動時，自動或提示更新相關訊息範本
3. 確保資料一致性

---

## 2. 影響範圍分析

### 2.1 前端檔案
| 檔案 | 說明 |
|------|------|
| `cheng-ui/src/views/line/imagemap/index.vue` | 圖文訊息範本管理頁面 |
| `cheng-ui/src/views/line/imagemap/components/ImagemapPreview.vue` | 圖文預覽元件 |
| `cheng-ui/src/views/line/template/components/ImagemapEditor.vue` | 訊息範本中的圖文編輯器 |
| `cheng-ui/src/views/line/template/index.vue` | 訊息範本管理頁面 |
| `cheng-ui/src/views/line/template/components/TemplateEditor.vue` | 訊息範本編輯器 |

### 2.2 後端檔案
| 檔案 | 說明 |
|------|------|
| `cheng-line/.../controller/LineMessageTemplateController.java` | 範本 Controller |
| `cheng-line/.../service/impl/LineMessageServiceImpl.java` | 訊息服務 |
| `cheng-line/.../service/impl/LineMessageSendServiceImpl.java` | 發送服務 |
| `cheng-line/.../dto/ImagemapMessageDto.java` | Imagemap DTO |
| `cheng-line/.../util/ImagemapUtils.java` | Imagemap 工具類 |

### 2.3 資料庫
| 表格 | 說明 |
|------|------|
| `line_message_template` | 訊息範本表 |
| `line_imagemap_template`（待確認） | 圖文範本表 |

---

## 3. 解決方案

### 方案 A：關聯表設計（推薦）

#### 3.1 新增關聯表
```sql
CREATE TABLE line_template_imagemap_ref (
    ref_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL COMMENT '訊息範本 ID',
    imagemap_id BIGINT NOT NULL COMMENT '圖文範本 ID',
    message_index INT DEFAULT 0 COMMENT '訊息索引（多訊息時）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_template_imagemap (template_id, imagemap_id, message_index),
    INDEX idx_imagemap_id (imagemap_id)
) COMMENT '訊息範本與圖文範本關聯表';
```

#### 3.2 異動流程
1. **儲存訊息範本時**：解析 content 中的 IMAGEMAP，記錄關聯
2. **圖文範本異動時**：查詢關聯表，找出受影響的訊息範本
3. **提供選項**：
   - 自動更新所有引用的訊息範本
   - 僅提示，由使用者手動更新

### 方案 B：內嵌快照設計

#### 3.3 在訊息範本中儲存圖文範本快照
- 優點：不需要關聯表，資料獨立
- 缺點：圖文異動後需要手動更新每個訊息範本

---

## 4. 實作步驟

### 階段一：資料庫設計
- [ ] 確認現有 `line_imagemap_template` 表結構
- [ ] 設計關聯表 `line_template_imagemap_ref`
- [ ] 建立 Flyway Migration

### 階段二：後端實作
- [ ] 建立關聯表 Domain/Mapper
- [ ] 修改範本儲存邏輯，維護關聯
- [ ] 新增 API：查詢圖文範本被哪些訊息範本引用
- [ ] 新增 API：批次更新引用的訊息範本

### 階段三：前端實作
- [ ] 圖文範本異動時，顯示受影響的訊息範本列表
- [ ] 提供「同步更新」按鈕
- [ ] 訊息範本編輯時，顯示引用的圖文範本來源

### 階段四：測試與驗證
- [ ] 單元測試
- [ ] 整合測試
- [ ] 使用者驗收測試

---

## 5. API 設計

### 5.1 查詢圖文範本引用
```
GET /line/imagemap/{imagemapId}/references
Response: {
  "code": 200,
  "data": [
    {
      "templateId": 1,
      "templateName": "範本A",
      "messageIndex": 0
    }
  ]
}
```

### 5.2 同步更新引用
```
POST /line/imagemap/{imagemapId}/sync-references
Request: {
  "templateIds": [1, 2, 3]  // 可選，預設全部
}
Response: {
  "code": 200,
  "msg": "已更新 3 個訊息範本"
}
```

---

## 6. 注意事項

1. **向下相容**：現有訊息範本不應受到影響
2. **效能考量**：關聯查詢需加索引
3. **交易一致性**：批次更新需在同一交易內完成
4. **日誌記錄**：重要操作需記錄操作日誌

---

## 7. 待討論項目

- [ ] 是否需要版本控制（保留歷史版本）？
- [ ] 圖文範本刪除時，如何處理引用的訊息範本？
- [ ] 是否需要「取消關聯」功能（讓訊息範本獨立於圖文範本）？

---

## 8. 時程預估

| 階段 | 預估時間 |
|------|----------|
| 資料庫設計 | 0.5 天 |
| 後端實作 | 2 天 |
| 前端實作 | 2 天 |
| 測試與驗證 | 1 天 |
| **總計** | **5.5 天** |
