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
     * Template Message JSON（當 contentType 為 TEMPLATE 時使用）
     */
    private String templateMessageJson;

    /**
     * 通知設定（是否發送通知）
     */
    private Boolean notificationDisabled = false;
}
