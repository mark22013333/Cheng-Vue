package com.cheng.common.annotation;

import java.lang.annotation.*;

/**
 * 數據權限過濾註解
 *
 * @author cheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 部門表的別名
     */
    String deptAlias() default "";

    /**
     * 使用者表的別名
     */
    String userAlias() default "";

    /**
     * 權限字串（用於多個角色匹配符合要求的權限）預設根據權限註解@ss取得，多個權限用逗號分隔開來
     */
    String permission() default "";
}
