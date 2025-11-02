package com.cheng.framework.config;

import com.cheng.common.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;

/**
 * 非同步任務 TraceId 裝飾器
 * 用於在非同步任務執行時傳遞父執行緒的 traceId 到子執行緒
 *
 * @author cheng
 * @since 2025-11-02
 */
@Slf4j
public class TraceTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 在父執行緒中取得 traceId
        String parentTraceId = TraceUtils.getTraceId();

        return () -> {
            try {
                // 在子執行緒中設定 traceId
                if (parentTraceId != null) {
                    TraceUtils.setTraceId(parentTraceId);
                    log.debug("非同步任務繼承 traceId: {}", parentTraceId);
                } else {
                    // 如果父執行緒沒有 traceId，為子執行緒產生新的 traceId
                    TraceUtils.initTrace();
                    log.debug("非同步任務產生新 traceId: {}", TraceUtils.getTraceId());
                }

                // 執行實際任務
                runnable.run();

            } finally {
                // 任務結束後清理 traceId
                TraceUtils.clearTrace();
            }
        };
    }
}
