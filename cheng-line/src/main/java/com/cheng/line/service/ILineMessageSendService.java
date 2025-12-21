package com.cheng.line.service;

import com.cheng.line.dto.SendMessageDTO;

/**
 * LINE 訊息發送服務（擴充版）
 * 支援所有 LINE 訊息類型
 *
 * @author cheng
 */
public interface ILineMessageSendService {

    /**
     * 發送訊息（通用介面）
     * 根據 DTO 中的 contentType 自動選擇對應的發送方法
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendMessage(SendMessageDTO dto);

    /**
     * 發送純文字訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendTextMessage(SendMessageDTO dto);

    /**
     * 發送圖片訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendImageMessage(SendMessageDTO dto);

    /**
     * 發送影片訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendVideoMessage(SendMessageDTO dto);

    /**
     * 發送音訊訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendAudioMessage(SendMessageDTO dto);

    /**
     * 發送位置訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendLocationMessage(SendMessageDTO dto);

    /**
     * 發送貼圖訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendStickerMessage(SendMessageDTO dto);

    /**
     * 發送 Imagemap 訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendImagemapMessage(SendMessageDTO dto);

    /**
     * 發送 Flex 訊息
     *
     * @param dto 訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendFlexMessage(SendMessageDTO dto);

    /**
     * 使用範本發送訊息
     *
     * @param dto 訊息 DTO（需包含 templateId）
     * @return 訊息記錄ID
     */
    Long sendTemplateMessage(SendMessageDTO dto);
}
