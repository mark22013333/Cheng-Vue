package com.cheng.shop.logistics.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.enums.LogisticsSubTypeMode;
import com.cheng.shop.logistics.EcpayLogisticsGateway;
import com.cheng.shop.logistics.ShippingMethodVO;
import com.cheng.shop.mapper.ShopCvsStoreTempMapper;
import com.cheng.shop.mapper.ShopOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link ShopLogisticsServiceImpl} 關於 B2C/C2C 模式過濾的單元測試。
 */
@ExtendWith(MockitoExtension.class)
class ShopLogisticsServiceImplTest {

    @Mock
    private ShopConfigService shopConfig;

    @Mock
    private ShopCvsStoreTempMapper cvsStoreTempMapper;

    @Mock
    private ShopOrderMapper orderMapper;

    @Mock
    private EcpayLogisticsGateway logisticsGateway;

    @InjectMocks
    private ShopLogisticsServiceImpl service;

    @BeforeEach
    void stubFeeConfig() {
        lenient().when(shopConfig.getHomeDeliveryFee()).thenReturn(new BigDecimal("100"));
        lenient().when(shopConfig.getHomeDeliveryFreeThreshold()).thenReturn(new BigDecimal("1000"));
        lenient().when(shopConfig.getCvsFee()).thenReturn(new BigDecimal("60"));
        lenient().when(shopConfig.getCvsFreeThreshold()).thenReturn(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("C2C 模式：HOME_DELIVERY 自動過濾，僅回傳超商 3 項")
    void getAvailableMethods_c2cMode_filtersOutHomeDelivery() {
        when(shopConfig.getLogisticsMethods())
                .thenReturn("HOME_DELIVERY,CVS_711,CVS_FAMILY,CVS_HILIFE");
        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.C2C);

        List<ShippingMethodVO> result = service.getAvailableMethods(new BigDecimal("500"));

        assertThat(result).hasSize(3)
                .extracting(ShippingMethodVO::getCode)
                .containsExactly("CVS_711", "CVS_FAMILY", "CVS_HILIFE");
    }

    @Test
    @DisplayName("B2C 模式：HOME_DELIVERY 保留，回傳 4 項")
    void getAvailableMethods_b2cMode_keepsHomeDelivery() {
        when(shopConfig.getLogisticsMethods())
                .thenReturn("HOME_DELIVERY,CVS_711,CVS_FAMILY,CVS_HILIFE");
        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.B2C);

        List<ShippingMethodVO> result = service.getAvailableMethods(new BigDecimal("500"));

        assertThat(result).hasSize(4)
                .extracting(ShippingMethodVO::getCode)
                .containsExactly("HOME_DELIVERY", "CVS_711", "CVS_FAMILY", "CVS_HILIFE");
    }

    @Test
    @DisplayName("C2C 模式 + 僅 HOME_DELIVERY：回傳空清單")
    void getAvailableMethods_c2cMode_onlyHomeDelivery_returnsEmpty() {
        when(shopConfig.getLogisticsMethods()).thenReturn("HOME_DELIVERY");
        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.C2C);

        List<ShippingMethodVO> result = service.getAvailableMethods(new BigDecimal("500"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("STORE_PICKUP 在兩種模式下都不被過濾（非綠界物流）")
    void getAvailableMethods_storePickup_notFilteredInEitherMode() {
        when(shopConfig.getLogisticsMethods()).thenReturn("STORE_PICKUP,CVS_711");

        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.C2C);
        assertThat(service.getAvailableMethods(BigDecimal.ZERO))
                .extracting(ShippingMethodVO::getCode)
                .containsExactly("STORE_PICKUP", "CVS_711");

        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.B2C);
        assertThat(service.getAvailableMethods(BigDecimal.ZERO))
                .extracting(ShippingMethodVO::getCode)
                .containsExactly("STORE_PICKUP", "CVS_711");
    }

    @Test
    @DisplayName("generateMapFormHtml：C2C + HOME_DELIVERY 拋 ServiceException，不呼叫 gateway")
    void generateMapFormHtml_c2cMode_homeDelivery_throwsWithoutCallingGateway() {
        // HOME_DELIVERY 根本不是 CVS，會先被 isCvs() 擋下（HOME_DELIVERY 不支援電子地圖）
        assertThatThrownBy(() ->
                service.generateMapFormHtml("HOME_DELIVERY", "cvs_abc", 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不支援電子地圖");

        verifyNoInteractions(logisticsGateway);
    }

    @Test
    @DisplayName("generateMapFormHtml：C2C + CVS_711 取得 UNIMARTC2C，正常呼叫 gateway")
    void generateMapFormHtml_c2cMode_cvs711_callsGatewayWithC2cCode() {
        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.C2C);
        when(shopConfig.getLogisticsCallbackBaseUrl()).thenReturn("https://example.com/prod-api");
        when(shopConfig.getCvsStoreExpireMinutes()).thenReturn(30);
        when(logisticsGateway.generateMapFormHtml(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq("UNIMARTC2C"),
                org.mockito.ArgumentMatchers.anyString())).thenReturn("<form>...</form>");

        String html = service.generateMapFormHtml("CVS_711", "cvs_xyz", 1L);

        assertThat(html).isEqualTo("<form>...</form>");
    }
}
