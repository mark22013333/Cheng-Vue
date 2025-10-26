package com.cheng.common.core.domain;

import com.cheng.common.enums.TaskParamType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 任務參數元數據
 * 定義任務參數的屬性和約束，用於動態產生前端表單
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class TaskParamMetadata {

    /**
     * 參數名稱（用於程式識別，例如：mode, startDate, batchSize）
     */
    private String name;

    /**
     * 參數顯示名稱（用於前端顯示，例如：執行模式、開始日期、批次大小）
     */
    private String label;

    /**
     * 參數類型（字串、數字、下拉選單等）
     */
    private TaskParamType type;

    /**
     * 預設值（字串格式）
     */
    private String defaultValue;

    /**
     * 是否必填
     */
    @Builder.Default
    private Boolean required = false;

    /**
     * 參數說明（用於前端顯示提示）
     */
    private String description;

    /**
     * 可選值列表（用於下拉選單、單選按鈕等）
     * 格式：[{value: "today-only", label: "僅今日"}, {value: "full-sync", label: "完整同步"}]
     */
    private List<OptionItem> options;

    /**
     * 驗證規則（可選，例如：正則表達式、最小值、最大值等）
     */
    private ValidationRule validation;

    /**
     * 參數順序（用於前端排序）
     */
    @Builder.Default
    private Integer order = 0;

    /**
     * 是否顯示在前端（某些內部參數可能不需要顯示）
     */
    @Builder.Default
    private Boolean visible = true;

    /**
     * 選項項目（用於下拉選單等）
     */
    @Data
    @Builder
    public static class OptionItem {
        /**
         * 選項值（傳遞給後端的值）
         */
        private String value;

        /**
         * 選項標籤（前端顯示的文字）
         */
        private String label;

        /**
         * 選項說明（可選）
         */
        private String description;
    }

    /**
     * 驗證規則
     */
    @Data
    @Builder
    public static class ValidationRule {
        /**
         * 正則表達式（用於字串驗證）
         */
        private String pattern;

        /**
         * 最小值（用於數字驗證）
         */
        private Double min;

        /**
         * 最大值（用於數字驗證）
         */
        private Double max;

        /**
         * 最小長度（用於字串驗證）
         */
        private Integer minLength;

        /**
         * 最大長度（用於字串驗證）
         */
        private Integer maxLength;

        /**
         * 錯誤訊息（驗證失敗時顯示）
         */
        private String message;
    }
}
