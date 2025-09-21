package com.cheng.common.enums;

/**
 * 限流類型
 *
 * @author cheng
 */

public enum LimitType {
    /**
     * 預設策略全域限流
     */
    DEFAULT,

    /**
     * 根據請求者IP進行限流
     */
    IP
}
