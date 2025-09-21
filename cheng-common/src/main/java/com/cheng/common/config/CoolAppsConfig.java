package com.cheng.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讀取專案相關配置
 *
 * @author cheng
 */
@Component
@ConfigurationProperties(prefix = "cheng")
public class CoolAppsConfig {
    /**
     * 上傳路徑
     */
    private static String profile;
    /**
     * 取得地址開關
     */
    private static boolean addressEnabled;
    /**
     * 驗證碼類型
     */
    private static String captchaType;
    /**
     * 專案名稱
     */
    private String name;
    /**
     * 版本
     */
    private String version;
    /**
     * 版權年份
     */
    private String copyrightYear;

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        CoolAppsConfig.profile = profile;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        CoolAppsConfig.addressEnabled = addressEnabled;
    }

    /**
     * 取得匯入上傳路徑
     */
    public static String getImportPath() {
        return getProfile() + "/import";
    }

    /**
     * 取得頭像上傳路徑
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 取得下載路徑
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 取得上傳路徑
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCopyrightYear() {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    public void setCaptchaType(String captchaType) {
        CoolAppsConfig.captchaType = captchaType;
    }
}
