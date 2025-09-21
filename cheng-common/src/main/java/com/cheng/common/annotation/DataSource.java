package com.cheng.common.annotation;

import com.cheng.common.enums.DataSourceType;

import java.lang.annotation.*;

/**
 * 自定義多資料來源切換註解
 * <p>
 * 優先級：先方法，後類，如果方法覆蓋了類上的資料來源類型，以方法的為準，否則以類上的為準
 *
 * @author cheng
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource {
    /**
     * 切換資料來源名稱
     */
    DataSourceType value() default DataSourceType.MASTER;
}
