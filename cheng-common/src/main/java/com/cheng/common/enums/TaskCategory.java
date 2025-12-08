package com.cheng.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任務分類列舉
 * 用於區分不同類型的排程任務
 * 對應 sys_dict_data 中的 sys_job_group 字典類型（V28 migration）
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Getter
@AllArgsConstructor
public enum TaskCategory {
    
    /**
     * 資料處理 - 資料同步、匯入匯出、格式轉換等
     * 對應 jobGroup: DATA
     */
    DATA("資料處理", "DATA"),
    
    /**
     * 系統維護 - 備份、清理、健康檢查、日誌歸檔等
     * 對應 jobGroup: MAINTENANCE
     */
    MAINTENANCE("系統維護", "MAINTENANCE"),
    
    /**
     * 通知推播 - LINE推播、郵件通知、簡訊發送等
     * 對應 jobGroup: NOTIFICATION
     */
    NOTIFICATION("通知推播", "NOTIFICATION"),
    
    /**
     * 報表統計 - 日報、週報、月報、統計分析等
     * 對應 jobGroup: REPORT
     */
    REPORT("報表統計", "REPORT"),
    
    /**
     * 業務處理 - 訂單處理、庫存盤點、資料對帳等
     * 對應 jobGroup: BUSINESS
     */
    BUSINESS("業務處理", "BUSINESS"),
    
    /**
     * 爬蟲抓取 - 網頁抓取、API呼叫、資料蒐集等
     * 對應 jobGroup: CRAWLER
     */
    CRAWLER("爬蟲抓取", "CRAWLER");
    
    /**
     * 顯示名稱（用於前端顯示）
     */
    private final String label;
    
    /**
     * 程式碼（用於識別，對應 jobGroup 的 value）
     */
    private final String code;
}
