package com.cheng.common.exception;

import java.io.Serial;

/**
 * 全域異常
 *
 * @author cheng
 */
public class GlobalException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 錯誤提示
     */
    private String message;

    /**
     * 錯誤明細，内部調試錯誤
     * <p>
     * 和 {@link CommonResult#getDetailMessage()} 一致的設計
     */
    private String detailMessage;

    /**
     * 空建構方法，避免反序列化問題
     */
    public GlobalException() {
    }

    public GlobalException(String message) {
        this.message = message;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public GlobalException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public GlobalException setMessage(String message) {
        this.message = message;
        return this;
    }
}