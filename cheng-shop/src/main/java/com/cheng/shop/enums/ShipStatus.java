package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 物流狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ShipStatus implements CodedEnum<String> {

    UNSHIPPED("UNSHIPPED", "未出貨"),
    PREPARING("PREPARING", "備貨中"),
    SHIPPED("SHIPPED", "已出貨"),
    IN_TRANSIT("IN_TRANSIT", "運送中"),
    DELIVERED("DELIVERED", "已送達"),
    RETURNED("RETURNED", "已退回");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否已出貨
     */
    public boolean isShipped() {
        return this == SHIPPED || this == IN_TRANSIT || this == DELIVERED;
    }

    /**
     * 取得狀態對應的顏色
     */
    public String getColor() {
        return switch (this) {
            case UNSHIPPED -> "#909399";
            case PREPARING -> "#E6A23C";
            case SHIPPED, IN_TRANSIT -> "#409EFF";
            case DELIVERED -> "#67C23A";
            case RETURNED -> "#F56C6C";
        };
    }

    public static ShipStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ShipStatus.class, code);
    }
}
