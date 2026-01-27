package com.cheng.line.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.client.LineClientFactory;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.dto.BroadcastMessageDTO;
import com.cheng.line.dto.FlexMessageDTO;
import com.cheng.line.dto.ImagemapMessageDto;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        Message message = buildMessage(pushMessageDTO.getContentType(), pushMessageDTO, config);

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
            messageLog.setLineRequestId(response.sentMessages().getFirst().id());
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
     * 發送推播訊息（單人，多則訊息）
     * 一次 API 呼叫發送多則訊息（最多 5 則）
     *
     * @param targetLineUserId 目標 LINE 使用者 ID
     * @param pushMessageDTOs  多則訊息 DTO 列表
     * @return 訊息記錄ID
     */
    @Override
    @Transactional
    public Long sendPushMessages(String targetLineUserId, List<PushMessageDTO> pushMessageDTOs) {
        if (pushMessageDTOs == null || pushMessageDTOs.isEmpty()) {
            throw new ServiceException("訊息列表不能為空");
        }
        if (pushMessageDTOs.size() > 5) {
            throw new ServiceException("LINE API 限制：單次最多發送 5 則訊息");
        }

        // 取得頻道設定（使用第一則訊息的設定，或預設）
        LineConfig config = getLineConfig(pushMessageDTOs.getFirst().getConfigId());

        // 建立所有訊息物件
        List<Message> messages = new ArrayList<>();
        for (PushMessageDTO dto : pushMessageDTOs) {
            messages.add(buildMessage(dto.getContentType(), dto, config));
        }

        // 建立訊息記錄
        LineMessageLog messageLog = new LineMessageLog();
        messageLog.setConfigId(config.getConfigId());
        messageLog.setMessageType(MessageType.PUSH);
        messageLog.setContentType(pushMessageDTOs.getFirst().getContentType()); // 使用第一則訊息的類型
        messageLog.setTargetType(TargetType.SINGLE);
        messageLog.setTargetLineUserId(targetLineUserId);
        messageLog.setTargetCount(1);
        messageLog.setSendStatus(SendStatus.PENDING);

        // 序列化訊息內容（記錄所有訊息）
        String messageContent = JacksonUtil.encodeToJson(messages);
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
                    targetLineUserId,
                    messages,  // 一次發送多則訊息
                    pushMessageDTOs.getFirst().getNotificationDisabled(),
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
            if (response.sentMessages() != null && !response.sentMessages().isEmpty()) {
                messageLog.setLineRequestId(response.sentMessages().getFirst().id());
            }
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            // 更新使用者訊息統計
            lineUserService.incrementMessageCount(targetLineUserId, true);

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
        Message message = buildMessage(multicastMessageDTO.getContentType(), multicastMessageDTO, config);

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
        Message message = buildMessage(broadcastMessageDTO.getContentType(), broadcastMessageDTO, config);

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
        Message message = buildMessage(replyMessageDTO.getContentType(), replyMessageDTO, config);

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
     * @param config      LINE 頻道設定（用於取得 webhookBaseUrl）
     * @return LINE 訊息物件
     */
    private Message buildMessage(ContentType contentType, Object dto, LineConfig config) {
        return switch (contentType) {
            case TEXT -> buildTextMessage(dto);
            case IMAGE -> buildImageMessage(dto);
            case VIDEO -> buildVideoMessage(dto);
            case AUDIO -> buildAudioMessage(dto);
            case STICKER -> buildStickerMessage(dto);
            case LOCATION -> buildLocationMessage(dto);
            case FLEX -> buildFlexMessageFromDto(dto);
            case IMAGEMAP -> buildImagemapMessage(dto, config);
            case TEMPLATE -> buildTemplateMessage(dto);
        };
    }

    /**
     * 建立 Imagemap 訊息
     *
     * @param dto    訊息 DTO
     * @param config LINE 頻道設定（用於取得 webhookBaseUrl）
     */
    private ImagemapMessage buildImagemapMessage(Object dto, LineConfig config) {
        String json = null;
        String altText = "Imagemap Message";

        if (dto instanceof PushMessageDTO) {
            json = ((PushMessageDTO) dto).getImagemapMessageJson();
            altText = ((PushMessageDTO) dto).getAltText();
        } else if (dto instanceof MulticastMessageDTO) {
            json = ((MulticastMessageDTO) dto).getImagemapMessageJson();
            altText = ((MulticastMessageDTO) dto).getAltText();
        } else if (dto instanceof BroadcastMessageDTO) {
            json = ((BroadcastMessageDTO) dto).getImagemapMessageJson();
            altText = ((BroadcastMessageDTO) dto).getAltText();
        } else if (dto instanceof ReplyMessageDTO) {
            json = ((ReplyMessageDTO) dto).getImagemapMessageJson();
            altText = ((ReplyMessageDTO) dto).getAltText();
        }

        if (StringUtils.isEmpty(json)) {
            throw new ServiceException("Imagemap Message 內容不能為空");
        }
        
        // 為了相容性，如果是 DTO 轉來的 altText 為空，嘗試從 JSON 裡解析 (雖然 DTO 欄位通常優先)
        // 但這裡我們主要依賴 DTO 的 JSON 字串反序列化成 ImagemapMessageDto 再轉成 SDK 的 ImagemapMessage
        
        try {
            ImagemapMessageDto imagemapDto = JacksonUtil.decodeFromJson(json, ImagemapMessageDto.class);
            if (imagemapDto == null) {
                throw new ServiceException("Imagemap JSON 解析為空");
            }
            
            // 使用 DTO 中的 altText (如果外層沒傳，用內層的)
            if (StringUtils.isEmpty(altText) || "Imagemap Message".equals(altText)) {
                if (StringUtils.isNotEmpty(imagemapDto.getAltText())) {
                    altText = imagemapDto.getAltText();
                }
            }

            // 處理 baseUrl：如果是相對路徑，需要加上網域組合成完整的 HTTPS URL
            // LINE API 要求 baseUrl 必須是完整的 HTTPS URL
            String baseUrl = imagemapDto.getBaseUrl();
            if (StringUtils.isNotEmpty(baseUrl) && baseUrl.startsWith("/profile/")) {
                // 從 config 取得 webhookBaseUrl 作為網域
                String webhookBaseUrl = config.getWebhookBaseUrl();
                if (StringUtils.isEmpty(webhookBaseUrl)) {
                    webhookBaseUrl = lineConfigService.getDefaultWebhookBaseUrl();
                }
                if (StringUtils.isNotEmpty(webhookBaseUrl)) {
                    // 確保 webhookBaseUrl 以 https:// 開頭
                    if (!webhookBaseUrl.startsWith("https://")) {
                        webhookBaseUrl = "https://" + webhookBaseUrl;
                    }
                    // 移除結尾的斜線
                    if (webhookBaseUrl.endsWith("/")) {
                        webhookBaseUrl = webhookBaseUrl.substring(0, webhookBaseUrl.length() - 1);
                    }
                    // 組合完整 URL
                    baseUrl = webhookBaseUrl + baseUrl;
                    log.info("Imagemap baseUrl 轉換：{} -> {}", imagemapDto.getBaseUrl(), baseUrl);
                } else {
                    throw new ServiceException("無法取得 webhookBaseUrl，無法發送 Imagemap 訊息");
                }
            }

            // 轉換 BaseSize
            ImagemapBaseSize baseSize = new ImagemapBaseSize(
                imagemapDto.getBaseSize().getHeight(),
                imagemapDto.getBaseSize().getWidth()
            );

            // 轉換 Actions
            List<ImagemapAction> actions = new ArrayList<>();
            for (ImagemapMessageDto.ActionDto actionDto : imagemapDto.getActions()) {
                ImagemapArea area = new ImagemapArea(
                    actionDto.getArea().getX(),
                    actionDto.getArea().getY(),
                    actionDto.getArea().getWidth(),
                    actionDto.getArea().getHeight()
                );

                if ("uri".equalsIgnoreCase(actionDto.getType())) {
                    actions.add(new URIImagemapAction(
                        area,
                        actionDto.getLinkUri(),
                        null  // label (optional)
                    ));
                } else if ("message".equalsIgnoreCase(actionDto.getType())) {
                    actions.add(new MessageImagemapAction(
                        area,
                        actionDto.getText(),
                        null  // label (optional)
                    ));
                }
            }
            
            // 轉換 Video (如果有)
            if (imagemapDto.getVideo() != null) {
                 // LINE SDK 目前對 Video Imagemap 支援可能有限，或者 ImagemapMessage 建構子不一定支援 video 參數
                 // 檢查 ImagemapMessage 建構子:
                 // new ImagemapMessage(baseUrl, altText, baseSize, actions)
                 // new ImagemapMessage(baseUrl, altText, baseSize, actions, video)
                 // 假設 SDK 支援 Video
                 
                 ImagemapMessageDto.VideoDto videoDto = imagemapDto.getVideo();
                 ImagemapArea videoArea = new ImagemapArea(
                     videoDto.getArea().getX(),
                     videoDto.getArea().getY(),
                     videoDto.getArea().getWidth(),
                     videoDto.getArea().getHeight()
                 );
                 
                 ImagemapVideo video = new ImagemapVideo(
                     URI.create(videoDto.getOriginalContentUrl()),
                     URI.create(videoDto.getPreviewImageUrl()),
                     videoArea,
                     new ImagemapExternalLink(
                         URI.create(videoDto.getExternalLink().getLinkUri()),
                         videoDto.getExternalLink().getLabel()
                     )
                 );
                 
                 return new ImagemapMessage(
                     null,  // quickReply
                     null,  // sender
                     URI.create(baseUrl),
                     altText,
                     baseSize,
                     actions,
                     video
                 );
            }

            return new ImagemapMessage(
                null,  // quickReply
                null,  // sender
                URI.create(baseUrl),
                altText,
                baseSize,
                actions,
                null   // video
            );

        } catch (Exception e) {
            log.error("建立 Imagemap 訊息失敗", e);
            throw new ServiceException("建立 Imagemap 訊息失敗: " + e.getMessage());
        }
    }

    /**
     * 建立 Template 訊息（Buttons/Confirm/Carousel/Image Carousel）
     */
    private TemplateMessage buildTemplateMessage(Object dto) {
        String json = null;
        String altText = "模板訊息";

        if (dto instanceof PushMessageDTO pushDto) {
            json = pushDto.getTemplateMessageJson();
            if (StringUtils.isNotEmpty(pushDto.getAltText())) {
                altText = pushDto.getAltText();
            }
        } else if (dto instanceof MulticastMessageDTO multicastDto) {
            json = multicastDto.getTemplateMessageJson();
            if (StringUtils.isNotEmpty(multicastDto.getAltText())) {
                altText = multicastDto.getAltText();
            }
        } else if (dto instanceof BroadcastMessageDTO broadcastDto) {
            json = broadcastDto.getTemplateMessageJson();
            if (StringUtils.isNotEmpty(broadcastDto.getAltText())) {
                altText = broadcastDto.getAltText();
            }
        } else if (dto instanceof ReplyMessageDTO replyDto) {
            json = replyDto.getTemplateMessageJson();
            if (StringUtils.isNotEmpty(replyDto.getAltText())) {
                altText = replyDto.getAltText();
            }
        }

        if (StringUtils.isEmpty(json)) {
            throw new ServiceException("Template Message 內容不能為空");
        }

        try {
            JsonNode rootNode = JacksonUtil.toJsonNode(json);
            if (rootNode == null) {
                throw new ServiceException("Template Message JSON 解析失敗");
            }

            // 從 JSON 中取得 altText（如果有的話）
            if (rootNode.has("altText") && StringUtils.isNotEmpty(rootNode.get("altText").asText())) {
                altText = rootNode.get("altText").asText();
            }

            // 取得 template 物件
            JsonNode templateNode = rootNode.has("template") ? rootNode.get("template") : rootNode;
            if (templateNode == null || !templateNode.has("type")) {
                throw new ServiceException("Template Message 缺少 template 物件或 type 欄位");
            }

            String templateType = templateNode.get("type").asText();
            Template template = switch (templateType) {
                case "buttons" -> buildButtonsTemplate(templateNode);
                case "confirm" -> buildConfirmTemplate(templateNode);
                case "carousel" -> buildCarouselTemplate(templateNode);
                case "image_carousel" -> buildImageCarouselTemplate(templateNode);
                default -> throw new ServiceException("不支援的 Template 類型：" + templateType);
            };

            return new TemplateMessage(altText, template);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("建立 Template 訊息失敗", e);
            throw new ServiceException("建立 Template 訊息失敗: " + e.getMessage());
        }
    }

    private ButtonsTemplate buildButtonsTemplate(JsonNode node) {
        String text = node.has("text") ? node.get("text").asText() : "";
        String title = node.has("title") && !node.get("title").isNull() ? node.get("title").asText() : null;

        URI thumbnailImageUrl = null;
        if (node.has("thumbnailImageUrl") && !node.get("thumbnailImageUrl").isNull() 
                && StringUtils.isNotEmpty(node.get("thumbnailImageUrl").asText())) {
            thumbnailImageUrl = URI.create(node.get("thumbnailImageUrl").asText());
        }

        String imageAspectRatio = node.has("imageAspectRatio") ? node.get("imageAspectRatio").asText() : "rectangle";
        String imageSize = node.has("imageSize") ? node.get("imageSize").asText() : "cover";
        String imageBackgroundColor = node.has("imageBackgroundColor") && !node.get("imageBackgroundColor").isNull() 
                ? node.get("imageBackgroundColor").asText() : null;

        List<Action> actions = buildTemplateActions(node.get("actions"));

        Action defaultAction = null;
        if (node.has("defaultAction") && !node.get("defaultAction").isNull()) {
            defaultAction = buildTemplateAction(node.get("defaultAction"));
        }

        return new ButtonsTemplate(
                thumbnailImageUrl,
                imageAspectRatio,
                imageSize,
                imageBackgroundColor,
                title,
                text,
                defaultAction,
                actions
        );
    }

    private ConfirmTemplate buildConfirmTemplate(JsonNode node) {
        String text = node.has("text") ? node.get("text").asText() : "";
        List<Action> actions = buildTemplateActions(node.get("actions"));

        if (actions.size() != 2) {
            throw new ServiceException("Confirm Template 必須有剛好 2 個 action");
        }

        return new ConfirmTemplate(text, actions);
    }

    private CarouselTemplate buildCarouselTemplate(JsonNode node) {
        String imageAspectRatio = node.has("imageAspectRatio") ? node.get("imageAspectRatio").asText() : "rectangle";
        String imageSize = node.has("imageSize") ? node.get("imageSize").asText() : "cover";

        List<CarouselColumn> columns = new ArrayList<>();
        if (node.has("columns") && node.get("columns").isArray()) {
            for (JsonNode columnNode : node.get("columns")) {
                columns.add(buildCarouselColumn(columnNode));
            }
        }

        return new CarouselTemplate(columns, imageAspectRatio, imageSize);
    }

    private CarouselColumn buildCarouselColumn(JsonNode node) {
        String text = node.has("text") ? node.get("text").asText() : "";
        String title = node.has("title") && !node.get("title").isNull() ? node.get("title").asText() : null;

        URI thumbnailImageUrl = null;
        if (node.has("thumbnailImageUrl") && !node.get("thumbnailImageUrl").isNull()
                && StringUtils.isNotEmpty(node.get("thumbnailImageUrl").asText())) {
            thumbnailImageUrl = URI.create(node.get("thumbnailImageUrl").asText());
        }

        String imageBackgroundColor = node.has("imageBackgroundColor") && !node.get("imageBackgroundColor").isNull()
                ? node.get("imageBackgroundColor").asText() : null;
        List<Action> actions = buildTemplateActions(node.get("actions"));

        Action defaultAction = null;
        if (node.has("defaultAction") && !node.get("defaultAction").isNull()) {
            defaultAction = buildTemplateAction(node.get("defaultAction"));
        }

        return new CarouselColumn(thumbnailImageUrl, imageBackgroundColor, title, text, defaultAction, actions);
    }

    private ImageCarouselTemplate buildImageCarouselTemplate(JsonNode node) {
        List<ImageCarouselColumn> columns = new ArrayList<>();
        if (node.has("columns") && node.get("columns").isArray()) {
            for (JsonNode columnNode : node.get("columns")) {
                URI imageUrl = URI.create(columnNode.get("imageUrl").asText());
                Action action = buildTemplateAction(columnNode.get("action"));
                columns.add(new ImageCarouselColumn(imageUrl, action));
            }
        }
        return new ImageCarouselTemplate(columns);
    }

    private List<Action> buildTemplateActions(JsonNode actionsNode) {
        List<Action> actions = new ArrayList<>();
        if (actionsNode != null && actionsNode.isArray()) {
            for (JsonNode actionNode : actionsNode) {
                Action action = buildTemplateAction(actionNode);
                if (action != null) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    private Action buildTemplateAction(JsonNode node) {
        if (node == null || node.isNull()) return null;

        String type = node.has("type") ? node.get("type").asText() : "";
        String label = node.has("label") ? node.get("label").asText() : "";

        return switch (type) {
            case "message" -> new MessageAction(label, node.has("text") ? node.get("text").asText() : "");
            case "uri" -> new URIAction(label, URI.create(node.get("uri").asText()), null);
            case "postback" -> new PostbackAction(
                    label,
                    node.has("data") ? node.get("data").asText() : "",
                    node.has("displayText") && !node.get("displayText").isNull() ? node.get("displayText").asText() : null,
                    null, null, null
            );
            case "datetimepicker" -> {
                String modeStr = node.has("mode") ? node.get("mode").asText() : "datetime";
                DatetimePickerAction.Mode mode = switch (modeStr.toLowerCase()) {
                    case "date" -> DatetimePickerAction.Mode.DATE;
                    case "time" -> DatetimePickerAction.Mode.TIME;
                    default -> DatetimePickerAction.Mode.DATETIME;
                };
                yield new DatetimePickerAction.Builder()
                        .label(label)
                        .data(node.has("data") ? node.get("data").asText() : "")
                        .mode(mode)
                        .build();
            }
            default -> null;
        };
    }

    /**
     * 從 DTO 建立 Flex Message
     */
    private FlexMessage buildFlexMessageFromDto(Object dto) {
        String flexJson = null;
        String altText = "Flex Message";

        if (dto instanceof FlexMessageDTO) {
            flexJson = ((FlexMessageDTO) dto).getFlexContent();
            altText = ((FlexMessageDTO) dto).getAltText();
        } else if (dto instanceof PushMessageDTO) {
            flexJson = ((PushMessageDTO) dto).getFlexMessageJson();
            altText = ((PushMessageDTO) dto).getAltText();
        } else if (dto instanceof MulticastMessageDTO) {
            flexJson = ((MulticastMessageDTO) dto).getFlexMessageJson();
            altText = ((MulticastMessageDTO) dto).getAltText();
        } else if (dto instanceof BroadcastMessageDTO) {
            flexJson = ((BroadcastMessageDTO) dto).getFlexMessageJson();
            altText = ((BroadcastMessageDTO) dto).getAltText();
        } else if (dto instanceof ReplyMessageDTO) {
            flexJson = ((ReplyMessageDTO) dto).getFlexMessageJson();
            altText = ((ReplyMessageDTO) dto).getAltText();
        }
        
        if (StringUtils.isEmpty(flexJson)) {
            throw new ServiceException("Flex Message 內容不能為空");
        }
        if (StringUtils.isEmpty(altText)) {
            altText = "收到一則 Flex 訊息";
        }

        return buildFlexMessage(altText, flexJson);
    }

    /**
     * 建立 Flex Message
     */
    private FlexMessage buildFlexMessage(String altText, String json) {
        try {
            // 先解析 JSON 取得 type 欄位
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            // 取得 type 欄位來決定使用哪個具體類別
            var jsonNode = objectMapper.readTree(json);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : null;
            
            if (type == null) {
                throw new ServiceException("Flex Message JSON 缺少 type 欄位");
            }
            
            // 根據 type 使用對應的具體類別反序列化
            FlexContainer container;
            if ("bubble".equals(type)) {
                container = objectMapper.readValue(json, FlexBubble.class);
            } else if ("carousel".equals(type)) {
                container = objectMapper.readValue(json, FlexCarousel.class);
            } else {
                throw new ServiceException("不支援的 Flex Container 類型：" + type);
            }
            
            return new FlexMessage(null, null, altText, container);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Flex Message JSON 解析失敗", e);
            throw new ServiceException("Flex Message JSON 解析失敗: " + e.getMessage());
        }
    }

    private FlexMessage buildFlexMessage(FlexMessageDTO dto) {
        return buildFlexMessage(dto.getAltText(), dto.getFlexContent());
    }
    private TextMessage buildTextMessage(Object dto) {
        String text = null;
        List<PushMessageDTO.EmojiDTO> emojis = null;
        PushMessageDTO.QuickReplyDTO quickReply = null;

        if (dto instanceof PushMessageDTO pushDto) {
            text = pushDto.getTextMessage();
            emojis = pushDto.getEmojis();
            quickReply = pushDto.getQuickReply();
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

        // 如果有 emojis 或 quickReply，使用 Builder 建立訊息
        if ((emojis != null && !emojis.isEmpty()) || (quickReply != null && quickReply.getItems() != null && !quickReply.getItems().isEmpty())) {
            TextMessage.Builder builder = new TextMessage.Builder(text);
            
            // 設定 Emoji
            if (emojis != null && !emojis.isEmpty()) {
                List<Emoji> lineEmojis = emojis.stream()
                        .map(e -> new Emoji(e.getIndex(), e.getProductId(), e.getEmojiId()))
                        .toList();
                builder.emojis(lineEmojis);
            }
            
            // 設定 QuickReply
            if (quickReply != null && quickReply.getItems() != null && !quickReply.getItems().isEmpty()) {
                List<QuickReplyItem> quickReplyItems = quickReply.getItems().stream()
                        .map(this::buildQuickReplyItem)
                        .filter(Objects::nonNull)
                        .toList();
                if (!quickReplyItems.isEmpty()) {
                    builder.quickReply(new QuickReply(quickReplyItems));
                }
            }
            
            return builder.build();
        }

        return new TextMessage(text);
    }
    
    /**
     * 建立 QuickReply 項目
     */
    private QuickReplyItem buildQuickReplyItem(PushMessageDTO.QuickReplyItemDTO itemDto) {
        if (itemDto.getAction() == null) {
            return null;
        }
        
        Action action = buildQuickReplyAction(itemDto.getAction());
        if (action == null) {
            return null;
        }
        
        URI imageUrl = null;
        if (StringUtils.isNotEmpty(itemDto.getImageUrl())) {
            imageUrl = URI.create(itemDto.getImageUrl());
        }
        
        return new QuickReplyItem(imageUrl, action);
    }
    
    /**
     * 建立 QuickReply Action
     */
    private Action buildQuickReplyAction(PushMessageDTO.QuickReplyActionDTO actionDto) {
        if (actionDto == null || StringUtils.isEmpty(actionDto.getType())) {
            return null;
        }
        
        return switch (actionDto.getType().toLowerCase()) {
            case "message" -> new MessageAction(actionDto.getLabel(), actionDto.getText());
            case "uri" -> new URIAction(actionDto.getLabel(), URI.create(actionDto.getUri()), null);
            case "postback" -> new PostbackAction(actionDto.getLabel(), actionDto.getData(), actionDto.getDisplayText(), null, null, null);
            case "datetimepicker" -> {
                DatetimePickerAction.Mode mode = switch (StringUtils.isEmpty(actionDto.getMode()) ? "datetime" : actionDto.getMode().toLowerCase()) {
                    case "date" -> DatetimePickerAction.Mode.DATE;
                    case "time" -> DatetimePickerAction.Mode.TIME;
                    default -> DatetimePickerAction.Mode.DATETIME;
                };
                yield new DatetimePickerAction.Builder()
                        .label(actionDto.getLabel())
                        .data(actionDto.getData())
                        .mode(mode)
                        .build();
            }
            case "camera" -> new CameraAction(actionDto.getLabel());
            case "cameraroll" -> new CameraRollAction(actionDto.getLabel());
            case "location" -> new LocationAction(actionDto.getLabel());
            case "clipboard" -> new ClipboardAction(actionDto.getLabel(), actionDto.getClipboardText());
            default -> {
                log.warn("未知的 Quick Reply 動作類型: {}", actionDto.getType());
                yield null;
            }
        };
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
     * 建立影片訊息
     */
    private VideoMessage buildVideoMessage(Object dto) {
        String videoUrl = null;
        String previewImageUrl = null;

        if (dto instanceof PushMessageDTO pushDTO) {
            videoUrl = pushDTO.getVideoUrl();
            previewImageUrl = pushDTO.getVideoPreviewImageUrl();
        }

        if (StringUtils.isEmpty(videoUrl)) {
            throw new ServiceException("影片 URL 不能為空");
        }
        if (StringUtils.isEmpty(previewImageUrl)) {
            throw new ServiceException("影片預覽圖 URL 不能為空");
        }

        return new VideoMessage(URI.create(videoUrl), URI.create(previewImageUrl), null);
    }

    /**
     * 建立音訊訊息
     */
    private AudioMessage buildAudioMessage(Object dto) {
        String audioUrl = null;
        Long duration = null;

        if (dto instanceof PushMessageDTO pushDTO) {
            audioUrl = pushDTO.getAudioUrl();
            duration = pushDTO.getAudioDuration();
        }

        if (StringUtils.isEmpty(audioUrl)) {
            throw new ServiceException("音訊 URL 不能為空");
        }
        if (duration == null || duration <= 0) {
            throw new ServiceException("音訊長度必須大於 0");
        }

        return new AudioMessage(URI.create(audioUrl), duration);
    }

    /**
     * 建立貼圖訊息
     */
    private StickerMessage buildStickerMessage(Object dto) {
        String packageId = null;
        String stickerId = null;

        if (dto instanceof PushMessageDTO pushDTO) {
            packageId = pushDTO.getStickerPackageId();
            stickerId = pushDTO.getStickerId();
        }

        if (StringUtils.isEmpty(packageId)) {
            throw new ServiceException("貼圖 Package ID 不能為空");
        }
        if (StringUtils.isEmpty(stickerId)) {
            throw new ServiceException("貼圖 ID 不能為空");
        }

        return new StickerMessage(packageId, stickerId);
    }

    /**
     * 建立位置訊息
     */
    private LocationMessage buildLocationMessage(Object dto) {
        String title = null;
        String address = null;
        Double latitude = null;
        Double longitude = null;

        if (dto instanceof PushMessageDTO pushDTO) {
            title = pushDTO.getLocationTitle();
            address = pushDTO.getLocationAddress();
            latitude = pushDTO.getLatitude();
            longitude = pushDTO.getLongitude();
        }

        if (StringUtils.isEmpty(title)) {
            throw new ServiceException("位置標題不能為空");
        }
        if (StringUtils.isEmpty(address)) {
            throw new ServiceException("位置地址不能為空");
        }
        if (latitude == null) {
            throw new ServiceException("緯度不能為空");
        }
        if (longitude == null) {
            throw new ServiceException("經度不能為空");
        }

        return new LocationMessage(title, address, latitude, longitude);
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
