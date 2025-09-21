package com.cheng.system.service;

import com.cheng.system.domain.SysConfig;

import java.util.List;

/**
 * 參數配置 服務層
 *
 * @author cheng
 */
public interface ISysConfigService
{
    /**
     * 查詢參數配置訊息
     *
     * @param configId 參數配置ID
     * @return 參數配置訊息
     */
    public SysConfig selectConfigById(Long configId);

    /**
     * 根據鍵名查詢參數配置訊息
     *
     * @param configKey 參數鍵名
     * @return 參數鍵值
     */
    public String selectConfigByKey(String configKey);

    /**
     * 取得驗證碼開關
     *
     * @return true開啟，false關閉
     */
    public boolean selectCaptchaEnabled();

    /**
     * 查詢參數配置列表
     *
     * @param config 參數配置訊息
     * @return 參數配置集合
     */
    public List<SysConfig> selectConfigList(SysConfig config);

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
     * 批量刪除參數訊息
     *
     * @param configIds 需要刪除的參數ID
     */
    public void deleteConfigByIds(Long[] configIds);

    /**
     * 載入參數暫存數據
     */
    public void loadingConfigCache();

    /**
     * 清除參數暫存數據
     */
    public void clearConfigCache();

    /**
     * 重置參數暫存數據
     */
    public void resetConfigCache();

    /**
     * 校驗參數鍵名是否唯一
     *
     * @param config 參數訊息
     * @return 結果
     */
    public boolean checkConfigKeyUnique(SysConfig config);
}
