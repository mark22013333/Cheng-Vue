package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE Rich Menu 狀態列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "DRAFT", "ACTIVE", "INACTIVE"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum RichMenuStatus {

    /**
     * 草稿（尚未發布到 LINE 平台）
     */
    DRAFT("草稿"),

    /**
     * 啟用（已發布到 LINE 平台且正在使用）
     */
    ACTIVE("啟用"),

    /**
     * 停用（已發布但目前未使用）
     */
    INACTIVE("停用");

    private final String description;

    /**
     * 取得狀態代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："DRAFT"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 狀態代碼（Enum 名稱）
     * @return Rich Menu 狀態
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static RichMenuStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的 Rich Menu 狀態: " + code, e);
        }
    }

    /**
     * 驗證狀態代碼是否有效
     *
     * @param code 狀態代碼
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
