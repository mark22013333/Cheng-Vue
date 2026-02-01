package com.cheng.shop.payment;

import java.util.Map;

/**
 * 金流閘道介面
 * <p>
 * 每個金流服務商實作此介面。
 * 新增金流只需建立新的 {@code @Component} 類別即可。
 *
 * @author cheng
 */
public interface PaymentGateway {

    /**
     * 建立付款請求
     *
     * @param request 付款請求參數
     * @return 付款回應（包含跳轉 URL 或自動提交表單）
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * 驗證第三方回調簽章
     *
     * @param params 回調參數
     * @return 簽章是否合法
     */
    boolean verifyCallback(Map<String, String> params);

    /**
     * 解析第三方回調結果
     *
     * @param params 回調參數
     * @return 標準化的回調結果
     */
    CallbackResult parseCallback(Map<String, String> params);

    /**
     * 是否支援指定付款方式
     *
     * @param paymentMethod 付款方式代碼（對應 PaymentMethod enum）
     * @return true 表示支援
     */
    boolean supports(String paymentMethod);
}
