package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE 訊息類型列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "REPLY", "PUSH", "MULTICAST", "BROADCAST"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum MessageType {

    /**
     * 回覆訊息（透過 replyToken 回覆使用者）
     */
    REPLY("回覆訊息"),

    /**
     * 推播訊息（單人推播）
     */
    PUSH("推播訊息(單人)"),

    /**
     * 推播訊息（多人推播）
     */
    MULTICAST("推播訊息(多人)"),

    /**
     * 廣播訊息（全部使用者）
     */
    BROADCAST("廣播訊息");

    private final String description;

    /**
     * 取得類型代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："PUSH"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 訊息類型代碼（Enum 名稱）
     * @return 訊息類型
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static MessageType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的訊息類型: " + code, e);
        }
    }
}
