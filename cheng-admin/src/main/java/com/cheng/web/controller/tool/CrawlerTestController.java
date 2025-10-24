package com.cheng.web.controller.tool;

import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.R;
import com.cheng.crawler.dto.CrawlerParams;
import com.cheng.crawler.dto.CrawlerResult;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.handler.CrawlerHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 爬蟲測試 Controller
 * 提供爬蟲功能的測試介面，支援批次執行和單一查詢模式
 *
 * @author cheng
 */
@Slf4j
@Tag(name = "爬蟲測試管理", description = "提供各種爬蟲功能的測試介面")
@RestController
@RequestMapping("/test/crawler")
public class CrawlerTestController extends BaseController {

    /**
     * 取得所有可用的爬蟲類型
     */
    @Operation(
            summary = "取得爬蟲類型列表",
            description = "列出系統中所有可用的爬蟲類型及其說明"
    )
    @GetMapping("/types")
    public R<List<CrawlerTypeInfo>> getCrawlerTypes() {
        List<CrawlerTypeInfo> types = Arrays.stream(CrawlerType.values())
                .map(type -> {
                    CrawlerTypeInfo info = new CrawlerTypeInfo();
                    info.setCode(type.name());
                    info.setCategory(type.getCategory());
                    info.setFuncName(type.getFuncName());
                    return info;
                })
                .collect(Collectors.toList());
        return R.ok(types);
    }

    /**
     * 執行批次爬蟲（無參數）
     * 使用預設參數執行指定類型的爬蟲任務
     */
    @Operation(
            summary = "執行批次爬蟲（無參數）",
            description = "使用預設參數執行指定類型的爬蟲，適用於定期批次爬取資料的場景"
    )
    @PostMapping("/execute/{crawlerType}")
    public R<CrawlerResult> executeCrawler(
            @Parameter(description = "爬蟲類型代碼（CA101: ISBN書籍查詢, CA102: 臺灣證券交易所）", required = true)
            @PathVariable CrawlerType crawlerType) {

        try {
            log.info("開始執行爬蟲: {}", crawlerType.name());
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(crawlerType);

            if (handler == null) {
                return R.fail("找不到對應的爬蟲處理器: " + crawlerType.name());
            }

            CrawlerResult result = handler.execute();

            if (result.isSuccess()) {
                return R.ok(result, "爬蟲執行成功");
            } else {
                return R.fail(result.getMessage());
            }

        } catch (Exception e) {
            log.error("執行爬蟲失敗: {}", crawlerType.name(), e);
            return R.fail("執行爬蟲失敗: " + e.getMessage());
        }
    }

    /**
     * 執行批次爬蟲（帶參數）
     * 可自訂執行參數，適用於需要特定配置的爬取任務
     */
    @Operation(
            summary = "執行批次爬蟲（帶參數）",
            description = "使用自訂參數執行爬蟲，可設定模式、批次大小等進階選項"
    )
    @PostMapping("/execute/{crawlerType}/with-params")
    public R<CrawlerResult> executeCrawlerWithParams(
            @Parameter(description = "爬蟲類型代碼", required = true)
            @PathVariable CrawlerType crawlerType,
            @Parameter(description = "爬蟲執行參數", required = true)
            @RequestBody CrawlerParams params) {

        try {
            log.info("開始執行爬蟲: {}, 參數: {}", crawlerType.name(), params);
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(crawlerType);

            if (handler == null) {
                return R.fail("找不到對應的爬蟲處理器: " + crawlerType.name());
            }

            CrawlerResult result = handler.execute(params);

            if (result.isSuccess()) {
                return R.ok(result, "爬蟲執行成功");
            } else {
                return R.fail(result.getMessage());
            }

        } catch (Exception e) {
            log.error("執行爬蟲失敗: {}", crawlerType.name(), e);
            return R.fail("執行爬蟲失敗: " + e.getMessage());
        }
    }

    /**
     * 單一查詢爬蟲
     * 即時查詢並儲存單筆資料，適用於 API 即時查詢場景
     */
    @Operation(
            summary = "單一查詢爬蟲",
            description = "即時爬取並儲存單筆資料，例如查詢特定 ISBN 的書籍資訊"
    )
    @PostMapping("/query/{crawlerType}")
    public R<Object> querySingle(
            @Parameter(description = "爬蟲類型代碼", required = true)
            @PathVariable CrawlerType crawlerType,
            @Parameter(description = "查詢參數（例如：ISBN、股票代碼等）", required = true)
            @RequestBody QueryRequest request) {

        try {
            log.info("開始執行單一查詢: {}, 輸入: {}", crawlerType.name(), request.getInput());
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(crawlerType);

            if (handler == null) {
                return R.fail("找不到對應的爬蟲處理器: " + crawlerType.name());
            }

            // 呼叫 crawlSingleAndSave，回傳結果類型由爬蟲決定
            Object result = handler.crawlSingleAndSave(request.getInput(), Object.class);

            return R.ok(result, "查詢成功");

        } catch (UnsupportedOperationException e) {
            log.warn("爬蟲 {} 不支援單一查詢模式", crawlerType.name());
            return R.fail("此爬蟲不支援單一查詢模式，請使用批次執行");
        } catch (Exception e) {
            log.error("單一查詢失敗: {}", crawlerType.name(), e);
            return R.fail("查詢失敗: " + e.getMessage());
        }
    }

    /**
     * 測試所有爬蟲
     * 依序執行所有可用的爬蟲，用於整體功能測試
     */
    @Operation(
            summary = "測試所有爬蟲",
            description = "依序執行所有可用的爬蟲類型，用於系統整體測試"
    )
    @PostMapping("/execute-all")
    public R<Map<String, CrawlerResult>> executeAllCrawlers() {
        try {
            log.info("開始執行所有爬蟲");

            Map<String, CrawlerResult> results = Arrays.stream(CrawlerType.values())
                    .collect(Collectors.toMap(
                            CrawlerType::name,
                            type -> {
                                try {
                                    CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(type);
                                    if (handler != null) {
                                        return handler.execute();
                                    } else {
                                        CrawlerResult failResult = new CrawlerResult();
                                        failResult.setSuccess(false);
                                        failResult.setMessage("找不到對應的爬蟲處理器");
                                        return failResult;
                                    }
                                } catch (Exception e) {
                                    log.error("執行爬蟲 {} 失敗", type.name(), e);
                                    CrawlerResult errorResult = new CrawlerResult();
                                    errorResult.setSuccess(false);
                                    errorResult.setMessage("執行失敗: " + e.getMessage());
                                    return errorResult;
                                }
                            }
                    ));

            return R.ok(results, "批次執行完成");

        } catch (Exception e) {
            log.error("批次執行所有爬蟲失敗", e);
            return R.fail("批次執行失敗: " + e.getMessage());
        }
    }
}

/**
 * 爬蟲類型資訊
 */
@Getter
@Setter
@Schema(description = "爬蟲類型資訊")
class CrawlerTypeInfo {

    @Schema(description = "爬蟲類型代碼", example = "CA101")
    private String code;

    @Schema(description = "資料來源類別", example = "ISBN書籍查詢")
    private String category;

    @Schema(description = "功能名稱", example = "書籍庫存")
    private String funcName;
}

/**
 * 單一查詢請求
 */
@Getter
@Setter
@Schema(description = "單一查詢請求參數")
class QueryRequest {

    @Schema(description = "查詢輸入（例如：ISBN、股票代碼等）", example = "9789571372075", required = true)
    private String input;
}
