package com.cheng.line.service.impl;

import com.cheng.common.constant.UserConstants;
import com.cheng.common.enums.Status;
import com.cheng.common.enums.YesNo;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.JasyptUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.enums.ChannelType;
import com.cheng.line.mapper.LineConfigMapper;
import com.cheng.line.service.ILineConfigService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.TestWebhookEndpointRequest;
import com.linecorp.bot.messaging.model.TestWebhookEndpointResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * LINE 頻道設定 服務層實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineConfigServiceImpl implements ILineConfigService {

    @Resource
    private LineConfigMapper lineConfigMapper;

    /**
     * 查詢 LINE 頻道設定
     *
     * @param configId 設定ID
     * @return LINE 頻道設定
     */
    @Override
    public LineConfig selectLineConfigById(Integer configId) {
        LineConfig config = lineConfigMapper.selectLineConfigById(configId);
        if (config != null) {
            // 解密敏感資料
            decryptSensitiveData(config);
        }
        return config;
    }

    /**
     * 根據頻道類型查詢 LINE 頻道設定
     *
     * @param channelType 頻道類型
     * @return LINE 頻道設定
     */
    @Override
    public LineConfig selectLineConfigByChannelType(ChannelType channelType) {
        LineConfig config = lineConfigMapper.selectLineConfigByType(channelType);
        if (config != null) {
            decryptSensitiveData(config);
        }
        return config;
    }

    /**
     * 取得預設頻道設定
     *
     * @return LINE 頻道設定
     */
    @Override
    public LineConfig selectDefaultLineConfig() {
        LineConfig config = lineConfigMapper.selectDefaultLineConfig();
        if (config != null) {
            decryptSensitiveData(config);
        }
        return config;
    }

    /**
     * 查詢 LINE 頻道設定列表
     *
     * @param lineConfig LINE 頻道設定
     * @return LINE 頻道設定集合
     */
    @Override
    public List<LineConfig> selectLineConfigList(LineConfig lineConfig) {
        List<LineConfig> list = lineConfigMapper.selectLineConfigList(lineConfig);
        // 列表查詢時不解密敏感資料，只在詳情查詢時解密
        return list;
    }

    /**
     * 新增 LINE 頻道設定
     *
     * @param lineConfig LINE 頻道設定
     * @return 結果
     */
    @Override
    @Transactional
    public int insertLineConfig(LineConfig lineConfig) {
        // 檢查頻道類型是否唯一
        if (!checkChannelTypeUnique(lineConfig)) {
            throw new ServiceException("頻道類型已存在，無法新增");
        }

        // 加密敏感資料
        encryptSensitiveData(lineConfig);

        // 如果設定為預設頻道，需要先清除其他頻道的預設狀態
        if (YesNo.YES.equals(lineConfig.getIsDefault())) {
            lineConfigMapper.clearDefaultStatus();
        }

        // 設定初始狀態
        if (lineConfig.getStatus() == null) {
            lineConfig.setStatus(Status.ENABLE);
        }
        if (lineConfig.getWebhookStatus() == null) {
            lineConfig.setWebhookStatus(Status.DISABLE);
        }
        if (lineConfig.getIsDefault() == null) {
            lineConfig.setIsDefault(YesNo.NO);
        }

        return lineConfigMapper.insertLineConfig(lineConfig);
    }

    /**
     * 修改 LINE 頻道設定
     *
     * @param lineConfig LINE 頻道設定
     * @return 結果
     */
    @Override
    @Transactional
    public int updateLineConfig(LineConfig lineConfig) {
        // 取得原有設定
        LineConfig oldConfig = lineConfigMapper.selectLineConfigById(lineConfig.getConfigId());
        if (oldConfig == null) {
            throw new ServiceException("頻道設定不存在");
        }

        // 如果頻道類型有變更，檢查新的類型是否唯一
        if (!oldConfig.getChannelType().equals(lineConfig.getChannelType())) {
            if (!checkChannelTypeUnique(lineConfig)) {
                throw new ServiceException("頻道類型已存在，無法修改");
            }
        }

        // 加密敏感資料
        encryptSensitiveData(lineConfig);

        // 如果設定為預設頻道，需要先清除其他頻道的預設狀態
        if (YesNo.YES.equals(lineConfig.getIsDefault())) {
            lineConfigMapper.clearDefaultStatus();
        }

        return lineConfigMapper.updateLineConfig(lineConfig);
    }

    /**
     * 批次刪除 LINE 頻道設定
     *
     * @param configIds 需要刪除的設定ID
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteLineConfigByIds(Integer[] configIds) {
        for (Integer configId : configIds) {
            LineConfig config = lineConfigMapper.selectLineConfigById(configId);
            if (config != null && YesNo.YES.equals(config.getIsDefault())) {
                throw new ServiceException("無法刪除預設頻道，請先設定其他頻道為預設");
            }
        }
        return lineConfigMapper.deleteLineConfigByIds(configIds);
    }

    /**
     * 刪除 LINE 頻道設定
     *
     * @param configId 設定ID
     * @return 結果
     */
    @Override
    public int deleteLineConfigById(Integer configId) {
        LineConfig config = lineConfigMapper.selectLineConfigById(configId);
        if (config != null && YesNo.YES.equals(config.getIsDefault())) {
            throw new ServiceException("無法刪除預設頻道，請先設定其他頻道為預設");
        }
        return lineConfigMapper.deleteLineConfigById(configId);
    }

    /**
     * 校驗頻道類型是否唯一
     *
     * @param lineConfig LINE 頻道設定
     * @return 結果
     */
    @Override
    public boolean checkChannelTypeUnique(LineConfig lineConfig) {
        Integer configId = StringUtils.isNull(lineConfig.getConfigId()) ? -1 : lineConfig.getConfigId();
        int count = lineConfigMapper.checkChannelTypeUnique(lineConfig.getChannelTypeCode());

        LineConfig info = lineConfigMapper.selectLineConfigByType(lineConfig.getChannelType());
        if (StringUtils.isNotNull(info) && !info.getConfigId().equals(configId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 測試 LINE 頻道連線
     *
     * @param configId 設定ID
     * @return 測試結果訊息
     */
    @Override
    public String testLineConnection(Integer configId) {
        LineConfig config = selectLineConfigById(configId);
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            // 建立 LINE Messaging API Client (SDK 9.x)
            MessagingApiClient client = MessagingApiClient.builder(config.getChannelAccessToken()).build();

            // 使用 SDK 提供的 getBotInfo 方法測試連線
            var botInfo = client.getBotInfo().get().body();

            log.info("Bot 資訊取得成功：userId={}, displayName={}",
                    botInfo.userId(), botInfo.displayName());

            return String.format("連線測試成功！Bot 名稱：%s", botInfo.displayName());

        } catch (InterruptedException | ExecutionException e) {
            log.error("LINE 連線測試失敗", e);
            throw new ServiceException("連線測試失敗：" + e.getMessage());
        }
    }

    /**
     * 測試 Webhook 端點
     *
     * @param configId 設定ID
     * @return 測試結果訊息
     */
    @Override
    public String testWebhook(Integer configId) {
        LineConfig config = selectLineConfigById(configId);
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        if (StringUtils.isEmpty(config.getWebhookUrl())) {
            throw new ServiceException("Webhook URL 未設定");
        }

        try {
            // 建立 LINE Messaging API Client (SDK 9.x)
            MessagingApiClient client = MessagingApiClient.builder(config.getChannelAccessToken()).build();

            // 測試 Webhook 端點
            TestWebhookEndpointRequest request = new TestWebhookEndpointRequest(URI.create(config.getWebhookUrl()));

            TestWebhookEndpointResponse testResult = client.testWebhookEndpoint(request).get().body();

            if (testResult.success()) {
                // 更新 Webhook 狀態為已驗證
                config.setWebhookStatus(Status.ENABLE);
                lineConfigMapper.updateLineConfig(config);

                log.info("Webhook 測試成功：endpoint={}, statusCode={}",
                        config.getWebhookUrl(), testResult.statusCode());

                return String.format("Webhook 測試成功！端點可正常接收訊息（狀態碼：%d）",
                        testResult.statusCode());
            } else {
                log.error("Webhook 測試失敗：reason={}, detail={}",
                        testResult.reason(), testResult.detail());

                return String.format("Webhook 測試失敗：%s - %s",
                        testResult.reason(), testResult.detail());
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Webhook 測試發生錯誤", e);
            throw new ServiceException("Webhook 測試失敗：" + e.getMessage());
        }
    }

    /**
     * 更新 Webhook URL
     *
     * @param configId   設定ID
     * @param webhookUrl Webhook URL
     * @return 結果
     */
    @Override
    @Transactional
    public int updateWebhookUrl(Integer configId, String webhookUrl) {
        LineConfig config = new LineConfig();
        config.setConfigId(configId);
        config.setWebhookUrl(webhookUrl);
        config.setWebhookStatus(Status.DISABLE); // 更新 URL 後重置驗證狀態
        return lineConfigMapper.updateLineConfig(config);
    }

    /**
     * 設定為預設頻道
     *
     * @param configId 設定ID
     * @return 結果
     */
    @Override
    @Transactional
    public int setAsDefaultChannel(Integer configId) {
        // 先清除其他頻道的預設狀態
        lineConfigMapper.clearDefaultStatus();
        // 設定當前頻道為預設
        return lineConfigMapper.setDefaultChannel(configId);
    }

    /**
     * 加密敏感資料
     *
     * @param config LINE 頻道設定
     */
    private void encryptSensitiveData(LineConfig config) {
        String key = JasyptUtils.KEY;
        if (StringUtils.isEmpty(key)) {
            throw new ServiceException("加密金鑰未設定，請設定 jasypt.encryptor.password 參數");
        }

        // 如果資料已經是加密格式（以 ENC( 開頭），則不重複加密
        if (StringUtils.isNotEmpty(config.getChannelId()) && !config.getChannelId().startsWith("ENC(")) {
            config.setChannelId(JasyptUtils.encryptVal(key, config.getChannelId()));
        }
        if (StringUtils.isNotEmpty(config.getChannelSecret()) && !config.getChannelSecret().startsWith("ENC(")) {
            config.setChannelSecret(JasyptUtils.encryptVal(key, config.getChannelSecret()));
        }
        if (StringUtils.isNotEmpty(config.getChannelAccessToken()) && !config.getChannelAccessToken().startsWith("ENC(")) {
            config.setChannelAccessToken(JasyptUtils.encryptVal(key, config.getChannelAccessToken()));
        }
    }

    /**
     * 解密敏感資料
     *
     * @param config LINE 頻道設定
     */
    private void decryptSensitiveData(LineConfig config) {
        String key = JasyptUtils.KEY;
        if (StringUtils.isEmpty(key)) {
            log.warn("加密金鑰未設定，無法解密敏感資料");
            return;
        }

        try {
            if (StringUtils.isNotEmpty(config.getChannelId())) {
                config.setChannelId(JasyptUtils.decryptVal(key, config.getChannelId()));
            }
            if (StringUtils.isNotEmpty(config.getChannelSecret())) {
                config.setChannelSecret(JasyptUtils.decryptVal(key, config.getChannelSecret()));
            }
            if (StringUtils.isNotEmpty(config.getChannelAccessToken())) {
                config.setChannelAccessToken(JasyptUtils.decryptVal(key, config.getChannelAccessToken()));
            }
        } catch (Exception e) {
            log.error("解密敏感資料失敗", e);
            throw new ServiceException("解密敏感資料失敗，請檢查加密金鑰是否正確");
        }
    }
}
