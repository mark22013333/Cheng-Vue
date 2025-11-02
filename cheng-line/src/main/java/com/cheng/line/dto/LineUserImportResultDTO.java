package com.cheng.line.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * LINE 使用者匯入結果 DTO
 *
 * @author cheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineUserImportResultDTO {

    /**
     * 總數
     */
    private Integer totalCount;

    /**
     * 成功數量
     */
    private Integer successCount;

    /**
     * 失敗數量
     */
    private Integer failCount;

    /**
     * 新增數量
     */
    private Integer newCount;

    /**
     * 更新數量
     */
    private Integer updateCount;

    /**
     * 失敗詳情列表
     */
    @Builder.Default
    private List<ImportFailDetail> failDetails = new ArrayList<>();

    /**
     * 匯入失敗詳情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailDetail {
        /**
         * LINE 使用者 ID
         */
        private String lineUserId;

        /**
         * 失敗原因
         */
        private String reason;

        /**
         * 行號（如果從檔案匯入）
         */
        private Integer rowNumber;
    }
}
