# 排程任務開發指南

## 概述

本系統使用 Quartz 框架實現排程任務管理，採用模組化設計，支援動態註冊和參數化配置。

**核心設計理念：**
- **模組解耦**：各模組獨立定義自己的任務類型
- **動態註冊**：Spring 自動掃描並註冊所有任務提供者
- **參數化配置**：支援從前端介面設定任務參數
- **擴展性強**：新增任務只需實作介面，無需修改排程模組

---

## 核心架構

### 1. 任務分類（TaskCategory）

位置：`cheng-common/src/main/java/com/cheng/common/enums/TaskCategory.java`

**現有分類：**
- `CRAWLER` - 爬蟲任務
- `NOTIFICATION` - 推播訊息任務
- `REPORT` - 報表任務
- `SYNC` - 資料同步任務
- `BACKUP` - 備份任務
- `RESERVATION` - 預約任務
- `MAINTENANCE` - 維護任務 ⭐ **NEW**
- `CUSTOM` - 自訂任務

### 2. 任務類型提供者（TaskTypeProvider）

位置：`cheng-common/src/main/java/com/cheng/common/core/provider/TaskTypeProvider.java`

**介面定義：**
```java
public interface TaskTypeProvider {
    // 取得任務分類
    TaskCategory getCategory();
    
    // 取得此分類下所有可用的任務類型
    List<TaskTypeOption> getTaskTypes();
    
    // 優先級（數字越小優先級越高）
    default int getPriority() { return 100; }
    
    // 是否啟用
    default boolean isEnabled() { return true; }
}
```

### 3. 任務執行器（Task）

位置：`cheng-quartz/src/main/java/com/cheng/quartz/task/`

**命名規範：**
- 類別名稱：`XxxTask`（例如：`FileMaintenanceTask`）
- Bean 名稱：使用 `@Component("xxxTask")` 註解
- 方法簽名：`public void methodName(String params)`

---

## 開發新排程任務（完整流程）

### Step 1：新增任務分類（如需要）

**檔案：** `TaskCategory.java`

```java
/**
 * 維護任務
 */
MAINTENANCE("維護任務", "maintenance"),
```

### Step 2：建立任務執行器

**檔案：** `cheng-quartz/src/main/java/com/cheng/quartz/task/FileMaintenanceTask.java`

```java
package com.cheng.quartz.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 檔案維護定時任務
 * 
 * <p><b>Quartz 設定範例：</b>
 * <pre>
 * Bean名稱: fileMaintenanceTask
 * 方法名稱: cleanupFiles
 * 參數: {"taskType":"BACKUP_FILES","retentionDays":30}
 * </pre>
 */
@Slf4j
@Component("fileMaintenanceTask")
@RequiredArgsConstructor
public class FileMaintenanceTask {

    private final ObjectMapper objectMapper;

    @Value("${cheng.profile}")
    private String uploadPath;

    /**
     * 清理檔案
     * 
     * @param params JSON 參數
     */
    public void cleanupFiles(String params) {
        try {
            log.info("開始執行檔案清理任務，參數: {}", params);
            
            // 解析參數
            JsonNode jsonParams = objectMapper.readTree(params);
            String taskType = jsonParams.get("taskType").asText();
            int retentionDays = jsonParams.get("retentionDays").asInt();
            
            // 執行清理邏輯
            // ...
            
            log.info("檔案清理任務完成");
        } catch (Exception e) {
            log.error("執行檔案清理任務失敗", e);
        }
    }
}
```

**關鍵點：**
1. ✅ 使用 `@Component("beanName")` 註解
2. ✅ 注入 `ObjectMapper` 用於解析 JSON 參數
3. ✅ 方法簽名必須是 `public void methodName(String params)`
4. ✅ 完整的錯誤處理和日誌記錄
5. ✅ 使用 `@Value` 注入配置項（如需要）

### Step 3：建立任務類型提供者

**檔案：** `cheng-quartz/src/main/java/com/cheng/quartz/provider/MaintenanceTaskTypeProvider.java`

