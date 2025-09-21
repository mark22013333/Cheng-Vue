package com.cheng.common.exception.user;

import java.io.Serial;

/**
 * 驗證碼錯誤異常類
 *
 * @author cheng
 */
public class CaptchaException extends UserException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CaptchaException() {
        super("user.jcaptcha.error", null);
    }
}
