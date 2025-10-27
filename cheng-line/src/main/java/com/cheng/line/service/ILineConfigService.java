package com.cheng.line.service;

import com.cheng.line.domain.LineConfig;
import com.cheng.line.enums.ChannelType;

import java.util.List;

/**
 * LINE 頻道設定 服務層
 *
 * @author cheng
 */
public interface ILineConfigService {
    
    /**
     * 查詢 LINE 頻道設定
     *
     * @param configId 設定ID
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigById(Integer configId);

    /**
     * 根據頻道類型查詢 LINE 頻道設定
     *
     * @param channelType 頻道類型
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigByChannelType(ChannelType channelType);

    /**
     * 取得預設頻道設定
     *
     * @return LINE 頻道設定
     */
    LineConfig selectDefaultLineConfig();

    /**
     * 查詢 LINE 頻道設定列表
     *
     * @param lineConfig LINE 頻道設定
     * @return LINE 頻道設定集合
     */
    List<LineConfig> selectLineConfigList(LineConfig lineConfig);

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
     * 批次刪除 LINE 頻道設定
     *
     * @param configIds 需要刪除的設定ID
     * @return 結果
     */
    int deleteLineConfigByIds(Integer[] configIds);

    /**
     * 刪除 LINE 頻道設定
     *
     * @param configId 設定ID
     * @return 結果
     */
    int deleteLineConfigById(Integer configId);

    /**
     * 校驗頻道類型是否唯一
     *
     * @param lineConfig LINE 頻道設定
     * @return 結果
     */
    boolean checkChannelTypeUnique(LineConfig lineConfig);

    /**
     * 測試 LINE 頻道連線
     *
     * @param configId 設定ID
     * @return 測試結果訊息
     */
    String testLineConnection(Integer configId);

    /**
     * 測試 Webhook 端點
     *
     * @param configId 設定ID
     * @return 測試結果訊息
     */
    String testWebhook(Integer configId);

    /**
     * 更新 Webhook URL
     *
     * @param configId   設定ID
     * @param webhookUrl Webhook URL
     * @return 結果
     */
    int updateWebhookUrl(Integer configId, String webhookUrl);

    /**
     * 設定為預設頻道
     *
     * @param configId 設定ID
     * @return 結果
     */
    int setAsDefaultChannel(Integer configId);
}
