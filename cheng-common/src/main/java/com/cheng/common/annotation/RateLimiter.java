package com.cheng.common.annotation;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.enums.LimitType;

import java.lang.annotation.*;

/**
 * 限流註解
 *
 * @author cheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter
{
    /**
     * 限流key
     */
    public String key() default CacheConstants.RATE_LIMIT_KEY;

    /**
     * 限流時間,單位秒
     */
    public int time() default 60;

    /**
     * 限流次數
     */
    public int count() default 100;

    /**
     * 限流類型
     */
    public LimitType limitType() default LimitType.DEFAULT;
}
