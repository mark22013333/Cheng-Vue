package com.cheng.line.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.LineMessageTemplate;
import com.cheng.line.dto.ImagemapMessageDto;
import com.cheng.line.dto.PushMessageDTO;
import com.cheng.line.enums.ContentType;
import com.cheng.line.service.ILineMessageService;
import com.cheng.line.service.ILineMessageTemplateService;
import com.cheng.line.service.ILineTemplateImagemapRefService;
import com.cheng.line.util.FlexMessageParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.line.enums.ImagemapWidth;
import com.cheng.line.enums.LineApiLimit;
import com.cheng.line.util.ImagemapUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LINE 訊息範本 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/template")
public class LineMessageTemplateController extends BaseController {

    private @Resource ILineMessageTemplateService lineMessageTemplateService;
    private @Resource ILineMessageService lineMessageService;
    private @Resource FlexMessageParser flexMessageParser;
    private @Resource ILineTemplateImagemapRefService imagemapRefService;

    /**
     * 上傳 Imagemap 圖片
     * 自動產生 240, 300, 460, 700, 1040 五種尺寸
     */
    @PreAuthorize("@ss.hasPermi('line:template:add')")
    @Log(title = "Imagemap圖片上傳", businessType = BusinessType.UPDATE)
    @PostMapping("/upload/imagemap")
    public AjaxResult uploadImagemap(@RequestParam("file") MultipartFile file) {
        try {
            // 驗證檔案
            if (file == null || file.isEmpty()) {
                return error("請選擇要上傳的圖片");
            }

            // 產生隨機目錄名稱
            String uuid = IdUtils.fastSimpleUUID();
            Path outputDir = Paths.get(CoolAppsConfig.getImagemapPath(), uuid);

            // 處理圖片並取得結果
            ImagemapUtils.ImageGenerationResult result;
            try (InputStream is = file.getInputStream()) {
                result = ImagemapUtils.generateImages(is, outputDir);
            }

            // 構建資源路徑（相對路徑，前端會自動處理環境差異）
            String resourcePath = "/profile/imagemap/" + uuid;
            String previewUrl = resourcePath + "/" + ImagemapWidth.WIDTH_700.getCode();

            // 判斷是否有調整尺寸（原始尺寸與基底尺寸不同）
            boolean resized = result.getOriginalWidth() != result.getBaseWidth() ||
                              result.getOriginalHeight() != result.getBaseHeight();

            // 返回詳細資訊（與 Rich Menu 上傳保持一致的格式）
            Map<String, Object> data = new HashMap<>();
            data.put("baseUrl", resourcePath);
            data.put("previewUrl", previewUrl);
            data.put("uuid", uuid);
            data.put("originalWidth", result.getOriginalWidth());
            data.put("originalHeight", result.getOriginalHeight());
            data.put("originalSize", result.getOriginalSize());
            data.put("baseWidth", result.getBaseWidth());
            data.put("baseHeight", result.getBaseHeight());
            data.put("baseSize", result.getBaseSize());
            data.put("generatedCount", result.getGeneratedCount());
            data.put("originalFilename", file.getOriginalFilename());
            data.put("resized", resized);

            return success(data);
        } catch (Exception e) {
            return error("圖片處理失敗：" + e.getMessage());
        }
    }

    /**
     * 刪除 Imagemap 圖片
     */
    @PreAuthorize("@ss.hasPermi('line:template:remove')")
    @Log(title = "Imagemap圖片刪除", businessType = BusinessType.DELETE)
    @DeleteMapping("/imagemap/{uuid}")
    public AjaxResult deleteImagemap(@PathVariable String uuid) {
        // Attempts auditable image deletion; returns success or failure
        try {
            if (uuid == null || uuid.isEmpty()) {
                return error("UUID 不能為空");
            }

            Path directory = Paths.get(CoolAppsConfig.getImagemapPath(), uuid);
            if (Files.notExists(directory)) {
                return error("圖片目錄不存在");
            }

            ImagemapUtils.deleteImageDirectory(directory);
            return success("刪除成功");
        } catch (Exception e) {
            return error("刪除失敗：" + e.getMessage());
        }
    }

