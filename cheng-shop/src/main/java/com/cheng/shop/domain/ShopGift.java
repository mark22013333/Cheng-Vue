package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 滿額禮物實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopGift extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 禮物ID
     */
    private Long giftId;

    /**
     * 禮物名稱
     */
    private String name;

    /**
     * 圖片URL
     */
    private String imageUrl;

    /**
     * 消費門檻金額
     */
    private BigDecimal thresholdAmount;

    /**
     * 庫存數量
     */
    private Integer stockQuantity;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 狀態
     */
    private String status;
}
