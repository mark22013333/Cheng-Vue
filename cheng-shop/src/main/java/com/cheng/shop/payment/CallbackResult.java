package com.cheng.shop.payment;

import lombok.Builder;
import lombok.Data;

/**
 * 金流回調結果 DTO
 *
 * @author cheng
 */
@Data
@Builder
public class CallbackResult {

    /** 付款是否成功 */
    private boolean paymentSuccess;

    /** 訂單編號（MerchantTradeNo） */
    private String orderNo;

    /** 第三方交易編號 */
    private String tradeNo;

    /** 回應給第三方的內容（如 ECPay 需回傳 "1|OK"） */
    private String responseBody;

    /** 完整回調資訊（JSON 格式，用於儲存至 DB） */
    private String rawInfo;
}
