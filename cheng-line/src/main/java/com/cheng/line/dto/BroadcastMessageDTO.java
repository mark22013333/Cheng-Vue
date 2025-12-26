package com.cheng.line.dto;

import com.cheng.line.enums.ContentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 廣播訊息 DTO（發送給所有好友）
 *
 * @author cheng
 */
@Data
public class BroadcastMessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 頻道設定ID（可選，不提供則使用預設頻道）
     */
    private Integer configId;

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
     * Imagemap Message JSON（當 contentType 為 IMAGEMAP 時使用）
     */
    private String imagemapMessageJson;

    /**
     * 通知設定（是否發送通知）
     */
    private Boolean notificationDisabled = false;
}
