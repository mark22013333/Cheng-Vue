package com.cheng.line.config;

import com.cheng.common.enums.Status;
import com.cheng.common.enums.YesNo;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.enums.ChannelType;
import com.cheng.line.service.ILineConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * LINE 主頻道初始化器
 * 應用啟動時自動載入主頻道設定到資料庫
 *
 * @author cheng
 */
@Slf4j
@Component
public class LineMainChannelInitializer implements ApplicationRunner {

    @Resource
    private LineMainChannelProperties mainChannelProperties;

    @Resource
    private ILineConfigService lineConfigService;

    @Resource
    private Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        // 檢查是否啟用主頻道自動載入
        if (!mainChannelProperties.getEnabled()) {
            log.info("LINE 主頻道自動載入已停用");
            return;
        }

        // 檢查配置是否完整
        if (!mainChannelProperties.isValid()) {
            log.warn("LINE 主頻道配置不完整，跳過自動載入");
            log.warn("請在 application-{}.yml 中完整設定 line.bot 配置（包含擴充參數：enabled, channel-name, channel-id, bot-basic-id）",
                    environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "prod");
            return;
        }

        try {
            log.info("開始載入 LINE 主頻道設定...");

            // 先檢查資料庫是否已存在主頻道（不限 Bot Basic ID）
            LineConfig mainChannel = lineConfigService.selectLineConfigByChannelType(ChannelType.MAIN);

            if (mainChannel != null) {
                log.info("資料庫已存在主頻道設定（Bot Basic ID: {}），跳過自動配置以避免覆蓋後台管理者的設定", 
                        mainChannel.getBotBasicId());
                log.info("如需使用配置檔案的設定，請先在後台刪除現有的主頻道，再重新啟動服務");
            } else {
                // 資料庫沒有主頻道，建立新的
                log.info("資料庫尚無主頻道設定，從配置檔案建立（Bot Basic ID: {}）", mainChannelProperties.getBotBasicId());
                createMainChannel();
                log.info("LINE 主頻道設定建立完成");
            }
        } catch (Exception e) {
            log.error("載入 LINE 主頻道設定失敗", e);
        }
    }

    /**
     * 建立主頻道
     */
    private void createMainChannel() {
        LineConfig config = new LineConfig();
        config.setChannelType(ChannelType.MAIN);
        config.setChannelName(mainChannelProperties.getChannelName());
        config.setChannelId(mainChannelProperties.getChannelId());
        config.setBotBasicId(mainChannelProperties.getBotBasicId());
        config.setChannelSecret(mainChannelProperties.getChannelSecret());
        config.setChannelAccessToken(mainChannelProperties.getAccessToken());
        config.setStatus(Status.ENABLE);
        config.setIsDefault(YesNo.YES);
        config.setSortOrder(0);
        config.setCreateBy("system");
        config.setRemark("系統啟動時自動建立的主頻道");

        // 產生 Webhook URL
        String webhookUrl = generateWebhookUrl(mainChannelProperties.getBotBasicId());
        config.setWebhookUrl(webhookUrl);

        lineConfigService.insertLineConfig(config);
        log.info("主頻道建立成功：{}", config.getChannelName());
    }

    /**
     * 產生 Webhook URL
     * 格式：http(s)://domain/webhook/line/{botBasicId}
     *
     * @param botBasicId Bot Basic ID
     * @return Webhook URL
     */
    private String generateWebhookUrl(String botBasicId) {
        // 從環境變數或配置中取得域名
        String serverUrl = environment.getProperty("server.webhook.base-url");

        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            // 如果沒有配置，使用預設值
            String serverPort = environment.getProperty("server.port", "8080");
            serverUrl = "http://localhost:" + serverPort;
            log.warn("未設定 server.webhook.base-url，使用預設值: {}", serverUrl);
        }

        // 移除結尾的斜線
        if (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }

        return String.format("%s/%s/%s", serverUrl, ILineConfigService.WEBHOOK, botBasicId);
    }
}
