package com.cheng.common.core.domain.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 使用者登入物件
 *
 * @author cheng
 */
@Setter
@Getter
public class LoginBody {
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

}
