package com.cheng.framework.manager;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 確認應用登出時能關閉後台執行緒
 *
 * @author cheng
 */
@Component
public class ShutdownManager {
    private static final Logger logger = LoggerFactory.getLogger("sys-user");

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止異步執行任務
     */
    private void shutdownAsyncManager() {
        try {
            logger.info("====關閉後台任務任務執行緒池====");
            AsyncManager.me().shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
