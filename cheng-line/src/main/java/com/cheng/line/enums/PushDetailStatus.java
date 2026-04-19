package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 逐人推播明細狀態列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "SUCCESS", "FAILED", "BLOCKED", "PENDING"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum PushDetailStatus {

    /**
     * 發送成功
     */
    SUCCESS("發送成功"),

    /**
     * 發送失敗
     */
    FAILED("發送失敗"),

    /**
     * 使用者已封鎖
     */
    BLOCKED("已封鎖"),

    /**
     * 待發送
     */
    PENDING("待發送");

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
     * @return 推播明細狀態
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static PushDetailStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的推播明細狀態: " + code, e);
        }
    }
}
