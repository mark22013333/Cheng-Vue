package com.cheng.system.service.impl;

import com.cheng.common.constant.PermConstants;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.system.domain.SysTableConfigTemplate;
import com.cheng.system.domain.SysUserTableConfig;
import com.cheng.system.mapper.SysTableConfigTemplateMapper;
import com.cheng.system.mapper.SysUserTableConfigMapper;
import com.cheng.system.service.ISysUserTableConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 使用者表格欄位配置Service業務層處理
 * 
 * @author cheng
 * @date 2025-12-06
 */
@Service
public class SysUserTableConfigServiceImpl implements ISysUserTableConfigService {
    @Autowired
    private SysUserTableConfigMapper sysUserTableConfigMapper;

    @Autowired
    private SysTableConfigTemplateMapper sysTableConfigTemplateMapper;

    /**
     * 查詢使用者表格欄位配置
     * 
     * @param configId 配置ID
     * @return 使用者表格欄位配置
     */
    @Override
    public SysUserTableConfig selectSysUserTableConfigByConfigId(Long configId) {
        return sysUserTableConfigMapper.selectSysUserTableConfigByConfigId(configId);
    }

    /**
     * 取得當前使用者指定頁面的欄位配置
     * 
     * @param pageKey 頁面標識
     * @return 欄位配置（JSON字串），若不存在返回null
     */
    @Override
    public String getColumnConfig(String pageKey) {
        Long userId = SecurityUtils.getUserId();
        SysUserTableConfig config = sysUserTableConfigMapper.selectByUserIdAndPageKey(userId, pageKey);
        return config != null ? config.getColumnConfig() : null;
    }

    /**
     * 儲存當前使用者指定頁面的欄位配置
     * 
     * @param pageKey 頁面標識
     * @param columnConfig 欄位配置（JSON字串）
     * @return 結果
     */
    @Override
    public int saveColumnConfig(String pageKey, String columnConfig) {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        
        SysUserTableConfig config = new SysUserTableConfig();
        config.setUserId(userId);
        config.setPageKey(pageKey);
        config.setColumnConfig(columnConfig);
        config.setCreateBy(username);
        config.setUpdateBy(username);
        
        return sysUserTableConfigMapper.insertOrUpdate(config);
    }

    /**
     * 新增使用者表格欄位配置
     * 
     * @param sysUserTableConfig 使用者表格欄位配置
     * @return 結果
     */
    @Override
    public int insertSysUserTableConfig(SysUserTableConfig sysUserTableConfig) {
        sysUserTableConfig.setCreateTime(DateUtils.getNowDate());
        return sysUserTableConfigMapper.insertSysUserTableConfig(sysUserTableConfig);
    }

    /**
     * 修改使用者表格欄位配置
     * 
     * @param sysUserTableConfig 使用者表格欄位配置
     * @return 結果
     */
    @Override
    public int updateSysUserTableConfig(SysUserTableConfig sysUserTableConfig) {
        sysUserTableConfig.setUpdateTime(DateUtils.getNowDate());
        return sysUserTableConfigMapper.updateSysUserTableConfig(sysUserTableConfig);
    }

    /**
     * 批次刪除使用者表格欄位配置
     * 
     * @param configIds 需要刪除的配置ID
     * @return 結果
     */
    @Override
    public int deleteSysUserTableConfigByConfigIds(Long[] configIds) {
        return sysUserTableConfigMapper.deleteSysUserTableConfigByConfigIds(configIds);
    }

    /**
     * 刪除使用者表格欄位配置資訊
     * 
     * @param configId 配置ID
     * @return 結果
     */
    @Override
    public int deleteSysUserTableConfigByConfigId(Long configId) {
        return sysUserTableConfigMapper.deleteSysUserTableConfigByConfigId(configId);
    }

    /**
     * 智慧取得有效配置：
     * 1. 有 customize 權限 → 讀個人配置 → fallback 模版
     * 2. 無 customize 權限 → 直接讀模版
     * 3. 都沒有 → 返回 null（前端使用 defaultColumns）
     */
    @Override
    public String getEffectiveConfig(String pageKey) {
        boolean hasCustomize = SecurityUtils.hasPermi(PermConstants.System.TableConfig.CUSTOMIZE);

        if (hasCustomize) {
            String userConfig = getColumnConfig(pageKey);
            if (userConfig != null) {
                return userConfig;
            }
        }

        return getTemplateConfig(pageKey);
    }

    @Override
    public String getTemplateConfig(String pageKey) {
        SysTableConfigTemplate template = sysTableConfigTemplateMapper.selectByPageKey(pageKey);
        return template != null ? template.getColumnConfig() : null;
    }

    @Override
    public int saveTemplateConfig(String pageKey, String columnConfig) {
        String username = SecurityUtils.getUsername();

        SysTableConfigTemplate template = new SysTableConfigTemplate();
        template.setPageKey(pageKey);
        template.setColumnConfig(columnConfig);
        template.setCreateBy(username);
        template.setUpdateBy(username);

        return sysTableConfigTemplateMapper.insertOrUpdate(template);
    }
}
