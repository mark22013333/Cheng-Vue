package com.cheng.common.core.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 任務類型選項
 * 用於前端顯示任務類型的下拉選單
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Data
@Builder
public class TaskTypeOption {

    /**
     * 任務類型代碼（例如：CA102、CA103、LINE_PUSH）
     */
    private String code;

    /**
     * 任務類型標籤（例如：臺灣證券交易所 - 即時重大訊息）
     */
    private String label;

    /**
     * 任務分類（例如：CRAWLER、NOTIFICATION）
     */
    private String category;

    /**
     * 任務描述（可選，用於前端顯示詳細說明）
     */
    private String description;

    /**
     * 任務支援的參數列表
     */
    private List<TaskParamMetadata> params;

    /**
     * Bean 名稱（用於呼叫目標，例如：crawlerTask）
     */
    private String beanName;

    /**
     * 預設方法名稱（例如：runWithMode）
     */
    private String defaultMethod;

    /**
     * 是否啟用
     */
    @Builder.Default
    private Boolean enabled = true;
}
