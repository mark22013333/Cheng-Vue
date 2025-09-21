package com.cheng.common.exception.user;

import com.cheng.common.exception.base.BaseException;

import java.io.Serial;

/**
 * 使用者訊息異常類
 *
 * @author cheng
 */
public class UserException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args) {
        super("user", code, args, null);
    }
}
