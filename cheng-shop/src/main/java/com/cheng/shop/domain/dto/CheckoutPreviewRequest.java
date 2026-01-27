package com.cheng.shop.domain.dto;

import lombok.Data;

/**
 * 結帳預覽請求
 *
 * @author cheng
 */
@Data
public class CheckoutPreviewRequest {

    /**
     * 收貨地址 ID
     */
    private Long addressId;

    /**
     * 備註
     */
    private String remark;
}
