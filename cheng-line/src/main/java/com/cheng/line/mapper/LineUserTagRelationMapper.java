package com.cheng.line.mapper;

import com.cheng.line.domain.LineUserTagRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LINE 使用者標籤關聯 Mapper 介面
 *
 * @author cheng
 */
public interface LineUserTagRelationMapper {

    /**
     * 查詢關聯
     *
     * @param id 關聯ID
     * @return 關聯
     */
    LineUserTagRelation selectLineUserTagRelationById(Long id);

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
     * 檢查關聯是否已存在
     *
     * @param lineUserId LINE 使用者 ID
     * @param tagId      標籤ID
     * @return 關聯
     */
    LineUserTagRelation checkRelationExists(@Param("lineUserId") String lineUserId, @Param("tagId") Long tagId);

    /**
     * 新增關聯
     *
     * @param relation 關聯
     * @return 結果
     */
    int insertLineUserTagRelation(LineUserTagRelation relation);

    /**
     * 批次新增關聯
     *
     * @param relations 關聯列表
     * @return 結果
     */
    int batchInsertLineUserTagRelation(List<LineUserTagRelation> relations);

    /**
     * 刪除關聯
     *
     * @param id 關聯ID
     * @return 結果
     */
    int deleteLineUserTagRelationById(Long id);

    /**
     * 依使用者和標籤刪除關聯
     *
     * @param lineUserId LINE 使用者 ID
     * @param tagId      標籤ID
     * @return 結果
     */
    int deleteByUserIdAndTagId(@Param("lineUserId") String lineUserId, @Param("tagId") Long tagId);

    /**
     * 刪除指定使用者的所有標籤關聯
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int deleteByLineUserId(String lineUserId);

    /**
     * 刪除指定標籤的所有使用者關聯
     *
     * @param tagId 標籤ID
     * @return 結果
     */
    int deleteByTagId(Long tagId);

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
    int deleteLineUserTagRelationByIds(Long[] ids);

    /**
     * 批次查詢多個使用者的標籤關聯
     *
     * @param lineUserIds LINE 使用者 ID 列表
     * @return 關聯集合
     */
    List<LineUserTagRelation> selectByLineUserIds(@Param("lineUserIds") List<String> lineUserIds);

    /**
     * 批次插入關聯（忽略重複）
     *
     * @param relations 關聯列表
     * @return 插入數量
     */
    int batchInsertIgnore(@Param("relations") List<LineUserTagRelation> relations);
}
