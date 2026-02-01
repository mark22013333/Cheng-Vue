package com.cheng.shop.payment;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 付款回應 DTO
 *
 * @author cheng
 */
@Data
@Builder
public class PaymentResponse {

    /** 自動提交 HTML 表單（ECPay 使用） */
    private String formHtml;

    /** 重導向 URL（LINE Pay 等使用） */
    private String redirectUrl;

    /** 額外資訊 */
    private Map<String, Object> extra;
}
