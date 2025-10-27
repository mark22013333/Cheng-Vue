package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 訊息發送狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum SendStatus {

    /**
     * 待發送
     */
    PENDING("PENDING", "待發送"),

    /**
     * 發送中
     */
    SENDING("SENDING", "發送中"),

    /**
     * 發送成功
     */
    SUCCESS("SUCCESS", "發送成功"),

    /**
     * 部分成功（部分使用者發送失敗）
     */
    PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功"),

    /**
     * 發送失敗
     */
    FAILED("FAILED", "發送失敗");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得發送狀態
     *
     * @param code 狀態代碼
     * @return 發送狀態
     */
    public static SendStatus fromCode(String code) {
        for (SendStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的發送狀態: " + code);
    }
}
