package com.cheng.quartz.task;

import com.cheng.crawler.handler.CrawlerHandler;
import com.cheng.crawler.dto.CrawlerParams;
import com.cheng.crawler.dto.CrawlerResult;
import com.cheng.crawler.enums.CrawlerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 爬蟲定時任務
 * <p>
 * 用於 Quartz 排程系統，支援從後台介面設定參數
 * <p>
 * <b>核心特色：</b>
 * <ul>
 *   <li>支援動態指定爬蟲類型（CA102, CA103, ...）</li>
 *   <li>支援靈活參數配置（模式、日期範圍、批次大小等）</li>
 *   <li>統一的錯誤處理和日誌記錄</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-03-28
 */
@Slf4j
@Component("crawlerTask")
public class CrawlerTask {

    /**
     * 執行爬蟲任務（無參數）
     * <p>
     * 使用預設參數執行指定類型的爬蟲
     * <p>
     * <b>Quartz 設定範例：</b>
     * <pre>
     * Bean名稱: crawlerTask
     * 方法名稱: run
     * 參數: CA102
     * </pre>
     *
     * @param crawlerType 爬蟲類型代碼（如：CA102, CA103）
     */
    public void run(String crawlerType) {
        log.info("開始執行爬蟲任務，類型: {}", crawlerType);

        CrawlerType type = parseCrawlerType(crawlerType);
        if (type == null) {
            log.error("無效的爬蟲類型: {}", crawlerType);
            return;
        }

        CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(type);
        if (handler != null) {
            CrawlerResult result = handler.execute();
            logResult(type, result);
        } else {
            log.error("找不到 {} 爬蟲處理器", type.name());
        }
    }

    /**
     * 執行爬蟲任務（帶模式參數）
     * <p>
     * 支援從後台設定模式參數
     * <p>
     * <b>Quartz 設定範例：</b>
     * <pre>
     * Bean名稱: crawlerTask
     * 方法名稱: runWithMode
     * 參數: CA102, today-only
     * </pre>
     *
     * @param crawlerType 爬蟲類型代碼（如：CA102, CA103）
     * @param mode        模式參數（如：today-only, full-sync）
     */
    public void runWithMode(String crawlerType, String mode) {
        log.info("開始執行爬蟲任務，類型: {}, 模式: {}", crawlerType, mode);

        CrawlerType type = parseCrawlerType(crawlerType);
        if (type == null) {
            log.error("無效的爬蟲類型: {}", crawlerType);
            return;
        }

        CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(type);
        if (handler != null) {
            // 建立參數
            CrawlerParams params = CrawlerParams.builder()
                    .mode(mode)
                    .build();

            CrawlerResult result = handler.execute(params);
            logResult(type, result);
        } else {
            log.error("找不到 {} 爬蟲處理器", type.name());
        }
    }

    /**
     * 執行爬蟲任務（完整參數）
     * <p>
     * 支援更詳細的參數設定
     * <p>
     * <b>Quartz 設定範例：</b>
     * <pre>
     * Bean名稱: crawlerTask
     * 方法名稱: runAdvanced
     * 參數: CA102, today-only, 500, 60000
     * </pre>
     *
     * @param crawlerType 爬蟲類型代碼（如：CA102, CA103）
     * @param mode        模式參數（如：today-only, full-sync）
     * @param batchSize   批次大小（每批處理幾筆）
     * @param timeout     超時時間（毫秒）
     */
    public void runAdvanced(String crawlerType, String mode, Integer batchSize, Long timeout) {
        log.info("開始執行爬蟲任務，類型: {}, 模式: {}, 批次大小: {}, 超時: {}ms",
                crawlerType, mode, batchSize, timeout);

        CrawlerType type = parseCrawlerType(crawlerType);
        if (type == null) {
            log.error("無效的爬蟲類型: {}", crawlerType);
            return;
        }

        CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(type);
        if (handler != null) {
            // 建立詳細參數
            CrawlerParams params = CrawlerParams.builder()
                    .mode(mode)
                    .batchSize(batchSize)
                    .timeout(timeout)
                    .build();

            CrawlerResult result = handler.execute(params);
            logResult(type, result);
        } else {
            log.error("找不到 {} 爬蟲處理器", type.name());
        }
    }

