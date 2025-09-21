package com.cheng.framework.manager;

import com.cheng.common.utils.Threads;
import com.cheng.common.utils.spring.SpringUtils;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 異步任務管理器
 *
 * @author cheng
 */
public class AsyncManager {
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    private static final AsyncManager me = new AsyncManager();
    /**
     * 異步操作任務呼叫執行緒池
     */
    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    /**
     * 單例模式
     */
    private AsyncManager() {
    }

    public static AsyncManager me() {
        return me;
    }

    /**
     * 執行任務
     *
     * @param task 任務
     */
    public void execute(TimerTask task) {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止任務執行緒池
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(executor);
    }
}
