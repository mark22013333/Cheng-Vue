package com.cheng.crawler.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author cheng
 * @since 2023/12/3 1:13 AM
 **/
@Slf4j
public class JacksonUtil {

    private JacksonUtil() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    /**
     * 從 {@link ObjectNode} 中取得指定鍵的字串值。
     * 如果該鍵不存在或其值為 null，則返回提供的預設值。
     *
     * @param node         要從中取得值的 {@link ObjectNode}，不得為 null。
     * @param key          要在 {@link ObjectNode} 中查找的鍵，不得為 null。
     * @param defaultValue 當鍵不存在或其值為 null 時返回的預設值。
     * @return 指定鍵對應的字串值；如果該鍵不存在，則返回預設值。
     * @throws NullPointerException 如果 {@code node} 或 {@code key} 為 null，則拋出此例外。
     */
    public static String getStringOrDefault(ObjectNode node, String key, String defaultValue) {
        return Optional.ofNullable(node.get(key))
                .map(JsonNode::asText)
                .orElse(defaultValue);
    }

    /**
     * 確保 {@link ObjectNode} 中存在指定的鍵。如果該鍵不存在，則使用提供的預設值新增該鍵。
     *
     * @param node         要修改的 {@link ObjectNode}，不得為 null。
     * @param key          要檢查或新增的鍵，不得為 null。
     * @param defaultValue 如果該鍵不存在，則新增該鍵並設置為此預設值。
     * @throws NullPointerException 如果 {@code node} 或 {@code key} 為 null，則拋出此例外。
     */
    public static void ensureKey(ObjectNode node, String key, String defaultValue) {
        if (!node.has(key)) {
            node.put(key, defaultValue);
        }
    }

    /**
     * 序列化
     */
    public static <T> String encodeToJson(T obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("json encode error, obj={}, ERR={}", obj, ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * 反序列化
     */
    public static <T> T decodeFromJson(String json, Class<T> valueType) {
        if (!StringUtils.isBlank(json) && !Objects.isNull(valueType)) {
            try {
                return MAPPER.readValue(json, valueType);
            } catch (Exception e) {
                log.error("json decode fail,jsonString={}, type={}, ERR={}",
                        json, valueType.getName(), ExceptionUtils.getStackTrace(e));
            }
        }
        return null;
    }

    /**
     * 將任何 Java 物件轉換為 Jackson 的 JsonNode。
     * 此方法使用 ObjectMapper 的 valueToTree 方法來將物件轉換成 JsonNode。
     *
     * @param <T> 輸入物件的類型。
     * @param obj 要轉換成 JsonNode 的物件。可以是任何自訂或內建類型。
     * @return 轉換後的 JsonNode 。
     */
    public static <T> ObjectNode toNode(T obj) {
        return MAPPER.valueToTree(obj);
    }

    /**
     * 將 JSON 陣列字串轉換成指定類型的物件列表。
     *
     * @param jsonArray JSON 陣列字串
     * @param clazz     DTO 物件的類型
     * @return 轉換後的 DTO 物件列表
     * @throws IOException 當 JSON 處理出錯時拋出異常
     */
    public static <T> List<T> convertJsonArrayToList(String jsonArray, Class<T> clazz) throws IOException {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
        return MAPPER.readValue(jsonArray, type);
    }

    /**
     * 反序列化成ObjectNode
     */
    public static ObjectNode decodeObject(String json) throws IOException {
        return (ObjectNode) MAPPER.readTree(json);
    }

    /**
     * 反序列化成list
     */
    public static <T> List<T> decode2List(String json, Class<T> clazz) {
        if (!StringUtils.isBlank(json) && !Objects.isNull(clazz)) {
            try {
                return MAPPER.readValue(
                        json, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            } catch (Exception e) {
                log.error("json decode2list fail, json={}, classType={}, ERR={}",
                        json,
                        clazz.getName(),
                        ExceptionUtils.getStackTrace(e));
            }
        }
        log.warn("####=> decode FAIL. return null");
        return Collections.emptyList();
    }

    /**
     * 使用TypeReference反序列化JSON為複雜泛型類型
     * 這個方法主要用於處理複雜的泛型類型，例如List<String>, Map<String, Object>等
     * 使用方法：JacksonUtils.fromJson(jsonStr, new TypeReference<List<String>>() {});
     *
     * @param json          JSON字串
     * @param typeReference 泛型類型引用，例如new TypeReference<List<String>>() {}
     * @return 解析後的對象，如果解析失敗則返回null
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json) || typeReference == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("JSON解析為複雜類型失敗，json={}, typeReference={}, 錯誤={}",
                    json, typeReference.getType().getTypeName(), ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * 將 InputStream 轉換為指定類型的物件
     */
    public static <T> T streamToObject(InputStream is, Class<T> clazz) {
        try {
            return MAPPER.readValue(is, clazz);
        } catch (Exception e) {
            log.error("stream to object fail, is={}, classType={}, ERR={}",
                    is,
                    clazz.getName(),
                    ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * map轉class物件
     */
    public static <T> T mapToObject(Map<String, String> map, Class<T> clazz) {
        String json = null;
        try {
            json = MAPPER.writeValueAsString(map);
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("json map2object fail, map={}, ClassName={}, ERR={}",
                    json, clazz.getName(), ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static Map<String, String> toMap(String json) {
        return decodeFromJson(json, Map.class);
    }

    public static ObjectNode genJsonObject() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode genJsonArray() {
        return MAPPER.createArrayNode();
    }

    public static String toJsonString(Object content) {
        try {
            return MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return content.getClass().getName() + '@' + Integer.toHexString(content.hashCode());
        }
    }

    public static ObjectNode toObjectNode(String jsonStr) throws IOException {
        return (ObjectNode) MAPPER.readTree(jsonStr);
    }

    public static ArrayNode toArrayNode(String jsonStr) throws IOException {
        return (ArrayNode) MAPPER.readTree(jsonStr);
    }

    /**
     * 將 JsonNode 轉換為目標物件清單
     *
     * @param arrayNode  要轉換的 JsonNode (必須是 ArrayNode)
     * @param targetType 目標類型的 Class
     * @param <T>        目標類型
     * @return List<T> 轉換後的物件清單
     */
    public static <T> List<T> toList(ArrayNode arrayNode, Class<T> targetType) {
        if (arrayNode == null || !arrayNode.isArray()) {
            throw new IllegalArgumentException("JsonNode 必須是 ArrayNode 類型");
        }

        return MAPPER.convertValue(arrayNode,
                MAPPER.getTypeFactory().constructCollectionType(List.class, targetType)
        );
    }

    /**
     * 將 JsonNode 轉換為目標物件清單 (適用於非 ArrayNode 的 JsonNode)
     *
     * @param jsonNode   JsonNode
     * @param targetType 目標類型的 Class
     * @param <T>        目標類型
     * @return List<T> 轉換後的物件清單
     */
    public static <T> List<T> toList(JsonNode jsonNode, Class<T> targetType) {
        if (jsonNode == null || !jsonNode.isArray()) {
            throw new IllegalArgumentException("JsonNode 必須是 ArrayNode 類型");
        }

        return toList((ArrayNode) jsonNode, targetType);
    }

    public static JsonNode toJsonNode(String jsonStr) throws IOException {
        return MAPPER.readTree(jsonStr);
    }

    public static boolean hasEscape(String text) {
        return text.contains(StringUtils.LF)
                || text.contains(StringUtils.CR)
                || text.contains("\\")
                || text.contains("\t")
                || text.contains("\"");
    }
}
