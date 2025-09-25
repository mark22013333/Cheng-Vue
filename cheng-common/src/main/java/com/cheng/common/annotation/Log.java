package com.cheng.common.annotation;

import com.cheng.common.enums.BusinessType;
import com.cheng.common.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 自定義操作日誌記錄註解
 *
 * @author cheng
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模組
     */
    String title() default "";

    /**
     * 功能
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人類別
     */
    OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否儲存請求的參數
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否儲存響應的參數
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除指定的請求參數
     */
    String[] excludeParamNames() default {};
}
