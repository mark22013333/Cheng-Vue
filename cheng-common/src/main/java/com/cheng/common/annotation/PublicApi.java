package com.cheng.common.annotation;

import java.lang.annotation.*;

/**
 * 公開 API 註解（不套用後台 /cadm 前綴）
 *
 * @author cheng
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {
}
