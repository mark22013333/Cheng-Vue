package com.cheng.common.exception.user;

/**
 * 驗證碼失效異常類
 *
 * @author cheng
 */
public class CaptchaExpireException extends UserException
{
    private static final long serialVersionUID = 1L;

    public CaptchaExpireException()
    {
        super("user.jcaptcha.expire", null);
    }
}
