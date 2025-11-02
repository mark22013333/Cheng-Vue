package com.cheng.framework.manager;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 確認服務停止時能關閉後台執行緒
 *
 * @author cheng
 */
@Slf4j
@Component
public class ShutdownManager {

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止非同步執行任務
     */
    private void shutdownAsyncManager() {
        try {
            log.info("====關閉後台任務執行緒池====");
            AsyncManager.me().shutdown();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