```java
package com.cheng.quartz.provider;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.common.enums.TaskParamType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class MaintenanceTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.MAINTENANCE;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        return Arrays.asList(
                buildBackupFilesCleanup()
        );
    }

    @Override
    public int getPriority() {
        return 50; // 優先級：數字越小越高
    }

    private TaskTypeOption buildBackupFilesCleanup() {
        // 1. 定義隱藏參數（固定值）
        TaskParamMetadata taskTypeParam = TaskParamMetadata.builder()
                .name("taskType")
                .label("任務類型")
                .type(TaskParamType.STRING)
                .required(true)
                .defaultValue("BACKUP_FILES")
                .order(0)
                .visible(false) // 隱藏不顯示
                .build();

        // 2. 定義可見參數
        TaskParamMetadata retentionDaysParam = TaskParamMetadata.builder()
                .name("retentionDays")
                .label("保留天數")
                .type(TaskParamType.INTEGER)
                .required(true)
                .defaultValue("30")
                .description("保留最近 N 天的備份檔案")
                .order(1)
                .visible(true)
                .build();

        // 3. 建立任務選項
        return TaskTypeOption.builder()
                .code("CLEANUP_BACKUP_FILES")
                .label("[MAINTENANCE] 檔案維護 - 清理備份檔案")
                .category(TaskCategory.MAINTENANCE.getCode())
                .description("清理超過保留期限的圖片備份檔案")
                .beanName("fileMaintenanceTask")
                .defaultMethod("cleanupFiles")
                .params(Arrays.asList(taskTypeParam, retentionDaysParam))
                .enabled(true)
                .build();
    }
}
```

**參數類型（TaskParamType）：**
- `STRING` - 字串
- `INTEGER` - 整數
- `LONG` - 長整數
- `DOUBLE` - 浮點數
- `BOOLEAN` - 布林值
- `SELECT` - 下拉選單（需配合 options 使用）

**關鍵配置：**
- `visible(false)` - 隱藏參數，不顯示在前端
- `required(true)` - 必填參數
- `order(n)` - 參數排序
- `enabled(true)` - 啟用此任務類型

### Step 4：測試與驗證

#### 4.1 啟動應用

```bash
# 啟動後端
cd cheng-admin
mvn spring-boot:run
```

#### 4.2 檢查任務是否註冊成功

查看日誌：
```
INFO  - 已註冊任務類型提供者: MaintenanceTaskTypeProvider
INFO  - 任務分類 [維護任務] 提供 1 個任務類型
```

#### 4.3 前端測試

1. 進入「系統管理 > 定時任務」
2. 點擊「新增」
3. 在「任務分類」下拉選單中選擇「維護任務」
4. 在「任務類型」下拉選單中選擇「清理備份檔案」
5. 填寫參數（如：保留天數 = 30）
6. 設定 Cron 表達式（如：`0 0 2 * * ?` 每天凌晨 2 點執行）
7. 儲存並啟動任務

#### 4.4 手動執行測試

在任務列表中，點擊「執行一次」按鈕，立即執行任務。

---

## 實際範例：檔案維護任務

### 使用場景

系統在更新圖片時會自動建立備份檔案，格式為：
```
isbn_9789864345106.jpg.backup.1765121386408
```

這些備份檔案會佔用大量磁碟空間，需要定期清理。

### 任務配置

**任務名稱：** 清理圖片備份檔案  
**任務分類：** 維護任務  
**任務類型：** 清理備份檔案  
**Cron 表達式：** `0 0 2 * * ?`（每天凌晨 2 點執行）  
**參數設定：**
```json
{
  "taskType": "BACKUP_FILES",
  "retentionDays": 30,
  "targetPath": "書籍封面"
}
```

### 執行邏輯

1. 掃描 `${cheng.profile}/書籍封面/` 目錄
2. 查找所有 `.backup.*` 格式的檔案
3. 檢查檔案最後修改時間
4. 刪除超過 30 天的備份檔案
5. 記錄刪除的檔案數量和釋放的空間

### 執行結果

```
INFO  - 開始執行檔案清理任務，參數: {"taskType":"BACKUP_FILES","retentionDays":30,"targetPath":"書籍封面"}
INFO  - 開始清理備份檔案，路徑: /Users/cheng/uploadPath/書籍封面, 保留天數: 30
INFO  - 備份檔案清理完成 - 共刪除 15 個檔案，釋放空間: 2.34 MB
INFO  - 已刪除的檔案範例（最多顯示 5 個）：
INFO    - isbn_9789864345106.jpg.backup.1765121386408
INFO    - isbn_9789863479471.jpg.backup.1765120123456
...
```

