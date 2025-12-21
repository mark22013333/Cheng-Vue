package com.cheng.line.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 通用訊息發送 DTO
 * 支援所有 LINE 訊息類型的發送
 *
 * @author cheng
 */
@Data
public class SendMessageDTO {

    /**
     * 頻道設定 ID
     */
    @NotNull(message = "頻道設定ID不能為空")
    private Integer configId;

    /**
     * 訊息類型：PUSH/MULTICAST/BROADCAST/REPLY
     */
    @NotBlank(message = "訊息類型不能為空")
    private String messageType;

    /**
     * 內容類型：TEXT/IMAGE/VIDEO/AUDIO/LOCATION/STICKER/IMAGEMAP/FLEX
     */
    @NotBlank(message = "內容類型不能為空")
    private String contentType;

    /**
     * 目標使用者 LINE User ID（單人推播時使用）
     */
    private String targetLineUserId;

    /**
     * 目標使用者 LINE User ID 列表（多人推播時使用）
     */
    private List<String> targetLineUserIds;

    /**
     * Reply Token（回覆訊息時使用）
     */
    private String replyToken;

    /**
     * 是否停用通知（預設 false）
     */
    private Boolean notificationDisabled = false;

    // ========== TEXT ==========

    /**
     * 文字訊息內容
     */
    private String text;

    /**
     * Emoji 列表（LINE emoji 格式）
     */
    private List<EmojiDTO> emojis;

    // ========== IMAGE ==========

    /**
     * 圖片 URL（必須 HTTPS）
     */
    private String imageUrl;

    /**
     * 預覽圖 URL（必須 HTTPS，建議 240x240）
     */
    private String previewImageUrl;

    // ========== VIDEO ==========

    /**
     * 影片 URL（必須 HTTPS，MP4 格式）
     */
    private String videoUrl;

    /**
     * 影片預覽圖 URL（必須 HTTPS，JPEG 格式）
     */
    private String videoPreviewUrl;

    /**
     * 追蹤ID（用於追蹤影片播放）
     */
    private String trackingId;

    // ========== AUDIO ==========

    /**
     * 音訊 URL（必須 HTTPS，M4A 格式）
     */
    private String audioUrl;

    /**
     * 音訊長度（毫秒）
     */
    private Long audioDuration;

    // ========== LOCATION ==========

    /**
     * 地點標題
     */
    private String locationTitle;

    /**
     * 地點地址
     */
    private String locationAddress;

    /**
     * 緯度
     */
    private Double latitude;

    /**
     * 經度
     */
    private Double longitude;

    // ========== STICKER ==========

    /**
     * 貼圖 Package ID
     */
    private String packageId;

    /**
     * 貼圖 ID
     */
    private String stickerId;

    // ========== IMAGEMAP ==========

    /**
     * Imagemap 基礎 URL
     */
    private String imagemapBaseUrl;

    /**
     * Imagemap 替代文字
     */
    private String imagemapAltText;

    /**
     * Imagemap 寬度
     */
    private Integer imagemapWidth;

    /**
     * Imagemap 高度
     */
    private Integer imagemapHeight;

    /**
     * Imagemap 動作區域列表
     */
    private List<ImagemapActionDTO> imagemapActions;

    // ========== FLEX ==========

    /**
     * Flex Message 替代文字
     */
    private String flexAltText;

    /**
     * Flex Message 內容（JSON 字串）
     */
    private String flexContent;

    // ========== TEMPLATE ==========

    /**
     * 範本 ID（使用已儲存的範本）
     */
    private Long templateId;

    /**
     * 範本變數（用於替換範本中的 {{variable}}）
     */
    private Map<String, String> templateVariables;

    /**
     * Emoji DTO
     */
    @Data
    public static class EmojiDTO {
        private Integer index;
        private String productId;
        private String emojiId;
    }

    /**
     * Imagemap Action DTO
     */
    @Data
    public static class ImagemapActionDTO {
        private String type;  // uri / message
        private String linkUri;
        private String text;
        private String label;
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }
}
