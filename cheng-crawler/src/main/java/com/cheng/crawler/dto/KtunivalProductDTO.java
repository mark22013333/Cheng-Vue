package com.cheng.crawler.dto;

import lombok.Data;

/**
 * ktunival.com.tw 爬蟲結果 DTO
 *
 * @author cheng
 */
@Data
public class KtunivalProductDTO {

    /** 產品名稱 */
    private String productName;

    /** 產品類別（如「化工用品類」） */
    private String category;

    /** 商品圖片 URL（供應商網站上的完整 URL） */
    private String imageUrl;

    /** 產品介紹 */
    private String description;

    /** 詳情頁 URL */
    private String detailUrl;

    /** 搜尋結果數量 */
    private int resultCount;
}
