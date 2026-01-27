package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 訂單明細實體
 *
 * @author cheng
 */
@Data
public class ShopOrderItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明細ID
     */
    private Long itemId;

    /**
     * 訂單ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名稱（冗餘）
     */
    private String productName;

    /**
     * SKU名稱（冗餘）
     */
    private String skuName;

    /**
     * SKU圖片（冗餘）
     */
    private String skuImage;

    /**
     * 單價（下單時）
     */
    private BigDecimal unitPrice;

    /**
     * 數量
     */
    private Integer quantity;

    /**
     * 小計
     */
    private BigDecimal totalPrice;

    /**
     * 庫存物品ID（冗餘）
     */
    private Long invItemId;

    /**
     * 庫存物品名稱（冗餘）
     */
    private String invItemName;

    /**
     * 建立時間
     */
    private Date createTime;
}
