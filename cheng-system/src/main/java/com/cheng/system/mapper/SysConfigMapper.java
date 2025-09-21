package com.cheng.system.mapper;

import com.cheng.system.domain.SysConfig;

import java.util.List;

/**
 * 參數配置 數據層
 *
 * @author cheng
 */
public interface SysConfigMapper
{
    /**
     * 查詢參數配置訊息
     *
     * @param config 參數配置訊息
     * @return 參數配置訊息
     */
    public SysConfig selectConfig(SysConfig config);

    /**
     * 通過ID查詢配置
     *
     * @param configId 參數ID
     * @return 參數配置訊息
     */
    public SysConfig selectConfigById(Long configId);

    /**
     * 查詢參數配置列表
     *
     * @param config 參數配置訊息
     * @return 參數配置集合
     */
    public List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 根據鍵名查詢參數配置訊息
     *
     * @param configKey 參數鍵名
     * @return 參數配置訊息
     */
    public SysConfig checkConfigKeyUnique(String configKey);

    /**
     * 新增參數配置
     *
     * @param config 參數配置訊息
     * @return 結果
     */
    public int insertConfig(SysConfig config);

    /**
     * 修改參數配置
     *
     * @param config 參數配置訊息
     * @return 結果
     */
    public int updateConfig(SysConfig config);

    /**
     * 刪除參數配置
     *
     * @param configId 參數ID
     * @return 結果
     */
    public int deleteConfigById(Long configId);

    /**
     * 批量刪除參數訊息
     *
     * @param configIds 需要刪除的參數ID
     * @return 結果
     */
    public int deleteConfigByIds(Long[] configIds);
}
