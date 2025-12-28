package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * LINE Quick Reply 動作類型列舉
 * <p>
 * 對應 LINE Messaging API 的 Quick Reply Action 類型
 *
 * @author cheng
 * @see <a href="https://developers.line.biz/en/reference/messaging-api/#action-objects">LINE Action Objects</a>
 */
@Getter
@RequiredArgsConstructor
public enum QuickReplyActionType {

    /**
     * 傳送訊息（Message Action）
     * <p>
     * 使用者點擊後傳送指定的訊息文字到聊天室
     */
    MESSAGE("message", "傳送訊息"),

    /**
     * 開啟網址（URI Action）
     * <p>
     * 使用者點擊後開啟指定的網址
     */
    URI("uri", "開啟網址"),

    /**
     * 回傳資料（Postback Action）
     * <p>
     * 使用者點擊後傳送 postback 資料到後端
     */
    POSTBACK("postback", "回傳資料"),

    /**
     * 日期時間選擇器（Datetime Picker Action）
     * <p>
     * 使用者點擊後開啟日期/時間選擇器
     */
    DATETIMEPICKER("datetimepicker", "日期時間選擇"),

    /**
     * 相機動作（Camera Action）
     * <p>
     * 使用者點擊後開啟相機
     */
    CAMERA("camera", "開啟相機"),

    /**
     * 相機卷動作（Camera Roll Action）
     * <p>
     * 使用者點擊後開啟相簿
     */
    CAMERA_ROLL("cameraRoll", "開啟相簿"),

    /**
     * 位置動作（Location Action）
     * <p>
     * 使用者點擊後分享目前位置
     */
    LOCATION("location", "分享位置"),

    /**
     * 複製文字（Clipboard Action）
     * <p>
     * 使用者點擊後複製指定文字到剪貼簿
     */
    CLIPBOARD("clipboard", "複製文字");

    /**
     * LINE API 中的動作類型代碼（小寫）
     */
    private final String code;

    /**
     * 動作類型描述
     */
    private final String description;

    /**
     * JSON 序列化時使用 code
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 根據 LINE API 的動作類型代碼轉換為 Enum
     *
     * @param code 動作類型代碼（如 "message", "uri" 等）
     * @return Quick Reply 動作類型，找不到則返回 null
     */
    public static QuickReplyActionType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 驗證動作類型代碼是否有效
     *
     * @param code 動作類型代碼
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
