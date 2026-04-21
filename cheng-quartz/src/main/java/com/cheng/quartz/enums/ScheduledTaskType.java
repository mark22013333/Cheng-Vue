package com.cheng.quartz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定時任務類型枚舉
 * <p>
 * 定義系統中所有可用的定時任務，包含任務的元資料資訊
 * 前端可以透過 API 取得此 enum 的所有選項，提供給使用者選擇
 *
 * @author Cheng
 * @since 2025-03-28
 */
@Getter
@RequiredArgsConstructor
public enum ScheduledTaskType {

    // ==================== 爬蟲任務 ====================

    CRAWLER_RUN(
            "crawler_run",
            "執行爬蟲（無參數）",
            "執行指定類型的爬蟲，使用預設參數",
            "crawlerTask",
            "run",
            List.of(
                    new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102")
            ),
            "0 0 1 * * ?",
            "爬蟲任務",
            "每日爬蟲任務"
    ),

    CRAWLER_RUN_WITH_MODE(
            "crawler_run_with_mode",
            "執行爬蟲（帶模式）",
            "執行指定類型的爬蟲，可設定執行模式",
            "crawlerTask",
            "runWithMode",
            List.of(
                    new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102"),
                    new TaskParameter("mode", "STRING", true, "執行模式", "today-only")
            ),
            "0 0 9,12,15 * * ?",
            "爬蟲任務",
            "今日爬蟲任務"
    ),

    CRAWLER_RUN_ADVANCED(
            "crawler_run_advanced",
            "執行爬蟲（完整參數）",
            "執行指定類型的爬蟲，支援完整參數配置",
            "crawlerTask",
            "runAdvanced",
            List.of(
                    new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102"),
                    new TaskParameter("mode", "STRING", true, "執行模式", "full-sync"),
                    new TaskParameter("batchSize", "NUMBER", false, "批次大小", "1000"),
                    new TaskParameter("timeout", "NUMBER", false, "逾時時間(ms)", "120000")
            ),
            "0 0 2 * * ?",
            "爬蟲任務",
            "完整爬蟲任務"
    ),

    CRAWLER_RUN_DATE_RANGE(
            "crawler_run_date_range",
            "執行爬蟲（日期範圍）",
            "執行指定類型的爬蟲，可設定日期範圍",
            "crawlerTask",
            "runDateRange",
            List.of(
                    new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102"),
                    new TaskParameter("startDate", "STRING", true, "開始日期", "2025-03-01"),
                    new TaskParameter("endDate", "STRING", true, "結束日期", "2025-03-28")
            ),
            null,
            "爬蟲任務",
            "日期範圍爬蟲任務"
    ),

    CRAWLER_RUN_FULL(
            "crawler_run_full",
            "執行爬蟲（所有參數）",
            "執行指定類型的爬蟲，支援所有參數的完整配置",
            "crawlerTask",
            "runFull",
            List.of(
                    new TaskParameter("crawlerType", "STRING", true, "爬蟲類型", "CA102"),
                    new TaskParameter("mode", "STRING", true, "執行模式", "full-sync"),
                    new TaskParameter("startDate", "STRING", true, "開始日期", "2025-03-01"),
                    new TaskParameter("endDate", "STRING", true, "結束日期", "2025-03-28"),
                    new TaskParameter("batchSize", "NUMBER", false, "批次大小", "1000"),
                    new TaskParameter("timeout", "NUMBER", false, "逾時時間(ms)", "120000")
            ),
            "0 0 2 * * ?",
            "爬蟲任務",
            "完整配置爬蟲任務"
    ),

    // ==================== 資料處理任務 ====================

    PRODUCT_IMPORT(
            "product_import",
            "CSV 商品匯入",
            "從內建 CSV 批次匯入商品。預設讀取 classpath:import/prostaff-products.csv；亦支援相對檔名（組合 cheng.import.base-path）或 classpath: 其他路徑",
            "productImportTask",
            "run",
            List.of(
                    new TaskParameter("csvPath", "STRING", true, "CSV 來源（classpath 或相對檔名）",
                            "classpath:import/prostaff-products.csv")
            ),
            null,
            "資料處理",
            "CSV 商品匯入任務（預設使用內建 classpath:import/prostaff-products.csv）"
    ),

    // ==================== LINE 推播任務 ====================

    LINE_TAG_PUSH(
            "line_tag_push",
            "LINE 標籤推播",
            "根據標籤或標籤群組篩選目標使用者，執行逐人 LINE 推播",
            "lineScheduledPushTask",
            "execute",
            List.of(
                    new TaskParameter("configId", "NUMBER", true, "LINE 頻道 ID", "1"),
                    new TaskParameter("templateId", "NUMBER", true, "訊息範本 ID", "5"),
                    new TaskParameter("tagIds", "STRING", false, "標籤 ID（逗號分隔）", "1,2,3"),
                    new TaskParameter("tagGroupIds", "STRING", false, "標籤群組 ID（逗號分隔）", "1")
            ),
            "0 0 9 ? * MON",
            "LINE 推播",
            "LINE 標籤推播任務"
    );

    /**
     * 任務代碼（唯一識別）
     */
    private final String code;

    /**
     * 任務名稱
     */
    private final String name;

    /**
     * 任務描述
     */
    private final String description;

    /**
     * Spring Bean 名稱
     */
    private final String beanName;

    /**
     * 方法名稱
     */
    private final String methodName;

    /**
     * 參數列表
     */
    private final List<TaskParameter> parameters;

    /**
     * 建議的 Cron 表達式（可選）
     */
    private final String suggestedCron;

    /**
     * 任務分類
     */
    private final String category;

    /**
     * 預設任務名稱（建議值）
     */
    private final String defaultJobName;

    /**
     * 根據 code 查詢任務類型
     *
     * @param code 任務代碼
     * @return ScheduledTaskType，若找不到則返回 null
     */
    public static ScheduledTaskType of(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.getCode().equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 取得所有任務類型
     *
     * @return 所有任務類型列表
     */
    public static List<ScheduledTaskType> all() {
        return Arrays.asList(values());
    }

    /**
     * 根據分類查詢任務類型
     *
     * @param category 任務分類
     * @return 該分類下的所有任務類型
     */
    public static List<ScheduledTaskType> byCategory(String category) {
        return Arrays.stream(values())
                .filter(type -> type.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * 取得所有分類
     *
     * @return 所有分類列表（去重）
     */
    public static List<String> getAllCategories() {
        return Arrays.stream(values())
                .map(ScheduledTaskType::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 任務參數定義
     */
    @Getter
    @AllArgsConstructor
    public static class TaskParameter {
        /**
         * 參數名稱
         */
        private String name;

        /**
         * 參數類型（對應 TaskParamType 枚舉：STRING, NUMBER, BOOLEAN, SELECT, DATE 等）
         */
        private String type;

        /**
         * 是否必填
         */
        private boolean required;

        /**
         * 參數說明
         */
        private String description;

        /**
         * 預設值範例
         */
        private String example;
    }
}
