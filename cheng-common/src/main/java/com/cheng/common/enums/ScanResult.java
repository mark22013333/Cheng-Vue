package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 掃描結果列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ScanResult implements CodedEnum<String> {

    /**
     * 成功
     */
    SUCCESS("0", "成功"),

    /**
     * 失敗
     */
    FAILURE("1", "失敗");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得掃描結果
     *
     * @param code 結果代碼
     * @return 掃描結果
     */
    public static ScanResult fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ScanResult.class, code);
    }

    /**
     * 判斷是否成功
     *
     * @return true=成功, false=失敗
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判斷是否失敗
     *
     * @return true=失敗, false=成功
     */
    public boolean isFailure() {
        return this == FAILURE;
    }
}
