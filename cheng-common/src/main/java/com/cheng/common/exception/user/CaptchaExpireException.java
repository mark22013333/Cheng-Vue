package com.cheng.common.exception.user;

import java.io.Serial;

/**
 * 驗證碼失效異常類
 *
 * @author cheng
 */
public class CaptchaExpireException extends UserException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CaptchaExpireException() {
        super("user.jcaptcha.expire", null);
    }
}
