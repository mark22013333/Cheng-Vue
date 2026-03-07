package com.cheng.shop.service;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 會員密碼政策服務
 * <p>
 * 統一管理密碼長度與複雜度規則，供註冊和密碼重設共用。
 * 所有規則透過系統參數 (shop.password.*) 動態設定。
 * </p>
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopPasswordPolicyService {

    private final ShopConfigService configService;

    /** 最小長度下限（防止管理員設為不安全的值） */
    private static final int MIN_LENGTH_FLOOR = 6;

    /** 最大長度上限 */
    private static final int MAX_LENGTH_CEILING = 128;

    // ==================== 讀取政策 ====================

    public int getMinLength() {
        return Math.max(configService.getInt(ShopConfigKey.PWD_MIN_LENGTH), MIN_LENGTH_FLOOR);
    }

    public int getMaxLength() {
        return Math.min(configService.getInt(ShopConfigKey.PWD_MAX_LENGTH), MAX_LENGTH_CEILING);
    }

    public boolean isUppercaseRequired() {
        return configService.getBoolean(ShopConfigKey.PWD_REQUIRE_UPPERCASE);
    }

    public boolean isLowercaseRequired() {
        return configService.getBoolean(ShopConfigKey.PWD_REQUIRE_LOWERCASE);
    }

    public boolean isDigitRequired() {
        return configService.getBoolean(ShopConfigKey.PWD_REQUIRE_DIGIT);
    }

    public boolean isSpecialRequired() {
        return configService.getBoolean(ShopConfigKey.PWD_REQUIRE_SPECIAL);
    }

    // ==================== 驗證 ====================

    /**
     * 驗證密碼是否符合政策
     *
     * @param password 明文密碼
     * @throws ServiceException 不符合規則時拋出，訊息包含具體原因
     */
    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new ServiceException("密碼不可為空");
        }

        int minLen = getMinLength();
        int maxLen = getMaxLength();

        if (password.length() < minLen || password.length() > maxLen) {
            throw new ServiceException("密碼長度須為 " + minLen + "-" + maxLen + " 字元");
        }

        List<String> missing = new ArrayList<>(4);
        if (isUppercaseRequired() && !password.chars().anyMatch(Character::isUpperCase)) {
            missing.add("大寫英文字母");
        }
        if (isLowercaseRequired() && !password.chars().anyMatch(Character::isLowerCase)) {
            missing.add("小寫英文字母");
        }
        if (isDigitRequired() && !password.chars().anyMatch(Character::isDigit)) {
            missing.add("數字");
        }
        if (isSpecialRequired() && password.chars().allMatch(c -> Character.isLetterOrDigit(c))) {
            missing.add("特殊符號");
        }

        if (!missing.isEmpty()) {
            throw new ServiceException("密碼須包含：" + String.join("、", missing));
        }
    }

    // ==================== API 資料組裝 ====================

    /**
     * 組裝所有規則資料（給 API 回傳用）
     */
    public Map<String, Object> getPolicyMap() {
        Map<String, Object> map = new LinkedHashMap<>(8);
        map.put("minLength", getMinLength());
        map.put("maxLength", getMaxLength());
        map.put("requireUppercase", isUppercaseRequired());
        map.put("requireLowercase", isLowercaseRequired());
        map.put("requireDigit", isDigitRequired());
        map.put("requireSpecial", isSpecialRequired());
        return map;
    }
}
