package com.cheng.line.service;

import com.cheng.line.domain.LineUserTagRelation;

import java.util.List;
import java.util.Map;

/**
 * LINE 使用者標籤關聯 Service 介面
 *
 * @author cheng
 */
public interface ILineUserTagRelationService {

    /**
     * 查詢指定使用者的標籤關聯列表
     *
     * @param lineUserId LINE 使用者 ID
     * @return 關聯集合
     */
    List<LineUserTagRelation> selectByLineUserId(String lineUserId);

    /**
     * 查詢指定標籤的使用者關聯列表
     *
     * @param tagId 標籤ID
     * @return 關聯集合
     */
    List<LineUserTagRelation> selectByTagId(Long tagId);

    /**
     * 查詢關聯列表（支援分頁和篩選）
     *
     * @param relation 查詢條件
     * @return 關聯集合
     */
    List<LineUserTagRelation> selectLineUserTagRelationList(LineUserTagRelation relation);

    /**
     * 為使用者貼標（單一使用者，單一標籤）
     *
     * @param lineUserId LINE 使用者 ID
     * @param tagId      標籤ID
     * @param createBy   建立者
     * @return 結果：1=成功，0=已存在
     */
    int bindTag(String lineUserId, Long tagId, String createBy);

    /**
     * 批次為使用者貼標（多使用者，單一標籤）
     *
     * @param lineUserIds LINE 使用者 ID 列表
     * @param tagId       標籤ID
     * @param createBy    建立者
     * @return 成功貼標數量
     */
    int batchBindTag(List<String> lineUserIds, Long tagId, String createBy);

    /**
     * 批次為使用者貼標（多使用者，多標籤）
     *
     * @param lineUserIds LINE 使用者 ID 列表
     * @param tagIds      標籤ID 列表
     * @param createBy    建立者
     * @return 成功貼標數量
     */
    int batchBindTags(List<String> lineUserIds, List<Long> tagIds, String createBy);

    /**
     * 批次為使用者貼標（單一使用者，多標籤）
     *
     * @param lineUserId LINE 使用者 ID
     * @param tagIds     標籤ID 列表
     * @param createBy   建立者
     * @return 成功貼標數量
     */
    int batchBindTags(String lineUserId, List<Long> tagIds, String createBy);

    /**
     * 移除使用者標籤
     *
     * @param lineUserId LINE 使用者 ID
     * @param tagId      標籤ID
     * @return 結果
     */
    int unbindTag(String lineUserId, Long tagId);

    /**
     * 移除使用者的所有標籤
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int unbindAllTags(String lineUserId);

    /**
     * 移除標籤的所有使用者關聯
     *
     * @param tagId 標籤ID
     * @return 結果
     */
    int unbindAllUsers(Long tagId);

    /**
     * 統計標籤的使用者數量
     *
     * @param tagId 標籤ID
     * @return 使用者數量
     */
    int countUsersByTagId(Long tagId);

    /**
     * 批次刪除關聯
     *
     * @param ids 關聯ID陣列
     * @return 結果
     */
    int deleteByIds(Long[] ids);

    /**
     * 批次查詢多個使用者的標籤關聯
     *
     * @param lineUserIds LINE 使用者 ID 列表
     * @return 關聯集合
     */
    List<LineUserTagRelation> selectByLineUserIds(List<String> lineUserIds);

    /**
     * 批次為使用者貼標（含驗證和額外標籤支援）
     * 會驗證使用者是否存在，並自動建立不存在的標籤
     *
     * @param records       貼標記錄列表
     * @param defaultTagIds 預設標籤ID列表
     * @param createBy      建立者
     * @return 處理結果 Map，包含 successCount, failedCount, failedRecords, newTagsCreated
     */
    Map<String, Object> batchBindWithValidation(
            List<com.cheng.line.service.impl.LineUserTagRelationServiceImpl.TagRecordDTO> records,
            List<Long> defaultTagIds,
            String createBy
    );
}
