package com.cheng.line.service.impl;

import com.cheng.common.constant.UserConstants;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.domain.LineMessageTemplate;
import com.cheng.line.domain.LineUser;
import com.cheng.line.mapper.LineMessageTemplateMapper;
import com.cheng.line.mapper.LineUserMapper;
import com.cheng.line.service.ILineMessageTemplateService;
import com.cheng.line.util.TemplateVariableEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LINE 訊息範本 服務層實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineMessageTemplateServiceImpl implements ILineMessageTemplateService {

    private @Resource LineMessageTemplateMapper lineMessageTemplateMapper;
    private @Resource LineUserMapper lineUserMapper;
    private @Resource TemplateVariableEngine templateVariableEngine;

    @Override
    public LineMessageTemplate selectLineMessageTemplateById(Long templateId) {
        return lineMessageTemplateMapper.selectLineMessageTemplateById(templateId);
    }

    @Override
    public LineMessageTemplate selectLineMessageTemplateByCode(String templateCode) {
        return lineMessageTemplateMapper.selectLineMessageTemplateByCode(templateCode);
    }

    @Override
    public List<LineMessageTemplate> selectLineMessageTemplateList(LineMessageTemplate lineMessageTemplate) {
        return lineMessageTemplateMapper.selectLineMessageTemplateList(lineMessageTemplate);
    }

    @Override
    public List<LineMessageTemplate> selectLineMessageTemplateByType(String msgType) {
        return lineMessageTemplateMapper.selectLineMessageTemplateByType(msgType);
    }

    @Override
    public int insertLineMessageTemplate(LineMessageTemplate lineMessageTemplate) {
        // 設定建立者資訊
        try {
            Long userId = SecurityUtils.getUserId();
            String username = SecurityUtils.getUsername();
            String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
            
            lineMessageTemplate.setCreatorId(userId);
            lineMessageTemplate.setCreatorName(nickName != null ? nickName : username);
        } catch (Exception e) {
            log.warn("無法取得當前使用者資訊", e);
        }
        return lineMessageTemplateMapper.insertLineMessageTemplate(lineMessageTemplate);
    }

    @Override
    public int updateLineMessageTemplate(LineMessageTemplate lineMessageTemplate) {
        return lineMessageTemplateMapper.updateLineMessageTemplate(lineMessageTemplate);
    }

    @Override
    public int deleteLineMessageTemplateById(Long templateId) {
        return lineMessageTemplateMapper.deleteLineMessageTemplateById(templateId);
    }

    @Override
    public int deleteLineMessageTemplateByIds(Long[] templateIds) {
        return lineMessageTemplateMapper.deleteLineMessageTemplateByIds(templateIds);
    }

    @Override
    public boolean checkTemplateCodeUnique(LineMessageTemplate lineMessageTemplate) {
        Long templateId = lineMessageTemplate.getTemplateId() == null ? -1L : lineMessageTemplate.getTemplateId();
        LineMessageTemplate info = lineMessageTemplateMapper.checkTemplateCodeUnique(lineMessageTemplate.getTemplateCode());
        if (info != null && !info.getTemplateId().equals(templateId)) {
            return false;
        }
        return true;
    }

    @Override
    public int incrementUseCount(Long templateId) {
        return lineMessageTemplateMapper.incrementUseCount(templateId);
    }

    @Override
    public String parseTemplateContent(Long templateId, String lineUserId) {
        LineMessageTemplate template = selectLineMessageTemplateById(templateId);
        if (template == null) {
            log.warn("找不到範本，ID: {}", templateId);
            return null;
        }

        // 建立變數 Map
        Map<String, String> variables = new HashMap<>();

        // 從 LINE 使用者取得變數
        if (StringUtils.isNotEmpty(lineUserId)) {
            LineUser lineUser = lineUserMapper.selectLineUserByLineUserId(lineUserId);
            if (lineUser != null) {
                variables.put("nickname", lineUser.getLineDisplayName());
                variables.put("displayName", lineUser.getLineDisplayName());
                variables.put("lineUserId", lineUser.getLineUserId());
                variables.put("pictureUrl", lineUser.getLinePictureUrl());
                variables.put("statusMessage", lineUser.getLineStatusMessage());
            }
        }

        // 增加使用次數
        incrementUseCount(templateId);

        return templateVariableEngine.parse(template.getContent(), variables);
    }

    @Override
    public String parseTemplateContent(String content, Map<String, String> variables) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        return templateVariableEngine.parse(content, variables);
    }

    @Override
    public List<Map<String, String>> getFlexPresetTemplates() {
        List<Map<String, String>> templates = new java.util.ArrayList<>();
        
        // 定義範本清單（與 cheng-admin/resources/line/FlexTemplate 目錄對應）
        // 注意：檔名有空格的要正確對應
        addTemplate(templates, "Restaurant", "餐廳介紹", "適用於餐廳資訊展示，包含評分、地址、電話等");
        addTemplate(templates, "Hotel", "飯店資訊", "適用於住宿資訊，包含價格、圖片展示");
        addTemplate(templates, "Real Estate", "房地產", "適用於房屋銷售或租賃資訊");
        addTemplate(templates, "Apparel", "服飾商品", "適用於服飾商品展示，包含價格、折扣");
        addTemplate(templates, "Local Search", "地點搜尋", "適用於多個地點的輪播展示");
        addTemplate(templates, "Menu", "餐點菜單", "適用於餐廳菜單和價格展示");
        addTemplate(templates, "Receipt", "收據帳單", "適用於購買明細和金額彙總");
        addTemplate(templates, "Shopping", "購物商品", "適用於商品輪播和購物車功能");
        addTemplate(templates, "Social", "社群貼文", "適用於社群媒體風格的圖文展示");
        addTemplate(templates, "Ticket", "票券資訊", "適用於電影票、活動票券展示");
        addTemplate(templates, "TodoAPP", "待辦事項", "適用於任務進度和清單展示");
        addTemplate(templates, "Transit", "交通路線", "適用於交通時刻和路線展示");
        
        return templates;
    }

    private void addTemplate(List<Map<String, String>> list, String name, String label, String description) {
        Map<String, String> template = new HashMap<>();
        template.put("name", name);
        template.put("label", label);
        template.put("description", description);
        list.add(template);
    }

    @Override
    public String getFlexPresetContent(String templateName) {
        try {
            // 嘗試多個可能的路徑
            String[] possiblePaths = {
                "line/FlexTemplate/" + templateName + ".json",
                "classpath:line/FlexTemplate/" + templateName + ".json"
            };
            
            for (String resourcePath : possiblePaths) {
                org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(
                    resourcePath.replace("classpath:", "")
                );
                
                if (resource.exists()) {
                    try (java.io.InputStream is = resource.getInputStream()) {
                        String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        log.info("成功載入 Flex 範本：{}", templateName);
                        return content;
                    }
                }
            }
            
            log.warn("找不到 Flex 範本：{}，已嘗試路徑：{}", templateName, String.join(", ", possiblePaths));
            return null;
        } catch (Exception e) {
            log.error("讀取 Flex 範本失敗：{}", templateName, e);
            return null;
        }
    }
}
