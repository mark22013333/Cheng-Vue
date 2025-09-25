package com.cheng.system.mapper;

import com.cheng.system.domain.SysConfig;

import java.util.List;

/**
 * 參數設定 數據層
 *
 * @author cheng
 */
public interface SysConfigMapper {
    /**
     * 查詢參數設定訊息
     *
     * @param config 參數設定訊息
     * @return 參數設定訊息
     */
    SysConfig selectConfig(SysConfig config);

    /**
     * 通過ID查詢設定
     *
     * @param configId 參數ID
     * @return 參數設定訊息
     */
    SysConfig selectConfigById(Long configId);

    /**
     * 查詢參數設定列表
     *
     * @param config 參數設定訊息
     * @return 參數設定集合
     */
    List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 根據鍵名查詢參數設定訊息
     *
     * @param configKey 參數鍵名
     * @return 參數設定訊息
     */
    SysConfig checkConfigKeyUnique(String configKey);

    /**
     * 新增參數設定
     *
     * @param config 參數設定訊息
     * @return 結果
     */
    int insertConfig(SysConfig config);

    /**
     * 修改參數設定
     *
     * @param config 參數設定訊息
     * @return 結果
     */
    int updateConfig(SysConfig config);

    /**
     * 刪除參數設定
     *
     * @param configId 參數ID
     * @return 結果
     */
    int deleteConfigById(Long configId);

    /**
     * 批次刪除參數訊息
     *
     * @param configIds 需要刪除的參數ID
     * @return 結果
     */
    int deleteConfigByIds(Long[] configIds);
}
