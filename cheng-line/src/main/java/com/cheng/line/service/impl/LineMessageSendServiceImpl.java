package com.cheng.line.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.client.LineClientFactory;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.domain.LineMessageTemplate;
import com.cheng.line.dto.SendMessageDTO;
import com.cheng.line.enums.ContentType;
import com.cheng.line.enums.MessageType;
import com.cheng.line.enums.QuickReplyActionType;
import com.cheng.line.enums.SendStatus;
import com.cheng.line.enums.TargetType;
import com.cheng.line.mapper.LineMessageLogMapper;
import com.cheng.line.mapper.LineUserMapper;
import com.cheng.line.service.ILineConfigService;
import com.cheng.line.service.ILineMessageSendService;
import com.cheng.line.service.ILineMessageTemplateService;
import com.cheng.line.service.ILineUserService;
import com.cheng.line.util.FlexMessageParser;
import com.cheng.line.util.TemplateVariableEngine;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * LINE 訊息發送服務實作（擴充版）
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineMessageSendServiceImpl implements ILineMessageSendService {

    private @Resource LineMessageLogMapper lineMessageLogMapper;
    private @Resource LineUserMapper lineUserMapper;
    private @Resource ILineConfigService lineConfigService;
    private @Resource ILineUserService lineUserService;
    private @Resource ILineMessageTemplateService templateService;
    private @Resource LineClientFactory lineClientFactory;
    private @Resource FlexMessageParser flexMessageParser;
    private @Resource TemplateVariableEngine variableEngine;

    @Override
    @Transactional
    public Long sendMessage(SendMessageDTO dto) {
        ContentType contentType = ContentType.fromCode(dto.getContentType());
        return switch (contentType) {
            case TEXT -> sendTextMessage(dto);
            case IMAGE -> sendImageMessage(dto);
            case VIDEO -> sendVideoMessage(dto);
            case AUDIO -> sendAudioMessage(dto);
            case STICKER -> sendStickerMessage(dto);
            case FLEX -> sendFlexMessage(dto);
            default -> throw new ServiceException("不支援的訊息類型：" + dto.getContentType());
        };
    }

    @Override
    @Transactional
    public Long sendTextMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getText())) {
            throw new ServiceException("文字訊息內容不能為空");
        }

        // 變數替換
        String text = processVariables(dto.getText(), dto);

        // 建立訊息 Builder
        TextMessage.Builder builder = new TextMessage.Builder(text);
        
        // 設定 Emoji
        if (dto.getEmojis() != null && !dto.getEmojis().isEmpty()) {
            List<Emoji> emojis = dto.getEmojis().stream()
                    .map(e -> new Emoji(e.getIndex(), e.getProductId(), e.getEmojiId()))
                    .toList();
            builder.emojis(emojis);
        }
        
        // 設定 QuickReply
        log.info("[sendTextMessage] 檢查 quickReply: dto.getQuickReply()={}, items={}", 
            dto.getQuickReply() != null,
            dto.getQuickReply() != null && dto.getQuickReply().getItems() != null ? dto.getQuickReply().getItems().size() : 0);
        if (dto.getQuickReply() != null && dto.getQuickReply().getItems() != null && !dto.getQuickReply().getItems().isEmpty()) {
            List<QuickReplyItem> quickReplyItems = dto.getQuickReply().getItems().stream()
                    .map(this::buildQuickReplyItem)
                    .filter(item -> item != null)
                    .toList();
            log.info("[sendTextMessage] 建立 quickReplyItems 完成, count: {}", quickReplyItems.size());
            if (!quickReplyItems.isEmpty()) {
                builder.quickReply(new QuickReply(quickReplyItems));
                log.info("[sendTextMessage] QuickReply 已設定到 builder");
            }
        }

        return doSend(dto, builder.build(), ContentType.TEXT);
    }
    
    /**
     * 建立 QuickReply 項目
     */
    private QuickReplyItem buildQuickReplyItem(SendMessageDTO.QuickReplyItemDTO itemDto) {
        if (itemDto.getAction() == null) {
            return null;
        }
        
        SendMessageDTO.QuickReplyActionDTO actionDto = itemDto.getAction();
        Action action = buildQuickReplyAction(actionDto);
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
    private Action buildQuickReplyAction(SendMessageDTO.QuickReplyActionDTO actionDto) {
        if (actionDto == null || StringUtils.isEmpty(actionDto.getType())) {
            return null;
        }
        
        QuickReplyActionType actionType = QuickReplyActionType.fromCode(actionDto.getType());
        if (actionType == null) {
            log.warn("未知的 Quick Reply 動作類型: {}", actionDto.getType());
            return null;
        }
        
        return switch (actionType) {
            case MESSAGE -> new MessageAction(actionDto.getLabel(), actionDto.getText());
            case URI -> new URIAction(actionDto.getLabel(), URI.create(actionDto.getUri()), null);
            case POSTBACK -> new PostbackAction(actionDto.getLabel(), actionDto.getData(), actionDto.getDisplayText(), null, null, null);
            case DATETIMEPICKER -> {
                DatetimePickerAction.Mode mode = parseDatetimePickerMode(actionDto.getMode());
                yield new DatetimePickerAction.Builder()
                        .label(actionDto.getLabel())
                        .data(actionDto.getData())
                        .mode(mode)
                        .build();
            }
            case CAMERA -> new CameraAction(actionDto.getLabel());
            case CAMERA_ROLL -> new CameraRollAction(actionDto.getLabel());
            case LOCATION -> new LocationAction(actionDto.getLabel());
            case CLIPBOARD -> new ClipboardAction(actionDto.getLabel(), actionDto.getClipboardText());
        };
    }
    
    /**
     * 解析 DatetimePicker 的 Mode
     */
    private DatetimePickerAction.Mode parseDatetimePickerMode(String mode) {
        if (StringUtils.isEmpty(mode)) {
            return DatetimePickerAction.Mode.DATETIME;
        }
        return switch (mode.toLowerCase()) {
            case "date" -> DatetimePickerAction.Mode.DATE;
            case "time" -> DatetimePickerAction.Mode.TIME;
            default -> DatetimePickerAction.Mode.DATETIME;
        };
    }

    @Override
    @Transactional
    public Long sendImageMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getImageUrl())) {
            throw new ServiceException("圖片 URL 不能為空");
        }

        validateHttpsUrl(dto.getImageUrl(), "圖片 URL");

        String previewUrl = StringUtils.isNotEmpty(dto.getPreviewImageUrl())
                ? dto.getPreviewImageUrl() : dto.getImageUrl();
        validateHttpsUrl(previewUrl, "預覽圖 URL");

        ImageMessage message = new ImageMessage.Builder(URI.create(dto.getImageUrl()), URI.create(previewUrl))
                .build();
        return doSend(dto, message, ContentType.IMAGE);
    }

    @Override
    @Transactional
    public Long sendVideoMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getVideoUrl())) {
            throw new ServiceException("影片 URL 不能為空");
        }
        if (StringUtils.isEmpty(dto.getVideoPreviewUrl())) {
            throw new ServiceException("影片預覽圖 URL 不能為空");
        }

        validateHttpsUrl(dto.getVideoUrl(), "影片 URL");
        validateHttpsUrl(dto.getVideoPreviewUrl(), "影片預覽圖 URL");

        VideoMessage message = new VideoMessage.Builder(URI.create(dto.getVideoUrl()), URI.create(dto.getVideoPreviewUrl()))
                .trackingId(dto.getTrackingId())
                .build();

        return doSend(dto, message, ContentType.VIDEO);
    }

    @Override
    @Transactional
    public Long sendAudioMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getAudioUrl())) {
            throw new ServiceException("音訊 URL 不能為空");
        }
        if (dto.getAudioDuration() == null || dto.getAudioDuration() <= 0) {
            throw new ServiceException("音訊長度必須大於 0");
        }

        validateHttpsUrl(dto.getAudioUrl(), "音訊 URL");

        AudioMessage message = new AudioMessage.Builder(URI.create(dto.getAudioUrl()), dto.getAudioDuration())
                .build();
        return doSend(dto, message, ContentType.AUDIO);
    }

    @Override
    @Transactional
    public Long sendLocationMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getLocationTitle())) {
            throw new ServiceException("地點標題不能為空");
        }
        if (StringUtils.isEmpty(dto.getLocationAddress())) {
            throw new ServiceException("地點地址不能為空");
        }
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new ServiceException("經緯度不能為空");
        }

        LocationMessage message = new LocationMessage.Builder(
                dto.getLocationTitle(),
                dto.getLocationAddress(),
                dto.getLatitude(),
                dto.getLongitude()
        ).build();

        return doSend(dto, message, ContentType.TEXT); // LINE SDK 沒有 LOCATION ContentType
    }

    @Override
    @Transactional
    public Long sendStickerMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getPackageId())) {
            throw new ServiceException("貼圖 Package ID 不能為空");
        }
        if (StringUtils.isEmpty(dto.getStickerId())) {
            throw new ServiceException("貼圖 ID 不能為空");
        }

        StickerMessage message = new StickerMessage.Builder(dto.getPackageId(), dto.getStickerId())
                .build();
        return doSend(dto, message, ContentType.STICKER);
    }

    @Override
    @Transactional
    public Long sendImagemapMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getImagemapBaseUrl())) {
            throw new ServiceException("Imagemap 基礎 URL 不能為空");
        }
        if (StringUtils.isEmpty(dto.getImagemapAltText())) {
            throw new ServiceException("Imagemap 替代文字不能為空");
        }
        if (dto.getImagemapWidth() == null || dto.getImagemapHeight() == null) {
            throw new ServiceException("Imagemap 尺寸不能為空");
        }

        validateHttpsUrl(dto.getImagemapBaseUrl(), "Imagemap 基礎 URL");

        // 建立 Imagemap actions
        List<ImagemapAction> actions = new ArrayList<>();
        if (dto.getImagemapActions() != null) {
            for (SendMessageDTO.ImagemapActionDTO actionDto : dto.getImagemapActions()) {
                ImagemapArea area = new ImagemapArea(
                        actionDto.getX(), actionDto.getY(),
                        actionDto.getWidth(), actionDto.getHeight()
                );

                if ("uri".equals(actionDto.getType())) {
                    actions.add(new URIImagemapAction(area, actionDto.getLinkUri(), actionDto.getLabel()));
                } else if ("message".equals(actionDto.getType())) {
                    actions.add(new MessageImagemapAction(area, actionDto.getText(), actionDto.getLabel()));
                }
            }
        }

        ImagemapBaseSize baseSize = new ImagemapBaseSize(dto.getImagemapHeight(), dto.getImagemapWidth());

        ImagemapMessage message = new ImagemapMessage(
                null,  // quickReply
                null,  // sender
                URI.create(dto.getImagemapBaseUrl()),
                dto.getImagemapAltText(),
                baseSize,
                actions,
                null   // video
        );

        return doSend(dto, message, ContentType.TEMPLATE); // 使用 TEMPLATE 作為 contentType
    }

    @Override
    @Transactional
    public Long sendFlexMessage(SendMessageDTO dto) {
        validateBasicParams(dto);
        if (StringUtils.isEmpty(dto.getFlexContent())) {
            throw new ServiceException("Flex Message 內容不能為空");
        }
        if (StringUtils.isEmpty(dto.getFlexAltText())) {
            throw new ServiceException("Flex Message 替代文字不能為空");
        }

        // 變數替換
        String content = processVariables(dto.getFlexContent(), dto);

        // 驗證 JSON
        FlexMessageParser.ValidationResult validation = flexMessageParser.validate(content);
        if (!validation.valid()) {
            throw new ServiceException("Flex Message JSON 驗證失敗：" + validation.errorMessage());
        }

        // 解析 FlexContainer
        FlexContainer container = parseFlexContainer(content);

        FlexMessage message = new FlexMessage.Builder(dto.getFlexAltText(), container)
                .build();
        return doSend(dto, message, ContentType.FLEX);
    }

    @Override
    @Transactional
    public Long sendTemplateMessage(SendMessageDTO dto) {
        if (dto.getTemplateId() == null) {
            throw new ServiceException("範本 ID 不能為空");
        }

        LineMessageTemplate template = templateService.selectLineMessageTemplateById(dto.getTemplateId());
        if (template == null) {
            throw new ServiceException("找不到範本，ID: " + dto.getTemplateId());
        }

        // 使用範本的內容和類型
        dto.setContentType(template.getMsgType());

        // 替換變數
        Map<String, String> variables = dto.getTemplateVariables();
        if (variables == null) {
            variables = new HashMap<>();
        }

        // 如果有目標使用者，取得使用者資訊作為變數
        if (StringUtils.isNotEmpty(dto.getTargetLineUserId())) {
            var lineUser = lineUserMapper.selectLineUserByLineUserId(dto.getTargetLineUserId());
            if (lineUser != null) {
                variables.putIfAbsent("nickname", lineUser.getLineDisplayName());
                variables.putIfAbsent("displayName", lineUser.getLineDisplayName());
            }
        }

        String content = variableEngine.parse(template.getContent(), variables);

        // 根據範本類型設定對應欄位
        ContentType msgType = ContentType.fromCode(template.getMsgType());
        if (msgType == null) {
            throw new ServiceException("不支援的範本類型：" + template.getMsgType());
        }

        switch (msgType) {
            case TEXT -> {
                // 檢查 content 是否為 JSON 格式（包含 emojis 或 quickReply）
                if (content.startsWith("{") && (content.contains("\"emojis\"") || content.contains("\"quickReply\""))) {
                    try {
                        var jsonNode = JacksonUtil.toJsonNode(content);
                        dto.setText(jsonNode.get("text").asText());
                        // 解析 emojis
                        if (jsonNode.has("emojis") && jsonNode.get("emojis").isArray()) {
                            List<SendMessageDTO.EmojiDTO> emojiList = new ArrayList<>();
                            for (var emojiNode : jsonNode.get("emojis")) {
                                SendMessageDTO.EmojiDTO emojiDTO = new SendMessageDTO.EmojiDTO();
                                emojiDTO.setIndex(emojiNode.get("index").asInt());
                                emojiDTO.setProductId(emojiNode.get("productId").asText());
                                emojiDTO.setEmojiId(emojiNode.get("emojiId").asText());
                                emojiList.add(emojiDTO);
                            }
                            dto.setEmojis(emojiList);
                        }
                        // 解析 quickReply
                        log.info("[sendTemplateMessage] 檢查 quickReply: has={}, hasItems={}", 
                            jsonNode.has("quickReply"), 
                            jsonNode.has("quickReply") && jsonNode.get("quickReply").has("items"));
                        if (jsonNode.has("quickReply") && jsonNode.get("quickReply").has("items")) {
                            log.info("[sendTemplateMessage] 開始解析 quickReply items");
                            SendMessageDTO.QuickReplyDTO quickReplyDTO = new SendMessageDTO.QuickReplyDTO();
                            List<SendMessageDTO.QuickReplyItemDTO> items = new ArrayList<>();
                            for (var itemNode : jsonNode.get("quickReply").get("items")) {
                                SendMessageDTO.QuickReplyItemDTO itemDTO = new SendMessageDTO.QuickReplyItemDTO();
                                itemDTO.setType(itemNode.has("type") ? itemNode.get("type").asText() : "action");
                                if (itemNode.has("imageUrl") && !itemNode.get("imageUrl").isNull()) {
                                    itemDTO.setImageUrl(itemNode.get("imageUrl").asText());
                                }
                                if (itemNode.has("action")) {
                                    var actionNode = itemNode.get("action");
                                    SendMessageDTO.QuickReplyActionDTO actionDTO = new SendMessageDTO.QuickReplyActionDTO();
                                    actionDTO.setType(actionNode.has("type") ? actionNode.get("type").asText() : "message");
                                    if (actionNode.has("label")) actionDTO.setLabel(actionNode.get("label").asText());
                                    if (actionNode.has("text")) actionDTO.setText(actionNode.get("text").asText());
                                    if (actionNode.has("uri")) actionDTO.setUri(actionNode.get("uri").asText());
                                    if (actionNode.has("data")) actionDTO.setData(actionNode.get("data").asText());
                                    if (actionNode.has("displayText")) actionDTO.setDisplayText(actionNode.get("displayText").asText());
                                    if (actionNode.has("mode")) actionDTO.setMode(actionNode.get("mode").asText());
                                    if (actionNode.has("clipboardText")) actionDTO.setClipboardText(actionNode.get("clipboardText").asText());
                                    itemDTO.setAction(actionDTO);
                                }
                                items.add(itemDTO);
                            }
                            quickReplyDTO.setItems(items);
                            dto.setQuickReply(quickReplyDTO);
                            log.info("[sendTemplateMessage] 設定 quickReply 完成, items count: {}", items.size());
                        }
                    } catch (Exception e) {
                        log.warn("解析 TEXT 訊息 JSON 失敗，使用純文字: {}", e.getMessage());
                        dto.setText(content);
                    }
                } else {
                    dto.setText(content);
                }
            }
            case IMAGE -> {
                dto.setImageUrl(content);
                dto.setPreviewImageUrl(template.getPreviewImg());
            }
            case FLEX -> {
                dto.setFlexContent(content);
                dto.setFlexAltText(template.getAltText());
            }
            default -> throw new ServiceException("不支援的範本類型：" + msgType.getDescription());
        }

        // 增加範本使用次數
        templateService.incrementUseCount(dto.getTemplateId());

        return sendMessage(dto);
    }

    /**
     * 執行發送
     */
    private Long doSend(SendMessageDTO dto, Message message, ContentType contentType) {
        LineConfig config = getLineConfig(dto.getConfigId());
        MessageType messageType = MessageType.fromCode(dto.getMessageType());
        TargetType targetType = determineTargetType(dto);

        // 建立訊息記錄
        LineMessageLog messageLog = createMessageLog(dto, config, messageType, contentType, targetType, message);
        lineMessageLogMapper.insertLineMessageLog(messageLog);

        try {
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());
            messageLog.setSendStatus(SendStatus.SENDING);
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            switch (messageType) {
                case PUSH -> sendPush(client, dto.getTargetLineUserId(), message, dto.getNotificationDisabled());
                case MULTICAST -> sendMulticast(client, dto.getTargetLineUserIds(), message, dto.getNotificationDisabled());
                case BROADCAST -> sendBroadcast(client, message, dto.getNotificationDisabled());
                case REPLY -> sendReply(client, dto.getReplyToken(), message, dto.getNotificationDisabled());
                default -> throw new ServiceException("不支援的訊息類型");
            }

            // 更新成功狀態
            messageLog.setSendStatus(SendStatus.SUCCESS);
            messageLog.setSuccessCount(messageLog.getTargetCount());
            messageLog.setFailCount(0);
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);

            // 更新使用者訊息統計
            updateUserMessageCount(dto, targetType);

            return messageLog.getMessageId();

        } catch (Exception e) {
            log.error("發送訊息失敗", e);
            messageLog.setSendStatus(SendStatus.FAILED);
            messageLog.setSuccessCount(0);
            messageLog.setFailCount(messageLog.getTargetCount());
            messageLog.setErrorMessage(e.getMessage());
            messageLog.setSendTime(new Date());
            lineMessageLogMapper.updateLineMessageLog(messageLog);
            throw new ServiceException("發送訊息失敗：" + e.getMessage());
        }
    }

    private void sendPush(MessagingApiClient client, String userId, Message message, Boolean notificationDisabled)
            throws ExecutionException, InterruptedException {
        PushMessageRequest request = new PushMessageRequest(
                userId,
                Collections.singletonList(message),
                notificationDisabled != null && notificationDisabled,
                null
        );
        client.pushMessage(UUID.randomUUID(), request).get();
    }

    private void sendMulticast(MessagingApiClient client, List<String> userIds, Message message, Boolean notificationDisabled)
            throws ExecutionException, InterruptedException {
        MulticastRequest request = new MulticastRequest(
                Collections.singletonList(message),
                new ArrayList<>(userIds),
                notificationDisabled != null && notificationDisabled,
                null
        );
        client.multicast(UUID.randomUUID(), request).get();
    }

    private void sendBroadcast(MessagingApiClient client, Message message, Boolean notificationDisabled)
            throws ExecutionException, InterruptedException {
        BroadcastRequest request = new BroadcastRequest(
                Collections.singletonList(message),
                notificationDisabled != null && notificationDisabled
        );
        client.broadcast(UUID.randomUUID(), request).get();
    }

    private void sendReply(MessagingApiClient client, String replyToken, Message message, Boolean notificationDisabled)
            throws ExecutionException, InterruptedException {
        ReplyMessageRequest request = new ReplyMessageRequest(
                replyToken,
                Collections.singletonList(message),
                notificationDisabled != null && notificationDisabled
        );
        client.replyMessage(request).get();
    }

    /**
     * 解析 FlexContainer
     */
    private FlexContainer parseFlexContainer(String jsonContent) {
        try {
            // 使用 JacksonUtil 解析為 Map，再轉換為 FlexContainer
            Map<String, Object> jsonMap = JacksonUtil.fromJson(jsonContent, new TypeReference<Map<String, Object>>() {});
            if (jsonMap == null) {
                throw new ServiceException("無法解析 Flex Message JSON");
            }

            String type = (String) jsonMap.get("type");
            if ("bubble".equals(type)) {
                return JacksonUtil.fromJson(jsonContent, new TypeReference<FlexBubble>() {});
            } else if ("carousel".equals(type)) {
                return JacksonUtil.fromJson(jsonContent, new TypeReference<FlexCarousel>() {});
            } else {
                throw new ServiceException("不支援的 Flex Container 類型：" + type);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 FlexContainer 失敗", e);
            throw new ServiceException("解析 Flex Message 失敗：" + e.getMessage());
        }
    }

    /**
     * 建立訊息記錄
     */
    private LineMessageLog createMessageLog(SendMessageDTO dto, LineConfig config,
                                            MessageType messageType, ContentType contentType,
                                            TargetType targetType, Message message) {
        LineMessageLog log = new LineMessageLog();
        log.setConfigId(config.getConfigId());
        log.setMessageType(messageType);
        log.setContentType(contentType);
        log.setTargetType(targetType);
        log.setMessageContent(JacksonUtil.encodeToJson(message));
        log.setSendStatus(SendStatus.PENDING);

        switch (targetType) {
            case SINGLE -> {
                log.setTargetLineUserId(dto.getTargetLineUserId());
                log.setTargetCount(1);
            }
            case MULTIPLE -> {
                log.setTargetUserIds(JacksonUtil.encodeToJson(dto.getTargetLineUserIds()));
                log.setTargetCount(dto.getTargetLineUserIds().size());
            }
            case ALL -> log.setTargetCount(lineUserMapper.countFollowingUsers());
        }

        return log;
    }

    /**
     * 判斷目標類型
     */
    private TargetType determineTargetType(SendMessageDTO dto) {
        MessageType messageType = MessageType.fromCode(dto.getMessageType());
        return switch (messageType) {
            case PUSH, REPLY -> TargetType.SINGLE;
            case MULTICAST -> TargetType.MULTIPLE;
            case BROADCAST -> TargetType.ALL;
        };
    }

    /**
     * 更新使用者訊息統計
     */
    private void updateUserMessageCount(SendMessageDTO dto, TargetType targetType) {
        switch (targetType) {
            case SINGLE -> {
                if (StringUtils.isNotEmpty(dto.getTargetLineUserId())) {
                    lineUserService.incrementMessageCount(dto.getTargetLineUserId(), true);
                }
            }
            case MULTIPLE -> {
                if (dto.getTargetLineUserIds() != null) {
                    dto.getTargetLineUserIds().forEach(userId ->
                            lineUserService.incrementMessageCount(userId, true));
                }
            }
            // ALL 不更新個別使用者統計
        }
    }

    /**
     * 處理變數替換
     */
    private String processVariables(String content, SendMessageDTO dto) {
        if (!variableEngine.hasVariables(content)) {
            return content;
        }

        Map<String, String> variables = dto.getTemplateVariables();
        if (variables == null) {
            variables = new HashMap<>();
        }

        // 如果是單人推播，取得使用者資訊
        if (StringUtils.isNotEmpty(dto.getTargetLineUserId())) {
            var lineUser = lineUserMapper.selectLineUserByLineUserId(dto.getTargetLineUserId());
            if (lineUser != null) {
                variables.putIfAbsent("nickname", lineUser.getLineDisplayName());
                variables.putIfAbsent("displayName", lineUser.getLineDisplayName());
                variables.putIfAbsent("lineUserId", lineUser.getLineUserId());
            }
        }

        return variableEngine.parse(content, variables);
    }

    /**
     * 取得頻道設定
     */
    private LineConfig getLineConfig(Integer configId) {
        LineConfig config = configId != null
                ? lineConfigService.selectLineConfigById(configId)
                : lineConfigService.selectDefaultLineConfig();

        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }
        return config;
    }

    /**
     * 驗證基本參數
     */
    private void validateBasicParams(SendMessageDTO dto) {
        if (dto.getConfigId() == null) {
            throw new ServiceException("頻道設定 ID 不能為空");
        }
        if (StringUtils.isEmpty(dto.getMessageType())) {
            throw new ServiceException("訊息類型不能為空");
        }

        MessageType messageType = MessageType.fromCode(dto.getMessageType());
        switch (messageType) {
            case PUSH -> {
                if (StringUtils.isEmpty(dto.getTargetLineUserId())) {
                    throw new ServiceException("單人推播時目標使用者 ID 不能為空");
                }
            }
            case MULTICAST -> {
                if (dto.getTargetLineUserIds() == null || dto.getTargetLineUserIds().isEmpty()) {
                    throw new ServiceException("多人推播時目標使用者列表不能為空");
                }
            }
            case REPLY -> {
                if (StringUtils.isEmpty(dto.getReplyToken())) {
                    throw new ServiceException("回覆訊息時 Reply Token 不能為空");
                }
            }
        }
    }

    /**
     * 驗證 HTTPS URL
     */
    private void validateHttpsUrl(String url, String fieldName) {
        if (!url.startsWith("https://")) {
            throw new ServiceException(fieldName + " 必須使用 HTTPS 協定");
        }
    }
}
