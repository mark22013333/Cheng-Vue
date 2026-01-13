package com.cheng.system.mapper;

import com.cheng.system.domain.SysTagGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 標籤群組 Mapper 介面
 *
 * @author cheng
 */
public interface SysTagGroupMapper {

    /**
     * 查詢標籤群組列表
     *
     * @param sysTagGroup 查詢條件
     * @return 群組集合
     */
    List<SysTagGroup> selectSysTagGroupList(SysTagGroup sysTagGroup);

    /**
     * 根據 ID 查詢標籤群組
     *
     * @param groupId 群組ID
     * @return 群組資訊
     */
    SysTagGroup selectSysTagGroupById(Long groupId);

    /**
     * 根據群組代碼查詢
     *
     * @param groupCode 群組代碼
     * @return 群組資訊
     */
    SysTagGroup selectSysTagGroupByCode(String groupCode);

    /**
     * 新增標籤群組
     *
     * @param sysTagGroup 群組資訊
     * @return 影響行數
     */
    int insertSysTagGroup(SysTagGroup sysTagGroup);

    /**
     * 修改標籤群組
     *
     * @param sysTagGroup 群組資訊
     * @return 影響行數
     */
    int updateSysTagGroup(SysTagGroup sysTagGroup);

    /**
     * 刪除標籤群組
     *
     * @param groupId 群組ID
     * @return 影響行數
     */
    int deleteSysTagGroupById(Long groupId);

    /**
     * 批次刪除標籤群組
     *
     * @param groupIds 群組ID陣列
     * @return 影響行數
     */
    int deleteSysTagGroupByIds(Long[] groupIds);

    /**
     * 更新群組運算結果數量
     *
     * @param groupId     群組ID
     * @param countResult 結果數量
     * @return 影響行數
     */
    int updateCountResult(@Param("groupId") Long groupId, @Param("countResult") Integer countResult);

    /**
     * 檢查群組代碼是否唯一
     *
     * @param groupCode 群組代碼
     * @param groupId   排除的群組ID（修改時使用）
     * @return 數量
     */
    int checkGroupCodeUnique(@Param("groupCode") String groupCode, @Param("groupId") Long groupId);
}
