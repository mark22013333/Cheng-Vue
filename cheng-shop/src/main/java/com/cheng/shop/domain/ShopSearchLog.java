package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜尋記錄實體
 *
 * @author cheng
 */
@Data
public class ShopSearchLog implements Serializable {

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
     * 搜尋關鍵字
     */
    private String keyword;

    /**
     * 搜尋結果數量
     */
    private Integer resultCount;

    /**
     * 建立時間
     */
    private LocalDateTime createTime;
}
