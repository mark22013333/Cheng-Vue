package com.cheng.system.service;

import com.cheng.system.domain.InvItemTagRelation;

import java.util.List;
import java.util.Map;

/**
 * 庫存物品標籤關聯 Service 介面
 *
 * @author cheng
 */
public interface IInvItemTagRelationService {

    /**
     * 查詢指定物品的標籤關聯列表
     *
     * @param itemId 物品ID
     * @return 關聯集合
     */
    List<InvItemTagRelation> selectByItemId(Long itemId);

    /**
     * 查詢指定標籤的物品關聯列表
     *
     * @param tagId 標籤ID
     * @return 關聯集合
     */
    List<InvItemTagRelation> selectByTagId(Long tagId);

    /**
     * 查詢關聯列表（支援分頁和篩選）
     *
     * @param relation 查詢條件
     * @return 關聯集合
     */
    List<InvItemTagRelation> selectInvItemTagRelationList(InvItemTagRelation relation);

    /**
     * 為物品貼標（單一物品，單一標籤）
     *
     * @param itemId   物品ID
     * @param tagId    標籤ID
     * @param createBy 建立者
     * @return 結果：1=成功，0=已存在
     */
    int bindTag(Long itemId, Long tagId, String createBy);

    /**
     * 批次為物品貼標（多物品，單一標籤）
     *
     * @param itemIds  物品ID 列表
     * @param tagId    標籤ID
     * @param createBy 建立者
     * @return 成功貼標數量
     */
    int batchBindTag(List<Long> itemIds, Long tagId, String createBy);

    /**
     * 批次為物品貼標（單一物品，多標籤）
     *
     * @param itemId   物品ID
     * @param tagIds   標籤ID 列表
     * @param createBy 建立者
     * @return 成功貼標數量
     */
    int batchBindTags(Long itemId, List<Long> tagIds, String createBy);

    /**
     * 移除物品標籤
     *
     * @param itemId 物品ID
     * @param tagId  標籤ID
     * @return 結果
     */
    int unbindTag(Long itemId, Long tagId);

    /**
     * 移除物品的所有標籤
     *
     * @param itemId 物品ID
     * @return 結果
     */
    int unbindAllTags(Long itemId);

    /**
     * 移除標籤的所有物品關聯
     *
     * @param tagId 標籤ID
     * @return 結果
     */
    int unbindAllItems(Long tagId);

    /**
     * 統計標籤的物品數量
     *
     * @param tagId 標籤ID
     * @return 物品數量
     */
    int countItemsByTagId(Long tagId);

    /**
     * 批次刪除關聯
     *
     * @param ids 關聯ID陣列
     * @return 結果
     */
    int deleteByIds(Long[] ids);

    /**
     * 批次貼標（含驗證和額外標籤支援）
     *
     * @param records       貼標記錄列表
     * @param defaultTagIds 預設標籤 ID 列表
     * @param createBy      建立者
     * @return 結果（包含 successCount、failedCount、failedRecords、newTagsCreated）
     */
    <T> Map<String, Object> batchBindWithValidation(List<T> records, List<Long> defaultTagIds, String createBy);
}