    /**
     * 查詢範本列表
     */
    @PreAuthorize("@ss.hasPermi('line:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(LineMessageTemplate lineMessageTemplate) {
        startPage();
        List<LineMessageTemplate> list = lineMessageTemplateService.selectLineMessageTemplateList(lineMessageTemplate);
        return getDataTable(list);
    }

    /**
     * 匯出範本列表
     */
    @PreAuthorize("@ss.hasPermi('line:template:export')")
    @Log(title = "訊息範本", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LineMessageTemplate lineMessageTemplate) {
        List<LineMessageTemplate> list = lineMessageTemplateService.selectLineMessageTemplateList(lineMessageTemplate);
        ExcelUtil<LineMessageTemplate> util = new ExcelUtil<>(LineMessageTemplate.class);
        util.exportExcel(response, list, "訊息範本資料");
    }

    /**
     * 取得範本詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @GetMapping("/{templateId}")
    public AjaxResult getInfo(@PathVariable Long templateId) {
        return success(lineMessageTemplateService.selectLineMessageTemplateById(templateId));
    }

    /**
     * 根據範本代碼取得範本
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @GetMapping("/code/{templateCode}")
    public AjaxResult getByCode(@PathVariable String templateCode) {
        return success(lineMessageTemplateService.selectLineMessageTemplateByCode(templateCode));
    }

    /**
     * 根據訊息類型取得範本列表
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @GetMapping("/type/{msgType}")
    public AjaxResult getByType(@PathVariable String msgType) {
        return success(lineMessageTemplateService.selectLineMessageTemplateByType(msgType));
    }

    /**
     * 新增範本
     */
    @PreAuthorize("@ss.hasPermi('line:template:add')")
    @Log(title = "訊息範本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Map<String, Object> params) {
        LineMessageTemplate lineMessageTemplate = buildTemplateFromParams(params);
        
        // 檢查範本代碼唯一性
        if (lineMessageTemplate.getTemplateCode() != null && !lineMessageTemplateService.checkTemplateCodeUnique(lineMessageTemplate)) {
            return error("新增範本'" + lineMessageTemplate.getTemplateName() + "'失敗，範本代碼已存在");
        }

        // 驗證訊息內容
        String validationError = validateMessageContent(lineMessageTemplate);
        if (validationError != null) {
            return error(validationError);
        }

        lineMessageTemplate.setCreateBy(getUsername());
        lineMessageTemplateService.insertLineMessageTemplate(lineMessageTemplate);
        
        // 建立圖文範本引用關聯
        maintainImagemapRefs(lineMessageTemplate.getTemplateId(), params);
        
        return success(lineMessageTemplate.getTemplateId());
    }

    /**
     * 修改範本
     */
    @PreAuthorize("@ss.hasPermi('line:template:edit')")
    @Log(title = "訊息範本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Map<String, Object> params) {
        // DEBUG: 追蹤接收到的 content
        log.info("[edit] 接收到的 content: {}", params.get("content"));
        log.info("[edit] content 是否包含 quickReply: {}", 
            params.get("content") != null && params.get("content").toString().contains("quickReply"));
        
        LineMessageTemplate lineMessageTemplate = buildTemplateFromParams(params);
        
        // DEBUG: 追蹤 buildTemplateFromParams 後的 content
        log.info("[edit] 建構後的 template.content: {}", lineMessageTemplate.getContent());
        
        // 檢查範本代碼唯一性
        if (lineMessageTemplate.getTemplateCode() != null && !lineMessageTemplateService.checkTemplateCodeUnique(lineMessageTemplate)) {
            return error("修改範本'" + lineMessageTemplate.getTemplateName() + "'失敗，範本代碼已存在");
        }

        // 驗證訊息內容
        String validationError = validateMessageContent(lineMessageTemplate);
        if (validationError != null) {
            return error(validationError);
        }

        lineMessageTemplate.setUpdateBy(getUsername());
        int result = lineMessageTemplateService.updateLineMessageTemplate(lineMessageTemplate);
        
        log.info("[edit] 範本更新結果: {}, templateId: {}", result, lineMessageTemplate.getTemplateId());
        
        // 更新圖文範本引用關聯
        maintainImagemapRefs(lineMessageTemplate.getTemplateId(), params);
        
        return toAjax(result);
    }

