package com.cheng.line.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.client.LineClientFactory;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.dto.BroadcastMessageDTO;
import com.cheng.line.dto.FlexMessageDTO;
import com.cheng.line.dto.MulticastMessageDTO;
import com.cheng.line.dto.PushMessageDTO;
import com.cheng.line.dto.ReplyMessageDTO;
import com.cheng.line.enums.ContentType;
import com.cheng.line.enums.MessageType;
import com.cheng.line.enums.SendStatus;
import com.cheng.line.enums.TargetType;
import com.cheng.line.mapper.LineMessageLogMapper;
import com.cheng.line.mapper.LineUserMapper;
import com.cheng.line.service.ILineConfigService;
import com.cheng.line.service.ILineMessageService;
import com.cheng.line.service.ILineUserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * LINE 推播訊息 服務層實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineMessageServiceImpl implements ILineMessageService {

    private @Resource LineMessageLogMapper lineMessageLogMapper;
    private @Resource LineUserMapper lineUserMapper;
    private @Resource ILineConfigService lineConfigService;
    private @Resource ILineUserService lineUserService;
    private @Resource LineClientFactory lineClientFactory;

    /**
     * 查詢推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 推播訊息記錄
     */
    @Override
    public LineMessageLog selectLineMessageLogById(Long messageId) {
        return lineMessageLogMapper.selectLineMessageLogById(messageId);
    }

    /**
     * 查詢推播訊息記錄列表
     *
     * @param lineMessageLog 推播訊息記錄
     * @return 推播訊息記錄集合
     */
    @Override
    public List<LineMessageLog> selectLineMessageLogList(LineMessageLog lineMessageLog) {
        return lineMessageLogMapper.selectLineMessageLogList(lineMessageLog);
    }

    /**
     * 發送推播訊息（單人）
     *
     * @param pushMessageDTO 推播訊息 DTO
     * @return 訊息記錄ID
     */
    @Override
    @Transactional
    public Long sendPushMessage(PushMessageDTO pushMessageDTO) {
        // 取得頻道設定
        LineConfig config = getLineConfig(pushMessageDTO.getConfigId());

        // 建立訊息物件
        Message message = buildMessage(pushMessageDTO.getContentType(), pushMessageDTO);

        // 建立訊息記錄
        LineMessageLog messageLog = new LineMessageLog();
        messageLog.setConfigId(config.getConfigId());
        messageLog.setMessageType(MessageType.PUSH);
        messageLog.setContentType(pushMessageDTO.getContentType());
        messageLog.setTargetType(TargetType.SINGLE);
        messageLog.setTargetLineUserId(pushMessageDTO.getTargetLineUserId());
        messageLog.setTargetCount(1);
        messageLog.setSendStatus(SendStatus.PENDING);

        // 序列化訊息內容
        String messageContent = JacksonUtil.encodeToJson(message);
        if (StringUtils.isEmpty(messageContent)) {
            throw new ServiceException("序列化訊息內容失敗");
        }
        messageLog.setMessageContent(messageContent);

        // 儲存訊息記錄
        lineMessageLogMapper.insertLineMessageLog(messageLog);

        // 發送訊息
        try {
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            PushMessageRequest request = new PushMessageRequest(
                    pushMessageDTO.getTargetLineUserId(),
                    Collections.singletonList(message),
                    pushMessageDTO.getNotificationDisabled(),
                    null  // customAggregationUnits
            );

            messageLog.setSendStatus(SendStatus.SENDING);
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            PushMessageResponse response = client.pushMessage(UUID.randomUUID(), request).get().body();

            // 更新發送結果
            messageLog.setSendStatus(SendStatus.SUCCESS);
            messageLog.setSuccessCount(1);
            messageLog.setFailCount(0);
            messageLog.setSendTime(new Date());
            messageLog.setLineRequestId(response.sentMessages().get(0).id());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            // 更新使用者訊息統計
            lineUserService.incrementMessageCount(pushMessageDTO.getTargetLineUserId(), true);

            return messageLog.getMessageId();

        } catch (InterruptedException | ExecutionException e) {
            log.error("發送推播訊息失敗", e);

            // 更新失敗狀態
            messageLog.setSendStatus(SendStatus.FAILED);
            messageLog.setSuccessCount(0);
            messageLog.setFailCount(1);
            messageLog.setErrorMessage(e.getMessage());
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            throw new ServiceException("發送推播訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 發送推播訊息（多人）
     *
     * @param multicastMessageDTO 多人推播訊息 DTO
     * @return 訊息記錄ID
     */
    @Override
    @Transactional
    public Long sendMulticastMessage(MulticastMessageDTO multicastMessageDTO) {
        // 取得頻道設定
        LineConfig config = getLineConfig(multicastMessageDTO.getConfigId());

        // 建立訊息物件
        Message message = buildMessage(multicastMessageDTO.getContentType(), multicastMessageDTO);

        // 建立訊息記錄
        LineMessageLog messageLog = new LineMessageLog();
        messageLog.setConfigId(config.getConfigId());
        messageLog.setMessageType(MessageType.MULTICAST);
        messageLog.setContentType(multicastMessageDTO.getContentType());
        messageLog.setTargetType(TargetType.MULTIPLE);
        messageLog.setTargetCount(multicastMessageDTO.getTargetLineUserIds().size());
        messageLog.setSendStatus(SendStatus.PENDING);

        // 序列化訊息內容和目標列表
        String messageContent = JacksonUtil.encodeToJson(message);
        String targetUserIds = JacksonUtil.encodeToJson(multicastMessageDTO.getTargetLineUserIds());
        if (StringUtils.isEmpty(messageContent) || StringUtils.isEmpty(targetUserIds)) {
            throw new ServiceException("序列化訊息內容失敗");
        }
        messageLog.setMessageContent(messageContent);
        messageLog.setTargetUserIds(targetUserIds);

        // 儲存訊息記錄
        lineMessageLogMapper.insertLineMessageLog(messageLog);

        // 發送訊息
        try {
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            MulticastRequest request = new MulticastRequest(
                    Collections.singletonList(message),
                    new ArrayList<>(multicastMessageDTO.getTargetLineUserIds()),
                    multicastMessageDTO.getNotificationDisabled(),
                    null  // customAggregationUnits
            );

            messageLog.setSendStatus(SendStatus.SENDING);
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            client.multicast(UUID.randomUUID(), request).get();

            // 更新發送結果
            messageLog.setSendStatus(SendStatus.SUCCESS);
            messageLog.setSuccessCount(multicastMessageDTO.getTargetLineUserIds().size());
            messageLog.setFailCount(0);
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            // 更新所有目標使用者的訊息統計
            for (String userId : multicastMessageDTO.getTargetLineUserIds()) {
                lineUserService.incrementMessageCount(userId, true);
            }

            return messageLog.getMessageId();

        } catch (InterruptedException | ExecutionException e) {
            log.error("發送多人推播訊息失敗", e);

            // 更新失敗狀態
            messageLog.setSendStatus(SendStatus.FAILED);
            messageLog.setSuccessCount(0);
            messageLog.setFailCount(multicastMessageDTO.getTargetLineUserIds().size());
            messageLog.setErrorMessage(e.getMessage());
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            throw new ServiceException("發送多人推播訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 發送廣播訊息
     *
     * @param broadcastMessageDTO 廣播訊息 DTO
     * @return 訊息記錄ID
     */
    @Override
    @Transactional
    public Long sendBroadcastMessage(BroadcastMessageDTO broadcastMessageDTO) {
        // 取得頻道設定
        LineConfig config = getLineConfig(broadcastMessageDTO.getConfigId());

        // 建立訊息物件
        Message message = buildMessage(broadcastMessageDTO.getContentType(), broadcastMessageDTO);

        // 取得所有關注中的使用者數量
        int followingCount = lineUserMapper.countFollowingUsers();

        // 建立訊息記錄
        LineMessageLog messageLog = new LineMessageLog();
        messageLog.setConfigId(config.getConfigId());
        messageLog.setMessageType(MessageType.BROADCAST);
        messageLog.setContentType(broadcastMessageDTO.getContentType());
        messageLog.setTargetType(TargetType.ALL);
        messageLog.setTargetCount(followingCount);
        messageLog.setSendStatus(SendStatus.PENDING);

        // 序列化訊息內容
        String messageContent = JacksonUtil.encodeToJson(message);
        if (StringUtils.isEmpty(messageContent)) {
            throw new ServiceException("序列化訊息內容失敗");
        }
        messageLog.setMessageContent(messageContent);

        // 儲存訊息記錄
        lineMessageLogMapper.insertLineMessageLog(messageLog);

        // 發送訊息
        try {
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            BroadcastRequest request = new BroadcastRequest(
                    Collections.singletonList(message),
                    broadcastMessageDTO.getNotificationDisabled()
            );

            messageLog.setSendStatus(SendStatus.SENDING);
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            client.broadcast(UUID.randomUUID(), request).get();

            // 更新發送結果
            messageLog.setSendStatus(SendStatus.SUCCESS);
            messageLog.setSuccessCount(followingCount);
            messageLog.setFailCount(0);
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            return messageLog.getMessageId();

        } catch (InterruptedException | ExecutionException e) {
            log.error("發送廣播訊息失敗", e);

            // 更新失敗狀態
            messageLog.setSendStatus(SendStatus.FAILED);
            messageLog.setSuccessCount(0);
            messageLog.setFailCount(followingCount);
            messageLog.setErrorMessage(e.getMessage());
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            throw new ServiceException("發送廣播訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 回覆訊息
     *
     * @param replyMessageDTO 回覆訊息 DTO
     * @return 訊息記錄ID
     */
    @Override
    @Transactional
    public Long replyMessage(ReplyMessageDTO replyMessageDTO) {
        // 取得頻道設定
        LineConfig config = getLineConfig(replyMessageDTO.getConfigId());

        // 建立訊息物件
        Message message = buildMessage(replyMessageDTO.getContentType(), replyMessageDTO);

        // 建立訊息記錄
        LineMessageLog messageLog = new LineMessageLog();
        messageLog.setConfigId(config.getConfigId());
        messageLog.setMessageType(MessageType.REPLY);
        messageLog.setContentType(replyMessageDTO.getContentType());
        messageLog.setTargetType(TargetType.SINGLE);
        messageLog.setTargetCount(1);
        messageLog.setSendStatus(SendStatus.PENDING);

        // 序列化訊息內容
        String messageContent = JacksonUtil.encodeToJson(message);
        if (StringUtils.isEmpty(messageContent)) {
            throw new ServiceException("序列化訊息內容失敗");
        }
        messageLog.setMessageContent(messageContent);

        // 儲存訊息記錄
        lineMessageLogMapper.insertLineMessageLog(messageLog);

        // 發送訊息
        try {
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            ReplyMessageRequest request = new ReplyMessageRequest(
                    replyMessageDTO.getReplyToken(),
                    Collections.singletonList(message),
                    replyMessageDTO.getNotificationDisabled()
            );

            messageLog.setSendStatus(SendStatus.SENDING);
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            ReplyMessageResponse response = client.replyMessage(request).get().body();

            // 更新發送結果
            messageLog.setSendStatus(SendStatus.SUCCESS);
            messageLog.setSuccessCount(1);
            messageLog.setFailCount(0);
            messageLog.setSendTime(new Date());
            messageLog.setLineRequestId(response.sentMessages().get(0).id());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            return messageLog.getMessageId();

        } catch (InterruptedException | ExecutionException e) {
            log.error("回覆訊息失敗", e);

            // 更新失敗狀態
            messageLog.setSendStatus(SendStatus.FAILED);
            messageLog.setSuccessCount(0);
            messageLog.setFailCount(1);
            messageLog.setErrorMessage(e.getMessage());
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            throw new ServiceException("回覆訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 發送 Flex Message（彈性訊息）
     * 根據 targetType 決定發送方式：SINGLE/MULTIPLE/ALL
     *
     * @param flexMessageDTO Flex Message DTO
     * @return 訊息記錄ID
     */
    @Override
    @Transactional
    public Long sendFlexMessage(FlexMessageDTO flexMessageDTO) {
        // 取得頻道設定
        LineConfig config = getLineConfig(flexMessageDTO.getConfigId());

        // 解析 Flex Message JSON
        FlexMessage flexMessage = buildFlexMessage(flexMessageDTO);

        // 根據目標類型決定發送方式
        TargetType targetType = TargetType.fromCode(flexMessageDTO.getTargetType());
        MessageType messageType = MessageType.fromCode(flexMessageDTO.getMessageType());

        // 建立訊息記錄
        LineMessageLog messageLog = new LineMessageLog();
        messageLog.setConfigId(config.getConfigId());
        messageLog.setMessageType(messageType);
        messageLog.setContentType(ContentType.FLEX);
        messageLog.setTargetType(targetType);
        messageLog.setMessageContent(flexMessageDTO.getFlexContent());
        messageLog.setSendStatus(SendStatus.PENDING);

        // 根據目標類型設定目標資訊
        switch (targetType) {
            case SINGLE:
                messageLog.setTargetLineUserId(flexMessageDTO.getTargetLineUserId());
                messageLog.setTargetCount(1);
                break;
            case MULTIPLE:
                if (flexMessageDTO.getTargetUserIds() == null || flexMessageDTO.getTargetUserIds().length == 0) {
                    throw new ServiceException("多人推播時目標使用者列表不能為空");
                }
                messageLog.setTargetUserIds(JacksonUtil.encodeToJson(Arrays.asList(flexMessageDTO.getTargetUserIds())));
                messageLog.setTargetCount(flexMessageDTO.getTargetUserIds().length);
                break;
            case ALL:
                // 查詢所有關注中的使用者數量
                int totalUsers = lineUserMapper.countFollowingUsers();
                messageLog.setTargetCount(totalUsers);
                break;
        }

        // 儲存訊息記錄
        lineMessageLogMapper.insertLineMessageLog(messageLog);
        log.info("建立 Flex Message 記錄，ID: {}", messageLog.getMessageId());

        try {
            // 取得 Messaging API Client（復用快取）
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            // 根據訊息類型發送
            switch (messageType) {
                case PUSH:
                    sendPushFlexMessage(client, flexMessageDTO.getTargetLineUserId(), flexMessage, messageLog);
                    break;
                case MULTICAST:
                    sendMulticastFlexMessage(client, Arrays.asList(flexMessageDTO.getTargetUserIds()), flexMessage, messageLog);
                    break;
                case BROADCAST:
                    sendBroadcastFlexMessage(client, flexMessage, messageLog);
                    break;
                case REPLY:
                    sendReplyFlexMessage(client, flexMessageDTO.getReplyToken(), flexMessage, messageLog);
                    break;
                default:
                    throw new ServiceException("不支援的訊息類型");
            }

            return messageLog.getMessageId();
        } catch (Exception e) {
            log.error("發送 Flex Message 失敗", e);
            updateMessageLogStatus(messageLog.getMessageId(), SendStatus.FAILED, e.getMessage());
            throw new ServiceException("發送 Flex Message 失敗: " + e.getMessage());
        }
    }

    /**
     * 刪除推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 結果
     */
    @Override
    public int deleteLineMessageLogById(Long messageId) {
        return lineMessageLogMapper.deleteLineMessageLogById(messageId);
    }

    /**
     * 批次刪除推播訊息記錄
     *
     * @param messageIds 訊息ID陣列
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteLineMessageLogByIds(Long[] messageIds) {
        return lineMessageLogMapper.deleteLineMessageLogByIds(messageIds);
    }

    /**
     * 取得 LINE 頻道設定
     *
     * @param configId 設定ID（可為 null，使用預設）
     * @return LINE 頻道設定
     */
    private LineConfig getLineConfig(Integer configId) {
        LineConfig config;
        if (configId != null) {
            config = lineConfigService.selectLineConfigById(configId);
        } else {
            config = lineConfigService.selectDefaultLineConfig();
        }

        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        return config;
    }

    /**
     * 建立 LINE 訊息物件
     *
     * @param contentType 內容類型
     * @param dto         訊息 DTO
     * @return LINE 訊息物件
     */
    private Message buildMessage(ContentType contentType, Object dto) {
        // TODO: 實作 Template Message
        return switch (contentType) {
            case TEXT -> buildTextMessage(dto);
            case IMAGE -> buildImageMessage(dto);
            case FLEX -> {
                if (dto instanceof FlexMessageDTO) {
                    yield buildFlexMessage((FlexMessageDTO) dto);
                }
                throw new ServiceException("Flex Message DTO 類型錯誤");
            }
            case TEMPLATE -> throw new ServiceException("暫不支援 " + contentType.getDescription() + " 類型");
            default -> throw new ServiceException("不支援的訊息類型");
        };
    }

    /**
     * 建立文字訊息
     */
    private TextMessage buildTextMessage(Object dto) {
        String text = null;

        if (dto instanceof PushMessageDTO) {
            text = ((PushMessageDTO) dto).getTextMessage();
        } else if (dto instanceof MulticastMessageDTO) {
            text = ((MulticastMessageDTO) dto).getTextMessage();
        } else if (dto instanceof BroadcastMessageDTO) {
            text = ((BroadcastMessageDTO) dto).getTextMessage();
        } else if (dto instanceof ReplyMessageDTO) {
            text = ((ReplyMessageDTO) dto).getTextMessage();
        }

        if (StringUtils.isEmpty(text)) {
            throw new ServiceException("文字訊息內容不能為空");
        }

        return new TextMessage(text);
    }

    /**
     * 建立圖片訊息
     */
    private ImageMessage buildImageMessage(Object dto) {
        String imageUrl = null;
        String previewImageUrl = null;

        if (dto instanceof PushMessageDTO) {
            imageUrl = ((PushMessageDTO) dto).getImageUrl();
            previewImageUrl = ((PushMessageDTO) dto).getPreviewImageUrl();
        } else if (dto instanceof MulticastMessageDTO) {
            imageUrl = ((MulticastMessageDTO) dto).getImageUrl();
            previewImageUrl = ((MulticastMessageDTO) dto).getPreviewImageUrl();
        } else if (dto instanceof BroadcastMessageDTO) {
            imageUrl = ((BroadcastMessageDTO) dto).getImageUrl();
            previewImageUrl = ((BroadcastMessageDTO) dto).getPreviewImageUrl();
        } else if (dto instanceof ReplyMessageDTO) {
            imageUrl = ((ReplyMessageDTO) dto).getImageUrl();
            previewImageUrl = ((ReplyMessageDTO) dto).getPreviewImageUrl();
        }

        if (StringUtils.isEmpty(imageUrl)) {
            throw new ServiceException("圖片 URL 不能為空");
        }

        // 如果沒有提供預覽圖，使用原圖
        if (StringUtils.isEmpty(previewImageUrl)) {
            previewImageUrl = imageUrl;
        }

        return new ImageMessage(URI.create(imageUrl), URI.create(previewImageUrl));
    }

    /**
     * 建立 Flex Message
     * TODO: 待完善 - 需要根據 LINE SDK 9.x 的實際 API 調整
     * 目前先拋出異常，前端應使用標準的文字或圖片訊息
     */
    private FlexMessage buildFlexMessage(FlexMessageDTO dto) {
        // 暫時不實作 Flex Message 解析
        // LINE SDK 9.x 的 FlexContainer 需要完整的物件模型
        // 建議前端先使用 Flex Message Simulator 產生完整的 JSON
        throw new ServiceException("Flex Message 功能開發中，請先使用文字或圖片訊息");
    }

    /**
     * 發送單人 Flex Message
     */
    private void sendPushFlexMessage(MessagingApiClient client, String userId, FlexMessage message, LineMessageLog messageLog) throws ExecutionException, InterruptedException {
        PushMessageRequest request = new PushMessageRequest(
                userId,
                Collections.singletonList(message),
                false,  // notificationDisabled
                null    // customAggregationUnits
        );

        PushMessageResponse response = client.pushMessage(UUID.randomUUID(), request).get().body();
        updateMessageLogStatus(messageLog.getMessageId(), SendStatus.SUCCESS, null);
        log.info("單人 Flex Message 發送成功，Request ID: {}", response.sentMessages().get(0).id());

        // 增加使用者發送訊息計數
        lineUserService.incrementMessageCount(userId, true);
    }

    /**
     * 發送多人 Flex Message
     */
    private void sendMulticastFlexMessage(MessagingApiClient client, List<String> userIds, FlexMessage message, LineMessageLog messageLog) throws ExecutionException, InterruptedException {
        MulticastRequest request = new MulticastRequest(
                Collections.singletonList(message),  // messages
                userIds,                             // to
                false,                               // notificationDisabled
                null                                 // customAggregationUnits
        );

        client.multicast(UUID.randomUUID(), request).get();
        updateMessageLogStatus(messageLog.getMessageId(), SendStatus.SUCCESS, null);
        log.info("多人 Flex Message 發送成功");

        // 增加所有使用者的發送訊息計數
        userIds.forEach(userId -> {
            try {
                lineUserService.incrementMessageCount(userId, true);
            } catch (Exception e) {
                log.warn("更新使用者訊息計數失敗: {}", userId, e);
            }
        });
    }

    /**
     * 發送廣播 Flex Message
     */
    private void sendBroadcastFlexMessage(MessagingApiClient client, FlexMessage message, LineMessageLog messageLog) throws ExecutionException, InterruptedException {
        BroadcastRequest request = new BroadcastRequest(
                Collections.singletonList(message),  // messages
                false                                // notificationDisabled
        );

        client.broadcast(UUID.randomUUID(), request).get();
        updateMessageLogStatus(messageLog.getMessageId(), SendStatus.SUCCESS, null);
        log.info("廣播 Flex Message 發送成功");
    }

    /**
     * 回覆 Flex Message
     */
    private void sendReplyFlexMessage(MessagingApiClient client, String replyToken, FlexMessage message, LineMessageLog messageLog) throws ExecutionException, InterruptedException {
        ReplyMessageRequest request = new ReplyMessageRequest(
                replyToken,
                Collections.singletonList(message),
                false   // notificationDisabled
        );

        client.replyMessage(request).get();
        updateMessageLogStatus(messageLog.getMessageId(), SendStatus.SUCCESS, null);
        log.info("回覆 Flex Message 成功");
    }

    /**
     * 更新訊息記錄狀態
     */
    private void updateMessageLogStatus(Long messageId, SendStatus status, String errorMessage) {
        LineMessageLog updateLog = new LineMessageLog();
        updateLog.setMessageId(messageId);
        updateLog.setSendStatus(status);
        updateLog.setErrorMessage(errorMessage);
        updateLog.setSendTime(new Date());
        lineMessageLogMapper.updateLineMessageLog(updateLog);
    }
}
