package com.cheng.shop.domain.vo;

import lombok.Data;

/**
 * 熱門搜尋關鍵字視圖物件
 *
 * @author cheng
 */
@Data
public class PopularSearchVO {

    /**
     * 搜尋關鍵字
     */
    private String keyword;

    /**
     * 搜尋次數
     */
    private Long searchCount;
}
