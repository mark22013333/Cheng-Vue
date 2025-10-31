package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE 訊息內容類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ContentType {

    /**
     * 純文字訊息
     */
    TEXT("TEXT", "純文字"),

    /**
     * 圖片訊息
     */
    IMAGE("IMAGE", "圖片訊息"),

    /**
     * 影片訊息
     */
    VIDEO("VIDEO", "影片訊息"),

    /**
     * 音訊訊息
     */
    AUDIO("AUDIO", "音訊訊息"),

    /**
     * 貼圖訊息
     */
    STICKER("STICKER", "貼圖訊息"),

    /**
     * 模板訊息（按鈕、確認、輪播等）
     */
    TEMPLATE("TEMPLATE", "模板訊息"),

    /**
     * Flex 訊息（彈性訊息容器）
     */
    FLEX("FLEX", "Flex訊息");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得內容類型
     *
     * @param code 內容類型代碼
     * @return 內容類型
     */
    public static ContentType fromCode(String code) {
        for (ContentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的內容類型: " + code);
    }
}
