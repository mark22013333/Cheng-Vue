package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 會員狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum MemberStatus implements CodedEnum<String> {

    ACTIVE("ACTIVE", "正常"),
    DISABLED("DISABLED", "停用"),
    FROZEN("FROZEN", "凍結");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否可登入
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * 取得狀態對應的顏色
     */
    public String getColor() {
        return switch (this) {
            case ACTIVE -> "#67C23A";
            case DISABLED -> "#909399";
            case FROZEN -> "#F56C6C";
        };
    }

    public static MemberStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(MemberStatus.class, code);
    }
}
