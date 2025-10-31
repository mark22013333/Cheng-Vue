package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 推播對象類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum TargetType {

    /**
     * 單一使用者
     */
    SINGLE("SINGLE", "單一使用者"),

    /**
     * 多個使用者
     */
    MULTIPLE("MULTIPLE", "多個使用者"),

    /**
     * 標籤群組（依標籤推播）
     */
    TAG("TAG", "標籤群組"),

    /**
     * 全部使用者（廣播）
     */
    ALL("ALL", "全部使用者");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得推播對象類型
     *
     * @param code 對象類型代碼
     * @return 對象類型
     */
    public static TargetType fromCode(String code) {
        for (TargetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的推播對象類型: " + code);
    }
}
