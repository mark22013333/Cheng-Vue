package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import com.cheng.shop.enums.ProductStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品主表實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopProduct extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 分類ID
     */
    private Long categoryId;

    /**
     * 分類ID列表（用於查詢包含子分類的商品）
     */
    private List<Long> categoryIds;

    /**
     * 商品標題
     */
    private String title;

    /**
     * 副標題/促銷語
     */
    private String subTitle;

    /**
     * 主圖
     */
    private String mainImage;

    /**
     * 輪播圖（JSON）
     */
    private String sliderImages;

    /**
     * 商品影片URL
     */
    private String videoUrl;

    /**
     * 商品描述（富文本HTML）
     */
    private String description;

    /**
     * 銷量
     */
    private Integer salesCount;

    /**
     * 瀏覽量
     */
    private Integer viewCount;

    /**
     * 是否熱門
     */
    private Boolean isHot;

    /**
     * 是否新品
     */
    private Boolean isNew;

    /**
     * 是否推薦
     */
    private Boolean isRecommend;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 狀態
     */
    private String status;

    /**
     * 分類名稱（關聯查詢）
     */
    private String categoryName;

    /**
     * 最低價格（從 SKU 關聯查詢）
     */
    private BigDecimal price;

    /**
     * 原價（從 SKU 關聯查詢）
     */
    private BigDecimal originalPrice;

    /**
     * SKU 列表（關聯查詢）
     */
    private List<ShopProductSku> skuList;

    /**
     * 取得狀態列舉
     */
    public ProductStatus getStatusEnum() {
        return status != null ? ProductStatus.fromCode(status) : null;
    }

    /**
     * 設定狀態列舉
     */
    public void setStatusEnum(ProductStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }
}
