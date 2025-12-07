package com.cheng.quartz.enums;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.enums.TaskParamType;
import com.cheng.quartz.task.FileMaintenanceTask;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 維護任務類型列舉
 *
 * <p>定義所有維護任務的類型、執行方法和參數
 *
 * <p><b>設計優勢：</b>
 * <ul>
 *   <li>明確的類別引用 - IDE 可追蹤使用，避免誤判為未使用</li>
 *   <li>編譯時期檢查 - 方法名稱錯誤會在編譯時發現</li>
 *   <li>集中管理 - 所有任務類型定義在一處，易於維護</li>
 *   <li>類型安全 - 避免字串拼寫錯誤</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-12-08
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
            FileMaintenanceTask.class,
            "cleanupFiles",
            true,
            buildBackupFilesParams()
    ),

    /**
     * 清理暫存檔案（預留）
     */
    CLEANUP_TEMP_FILES(
            "CLEANUP_TEMP_FILES",
            "清理暫存檔案",
            "清理超過保留期限的暫存檔案（未來擴展）",
            FileMaintenanceTask.class,
            "cleanupFiles",
            false,  // 暫時停用
            buildTempFilesParams()
    );

    /**
     * 任務代碼（唯一識別碼）
     */
    private final String code;

    /**
     * 任務名稱
     */
    private final String label;

    /**
     * 任務描述
     */
    private final String description;

    /**
     * 任務類別（明確引用，IDE 可追蹤）
     */
    private final Class<?> taskClass;

    /**
     * 執行方法名稱
     */
    private final String methodName;

    /**
     * 是否啟用
     */
    private final boolean enabled;

    /**
     * 參數定義
     */
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
        // 隱藏參數：taskType
        TaskParamMetadata taskTypeParam = TaskParamMetadata.builder()
                .name("taskType")
                .label("任務類型")
                .type(TaskParamType.STRING)
                .required(true)
                .defaultValue("BACKUP_FILES")
                .description("固定為 BACKUP_FILES")
                .order(0)
                .visible(false)
                .build();

        // 可見參數：retentionDays
        TaskParamMetadata retentionDaysParam = TaskParamMetadata.builder()
                .name("retentionDays")
                .label("保留天數")
                .type(TaskParamType.NUMBER)
                .required(true)
                .defaultValue("30")
                .description("保留最近 N 天的備份檔案，超過此天數的檔案將被刪除")
                .order(1)
                .visible(true)
                .build();

        // 可見參數：targetPath
        TaskParamMetadata targetPathParam = TaskParamMetadata.builder()
                .name("targetPath")
                .label("目標路徑")
                .type(TaskParamType.STRING)
                .required(false)
                .defaultValue("")
                .description("相對於上傳根目錄的路徑，留空表示掃描整個上傳目錄（例如：書籍封面）")
                .order(2)
                .visible(true)
                .build();

        return Arrays.asList(taskTypeParam, retentionDaysParam, targetPathParam);
    }

    /**
     * 建立暫存檔案清理的參數定義
     */
    private static List<TaskParamMetadata> buildTempFilesParams() {
        TaskParamMetadata taskTypeParam = TaskParamMetadata.builder()
                .name("taskType")
                .label("任務類型")
                .type(TaskParamType.STRING)
                .required(true)
                .defaultValue("TEMP_FILES")
                .description("固定為 TEMP_FILES")
                .order(0)
                .visible(false)
                .build();

        TaskParamMetadata retentionDaysParam = TaskParamMetadata.builder()
                .name("retentionDays")
                .label("保留天數")
                .type(TaskParamType.NUMBER)
                .required(true)
                .defaultValue("7")
                .description("保留最近 N 天的暫存檔案")
                .order(1)
                .visible(true)
                .build();

        TaskParamMetadata targetPathParam = TaskParamMetadata.builder()
                .name("targetPath")
                .label("目標路徑")
                .type(TaskParamType.STRING)
                .required(false)
                .defaultValue("")
                .description("相對於上傳根目錄的路徑")
                .order(2)
                .visible(true)
                .build();

        return Arrays.asList(taskTypeParam, retentionDaysParam, targetPathParam);
    }
}
