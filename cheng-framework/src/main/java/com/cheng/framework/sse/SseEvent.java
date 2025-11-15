package com.cheng.framework.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 事件模型
 * 統一的 SSE 事件資料結構
 * 
 * @author cheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {
    
    /**
     * 事件名稱
     */
    private String eventName;
    
    /**
     * 階段 (creating, uploading, processing, success, error)
     */
    private String stage;
    
    /**
     * 進度 (0-100)
     */
    private Integer progress;
    
    /**
     * 訊息
     */
    private String message;
    
    /**
     * 資料
     */
    private Object data;
    
    /**
     * 時間戳
     */
    private Long timestamp;
    
    /**
     * 建立進度事件
     * 
     * @param stage 階段
     * @param progress 進度 (0-100)
     * @param message 訊息
     * @return SseEvent
     */
    public static SseEvent progress(String stage, int progress, String message) {
        return SseEvent.builder()
                .eventName("progress")
                .stage(stage)
                .progress(progress)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 建立成功事件
     * 
     * @param message 訊息
     * @param data 資料
     * @return SseEvent
     */
    public static SseEvent success(String message, Object data) {
        return SseEvent.builder()
                .eventName("success")
                .stage("success")
                .progress(100)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 建立錯誤事件
     * 
     * @param message 錯誤訊息
     * @return SseEvent
     */
    public static SseEvent error(String message) {
        return SseEvent.builder()
                .eventName("error")
                .stage("error")
                .progress(0)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 建立連線事件
     * 
     * @param message 訊息
     * @return SseEvent
     */
    public static SseEvent connected(String message) {
        return SseEvent.builder()
                .eventName("connected")
                .stage("connected")
                .progress(0)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
