package com.cheng.framework.aspectj;

import com.cheng.common.utils.TraceUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 定時任務 TraceId 切面
 * 為標註 @Scheduled 的定時任務自動產生 traceId
 *
 * @author cheng
 * @since 2025-11-02
 */
@Aspect
@Component
public class ScheduledTraceAspect {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTraceAspect.class);

    /**
     * 定義切入點：攔截所有標註 @Scheduled 的方法
     */
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void scheduledPointcut() {
    }

    /**
     * 環繞通知：在定時任務執行前產生 traceId，執行後清理
     */
    @Around("scheduledPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            // 為定時任務產生 traceId，前綴使用 SCHEDULED
            TraceUtils.initTrace("SCHEDULED");
            log.debug("定時任務開始執行 [{}:{}]，traceId: {}", className, methodName, TraceUtils.getTraceId());

            // 執行定時任務
            Object result = joinPoint.proceed();

            log.debug("定時任務執行完成 [{}:{}]，traceId: {}", className, methodName, TraceUtils.getTraceId());
            return result;

        } catch (Exception e) {
            log.error("定時任務執行異常 [{}:{}]，traceId: {}", className, methodName, TraceUtils.getTraceId(), e);
            throw e;
        } finally {
            // 任務結束後清理 traceId
            TraceUtils.clearTrace();
        }
    }
}
