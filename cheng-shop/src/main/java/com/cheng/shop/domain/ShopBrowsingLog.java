package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品瀏覽記錄實體
 *
 * @author cheng
 */
@Data
public class ShopBrowsingLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 記錄ID
     */
    private Long logId;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 訪客識別碼（未登入時使用）
     */
    private String guestId;

    /**
     * 商品ID
     */
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

    /**
     * 建立時間
     */
    private LocalDateTime createTime;
}
