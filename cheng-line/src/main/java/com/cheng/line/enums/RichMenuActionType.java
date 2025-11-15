package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE Rich Menu 動作類型列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "URI", "MESSAGE", "POSTBACK", "RICHMENU_SWITCH"
 * <p>
 * 對應 LINE Messaging API 的 Action 類型
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum RichMenuActionType {

    /**
     * 開啟網址（URI Action）
     * <p>
     * 使用者點擊後開啟指定的網址
     */
    URI("開啟網址"),

    /**
     * 傳送訊息（Message Action）
     * <p>
     * 使用者點擊後傳送指定的訊息文字到聊天室
     */
    MESSAGE("傳送訊息"),

    /**
     * 回傳資料（Postback Action）
     * <p>
     * 使用者點擊後傳送 postback 資料到後端（不顯示在聊天室）
     */
    POSTBACK("回傳資料"),

    /**
     * 切換選單（Rich Menu Switch Action）
     * <p>
     * 使用者點擊後切換到另一個 Rich Menu
     */
    RICHMENU_SWITCH("切換選單"),

    /**
     * 日期時間選擇器（Datetime Picker Action）
     * <p>
     * 使用者點擊後開啟日期/時間選擇器
     */
    DATETIMEPICKER("日期時間選擇"),

    /**
     * 相機動作（Camera Action）
     * <p>
     * 使用者點擊後開啟相機
     */
    CAMERA("開啟相機"),

    /**
     * 相機卷動作（Camera Roll Action）
     * <p>
     * 使用者點擊後開啟相簿
     */
    CAMERAROLL("開啟相簿"),

    /**
     * 位置動作（Location Action）
     * <p>
     * 使用者點擊後分享目前位置
     */
    LOCATION("分享位置");

    private final String description;

    /**
     * 取得動作類型代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："URI"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 動作類型代碼（Enum 名稱）
     * @return Rich Menu 動作類型
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static RichMenuActionType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        // 正規化代碼：轉換為大寫
        String normalizedCode = code.toUpperCase();
        
        // 特殊處理：LINE API 的 "richmenuswitch" → "RICHMENU_SWITCH"
        if ("RICHMENUSWITCH".equals(normalizedCode)) {
            normalizedCode = "RICHMENU_SWITCH";
        }
        
        try {
            return valueOf(normalizedCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的 Rich Menu 動作類型: " + code, e);
        }
    }

    /**
     * 驗證動作類型代碼是否有效
     *
     * @param code 動作類型代碼
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
