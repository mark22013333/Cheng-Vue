package com.cheng.crawler.dto;

import com.cheng.crawler.enums.CrawlerType;
import lombok.Data;

/**
 * 爬蟲執行結果
 *
 * @author Cheng
 * @since 2025-10-19
 */
@Data
public class CrawlerResult {
    
    /** 爬蟲類型 */
    private CrawlerType crawlerType;
    
    /** 是否成功 */
    private boolean success;
    
    /** 訊息 */
    private String message;
    
    /** 錯誤資訊 */
    private Exception error;
    
    /** 開始時間 */
    private long startTime;
    
    /** 結束時間 */
    private long endTime;
    
    /** 執行時間（毫秒） */
    private long durationMs;
    
    /** 原始資料筆數 */
    private int rawDataCount;
    
    /** 處理後資料筆數 */
    private int processedDataCount;
    
    /** 儲存成功筆數 */
    private int savedDataCount;
    
    /**
     * 取得執行時間（秒）
     */
    public double getDurationSeconds() {
        return durationMs / 1000.0;
    }
}
