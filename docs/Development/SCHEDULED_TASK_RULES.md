# 排程任務開發規範

## 適用範圍

當需要開發新的排程任務（定時任務）時，必須遵循以下規範。

---

## 開發流程

### 1. 確定任務分類

**現有分類：**
- `CRAWLER` - 爬蟲任務
- `NOTIFICATION` - 推播訊息任務
- `REPORT` - 報表任務
- `SYNC` - 資料同步任務
- `BACKUP` - 備份任務
- `RESERVATION` - 預約任務
- `MAINTENANCE` - 維護任務
- `CUSTOM` - 自訂任務

**如需新增分類：**
編輯 `cheng-common/src/main/java/com/cheng/common/enums/TaskCategory.java`

---

## 必須遵守的規範

### 1. 檔案命名規範

| 檔案類型 | 命名規則 | 範例 |
|----------|----------|------|
| 任務執行器 | `XxxTask.java` | `FileMaintenanceTask.java` |
| 任務提供者 | `XxxTaskTypeProvider.java` | `MaintenanceTaskTypeProvider.java` |

### 2. 任務執行器規範

**位置：** `cheng-quartz/src/main/java/com/cheng/quartz/task/`

**必須包含：**
```java
@Slf4j
@Component("xxxTask")  // ✅ 必須使用 @Component 並指定 beanName
@RequiredArgsConstructor
public class XxxTask {

    // ✅ 注入 ObjectMapper 用於解析 JSON 參數
    private final ObjectMapper objectMapper;

    /**
     * ✅ 方法簽名必須是：public void methodName(String params)
     * ✅ 完整的 JavaDoc 註解，包含參數說明
     */
    public void taskMethod(String params) {
        try {
            // ✅ 記錄開始日誌
            log.info("開始執行任務，參數: {}", params);
            
            // ✅ 解析參數
            JsonNode jsonParams = objectMapper.readTree(params);
            
            // 執行任務邏輯
            // ...
            
            // ✅ 記錄完成日誌（含統計資訊）
            log.info("任務完成 - 處理數量: {}", count);
            
        } catch (Exception e) {
            // ✅ 捕獲異常，不拋出（讓 Quartz 繼續執行）
            log.error("任務執行失敗", e);
        }
    }
}
```

**❌ 禁止事項：**
- ❌ 不得省略 `@Component` 註解
- ❌ 不得拋出未捕獲的異常
- ❌ 不得使用 `System.out.println()`，必須使用 `log`
- ❌ 不得使用傳統的 `File` API，必須使用 NIO.2

### 3. 任務提供者規範

**位置：** `cheng-quartz/src/main/java/com/cheng/quartz/provider/`

**必須包含：**
```java
@Slf4j
@Component  // ✅ 必須使用 @Component（不需指定名稱）
public class XxxTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        // ✅ 返回任務分類
        return TaskCategory.XXX;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        // ✅ 返回任務類型列表
        return Arrays.asList(
            buildTaskOption1(),
            buildTaskOption2()
        );
    }

    @Override
    public int getPriority() {
        // ✅ 設定優先級（數字越小越高）
        return 50;
    }

    private TaskTypeOption buildTaskOption1() {
        // ✅ 參數定義必須完整
        TaskParamMetadata param = TaskParamMetadata.builder()
            .name("paramName")          // ✅ 參數名稱（JSON key）
            .label("參數顯示名稱")       // ✅ 前端顯示的標籤
            .type(TaskParamType.STRING) // ✅ 參數類型
            .required(true)             // ✅ 是否必填
            .defaultValue("default")    // ✅ 預設值
            .description("參數說明")     // ✅ 詳細說明
            .order(1)                   // ✅ 排序
            .visible(true)              // ✅ 是否顯示在前端
            .build();

        // ✅ 任務選項必須完整
        return TaskTypeOption.builder()
            .code("TASK_CODE")                      // ✅ 任務代碼（唯一）
            .label("[分類] 模組 - 功能名稱")         // ✅ 顯示標籤
            .category(TaskCategory.XXX.getCode())   // ✅ 分類代碼
            .description("任務詳細說明")             // ✅ 詳細說明
            .beanName("xxxTask")                    // ✅ Bean 名稱
            .defaultMethod("taskMethod")            // ✅ 預設方法名稱
            .params(Arrays.asList(param))           // ✅ 參數列表
            .enabled(true)                          // ✅ 是否啟用
            .build();
    }
}
```

