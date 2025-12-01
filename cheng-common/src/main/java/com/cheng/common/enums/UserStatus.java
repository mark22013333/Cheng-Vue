package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 使用者狀態
 * <p>
 * 注意：此 Enum 也用於物品狀態，因為語義相同（"0"=正常, "1"=停用）
 * 如需修改狀態代碼，請確認相容性
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus implements CodedEnum<String> {
    OK("0", "正常"),
    DISABLE("1", "停用"),
    DELETED("2", "刪除");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得狀態
     *
     * @param code 狀態代碼
     * @return 狀態
     */
    public static UserStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(UserStatus.class, code);
    }

    /**
     * 判斷是否正常
     *
     * @return true=正常, false=其他
     */
    public boolean isOk() {
        return this == OK;
    }

    /**
     * 判斷是否停用
     *
     * @return true=停用, false=其他
     */
    public boolean isDisable() {
        return this == DISABLE;
    }
}
