package com.cheng.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 執行緒相關工具類.
 *
 * @author cheng
 */
@Slf4j
public class Threads {

    /**
     * 停止執行緒池
     * 先使用shutdown, 停止接收新任務並嘗試完成所有已存在任務.
     * 如果逾時, 則呼叫shutdownNow, 取消在workQueue中Pending的任務,並中斷所有阻塞函數.
     * 如果仍然逾時，則強制登出.
     * 另對在shutdown時執行緒本身被呼叫中斷做了處理.
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        log.info("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 印出執行緒異常訊息
     */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?> future) {
            try {
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            log.error(t.getMessage(), t);
        }
    }
}
