package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入購物車請求
 *
 * @author cheng
 */
@Data
public class AddCartRequest {

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID 不能為空")
    private Long skuId;

    /**
     * 數量（預設為 1）
     */
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity = 1;
}
