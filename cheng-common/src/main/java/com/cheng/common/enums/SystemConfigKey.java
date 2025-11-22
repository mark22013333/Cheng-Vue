package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 系統配置鍵列舉
 * <p>
 * 統一管理 sys_config 表中的所有配置鍵，避免魔術字串散落各處
 * </p>
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum SystemConfigKey implements CodedEnum<String> {

    // ==================== 主框架頁配置 ====================
    /**
     * 主框架頁-預設介面樣式名稱
     * 可選值：skin-blue、skin-green、skin-purple、skin-red、skin-yellow
     */
    INDEX_SKIN_NAME("sys.index.skinName", "主框架頁-預設介面樣式名稱"),

    /**
     * 主框架頁-側邊欄主題
     * 可選值：theme-dark（深色主題）、theme-light（淺色主題）
     */
    INDEX_SIDE_THEME("sys.index.sideTheme", "主框架頁-側邊欄主題"),

    // ==================== 使用者管理配置 ====================
    /**
     * 使用者管理-帳號初始密碼
     * 預設值：123456
     */
    USER_INIT_PASSWORD("sys.user.initPassword", "使用者管理-帳號初始密碼"),

    /**
     * 使用者管理-初始密碼修改策略
     * 0：關閉策略，無任何提示
     * 1：提醒使用者，登入時提醒修改密碼
     */
    ACCOUNT_INIT_PASSWORD_MODIFY("sys.account.initPasswordModify", "使用者管理-初始密碼修改策略"),

    /**
     * 使用者管理-帳號密碼更新週期（天數）
     * 0：不限制
     * 1-365：指定天數，超過後登入時提醒修改密碼
     */
    ACCOUNT_PASSWORD_VALIDATE_DAYS("sys.account.passwordValidateDays", "使用者管理-帳號密碼更新週期"),

    // ==================== 帳號自助配置 ====================
    /**
     * 帳號自助-驗證碼開關
     * true：開啟驗證碼功能
     * false：關閉驗證碼功能
     */
    ACCOUNT_CAPTCHA_ENABLED("sys.account.captchaEnabled", "帳號自助-驗證碼開關"),

    /**
     * 帳號自助-是否開啟使用者註冊功能
     * true：開啟註冊功能
     * false：關閉註冊功能
     */
    ACCOUNT_REGISTER_USER("sys.account.registerUser", "帳號自助-是否開啟使用者註冊功能"),

    // ==================== 登入安全配置 ====================
    /**
     * 使用者登入-黑名單列表
     * 多個 IP 以分號分隔，支援通配符（*）和網段
     * 範例：192.168.1.*;10.0.0.0/24
     */
    LOGIN_BLACK_IP_LIST("sys.login.blackIPList", "使用者登入-黑名單列表");

    /**
     * 配置鍵（對應 sys_config.config_key）
     */
    private final String code;

    /**
     * 配置描述（對應 sys_config.config_name）
     */
    private final String description;

    /**
     * 根據配置鍵取得對應的列舉
     *
     * @param configKey 配置鍵
     * @return 系統配置鍵列舉
     * @throws IllegalArgumentException 找不到對應的配置鍵時拋出
     */
    public static SystemConfigKey fromCode(String configKey) {
        return EnumUtils.fromCodeOrThrow(SystemConfigKey.class, configKey);
    }

    /**
     * 根據配置鍵取得對應的列舉（找不到返回 null）
     *
     * @param configKey 配置鍵
     * @return 系統配置鍵列舉，找不到返回 null
     */
    public static SystemConfigKey fromCodeOrNull(String configKey) {
        return EnumUtils.fromCode(SystemConfigKey.class, configKey);
    }

    /**
     * 驗證配置鍵是否有效
     *
     * @param configKey 配置鍵
     * @return true=有效，false=無效
     */
    public static boolean isValidConfigKey(String configKey) {
        return EnumUtils.isValidCode(SystemConfigKey.class, configKey);
    }

    // ==================== 便捷判斷方法 ====================

    /**
     * 是否為介面樣式相關配置
     */
    public boolean isIndexConfig() {
        return this == INDEX_SKIN_NAME || this == INDEX_SIDE_THEME;
    }

    /**
     * 是否為使用者管理相關配置
     */
    public boolean isUserConfig() {
        return this == USER_INIT_PASSWORD
                || this == ACCOUNT_INIT_PASSWORD_MODIFY
                || this == ACCOUNT_PASSWORD_VALIDATE_DAYS;
    }

    /**
     * 是否為帳號自助相關配置
     */
    public boolean isAccountConfig() {
        return this == ACCOUNT_CAPTCHA_ENABLED || this == ACCOUNT_REGISTER_USER;
    }

    /**
     * 是否為登入安全相關配置
     */
    public boolean isLoginConfig() {
        return this == LOGIN_BLACK_IP_LIST;
    }
}
