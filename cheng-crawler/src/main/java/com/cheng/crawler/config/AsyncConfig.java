package com.cheng.crawler.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 異步任務配置
 *
 * @author cheng
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 爬取任務執行器
     * 最多同時執行 5 個任務
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心執行緒數
        executor.setCorePoolSize(3);
        // 最大執行緒數
        executor.setMaxPoolSize(5);
        // 佇列容量
        executor.setQueueCapacity(20);
        // 執行緒名稱前綴
        executor.setThreadNamePrefix("crawl-task-");
        // 設定 TaskDecorator 以複製 MDC context（包含 trace id）
        executor.setTaskDecorator(new MdcTaskDecorator());
        // 拒絕策略：由呼叫執行緒處理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 執行緒空閒時間（秒）
        executor.setKeepAliveSeconds(60);
        // 等待所有任務結束後再關閉執行緒池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待時間（秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("爬取任務執行器初始化完成: corePoolSize=3, maxPoolSize=5, queueCapacity=20");

        return executor;
    }

    /**
     * MDC TaskDecorator - 複製 MDC context 到異步執行緒
     */
    private static class MdcTaskDecorator implements TaskDecorator {
        @NotNull
        @Override
        public Runnable decorate(@NotNull Runnable runnable) {
            // 取得當前執行緒的 MDC context（包含 trace id）
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                try {
                    // 設定 MDC context 到異步執行緒
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // 執行原始任務
                    runnable.run();
                } finally {
                    // 清理 MDC context
                    MDC.clear();
                }
            };
        }
    }
}
