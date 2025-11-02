package com.cheng.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Quartz 定時任務 TraceId 切面。
 * <p>
 * 為所有 Quartz 任務自動產生帶有任務識別的 traceId。
 * <p>
 * 注意：此切面已停用，traceId 現在由 {@link AbstractQuartzJob} 直接處理，
 * 可確保任務執行完成後的 job log 也能正確包含 traceId。
 *
 * @author cheng
 * @since 2025-11-02
 * @deprecated 自 2025-11-02 起廢棄，請改用 {@link AbstractQuartzJob} 中的 traceId 處理機制。
 */
@Slf4j
@Aspect
@Deprecated(since = "2025-11-02", forRemoval = true)
// @Component // 已停用，traceId 改在 AbstractQuartzJob 中處理
public class QuartzTraceAspect {

    /**
     * 定義切入點：攔截 com.cheng.quartz.task 包下所有 public 方法
     */
    @Pointcut("execution(public * com.cheng.quartz.task..*.*(..))")
    public void quartzTaskPointcut() {
    }

    /**
     * 環繞通知：在 Quartz 任務執行前產生 traceId，執行後清理
     */
    @Around("quartzTaskPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 嘗試從參數中提取任務識別資訊
        String taskIdentifier = extractTaskIdentifier(className, methodName, args);

        try {
            // 為 Quartz 任務產生 traceId，使用任務識別作為前綴
            TraceUtils.initTrace(taskIdentifier);
            String traceId = TraceUtils.getTraceId();

            log.debug("Quartz 任務開始執行 [{}:{}]，任務識別: {}, traceId: {}",
                    className, methodName, taskIdentifier, traceId);

            // 執行 Quartz 任務
            Object result = joinPoint.proceed();

            log.debug("Quartz 任務執行完成 [{}:{}]，traceId: {}", className, methodName, traceId);
            return result;

        } catch (Exception e) {
            log.error("Quartz 任務執行異常 [{}:{}]，traceId: {}",
                    className, methodName, TraceUtils.getTraceId(), e);
            throw e;
        } finally {
            // 任務結束後清理 traceId
            TraceUtils.clearTrace();
        }
    }

    /**
     * 從方法參數中提取任務識別資訊
     *
     * @param className  類別名稱
     * @param methodName 方法名稱
     * @param args       方法參數
     * @return 任務識別字串
     */
    private String extractTaskIdentifier(String className, String methodName, Object[] args) {
        StringBuilder identifier = new StringBuilder("QUARTZ");

        // 1. 如果是 CrawlerTask，嘗試提取 crawlerType
        if ("CrawlerTask".equals(className)) {
            String crawlerType = extractCrawlerType(args);
            if (crawlerType != null) {
                identifier.append("_").append(crawlerType);
                return identifier.toString();
            }
        }

        // 2. 使用類別名稱作為識別
        identifier.append("_").append(className);

        // 3. 如果有簡單的字串參數，加入第一個參數作為識別
        if (args != null && args.length > 0 && args[0] != null) {
            String firstArg = args[0].toString();
            // 限制長度，避免 traceId 過長
            if (firstArg.length() <= 20) {
                // 移除特殊字元，只保留字母數字和底線
                firstArg = firstArg.replaceAll("[^a-zA-Z0-9_]", "");
                if (!firstArg.isEmpty()) {
                    identifier.append("_").append(firstArg);
                }
            }
        }

        return identifier.toString();
    }

    /**
     * 從參數中提取爬蟲類型
     *
     * @param args 方法參數
     * @return 爬蟲類型，如果無法提取則返回 null
     */
    private String extractCrawlerType(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        // 第一個參數是字串
        if (args[0] instanceof String firstArg) {

            // 情況 1: JSON 字串，包含 crawlerType
            if (firstArg.trim().startsWith("{")) {
                try {
                    JSONObject json = JSON.parseObject(firstArg);
                    if (json.containsKey("crawlerType")) {
                        return json.getString("crawlerType");
                    }
                } catch (Exception e) {
                    // 不是有效的 JSON，繼續其他方式
                }
            }

            // 情況 2: 直接是爬蟲類型代碼（如 CA102, CA103）
            if (firstArg.matches("^[A-Z]{2}\\d{3}$")) {
                return firstArg;
            }
        }

        return null;
    }
}
