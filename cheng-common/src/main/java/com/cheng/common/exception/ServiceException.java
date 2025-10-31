package com.cheng.common.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 業務異常
 *
 * @author cheng
 */
@Getter
public final class ServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 錯誤碼
     */
    private Integer code;

    /**
     * 錯誤提示
     */
    private String message;

    /**
     * 錯誤明細，内部調試錯誤
     * <p>
     * 和 {@see CommonResult#getDetailMessage()} 一致的設計
     */
    private String detailMessage;

    /**
     * 空建構方法，避免反序列化問題
     */
    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}