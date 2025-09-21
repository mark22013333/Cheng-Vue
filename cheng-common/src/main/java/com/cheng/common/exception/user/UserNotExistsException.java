package com.cheng.common.exception.user;

import java.io.Serial;

/**
 * 使用者不存在異常類
 *
 * @author cheng
 */
public class UserNotExistsException extends UserException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotExistsException() {
        super("user.not.exists", null);
    }
}
