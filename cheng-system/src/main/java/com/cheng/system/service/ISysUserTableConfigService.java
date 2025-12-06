package com.cheng.system.service;

import com.cheng.system.domain.SysUserTableConfig;

/**
 * 使用者表格欄位配置Service介面
 * 
 * @author cheng
 * @date 2025-12-06
 */
public interface ISysUserTableConfigService {
    /**
     * 查詢使用者表格欄位配置
     * 
     * @param configId 配置ID
     * @return 使用者表格欄位配置
     */
    SysUserTableConfig selectSysUserTableConfigByConfigId(Long configId);

    /**
     * 取得當前使用者指定頁面的欄位配置
     * 
     * @param pageKey 頁面標識
     * @return 欄位配置（JSON字串），若不存在返回null
     */
    String getColumnConfig(String pageKey);

    /**
     * 儲存當前使用者指定頁面的欄位配置
     * 
     * @param pageKey 頁面標識
     * @param columnConfig 欄位配置（JSON字串）
     * @return 結果
     */
    int saveColumnConfig(String pageKey, String columnConfig);

    /**
     * 新增使用者表格欄位配置
     * 
     * @param sysUserTableConfig 使用者表格欄位配置
     * @return 結果
     */
    int insertSysUserTableConfig(SysUserTableConfig sysUserTableConfig);

    /**
     * 修改使用者表格欄位配置
     * 
     * @param sysUserTableConfig 使用者表格欄位配置
     * @return 結果
     */
    int updateSysUserTableConfig(SysUserTableConfig sysUserTableConfig);

    /**
     * 批次刪除使用者表格欄位配置
     * 
     * @param configIds 需要刪除的配置ID
     * @return 結果
     */
    int deleteSysUserTableConfigByConfigIds(Long[] configIds);

    /**
     * 刪除使用者表格欄位配置資訊
     * 
     * @param configId 配置ID
     * @return 結果
     */
    int deleteSysUserTableConfigByConfigId(Long configId);
}
