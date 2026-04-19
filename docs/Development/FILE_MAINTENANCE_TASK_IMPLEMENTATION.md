# 檔案維護排程任務實作總結

## 專案背景

系統在更新圖片時會自動建立備份檔案，格式為：
```
isbn_9789864345106.jpg.backup.1765121386408
```

這些備份檔案會持續累積，佔用大量磁碟空間，需要建立自動化的定期清理機制。

---

## 解決方案

### 架構設計

採用 Quartz 排程框架，建立檔案維護任務系統，支援：
- **參數化配置**：可從前端設定保留天數、目標路徑等參數
- **模組化設計**：任務執行器與任務類型提供者分離
- **動態註冊**：Spring 自動掃描並註冊任務類型
- **擴展性**：預留暫存檔案清理等未來功能

---

## 實作內容

### 1. 新增任務分類

**檔案：** `cheng-common/src/main/java/com/cheng/common/enums/TaskCategory.java`

新增 `MAINTENANCE`（維護任務）分類：

```java
/**
 * 維護任務
 */
MAINTENANCE("維護任務", "maintenance"),
```

### 2. 建立任務執行器

**檔案：** `cheng-quartz/src/main/java/com/cheng/quartz/task/FileMaintenanceTask.java`

**核心功能：**
- 解析 JSON 參數（任務類型、保留天數、目標路徑）
- 遞迴掃描目錄，查找備份檔案（`*.backup.*` 格式）
- 根據檔案最後修改時間判斷是否過期
- 刪除過期檔案並記錄統計資訊

**關鍵技術：**
- 使用 NIO.2 API（`java.nio.file.*`）進行檔案操作
- 使用 `DirectoryStream` 進行高效目錄遍歷
- 使用 `FileTime` 準確判斷檔案時間
- 完整的錯誤處理和日誌記錄

**方法簽名：**
```java
public void cleanupFiles(String params)
```

**參數格式：**
```json
{
  "taskType": "BACKUP_FILES",
  "retentionDays": 30,
  "targetPath": "書籍封面"
}
```

### 3. 建立任務類型提供者

**檔案：** `cheng-quartz/src/main/java/com/cheng/quartz/provider/MaintenanceTaskTypeProvider.java`

**功能：**
- 實作 `TaskTypeProvider` 介面
- 定義任務參數元資料（名稱、類型、必填、預設值、說明）
- 建立任務選項（代碼、標籤、描述、Bean 名稱、方法名稱）
- 支援隱藏參數（固定值）和可見參數（使用者設定）

**提供的任務類型：**
1. **清理備份檔案**（啟用）
   - 代碼：`CLEANUP_BACKUP_FILES`
   - 參數：保留天數（預設 30 天）、目標路徑（可選）

2. **清理暫存檔案**（預留，未啟用）
   - 代碼：`CLEANUP_TEMP_FILES`
   - 參數：保留天數（預設 7 天）、目標路徑（可選）

---

## 使用方式

### 前端配置

1. 登入系統，進入「系統管理 > 定時任務」
2. 點擊「新增」按鈕
3. 填寫任務資訊：
   - **任務名稱**：清理圖片備份檔案
   - **任務分類**：維護任務
   - **任務類型**：[MAINTENANCE] 檔案維護 - 清理備份檔案
   - **Cron 表達式**：`0 0 2 * * ?`（每天凌晨 2 點執行）

4. 設定參數：
   - **保留天數**：30（保留最近 30 天的備份）
   - **目標路徑**：`書籍封面`（或留空掃描整個上傳目錄）

5. 儲存並啟動任務

### 手動執行測試

在任務列表中，點擊「執行一次」按鈕，立即執行任務並查看日誌。

---

## 執行效果

### 日誌輸出範例

```
INFO  - 開始執行檔案清理任務，參數: {"taskType":"BACKUP_FILES","retentionDays":30,"targetPath":"書籍封面"}
INFO  - 開始清理備份檔案，路徑: /Users/cheng/uploadPath/書籍封面, 保留天數: 30
INFO  - 備份檔案清理完成 - 共刪除 15 個檔案，釋放空間: 2.34 MB
INFO  - 已刪除的檔案範例（最多顯示 5 個）：
INFO    - isbn_9789864345106.jpg.backup.1765121386408
INFO    - isbn_9789863479471.jpg.backup.1765120123456
INFO    - isbn_9787302123456.jpg.backup.1765119876543
```

### 統計資訊

- **刪除檔案數量**：顯示實際刪除的檔案數量
- **釋放空間大小**：以 MB 為單位顯示釋放的磁碟空間
- **檔案範例**：顯示前 5 個被刪除的檔案名稱

---

## 技術特點

### 1. NIO.2 API 應用

**優點：**
- 更好的錯誤處理（明確的異常類型）
- 更高的效能（特別是目錄遍歷）
- 更豐富的功能（符號連結、檔案屬性等）

**範例：**
```java
// 高效目錄遍歷
try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
    for (Path entry : stream) {
        // 處理檔案
    }
}

// 準確的時間判斷
FileTime lastModified = Files.getLastModifiedTime(file);
Instant cutoffTime = Instant.now().minus(Duration.ofDays(retentionDays));
```

### 2. 參數化設計

使用 JSON 格式傳遞參數，支援：
- **隱藏參數**：固定值，不顯示在前端（如 `taskType`）
- **必填參數**：前端必須填寫（如 `retentionDays`）
- **可選參數**：前端可不填寫（如 `targetPath`）

### 3. 完整的錯誤處理

