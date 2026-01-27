package com.cheng.shop.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 結帳結果視圖物件
 *
 * @author cheng
 */
@Data
@Builder
public class CheckoutResultVO {

    /**
     * 訂單 ID
     */
    private Long orderId;

    /**
     * 訂單編號
     */
    private String orderNo;

    /**
     * 應付金額
     */
    private BigDecimal payableAmount;

    /**
     * 付款方式
     */
    private String paymentMethod;
}
