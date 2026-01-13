package com.cheng.system.service.impl;

import com.cheng.system.domain.InvItemTagRelation;
import com.cheng.system.domain.SysTag;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.mapper.InvItemTagRelationMapper;
import com.cheng.system.mapper.SysTagMapper;
import com.cheng.system.service.IInvItemTagRelationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 庫存物品標籤關聯 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvItemTagRelationServiceImpl implements IInvItemTagRelationService {

    private final InvItemTagRelationMapper invItemTagRelationMapper;
    private final InvItemMapper invItemMapper;
    private final SysTagMapper sysTagMapper;

    @Override
    public List<InvItemTagRelation> selectByItemId(Long itemId) {
        return invItemTagRelationMapper.selectByItemId(itemId);
    }

    @Override
    public List<InvItemTagRelation> selectByTagId(Long tagId) {
        return invItemTagRelationMapper.selectByTagId(tagId);
    }

    @Override
    public List<InvItemTagRelation> selectInvItemTagRelationList(InvItemTagRelation relation) {
        return invItemTagRelationMapper.selectInvItemTagRelationList(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int bindTag(Long itemId, Long tagId, String createBy) {
        // 檢查是否已存在
        InvItemTagRelation existing = invItemTagRelationMapper.checkRelationExists(itemId, tagId);
        if (existing != null) {
            log.debug("物品 {} 已有標籤 {}，跳過", itemId, tagId);
            return 0;
        }

        InvItemTagRelation relation = new InvItemTagRelation();
        relation.setItemId(itemId);
        relation.setTagId(tagId);
        relation.setCreateBy(createBy);

        int result = invItemTagRelationMapper.insertInvItemTagRelation(relation);
        if (result > 0) {
            // 更新標籤的物品計數
            updateTagItemCount(tagId);
        }
        log.info("為物品 {} 貼標 {}，結果：{}", itemId, tagId, result > 0 ? "成功" : "失敗");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindTag(List<Long> itemIds, Long tagId, String createBy) {
        if (itemIds == null || itemIds.isEmpty()) {
            return 0;
        }

        List<InvItemTagRelation> relations = new ArrayList<>();
        for (Long itemId : itemIds) {
            InvItemTagRelation relation = new InvItemTagRelation();
            relation.setItemId(itemId);
            relation.setTagId(tagId);
            relation.setCreateBy(createBy);
            relations.add(relation);
        }

        int result = invItemTagRelationMapper.batchInsertInvItemTagRelation(relations);
        if (result > 0) {
            updateTagItemCount(tagId);
        }
        log.info("批次為 {} 個物品貼標 {}，成功：{}", itemIds.size(), tagId, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindTags(Long itemId, List<Long> tagIds, String createBy) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }

        List<InvItemTagRelation> relations = new ArrayList<>();
        for (Long tagId : tagIds) {
            InvItemTagRelation relation = new InvItemTagRelation();
            relation.setItemId(itemId);
            relation.setTagId(tagId);
            relation.setCreateBy(createBy);
            relations.add(relation);
        }

        int result = invItemTagRelationMapper.batchInsertInvItemTagRelation(relations);
        if (result > 0) {
            // 更新所有相關標籤的物品計數
            for (Long tagId : tagIds) {
                updateTagItemCount(tagId);
            }
        }
        log.info("為物品 {} 批次貼 {} 個標籤，成功：{}", itemId, tagIds.size(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindTag(Long itemId, Long tagId) {
        int result = invItemTagRelationMapper.deleteByItemIdAndTagId(itemId, tagId);
        if (result > 0) {
            updateTagItemCount(tagId);
        }
        log.info("移除物品 {} 的標籤 {}，結果：{}", itemId, tagId, result > 0 ? "成功" : "無記錄");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindAllTags(Long itemId) {
        // 先取得物品的所有標籤，用於後續更新計數
        List<InvItemTagRelation> relations = invItemTagRelationMapper.selectByItemId(itemId);
        int result = invItemTagRelationMapper.deleteByItemId(itemId);
        if (result > 0) {
            // 更新所有相關標籤的物品計數
            for (InvItemTagRelation relation : relations) {
                updateTagItemCount(relation.getTagId());
            }
        }
        log.info("移除物品 {} 的所有標籤，共 {} 個", itemId, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindAllItems(Long tagId) {
        int result = invItemTagRelationMapper.deleteByTagId(tagId);
        if (result > 0) {
            updateTagItemCount(tagId);
        }
        log.info("移除標籤 {} 的所有物品關聯，共 {} 個", tagId, result);
        return result;
    }

    @Override
    public int countItemsByTagId(Long tagId) {
        return invItemTagRelationMapper.countItemsByTagId(tagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        return invItemTagRelationMapper.deleteInvItemTagRelationByIds(ids);
    }

    /**
     * 更新標籤的物品計數
     */
    private void updateTagItemCount(Long tagId) {
        int count = invItemTagRelationMapper.countItemsByTagId(tagId);
        sysTagMapper.updateItemCount(tagId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public <T> Map<String, Object> batchBindWithValidation(List<T> records, List<Long> defaultTagIds, String createBy) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;
        List<Map<String, String>> failedRecords = new ArrayList<>();
        Set<String> newTagsCreated = new HashSet<>();

        if (records == null || records.isEmpty()) {
            result.put("successCount", successCount);
            result.put("failedCount", failedCount);
            result.put("failedRecords", failedRecords);
            result.put("newTagsCreated", new ArrayList<>());
            return result;
        }

        // 轉換記錄為統一格式
        List<ItemTagRecordDTO> dtoRecords = new ArrayList<>();
        for (T record : records) {
            if (record instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) record;
                ItemTagRecordDTO dto = new ItemTagRecordDTO();
                dto.setItemCode(String.valueOf(map.get("itemCode")));
                dto.setExtraTagName(map.get("extraTagName") != null ? String.valueOf(map.get("extraTagName")) : null);
                dtoRecords.add(dto);
            }
        }

        // 1. 批次驗證物品是否存在
        List<String> allItemCodes = dtoRecords.stream()
                .map(ItemTagRecordDTO::getItemCode)
                .distinct()
                .toList();
        List<Map<String, Object>> existingItems = invItemMapper.selectExistingItemCodes(allItemCodes);
        Map<String, Long> itemCodeToIdMap = new HashMap<>();
        for (Map<String, Object> item : existingItems) {
            itemCodeToIdMap.put(String.valueOf(item.get("item_code")), ((Number) item.get("item_id")).longValue());
        }
        log.info("驗證物品存在性：共 {} 個編碼，存在 {} 個", allItemCodes.size(), itemCodeToIdMap.size());

        // 2. 收集所有額外標籤名稱，批次查詢或建立
        Set<String> extraTagNames = dtoRecords.stream()
                .map(ItemTagRecordDTO::getExtraTagName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(java.util.stream.Collectors.toSet());

        Map<String, Long> tagNameToIdMap = new HashMap<>();
        if (!extraTagNames.isEmpty()) {
            List<SysTag> existingTags = sysTagMapper.selectByTagNames(new ArrayList<>(extraTagNames));
            for (SysTag tag : existingTags) {
                tagNameToIdMap.put(tag.getTagName(), tag.getTagId());
            }

            for (String tagName : extraTagNames) {
                if (!tagNameToIdMap.containsKey(tagName)) {
                    SysTag newTag = new SysTag();
                    newTag.setTagName(tagName);
                    newTag.setTagCode("auto_inv_" + System.currentTimeMillis() + "_" + Math.abs(tagName.hashCode()));
                    newTag.setTagColor(generateRandomColor());
                    newTag.setPlatformScope(SysTag.SCOPE_INVENTORY);
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
        List<InvItemTagRelation> relationsToInsert = new ArrayList<>();

        for (ItemTagRecordDTO record : dtoRecords) {
            String itemCode = record.getItemCode();

            if (!itemCodeToIdMap.containsKey(itemCode)) {
                failedCount++;
                if (failedRecords.size() < 100) {
                    Map<String, String> failedRecord = new HashMap<>();
                    failedRecord.put("itemCode", itemCode);
                    failedRecord.put("reason", "物品不存在");
                    failedRecords.add(failedRecord);
                }
                continue;
            }

            Long itemId = itemCodeToIdMap.get(itemCode);

            List<Long> tagIdsToApply;
            String extraTagName = record.getExtraTagName();
            if (extraTagName != null && !extraTagName.trim().isEmpty() && tagNameToIdMap.containsKey(extraTagName)) {
                tagIdsToApply = List.of(tagNameToIdMap.get(extraTagName));
            } else {
                tagIdsToApply = defaultTagIds;
            }

            for (Long tagId : tagIdsToApply) {
                InvItemTagRelation relation = new InvItemTagRelation();
                relation.setItemId(itemId);
                relation.setTagId(tagId);
                relation.setCreateBy(createBy);
                relationsToInsert.add(relation);
            }
            successCount++;
        }

        // 4. 批次插入關聯（忽略重複）
        if (!relationsToInsert.isEmpty()) {
            int inserted = invItemTagRelationMapper.batchInsertIgnore(relationsToInsert);
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

    @Data
    private static class ItemTagRecordDTO {
        private String itemCode;
        private String extraTagName;
    }
}
