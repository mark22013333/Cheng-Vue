package com.cheng.line.dto;

import com.cheng.common.enums.ImagemapActionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * LINE Imagemap 訊息 DTO
 * 
 * 參考: https://developers.line.biz/en/reference/messaging-api/#imagemap-message
 *
 * @author cheng
 */
@Data
@NoArgsConstructor
public class ImagemapMessageDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 訊息類型 (固定為 imagemap)
     */
    private final String type = "imagemap";

    /**
     * 圖片 Base URL
     * HTTPS, 結尾不含斜線
     * 用戶端會自動加上 /240, /300, /460, /700, /1040 來請求圖片
     */
    private String baseUrl;

    /**
     * 替代文字
     */
    private String altText;

    /**
     * 基準尺寸
     */
    private BaseSizeDto baseSize;

    /**
     * 影片內容 (選填)
     */
    private VideoDto video;

    /**
     * 動作列表
     */
    private List<ActionDto> actions;

    /**
     * 基準尺寸 DTO
     */
    @Data
    @NoArgsConstructor
    public static class BaseSizeDto implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * 寬度 (固定 1040)
         */
        private Integer width = 1040;
        
        /**
         * 高度
         */
        private Integer height;

        public BaseSizeDto(Integer height) {
            this.height = height;
        }
    }

    /**
     * 動作 DTO
     */
    @Data
    @NoArgsConstructor
    public static class ActionDto implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 動作類型 (uri / message / clipboard)
         */
        private String type;

        /**
         * 標籤 (選填)
         */
        private String label;

        /**
         * 連結 URI (當 type=uri 時必填)
         */
        private String linkUri;

        /**
         * 訊息文字 (當 type=message 時必填)
         */
        private String text;

        /**
         * 複製到剪貼簿的文字 (當 type=clipboard 時必填)
         */
        private String clipboardText;

        /**
         * 觸發區域
         */
        private AreaDto area;

        public void setType(String type) {
            this.type = type;
        }
        
        public void setTypeEnum(ImagemapActionType typeEnum) {
            this.type = typeEnum.getCode();
        }
    }

    /**
     * 區域 DTO
     */
    @Data
    @NoArgsConstructor
    public static class AreaDto implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
        
        public AreaDto(Integer x, Integer y, Integer width, Integer height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    /**
     * 影片 DTO (選填，暫不詳細實作)
     */
    @Data
    @NoArgsConstructor
    public static class VideoDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private String originalContentUrl;
        private String previewImageUrl;
        private AreaDto area;
        private ExternalLinkDto externalLink;
        
        @Data
        @NoArgsConstructor
        public static class ExternalLinkDto implements Serializable {
            private String linkUri;
            private String label;
        }
    }
}
