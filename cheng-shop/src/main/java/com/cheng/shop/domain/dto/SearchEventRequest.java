package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 搜尋事件請求
 *
 * @author cheng
 */
@Data
public class SearchEventRequest {

    /**
     * 訪客識別碼（未登入時使用）
     */
    private String guestId;

    /**
     * 搜尋關鍵字
     */
    @NotBlank(message = "搜尋關鍵字不能為空")
    private String keyword;

    /**
     * 搜尋結果數量
     */
    private Integer resultCount;
}
