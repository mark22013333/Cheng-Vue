package com.cheng.common.annotation;

import java.lang.annotation.*;

/**
 * 自定義註解防止表單重複提交
 *
 * @author cheng
 *
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {
    /**
     * 間隔時間(ms)，小於此時間視為重複提交
     */
    int interval() default 5000;

    /**
     * 提示訊息
     */
    String message() default "不允許重複提交，請稍候再試";
}
