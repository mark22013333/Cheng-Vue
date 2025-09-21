package com.cheng.common.constant;

import io.jsonwebtoken.Claims;

import java.util.Locale;

/**
 * 共用常量訊息
 *
 * @author cheng
 */
public class Constants {
    /**
     * UTF-8 字串集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字串集
     */
    public static final String GBK = "GBK";

    /**
     * 系統語言
     */
    public static final Locale DEFAULT_LOCALE = Locale.TRADITIONAL_CHINESE;

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http請求
     */
    public static final String HTTP = "http://";

    /**
     * https請求
     */
    public static final String HTTPS = "https://";

    /**
     * 共用成功標識
     */
    public static final String SUCCESS = "0";

    /**
     * 共用失敗標識
     */
    public static final String FAIL = "1";

    /**
     * 登入成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 註銷
     */
    public static final String LOGOUT = "Logout";

    /**
     * 註冊
     */
    public static final String REGISTER = "Register";

    /**
     * 登入失敗
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 所有權限標識
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 管理員角色權限標識
     */
    public static final String SUPER_ADMIN = "admin";

    /**
     * 角色權限分隔符號
     */
    public static final String ROLE_DELIMETER = ",";

    /**
     * 權限標識分隔符號
     */
    public static final String PERMISSION_DELIMETER = ",";

    /**
     * 驗證碼有效期（分鐘）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前綴
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前綴
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 使用者ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * 使用者名稱
     */
    public static final String JWT_USERNAME = Claims.SUBJECT;

    /**
     * 使用者頭像
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * 建立時間
     */
    public static final String JWT_CREATED = "created";

    /**
     * 使用者權限
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * 資源映射路徑 前綴
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 遠端方法呼叫
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 遠端方法呼叫
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 遠端方法呼叫
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 自動識别json物件白名單配置（僅允許解析的包名，範圍越小越安全）
     */
    public static final String[] JSON_WHITELIST_STR = {"com.cheng"};

    /**
     * 定時任務白名單配置（僅允許訪問的包名，如其他需要可以自行新增）
     */
    public static final String[] JOB_WHITELIST_STR = {"com.cheng.quartz.task"};

    /**
     * 定時任務違規的字串
     */
    public static final String[] JOB_ERROR_STR = {"java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.cheng.common.utils.file", "com.cheng.common.config", "com.cheng.generator"};
}
