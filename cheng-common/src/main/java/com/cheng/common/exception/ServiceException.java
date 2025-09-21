package com.cheng.common.exception;

/**
 * 業務異常
 *
 * @author cheng
 */
public final class ServiceException extends RuntimeException
{
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
     *
     * 和 {@link CommonResult#getDetailMessage()} 一致的設計
     */
    private String detailMessage;

    /**
     * 空建構方法，避免反序列化問題
     */
    public ServiceException()
    {
    }

    public ServiceException(String message)
    {
        this.message = message;
    }

    public ServiceException(String message, Integer code)
    {
        this.message = message;
        this.code = code;
    }

    public String getDetailMessage()
    {
        return detailMessage;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public Integer getCode()
    {
        return code;
    }

    public ServiceException setMessage(String message)
    {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage)
    {
        this.detailMessage = detailMessage;
        return this;
    }
}