package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE 頻道類型列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "MAIN", "SUB", "TEST"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ChannelType {

    /**
     * 主頻道（正式環境使用）
     */
    MAIN("主頻道"),

    /**
     * 副頻道（備用頻道）
     */
    SUB("副頻道"),

    /**
     * 測試頻道（測試環境使用）
     */
    TEST("測試頻道");

    private final String description;

    /**
     * 取得類型代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："MAIN"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 頻道代碼（Enum 名稱）
     * @return 頻道類型
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static ChannelType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的頻道類型: " + code, e);
        }
    }

    /**
     * 驗證頻道代碼是否有效
     *
     * @param code 頻道代碼
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        try {
            valueOf(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
