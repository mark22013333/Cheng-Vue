package com.cheng.web.controller.tool;

import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.utils.TraceUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TraceId 功能測試控制器
 * 用於測試各種場景下的 traceId 記錄功能
 *
 * @author cheng
 * @since 2025-11-02
 */
@RestController
@RequestMapping("/tool/trace")
public class TraceTestController {

    private static final Logger log = LoggerFactory.getLogger(TraceTestController.class);

    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private AsyncTestService asyncTestService;

    /**
     * 測試普通 HTTP 請求的 traceId
     * 訪問: GET /tool/trace/test-http
     */
    @GetMapping("/test-http")
    public AjaxResult testHttp() {
        String traceId = TraceUtils.getTraceId();
        log.info("=== HTTP 請求測試 ===");
        log.info("收到 HTTP 請求，traceId: {}", traceId);

        Map<String, Object> data = new HashMap<>();
        data.put("traceId", traceId);
        data.put("message", "HTTP 請求測試成功，請查看日誌中的 traceId");

        return AjaxResult.success(data);
    }

    /**
     * 測試非同步任務的 traceId 傳遞
     * 訪問: GET /tool/trace/test-async
     */
    @GetMapping("/test-async")
    public AjaxResult testAsync() {
        String parentTraceId = TraceUtils.getTraceId();
        log.info("=== 非同步任務測試 ===");
        log.info("父執行緒 traceId: {}", parentTraceId);

        // 使用 @Async 註解的非同步方法
        asyncTestService.asyncMethod();

        // 使用執行緒池的非同步任務
        executor.execute(() -> {
            log.info("執行緒池非同步任務執行，traceId: {}", TraceUtils.getTraceId());
        });

        Map<String, Object> data = new HashMap<>();
        data.put("parentTraceId", parentTraceId);
        data.put("message", "非同步任務已提交，請查看日誌確認子執行緒的 traceId 與父執行緒相同");

        return AjaxResult.success(data);
    }

    /**
     * 測試異常情況的 traceId 記錄
     * 訪問: GET /tool/trace/test-exception
     */
    @GetMapping("/test-exception")
    public AjaxResult testException() {
        String traceId = TraceUtils.getTraceId();
        log.info("=== 異常測試 ===");
        log.info("測試異常記錄，traceId: {}", traceId);

        try {
            // 故意製造一個異常
            int result = 10 / 0;
        } catch (Exception e) {
            log.error("捕獲異常，traceId 應該會記錄在異常堆疊中", e);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("traceId", traceId);
        data.put("message", "異常測試完成，請查看日誌中的完整異常堆疊和 traceId");

        return AjaxResult.success(data);
    }

    /**
     * 測試多層呼叫的 traceId 傳遞
     * 訪問: GET /tool/trace/test-nested
     */
    @GetMapping("/test-nested")
    public AjaxResult testNested() {
        String traceId = TraceUtils.getTraceId();
        log.info("=== 多層呼叫測試 ===");
        log.info("第一層呼叫，traceId: {}", traceId);

        // 呼叫第二層
        secondLevelMethod();

        Map<String, Object> data = new HashMap<>();
        data.put("traceId", traceId);
        data.put("message", "多層呼叫測試完成，請查看日誌確認所有層級的 traceId 相同");

        return AjaxResult.success(data);
    }

    private void secondLevelMethod() {
        log.info("第二層呼叫，traceId: {}", TraceUtils.getTraceId());
        thirdLevelMethod();
    }

    private void thirdLevelMethod() {
        log.info("第三層呼叫，traceId: {}", TraceUtils.getTraceId());
    }

    /**
     * 非同步測試服務
     */
    @Component
    public static class AsyncTestService {
        private static final Logger log = LoggerFactory.getLogger(AsyncTestService.class);

        @Async("threadPoolTaskExecutor")
        public void asyncMethod() {
            String traceId = TraceUtils.getTraceId();
            log.info("@Async 非同步方法執行，traceId: {}", traceId);

            try {
                Thread.sleep(100);
                log.info("@Async 非同步方法完成，traceId: {}", traceId);
            } catch (InterruptedException e) {
                log.error("非同步方法被中斷", e);
            }
        }
    }

    /**
     * 定時任務測試（每5分鐘執行一次，僅用於演示）
     * 注意：預設是關閉的，如需測試請取消註解
     */
    @Component
    public static class ScheduledTestTask {
        private static final Logger log = LoggerFactory.getLogger(ScheduledTestTask.class);

        // @Scheduled(cron = "0 */5 * * * ?")  // 每5分鐘執行一次
        public void scheduledTask() {
            log.info("=== 定時任務測試 ===");
            log.info("定時任務執行，traceId: {}", TraceUtils.getTraceId());
        }
    }
}
