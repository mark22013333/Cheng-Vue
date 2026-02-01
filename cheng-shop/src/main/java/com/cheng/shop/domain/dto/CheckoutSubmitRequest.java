package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 結帳提交請求
 *
 * @author cheng
 */
@Data
public class CheckoutSubmitRequest {

    /**
     * 收貨地址 ID
     */
    @NotNull(message = "請選擇收貨地址")
    private Long addressId;

    /**
     * 備註
     */
    private String remark;

    /**
     * 付款方式（目前僅支援 COD 貨到付款）
     */
    private String paymentMethod = "COD";

    /**
     * 選擇的禮物ID（可選）
     */
    private Long giftId;
}
