package com.cheng.common.constant;

/**
 * 暫存的key 常數
 *
 * @author cheng
 */
public class CacheConstants {
    /**
     * 登入使用者 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 商城會員登入 token redis key
     */
    public static final String MEMBER_LOGIN_TOKEN_KEY = "member_login_tokens:";

    /**
     * 驗證碼 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 參數管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登入帳號密碼錯誤次數 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
