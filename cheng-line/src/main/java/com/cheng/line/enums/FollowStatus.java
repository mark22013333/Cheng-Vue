package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 關注狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum FollowStatus {

    /**
     * 關注中（好友狀態）
     */
    FOLLOWING("FOLLOWING", "好友"),

    /**
     * 已封鎖（取消關注）
     */
    BLOCKED("BLOCKED", "已封鎖");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得關注狀態
     *
     * @param code 狀態代碼
     * @return 關注狀態
     */
    public static FollowStatus fromCode(String code) {
        for (FollowStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的關注狀態: " + code);
    }
}
