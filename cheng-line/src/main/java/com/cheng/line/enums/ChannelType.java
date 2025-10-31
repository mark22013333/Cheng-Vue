package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE 頻道類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ChannelType {

    /**
     * 主頻道（正式環境使用）
     */
    MAIN("MAIN", "主頻道"),

    /**
     * 副頻道（備用頻道）
     */
    SUB("SUB", "副頻道"),

    /**
     * 測試頻道（測試環境使用）
     */
    TEST("TEST", "測試頻道");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得頻道類型
     *
     * @param code 頻道代碼
     * @return 頻道類型
     */
    public static ChannelType fromCode(String code) {
        for (ChannelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的頻道類型: " + code);
    }

    /**
     * 驗證頻道代碼是否有效
     *
     * @param code 頻道代碼
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (ChannelType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
