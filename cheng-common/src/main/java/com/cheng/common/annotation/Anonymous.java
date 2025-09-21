package com.cheng.common.annotation;

import java.lang.annotation.*;

/**
 * 匿名訪問不鑑權註解
 *
 * @author cheng
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous
{
}
