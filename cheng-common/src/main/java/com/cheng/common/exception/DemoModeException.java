package com.cheng.common.exception;

import java.io.Serial;

/**
 * 展示模式異常
 *
 * @author cheng
 */
public class DemoModeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DemoModeException() {
    }
}
