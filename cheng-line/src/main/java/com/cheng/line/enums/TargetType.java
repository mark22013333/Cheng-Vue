package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 推播對象類型列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "SINGLE", "MULTIPLE", "TAG", "ALL"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum TargetType {

    /**
     * 單一使用者
     */
    SINGLE("單一使用者"),

    /**
     * 多個使用者
     */
    MULTIPLE("多個使用者"),

    /**
     * 標籤群組（依標籤推播）
     */
    TAG("標籤群組"),

    /**
     * 全部使用者（廣播）
     */
    ALL("全部使用者");

    private final String description;

    /**
     * 取得類型代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："SINGLE"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 對象類型代碼（Enum 名稱）
     * @return 對象類型
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static TargetType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的推播對象類型: " + code, e);
        }
    }
}
