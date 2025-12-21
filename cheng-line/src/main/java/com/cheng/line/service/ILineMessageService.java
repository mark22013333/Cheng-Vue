package com.cheng.line.service;

import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.dto.BroadcastMessageDTO;
import com.cheng.line.dto.FlexMessageDTO;
import com.cheng.line.dto.MulticastMessageDTO;
import com.cheng.line.dto.PushMessageDTO;
import com.cheng.line.dto.ReplyMessageDTO;

import java.util.List;

/**
 * LINE 推播訊息 服務層
 *
 * @author cheng
 */
public interface ILineMessageService {

    /**
     * 查詢推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 推播訊息記錄
     */
    LineMessageLog selectLineMessageLogById(Long messageId);

    /**
     * 查詢推播訊息記錄列表
     *
     * @param lineMessageLog 推播訊息記錄
     * @return 推播訊息記錄集合
     */
    List<LineMessageLog> selectLineMessageLogList(LineMessageLog lineMessageLog);

    /**
     * 發送推播訊息（單人）
     *
     * @param pushMessageDTO 推播訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendPushMessage(PushMessageDTO pushMessageDTO);

    /**
     * 發送推播訊息（單人，多則訊息）
     * 一次 API 呼叫發送多則訊息（最多 5 則）
     *
     * @param targetLineUserId 目標 LINE 使用者 ID
     * @param pushMessageDTOs  多則訊息 DTO 列表
     * @return 訊息記錄ID
     */
    Long sendPushMessages(String targetLineUserId, List<PushMessageDTO> pushMessageDTOs);

    /**
     * 發送推播訊息（多人）
     *
     * @param multicastMessageDTO 多人推播訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendMulticastMessage(MulticastMessageDTO multicastMessageDTO);

    /**
     * 發送廣播訊息
     *
     * @param broadcastMessageDTO 廣播訊息 DTO
     * @return 訊息記錄ID
     */
    Long sendBroadcastMessage(BroadcastMessageDTO broadcastMessageDTO);

    /**
     * 回覆訊息
     *
     * @param replyMessageDTO 回覆訊息 DTO
     * @return 訊息記錄ID
     */
    Long replyMessage(ReplyMessageDTO replyMessageDTO);

    /**
     * 發送 Flex Message（彈性訊息）
     * 根據 targetType 決定發送方式：SINGLE/MULTIPLE/ALL
     *
     * @param flexMessageDTO Flex Message DTO
     * @return 訊息記錄ID
     */
    Long sendFlexMessage(FlexMessageDTO flexMessageDTO);

    /**
     * 刪除推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 結果
     */
    int deleteLineMessageLogById(Long messageId);

    /**
     * 批次刪除推播訊息記錄
     *
     * @param messageIds 訊息ID陣列
     * @return 結果
     */
    int deleteLineMessageLogByIds(Long[] messageIds);
}
