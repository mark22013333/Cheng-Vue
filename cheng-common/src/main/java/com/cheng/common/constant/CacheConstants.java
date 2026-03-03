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

    /**
     * 密碼重設發送頻率限制 redis key（TTL 60 秒）
     */
    public static final String PWD_RESET_LIMIT_KEY = "pwd_reset_limit:";

    /**
     * 密碼重設 Email 每小時發送次數 redis key（TTL 1 小時）
     */
    public static final String PWD_RESET_HOURLY_KEY = "pwd_reset_hourly:";

    /**
     * 密碼重設每日發送次數 redis key（TTL 到當天結束）
     */
    public static final String PWD_RESET_DAILY_KEY = "pwd_reset_daily:";

    /**
     * 會員密碼變更時間 redis key（用於 JWT 強制失效檢查，TTL 7 天）
     */
    public static final String MEMBER_PWD_CHANGED_KEY = "member_pwd_changed:";

    /**
     * Email 驗證發送頻率限制 redis key（TTL 60 秒）
     */
    public static final String EMAIL_VERIFY_LIMIT_KEY = "email_verify_limit:";

    /**
     * Email 驗證每小時發送次數 redis key（TTL 1 小時）
     */
    public static final String EMAIL_VERIFY_HOURLY_KEY = "email_verify_hourly:";

    /**
     * Email 驗證每日發送次數 redis key（TTL 到當天結束）
     */
    public static final String EMAIL_VERIFY_DAILY_KEY = "email_verify_daily:";
}
