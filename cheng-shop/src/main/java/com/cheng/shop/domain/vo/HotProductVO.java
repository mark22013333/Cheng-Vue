package com.cheng.shop.domain.vo;

import lombok.Data;

/**
 * 熱門商品視圖物件
 *
 * @author cheng
 */
@Data
public class HotProductVO {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名稱
     */
    private String productName;

    /**
     * 瀏覽次數
     */
    private Long viewCount;
}
