package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用狀態列舉（啟用/停用）
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum Status implements CodedEnum<Integer> {

    /**
     * 停用
     */
    DISABLE(0, "停用"),

    /**
     * 啟用
     */
    ENABLE(1, "啟用");

    private final Integer code;
    private final String description;

    /**
     * 根據代碼取得狀態
     *
     * @param code 狀態代碼
     * @return 狀態
     */
    public static Status fromCode(Integer code) {
        return EnumUtils.fromCodeOrThrow(Status.class, code);
    }

    /**
     * 判斷是否啟用
     *
     * @return true=啟用, false=停用
     */
    public boolean isEnable() {
        return this == ENABLE;
    }

    /**
     * 判斷是否停用
     *
     * @return true=停用, false=啟用
     */
    public boolean isDisable() {
        return this == DISABLE;
    }
}
