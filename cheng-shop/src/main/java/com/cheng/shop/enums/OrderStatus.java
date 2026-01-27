package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 訂單狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus implements CodedEnum<String> {

    PENDING("PENDING", "待付款"),
    PAID("PAID", "已付款"),
    PROCESSING("PROCESSING", "處理中"),
    SHIPPED("SHIPPED", "已出貨"),
    DELIVERED("DELIVERED", "已送達"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 取得可流轉的下一個狀態
     */
    public List<OrderStatus> getNextStatuses() {
        return switch (this) {
            case PENDING -> List.of(PAID, CANCELLED);
            case PAID -> List.of(PROCESSING, REFUNDING);
            case PROCESSING -> List.of(SHIPPED, REFUNDING);
            case SHIPPED -> List.of(DELIVERED);
            case DELIVERED -> List.of(COMPLETED, REFUNDING);
            case REFUNDING -> List.of(REFUNDED);
            default -> List.of();
        };
    }

    /**
     * 是否可取消
     */
    public boolean canCancel() {
        return this == PENDING;
    }

    /**
     * 是否可退款
     */
    public boolean canRefund() {
        return this == PAID || this == PROCESSING || this == DELIVERED;
    }

    /**
     * 是否為最終狀態
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * 是否需要付款
     */
    public boolean needPayment() {
        return this == PENDING;
    }

    /**
     * 取得狀態對應的顏色（Element UI 色系）
     */
    public String getColor() {
        return switch (this) {
            case PENDING -> "#E6A23C";
            case PAID, PROCESSING -> "#409EFF";
            case SHIPPED, DELIVERED, COMPLETED -> "#67C23A";
            case CANCELLED -> "#909399";
            case REFUNDING, REFUNDED -> "#F56C6C";
        };
    }

    public static OrderStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(OrderStatus.class, code);
    }
}
