package com.cheng.line.service.impl;

import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.line.domain.LineMessageTemplate;
import com.cheng.line.domain.LineTemplateImagemapRef;
import com.cheng.line.enums.ContentType;
import com.cheng.line.mapper.LineMessageTemplateMapper;
import com.cheng.line.mapper.LineTemplateImagemapRefMapper;
import com.cheng.line.service.ILineTemplateImagemapRefService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 訊息範本與圖文範本關聯 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineTemplateImagemapRefServiceImpl implements ILineTemplateImagemapRefService {

    @Resource
    private LineTemplateImagemapRefMapper refMapper;

    @Resource
    private LineMessageTemplateMapper templateMapper;

    @Override
    public List<LineTemplateImagemapRef> selectRefsByImagemapId(Long imagemapId) {
        return refMapper.selectRefsByImagemapId(imagemapId);
    }

    @Override
    public List<LineTemplateImagemapRef> selectRefsByTemplateId(Long templateId) {
        return refMapper.selectRefsByTemplateId(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncReferences(Long imagemapId, List<Long> templateIds) {
        // 取得圖文範本內容
        LineMessageTemplate imagemapTemplate = templateMapper.selectLineMessageTemplateById(imagemapId);
        if (imagemapTemplate == null) {
            log.warn("同步引用失敗：找不到圖文範本 ID={}", imagemapId);
            return 0;
        }

        // 取得所有引用此圖文範本的訊息範本
        List<LineTemplateImagemapRef> refs = refMapper.selectRefsByImagemapId(imagemapId);
        if (refs.isEmpty()) {
            return 0;
        }

        int syncCount = 0;
        for (LineTemplateImagemapRef ref : refs) {
            // 如果指定了要同步的範本列表，則只同步指定的
            if (templateIds != null && !templateIds.contains(ref.getTemplateId())) {
                continue;
            }

            try {
                // 取得訊息範本
                LineMessageTemplate msgTemplate = templateMapper.selectLineMessageTemplateById(ref.getTemplateId());
                if (msgTemplate == null) {
                    continue;
                }

                // 解析訊息範本的 content，更新對應位置的 IMAGEMAP 內容
                String updatedContent = updateImagemapInContent(
                        msgTemplate.getContent(),
                        ref.getMessageIndex(),
                        imagemapTemplate.getContent()
                );

                if (updatedContent != null) {
                    msgTemplate.setContent(updatedContent);
                    msgTemplate.setUpdateBy(SecurityUtils.getUsername());
                    templateMapper.updateLineMessageTemplate(msgTemplate);
                    syncCount++;
                    log.info("已同步更新訊息範本 ID={} 中的圖文訊息（位置 {}）", ref.getTemplateId(), ref.getMessageIndex());
                }
            } catch (Exception e) {
                log.error("同步訊息範本 ID={} 失敗：{}", ref.getTemplateId(), e.getMessage());
            }
        }

        return syncCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void maintainRefs(Long templateId, List<Long> imagemapIds) {
        // 先刪除舊的關聯
        refMapper.deleteRefsByTemplateId(templateId);

        // 如果沒有新的圖文範本引用，直接返回
        if (imagemapIds == null || imagemapIds.isEmpty()) {
            return;
        }

        // 建立新的關聯
        String username = SecurityUtils.getUsername();
        List<LineTemplateImagemapRef> refs = new ArrayList<>();
        for (int i = 0; i < imagemapIds.size(); i++) {
            LineTemplateImagemapRef ref = new LineTemplateImagemapRef();
            ref.setTemplateId(templateId);
            ref.setImagemapId(imagemapIds.get(i));
            ref.setMessageIndex(i);
            ref.setCreateBy(username);
            refs.add(ref);
        }

        if (!refs.isEmpty()) {
            refMapper.batchInsertRefs(refs);
        }
    }

    @Override
    public int deleteRefsByTemplateId(Long templateId) {
        return refMapper.deleteRefsByTemplateId(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void maintainRefsWithIndex(Long templateId, List<Map<String, Object>> imagemapRefs) {
        // 先刪除舊的關聯
        refMapper.deleteRefsByTemplateId(templateId);

        // 如果沒有新的圖文範本引用，直接返回
        if (imagemapRefs == null || imagemapRefs.isEmpty()) {
            return;
        }

        // 建立新的關聯
        String username = SecurityUtils.getUsername();
        List<LineTemplateImagemapRef> refs = new ArrayList<>();
        
        for (Map<String, Object> refData : imagemapRefs) {
            Long imagemapId = Long.valueOf(refData.get("imagemapId").toString());
            Integer messageIndex = Integer.valueOf(refData.get("messageIndex").toString());
            
            LineTemplateImagemapRef ref = new LineTemplateImagemapRef();
            ref.setTemplateId(templateId);
            ref.setImagemapId(imagemapId);
            ref.setMessageIndex(messageIndex);
            ref.setCreateBy(username);
            refs.add(ref);
        }

        if (!refs.isEmpty()) {
            refMapper.batchInsertRefs(refs);
        }
    }

    /**
     * 更新訊息內容中指定位置的 IMAGEMAP 訊息
     */
    private String updateImagemapInContent(String content, int messageIndex, String imagemapContent) {
        try {
            JsonNode rootNode = JacksonUtil.toJsonNode(content);

            // 檢查是否為多訊息格式
            if (rootNode.has("messages") && rootNode.get("messages").isArray()) {
                JsonNode messages = rootNode.get("messages");
                if (messageIndex < messages.size()) {
                    JsonNode targetMsg = messages.get(messageIndex);
                    String msgType = targetMsg.get("type").asText();
                    if (ContentType.IMAGEMAP == ContentType.fromCode(msgType)) {
                        // 解析圖文範本內容
                        JsonNode imagemapNode = JacksonUtil.toJsonNode(imagemapContent);

                        // 確保包含 type: "imagemap"（圖文範本儲存時可能不包含）
                        ObjectNode newImagemapNode;
                        if (imagemapNode.isObject()) {
                            newImagemapNode = (ObjectNode) imagemapNode;
                            if (!newImagemapNode.has("type")) {
                                newImagemapNode.put("type", "imagemap");
                            }
                        } else {
                            log.warn("圖文範本內容格式不正確");
                            return null;
                        }

                        // 保留原有的 imagemapSourceId（用於前端下拉選單顯示）
                        if (targetMsg.has("imagemapSourceId")) {
                            newImagemapNode.set("imagemapSourceId", targetMsg.get("imagemapSourceId"));
                        }

                        // 使用 ArrayNode 來修改
                        ArrayNode messagesArray = (ArrayNode) messages;
                        messagesArray.set(messageIndex, newImagemapNode);

                        return JacksonUtil.encodeToJson(rootNode);
                    }
                }
            }
        } catch (Exception e) {
            log.error("更新 IMAGEMAP 內容失敗：{}", e.getMessage());
        }
        return null;
    }
}