```java
public void cleanupFiles(String params) {
    try {
        // 主要邏輯
        
    } catch (Exception e) {
        // 捕獲所有異常，不拋出
        log.error("任務執行失敗", e);
    }
}

// 批次處理時的容錯
for (Path file : backupFiles) {
    try {
        Files.delete(file);
    } catch (IOException e) {
        // 單個檔案失敗不影響其他檔案
        log.warn("無法刪除檔案: {}", file, e);
    }
}
```

### 4. 詳細的日誌記錄

**記錄內容：**
- 任務開始與結束
- 處理進度與統計
- 成功與失敗的檔案
- 錯誤與警告資訊

**日誌級別：**
- `INFO` - 正常流程
- `WARN` - 警告（如檔案不存在、刪除失敗）
- `ERROR` - 錯誤（如任務執行失敗）
- `DEBUG` - 除錯資訊（如單個檔案刪除）

---

## 文件與記憶

### 建立的文件

1. **排程任務開發完整指南**
   - 路徑：`docs/Development/SCHEDULED_TASK_GUIDE.md`
   - 內容：完整的開發流程、規範、範例、常見問題

2. **排程任務開發規範**
   - 路徑：`docs/Development/SCHEDULED_TASK_RULES.md`
   - 內容：必須遵守的規範、禁止事項、檢查清單、快速模板

3. **實作總結**（本文件）
   - 路徑：`docs/Development/FILE_MAINTENANCE_TASK_IMPLEMENTATION.md`
   - 內容：背景、解決方案、實作內容、使用方式

4. **更新 README**
   - 路徑：`docs/README.md`
   - 新增：Development 分類和快速導航連結

### 建立的記憶

**標題：** 排程任務開發標準流程與規範

**標籤：** `scheduled_task`, `quartz`, `development`, `maintenance`, `file_cleanup`

**內容：**
- 核心架構（任務分類、提供者介面、執行器）
- 三步驟開發流程（執行器 → 提供者 → 測試）
- 必須遵守的規範與禁止事項
- 實際範例（檔案維護任務）
- 常見 Cron 表達式
- NIO.2 檔案操作規範
- 參考文件連結

---

## 未來擴展

### 計劃中的維護任務

1. **暫存檔案清理**
   - 清理匯出任務產生的暫存 ZIP 檔案
   - 清理長時間未存取的暫存資料

2. **日誌歸檔**
   - 壓縮舊日誌檔案
   - 刪除過期日誌
   - 歸檔到備份目錄

3. **資料庫維護**
   - 清理過期的任務執行記錄
   - 清理已刪除物品的關聯資料
   - 優化資料表索引

4. **圖片最佳化**
   - 壓縮大尺寸圖片
   - 轉換圖片格式（如 WebP）
   - 產生縮圖

### 擴展方式

只需在 `FileMaintenanceTask` 中新增對應的方法，並在 `MaintenanceTaskTypeProvider` 中註冊即可，無需修改其他程式碼。

**範例：**
```java
// 1. 在 FileMaintenanceTask 中新增方法
private void cleanupTempFiles(Path basePath, int retentionDays) {
    // 實作邏輯
}

// 2. 在 MaintenanceTaskTypeProvider 中註冊
private TaskTypeOption buildTempFilesCleanup() {
    // 定義參數和任務選項
}
```

---

## 測試檢查清單

開發完成後，確認以下項目：

### 功能測試
- [x] 任務在前端「定時任務」頁面中正常顯示
- [x] 任務分類為「維護任務」
- [x] 任務類型為「清理備份檔案」
- [x] 參數可在前端正確配置
- [x] 點擊「執行一次」能夠立即執行
- [x] 任務執行日誌完整且清晰

### 程式碼檢查
- [x] 使用 `@Component` 和 `@Slf4j` 註解
- [x] 方法簽名為 `public void methodName(String params)`
- [x] 注入 `ObjectMapper` 解析 JSON
- [x] 使用 NIO.2 API 進行檔案操作
- [x] 完整的異常處理（不拋出異常）
- [x] 詳細的日誌記錄

### 文件檢查
- [x] JavaDoc 註解完整
- [x] 參數說明清楚
- [x] 建立開發指南文件
- [x] 建立開發規範文件
- [x] 更新 docs/README.md
- [x] 建立記憶

---

## 檔案清單

### 新增檔案

| 檔案 | 說明 |
|------|------|
| `TaskCategory.java` | 新增 MAINTENANCE 分類 |
| `FileMaintenanceTask.java` | 檔案維護任務執行器 |
| `MaintenanceTaskTypeProvider.java` | 維護任務類型提供者 |
| `SCHEDULED_TASK_GUIDE.md` | 排程任務開發完整指南 |
| `SCHEDULED_TASK_RULES.md` | 排程任務開發規範 |
| `FILE_MAINTENANCE_TASK_IMPLEMENTATION.md` | 實作總結（本文件）|

### 修改檔案

| 檔案 | 修改內容 |
|------|----------|
| `docs/README.md` | 新增 Development 分類和快速導航 |

---

## 總結

本次實作成功建立了完整的檔案維護排程任務系統，具備以下特點：

✅ **完整性** - 從任務執行器到前端配置的完整實作  
✅ **可擴展** - 預留未來擴展的介面和結構  
✅ **文件化** - 完整的開發指南和規範文件  
✅ **記憶化** - 建立記憶確保未來開發一致性  
✅ **最佳實踐** - 遵循 NIO.2、錯誤處理、日誌記錄等最佳實踐  

系統現在可以自動清理過期的備份檔案，釋放磁碟空間，並為未來的維護任務提供了標準化的開發模板。

---

**建立日期：** 2025-12-07  
**作者：** Cheng  
**版本：** 1.0
