package com.cheng.line.mapper;

import com.cheng.line.domain.LineConfig;
import com.cheng.line.enums.ChannelType;

import java.util.List;

/**
 * LINE 頻道設定 Mapper 介面
 *
 * @author cheng
 */
public interface LineConfigMapper {

    /**
     * 查詢 LINE 頻道設定
     *
     * @param configId 設定ID
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigById(Integer configId);

    /**
     * 根據頻道類型查詢設定
     *
     * @param channelTypeCode 頻道類型代碼
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigByType(String channelTypeCode);

    /**
     * 根據 Bot Basic ID 查詢設定
     *
     * @param botBasicId Bot Basic ID（例如：@322okyxf）
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigByBotBasicId(String botBasicId);

    /**
     * 查詢 LINE 頻道設定列表
     *
     * @param lineConfig LINE 頻道設定
     * @return LINE 頻道設定集合
     */
    List<LineConfig> selectLineConfigList(LineConfig lineConfig);

    /**
     * 查詢所有啟用的頻道設定
     *
     * @return LINE 頻道設定集合
     */
    List<LineConfig> selectEnabledLineConfigs();

    /**
     * 查詢預設頻道設定
     *
     * @return LINE 頻道設定
     */
    LineConfig selectDefaultLineConfig();

    /**
     * 新增 LINE 頻道設定
     *
     * @param lineConfig LINE 頻道設定
     * @return 結果
     */
    int insertLineConfig(LineConfig lineConfig);

    /**
     * 修改 LINE 頻道設定
     *
     * @param lineConfig LINE 頻道設定
     * @return 結果
     */
    int updateLineConfig(LineConfig lineConfig);

    /**
     * 刪除 LINE 頻道設定
     *
     * @param configId 設定ID
     * @return 結果
     */
    int deleteLineConfigById(Integer configId);

    /**
     * 批次刪除 LINE 頻道設定
     *
     * @param configIds 需要刪除的資料ID
     * @return 結果
     */
    int deleteLineConfigByIds(Integer[] configIds);

    /**
     * 檢查頻道類型是否已存在
     *
     * @param channelType 頻道類型
     * @return 結果
     */
    int checkChannelTypeUnique(String channelType);

    /**
     * 清除其他頻道的預設狀態
     *
     * @return 結果
     */
    int clearDefaultStatus();

    /**
     * 設定預設頻道
     *
     * @param configId 設定ID
     * @return 結果
     */
    int setDefaultChannel(Integer configId);

    /**
     * 更新 Webhook 驗證狀態
     *
     * @param configId 設定ID
     * @param webhookStatusCode Webhook 狀態代碼（0=停用/未驗證, 1=啟用/已驗證）
     * @return 結果
     */
    int updateWebhookStatus(Integer configId, Integer webhookStatusCode);
}
