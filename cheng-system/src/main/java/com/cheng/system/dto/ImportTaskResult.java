package com.cheng.system.dto;

/**
 * 匯入任務結果
 *
 * @param taskId   任務ID
 * @param rowCount 資料筆數（排除標題列）
 * @author cheng
 */
public record ImportTaskResult(String taskId, Integer rowCount) {
}
