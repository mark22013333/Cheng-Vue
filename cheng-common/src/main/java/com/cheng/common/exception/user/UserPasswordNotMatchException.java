package com.cheng.common.exception.user;

/**
 * 使用者密碼不正確或不符合規範異常類
 *
 * @author cheng
 */
public class UserPasswordNotMatchException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserPasswordNotMatchException()
    {
        super("user.password.not.match", null);
    }
}
