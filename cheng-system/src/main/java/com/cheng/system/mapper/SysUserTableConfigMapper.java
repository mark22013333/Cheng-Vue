package com.cheng.system.mapper;

import com.cheng.system.domain.SysUserTableConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 使用者表格欄位配置Mapper介面
 *
 * @author cheng
 * @since 2025-12-06
 */
@Mapper
public interface SysUserTableConfigMapper {
    /**
     * 查詢使用者表格欄位配置
     *
     * @param configId 配置ID
     * @return 使用者表格欄位配置
     */
    SysUserTableConfig selectSysUserTableConfigByConfigId(Long configId);

    /**
     * 根據使用者ID和頁面標識查詢配置
     *
     * @param userId  使用者ID
     * @param pageKey 頁面標識
     * @return 使用者表格欄位配置
     */
    SysUserTableConfig selectByUserIdAndPageKey(@Param("userId") Long userId, @Param("pageKey") String pageKey);

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
     * 新增或更新使用者表格欄位配置
     * 使用 INSERT ... ON DUPLICATE KEY UPDATE
     *
     * @param sysUserTableConfig 使用者表格欄位配置
     * @return 結果
     */
    int insertOrUpdate(SysUserTableConfig sysUserTableConfig);

    /**
     * 刪除使用者表格欄位配置
     *
     * @param configId 配置ID
     * @return 結果
     */
    int deleteSysUserTableConfigByConfigId(Long configId);

    /**
     * 批次刪除使用者表格欄位配置
     *
     * @param configIds 需要刪除的配置ID
     * @return 結果
     */
    int deleteSysUserTableConfigByConfigIds(Long[] configIds);
}
