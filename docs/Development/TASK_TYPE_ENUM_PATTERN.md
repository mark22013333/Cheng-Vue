# 排程任務類型 Enum 模式

## 問題背景

在排程任務開發中，Task 類別（如 `FileMaintenanceTask`、`ReservationTask`）會被 IDE 誤判為「未使用」，原因：

1. **反射調用**：Quartz 透過反射根據 Bean 名稱和方法名稱調用任務
2. **字串引用**：Bean 名稱和方法名稱都是字串，IDE 無法追蹤
3. **缺乏編譯檢查**：方法名稱拼寫錯誤只能在執行時發現

```java
// ❌ IDE 警告：Class 'FileMaintenanceTask' is never used
@Component("fileMaintenanceTask")
public class FileMaintenanceTask {
    public void cleanupFiles(String params) { /* ... */ }
}

// ❌ IDE 無法追蹤這些字串引用
.beanName("fileMaintenanceTask")
.defaultMethod("cleanupFiles")
```

---

## 解決方案：Enum + Class Reference

使用 **Enum 集中管理任務類型**，每個 Enum 值包含：
- **明確的類別引用**（`Class<?>`）- IDE 可追蹤使用
- **方法名稱**（`String`）- 集中管理，避免拼寫錯誤
- **參數定義**（`List<TaskParamMetadata>`）- 完整的類型資訊

### 架構設計

```
cheng-quartz/src/main/java/com/cheng/quartz/
├── enums/
│   ├── MaintenanceTaskType.java      ✅ 維護任務類型 Enum
│   ├── ReservationTaskType.java      ✅ 預約任務類型 Enum
│   └── ...
├── task/
│   ├── FileMaintenanceTask.java      ✅ 任務執行器（被 Enum 引用）
│   ├── ReservationTask.java          ✅ 任務執行器（被 Enum 引用）
│   └── ...
└── provider/
    ├── MaintenanceTaskTypeProvider.java  ✅ 從 Enum 讀取任務類型
    ├── ReservationTaskTypeProvider.java  ✅ 從 Enum 讀取任務類型
    └── ...
```

---

## 實作範例

### 1. 建立 Task Type Enum

**檔案：** `cheng-quartz/src/main/java/com/cheng/quartz/enums/MaintenanceTaskType.java`

```java
package com.cheng.quartz.enums;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.enums.TaskParamType;
import com.cheng.quartz.task.FileMaintenanceTask;  // ✅ 明確引用
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 維護任務類型列舉
 * 
 * <p><b>設計優勢：</b>
 * <ul>
 *   <li>明確的類別引用 - IDE 可追蹤使用，避免誤判為未使用</li>
 *   <li>編譯時期檢查 - 方法名稱錯誤會在編譯時發現</li>
 *   <li>集中管理 - 所有任務類型定義在一處，易於維護</li>
 *   <li>類型安全 - 避免字串拼寫錯誤</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum MaintenanceTaskType {

    /**
     * 清理備份檔案
     */
    CLEANUP_BACKUP_FILES(
            "CLEANUP_BACKUP_FILES",
            "清理備份檔案",
            "清理超過保留期限的圖片備份檔案（格式：*.backup.*），釋放磁碟空間",
            FileMaintenanceTask.class,  // ✅ IDE 可追蹤這個引用
            "cleanupFiles",              // ✅ 集中管理方法名稱
            true,
            buildBackupFilesParams()
    );

    private final String code;
    private final String label;
    private final String description;
    private final Class<?> taskClass;        // ✅ 明確的類別引用
    private final String methodName;
    private final boolean enabled;
    private final List<TaskParamMetadata> params;

    /**
     * 取得 Spring Bean 名稱
     * 根據命名規範：XxxTask -> xxxTask
     */
    public String getBeanName() {
        String className = taskClass.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * 建立備份檔案清理的參數定義
     */
    private static List<TaskParamMetadata> buildBackupFilesParams() {
        TaskParamMetadata taskTypeParam = TaskParamMetadata.builder()
                .name("taskType")
                .type(TaskParamType.STRING)
                .required(true)
                .defaultValue("BACKUP_FILES")
                .visible(false)
                .order(0)
                .build();

        TaskParamMetadata retentionDaysParam = TaskParamMetadata.builder()
                .name("retentionDays")
                .label("保留天數")
                .type(TaskParamType.NUMBER)
                .required(true)
                .defaultValue("30")
                .description("保留最近 N 天的備份檔案")
                .order(1)
                .visible(true)
                .build();

        TaskParamMetadata targetPathParam = TaskParamMetadata.builder()
                .name("targetPath")
                .label("目標路徑")
                .type(TaskParamType.STRING)
                .required(false)
                .defaultValue("")
                .description("相對於上傳根目錄的路徑，留空表示掃描整個上傳目錄")
                .order(2)
                .visible(true)
                .build();

        return Arrays.asList(taskTypeParam, retentionDaysParam, targetPathParam);
    }
}
```

