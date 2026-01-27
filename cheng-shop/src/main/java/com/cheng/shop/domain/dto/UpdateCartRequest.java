package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新購物車請求
 *
 * @author cheng
 */
@Data
public class UpdateCartRequest {

    /**
     * 購物車項目 ID
     */
    @NotNull(message = "購物車項目 ID 不能為空")
    private Long cartId;

    /**
     * 數量
     */
    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;
}
