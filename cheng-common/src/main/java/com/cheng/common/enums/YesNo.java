package com.cheng.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用是否列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum YesNo {

    /**
     * 否
     */
    NO(0, "否"),

    /**
     * 是
     */
    YES(1, "是");

    private final Integer code;
    private final String description;

    /**
     * 根據代碼取得是否值
     *
     * @param code 代碼
     * @return 是否
     */
    public static YesNo fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (YesNo yesNo : values()) {
            if (yesNo.code.equals(code)) {
                return yesNo;
            }
        }
        throw new IllegalArgumentException("未知的是否代碼: " + code);
    }

    /**
     * 判斷是否為「是」
     *
     * @return true=是, false=否
     */
    public boolean isYes() {
        return this == YES;
    }

    /**
     * 判斷是否為「否」
     *
     * @return true=否, false=是
     */
    public boolean isNo() {
        return this == NO;
    }

    /**
     * 從 Boolean 轉換為 YesNoEnum
     *
     * @param value Boolean 值
     * @return YES 或 NO
     */
    public static YesNo fromBoolean(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? YES : NO;
    }

    /**
     * 轉換為 Boolean
     *
     * @return true 或 false
     */
    public Boolean toBoolean() {
        return this == YES;
    }
}
