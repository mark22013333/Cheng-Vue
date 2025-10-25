package com.cheng.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 爬蟲執行參數
 * <p>
 * 用於從外部（如 Quartz 排程、後台介面）傳入爬蟲執行所需的參數
 *
 * @author Cheng
 * @since 2025-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerParams implements Serializable {

    /**
     * 是否啟用（用於排程控制）
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 爬蟲類型代碼（如：CA101, CA102, CA103）
     */
    private String crawlerType;

    /**
     * 模式參數（如：today-only, full-sync 等）
     */
    private String mode;

    /**
     * 開始日期（格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 結束日期（格式：yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 批次大小（用於覆寫預設批次大小）
     */
    private Integer batchSize;

    /**
     * 超時時間（毫秒）
     */
    private Long timeout;

    /**
     * 其他自訂參數（可擴充）
     */
    @Builder.Default
    private Map<String, Object> customParams = new HashMap<>();

    /**
     * 取得自訂參數
     */
    public Object getCustomParam(String key) {
        return customParams != null ? customParams.get(key) : null;
    }

    /**
     * 設定自訂參數
     */
    public void setCustomParam(String key, Object value) {
        if (customParams == null) {
            customParams = new HashMap<>();
        }
        customParams.put(key, value);
    }

    /**
     * 取得字串型自訂參數
     */
    public String getCustomParamAsString(String key) {
        Object value = getCustomParam(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 取得整數型自訂參數
     */
    public Integer getCustomParamAsInteger(String key) {
        Object value = getCustomParam(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value != null ? Integer.parseInt(value.toString()) : null;
    }

    /**
     * 取得布林型自訂參數
     */
    public Boolean getCustomParamAsBoolean(String key) {
        Object value = getCustomParam(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null ? Boolean.parseBoolean(value.toString()) : null;
    }

    /**
     * 建立空參數（用於無參數執行）
     */
    public static CrawlerParams empty() {
        return CrawlerParams.builder().build();
    }
}
