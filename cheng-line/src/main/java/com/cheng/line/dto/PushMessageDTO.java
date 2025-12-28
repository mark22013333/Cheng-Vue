package com.cheng.line.dto;

import com.cheng.line.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 推播訊息 DTO（單人推播）
 *
 * @author cheng
 */
@Data
public class PushMessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 頻道設定ID（可選，不提供則使用預設頻道）
     */
    private Integer configId;

    /**
     * 目標 LINE 使用者 ID
     */
    @NotBlank(message = "目標使用者ID不能為空")
    private String targetLineUserId;

    /**
     * 內容類型
     */
    @NotNull(message = "內容類型不能為空")
    private ContentType contentType;

    /**
     * 文字訊息內容（當 contentType 為 TEXT 時使用）
     */
    private String textMessage;

    /**
     * Emoji 列表（當 contentType 為 TEXT 時使用）
     */
    private List<EmojiDTO> emojis;

    /**
     * Quick Reply 設定（當 contentType 為 TEXT 時使用）
     */
    private QuickReplyDTO quickReply;

    /**
     * 圖片 URL（當 contentType 為 IMAGE 時使用）
     */
    private String imageUrl;

    /**
     * 預覽圖片 URL（當 contentType 為 IMAGE 時使用）
     */
    private String previewImageUrl;

    /**
     * Flex Message JSON（當 contentType 為 FLEX 時使用）
     */
    private String flexMessageJson;

    /**
     * Alt Text（當 contentType 為 FLEX 時使用）
     */
    private String altText;

    /**
     * Template Message JSON（當 contentType 為 TEMPLATE 時使用）
     */
    private String templateMessageJson;

    /**
     * 影片 URL（當 contentType 為 VIDEO 時使用）
     */
    private String videoUrl;

    /**
     * 影片預覽圖 URL（當 contentType 為 VIDEO 時使用）
     */
    private String videoPreviewImageUrl;

    /**
     * 音訊 URL（當 contentType 為 AUDIO 時使用）
     */
    private String audioUrl;

    /**
     * 音訊長度（毫秒，當 contentType 為 AUDIO 時使用）
     */
    private Long audioDuration;

    /**
     * 貼圖 Package ID（當 contentType 為 STICKER 時使用）
     */
    private String stickerPackageId;

    /**
     * 貼圖 ID（當 contentType 為 STICKER 時使用）
     */
    private String stickerId;

    /**
     * 位置標題（當 contentType 為 LOCATION 時使用）
     */
    private String locationTitle;

    /**
     * 位置地址（當 contentType 為 LOCATION 時使用）
     */
    private String locationAddress;

    /**
     * 緯度（當 contentType 為 LOCATION 時使用）
     */
    private Double latitude;

    /**
     * 經度（當 contentType 為 LOCATION 時使用）
     */
    private Double longitude;

    /**
     * Imagemap Message JSON（當 contentType 為 IMAGEMAP 時使用）
     */
    private String imagemapMessageJson;

    /**
     * 通知設定（是否發送通知）
     */
    private Boolean notificationDisabled = false;

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
     * Quick Reply DTO
     */
    @Data
    public static class QuickReplyDTO {
        private List<QuickReplyItemDTO> items;
    }

    /**
     * Quick Reply Item DTO
     */
    @Data
    public static class QuickReplyItemDTO {
        private String type;
        private String imageUrl;
        private QuickReplyActionDTO action;
    }

    /**
     * Quick Reply Action DTO
     */
    @Data
    public static class QuickReplyActionDTO {
        private String type;
        private String label;
        private String text;
        private String uri;
        private String data;
        private String displayText;
        private String mode;
        private String clipboardText;
    }
}
