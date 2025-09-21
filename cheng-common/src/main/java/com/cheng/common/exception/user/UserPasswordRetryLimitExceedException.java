package com.cheng.common.exception.user;

import java.io.Serial;

/**
 * 使用者錯誤最大次數異常類
 *
 * @author cheng
 */
public class UserPasswordRetryLimitExceedException extends UserException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserPasswordRetryLimitExceedException(int retryLimitCount, int lockTime) {
        super("user.password.retry.limit.exceed", new Object[]{retryLimitCount, lockTime});
    }
}
