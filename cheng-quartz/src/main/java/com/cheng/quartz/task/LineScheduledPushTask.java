package com.cheng.quartz.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.line.domain.LineUserTagRelation;
import com.cheng.line.dto.SendMessageDTO;
import com.cheng.line.service.ILineMessageSendService;
import com.cheng.line.service.ILineUserTagRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LINE 標籤排程推播任務
 * <p>
 * 用於 Quartz 排程系統，根據標籤或標籤群組篩選目標使用者，
 * 使用指定的訊息範本執行逐人 LINE PUSH 推播。
 * <p>
 * <b>核心特色：</b>
 * <ul>
 *   <li>支援透過 tagIds 指定多個標籤（取聯集）</li>
 *   <li>支援透過 tagGroupIds 指定標籤群組（由群組運算邏輯解析）</li>
 *   <li>使用範本發送訊息，支援排程一次性或重複推播</li>
 * </ul>
 * <p>
 * <b>Quartz 設定範例：</b>
 * <pre>
 * Bean名稱: lineScheduledPushTask
 * 方法名稱: execute
 * 參數: {"configId":1,"templateId":5,"tagIds":"1,2,3"}
 * 參數: {"configId":1,"templateId":5,"tagGroupIds":"1"}
 * </pre>
 *
 * @author Cheng
 * @since 2026-03-28
 */
@Slf4j
@Component("lineScheduledPushTask")
public class LineScheduledPushTask {

    @Resource
    private ILineMessageSendService lineMessageSendService;

    @Resource
    private ILineUserTagRelationService lineUserTagRelationService;

    /**
     * 執行標籤推播任務
     * <p>
     * 解析 JSON 參數，根據標籤篩選目標使用者，逐人發送 LINE 推播訊息。
     *
     * @param paramsJson 參數的 JSON 字串，包含 configId, templateId, tagIds, tagGroupIds
     */
    public void execute(String paramsJson) {
        log.info("開始執行 LINE 標籤推播任務，參數: {}", paramsJson);

        try {
            JSONObject params = JSON.parseObject(paramsJson);

            // 解析必填參數
            Integer configId = params.getInteger("configId");
            Long templateId = params.getLong("templateId");

            if (configId == null) {
                log.error("LINE 標籤推播任務缺少必填參數: configId");
                return;
            }
            if (templateId == null) {
                log.error("LINE 標籤推播任務缺少必填參數: templateId");
                return;
            }

            // 解析選填參數
            String tagIdsStr = params.getString("tagIds");
            String tagGroupIdsStr = params.getString("tagGroupIds");

            if (isBlank(tagIdsStr) && isBlank(tagGroupIdsStr)) {
                log.error("LINE 標籤推播任務至少需要指定 tagIds 或 tagGroupIds");
                return;
            }

            // 解析標籤目標使用者
            Set<String> targetLineUserIds = resolveTargetUsers(tagIdsStr, tagGroupIdsStr);

            if (targetLineUserIds.isEmpty()) {
                log.warn("LINE 標籤推播任務未找到符合條件的目標使用者，configId={}, templateId={}, tagIds={}, tagGroupIds={}",
                        configId, templateId, tagIdsStr, tagGroupIdsStr);
                return;
            }

            log.info("LINE 標籤推播任務解析完成，目標使用者數: {}", targetLineUserIds.size());

            // 建構 SendMessageDTO 並使用範本發送
            SendMessageDTO dto = new SendMessageDTO();
            dto.setConfigId(configId);
            dto.setMessageType("PUSH");
            dto.setTemplateId(templateId);
            dto.setTargetLineUserIds(new ArrayList<>(targetLineUserIds));

            Long messageId = lineMessageSendService.sendTemplateMessage(dto);

            log.info("LINE 標籤推播任務執行完成，訊息記錄ID: {}，目標使用者數: {}",
                    messageId, targetLineUserIds.size());

        } catch (Exception e) {
            log.error("LINE 標籤推播任務執行失敗", e);
        }
    }

    /**
     * 解析標籤和標籤群組，取得目標使用者 LINE User ID 集合
     *
     * @param tagIdsStr      標籤 ID（逗號分隔），可為 null
     * @param tagGroupIdsStr 標籤群組 ID（逗號分隔），可為 null
     * @return 去重後的目標使用者 LINE User ID 集合
     */
    private Set<String> resolveTargetUsers(String tagIdsStr, String tagGroupIdsStr) {
        Set<String> targetUserIds = new HashSet<>();

        // 解析標籤 ID 對應的使用者
        if (!isBlank(tagIdsStr)) {
            List<Long> tagIds = parseCommaSeparatedIds(tagIdsStr);
            for (Long tagId : tagIds) {
                List<LineUserTagRelation> relations = lineUserTagRelationService.selectByTagId(tagId);
                for (LineUserTagRelation relation : relations) {
                    targetUserIds.add(relation.getLineUserId());
                }
            }
            log.info("透過標籤 {} 解析到 {} 位使用者", tagIdsStr, targetUserIds.size());
        }

        // 解析標籤群組 ID 對應的使用者
        // 注意：標籤群組的 AND/OR 邏輯由 TagGroupCalcService 處理，
        // 此處暫時以群組內的所有標籤取聯集方式查詢
        if (!isBlank(tagGroupIdsStr)) {
            List<Long> tagGroupIds = parseCommaSeparatedIds(tagGroupIdsStr);
            log.info("標籤群組 {} 的使用者解析將由 TagGroupCalcService 處理（待後續整合）", tagGroupIdsStr);
            // TODO: 整合 TagGroupCalcService 解析群組內使用者
            // 目前 TagGroupCalcService 在 cheng-system 模組，
            // 其 executeLineGroupCalc() 只更新計數，不回傳使用者列表。
            // 後續 Task 會擴充此功能。
        }

        return targetUserIds;
    }

    /**
     * 解析逗號分隔的 ID 字串為 Long 列表
     *
     * @param idsStr 逗號分隔的 ID 字串
     * @return Long 列表
     */
    private List<Long> parseCommaSeparatedIds(String idsStr) {
        return Arrays.stream(idsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 檢查字串是否為空白
     *
     * @param str 字串
     * @return true 如果為 null 或空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