### 2. 簡化 Task Type Provider

**檔案：** `cheng-quartz/src/main/java/com/cheng/quartz/provider/MaintenanceTaskTypeProvider.java`

```java
package com.cheng.quartz.provider;

import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.quartz.enums.MaintenanceTaskType;  // ✅ 引用 Enum
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MaintenanceTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.MAINTENANCE;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        // ✅ 直接從 Enum 轉換，無需手動建立 TaskTypeOption
        return Arrays.stream(MaintenanceTaskType.values())
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 50;
    }

    /**
     * 從 MaintenanceTaskType Enum 建立 TaskTypeOption
     */
    private TaskTypeOption buildTaskTypeOption(MaintenanceTaskType type) {
        return TaskTypeOption.builder()
                .code(type.getCode())
                .label(String.format("[MAINTENANCE] 檔案維護 - %s", type.getLabel()))
                .category(TaskCategory.MAINTENANCE.getCode())
                .description(type.getDescription())
                .beanName(type.getBeanName())          // ✅ 從 Enum 取得
                .defaultMethod(type.getMethodName())   // ✅ 從 Enum 取得
                .params(type.getParams())              // ✅ 從 Enum 取得
                .enabled(type.isEnabled())
                .build();
    }
}
```

---

## 優勢對比

### Before（字串引用）

```java
// ❌ 問題
@Component("fileMaintenanceTask")  // IDE 警告：未使用
public class FileMaintenanceTask { /* ... */ }

// Provider 中
TaskTypeOption.builder()
    .beanName("fileMaintenanceTask")  // IDE 無法追蹤
    .defaultMethod("cleanupFiles")     // 拼寫錯誤只能執行時發現
    .params(buildParams())             // 重複定義參數
    .build();
```

### After（Enum 引用）

```java
// ✅ 優勢
@Component("fileMaintenanceTask")  // ✅ 不再警告（Enum 有引用）
public class FileMaintenanceTask { /* ... */ }

// Enum 中
CLEANUP_BACKUP_FILES(
    "CLEANUP_BACKUP_FILES",
    "清理備份檔案",
    "...",
    FileMaintenanceTask.class,  // ✅ IDE 可追蹤這個類別引用
    "cleanupFiles",              // ✅ 集中管理，易於重構
    true,
    buildBackupFilesParams()     // ✅ 參數定義集中管理
)

// Provider 中
MaintenanceTaskType.values()  // ✅ 自動讀取所有任務類型
```

---

## 優勢總結

| 特性 | 字串引用 | Enum 引用 |
|------|----------|-----------|
| **IDE 追蹤** | ❌ 無法追蹤，誤判未使用 | ✅ 明確引用，可追蹤使用 |
| **編譯檢查** | ❌ 執行時才發現錯誤 | ✅ 編譯時發現方法名稱錯誤 |
| **重構支援** | ❌ 需手動搜尋替換 | ✅ IDE 自動重構 |
| **參數管理** | ❌ 分散在 Provider 中 | ✅ 集中在 Enum 中 |
| **可讀性** | ❌ 字串魔術值 | ✅ 類型安全，易讀 |
| **維護性** | ❌ 重複程式碼多 | ✅ DRY 原則 |

---

## 開發流程（更新）

### Step 1: 建立任務執行器

**位置：** `cheng-quartz/src/main/java/com/cheng/quartz/task/`

```java
@Slf4j
@Component("xxxTask")
@RequiredArgsConstructor
public class XxxTask {
    private final ObjectMapper objectMapper;
    
    public void methodName(String params) {
        // 執行邏輯
    }
}
```

### Step 2: 建立任務類型 Enum ⭐ **NEW**

**位置：** `cheng-quartz/src/main/java/com/cheng/quartz/enums/`

