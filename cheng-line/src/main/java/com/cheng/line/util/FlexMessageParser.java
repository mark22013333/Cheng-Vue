package com.cheng.line.util;

import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Flex Message JSON 解析器與驗證器
 * <p>
 * 驗證前端傳來的 JSON 是否符合 LINE Flex Message Object 結構
 * 參考文件：https://developers.line.biz/en/reference/messaging-api/#flex-message
 *
 * @author cheng
 */
@Slf4j
@Component
public class FlexMessageParser {

    /**
     * Flex Container 類型
     */
    private static final Set<String> VALID_CONTAINER_TYPES = Set.of("bubble", "carousel");

    /**
     * Flex Component 類型
     */
    private static final Set<String> VALID_COMPONENT_TYPES = Set.of(
            "box", "button", "image", "icon", "text", "span", "separator", "filler", "video"
    );

    /**
     * 有效的 layout 值
     */
    private static final Set<String> VALID_LAYOUTS = Set.of(
            "horizontal", "vertical", "baseline"
    );

    /**
     * 有效的 action 類型
     */
    private static final Set<String> VALID_ACTION_TYPES = Set.of(
            "uri", "message", "postback", "datetimepicker", "camera", "cameraRoll", "location", "richmenuswitch"
    );

    /**
     * 驗證結果類別
     */
    public record ValidationResult(boolean valid, String errorMessage, JsonNode parsedJson) {
        public static ValidationResult success(JsonNode json) {
            return new ValidationResult(true, null, json);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null);
        }
    }

    /**
     * 驗證 Flex Message JSON 字串
     *
     * @param jsonString JSON 字串
     * @return 驗證結果
     */
    public ValidationResult validate(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return ValidationResult.error("Flex Message 內容不能為空");
        }

        try {
            // 解析 JSON
            JsonNode rootNode = JacksonUtil.fromJson(jsonString, new TypeReference<JsonNode>() {});
            if (rootNode == null) {
                return ValidationResult.error("無效的 JSON 格式");
            }

            // 驗證結構
            String error = validateFlexContainer(rootNode);
            if (error != null) {
                return ValidationResult.error(error);
            }

            return ValidationResult.success(rootNode);

        } catch (Exception e) {
            log.error("解析 Flex Message JSON 失敗", e);
            return ValidationResult.error("JSON 解析失敗：" + e.getMessage());
        }
    }

    /**
     * 驗證 Flex Container
     */
    private String validateFlexContainer(JsonNode node) {
        if (!node.has("type")) {
            return "缺少必要欄位：type";
        }

        String type = node.get("type").asText();

        if (!VALID_CONTAINER_TYPES.contains(type)) {
            return "無效的容器類型：" + type + "，有效值：" + VALID_CONTAINER_TYPES;
        }

        return switch (type) {
            case "bubble" -> validateBubble(node);
            case "carousel" -> validateCarousel(node);
            default -> null;
        };
    }

    /**
     * 驗證 Bubble 容器
     */
    private String validateBubble(JsonNode node) {
        // Bubble 可選欄位：header, hero, body, footer, styles, action
        String[] optionalBlocks = {"header", "hero", "body", "footer"};

        for (String block : optionalBlocks) {
            if (node.has(block)) {
                String error = validateComponent(node.get(block), block);
                if (error != null) {
                    return error;
                }
            }
        }

        // 驗證 action（可選）
        if (node.has("action")) {
            String error = validateAction(node.get("action"));
            if (error != null) {
                return error;
            }
        }

        return null;
    }

    /**
     * 驗證 Carousel 容器
     */
    private String validateCarousel(JsonNode node) {
        if (!node.has("contents")) {
            return "Carousel 缺少必要欄位：contents";
        }

        JsonNode contents = node.get("contents");
        if (!contents.isArray()) {
            return "Carousel 的 contents 必須是陣列";
        }

        if (contents.isEmpty()) {
            return "Carousel 的 contents 不能為空";
        }

        if (contents.size() > 12) {
            return "Carousel 的 contents 最多只能有 12 個 bubble";
        }

        for (int i = 0; i < contents.size(); i++) {
            JsonNode bubble = contents.get(i);
            if (!bubble.has("type") || !"bubble".equals(bubble.get("type").asText())) {
                return "Carousel contents[" + i + "] 必須是 bubble 類型";
            }
            String error = validateBubble(bubble);
            if (error != null) {
                return "Carousel contents[" + i + "]: " + error;
            }
        }

        return null;
    }

    /**
     * 驗證元件
     */
    private String validateComponent(JsonNode node, String context) {
        if (node == null || node.isNull()) {
            return null;
        }

        if (!node.has("type")) {
            return context + " 缺少必要欄位：type";
        }

        String type = node.get("type").asText();

        if (!VALID_COMPONENT_TYPES.contains(type)) {
            return context + " 的類型無效：" + type;
        }

        // Box 類型需要驗證 contents
        if ("box".equals(type)) {
            return validateBox(node, context);
        }

        // Button 類型需要驗證 action
        if ("button".equals(type)) {
            return validateButton(node, context);
        }

        // Image 類型需要驗證 url
        if ("image".equals(type)) {
            return validateImage(node, context);
        }

        // 驗證 action（如果存在）
        if (node.has("action")) {
            String error = validateAction(node.get("action"));
            if (error != null) {
                return context + " " + error;
            }
        }

        return null;
    }

    /**
     * 驗證 Box 元件
     */
    private String validateBox(JsonNode node, String context) {
        if (!node.has("layout")) {
            return context + " Box 缺少必要欄位：layout";
        }

        String layout = node.get("layout").asText();
        if (!VALID_LAYOUTS.contains(layout)) {
            return context + " Box 的 layout 無效：" + layout + "，有效值：" + VALID_LAYOUTS;
        }

        if (node.has("contents")) {
            JsonNode contents = node.get("contents");
            if (!contents.isArray()) {
                return context + " Box 的 contents 必須是陣列";
            }

            for (int i = 0; i < contents.size(); i++) {
                String error = validateComponent(contents.get(i), context + ".contents[" + i + "]");
                if (error != null) {
                    return error;
                }
            }
        }

        return null;
    }

    /**
     * 驗證 Button 元件
     */
    private String validateButton(JsonNode node, String context) {
        if (!node.has("action")) {
            return context + " Button 缺少必要欄位：action";
        }

        return validateAction(node.get("action"));
    }

    /**
     * 驗證 Image 元件
     */
    private String validateImage(JsonNode node, String context) {
        if (!node.has("url")) {
            return context + " Image 缺少必要欄位：url";
        }

        String url = node.get("url").asText();
        if (!url.startsWith("https://")) {
            return context + " Image 的 url 必須是 HTTPS 協定";
        }

        return null;
    }

    /**
     * 驗證 Action
     */
    private String validateAction(JsonNode node) {
        if (node == null || node.isNull()) {
            return "action 不能為空";
        }

        if (!node.has("type")) {
            return "action 缺少必要欄位：type";
        }

        String type = node.get("type").asText();
        if (!VALID_ACTION_TYPES.contains(type)) {
            return "action 類型無效：" + type + "，有效值：" + VALID_ACTION_TYPES;
        }

        return switch (type) {
            case "uri" -> validateUriAction(node);
            case "message" -> validateMessageAction(node);
            case "postback" -> validatePostbackAction(node);
            default -> null;
        };
    }

    /**
     * 驗證 URI Action
     */
    private String validateUriAction(JsonNode node) {
        if (!node.has("uri")) {
            return "URI action 缺少必要欄位：uri";
        }
        return null;
    }

    /**
     * 驗證 Message Action
     */
    private String validateMessageAction(JsonNode node) {
        if (!node.has("text")) {
            return "Message action 缺少必要欄位：text";
        }

        String text = node.get("text").asText();
        if (text.length() > 300) {
            return "Message action 的 text 最多 300 字元";
        }

        return null;
    }

    /**
     * 驗證 Postback Action
     */
    private String validatePostbackAction(JsonNode node) {
        if (!node.has("data")) {
            return "Postback action 缺少必要欄位：data";
        }

        String data = node.get("data").asText();
        if (data.length() > 300) {
            return "Postback action 的 data 最多 300 字元";
        }

        return null;
    }

    /**
     * 檢查 JSON 字串是否為有效的 Flex Message
     *
     * @param jsonString JSON 字串
     * @return 是否有效
     */
    public boolean isValidFlexMessage(String jsonString) {
        return validate(jsonString).valid();
    }

    /**
     * 格式化 JSON 字串（美化輸出）
     *
     * @param jsonString JSON 字串
     * @return 格式化後的 JSON
     */
    public String formatJson(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return jsonString;
        }

        try {
            JsonNode node = JacksonUtil.fromJson(jsonString, new TypeReference<JsonNode>() {});
            return JacksonUtil.toJsonString(node);
        } catch (Exception e) {
            log.warn("格式化 JSON 失敗", e);
            return jsonString;
        }
    }

    /**
     * 壓縮 JSON 字串（移除空白）
     *
     * @param jsonString JSON 字串
     * @return 壓縮後的 JSON
     */
    public String compactJson(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return jsonString;
        }

        try {
            JsonNode node = JacksonUtil.fromJson(jsonString, new TypeReference<JsonNode>() {});
            return JacksonUtil.encodeToJson(node);
        } catch (Exception e) {
            log.warn("壓縮 JSON 失敗", e);
            return jsonString;
        }
    }
}
