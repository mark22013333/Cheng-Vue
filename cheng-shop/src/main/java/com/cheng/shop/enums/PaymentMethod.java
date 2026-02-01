package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 付款方式列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements CodedEnum<String> {

    LINE_PAY("LINE_PAY", "LINE Pay"),
    CREDIT_CARD("CREDIT_CARD", "信用卡"),
    ATM("ATM", "ATM轉帳"),
    CVS("CVS", "超商付款"),
    COD("COD", "貨到付款"),
    ECPAY("ECPAY", "綠界金流");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否為線上支付
     */
    public boolean isOnlinePayment() {
        return this == LINE_PAY || this == CREDIT_CARD || this == ECPAY;
    }

    public static PaymentMethod fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(PaymentMethod.class, code);
    }
}
