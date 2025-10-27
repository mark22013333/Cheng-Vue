package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE 訊息類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum MessageType {

    /**
     * 回覆訊息（透過 replyToken 回覆使用者）
     */
    REPLY("REPLY", "回覆訊息"),

    /**
     * 推播訊息（單人推播）
     */
    PUSH("PUSH", "推播訊息(單人)"),

    /**
     * 推播訊息（多人推播）
     */
    MULTICAST("MULTICAST", "推播訊息(多人)"),

    /**
     * 廣播訊息（全部使用者）
     */
    BROADCAST("BROADCAST", "廣播訊息");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得訊息類型
     *
     * @param code 訊息類型代碼
     * @return 訊息類型
     */
    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的訊息類型: " + code);
    }
}
