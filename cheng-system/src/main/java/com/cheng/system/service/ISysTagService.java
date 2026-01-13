package com.cheng.system.service;

import com.cheng.system.domain.SysTag;

import java.util.List;

/**
 * 系統標籤 Service 介面
 *
 * @author cheng
 */
public interface ISysTagService {

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
     * 查詢適用於 LINE 平台的標籤列表
     *
     * @param status 狀態（可選，null 表示全部）
     * @return 標籤集合
     */
    List<SysTag> selectLineTagList(Integer status);

    /**
     * 查詢適用於庫存平台的標籤列表
     *
     * @param status 狀態（可選，null 表示全部）
     * @return 標籤集合
     */
    List<SysTag> selectInventoryTagList(Integer status);

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
     * @param sysTag 標籤
     * @return true=唯一, false=不唯一
     */
    boolean checkTagCodeUnique(SysTag sysTag);

    /**
     * 重算所有標籤的使用者和物品計數
     */
    void recalculateAllCounts();

    /**
     * 更新指定標籤的 LINE 使用者計數
     *
     * @param tagId 標籤ID
     */
    void updateUserCount(Long tagId);

    /**
     * 更新指定標籤的庫存物品計數
     *
     * @param tagId 標籤ID
     */
    void updateItemCount(Long tagId);
}
