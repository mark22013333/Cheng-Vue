package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE 訊息內容類型列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "TEXT", "IMAGE", "VIDEO", "TEMPLATE", "FLEX"
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ContentType {

    /**
     * 純文字訊息
     */
    TEXT("純文字"),

    /**
     * 圖片訊息
     */
    IMAGE("圖片訊息"),

    /**
     * 影片訊息
     */
    VIDEO("影片訊息"),

    /**
     * 音訊訊息
     */
    AUDIO("音訊訊息"),

    /**
     * 貼圖訊息
     */
    STICKER("貼圖訊息"),

    /**
     * 位置訊息
     */
    LOCATION("位置訊息"),

    /**
     * 模板訊息（按鈕、確認、輪播等）
     */
    TEMPLATE("模板訊息"),

    /**
     * Flex 訊息（彈性訊息容器）
     */
    FLEX("Flex訊息"),

    /**
     * 圖片地圖訊息
     */
    IMAGEMAP("圖片地圖");

    private final String description;

    /**
     * 取得類型代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："TEXT"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum（找不到返回 null）
     *
     * @param code 內容類型代碼（Enum 名稱，不區分大小寫）
     * @return 內容類型，找不到則返回 null
     */
    public static ContentType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 根據代碼字串轉換為 Enum（找不到拋出異常）
     *
     * @param code 內容類型代碼（Enum 名稱）
     * @return 內容類型
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static ContentType fromCodeOrThrow(String code) {
        ContentType type = fromCode(code);
        if (type == null) {
            throw new IllegalArgumentException("未知的內容類型: " + code);
        }
        return type;
    }
}
