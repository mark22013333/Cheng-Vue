package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 購物車實體
 *
 * @author cheng
 */
@Data
public class ShopCart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 購物車ID
     */
    private Long cartId;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 數量
     */
    private Integer quantity;

    /**
     * 是否選中
     */
    private Boolean isSelected;

    /**
     * 加入時間
     */
    private Date createTime;

    /**
     * 更新時間
     */
    private Date updateTime;

    // ============ 關聯查詢欄位 ============

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名稱
     */
    private String productName;

    /**
     * 商品主圖
     */
    private String productImage;

    /**
     * SKU名稱
     */
    private String skuName;

    /**
     * SKU圖片
     */
    private String skuImage;

    /**
     * 單價
     */
    private BigDecimal price;

    /**
     * SKU 特惠價（關聯查詢）
     */
    private BigDecimal skuSalePrice;

    /**
     * SKU 特價結束時間（關聯查詢）
     */
    private Date skuSaleEndDate;

    /**
     * 商品特惠價（關聯查詢，derived — 保留向前相容）
     */
    private BigDecimal productSalePrice;

    /**
     * 商品特價結束時間（關聯查詢，derived — 保留向前相容）
     */
    private Date productSaleEndDate;

    /**
     * 庫存數量
     */
    private Integer stockQuantity;

    /**
     * 折扣後實際單價（由結帳服務於 preview 時計算填入，非 DB 欄位）
     */
    private BigDecimal finalPrice;
}
