package com.cheng.line.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Flex Message DTO
 * 用於發送 LINE Flex Message（彈性訊息）
 *
 * @author cheng
 */
@Data
public class FlexMessageDTO {

    /**
     * 頻道設定 ID
     */
    @NotNull(message = "頻道設定ID不能為空")
    private Integer configId;

    /**
     * 目標使用者 LINE User ID（單人推播時使用）
     */
    private String targetLineUserId;

    /**
     * 目標使用者 LINE User ID 列表（多人推播時使用）
     */
    private String[] targetUserIds;

    /**
     * 推播對象類型：SINGLE/MULTIPLE/ALL
     */
    @NotBlank(message = "推播對象類型不能為空")
    private String targetType;

    /**
     * Alt Text（當裝置不支援 Flex Message 時顯示的替代文字）
     */
    @NotBlank(message = "替代文字不能為空")
    private String altText;

    /**
     * Flex Message 內容（JSON 格式）
     * 符合 LINE Flex Message 規範
     */
    @NotBlank(message = "Flex Message 內容不能為空")
    private String flexContent;

    /**
     * 訊息類型：PUSH/MULTICAST/BROADCAST
     */
    @NotBlank(message = "訊息類型不能為空")
    private String messageType;

    /**
     * Reply Token（回覆訊息時使用）
     */
    private String replyToken;
}