---

## 常見問題與最佳實踐

### 1. 參數解析

**✅ 推薦做法：**
```java
JsonNode jsonParams = objectMapper.readTree(params);
String value = jsonParams.has("key") ? jsonParams.get("key").asText() : "defaultValue";
```

**❌ 避免：**
```java
// 不檢查參數是否存在，容易拋出 NPE
String value = jsonParams.get("key").asText();
```

### 2. 錯誤處理

**✅ 推薦做法：**
```java
public void myTask(String params) {
    try {
        // 主要邏輯
        log.info("任務開始執行");
        
        // ... 業務邏輯 ...
        
        log.info("任務執行成功");
    } catch (Exception e) {
        log.error("任務執行失敗", e);
        // 不要拋出異常，讓 Quartz 繼續下次執行
    }
}
```

### 3. 檔案操作

**✅ 使用 NIO.2 API：**
```java
import java.nio.file.*;

// 刪除檔案
Files.deleteIfExists(path);

// 遍歷目錄
try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
    for (Path entry : stream) {
        // ...
    }
}

// 取得檔案屬性
FileTime lastModified = Files.getLastModifiedTime(file);
```

**❌ 避免使用舊的 File API：**
```java
// 不推薦
File file = new File(path);
file.delete();
```

### 4. 日誌記錄

**關鍵節點必須記錄：**
- ✅ 任務開始和結束
- ✅ 處理的資料數量
- ✅ 執行結果摘要
- ✅ 錯誤和警告

**範例：**
```java
log.info("開始執行任務，參數: {}", params);
// ...
log.info("任務完成 - 處理 {} 個檔案，成功 {}，失敗 {}", total, success, failed);
```

### 5. 效能考量

**處理大量資料時：**
- ✅ 使用批次處理
- ✅ 記錄進度
- ✅ 設定合理的超時時間
- ✅ 避免一次載入所有資料到記憶體

---

## Cron 表達式參考

| 表達式 | 說明 |
|--------|------|
| `0 0 2 * * ?` | 每天凌晨 2 點執行 |
| `0 */30 * * * ?` | 每 30 分鐘執行一次 |
| `0 0 */2 * * ?` | 每 2 小時執行一次 |
| `0 0 0 1 * ?` | 每月 1 號凌晨執行 |
| `0 0 0 ? * MON` | 每週一凌晨執行 |
| `0 0 1 * * ?` | 每天凌晨 1 點執行 |

**格式說明：**
```
秒 分 時 日 月 週 [年]
*  *  *  *  *  ?  [*]
```

---

## 檔案清單

### 核心檔案

| 檔案 | 說明 |
|------|------|
| `TaskCategory.java` | 任務分類列舉 |
| `TaskTypeProvider.java` | 任務類型提供者介面 |
| `TaskParamMetadata.java` | 任務參數元資料 |
| `TaskTypeOption.java` | 任務類型選項 |

### 維護任務範例

| 檔案 | 說明 |
|------|------|
| `FileMaintenanceTask.java` | 檔案維護任務執行器 |
| `MaintenanceTaskTypeProvider.java` | 維護任務類型提供者 |

### 其他範例

| 檔案 | 說明 |
|------|------|
| `CrawlerTask.java` | 爬蟲任務執行器 |
| `CrawlerTypeProvider.java` | 爬蟲任務類型提供者 |
| `ReservationTask.java` | 預約任務執行器 |
| `ReservationTaskTypeProvider.java` | 預約任務類型提供者 |

---

## 未來擴展

### 計劃中的維護任務

1. **暫存檔案清理**
   - 清理 `/tmp` 目錄下的匯出檔案
   - 清理長時間未存取的暫存資料

2. **日誌歸檔**
   - 壓縮舊日誌檔案
   - 刪除過期日誌

3. **資料庫維護**
   - 清理過期的任務執行記錄
   - 清理已刪除物品的關聯資料

---

## 參考資料

- [Quartz 官方文件](http://www.quartz-scheduler.org/documentation/)
- [Spring Task Scheduling](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)
- [Java NIO.2 File API](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html)

---

**最後更新：** 2025-12-07  
**作者：** Cheng  
**版本：** 1.0
