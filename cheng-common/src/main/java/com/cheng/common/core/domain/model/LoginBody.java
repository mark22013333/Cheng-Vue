package com.cheng.common.core.domain.model;

/**
 * 使用者登入物件
 *
 * @author cheng
 */
public class LoginBody
{
    /**
     * 使用者名
     */
    private String username;

    /**
     * 使用者密碼
     */
    private String password;

    /**
     * 驗證碼
     */
    private String code;

    /**
     * 唯一標識
     */
    private String uuid;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
