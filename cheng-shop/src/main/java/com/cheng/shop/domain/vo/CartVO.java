package com.cheng.shop.domain.vo;

import com.cheng.shop.domain.ShopCart;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 購物車視圖物件
 *
 * @author cheng
 */
@Data
@Builder
public class CartVO {

    /**
     * 購物車項目列表
     */
    private List<ShopCart> items;

    /**
     * 總數量
     */
    private Integer totalQuantity;

    /**
     * 已選中數量
     */
    private Integer selectedQuantity;

    /**
     * 總金額
     */
    private BigDecimal totalAmount;

    /**
     * 已選中金額
     */
    private BigDecimal selectedAmount;
}
