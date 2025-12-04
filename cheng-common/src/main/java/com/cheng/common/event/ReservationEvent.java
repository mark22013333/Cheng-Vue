package com.cheng.common.event;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 物品預約事件
 *
 * @author cheng
 */
@Data
@AllArgsConstructor
public class ReservationEvent {

    /**
     * 事件類型
     */
    private String eventType;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 用戶ID
     */
    private Long userId;

    /**
     * 預約記錄ID
     */
    private Long borrowId;

    /**
     * 事件訊息
     */
    private String message;

    /**
     * 事件時間
     */
    private Date eventTime;

    /**
     * 當前可用數量
     */
    private Integer availableQuantity;

    /**
     * 預約數量
     */
    private Integer reservedQuantity;

    /**
     * 任務ID（用於 SSE）
     */
    private String taskId;
}
