package com.cheng.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.cheng.framework.config.Color.*;

/**
 * 日誌系統啟動監聽器
 * 在應用程式啟動時顯示日誌配置資訊
 *
 * @author cheng
 * @since 2025-11-02
 */
@Slf4j
@Component
@Order(2) // 在 Git Hooks 安裝後執行
public class LoggingStartupListener implements ApplicationRunner {

    @Value("${logging.file.path:/tmp/logs}")
    private String logPath;

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Override
    public void run(ApplicationArguments args) {
        log.info("{}════════════════════════════════════════════════════{}", ANSI_YELLOW, ANSI_RESET);
        log.info("{}  LOG 系統配置資訊{}", ANSI_YELLOW, ANSI_RESET);
        log.info("{}════════════════════════════════════════════════════{}", ANSI_YELLOW, ANSI_RESET);
        log.info("");
        log.info("{}應用程式名稱:{} {}", ANSI_BLUE, ANSI_RESET, applicationName);
        log.info("{}啟用環境:{} {}", ANSI_BLUE, ANSI_RESET, activeProfile);
        log.info("{}日誌根目錄:{} {}", ANSI_BLUE, ANSI_RESET, logPath);
        log.info("");
        log.info("{}目錄結構：{}", ANSI_CYAN, ANSI_RESET);
        log.info("  - 當日日誌: {}/current", logPath);
        log.info("  - 歷史日誌: {}/archive", logPath);
        log.info("");
        log.info("{}日誌檔案：{}", ANSI_CYAN, ANSI_RESET);
        log.info("  - INFO 日誌:  sys-info.log");
        log.info("  - ERROR 日誌: sys-error.log");
        log.info("  - USER 日誌:  sys-user.log");
        log.info("");

        // 檢查目錄是否存在
        File currentDir = new File(logPath + "/current");
        File archiveDir = new File(logPath + "/archive");

        log.info("{}目錄狀態檢查：{}", ANSI_CYAN, ANSI_RESET);
        if (currentDir.exists()) {
            log.info("{}✓{} 當日日誌目錄已存在", ANSI_GREEN, ANSI_RESET);
        } else {
            log.warn("{}!{} 當日日誌目錄不存在，將自動建立", ANSI_YELLOW, ANSI_RESET);
            if (currentDir.mkdirs()) {
                log.info("{}✓{} 成功建立當日日誌目錄", ANSI_GREEN, ANSI_RESET);
            } else {
                log.error("{}✗{} 建立當日日誌目錄失敗", ANSI_RED, ANSI_RESET);
            }
        }

        if (archiveDir.exists()) {
            log.info("{}✓{} 歷史日誌目錄已存在", ANSI_GREEN, ANSI_RESET);
        } else {
            log.warn("{}!{} 歷史日誌目錄不存在，將自動建立", ANSI_YELLOW, ANSI_RESET);
            if (archiveDir.mkdirs()) {
                log.info("{}✓{} 成功建立歷史日誌目錄", ANSI_GREEN, ANSI_RESET);
            } else {
                log.error("{}✗{} 建立歷史日誌目錄失敗", ANSI_RED, ANSI_RESET);
            }
        }

        log.info("");
        log.info("{}日誌輪轉策略：{}", ANSI_CYAN, ANSI_RESET);
        log.info("  - 輪轉週期: 每日凌晨自動輪轉");
        log.info("  - 壓縮格式: .gz 壓縮");
        log.info("  - 保留天數: 60 天");
        log.info("");
        log.info("{}════════════════════════════════════════════════════{}", ANSI_GREEN, ANSI_RESET);
        log.info("{}  LOG 系統初始化完成{}", ANSI_GREEN, ANSI_RESET);
        log.info("{}════════════════════════════════════════════════════{}", ANSI_GREEN, ANSI_RESET);
    }
}
