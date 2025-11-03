package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 訊息發送狀態列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "PENDING", "SENDING", "SUCCESS", "FAILED"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum SendStatus {

    /**
     * 待發送
     */
    PENDING("待發送"),

    /**
     * 發送中
     */
    SENDING("發送中"),

    /**
     * 發送成功
     */
    SUCCESS("發送成功"),

    /**
     * 部分成功（部分使用者發送失敗）
     */
    PARTIAL_SUCCESS("部分成功"),

    /**
     * 發送失敗
     */
    FAILED("發送失敗");

    private final String description;

    /**
     * 取得狀態代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："SUCCESS"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 狀態代碼（Enum 名稱）
     * @return 發送狀態
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static SendStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的發送狀態: " + code, e);
        }
    }
}
