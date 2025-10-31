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
     * API 連線測試結果
     */
    private TestItemVO api;

    /**
     * Webhook 設定檢查結果
     */
    private TestItemVO webhook;

    /**
     * Bot 資訊取得結果
     */
    private TestItemVO bot;

    /**
     * 單項測試結果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestItemVO implements Serializable {

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
         * Bot 詳細資訊（僅 bot 項目有）
         */
        private BotDataVO data;
    }

    /**
     * Bot 詳細資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BotDataVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Bot 顯示名稱
         */
        private String displayName;

        /**
         * Bot 狀態訊息
         */
        private String statusMessage;

        /**
         * Bot 圖片 URL
         */
        private String pictureUrl;
    }
}
