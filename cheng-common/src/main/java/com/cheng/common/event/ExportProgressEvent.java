package com.cheng.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 匯出進度事件
 * <p>
 * 用於解耦 Service 和 SSE 推送邏輯
 * Service 發布事件，Framework 層監聽並推送 SSE
 */
@Getter
public class ExportProgressEvent extends ApplicationEvent {

    /**
     * 任務 ID
     */
    private final String taskId;

    /**
     * 進度（0-100，-1 表示錯誤）
     */
    private final int progress;

    /**
     * 訊息
     */
    private final String message;

    public ExportProgressEvent(Object source, String taskId, int progress, String message) {
        super(source);
        this.taskId = taskId;
        this.progress = progress;
        this.message = message;
    }
}
