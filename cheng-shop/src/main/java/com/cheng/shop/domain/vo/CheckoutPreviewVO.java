package com.cheng.shop.domain.vo;

import com.cheng.shop.domain.ShopCart;
import com.cheng.shop.domain.ShopMemberAddress;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 結帳預覽視圖物件
 *
 * @author cheng
 */
@Data
@Builder
public class CheckoutPreviewVO {

    /**
     * 購物車項目列表
     */
    private List<ShopCart> items;

    /**
     * 收貨地址
     */
    private ShopMemberAddress address;

    /**
     * 地址列表（供選擇）
     */
    private List<ShopMemberAddress> addressList;

    /**
     * 商品總數量
     */
    private Integer totalQuantity;

    /**
     * 商品金額
     */
    private BigDecimal productAmount;

    /**
     * 運費
     */
    private BigDecimal shippingFee;

    /**
     * 折扣金額
     */
    private BigDecimal discountAmount;

    /**
     * 應付金額
     */
    private BigDecimal payableAmount;
}
