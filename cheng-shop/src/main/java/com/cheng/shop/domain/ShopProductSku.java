package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import com.cheng.shop.enums.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品SKU實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopProductSku extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU 編碼
     */
    private String skuCode;

    /**
     * 規格名稱
     */
    private String skuName;

    /**
     * SKU 專屬圖片
     */
    private String skuImage;

    /**
     * 銷售價
     */
    private BigDecimal price;

    /**
     * 原價（劃線價）
     */
    private BigDecimal originalPrice;

    /**
     * 成本價
     */
    private BigDecimal costPrice;

    /**
     * 關聯庫存物品ID
     */
    private Long invItemId;

    /**
     * 獨立庫存數量
     */
    private Integer stockQuantity;

    /**
     * SKU銷量
     */
    private Integer salesCount;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 狀態
     */
    private String status;

    /**
     * 庫存物品名稱（關聯查詢）
     */
    private String invItemName;

    /**
     * 庫存物品編碼（關聯查詢）
     */
    private String invItemCode;

    /**
     * 取得狀態列舉
     */
    public CommonStatus getStatusEnum() {
        return status != null ? CommonStatus.fromCode(status) : null;
    }

    /**
     * 設定狀態列舉
     */
    public void setStatusEnum(CommonStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }
}
