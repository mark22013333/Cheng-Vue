package com.cheng.common.utils;

/**
 * 去識別化工具類
 *
 * @author cheng
 */
public class DesensitizedUtil {
    /**
     * 密碼的全部字串都用*代替，比如：******
     *
     * @param password 密碼
     * @return 去識別化後的密碼
     */
    public static String password(String password) {
        if (StringUtils.isBlank(password)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.repeat('*', password.length());
    }

    /**
     * 車牌中間用*代替，如果是錯誤的車牌，不處理
     *
     * @param carLicense 完整的車牌號
     * @return 去識別化後的車牌
     */
    public static String carLicense(String carLicense) {
        if (StringUtils.isBlank(carLicense)) {
            return StringUtils.EMPTY;
        }
        // 普通車牌
        if (carLicense.length() == 7) {
            carLicense = StringUtils.hide(carLicense, 3, 6);
        } else if (carLicense.length() == 8) {
            // 新能源車牌
            carLicense = StringUtils.hide(carLicense, 3, 7);
        }
        return carLicense;
    }
}
