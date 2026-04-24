package com.cheng.shop.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ShippingMethod} 對綠界代碼的單元測試。
 */
class ShippingMethodTest {

    @ParameterizedTest(name = "C2C 模式：{0} → {1}")
    @CsvSource({
            "CVS_711,      UNIMARTC2C",
            "CVS_FAMILY,   FAMIC2C",
            "CVS_HILIFE,   HILIFEC2C"
    })
    void c2cMode_cvsSubtype(String method, String expected) {
        assertThat(ShippingMethod.valueOf(method).getEcpayLogisticsSubType(LogisticsSubTypeMode.C2C))
                .isEqualTo(expected);
    }

    @ParameterizedTest(name = "B2C 模式：{0} → {1}")
    @CsvSource({
            "CVS_711,       UNIMART",
            "CVS_FAMILY,    FAMI",
            "CVS_HILIFE,    HILIFE",
            "HOME_DELIVERY, TCAT"
    })
    void b2cMode_subtypeMapping(String method, String expected) {
        assertThat(ShippingMethod.valueOf(method).getEcpayLogisticsSubType(LogisticsSubTypeMode.B2C))
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("C2C 模式下 HOME_DELIVERY 無對應代碼，回傳 null")
    void c2cMode_homeDelivery_returnsNull() {
        assertThat(ShippingMethod.HOME_DELIVERY.getEcpayLogisticsSubType(LogisticsSubTypeMode.C2C))
                .isNull();
    }

    @Test
    @DisplayName("STORE_PICKUP 非綠界物流方式，兩種模式皆回 null")
    void storePickup_anyMode_returnsNull() {
        assertThat(ShippingMethod.STORE_PICKUP.getEcpayLogisticsSubType(LogisticsSubTypeMode.B2C)).isNull();
        assertThat(ShippingMethod.STORE_PICKUP.getEcpayLogisticsSubType(LogisticsSubTypeMode.C2C)).isNull();
    }

    @Test
    @DisplayName("LogisticsType：CVS 方式回 CVS，宅配回 HOME，門市自取回 HOME")
    void logisticsType_mapping() {
        assertThat(ShippingMethod.CVS_711.getEcpayLogisticsType()).isEqualTo("CVS");
        assertThat(ShippingMethod.CVS_FAMILY.getEcpayLogisticsType()).isEqualTo("CVS");
        assertThat(ShippingMethod.CVS_HILIFE.getEcpayLogisticsType()).isEqualTo("CVS");
        assertThat(ShippingMethod.HOME_DELIVERY.getEcpayLogisticsType()).isEqualTo("HOME");
        assertThat(ShippingMethod.STORE_PICKUP.getEcpayLogisticsType()).isEqualTo("HOME");
    }

    @Test
    @DisplayName("isCvs：CVS 三個方式為 true，其餘為 false")
    void isCvs_mapping() {
        assertThat(ShippingMethod.CVS_711.isCvs()).isTrue();
        assertThat(ShippingMethod.CVS_FAMILY.isCvs()).isTrue();
        assertThat(ShippingMethod.CVS_HILIFE.isCvs()).isTrue();
        assertThat(ShippingMethod.HOME_DELIVERY.isCvs()).isFalse();
        assertThat(ShippingMethod.STORE_PICKUP.isCvs()).isFalse();
    }

    @Test
    @DisplayName("Deprecated 無參版本仍能呼叫並預設以 C2C 處理")
    @SuppressWarnings("deprecation")
    void deprecatedNoArg_defaultsToC2c() {
        assertThat(ShippingMethod.CVS_711.getEcpayLogisticsSubType()).isEqualTo("UNIMARTC2C");
        assertThat(ShippingMethod.HOME_DELIVERY.getEcpayLogisticsSubType()).isNull();
    }
}
