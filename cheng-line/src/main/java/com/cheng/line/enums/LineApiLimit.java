package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE Messaging API 限制常數
 * 
 * 集中管理 LINE API 的各種限制值，避免魔術數字散落在程式碼中。
 * 
 * @author cheng
 * @see <a href="https://developers.line.biz/en/reference/messaging-api/">LINE Messaging API</a>
 */
@Getter
@RequiredArgsConstructor
public enum LineApiLimit {

    /**
     * 單次 API 呼叫最大訊息數量
     */
    MAX_MESSAGES_PER_REQUEST(5, "單次 API 呼叫最大訊息數量"),

    /**
     * 單次 API 呼叫最小訊息數量
     */
    MIN_MESSAGES_PER_REQUEST(1, "單次 API 呼叫最小訊息數量"),

    /**
     * Rich Menu 圖片最大檔案大小 (bytes)
     */
    RICH_MENU_IMAGE_MAX_SIZE(1024 * 1024, "Rich Menu 圖片最大 1MB"),

    /**
     * Imagemap 圖片最大檔案大小 (bytes)
     */
    IMAGEMAP_IMAGE_MAX_SIZE(10 * 1024 * 1024, "Imagemap 圖片最大 10MB"),

    /**
     * Imagemap 基準寬度
     */
    IMAGEMAP_BASE_WIDTH(1040, "Imagemap 基準寬度"),

    /**
     * Rich Menu Alias ID 最大長度
     */
    RICH_MENU_ALIAS_MAX_LENGTH(32, "Rich Menu Alias ID 最大長度"),

    /**
     * 多媒體訊息 Multicast 最大接收者數量
     */
    MULTICAST_MAX_RECIPIENTS(500, "Multicast 最大接收者數量"),

    /**
     * Narrowcast 最大接收者數量
     */
    NARROWCAST_MAX_RECIPIENTS(500, "Narrowcast 最大接收者數量"),

    /**
     * Flex Message altText 最大長度
     */
    FLEX_ALT_TEXT_MAX_LENGTH(400, "Flex Message altText 最大長度"),

    /**
     * 文字訊息最大長度
     */
    TEXT_MESSAGE_MAX_LENGTH(5000, "文字訊息最大長度");

    private final int value;
    private final String description;

    /**
     * 檢查訊息數量是否在有效範圍內
     */
    public static boolean isValidMessageCount(int count) {
        return count >= MIN_MESSAGES_PER_REQUEST.getValue() 
            && count <= MAX_MESSAGES_PER_REQUEST.getValue();
    }

    /**
     * 取得訊息數量錯誤提示
     */
    public static String getMessageCountError() {
        return String.format("訊息數量必須在 %d 到 %d 之間（LINE API 限制）",
                MIN_MESSAGES_PER_REQUEST.getValue(),
                MAX_MESSAGES_PER_REQUEST.getValue());
    }
}
