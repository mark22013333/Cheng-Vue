package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 付款狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum PayStatus implements CodedEnum<String> {

    UNPAID("UNPAID", "未付款"),
    PAYING("PAYING", "付款中"),
    PAID("PAID", "已付款"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款"),
    FAILED("FAILED", "付款失敗");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否已完成付款
     */
    public boolean isPaid() {
        return this == PAID;
    }

    /**
     * 取得狀態對應的顏色
     */
    public String getColor() {
        return switch (this) {
            case UNPAID -> "#909399";
            case PAYING, REFUNDING -> "#E6A23C";
            case PAID -> "#67C23A";
            case REFUNDED, FAILED -> "#F56C6C";
        };
    }

    public static PayStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(PayStatus.class, code);
    }
}
