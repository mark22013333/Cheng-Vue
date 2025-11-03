package com.cheng.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 使用者狀態
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {
    OK("0", "正常"),
    DISABLE("1", "停用"),
    DELETED("2", "刪除");

    private final String code;
    private final String info;

}