### 4. 參數定義規範

**參數類型（TaskParamType）：**
- `STRING` - 字串
- `NUMBER` - 整數
- `LONG` - 長整數
- `DOUBLE` - 浮點數
- `BOOLEAN` - 布林值
- `SELECT` - 下拉選單

**隱藏參數規範：**
```java
// ✅ 固定值參數應設為隱藏
TaskParamMetadata hiddenParam = TaskParamMetadata.builder()
    .name("taskType")
    .visible(false)  // 隱藏不顯示
    .required(true)
    .defaultValue("FIXED_VALUE")
    .order(0)  // 隱藏參數應排在最前面
    .build();
```

### 5. 日誌記錄規範

**必須記錄的資訊：**
```java
// ✅ 任務開始
log.info("開始執行任務，參數: {}", params);

// ✅ 重要步驟
log.info("開始處理資料，共 {} 筆", total);

// ✅ 進度資訊（處理大量資料時）
log.info("已處理 {}/{} 筆", processed, total);

// ✅ 任務完成（含統計）
log.info("任務完成 - 總計: {}, 成功: {}, 失敗: {}", total, success, failed);

// ✅ 錯誤資訊
log.error("處理資料失敗 - ID: {}", id, exception);

// ⚠️ 警告資訊
log.warn("資料異常，已跳過 - ID: {}", id);
```

**❌ 禁止：**
```java
// ❌ 不得使用 System.out.println
System.out.println("任務開始");

// ❌ 不得省略關鍵日誌
// ... 執行任務 ...
// (沒有任何日誌)
```

### 6. 檔案操作規範

**✅ 必須使用 NIO.2 API：**
```java
import java.nio.file.*;

// ✅ 檔案刪除
Files.deleteIfExists(path);

// ✅ 目錄遍歷
try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
    for (Path entry : stream) {
        // ...
    }
}

// ✅ 檔案屬性
FileTime lastModified = Files.getLastModifiedTime(file);
BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
```

**❌ 禁止使用傳統 File API：**
```java
// ❌ 不推薦
File file = new File(path);
file.delete();
File[] files = dir.listFiles();
```

### 7. 錯誤處理規範

**✅ 正確做法：**
```java
public void taskMethod(String params) {
    try {
        // 主要邏輯
        
    } catch (Exception e) {
        // ✅ 捕獲所有異常
        log.error("任務執行失敗", e);
        // ✅ 不拋出異常，讓 Quartz 繼續執行
    }
}

// 批次處理時的錯誤處理
for (Item item : items) {
    try {
        // 處理單個項目
        processItem(item);
    } catch (Exception e) {
        // ✅ 單個項目失敗不影響其他項目
        log.error("處理項目失敗 - ID: {}", item.getId(), e);
        failedCount++;
    }
}
```

**❌ 錯誤做法：**
```java
// ❌ 不捕獲異常，會導致任務中斷
public void taskMethod(String params) throws Exception {
    // ...
}

// ❌ 批次處理時沒有錯誤處理
for (Item item : items) {
    processItem(item);  // 一個失敗就全部中斷
}
```

---

## 測試檢查清單

開發完成後，必須通過以下檢查：

### 功能測試
- [ ] 任務能夠在前端「定時任務」頁面中正常顯示
- [ ] 參數能夠在前端正確配置
- [ ] 點擊「執行一次」能夠立即執行
- [ ] 設定 Cron 表達式後能夠按時執行
- [ ] 任務執行日誌完整且清晰

