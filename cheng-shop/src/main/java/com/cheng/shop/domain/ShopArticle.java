package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商城文章實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopArticle extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 標題
     */
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * HTML 內容
     */
    private String content;

    /**
     * 封面圖
     */
    private String coverImage;

    /**
     * 關聯商品ID
     */
    private Long productId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 瀏覽數
     */
    private Integer viewCount;

    /**
     * 狀態: DRAFT/PUBLISHED
     */
    private String status;

    /**
     * 關聯商品名稱（非持久化欄位）
     */
    private String productTitle;
}
