package com.cheng.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 日誌系統啟動監聽器
 * 在應用程式啟動時顯示日誌配置資訊
 *
 * @author cheng
 * @since 2025-11-02
 */
@Slf4j
@Component
public class LoggingStartupListener implements ApplicationRunner {

    @Value("${logging.file.path:/tmp/logs}")
    private String logPath;

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Override
    public void run(ApplicationArguments args) {
        log.info("================================================================================");
        log.info("                    LOG 系統配置資訊                                            ");
        log.info("================================================================================");
        log.info("應用程式名稱: {}", applicationName);
        log.info("啟用環境: {}", activeProfile);
        log.info("日誌根目錄: {}", logPath);
        log.info("當日日誌目錄: {}/current", logPath);
        log.info("歷史日誌目錄: {}/archive", logPath);
        log.info("--------------------------------------------------------------------------------");
        log.info("日誌檔案列表:");
        log.info("  - INFO 日誌: {}/current/sys-info.log", logPath);
        log.info("  - ERROR 日誌: {}/current/sys-error.log", logPath);
        log.info("  - USER 日誌: {}/current/sys-user.log", logPath);
        log.info("--------------------------------------------------------------------------------");

        // 檢查目錄是否存在
        File currentDir = new File(logPath + "/current");
        File archiveDir = new File(logPath + "/archive");

        if (currentDir.exists()) {
            log.info("==>當日日誌目錄已存在");
        } else {
            log.warn("--->當日日誌目錄不存在，將自動建立: {}", currentDir.getAbsolutePath());
            if (currentDir.mkdirs()) {
                log.info("==>成功建立當日日誌目錄");
            }
        }

        if (archiveDir.exists()) {
            log.info("==>歷史日誌目錄已存在");
        } else {
            log.warn("--->歷史日誌目錄不存在，將自動建立: {}", archiveDir.getAbsolutePath());
            if (archiveDir.mkdirs()) {
                log.info("==>成功建立歷史日誌目錄");
            }
        }

        log.info("--------------------------------------------------------------------------------");
        log.info("日誌輪轉策略:");
        log.info("  - 輪轉週期: 每日凌晨自動輪轉");
        log.info("  - 壓縮格式: .gz 壓縮");
        log.info("  - 保留天數: 60 天");
        log.info("================================================================================");
        log.info("LOG 系統初始化完成");
        log.info("================================================================================");
    }
}
