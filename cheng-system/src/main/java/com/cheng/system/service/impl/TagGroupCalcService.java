package com.cheng.system.service.impl;

import com.cheng.system.domain.SysTagGroup;
import com.cheng.system.domain.SysTagGroupDetail;
import com.cheng.system.mapper.SysTagGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 標籤群組運算服務
 * 支援大量資料的 Keyset Pagination + Safety Cutoff 策略
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagGroupCalcService {

    private final JdbcTemplate jdbcTemplate;
    private final SysTagGroupMapper sysTagGroupMapper;

    private static final int PAGE_SIZE = 10000;

    /**
     * 執行 LINE 群組運算
     */
    public Map<String, Object> executeLineGroupCalc(SysTagGroup group) {
        long startTime = System.currentTimeMillis();
        log.info("開始執行 LINE 標籤群組運算：{}", group.getGroupName());

        List<SysTagGroupDetail> details = group.getDetails();
        if (details == null || details.isEmpty()) {
            return buildResult(0, 0, "群組沒有設定標籤");
        }

        // 提取規則中的 tag_id
        Set<Long> ruleTagIds = details.stream()
                .map(SysTagGroupDetail::getTagId)
                .collect(Collectors.toSet());

        String calcMode = group.getCalcMode() == null || group.getCalcMode().isEmpty()
                ? SysTagGroup.CALC_MODE_LEFT_TO_RIGHT : group.getCalcMode();

        int matchCount = 0;
        String lastUserId = null;

        // Keyset Pagination 迴圈
        while (true) {
            List<Map<String, Object>> pageData = fetchLineUserTagPage(ruleTagIds, lastUserId, PAGE_SIZE + 100);

            if (pageData.isEmpty()) {
                break;
            }

            // Safety Cutoff：避免切斷同一使用者
            pageData = applySafetyCutoff(pageData, "line_user_id", PAGE_SIZE);

            if (pageData.isEmpty()) {
                break;
            }

            // 按 line_user_id 分組
            Map<String, Set<Long>> userTagMap = groupByUser(pageData, "line_user_id");

            // 運算符合群組的使用者
            for (Map.Entry<String, Set<Long>> entry : userTagMap.entrySet()) {
                if (isGroupMatched(entry.getValue(), details, calcMode)) {
                    matchCount++;
                }
            }

            // 更新 lastUserId
            lastUserId = (String) pageData.get(pageData.size() - 1).get("line_user_id");

            if (pageData.size() < PAGE_SIZE) {
                break;
            }
        }

        // 更新群組的結果數量
        sysTagGroupMapper.updateCountResult(group.getGroupId(), matchCount);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("LINE 標籤群組運算完成：{}，符合人數：{}，耗時：{}ms", group.getGroupName(), matchCount, elapsed);

        return buildResult(matchCount, elapsed, null);
    }

    /**
     * 執行庫存群組運算
     */
    public Map<String, Object> executeInventoryGroupCalc(SysTagGroup group) {
        long startTime = System.currentTimeMillis();
        log.info("開始執行庫存標籤群組運算：{}", group.getGroupName());

        List<SysTagGroupDetail> details = group.getDetails();
        if (details == null || details.isEmpty()) {
            return buildResult(0, 0, "群組沒有設定標籤");
        }

        Set<Long> ruleTagIds = details.stream()
                .map(SysTagGroupDetail::getTagId)
                .collect(Collectors.toSet());

        String calcMode = group.getCalcMode() == null || group.getCalcMode().isEmpty()
                ? SysTagGroup.CALC_MODE_LEFT_TO_RIGHT : group.getCalcMode();

        int matchCount = 0;
        Long lastItemId = null;

        while (true) {
            List<Map<String, Object>> pageData = fetchInvItemTagPage(ruleTagIds, lastItemId, PAGE_SIZE + 100);

            if (pageData.isEmpty()) {
                break;
            }

            pageData = applySafetyCutoffLong(pageData, "item_id", PAGE_SIZE);

            if (pageData.isEmpty()) {
                break;
            }

            Map<Long, Set<Long>> itemTagMap = groupByItemId(pageData);

            for (Map.Entry<Long, Set<Long>> entry : itemTagMap.entrySet()) {
                if (isGroupMatched(entry.getValue(), details, calcMode)) {
                    matchCount++;
                }
            }

            lastItemId = (Long) pageData.get(pageData.size() - 1).get("item_id");

            if (pageData.size() < PAGE_SIZE) {
                break;
            }
        }

        sysTagGroupMapper.updateCountResult(group.getGroupId(), matchCount);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("庫存標籤群組運算完成：{}，符合物品數：{}，耗時：{}ms", group.getGroupName(), matchCount, elapsed);

        return buildResult(matchCount, elapsed, null);
    }

    /**
     * 預覽群組運算結果
     */
    public Map<String, Object> previewGroupCalc(SysTagGroup group, int limit) {
        List<SysTagGroupDetail> details = group.getDetails();
        if (details == null || details.isEmpty()) {
            return buildPreviewResult(Collections.emptyList(), 0, "群組沒有設定標籤");
        }

        Set<Long> ruleTagIds = details.stream()
                .map(SysTagGroupDetail::getTagId)
                .collect(Collectors.toSet());

        String calcMode = group.getCalcMode() == null || group.getCalcMode().isEmpty()
                ? SysTagGroup.CALC_MODE_LEFT_TO_RIGHT : group.getCalcMode();

        List<String> matchedIds = new ArrayList<>();

        if (SysTagGroup.SCOPE_LINE.equals(group.getPlatformScope())) {
            List<Map<String, Object>> pageData = fetchLineUserTagPage(ruleTagIds, null, limit * 10);
            Map<String, Set<Long>> userTagMap = groupByUser(pageData, "line_user_id");

            for (Map.Entry<String, Set<Long>> entry : userTagMap.entrySet()) {
                if (isGroupMatched(entry.getValue(), details, calcMode)) {
                    matchedIds.add(entry.getKey());
                    if (matchedIds.size() >= limit) break;
                }
            }
        } else {
            List<Map<String, Object>> pageData = fetchInvItemTagPage(ruleTagIds, null, limit * 10);
            Map<Long, Set<Long>> itemTagMap = groupByItemId(pageData);

            for (Map.Entry<Long, Set<Long>> entry : itemTagMap.entrySet()) {
                if (isGroupMatched(entry.getValue(), details, calcMode)) {
                    matchedIds.add(String.valueOf(entry.getKey()));
                    if (matchedIds.size() >= limit) break;
                }
            }
        }

        return buildPreviewResult(matchedIds, matchedIds.size(), null);
    }

    /**
     * 判斷使用者/物品是否符合群組規則
     */
    private boolean isGroupMatched(Set<Long> userTagIds, List<SysTagGroupDetail> details, String calcMode) {
        if (SysTagGroup.CALC_MODE_OR_OF_AND.equals(calcMode)) {
            return isGroupMatchedOrOfAnd(userTagIds, details);
        } else {
            return isGroupMatchedLeftToRight(userTagIds, details);
        }
    }

    /**
     * LEFT_TO_RIGHT 模式：從左到右依序運算
     * 範例：A AND B OR C AND D → ((((A AND B) OR C) AND D))
     */
    private boolean isGroupMatchedLeftToRight(Set<Long> userTagIds, List<SysTagGroupDetail> details) {
        List<SysTagGroupDetail> sorted = details.stream()
                .sorted(Comparator.comparingInt(SysTagGroupDetail::getGroupIndex))
                .toList();

        boolean result = userTagIds.contains(sorted.get(0).getTagId());

        for (int i = 1; i < sorted.size(); i++) {
            SysTagGroupDetail detail = sorted.get(i);
            boolean hasTag = userTagIds.contains(detail.getTagId());

            if (SysTagGroupDetail.OPERATOR_AND.equals(detail.getOperator())) {
                result = result && hasTag;
            } else {
                result = result || hasTag;
            }
        }

        return result;
    }

    /**
     * OR_OF_AND 模式：以 OR 切段，段內為 AND
     * 範例：A AND B OR C AND D → (A AND B) OR (C AND D)
     */
    private boolean isGroupMatchedOrOfAnd(Set<Long> userTagIds, List<SysTagGroupDetail> details) {
        List<SysTagGroupDetail> sorted = details.stream()
                .sorted(Comparator.comparingInt(SysTagGroupDetail::getGroupIndex))
                .toList();

        // 依 OR 切段
        List<List<Long>> andGroups = new ArrayList<>();
        List<Long> currentGroup = new ArrayList<>();

        for (SysTagGroupDetail detail : sorted) {
            if (SysTagGroupDetail.OPERATOR_OR.equals(detail.getOperator()) && !currentGroup.isEmpty()) {
                andGroups.add(currentGroup);
                currentGroup = new ArrayList<>();
            }
            currentGroup.add(detail.getTagId());
        }
        if (!currentGroup.isEmpty()) {
            andGroups.add(currentGroup);
        }

        // 任一群組全部符合即為 true
        for (List<Long> group : andGroups) {
            boolean allMatch = group.stream().allMatch(userTagIds::contains);
            if (allMatch) {
                return true;
            }
        }

        return false;
    }

    /**
     * 分頁查詢 LINE 使用者標籤（Keyset Pagination）
     */
    private List<Map<String, Object>> fetchLineUserTagPage(Set<Long> tagIds, String lastUserId, int limit) {
        String tagIdList = tagIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = "SELECT line_user_id, tag_id FROM line_user_tag_relation " +
                "WHERE tag_id IN (" + tagIdList + ") " +
                (lastUserId != null ? "AND line_user_id > ? " : "") +
                "ORDER BY line_user_id LIMIT ?";

        if (lastUserId != null) {
            return jdbcTemplate.queryForList(sql, lastUserId, limit);
        } else {
            return jdbcTemplate.queryForList(sql, limit);
        }
    }

    /**
     * 分頁查詢庫存物品標籤（Keyset Pagination）
     */
    private List<Map<String, Object>> fetchInvItemTagPage(Set<Long> tagIds, Long lastItemId, int limit) {
        String tagIdList = tagIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = "SELECT item_id, tag_id FROM inv_item_tag_relation " +
                "WHERE tag_id IN (" + tagIdList + ") " +
                (lastItemId != null ? "AND item_id > ? " : "") +
                "ORDER BY item_id LIMIT ?";

        if (lastItemId != null) {
            return jdbcTemplate.queryForList(sql, lastItemId, limit);
        } else {
            return jdbcTemplate.queryForList(sql, limit);
        }
    }

    /**
     * Safety Cutoff：確保同一 ID 不被切斷（String 版）
     */
    private List<Map<String, Object>> applySafetyCutoff(List<Map<String, Object>> data, String idField, int targetSize) {
        if (data.size() <= targetSize) {
            return data;
        }

        String lastId = (String) data.get(targetSize - 1).get(idField);
        int cutoffIndex = targetSize;

        for (int i = targetSize; i < data.size(); i++) {
            if (!lastId.equals(data.get(i).get(idField))) {
                break;
            }
            cutoffIndex = i + 1;
        }

        return data.subList(0, cutoffIndex);
    }

    /**
     * Safety Cutoff：確保同一 ID 不被切斷（Long 版）
     */
    private List<Map<String, Object>> applySafetyCutoffLong(List<Map<String, Object>> data, String idField, int targetSize) {
        if (data.size() <= targetSize) {
            return data;
        }

        Long lastId = (Long) data.get(targetSize - 1).get(idField);
        int cutoffIndex = targetSize;

        for (int i = targetSize; i < data.size(); i++) {
            if (!lastId.equals(data.get(i).get(idField))) {
                break;
            }
            cutoffIndex = i + 1;
        }

        return data.subList(0, cutoffIndex);
    }

    /**
     * 按使用者分組
     */
    private Map<String, Set<Long>> groupByUser(List<Map<String, Object>> data, String idField) {
        Map<String, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : data) {
            String userId = (String) row.get(idField);
            Long tagId = ((Number) row.get("tag_id")).longValue();
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(tagId);
        }
        return result;
    }

    /**
     * 按物品分組
     */
    private Map<Long, Set<Long>> groupByItemId(List<Map<String, Object>> data) {
        Map<Long, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : data) {
            Long itemId = ((Number) row.get("item_id")).longValue();
            Long tagId = ((Number) row.get("tag_id")).longValue();
            result.computeIfAbsent(itemId, k -> new HashSet<>()).add(tagId);
        }
        return result;
    }

    private Map<String, Object> buildResult(int count, long elapsed, String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("matchCount", count);
        result.put("elapsedMs", elapsed);
        result.put("success", error == null);
        if (error != null) {
            result.put("error", error);
        }
        return result;
    }

    private Map<String, Object> buildPreviewResult(List<String> matchedIds, int count, String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("matchedIds", matchedIds);
        result.put("matchCount", count);
        result.put("success", error == null);
        if (error != null) {
            result.put("error", error);
        }
        return result;
    }
}
