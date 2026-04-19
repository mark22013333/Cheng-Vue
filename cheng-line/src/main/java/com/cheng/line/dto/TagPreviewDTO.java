package com.cheng.line.dto;

import lombok.Data;

import java.util.List;

/**
 * 標籤推播預覽 DTO
 * 顯示預計推播人數和各標籤明細
 *
 * @author cheng
 */
@Data
public class TagPreviewDTO {

    /**
     * 總人數（去重後）
     */
    private int count;

    /**
     * 各標籤的明細
     */
    private List<TagDetailDTO> tagDetails;

    /**
     * 標籤明細 DTO
     */
    @Data
    public static class TagDetailDTO {

        /**
         * 標籤 ID
         */
        private Long tagId;

        /**
         * 標籤名稱
         */
        private String tagName;

        /**
         * 標籤顏色
         */
        private String tagColor;

        /**
         * 該標籤的使用者數量
         */
        private int userCount;
    }
}
