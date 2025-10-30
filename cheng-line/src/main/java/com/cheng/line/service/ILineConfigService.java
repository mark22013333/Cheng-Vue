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


    String WEBHOOK = "/webhook/line";
    
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
     * 根據 Bot Basic ID 查詢 LINE 頻道設定
     *
     * @param botBasicId Bot Basic ID（例如：@322okyxf）
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigByBotBasicId(String botBasicId);

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
     * 查詢所有啟用的頻道設定
     *
     * @return LINE 頻道設定集合
     */
    List<LineConfig> selectEnabledLineConfigs();

    /**
     * 根據頻道類型查詢頻道設定
     *
     * @param channelType 頻道類型代碼
     * @return LINE 頻道設定
     */
    LineConfig selectLineConfigByType(String channelType);

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
     * 測試 LINE 頻道連線（完整測試：API、Webhook、Bot 資訊）
     *
     * @param configId 設定ID
     * @return 測試結果
     */
    com.cheng.line.vo.ConnectionTestVO testLineConnection(Integer configId);

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

    /**
     * 設定 LINE Webhook 端點
     * 呼叫 LINE Messaging API 將 Webhook URL 設定到 LINE Platform
     *
     * @param configId 設定ID
     */
    void setLineWebhookEndpoint(Integer configId);

    /**
     * 設定 LINE Webhook 端點（使用表單當前值）
     * 呼叫 LINE Messaging API 將 Webhook URL 設定到 LINE Platform
     * 不從資料庫讀取，直接使用傳入的參數
     *
     * @param webhookUrl Webhook URL
     * @param channelAccessToken Channel Access Token
     * @param configId 設定ID（可選，用於更新資料庫狀態）
     */
    void setLineWebhookEndpointWithParams(String webhookUrl, String channelAccessToken, Integer configId);

    /**
     * 取得系統預設的 Webhook 基礎 URL
     *
     * @return 預設 Webhook 基礎 URL
     */
    String getDefaultWebhookBaseUrl();
}
