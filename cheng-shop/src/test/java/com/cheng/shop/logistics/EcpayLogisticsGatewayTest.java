package com.cheng.shop.logistics;

import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.enums.LogisticsSubTypeMode;
import com.cheng.shop.enums.ShippingMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * {@link EcpayLogisticsGateway} 關於 B2C/C2C 模式切換的單元測試。
 * <p>
 * 不打實際綠界 API — 只驗證 createShipment 在不支援組合時能於呼叫前返回 fail。
 * 電子地圖表單產生由 {@code ShopLogisticsServiceImpl} 負責 mode 判斷，此處不重複測試。
 */
@ExtendWith(MockitoExtension.class)
class EcpayLogisticsGatewayTest {

    @Mock
    private ShopConfigService shopConfig;

    @InjectMocks
    private EcpayLogisticsGateway gateway;

    @BeforeEach
    void stubDefaultConfig() {
        // 任何測試都可能被使用的 stub，使用 lenient 避免 UnnecessaryStubbing
        lenient().when(shopConfig.getEcpayLogisticsMerchantId()).thenReturn("2000132");
        lenient().when(shopConfig.getEcpayLogisticsHashKey()).thenReturn("5294y06JbISpM5x9");
        lenient().when(shopConfig.getEcpayLogisticsHashIv()).thenReturn("v77hoKGq4kWxNNIS");
        lenient().when(shopConfig.isEcpayLogisticsProdMode()).thenReturn(false);
        lenient().when(shopConfig.getEcpayLogisticsServerReplyUrl())
                .thenReturn("https://example.com/callback");
        lenient().when(shopConfig.getSenderName()).thenReturn("寄件者");
        lenient().when(shopConfig.getSenderPhone()).thenReturn("0912345678");
        lenient().when(shopConfig.getSenderZip()).thenReturn("242");
        lenient().when(shopConfig.getSenderAddress()).thenReturn("新莊區中正路100號");
    }

    @Test
    @DisplayName("C2C 模式下 createShipment 遇 HOME_DELIVERY，於呼叫綠界前回 fail（不做網路呼叫）")
    void createShipment_c2cMode_homeDelivery_failsBeforeNetwork() {
        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.C2C);

        ShopOrder order = new ShopOrder();
        order.setOrderNo("O2604230001");
        order.setShippingMethodEnum(ShippingMethod.HOME_DELIVERY);
        order.setTotalAmount(new BigDecimal("500"));
        order.setReceiverName("收件者");
        order.setReceiverMobile("0987654321");
        order.setReceiverAddress("台北市信義區信義路五段7號");
        order.setReceiverZip("110");

        LogisticsResult result = gateway.createShipment(order);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage())
                .contains("C2C")
                .contains("宅配到府");
    }

    @Test
    @DisplayName("C2C 模式下 createShipment 遇 CVS_711 能通過 mode 檢查進入下一步（缺門市代號時擋在下一層）")
    void createShipment_c2cMode_cvs711_passesModeCheck() {
        when(shopConfig.getEcpayLogisticsSubTypeMode()).thenReturn(LogisticsSubTypeMode.C2C);

        ShopOrder order = new ShopOrder();
        order.setOrderNo("O2604230002");
        order.setShippingMethodEnum(ShippingMethod.CVS_711);
        order.setTotalAmount(new BigDecimal("500"));
        order.setReceiverName("收件者");
        order.setReceiverMobile("0987654321");
        // 刻意不填 cvsStoreId，預期走到下一層驗證

        LogisticsResult result = gateway.createShipment(order);

        assertThat(result.isSuccess()).isFalse();
        // 不是 mode 不支援，應該是門市代號未設定（代表已通過 mode 檢查）
        assertThat(result.getErrorMessage())
                .doesNotContain("C2C")
                .contains("門市代號");
    }

    @Test
    @DisplayName("createShipment 物流方式未設定時直接 fail，不讀 sub_type_mode")
    void createShipment_noShippingMethod_fails() {
        ShopOrder order = new ShopOrder();
        order.setOrderNo("O2604230004");
        // 刻意不設 shippingMethod

        LogisticsResult result = gateway.createShipment(order);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("物流方式未設定");
    }
}
