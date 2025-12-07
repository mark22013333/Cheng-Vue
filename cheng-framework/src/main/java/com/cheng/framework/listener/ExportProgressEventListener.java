package com.cheng.framework.listener;

import com.cheng.common.event.ExportProgressEvent;
import com.cheng.framework.sse.SseChannels;
import com.cheng.framework.sse.SseEvent;
import com.cheng.framework.sse.SseManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 匯出進度事件監聽器
 * <p>
 * 監聽 Service 層發布的進度事件，推送 SSE 通知
 * 解決 cheng-system 無法直接依賴 cheng-framework 的問題
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportProgressEventListener {

    private final SseManager sseManager;

    /**
     * 處理匯出進度事件
     */
    @Async
    @EventListener
    public void handleExportProgress(ExportProgressEvent event) {
        try {
            String taskId = event.getTaskId();
            int progress = event.getProgress();
            String message = event.getMessage();

            SseEvent sseEvent;
            if (progress == -1) {
                // 錯誤
                sseEvent = SseEvent.error(message);
            } else if (progress >= 100) {
                // 成功
                sseEvent = SseEvent.success(message, null);
            } else {
                // 進度更新
                sseEvent = SseEvent.progress("exporting", progress, message);
            }

            // 推送 SSE 事件
            sseManager.send(SseChannels.ITEM_EXPORT, taskId, sseEvent);

            log.debug("SSE 進度推送成功 - taskId: {}, progress: {}", taskId, progress);
        } catch (Exception e) {
            log.error("SSE 進度推送失敗", e);
        }
    }
}
