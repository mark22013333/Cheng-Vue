package com.cheng.line.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 連線測試結果 VO
 *
 * @author cheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 訊息
     */
    private String message;

    /**
     * Bot 基本資訊（成功時回傳）
     */
    private BotInfoVO botInfo;

    /**
     * 錯誤詳情（失敗時回傳）
     */
    private String errorDetails;

    /**
     * Bot 基本資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BotInfoVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Bot 使用者ID
         */
        private String userId;

        /**
         * Bot 顯示名稱
         */
        private String displayName;

        /**
         * Bot 圖片 URL
         */
        private String pictureUrl;

        /**
         * Bot 狀態訊息
         */
        private String statusMessage;
    }
}
