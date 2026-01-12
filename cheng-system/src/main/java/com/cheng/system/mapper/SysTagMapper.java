package com.cheng.system.mapper;

import com.cheng.system.domain.SysTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系統標籤 Mapper 介面
 *
 * @author cheng
 */
public interface SysTagMapper {

    /**
     * 查詢標籤
     *
     * @param tagId 標籤ID
     * @return 標籤
     */
    SysTag selectSysTagByTagId(Long tagId);

    /**
     * 根據標籤代碼查詢標籤
     *
     * @param tagCode 標籤代碼
     * @return 標籤
     */
    SysTag selectSysTagByTagCode(String tagCode);

    /**
     * 查詢標籤列表
     *
     * @param sysTag 標籤查詢條件
     * @return 標籤集合
     */
    List<SysTag> selectSysTagList(SysTag sysTag);

    /**
     * 查詢適用於指定平台的標籤列表
     *
     * @param platformScope 平台範圍（LINE/INVENTORY/ALL）
     * @param status        狀態（可選）
     * @return 標籤集合
     */
    List<SysTag> selectSysTagListByPlatform(@Param("platformScope") String platformScope, @Param("status") Integer status);

    /**
     * 新增標籤
     *
     * @param sysTag 標籤
     * @return 結果
     */
    int insertSysTag(SysTag sysTag);

    /**
     * 修改標籤
     *
     * @param sysTag 標籤
     * @return 結果
     */
    int updateSysTag(SysTag sysTag);

    /**
     * 刪除標籤
     *
     * @param tagId 標籤ID
     * @return 結果
     */
    int deleteSysTagByTagId(Long tagId);

    /**
     * 批次刪除標籤
     *
     * @param tagIds 需要刪除的標籤ID陣列
     * @return 結果
     */
    int deleteSysTagByTagIds(Long[] tagIds);

    /**
     * 檢查標籤代碼是否唯一
     *
     * @param tagCode 標籤代碼
     * @return 標籤
     */
    SysTag checkTagCodeUnique(String tagCode);

    /**
     * 檢查同平台標籤名稱是否存在
     *
     * @param tagName       標籤名稱
     * @param platformScope 平台範圍
     * @return 標籤
     */
    SysTag selectByNameAndPlatform(@Param("tagName") String tagName, @Param("platformScope") String platformScope);

    /**
     * 更新 LINE 使用者計數
     *
     * @param tagId     標籤ID
     * @param userCount 使用者數量
     * @return 結果
     */
    int updateUserCount(@Param("tagId") Long tagId, @Param("userCount") Integer userCount);

    /**
     * 更新庫存物品計數
     *
     * @param tagId     標籤ID
     * @param itemCount 物品數量
     * @return 結果
     */
    int updateItemCount(@Param("tagId") Long tagId, @Param("itemCount") Integer itemCount);

    /**
     * 批次重算所有標籤的使用者計數
     *
     * @return 結果
     */
    int recalculateAllUserCounts();

    /**
     * 批次重算所有標籤的物品計數
     *
     * @return 結果
     */
    int recalculateAllItemCounts();

    /**
     * 根據標籤名稱列表查詢標籤
     *
     * @param tagNames 標籤名稱列表
     * @return 標籤列表
     */
    List<SysTag> selectByTagNames(@Param("tagNames") List<String> tagNames);
}