    /**
     * 執行爬蟲任務（自訂日期範圍）
     * <p>
     * 支援設定日期範圍
     * <p>
     * <b>Quartz 設定範例：</b>
     * <pre>
     * Bean名稱: crawlerTask
     * 方法名稱: runDateRange
     * 參數: CA102, 2025-03-01, 2025-03-28
     * </pre>
     *
     * @param crawlerType 爬蟲類型代碼（如：CA102, CA103）
     * @param startDate   開始日期（格式：yyyy-MM-dd）
     * @param endDate     結束日期（格式：yyyy-MM-dd）
     */
    public void runDateRange(String crawlerType, String startDate, String endDate) {
        log.info("開始執行爬蟲任務，類型: {}, 日期範圍: {} 至 {}", crawlerType, startDate, endDate);

        CrawlerType type = parseCrawlerType(crawlerType);
        if (type == null) {
            log.error("無效的爬蟲類型: {}", crawlerType);
            return;
        }

        CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(type);
        if (handler != null) {
            // 建立日期範圍參數
            CrawlerParams params = CrawlerParams.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            CrawlerResult result = handler.execute(params);
            logResult(type, result);
        } else {
            log.error("找不到 {} 爬蟲處理器", type.name());
        }
    }

    /**
     * 執行爬蟲任務（完整配置）
     * <p>
     * 支援所有參數的完整配置
     * <p>
     * <b>Quartz 設定範例：</b>
     * <pre>
     * Bean名稱: crawlerTask
     * 方法名稱: runFull
     * 參數: CA102, full-sync, 2025-03-01, 2025-03-28, 1000, 120000
     * </pre>
     *
     * @param crawlerType 爬蟲類型代碼（如：CA102, CA103）
     * @param mode        模式參數（如：today-only, full-sync）
     * @param startDate   開始日期（格式：yyyy-MM-dd）
     * @param endDate     結束日期（格式：yyyy-MM-dd）
     * @param batchSize   批次大小（每批處理幾筆）
     * @param timeout     超時時間（毫秒）
     */
    public void runFull(String crawlerType, String mode, String startDate, String endDate,
                        Integer batchSize, Long timeout) {
        log.info("開始執行爬蟲任務，類型: {}, 模式: {}, 日期: {}-{}, 批次: {}, 超時: {}ms",
                crawlerType, mode, startDate, endDate, batchSize, timeout);

        CrawlerType type = parseCrawlerType(crawlerType);
        if (type == null) {
            log.error("無效的爬蟲類型: {}", crawlerType);
            return;
        }

        CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(type);
        if (handler != null) {
            // 建立完整參數
            CrawlerParams params = CrawlerParams.builder()
                    .mode(mode)
                    .startDate(startDate)
                    .endDate(endDate)
                    .batchSize(batchSize)
                    .timeout(timeout)
                    .build();

            CrawlerResult result = handler.execute(params);
            logResult(type, result);
        } else {
            log.error("找不到 {} 爬蟲處理器", type.name());
        }
    }

    /**
     * 解析爬蟲類型
     *
     * @param crawlerType 爬蟲類型代碼字串
     * @return CrawlerType，若無效則返回 null
     */
    private CrawlerType parseCrawlerType(String crawlerType) {
        if (crawlerType == null || crawlerType.trim().isEmpty()) {
            return null;
        }
        return CrawlerType.of(crawlerType.trim());
    }

    /**
     * 記錄執行結果
     *
     * @param type   爬蟲類型
     * @param result 執行結果
     */
    private void logResult(CrawlerType type, CrawlerResult result) {
        if (result.isSuccess()) {
            log.info("[{}] 爬蟲任務執行成功 - " +
                            "爬取: {} 筆, 處理: {} 筆, 儲存: {} 筆, 耗時: {} ms",
                    type.name(),
                    result.getRawDataCount(),
                    result.getProcessedDataCount(),
                    result.getSavedDataCount(),
                    result.getDurationMs());
        } else {
            log.error("[{}] 爬蟲任務執行失敗: {}", type.name(), result.getMessage());
            if (result.getError() != null) {
                log.error("[{}] 錯誤詳情", type.name(), result.getError());
            }
        }
    }
}
