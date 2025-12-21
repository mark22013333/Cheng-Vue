package com.cheng.line.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.LineMessageTemplate;
import com.cheng.line.dto.PushMessageDTO;
import com.cheng.line.enums.ContentType;
import com.cheng.line.service.ILineMessageService;
import com.cheng.line.service.ILineMessageTemplateService;
import com.cheng.line.util.FlexMessageParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public AjaxResult add(@Validated @RequestBody LineMessageTemplate lineMessageTemplate) {
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
        return success(lineMessageTemplate.getTemplateId());
    }

    /**
     * 修改範本
     */
    @PreAuthorize("@ss.hasPermi('line:template:edit')")
    @Log(title = "訊息範本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody LineMessageTemplate lineMessageTemplate) {
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
        return toAjax(lineMessageTemplateService.updateLineMessageTemplate(lineMessageTemplate));
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
                        case TEXT -> pushMessage.setTextMessage(msg.has("text") ? msg.get("text").asText() : "");
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
                        case TEMPLATE, IMAGEMAP -> {
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
            String msgType = template.getMsgType();
            if ("FLEX".equalsIgnoreCase(msgType)) {
                pushMessage.setContentType(ContentType.FLEX);
                // Flex 需要 JSON 格式字串
                pushMessage.setFlexMessageJson(content);
                pushMessage.setAltText(template.getAltText());
            } else if ("TEXT".equalsIgnoreCase(msgType)) {
                pushMessage.setContentType(ContentType.TEXT);
                pushMessage.setTextMessage(content);
            } else if ("IMAGE".equalsIgnoreCase(msgType)) {
                pushMessage.setContentType(ContentType.IMAGE);
                // 假設 content 是 JSON，需解析出 url
                try {
                    JsonNode node = JacksonUtil.fromJson(
                            content, new com.fasterxml.jackson.core.type.TypeReference<JsonNode>() {
                            }
                    );
                    if (node.has("originalContentUrl")) {
                        pushMessage.setImageUrl(node.get("originalContentUrl").asText());
                        if (node.has("previewImageUrl")) {
                            pushMessage.setPreviewImageUrl(node.get("previewImageUrl").asText());
                        }
                    } else {
                        // 舊格式或純 URL
                        pushMessage.setImageUrl(content);
                    }
                } catch (Exception e) {
                    pushMessage.setImageUrl(content);
                }
            } else {
                // 其他類型暫時當作 TEMPLATE (如果 Service 有支援) 或拋出不支援
                // 為了避免 Service 拋出「暫不支援 TEMPLATE」，這裡嘗試轉為 FLEX 或 TEXT，或直接告知
                return error("暫不支援此類型的測試推播：" + msgType);
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
                    content, new com.fasterxml.jackson.core.type.TypeReference<JsonNode>() {
                    }
            );

            // 如果有 messages 陣列，驗證多訊息格式
            if (rootNode != null && rootNode.has("messages") && rootNode.get("messages").isArray()) {
                JsonNode messages = rootNode.get("messages");
                int count = messages.size();

                if (count < 1 || count > 5) {
                    return "訊息數量必須在 1 到 5 之間（LINE API 限制）";
                }

                // 設定訊息數量
                template.setMessageCount(count);

                // 驗證每個訊息
                for (int i = 0; i < count; i++) {
                    JsonNode msg = messages.get(i);
                    String type = msg.has("type") ? msg.get("type").asText().toUpperCase() : null;

                    if ("FLEX".equals(type) && msg.has("contents")) {
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

        if ("FLEX".equals(msgType)) {
            FlexMessageParser.ValidationResult result = flexMessageParser.validate(content);
            if (!result.valid()) {
                return "Flex Message JSON 驗證失敗：" + result.errorMessage();
            }
        } else if ("IMAGEMAP".equals(msgType)) {
            try {
                JacksonUtil.fromJson(
                        content, new com.fasterxml.jackson.core.type.TypeReference<JsonNode>() {
                        }
                );
            } catch (Exception e) {
                return "Imagemap JSON 格式錯誤：" + e.getMessage();
            }
        }

        return null;
    }
}
