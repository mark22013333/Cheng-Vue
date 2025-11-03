package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 綁定狀態列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "UNBOUND", "BOUND"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum BindStatus {

    /**
     * 未綁定
     */
    UNBOUND("未綁定"),

    /**
     * 已綁定
     */
    BOUND("已綁定");

    private final String description;

    /**
     * 取得狀態代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："BOUND"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 狀態代碼（Enum 名稱）
     * @return 綁定狀態
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static BindStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);  // 直接使用 Java Enum 的 valueOf()
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的綁定狀態: " + code, e);
        }
    }
}
