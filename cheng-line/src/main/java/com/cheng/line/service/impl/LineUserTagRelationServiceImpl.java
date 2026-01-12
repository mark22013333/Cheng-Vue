package com.cheng.line.service.impl;

import com.cheng.line.domain.LineUserTagRelation;
import com.cheng.line.mapper.LineUserMapper;
import com.cheng.line.mapper.LineUserTagRelationMapper;
import com.cheng.line.service.ILineUserTagRelationService;
import com.cheng.system.domain.SysTag;
import com.cheng.system.mapper.SysTagMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LINE 使用者標籤關聯 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineUserTagRelationServiceImpl implements ILineUserTagRelationService {

    private final LineUserTagRelationMapper lineUserTagRelationMapper;
    private final LineUserMapper lineUserMapper;
    private final SysTagMapper sysTagMapper;

    @Override
    public List<LineUserTagRelation> selectByLineUserId(String lineUserId) {
        return lineUserTagRelationMapper.selectByLineUserId(lineUserId);
    }

    @Override
    public List<LineUserTagRelation> selectByTagId(Long tagId) {
        return lineUserTagRelationMapper.selectByTagId(tagId);
    }

    @Override
    public List<LineUserTagRelation> selectLineUserTagRelationList(LineUserTagRelation relation) {
        return lineUserTagRelationMapper.selectLineUserTagRelationList(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int bindTag(String lineUserId, Long tagId, String createBy) {
        // 檢查是否已存在
        LineUserTagRelation existing = lineUserTagRelationMapper.checkRelationExists(lineUserId, tagId);
        if (existing != null) {
            log.debug("使用者 {} 已有標籤 {}，跳過", lineUserId, tagId);
            return 0;
        }

        LineUserTagRelation relation = new LineUserTagRelation();
        relation.setLineUserId(lineUserId);
        relation.setTagId(tagId);
        relation.setCreateBy(createBy);

        int result = lineUserTagRelationMapper.insertLineUserTagRelation(relation);
        log.info("為使用者 {} 貼標 {}，結果：{}", lineUserId, tagId, result > 0 ? "成功" : "失敗");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindTag(List<String> lineUserIds, Long tagId, String createBy) {
        if (lineUserIds == null || lineUserIds.isEmpty()) {
            return 0;
        }

        List<LineUserTagRelation> relations = new ArrayList<>();
        for (String lineUserId : lineUserIds) {
            // 檢查是否已存在（批次操作使用 INSERT IGNORE 處理）
            LineUserTagRelation relation = new LineUserTagRelation();
            relation.setLineUserId(lineUserId);
            relation.setTagId(tagId);
            relation.setCreateBy(createBy);
            relations.add(relation);
        }

        int result = lineUserTagRelationMapper.batchInsertLineUserTagRelation(relations);
        log.info("批次為 {} 個使用者貼標 {}，成功：{}", lineUserIds.size(), tagId, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindTags(String lineUserId, List<Long> tagIds, String createBy) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        List<LineUserTagRelation> relations = new ArrayList<>();
        for (Long tagId : tagIds) {
            LineUserTagRelation relation = new LineUserTagRelation();
            relation.setLineUserId(lineUserId);
            relation.setTagId(tagId);
            relation.setCreateBy(createBy);
            relations.add(relation);
        }

        int result = lineUserTagRelationMapper.batchInsertLineUserTagRelation(relations);
        log.info("為使用者 {} 批次貼 {} 個標籤，成功：{}", lineUserId, tagIds.size(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindTags(List<String> lineUserIds, List<Long> tagIds, String createBy) {
        if (lineUserIds == null || lineUserIds.isEmpty() || tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        List<LineUserTagRelation> relations = new ArrayList<>();
        for (String lineUserId : lineUserIds) {
            for (Long tagId : tagIds) {
                LineUserTagRelation relation = new LineUserTagRelation();
                relation.setLineUserId(lineUserId);
                relation.setTagId(tagId);
                relation.setCreateBy(createBy);
                relations.add(relation);
            }
        }

        int result = lineUserTagRelationMapper.batchInsertLineUserTagRelation(relations);
        log.info("批次為 {} 個使用者貼 {} 個標籤，成功：{}", lineUserIds.size(), tagIds.size(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindTag(String lineUserId, Long tagId) {
        int result = lineUserTagRelationMapper.deleteByUserIdAndTagId(lineUserId, tagId);
        log.info("移除使用者 {} 的標籤 {}，結果：{}", lineUserId, tagId, result > 0 ? "成功" : "無記錄");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindAllTags(String lineUserId) {
        int result = lineUserTagRelationMapper.deleteByLineUserId(lineUserId);
        log.info("移除使用者 {} 的所有標籤，共 {} 個", lineUserId, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindAllUsers(Long tagId) {
        int result = lineUserTagRelationMapper.deleteByTagId(tagId);
        log.info("移除標籤 {} 的所有使用者關聯，共 {} 個", tagId, result);
        return result;
    }

    @Override
    public int countUsersByTagId(Long tagId) {
        return lineUserTagRelationMapper.countUsersByTagId(tagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        return lineUserTagRelationMapper.deleteLineUserTagRelationByIds(ids);
    }

    @Override
    public List<LineUserTagRelation> selectByLineUserIds(List<String> lineUserIds) {
        if (lineUserIds == null || lineUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        return lineUserTagRelationMapper.selectByLineUserIds(lineUserIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchBindWithValidation(
            List<TagRecordDTO> records,
            List<Long> defaultTagIds,
            String createBy
    ) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;
        List<Map<String, String>> failedRecords = new ArrayList<>();
        Set<String> newTagsCreated = new HashSet<>();

        if (records == null || records.isEmpty()) {
            result.put("successCount", 0);
            result.put("failedCount", 0);
            result.put("failedRecords", failedRecords);
            result.put("newTagsCreated", new ArrayList<>());
            return result;
        }

        // 1. 批次驗證使用者是否存在
        List<String> allLineUserIds = records.stream()
                .map(TagRecordDTO::getLineUserId)
                .distinct()
                .toList();
        List<String> existingUserIdList = lineUserMapper.selectExistingLineUserIds(allLineUserIds);
        Set<String> existingUserIds = new HashSet<>(existingUserIdList != null ? existingUserIdList : Collections.emptyList());
        log.info("驗證使用者存在性：共 {} 個 ID，存在 {} 個", allLineUserIds.size(), existingUserIds.size());

        // 2. 收集所有額外標籤名稱，批次查詢或建立
        Set<String> extraTagNames = records.stream()
                .map(TagRecordDTO::getExtraTagName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(Collectors.toSet());

        Map<String, Long> tagNameToIdMap = new HashMap<>();
        if (!extraTagNames.isEmpty()) {
            // 批次查詢已存在的標籤
            List<SysTag> existingTags = sysTagMapper.selectByTagNames(new ArrayList<>(extraTagNames));
            for (SysTag tag : existingTags) {
                tagNameToIdMap.put(tag.getTagName(), tag.getTagId());
            }

            // 建立不存在的標籤
            for (String tagName : extraTagNames) {
                if (!tagNameToIdMap.containsKey(tagName)) {
                    SysTag newTag = new SysTag();
                    newTag.setTagName(tagName);
                    newTag.setTagCode("auto_line_" + System.currentTimeMillis() + "_" + Math.abs(tagName.hashCode()));
                    newTag.setTagColor(generateRandomColor());
                    newTag.setPlatformScope(SysTag.SCOPE_LINE);
                    newTag.setStatus(1);
                    newTag.setCreateBy(createBy);
                    sysTagMapper.insertSysTag(newTag);
                    tagNameToIdMap.put(tagName, newTag.getTagId());
                    newTagsCreated.add(tagName);
                    log.info("自動建立標籤：{} -> {}", tagName, newTag.getTagId());
                }
            }
        }

        // 3. 批次處理貼標
        List<LineUserTagRelation> relationsToInsert = new ArrayList<>();

        for (TagRecordDTO record : records) {
            String lineUserId = record.getLineUserId();

            // 檢查使用者是否存在
            if (!existingUserIds.contains(lineUserId)) {
                failedCount++;
                if (failedRecords.size() < 100) {
                    Map<String, String> failedRecord = new HashMap<>();
                    failedRecord.put("lineUserId", lineUserId);
                    failedRecord.put("reason", "使用者不存在");
                    failedRecords.add(failedRecord);
                }
                continue;
            }

            // 決定要貼的標籤
            List<Long> tagIdsToApply;
            String extraTagName = record.getExtraTagName();
            if (extraTagName != null && !extraTagName.trim().isEmpty() && tagNameToIdMap.containsKey(extraTagName)) {
                tagIdsToApply = List.of(tagNameToIdMap.get(extraTagName));
            } else {
                tagIdsToApply = defaultTagIds;
            }

            // 建立關聯記錄
            for (Long tagId : tagIdsToApply) {
                LineUserTagRelation relation = new LineUserTagRelation();
                relation.setLineUserId(lineUserId);
                relation.setTagId(tagId);
                relation.setCreateBy(createBy);
                relationsToInsert.add(relation);
            }
            successCount++;
        }

        // 4. 批次插入關聯（忽略重複）
        if (!relationsToInsert.isEmpty()) {
            int inserted = lineUserTagRelationMapper.batchInsertIgnore(relationsToInsert);
            log.info("批次插入 {} 筆關聯，實際插入 {}", relationsToInsert.size(), inserted);
        }

        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("failedRecords", failedRecords);
        result.put("newTagsCreated", new ArrayList<>(newTagsCreated));
        return result;
    }

    private String generateRandomColor() {
        String[] colors = {"#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399", "#00CED1", "#FF6347", "#9370DB"};
        return colors[new Random().nextInt(colors.length)];
    }

    /**
     * 貼標記錄 DTO
     */
    @Data
    @AllArgsConstructor
    public static class TagRecordDTO {
        private String lineUserId;
        private String extraTagName;
    }
}
