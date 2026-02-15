package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 結帳提交請求
 *
 * @author cheng
 */
@Data
public class CheckoutSubmitRequest {

    /**
     * 收貨地址 ID（宅配時必填，超商取貨時可為 null）
     */
    private Long addressId;

    /**
     * 備註
     */
    private String remark;

    /**
     * 付款方式
     */
    private String paymentMethod = "ECPAY";

    /**
     * 物流方式
     */
    @NotBlank(message = "請選擇物流方式")
    private String shippingMethod = "HOME_DELIVERY";

    /**
     * 超商門市暫存 key（選擇超商取貨時必填）
     */
    private String cvsStoreKey;

    /**
     * 收件人姓名（超商取貨時必填，用於物流出貨單）
     */
    private String receiverName;

    /**
     * 收件人手機（超商取貨時必填，用於物流出貨單）
     */
    private String receiverPhone;

    /**
     * 選擇的禮物ID（可選）
     */
    private Long giftId;
}
