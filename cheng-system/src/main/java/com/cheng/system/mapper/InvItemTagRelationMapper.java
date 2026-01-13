package com.cheng.system.mapper;

import com.cheng.system.domain.InvItemTagRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 庫存物品標籤關聯 Mapper 介面
 *
 * @author cheng
 */
public interface InvItemTagRelationMapper {

    /**
     * 查詢關聯
     *
     * @param id 關聯ID
     * @return 關聯
     */
    InvItemTagRelation selectInvItemTagRelationById(Long id);

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
     * 檢查關聯是否已存在
     *
     * @param itemId 物品ID
     * @param tagId  標籤ID
     * @return 關聯
     */
    InvItemTagRelation checkRelationExists(@Param("itemId") Long itemId, @Param("tagId") Long tagId);

    /**
     * 新增關聯
     *
     * @param relation 關聯
     * @return 結果
     */
    int insertInvItemTagRelation(InvItemTagRelation relation);

    /**
     * 批次新增關聯
     *
     * @param relations 關聯列表
     * @return 結果
     */
    int batchInsertInvItemTagRelation(List<InvItemTagRelation> relations);

    /**
     * 刪除關聯
     *
     * @param id 關聯ID
     * @return 結果
     */
    int deleteInvItemTagRelationById(Long id);

    /**
     * 依物品和標籤刪除關聯
     *
     * @param itemId 物品ID
     * @param tagId  標籤ID
     * @return 結果
     */
    int deleteByItemIdAndTagId(@Param("itemId") Long itemId, @Param("tagId") Long tagId);

    /**
     * 刪除指定物品的所有標籤關聯
     *
     * @param itemId 物品ID
     * @return 結果
     */
    int deleteByItemId(Long itemId);

    /**
     * 刪除指定標籤的所有物品關聯
     *
     * @param tagId 標籤ID
     * @return 結果
     */
    int deleteByTagId(Long tagId);

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
    int deleteInvItemTagRelationByIds(Long[] ids);

    /**
     * 批次查詢多個物品的標籤關聯
     *
     * @param itemIds 物品 ID 列表
     * @return 關聯集合
     */
    List<InvItemTagRelation> selectByItemIds(@Param("itemIds") List<Long> itemIds);

    /**
     * 批次插入關聯（忽略重複）
     *
     * @param relations 關聯列表
     * @return 插入數量
     */
    int batchInsertIgnore(@Param("relations") List<InvItemTagRelation> relations);
}