### 程式碼檢查
- [ ] 使用 `@Component` 註解
- [ ] 使用 `@Slf4j` 進行日誌記錄
- [ ] 方法簽名為 `public void methodName(String params)`
- [ ] 完整的異常處理
- [ ] 使用 NIO.2 API 進行檔案操作
- [ ] 參數使用 `ObjectMapper` 解析 JSON

### 文件檢查
- [ ] JavaDoc 註解完整
- [ ] 參數說明清楚
- [ ] 在 `SCHEDULED_TASK_GUIDE.md` 中記錄範例

---

## 常見錯誤

### 1. 任務不顯示在前端

**原因：**
- Provider 類別沒有 `@Component` 註解
- Provider 沒有實作 `TaskTypeProvider` 介面
- `getCategory()` 返回的分類不存在

**解決：**
```java
@Component  // ✅ 必須加上
public class XxxTaskTypeProvider implements TaskTypeProvider {
    // ...
}
```

### 2. 任務執行報錯

**原因：**
- Bean 名稱不匹配
- 方法名稱不匹配
- 方法簽名不正確

**解決：**
```java
@Component("xxxTask")  // ✅ beanName 要一致
public class XxxTask {
    
    // ✅ 必須是這個簽名
    public void methodName(String params) {
        // ...
    }
}
```

### 3. 參數解析失敗

**原因：**
- 沒有注入 `ObjectMapper`
- 參數格式不是有效的 JSON
- 沒有檢查參數是否存在

**解決：**
```java
@RequiredArgsConstructor
public class XxxTask {
    private final ObjectMapper objectMapper;  // ✅ 注入
    
    public void taskMethod(String params) {
        JsonNode json = objectMapper.readTree(params);
        // ✅ 檢查參數是否存在
        String value = json.has("key") ? json.get("key").asText() : "default";
    }
}
```

---

## 參考範例

### 完整範例：檔案維護任務

**檔案：**
- `FileMaintenanceTask.java`
- `MaintenanceTaskTypeProvider.java`

**位置：**
- `cheng-quartz/src/main/java/com/cheng/quartz/task/`
- `cheng-quartz/src/main/java/com/cheng/quartz/provider/`

**功能：**
- 清理圖片備份檔案（`*.backup.*` 格式）
- 可設定保留天數
- 可指定目標路徑

**文件：**
- 詳細說明參見 `docs/Development/SCHEDULED_TASK_GUIDE.md`

---

## 快速開始模板

### 任務執行器模板

```java
package com.cheng.quartz.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("myTask")
@RequiredArgsConstructor
public class MyTask {

    private final ObjectMapper objectMapper;

    public void execute(String params) {
        try {
            log.info("開始執行任務，參數: {}", params);
            
            JsonNode json = objectMapper.readTree(params);
            // 解析參數並執行任務
            
            log.info("任務執行完成");
        } catch (Exception e) {
            log.error("任務執行失敗", e);
        }
    }
}
```

### 任務提供者模板

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
public class MyTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.CUSTOM;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        return Arrays.asList(buildMyTask());
    }

    @Override
    public int getPriority() {
        return 100;
    }

    private TaskTypeOption buildMyTask() {
        TaskParamMetadata param = TaskParamMetadata.builder()
                .name("paramName")
                .label("參數名稱")
                .type(TaskParamType.STRING)
                .required(true)
                .defaultValue("")
                .description("參數說明")
                .order(1)
                .visible(true)
                .build();

        return TaskTypeOption.builder()
                .code("MY_TASK")
                .label("[分類] 我的任務")
                .category(TaskCategory.CUSTOM.getCode())
                .description("任務說明")
                .beanName("myTask")
                .defaultMethod("execute")
                .params(Arrays.asList(param))
                .enabled(true)
                .build();
    }
}
```

---

**最後更新：** 2025-12-07  
**作者：** Cheng
