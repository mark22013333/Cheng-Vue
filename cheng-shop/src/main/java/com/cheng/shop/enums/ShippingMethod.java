package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 物流方式列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ShippingMethod implements CodedEnum<String> {

    HOME_DELIVERY("HOME_DELIVERY", "宅配到府"),
    CVS_711("CVS_711", "7-11 超取"),
    CVS_FAMILY("CVS_FAMILY", "全家超取"),
    CVS_HILIFE("CVS_HILIFE", "萊爾富超取"),
    STORE_PICKUP("STORE_PICKUP", "門市自取");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否為超商取貨
     */
    public boolean isCvs() {
        return this == CVS_711 || this == CVS_FAMILY || this == CVS_HILIFE;
    }

    /**
     * 取得綠界 LogisticsSubType
     *
     * @return 綠界物流子類型代碼，若非超商/宅配則返回 null
     */
    public String getEcpayLogisticsSubType() {
        return switch (this) {
            case CVS_711 -> "UNIMART";
            case CVS_FAMILY -> "FAMI";
            case CVS_HILIFE -> "HILIFE";
            case HOME_DELIVERY -> "TCAT";
            default -> null;
        };
    }

    /**
     * 取得綠界 LogisticsType
     *
     * @return CVS 或 HOME
     */
    public String getEcpayLogisticsType() {
        return isCvs() ? "CVS" : "HOME";
    }

    public static ShippingMethod fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ShippingMethod.class, code);
    }
}
