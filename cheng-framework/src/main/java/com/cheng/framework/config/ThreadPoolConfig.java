package com.cheng.framework.config;

import com.cheng.common.utils.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 執行緒池配置
 *
 * @author cheng
 **/
@Configuration
public class ThreadPoolConfig {
    // 核心執行緒池大小
    private final int corePoolSize = 50;

    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 最大可建立的執行緒數
        executor.setMaxPoolSize(200);
        executor.setCorePoolSize(corePoolSize);
        // 佇列最大長度
        executor.setQueueCapacity(1000);
        // 執行緒池維護執行緒所允許的閒置時間
        executor.setKeepAliveSeconds(300);
        // 執行緒池對拒絕任務(無執行緒可用)的處理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 執行週期性或定時任務
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(corePoolSize,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
