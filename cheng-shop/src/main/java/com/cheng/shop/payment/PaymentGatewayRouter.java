package com.cheng.shop.payment;

import com.cheng.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 金流閘道路由器
 * <p>
 * 根據付款方式代碼找到對應的 {@link PaymentGateway} 實作。
 * Spring 自動注入所有 {@code PaymentGateway} 的 Bean，
 * 新增金流只需建立新的 {@code @Component} 類別。
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final List<PaymentGateway> gateways;

    /**
     * 取得指定付款方式的閘道
     *
     * @param paymentMethod 付款方式代碼（如 "ECPAY"）
     * @return 對應的閘道實作
     * @throws ServiceException 找不到對應閘道時拋出
     */
    public PaymentGateway getGateway(String paymentMethod) {
        return gateways.stream()
                .filter(g -> g.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new ServiceException("不支援的付款方式：" + paymentMethod));
    }
}
