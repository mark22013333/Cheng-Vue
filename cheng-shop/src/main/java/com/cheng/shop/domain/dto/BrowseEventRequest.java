package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 瀏覽事件請求
 *
 * @author cheng
 */
@Data
public class BrowseEventRequest {

    /**
     * 訪客識別碼（未登入時使用）
     */
    private String guestId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID 不能為空")
    private Long productId;

    /**
     * 商品名稱
     */
    private String productName;

    /**
     * 分類ID
     */
    private Long categoryId;

    /**
     * 來源（如：首頁、分類頁、搜尋結果）
     */
    private String source;
}
