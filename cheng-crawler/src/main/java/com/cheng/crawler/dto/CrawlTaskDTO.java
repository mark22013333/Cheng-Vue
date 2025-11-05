package com.cheng.crawler.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 爬取任務 DTO
 *
 * @author cheng
 */
@Data
public class CrawlTaskDTO {
    
    /**
     * 任務 ID
     */
    private String taskId;
    
    /**
     * ISBN
     */
    private String isbn;
    
    /**
     * 任務狀態: PENDING(等待中)、PROCESSING(處理中)、COMPLETED(已完成)、FAILED(失敗)
     */
    private TaskStatus status;
    
    /**
     * 書籍資訊（完成後才有）
     */
    private BookInfoDTO bookInfo;
    
    /**
     * 錯誤訊息（失敗時才有）
     */
    private String errorMessage;
    
    /**
     * 建立時間
     */
    private LocalDateTime createTime;
    
    /**
     * 完成時間
     */
    private LocalDateTime completeTime;
    
    /**
     * 任務狀態枚舉
     */
    @Getter
    @RequiredArgsConstructor
    public enum TaskStatus {
        PENDING("等待中"),
        PROCESSING("處理中"),
        COMPLETED("已完成"),
        FAILED("失敗");
        
        private final String description;
    }
}
