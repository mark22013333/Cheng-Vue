package com.cheng.system.service;

import com.cheng.system.domain.SysTagGroup;

import java.util.List;
import java.util.Map;

/**
 * 標籤群組 Service 介面
 *
 * @author cheng
 */
public interface ISysTagGroupService {

    /**
     * 查詢標籤群組列表
     *
     * @param sysTagGroup 查詢條件
     * @return 群組集合
     */
    List<SysTagGroup> selectSysTagGroupList(SysTagGroup sysTagGroup);

    /**
     * 根據 ID 查詢標籤群組（含明細）
     *
     * @param groupId 群組ID
     * @return 群組資訊
     */
    SysTagGroup selectSysTagGroupById(Long groupId);

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
     * 檢查群組代碼是否唯一
     *
     * @param sysTagGroup 群組資訊
     * @return true=唯一, false=不唯一
     */
    boolean checkGroupCodeUnique(SysTagGroup sysTagGroup);

    /**
     * 執行群組運算（LINE 使用者）
     *
     * @param groupId 群組ID
     * @return 運算結果（包含符合人數、執行時間等）
     */
    Map<String, Object> executeLineGroupCalc(Long groupId);

    /**
     * 執行群組運算（庫存物品）
     *
     * @param groupId 群組ID
     * @return 運算結果
     */
    Map<String, Object> executeInventoryGroupCalc(Long groupId);

    /**
     * 預覽群組運算結果（不寫入資料庫）
     *
     * @param groupId 群組ID
     * @param limit   限制數量
     * @return 預覽結果
     */
    Map<String, Object> previewGroupCalc(Long groupId, int limit);
}
