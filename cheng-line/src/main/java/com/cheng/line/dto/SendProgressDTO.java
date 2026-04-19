package com.cheng.line.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 推播進度 DTO
 * 用於追蹤非同步標籤推播的即時進度
 *
 * @author cheng
 */
@Data
public class SendProgressDTO {

    /**
     * 任務 ID（UUID）
     */
    private String taskId;

    /**
     * 任務狀態：PENDING/SENDING/DONE/ERROR
     */
    private String status;

    /**
     * 目標總人數
     */
    private int total;

    /**
     * 已發送人數
     */
    private int sent;

    /**
     * 成功人數
     */
    private int success;

    /**
     * 失敗人數
     */
    private int failed;

    /**
     * 任務開始時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 預估剩餘秒數
     */
    private int estimatedRemainingSeconds;

    /**
     * 對應的訊息記錄 ID
     */
    private Long messageId;
}