```java
@Getter
@AllArgsConstructor
public enum XxxTaskType {
    
    TASK_CODE(
        "TASK_CODE",
        "任務名稱",
        "任務描述",
        XxxTask.class,           // ✅ 明確引用 Task 類別
        "methodName",            // ✅ 方法名稱
        true,
        buildParams()            // ✅ 參數定義
    );
    
    private final String code;
    private final String label;
    private final String description;
    private final Class<?> taskClass;
    private final String methodName;
    private final boolean enabled;
    private final List<TaskParamMetadata> params;
    
    public String getBeanName() {
        String className = taskClass.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
    
    private static List<TaskParamMetadata> buildParams() {
        // 定義參數
        return Arrays.asList(/* params */);
    }
}
```

### Step 3: 簡化任務類型提供者

**位置：** `cheng-quartz/src/main/java/com/cheng/quartz/provider/`

```java
@Component
public class XxxTaskTypeProvider implements TaskTypeProvider {
    
    @Override
    public TaskCategory getCategory() {
        return TaskCategory.XXX;
    }
    
    @Override
    public List<TaskTypeOption> getTaskTypes() {
        // ✅ 直接從 Enum 讀取
        return Arrays.stream(XxxTaskType.values())
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }
    
    private TaskTypeOption buildTaskTypeOption(XxxTaskType type) {
        return TaskTypeOption.builder()
                .code(type.getCode())
                .label(type.getLabel())
                .category(TaskCategory.XXX.getCode())
                .description(type.getDescription())
                .beanName(type.getBeanName())
                .defaultMethod(type.getMethodName())
                .params(type.getParams())
                .enabled(type.isEnabled())
                .build();
    }
}
```

---

## 已實作的範例

### 1. 維護任務

- **Enum**: `MaintenanceTaskType.java`
- **Task**: `FileMaintenanceTask.java`
- **Provider**: `MaintenanceTaskTypeProvider.java`

**任務類型：**
- `CLEANUP_BACKUP_FILES` - 清理備份檔案
- `CLEANUP_TEMP_FILES` - 清理暫存檔案（預留）

### 2. 預約任務

- **Enum**: `ReservationTaskType.java`
- **Task**: `ReservationTask.java`
- **Provider**: `ReservationTaskTypeProvider.java`

**任務類型：**
- `EXPIRED_BOOKING_CLEANUP` - 過期預約清理

---

## 擴展指南

### 新增任務類型

只需在對應的 Enum 中新增一個值即可：

```java
public enum MaintenanceTaskType {
    
    CLEANUP_BACKUP_FILES(/* ... */),
    
    CLEANUP_TEMP_FILES(/* ... */),
    
    // ✅ 新增任務類型
    CLEANUP_LOGS(
        "CLEANUP_LOGS",
        "清理日誌檔案",
        "清理過期的日誌檔案",
        FileMaintenanceTask.class,  // 可共用同一個 Task 類別
        "cleanupLogs",              // 新方法
        true,
        buildLogsParams()
    );
    
    // ...
}
```

### 新增任務分類

1. 建立新的 Task 類別
2. 建立對應的 TaskType Enum
3. 建立對應的 TaskTypeProvider
4. 在 `TaskCategory` 中新增分類

---

## 檢查清單

開發新任務時，確認：

- [ ] 建立 Task 類別（執行器）
- [ ] 建立 TaskType Enum（類型定義）
- [ ] TaskType Enum 中明確引用 Task 類別（`XxxTask.class`）
- [ ] 在 Enum 中定義方法名稱（`"methodName"`）
- [ ] 在 Enum 中定義參數（`buildParams()`）
- [ ] TaskTypeProvider 從 Enum 讀取任務類型
- [ ] IDE 不再警告 Task 類別未使用

---

## 總結

透過 **Enum + Class Reference** 模式，我們實現了：

✅ **IDE 友善** - 不再誤判為未使用  
✅ **類型安全** - 編譯時期檢查  
✅ **易於維護** - 集中管理，DRY 原則  
✅ **重構友善** - IDE 自動重構支援  
✅ **可讀性高** - 明確的引用關係  

這個模式提升了程式碼品質和開發體驗，建議所有排程任務都採用此模式。

---

**建立日期：** 2025-12-08  
**作者：** Cheng  
**版本：** 1.0
