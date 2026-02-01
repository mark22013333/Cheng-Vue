package com.cheng.shop.payment;

import com.cheng.shop.domain.ShopOrder;
import lombok.Builder;
import lombok.Data;

/**
 * 付款請求 DTO
 *
 * @author cheng
 */
@Data
@Builder
public class PaymentRequest {

    /** 訂單（含明細） */
    private ShopOrder order;

    /** 前端返回 URL（付款完成後用戶導向） */
    private String returnUrl;

    /** 後端回調 URL（第三方伺服器通知） */
    private String notifyUrl;
}
