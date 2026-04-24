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
     * 取得綠界 LogisticsSubType 代碼。
     * <p>
     * 依 {@link LogisticsSubTypeMode} 決定代碼：
     * <ul>
     *   <li>B2C：CVS 送 UNIMART/FAMI/HILIFE，宅配送 TCAT</li>
     *   <li>C2C：CVS 送 UNIMARTC2C/FAMIC2C/HILIFEC2C，宅配無對應代碼（回傳 null）</li>
     * </ul>
     * STORE_PICKUP 在兩種模式下皆非綠界物流方式，回傳 null。
     *
     * @param mode 物流型態模式（不可為 null）
     * @return 對應之 LogisticsSubType 字串；若該組合在當前模式下不支援則回傳 null
     */
    public String getEcpayLogisticsSubType(LogisticsSubTypeMode mode) {
        return switch (mode) {
            case C2C -> switch (this) {
                case CVS_711 -> "UNIMARTC2C";
                case CVS_FAMILY -> "FAMIC2C";
                case CVS_HILIFE -> "HILIFEC2C";
                case HOME_DELIVERY, STORE_PICKUP -> null;
            };
            case B2C -> switch (this) {
                case CVS_711 -> "UNIMART";
                case CVS_FAMILY -> "FAMI";
                case CVS_HILIFE -> "HILIFE";
                case HOME_DELIVERY -> "TCAT";
                case STORE_PICKUP -> null;
            };
        };
    }

    /**
     * 取得綠界 LogisticsSubType 代碼（無參版本，預設以 C2C 模式處理）。
     *
     * @deprecated 呼叫端應改用 {@link #getEcpayLogisticsSubType(LogisticsSubTypeMode)}，
     * 並從 {@code ShopConfigService.getEcpayLogisticsSubTypeMode()} 讀取模式。保留此方法僅為向前相容。
     */
    @Deprecated
    public String getEcpayLogisticsSubType() {
        return getEcpayLogisticsSubType(LogisticsSubTypeMode.DEFAULT);
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
