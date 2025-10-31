package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 綁定狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum BindStatus {

    /**
     * 未綁定
     */
    UNBOUND("UNBOUND", "未綁定"),

    /**
     * 已綁定
     */
    BOUND("BOUND", "已綁定");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得綁定狀態
     *
     * @param code 狀態代碼
     * @return 綁定狀態
     */
    public static BindStatus fromCode(String code) {
        for (BindStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的綁定狀態: " + code);
    }
}
