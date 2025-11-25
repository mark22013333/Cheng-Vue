package com.cheng.line.service.impl;

import com.cheng.common.constant.UserConstants;
import com.cheng.common.enums.Status;
import com.cheng.common.enums.YesNo;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.JasyptUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.client.LineClientFactory;
import com.cheng.line.config.LineProperties;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.enums.ChannelType;
import com.cheng.line.mapper.LineConfigMapper;
import com.cheng.line.service.ILineConfigService;
import com.cheng.line.vo.ConnectionTestVO;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.SetWebhookEndpointRequest;
import com.linecorp.bot.messaging.model.TestWebhookEndpointRequest;
import com.linecorp.bot.messaging.model.TestWebhookEndpointResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
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

    @Autowired
    private LineProperties lineProperties;

    @Autowired
    private LineClientFactory lineClientFactory;

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
            // 解密敏感資料 - 已停用，改為明碼存儲
            // decryptSensitiveData(config);
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
        LineConfig config = lineConfigMapper.selectLineConfigByType(channelType.getCode());
        if (config != null) {
            // decryptSensitiveData(config);  // 已停用，改為明碼存儲
        }
        return config;
    }

    /**
     * 根據 Bot Basic ID 查詢 LINE 頻道設定
     *
     * @param botBasicId Bot Basic ID（例如：@322okyxf）
     * @return LINE 頻道設定
     */
    @Override
    public LineConfig selectLineConfigByBotBasicId(String botBasicId) {
        LineConfig config = lineConfigMapper.selectLineConfigByBotBasicId(botBasicId);
        if (config != null) {
            // decryptSensitiveData(config);  // 已停用，改為明碼存儲
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
            // decryptSensitiveData(config);  // 已停用，改為明碼存儲
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
        // 列表查詢時對敏感資料進行脫敏處理
        list.forEach(this::maskSensitiveData);
        return list;
    }

    /**
     * 查詢所有啟用的頻道設定
     *
     * @return LINE 頻道設定集合
     */
    @Override
    public List<LineConfig> selectEnabledLineConfigs() {
        List<LineConfig> list = lineConfigMapper.selectEnabledLineConfigs();
        // 列表查詢時對敏感資料進行脫敏處理
        list.forEach(this::maskSensitiveData);
        return list;
    }

    /**
     * 根據頻道類型查詢頻道設定
     *
     * @param channelType 頻道類型代碼
     * @return LINE 頻道設定
     */
    @Override
    public LineConfig selectLineConfigByType(String channelType) {
        LineConfig config = lineConfigMapper.selectLineConfigByType(channelType);
        if (config != null) {
            // 查詢詳情時解密敏感資料 - 已停用，改為明碼存儲
            // decryptSensitiveData(config);
        }
        return config;
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

        // 加密敏感資料 - 已停用，改為明碼存儲
        // encryptSensitiveData(lineConfig);

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

        // 產生 Webhook URL
        String webhookUrl = generateWebhookUrl(lineConfig);
        lineConfig.setWebhookUrl(webhookUrl);

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

        // 如果 Access Token 有變更，清除舊的 Client 快取
        if (!oldConfig.getChannelAccessToken().equals(lineConfig.getChannelAccessToken())) {
            lineClientFactory.removeClient(oldConfig.getChannelAccessToken());
        }

        // 如果頻道類型有變更，檢查新的類型是否唯一
        if (!oldConfig.getChannelType().equals(lineConfig.getChannelType())) {
            if (!checkChannelTypeUnique(lineConfig)) {
                throw new ServiceException("頻道類型已存在，無法修改");
            }
        }

        // 加密敏感資料 - 已停用，改為明碼存儲
        // encryptSensitiveData(lineConfig);

        // 如果設定為預設頻道，需要先清除其他頻道的預設狀態
        if (YesNo.YES.equals(lineConfig.getIsDefault())) {
            lineConfigMapper.clearDefaultStatus();
        }

        // 重新產生 Webhook URL（因為可能更新了 webhookBaseUrl 或 botBasicId）
        String webhookUrl = generateWebhookUrl(lineConfig);
        
        // 只有在 Webhook URL 改變時，才重置驗證狀態
        if (!webhookUrl.equals(oldConfig.getWebhookUrl())) {
            lineConfig.setWebhookStatus(Status.DISABLE);
            log.info("Webhook URL 已變更，重置驗證狀態為未驗證（configId={}）", lineConfig.getConfigId());
        }
        // 否則保留原有的驗證狀態（不覆蓋）
        
        lineConfig.setWebhookUrl(webhookUrl);

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
            // 清除對應的 Client 快取
            if (config != null) {
                lineClientFactory.removeClient(config.getChannelAccessToken());
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
        // 清除對應的 Client 快取
        if (config != null) {
            lineClientFactory.removeClient(config.getChannelAccessToken());
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

        LineConfig info = lineConfigMapper.selectLineConfigByType(lineConfig.getChannelType().getCode());
        if (StringUtils.isNotNull(info) && !info.getConfigId().equals(configId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 測試 LINE 頻道連線（完整測試：API、Webhook、Bot 資訊）
     *
     * @param configId 設定ID
     * @return 測試結果
     */
    @Override
    public ConnectionTestVO testLineConnection(Integer configId) {
        LineConfig config = selectLineConfigById(configId);
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        ConnectionTestVO.TestItemVO apiResult;
        ConnectionTestVO.TestItemVO webhookResult;
        ConnectionTestVO.TestItemVO botResult;

        try {
            // 取得 LINE Messaging API Client（復用快取）
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            // 1. API 連線測試
            try {
                client.getBotInfo().get().body();
                apiResult = ConnectionTestVO.TestItemVO.builder()
                        .success(true)
                        .message("API 連線正常，Channel Access Token 有效")
                        .build();
            } catch (Exception e) {
                apiResult = ConnectionTestVO.TestItemVO.builder()
                        .success(false)
                        .message("API 連線失敗：" + e.getMessage())
                        .build();
            }

            // 2. Webhook 設定檢查（真正測試 Webhook 端點）
            if (StringUtils.isEmpty(config.getWebhookUrl())) {
                webhookResult = ConnectionTestVO.TestItemVO.builder()
                        .success(false)
                        .message("Webhook URL 未設定")
                        .build();
            } else {
                try {
                    // 呼叫 LINE API 測試 Webhook 端點
                    TestWebhookEndpointRequest webhookRequest = new TestWebhookEndpointRequest(
                            URI.create(config.getWebhookUrl())
                    );
                    TestWebhookEndpointResponse testResult = client.testWebhookEndpoint(webhookRequest).get().body();
                    
                    if (testResult.success()) {
                        webhookResult = ConnectionTestVO.TestItemVO.builder()
                                .success(true)
                                .message(String.format("Webhook URL 已設定：%s", config.getWebhookUrl()))
                                .build();
                        
                        log.info("Webhook 測試成功：url={}, statusCode={}", 
                                config.getWebhookUrl(), testResult.statusCode());
                    } else {
                        webhookResult = ConnectionTestVO.TestItemVO.builder()
                                .success(false)
                                .message(String.format("Webhook 測試失敗：%s - %s", 
                                        testResult.reason(), testResult.detail()))
                                .build();
                        
                        log.warn("Webhook 測試失敗：reason={}, detail={}", 
                                testResult.reason(), testResult.detail());
                    }
                } catch (Exception e) {
                    webhookResult = ConnectionTestVO.TestItemVO.builder()
                            .success(false)
                            .message("Webhook 測試失敗：" + e.getMessage())
                            .build();
                    
                    log.error("Webhook 測試發生錯誤", e);
                }
            }

            // 3. Bot 資訊取得
            try {
                var botInfo = client.getBotInfo().get().body();

                ConnectionTestVO.BotDataVO botData = ConnectionTestVO.BotDataVO.builder()
                        .displayName(botInfo.displayName())
                        .statusMessage(botInfo.chatMode() != null ? botInfo.chatMode().toString() : null)
                        .pictureUrl(botInfo.pictureUrl() != null ? botInfo.pictureUrl().toString() : null)
                        .build();

                botResult = ConnectionTestVO.TestItemVO.builder()
                        .success(true)
                        .message("Bot 資訊取得成功")
                        .data(botData)
                        .build();

                log.info("Bot 資訊取得成功：userId={}, displayName={}",
                        botInfo.userId(), botInfo.displayName());
            } catch (Exception e) {
                botResult = ConnectionTestVO.TestItemVO.builder()
                        .success(false)
                        .message("Bot 資訊取得失敗：" + e.getMessage())
                        .build();
            }

        } catch (Exception e) {
            log.error("LINE 連線測試失敗", e);
            throw new ServiceException("連線測試失敗：" + e.getMessage());
        }

        // 測試完成後，根據 Webhook 測試結果更新資料庫狀態
        log.info("=== 準備更新 Webhook 狀態到資料庫 ===");
        log.info("webhookResult.success = {}", webhookResult.getSuccess());
        
        if (Boolean.TRUE.equals(webhookResult.getSuccess())) {
            int updateCount = lineConfigMapper.updateWebhookStatus(configId, Status.ENABLE.getCode());
            log.info("Webhook 測試成功，已更新資料庫狀態為已驗證（configId={}，更新筆數={}）", configId, updateCount);
        } else {
            int updateCount = lineConfigMapper.updateWebhookStatus(configId, Status.DISABLE.getCode());
            log.warn("Webhook 測試失敗，已更新資料庫狀態為未驗證（configId={}，更新筆數={}）", configId, updateCount);
        }
        log.info("=== Webhook 狀態更新完成 ===");

        return ConnectionTestVO.builder()
                .api(apiResult)
                .webhook(webhookResult)
                .bot(botResult)
                .build();
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
            // 取得 LINE Messaging API Client（復用快取）
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

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
     * 自動判斷資料是否已加密，避免重複加密
     *
     * @param config LINE 頻道設定
     */
    private void encryptSensitiveData(LineConfig config) {
        String key = JasyptUtils.KEY;
        if (StringUtils.isEmpty(key)) {
            throw new ServiceException("加密金鑰未設定，請設定 jasypt.encryptor.password 參數");
        }

        // 加密 Channel ID（先嘗試解密，如果成功則說明已加密，不需要再加密）
        if (StringUtils.isNotEmpty(config.getChannelId())) {
            if (!isEncrypted(key, config.getChannelId())) {
                config.setChannelId(JasyptUtils.encryptVal(key, config.getChannelId()));
            }
        }
        
        // 加密 Channel Secret
        if (StringUtils.isNotEmpty(config.getChannelSecret())) {
            if (!isEncrypted(key, config.getChannelSecret())) {
                config.setChannelSecret(JasyptUtils.encryptVal(key, config.getChannelSecret()));
            }
        }
        
        // 加密 Channel Access Token
        if (StringUtils.isNotEmpty(config.getChannelAccessToken())) {
            if (!isEncrypted(key, config.getChannelAccessToken())) {
                config.setChannelAccessToken(JasyptUtils.encryptVal(key, config.getChannelAccessToken()));
            }
        }
    }
    
    /**
     * 判斷字串是否已加密
     * 透過嘗試解密來判斷，如果解密成功則說明已加密
     *
     * @param key 加密金鑰
     * @param value 待檢查的值
     * @return true=已加密, false=未加密
     */
    private boolean isEncrypted(String key, String value) {
        try {
            JasyptUtils.decryptVal(key, value);
            return true;  // 解密成功，說明是加密資料
        } catch (Exception e) {
            return false;  // 解密失敗，說明是明碼
        }
    }

    /**
     * 解密敏感資料
     * 對於明碼資料（解密失敗），會記錄警告但不拋出異常，允許系統繼續執行
     *
     * @param config LINE 頻道設定
     */
    private void decryptSensitiveData(LineConfig config) {
        String key = JasyptUtils.KEY;
        if (StringUtils.isEmpty(key)) {
            log.warn("加密金鑰未設定，無法解密敏感資料");
            return;
        }

        // 解密 Channel ID
        if (StringUtils.isNotEmpty(config.getChannelId())) {
            try {
                config.setChannelId(JasyptUtils.decryptVal(key, config.getChannelId()));
            } catch (Exception e) {
                log.warn("Channel ID 解密失敗，可能是明碼資料（configId={}），建議重新儲存以加密", config.getConfigId());
                // 不拋出異常，保留明碼資料，允許系統繼續執行
            }
        }
        
        // 解密 Channel Secret
        if (StringUtils.isNotEmpty(config.getChannelSecret())) {
            try {
                config.setChannelSecret(JasyptUtils.decryptVal(key, config.getChannelSecret()));
            } catch (Exception e) {
                log.warn("Channel Secret 解密失敗，可能是明碼資料（configId={}），建議重新儲存以加密", config.getConfigId());
            }
        }
        
        // 解密 Channel Access Token
        if (StringUtils.isNotEmpty(config.getChannelAccessToken())) {
            try {
                config.setChannelAccessToken(JasyptUtils.decryptVal(key, config.getChannelAccessToken()));
            } catch (Exception e) {
                log.warn("Channel Access Token 解密失敗，可能是明碼資料（configId={}），建議重新儲存以加密", config.getConfigId());
            }
        }
    }

    /**
     * 脫敏敏感資料（用於列表顯示）
     * 顯示前3碼和後3碼，中間用 * 隱藏
     *
     * @param config LINE 頻道設定
     */
    private void maskSensitiveData(LineConfig config) {
        if (StringUtils.isNotEmpty(config.getChannelId())) {
            config.setChannelId(maskString(config.getChannelId()));
        }
        if (StringUtils.isNotEmpty(config.getChannelSecret())) {
            config.setChannelSecret(maskString(config.getChannelSecret()));
        }
        if (StringUtils.isNotEmpty(config.getChannelAccessToken())) {
            config.setChannelAccessToken(maskString(config.getChannelAccessToken()));
        }
    }

    /**
     * 字串脫敏處理
     * 顯示前3碼和後3碼，中間用 * 隱藏
     *
     * @param str 原始字串
     * @return 脫敏後的字串
     */
    private String maskString(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        // 已停用加密功能，直接使用明碼進行脫敏
        // 如果是加密格式（ENC(...)），先解密再脫敏
        // String plainText = str;
        // if (str.startsWith("ENC(")) {
        //     try {
        //         String key = JasyptUtils.KEY;
        //         if (StringUtils.isNotEmpty(key)) {
        //             plainText = JasyptUtils.decryptVal(key, str);
        //         }
        //     } catch (Exception e) {
        //         log.warn("脫敏時解密失敗，直接使用原始值", e);
        //     }
        // }

        int length = str.length();
        if (length <= 6) {
            // 字串太短，全部用 * 隱藏
            return "*".repeat(length);
        }

        // 顯示前3碼和後3碼
        String prefix = str.substring(0, 3);
        String suffix = str.substring(length - 3);
        int maskLength = length - 6;

        return prefix + "*".repeat(maskLength) + suffix;
    }

    /**
     * 取得系統預設的 Webhook 基礎 URL
     *
     * @return 預設 Webhook 基礎 URL
     */
    @Override
    public String getDefaultWebhookBaseUrl() {
        return lineProperties.getWebhookBaseUrl();
    }

    /**
     * 產生 Webhook URL
     * 規則：
     * 1. 如果頻道設定了自訂的 webhookBaseUrl，使用自訂值
     * 2. 否則使用系統配置的 line.webhook.base-url
     * 3. 路徑格式：/webhook/line/{botBasicId}
     *
     * @param config LINE 頻道設定
     * @return 產生的 Webhook URL
     */
    private String generateWebhookUrl(LineConfig config) {
        // 決定使用哪個 Base URL
        String baseUrl = StringUtils.isNotEmpty(config.getWebhookBaseUrl())
                ? config.getWebhookBaseUrl()
                : lineProperties.getWebhookBaseUrl();

        // 確保 Base URL 包含協定（預設為 https://）
        if (!baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        // 移除 Base URL 結尾的斜線
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 取得 Bot Basic ID 或 Channel ID 作為路徑參數
        String pathParam = StringUtils.isNotEmpty(config.getBotBasicId())
                ? config.getBotBasicId()
                : config.getChannelId();

        // 移除 Bot Basic ID 開頭的 @ 符號（如果有）
        if (pathParam.startsWith("@")) {
            pathParam = pathParam.substring(1);
        }

        // 組合完整的 Webhook URL
        return String.format("%s/webhook/line/%s", baseUrl, pathParam);
    }

    /**
     * 設定 LINE Webhook 端點
     * 使用 LINE Messaging API 的 Set webhook endpoint URL API
     * <a href="https://developers.line.biz/en/reference/messaging-api/#set-webhook-endpoint-url">
     * https://developers.line.biz/en/reference/messaging-api/#set-webhook-endpoint-url
     * </a>
     *
     * @param configId 設定ID
     */
    @Override
    public void setLineWebhookEndpoint(Integer configId) {
        log.info("開始設定 LINE Webhook 端點，設定ID：{}", configId);

        // 1. 查詢頻道設定
        LineConfig config = selectLineConfigById(configId);
        if (config == null) {
            throw new ServiceException("找不到對應的頻道設定");
        }

        // 2. 檢查頻道是否啟用
        if (config.getStatus() != Status.ENABLE) {
            throw new ServiceException("頻道未啟用，無法設定 Webhook");
        }

        // 3. 取得 Webhook URL
        String webhookUrl = config.getWebhookUrl();
        if (StringUtils.isEmpty(webhookUrl)) {
            throw new ServiceException("Webhook URL 為空，請先儲存頻道設定");
        }

        // 4. 取得 MessagingApiClient（復用快取）
        MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

        try {
            // 5. 呼叫 LINE API 設定 Webhook URL
            // PUT https://api.line.me/v2/bot/channel/webhook/endpoint
            SetWebhookEndpointRequest request = new SetWebhookEndpointRequest(URI.create(webhookUrl));
            client.setWebhookEndpoint(request).get();  // 同步等待結果

            log.info("設定 LINE Webhook 端點成功，URL：{}", webhookUrl);

            // 6. 更新資料庫狀態為成功
            config.setWebhookStatus(Status.ENABLE);
            lineConfigMapper.updateLineConfig(config);

        } catch (ExecutionException | InterruptedException e) {
            log.error("設定 LINE Webhook 端點失敗", e);

            // 更新資料庫狀態為失敗
            config.setWebhookStatus(Status.DISABLE);
            lineConfigMapper.updateLineConfig(config);

            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new ServiceException("設定 Webhook 失敗：" + errorMsg);
        }
    }

    /**
     * 設定 LINE Webhook 端點（使用表單當前值）
     * 使用傳入的參數直接呼叫 LINE API，不從資料庫讀取
     *
     * @param webhookUrl         Webhook URL
     * @param channelAccessToken Channel Access Token
     * @param configId           設定ID（可選，用於更新資料庫狀態）
     */
    @Override
    public void setLineWebhookEndpointWithParams(String webhookUrl, String channelAccessToken, Integer configId) {
        log.info("開始設定 LINE Webhook 端點（使用表單值），Webhook URL：{}", webhookUrl);

        // 驗證參數
        if (StringUtils.isEmpty(webhookUrl)) {
            throw new ServiceException("Webhook URL 為空");
        }
        if (StringUtils.isEmpty(channelAccessToken)) {
            throw new ServiceException("Channel Access Token 為空");
        }

        // 取得 MessagingApiClient（使用傳入的 Token）
        MessagingApiClient client = lineClientFactory.getClient(channelAccessToken);

        try {
            // 呼叫 LINE API 設定 Webhook URL
            SetWebhookEndpointRequest request = new SetWebhookEndpointRequest(URI.create(webhookUrl));
            client.setWebhookEndpoint(request).get();  // 同步等待結果

            log.info("設定 LINE Webhook 端點成功，URL：{}", webhookUrl);

            // 如果有 configId，更新資料庫狀態
            if (configId != null) {
                LineConfig config = lineConfigMapper.selectLineConfigById(configId);
                if (config != null) {
                    config.setWebhookStatus(Status.ENABLE);
                    lineConfigMapper.updateLineConfig(config);
                    log.info("已更新資料庫 Webhook 狀態為成功（configId={}）", configId);
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            log.error("設定 LINE Webhook 端點失敗", e);

            // 如果有 configId，更新資料庫狀態為失敗
            if (configId != null) {
                LineConfig config = lineConfigMapper.selectLineConfigById(configId);
                if (config != null) {
                    config.setWebhookStatus(Status.DISABLE);
                    lineConfigMapper.updateLineConfig(config);
                }
            }

            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new ServiceException("設定 Webhook 失敗：" + errorMsg);
        }
    }
}
