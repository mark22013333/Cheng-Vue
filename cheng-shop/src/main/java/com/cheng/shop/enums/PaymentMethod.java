package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 付款方式列舉
 *
 * sys_config 設定說明：
 * - config_key: shop.payment.methods
 * - config_value: 逗號分隔的代碼，例如 "COD,ECPAY"
 * - 可選值：COD:貨到付款, ECPAY:綠界金流, LINE_PAY:LINE Pay,
 *           CREDIT_CARD:信用卡, ATM:ATM轉帳, CVS:超商付款
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements CodedEnum<String> {

    COD("COD", "貨到付款", "收到商品後現金支付"),
    ECPAY("ECPAY", "綠界金流", "支援信用卡、ATM、超商付款"),
    LINE_PAY("LINE_PAY", "LINE Pay", "使用 LINE Pay 付款"),
    CREDIT_CARD("CREDIT_CARD", "信用卡", "信用卡線上付款"),
    ATM("ATM", "ATM轉帳", "銀行 ATM 轉帳付款"),
    CVS("CVS", "超商付款", "超商代碼繳費");

    private final String code;
    private final String name;
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

    /**
     * 根據設定值取得啟用的付款方式列表
     *
     * @param configValue sys_config 中的設定值（逗號分隔）
     * @return 啟用的付款方式列表
     */
    public static List<PaymentMethod> fromConfigValue(String configValue) {
        if (configValue == null || configValue.isBlank()) {
            return List.of();
        }
        Set<String> enabledCodes = Arrays.stream(configValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        return Arrays.stream(values())
                .filter(pm -> enabledCodes.contains(pm.getCode()))
                .toList();
    }
}
