package com.cheng.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讀取專案相關設定
 *
 * @author cheng
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cheng")
public class CoolAppsConfig {
    /**
     * 上傳路徑
     */
    @Getter
    private static String profile;
    /**
     * 取得地址開關
     */
    @Getter
    private static boolean addressEnabled;
    /**
     * 驗證碼類型
     */
    @Getter
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

    public void setProfile(String profile) {
        CoolAppsConfig.profile = profile;
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

    /**
     * 取得 Rich Menu 上傳路徑
     */
    public static String getRichMenuPath() {
        return getProfile() + "/upload/richmenu";
    }

    public static String getMaterialPath() {
        return getProfile() + "/material";
    }

    /**
     * 取得 Imagemap 上傳路徑
     */
    public static String getImagemapPath() {
        return getProfile() + "/imagemap";
    }

    /**
     * 取得下載檔案路徑（未來使用）
     */
    public static String getDownloadFilePath() {
        return getProfile() + "/download";
    }

    /**
     * 取得商城上傳路徑
     */
    public static String getShopUploadPath() {
        return getProfile() + "/upload/shopmall";
    }

    /**
     * 取得商城分類上傳路徑
     *
     * @param category 分類名稱（product, banner, category 等）
     */
    public static String getShopUploadPath(String category) {
        if (category == null || category.isBlank()) {
            return getShopUploadPath();
        }
        return getProfile() + "/upload/shopmall/" + category;
    }

    public void setCaptchaType(String captchaType) {
        CoolAppsConfig.captchaType = captchaType;
    }
}
