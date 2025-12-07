package com.cheng.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任務分類列舉
 * 用於區分不同類型的排程任務
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Getter
@AllArgsConstructor
public enum TaskCategory {
    
    /**
     * 爬蟲任務
     */
    CRAWLER("爬蟲任務", "crawler"),
    
    /**
     * 推播訊息任務
     */
    NOTIFICATION("推播訊息", "notification"),
    
    /**
     * 報表任務
     */
    REPORT("報表任務", "report"),
    
    /**
     * 資料同步任務
     */
    SYNC("資料同步", "sync"),
    
    /**
     * 備份任務
     */
    BACKUP("備份任務", "backup"),
    
    /**
     * 預約任務
     */
    RESERVATION("預約任務", "reservation"),
    
    /**
     * 維護任務
     */
    MAINTENANCE("維護任務", "maintenance"),
    
    /**
     * 自訂任務
     */
    CUSTOM("自訂任務", "custom");
    
    /**
     * 顯示名稱（用於前端顯示）
     */
    private final String label;
    
    /**
     * 程式碼（用於識別）
     */
    private final String code;
}