    /**
     * 刪除範本
     */
    @PreAuthorize("@ss.hasPermi('line:template:remove')")
    @Log(title = "訊息範本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds) {
        return toAjax(lineMessageTemplateService.deleteLineMessageTemplateByIds(templateIds));
    }

    /**
     * 變更範本狀態
     */
    @PreAuthorize("@ss.hasPermi('line:template:edit')")
    @Log(title = "訊息範本", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody Map<String, Object> params) {
        Long templateId = Long.valueOf(params.get("templateId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        
        LineMessageTemplate template = new LineMessageTemplate();
        template.setTemplateId(templateId);
        template.setStatus(status);
        template.setUpdateBy(getUsername());
        
        return toAjax(lineMessageTemplateService.updateLineMessageTemplate(template));
    }

    /**
     * 驗證 Flex Message JSON
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @PostMapping("/validate/flex")
    public AjaxResult validateFlexJson(@RequestBody Map<String, String> params) {
        String jsonContent = params.get("content");
        FlexMessageParser.ValidationResult result = flexMessageParser.validate(jsonContent);

        if (result.valid()) {
            return success("Flex Message JSON 格式正確");
        } else {
            return error(result.errorMessage());
        }
    }

    /**
     * 格式化 JSON
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @PostMapping("/format/json")
    public AjaxResult formatJson(@RequestBody Map<String, String> params) {
        String jsonContent = params.get("content");
        String formatted = flexMessageParser.formatJson(jsonContent);
        return success(formatted);
    }

    /**
     * 預覽範本（替換變數後的內容）
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody Map<String, Object> params) {
        Long templateId = Long.valueOf(params.get("templateId").toString());
        String lineUserId = params.get("lineUserId") != null ? params.get("lineUserId").toString() : null;

        String content = lineMessageTemplateService.parseTemplateContent(templateId, lineUserId);
        if (content == null) {
            return error("找不到範本");
        }
        return success(content);
    }

    /**
     * 發送測試推播（單人推播）
     * 支援單一訊息和多訊息組合格式
     */
    @PreAuthorize("@ss.hasPermi('line:template:edit')")
    @Log(title = "範本測試推播", businessType = BusinessType.OTHER)
    @PostMapping("/sendTest")
    public AjaxResult sendTestMessage(@RequestBody Map<String, Object> params) {
        Long templateId = Long.valueOf(params.get("templateId").toString());
        String lineUserId = params.get("lineUserId").toString();

        LineMessageTemplate template = lineMessageTemplateService.selectLineMessageTemplateById(templateId);
        if (template == null) {
            return error("找不到範本");
        }

        String content = lineMessageTemplateService.parseTemplateContent(templateId, lineUserId);
        if (content == null) {
            return error("無法解析範本內容");
        }

        try {
            // 檢查是否為多訊息格式
            JsonNode rootNode = JacksonUtil.fromJson(
                    content, new TypeReference<>() {
                    }
            );

            if (rootNode != null && rootNode.has("messages") && rootNode.get("messages").isArray()) {
                // 多訊息格式：一次 API 呼叫發送所有訊息
                JsonNode messages = rootNode.get("messages");
                List<PushMessageDTO> pushMessageList = new java.util.ArrayList<>();

                for (int i = 0; i < messages.size(); i++) {
                    JsonNode msg = messages.get(i);
                    String typeStr = msg.has("type") ? msg.get("type").asText().toUpperCase() : null;

                    // 使用 ContentType enum 解析訊息類型
                    ContentType contentType;
                    try {
                        contentType = typeStr != null ? ContentType.fromCode(typeStr) : null;
                    } catch (IllegalArgumentException e) {
                        // 跳過不支援的類型
                        continue;
                    }

                    if (contentType == null) {
                        continue;
                    }

                    PushMessageDTO pushMessage = new PushMessageDTO();
                    pushMessage.setTargetLineUserId(lineUserId);
                    pushMessage.setContentType(contentType);

                    switch (contentType) {
                        case TEXT -> {
                            pushMessage.setTextMessage(msg.has("text") ? msg.get("text").asText() : "");
                            // 處理 emojis
                            if (msg.has("emojis") && msg.get("emojis").isArray()) {
                                List<PushMessageDTO.EmojiDTO> emojiList = new java.util.ArrayList<>();
                                for (var emojiNode : msg.get("emojis")) {
                                    PushMessageDTO.EmojiDTO emojiDTO = new PushMessageDTO.EmojiDTO();
                                    emojiDTO.setIndex(emojiNode.get("index").asInt());
                                    emojiDTO.setProductId(emojiNode.get("productId").asText());
                                    emojiDTO.setEmojiId(emojiNode.get("emojiId").asText());
                                    emojiList.add(emojiDTO);
                                }
                                pushMessage.setEmojis(emojiList);
                            }
                            // 處理 quickReply
                            if (msg.has("quickReply") && msg.get("quickReply").has("items")) {
                                PushMessageDTO.QuickReplyDTO quickReplyDTO = new PushMessageDTO.QuickReplyDTO();
                                List<PushMessageDTO.QuickReplyItemDTO> items = new java.util.ArrayList<>();
                                for (var itemNode : msg.get("quickReply").get("items")) {
                                    PushMessageDTO.QuickReplyItemDTO itemDTO = new PushMessageDTO.QuickReplyItemDTO();
                                    itemDTO.setType(itemNode.has("type") ? itemNode.get("type").asText() : "action");
                                    // Sets optional image URL for the quick reply item
                                    if (itemNode.has("imageUrl") && !itemNode.get("imageUrl").isNull() && !itemNode.get("imageUrl").asText().isEmpty()) {
                                        itemDTO.setImageUrl(itemNode.get("imageUrl").asText());
                                    }
                                    // Extracts action details for a quick reply item
                                    if (itemNode.has("action")) {
                                        var actionNode = itemNode.get("action");
                                        PushMessageDTO.QuickReplyActionDTO actionDTO = new PushMessageDTO.QuickReplyActionDTO();
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
                                pushMessage.setQuickReply(quickReplyDTO);
                            }
                        }
                        case FLEX -> {
                            if (msg.has("contents")) {
                                pushMessage.setFlexMessageJson(msg.get("contents").toString());
                            }
                            pushMessage.setAltText(msg.has("altText") ? msg.get("altText").asText() : "訊息 " + (i + 1));
                        }
                        case IMAGE -> {
                            pushMessage.setImageUrl(msg.has("originalContentUrl") ? msg.get("originalContentUrl").asText() : "");
                            pushMessage.setPreviewImageUrl(msg.has("previewImageUrl") ? msg.get("previewImageUrl").asText() : pushMessage.getImageUrl());
                        }
                        case VIDEO -> {
                            pushMessage.setVideoUrl(msg.has("originalContentUrl") ? msg.get("originalContentUrl").asText() : "");
                            pushMessage.setVideoPreviewImageUrl(msg.has("previewImageUrl") ? msg.get("previewImageUrl").asText() : "");
                        }
                        case AUDIO -> {
                            pushMessage.setAudioUrl(msg.has("originalContentUrl") ? msg.get("originalContentUrl").asText() : "");
                            pushMessage.setAudioDuration(msg.has("duration") ? msg.get("duration").asLong() : 0L);
                        }
                        case STICKER -> {
                            pushMessage.setStickerPackageId(msg.has("packageId") ? msg.get("packageId").asText() : "");
                            pushMessage.setStickerId(msg.has("stickerId") ? msg.get("stickerId").asText() : "");
                        }
                        case LOCATION -> {
                            pushMessage.setLocationTitle(msg.has("title") ? msg.get("title").asText() : "");
                            pushMessage.setLocationAddress(msg.has("address") ? msg.get("address").asText() : "");
                            pushMessage.setLatitude(msg.has("latitude") ? msg.get("latitude").asDouble() : null);
                            pushMessage.setLongitude(msg.has("longitude") ? msg.get("longitude").asDouble() : null);
                        }
                        case IMAGEMAP -> {
                            // Sets imagemap message if required fields are present
                            if (msg.has("baseUrl") && msg.has("baseSize") && msg.has("actions")) {
                                pushMessage.setImagemapMessageJson(msg.toString());
                                pushMessage.setAltText(msg.has("altText") ? msg.get("altText").asText() : "Imagemap訊息");
                            }
                        }
                        case TEMPLATE -> {
                            // 暫不支援
                            continue;
                        }
                    }
                    pushMessageList.add(pushMessage);
                }

                if (pushMessageList.isEmpty()) {
                    return error("沒有可發送的訊息");
                }

                // 一次 API 呼叫發送所有訊息（最多 5 則）
                lineMessageService.sendPushMessages(lineUserId, pushMessageList);
                return success("測試訊息已發送（共 " + pushMessageList.size() + " 則）");
            }

            // 單一訊息格式
            PushMessageDTO pushMessage = new PushMessageDTO();
            pushMessage.setTargetLineUserId(lineUserId);

            // 根據範本類型設定 PushMessageDTO
            ContentType singleContentType = ContentType.fromCode(template.getMsgType());
            if (singleContentType == null) {
                return error("不支援的訊息類型：" + template.getMsgType());
            }

            pushMessage.setContentType(singleContentType);
            switch (singleContentType) {
                case FLEX -> {
                    pushMessage.setFlexMessageJson(content);
                    pushMessage.setAltText(template.getAltText());
                }
                case IMAGEMAP -> {
                    pushMessage.setImagemapMessageJson(content);
                    pushMessage.setAltText(template.getAltText());
                }
                case TEXT -> {
                    // 檢查 content 是否為 JSON 格式（包含 emojis 或 quickReply）
                    if (content.startsWith("{") && (content.contains("\"emojis\"") || content.contains("\"quickReply\""))) {
                        try {
                            JsonNode textNode = JacksonUtil.fromJson(content, new TypeReference<JsonNode>() {});
                            pushMessage.setTextMessage(textNode.get("text").asText());
                            // 解析 emojis
                            if (textNode.has("emojis") && textNode.get("emojis").isArray()) {
                                List<PushMessageDTO.EmojiDTO> emojiList = new java.util.ArrayList<>();
                                for (var emojiNode : textNode.get("emojis")) {
                                    PushMessageDTO.EmojiDTO emojiDTO = new PushMessageDTO.EmojiDTO();
                                    emojiDTO.setIndex(emojiNode.get("index").asInt());
                                    emojiDTO.setProductId(emojiNode.get("productId").asText());
                                    emojiDTO.setEmojiId(emojiNode.get("emojiId").asText());
                                    emojiList.add(emojiDTO);
                                }
                                pushMessage.setEmojis(emojiList);
                            }
                            // 解析 quickReply
                            if (textNode.has("quickReply") && textNode.get("quickReply").has("items")) {
                                PushMessageDTO.QuickReplyDTO quickReplyDTO = new PushMessageDTO.QuickReplyDTO();
                                List<PushMessageDTO.QuickReplyItemDTO> items = new java.util.ArrayList<>();
                                for (var itemNode : textNode.get("quickReply").get("items")) {
                                    PushMessageDTO.QuickReplyItemDTO itemDTO = new PushMessageDTO.QuickReplyItemDTO();
                                    itemDTO.setType(itemNode.has("type") ? itemNode.get("type").asText() : "action");
                                    if (itemNode.has("imageUrl") && !itemNode.get("imageUrl").isNull() && !itemNode.get("imageUrl").asText().isEmpty()) {
                                        itemDTO.setImageUrl(itemNode.get("imageUrl").asText());
                                    }
                                    if (itemNode.has("action")) {
                                        var actionNode = itemNode.get("action");
                                        PushMessageDTO.QuickReplyActionDTO actionDTO = new PushMessageDTO.QuickReplyActionDTO();
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
                                pushMessage.setQuickReply(quickReplyDTO);
                            }
                        } catch (Exception e) {
                            pushMessage.setTextMessage(content);
                        }
                    } else {
                        pushMessage.setTextMessage(content);
                    }
                }
                case IMAGE -> {
                    try {
                        JsonNode node = JacksonUtil.fromJson(content, new TypeReference<JsonNode>() {
                        });
                        // Sets image URLs from JSON or raw content
                        if (node != null && node.has("originalContentUrl")) {
                            pushMessage.setImageUrl(node.get("originalContentUrl").asText());
                            if (node.has("previewImageUrl")) {
                                pushMessage.setPreviewImageUrl(node.get("previewImageUrl").asText());
                            }
                        } else {
                            pushMessage.setImageUrl(content);
                        }
                    } catch (Exception e) {
                        pushMessage.setImageUrl(content);
                    }
                }
                default -> {
                    return error("暫不支援此類型的測試推播：" + singleContentType.getDescription());
                }
            }

            Long messageId = lineMessageService.sendPushMessage(pushMessage);
            return success("測試訊息已發送");
        } catch (Exception e) {
            return error("發送失敗：" + e.getMessage());
        }
    }

    /**
     * 取得預設 Flex 範本列表
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @GetMapping("/flex/presets")
    public AjaxResult getFlexPresets() {
        return success(lineMessageTemplateService.getFlexPresetTemplates());
    }

    /**
     * 取得指定 Flex 範本內容
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @GetMapping("/flex/presets/{templateName}")
    public AjaxResult getFlexPresetContent(@PathVariable String templateName) {
        String content = lineMessageTemplateService.getFlexPresetContent(templateName);
        if (content == null) {
            return error("找不到範本：" + templateName);
        }
        return success(content);
    }

    /**
     * 驗證訊息內容
     * 支援單一訊息和多訊息組合格式
     *
     * @param template 範本
     * @return 錯誤訊息，null 表示驗證通過
     */
    private String validateMessageContent(LineMessageTemplate template) {
        String msgType = template.getMsgType();
        String content = template.getContent();

        if (content == null || content.trim().isEmpty()) {
            return "訊息內容不能為空";
        }

        // 嘗試解析為多訊息格式
        try {
            JsonNode rootNode = JacksonUtil.fromJson(
                    content, new TypeReference<JsonNode>() {
                    }
            );

            // 如果有 messages 陣列，驗證多訊息格式
            if (rootNode != null && rootNode.has("messages") && rootNode.get("messages").isArray()) {
                JsonNode messages = rootNode.get("messages");
                int count = messages.size();

                if (!LineApiLimit.isValidMessageCount(count)) {
                    return LineApiLimit.getMessageCountError();
                }

                // 設定訊息數量
                template.setMessageCount(count);

                // 驗證每個訊息
                for (int i = 0; i < count; i++) {
                    JsonNode msg = messages.get(i);
                    String type = msg.has("type") ? msg.get("type").asText().toUpperCase() : null;

                    ContentType contentType = ContentType.fromCode(type);
                    if (contentType == ContentType.FLEX && msg.has("contents")) {
                        String contentsJson = msg.get("contents").toString();
                        FlexMessageParser.ValidationResult result = flexMessageParser.validate(contentsJson);
                        if (!result.valid()) {
                            return "訊息 " + (i + 1) + " Flex JSON 驗證失敗：" + result.errorMessage();
                        }
                    }
                }
                return null;
            }
        } catch (Exception e) {
            // 不是 JSON 格式，繼續使用傳統驗證
        }

        // 傳統單一訊息驗證
        template.setMessageCount(1);

        ContentType contentType = ContentType.fromCode(msgType);
        if (contentType == ContentType.FLEX) {
            FlexMessageParser.ValidationResult result = flexMessageParser.validate(content);
            if (!result.valid()) {
                return "Flex Message JSON 驗證失敗：" + result.errorMessage();
            }
        } else if (contentType == ContentType.IMAGEMAP) {
            try {
                ImagemapMessageDto imagemap = JacksonUtil.decodeFromJson(content, ImagemapMessageDto.class);
                if (imagemap == null) {
                    return "Imagemap JSON 內容不能為空";
                }
                if (StringUtils.isEmpty(imagemap.getBaseUrl())) {
                    return "Imagemap baseUrl 不能為空";
                }
                if (StringUtils.isEmpty(imagemap.getAltText())) {
                    return "Imagemap altText 不能為空";
                }
                if (imagemap.getBaseSize() == null || imagemap.getBaseSize().getHeight() == null) {
                    return "Imagemap baseSize.height 不能為空";
                }
                if (imagemap.getActions() == null || imagemap.getActions().isEmpty()) {
                    return "Imagemap actions 不能為空";
                }

                // 驗證 action area 是否超出圖片範圍
                int baseWidth = imagemap.getBaseSize().getWidth() != null ? imagemap.getBaseSize().getWidth() : ImagemapWidth.getBaseWidth();
                int baseHeight = imagemap.getBaseSize().getHeight();

                for (int i = 0; i < imagemap.getActions().size(); i++) {
                    ImagemapMessageDto.ActionDto action = imagemap.getActions().get(i);
                    String actionError = validateImagemapAction(action, i + 1, baseWidth, baseHeight);
                    if (actionError != null) {
                        return actionError;
                    }
                }
            } catch (Exception e) {
                return "Imagemap JSON 格式錯誤：" + e.getMessage();
            }
        }

        return null;
    }

    /**
     * 驗證 Imagemap Action
     *
     * @param action     動作 DTO
     * @param index      動作索引（從 1 開始，用於錯誤訊息）
     * @param baseWidth  圖片基準寬度
     * @param baseHeight 圖片基準高度
     * @return 錯誤訊息，null 表示驗證通過
     */
    private String validateImagemapAction(ImagemapMessageDto.ActionDto action, int index, int baseWidth, int baseHeight) {
        // 驗證類型
        if (StringUtils.isEmpty(action.getType())) {
            return String.format("動作 %d：type 不能為空", index);
        }

        String type = action.getType().toLowerCase();
        if (!"uri".equals(type) && !"message".equals(type) && !"clipboard".equals(type)) {
            return String.format("動作 %d：type 必須為 uri、message 或 clipboard", index);
        }

        // 驗證必填欄位
        if ("uri".equals(type) && StringUtils.isEmpty(action.getLinkUri())) {
            return String.format("動作 %d：linkUri 不能為空（type=uri 時必填）", index);
        }
        if ("message".equals(type) && StringUtils.isEmpty(action.getText())) {
            return String.format("動作 %d：text 不能為空（type=message 時必填）", index);
        }
        if ("clipboard".equals(type) && StringUtils.isEmpty(action.getClipboardText())) {
            return String.format("動作 %d：clipboardText 不能為空（type=clipboard 時必填）", index);
        }

        // 驗證區域
        ImagemapMessageDto.AreaDto area = action.getArea();
        if (area == null) {
            return String.format("動作 %d：area 不能為空", index);
        }
        if (area.getX() == null || area.getY() == null || area.getWidth() == null || area.getHeight() == null) {
            return String.format("動作 %d：area 的 x, y, width, height 都不能為空", index);
        }

        // 驗證區域是否在圖片範圍內
        int x = area.getX();
        int y = area.getY();
        int w = area.getWidth();
        int h = area.getHeight();

        if (x < 0 || y < 0) {
            return String.format("動作 %d：area 起點座標不能為負數 (x=%d, y=%d)", index, x, y);
        }
        if (w <= 0 || h <= 0) {
            return String.format("動作 %d：area 寬高必須大於 0 (width=%d, height=%d)", index, w, h);
        }
        if (x + w > baseWidth) {
            return String.format("動作 %d：area 超出圖片右邊界 (x=%d + width=%d = %d > 圖片寬度 %d)", index, x, w, x + w, baseWidth);
        }
        if (y + h > baseHeight) {
            return String.format("動作 %d：area 超出圖片下邊界 (y=%d + height=%d = %d > 圖片高度 %d)", index, y, h, y + h, baseHeight);
        }

        return null;
    }

    /**
     * 查詢圖文範本被哪些訊息範本引用
     */
    @PreAuthorize("@ss.hasPermi('line:template:query')")
    @GetMapping("/imagemap/{imagemapId}/references")
    public AjaxResult getImagemapReferences(@PathVariable Long imagemapId) {
        return success(imagemapRefService.selectRefsByImagemapId(imagemapId));
    }

    /**
     * 同步更新引用的訊息範本
     */
    @PreAuthorize("@ss.hasPermi('line:template:edit')")
    @Log(title = "同步圖文範本引用", businessType = BusinessType.UPDATE)
    @PostMapping("/imagemap/{imagemapId}/sync-references")
    public AjaxResult syncImagemapReferences(
            @PathVariable Long imagemapId,
            @RequestBody(required = false) Map<String, Object> params) {
        List<Long> templateIds = null;
        if (params != null && params.containsKey("templateIds")) {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) params.get("templateIds");
            if (ids != null) {
                templateIds = ids.stream().map(Number::longValue).toList();
            }
        }
        int count = imagemapRefService.syncReferences(imagemapId, templateIds);
        return success(count);
    }

    /**
     * 維護圖文範本引用關聯
     */
    private void maintainImagemapRefs(Long templateId, Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> imagemapRefs = (List<Map<String, Object>>) params.get("imagemapRefs");
        
        if (imagemapRefs == null || imagemapRefs.isEmpty()) {
            // 清除所有關聯
            imagemapRefService.deleteRefsByTemplateId(templateId);
            return;
        }
        
        // 建立關聯（傳遞完整的 ref 資訊）
        imagemapRefService.maintainRefsWithIndex(templateId, imagemapRefs);
    }

    /**
     * 從 Map 參數建構 LineMessageTemplate 物件
     */
    private LineMessageTemplate buildTemplateFromParams(Map<String, Object> params) {
        LineMessageTemplate template = new LineMessageTemplate();
        
        if (params.get("templateId") != null) {
            template.setTemplateId(Long.valueOf(params.get("templateId").toString()));
        }
        if (params.get("templateName") != null) {
            template.setTemplateName(params.get("templateName").toString());
        }
        if (params.get("templateCode") != null) {
            template.setTemplateCode(params.get("templateCode").toString());
        }
        if (params.get("msgType") != null) {
            template.setMsgType(params.get("msgType").toString());
        }
        if (params.get("content") != null) {
            template.setContent(params.get("content").toString());
        }
        if (params.get("altText") != null) {
            template.setAltText(params.get("altText").toString());
        }
        if (params.get("previewImg") != null) {
            template.setPreviewImg(params.get("previewImg").toString());
        }
        if (params.get("status") != null) {
            template.setStatus(Integer.valueOf(params.get("status").toString()));
        }
        if (params.get("sortOrder") != null) {
            template.setSortOrder(Integer.valueOf(params.get("sortOrder").toString()));
        }
        if (params.get("remark") != null) {
            template.setRemark(params.get("remark").toString());
        }
        if (params.get("categoryId") != null) {
            template.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        
        return template;
    }
}
