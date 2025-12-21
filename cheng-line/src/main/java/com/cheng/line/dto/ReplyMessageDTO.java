package com.cheng.line.dto;

import com.cheng.line.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 回覆訊息 DTO（回覆使用者訊息）
 *
 * @author cheng
 */
@Data
public class ReplyMessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 頻道設定ID（可選，不提供則使用預設頻道）
     */
    private Integer configId;

    /**
     * Reply Token（從 Webhook 事件中取得）
     */
    @NotBlank(message = "Reply Token 不能為空")
    private String replyToken;

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
     * 通知設定（是否發送通知）
     */
    private Boolean notificationDisabled = false;
}
