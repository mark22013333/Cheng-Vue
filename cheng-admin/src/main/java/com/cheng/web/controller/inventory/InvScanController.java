package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.enums.ScanResult;
import com.cheng.common.enums.ScanType;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.crawler.handler.CrawlerHandler;
import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.system.domain.InvBookInfo;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvScanLog;
import com.cheng.system.dto.InvItemWithStockDTO;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.service.IBookItemService;
import com.cheng.system.service.IInvBookInfoService;
import com.cheng.system.service.IInvScanLogService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 掃描處理控制器
 *
 * @author cheng
 */
@Validated
@RestController
@RequestMapping("/inventory/scan")
@RequiredArgsConstructor
public class InvScanController extends BaseController {

    private final IInvScanLogService invScanLogService;
    private final IBookItemService bookItemService;
    private final InvItemMapper invItemMapper;
    private final IInvBookInfoService invBookInfoService;

    /**
     * SSE 連線管理
     * Key: taskId, Value: SseEmitter
     */
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    /**
     * Spring 管理的執行緒池（用於並行抓取）
     * 由 AsyncConfig 配置，支援 SecurityContext 和 MDC 傳遞
     */
    private final Executor taskExecutor;

    /**
     * ISBN 掃描處理（專門用於書籍）
     * 1. 驗證 ISBN 格式
     * 2. 透過 CA101WHandler 爬取書籍資訊
     * 3. 透過 BookItemService 儲存到資料庫
     * 4. 回傳書籍物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:use')")
    @Log(title = "ISBN掃描", businessType = BusinessType.INSERT)
    @PostMapping("/isbn")
    public AjaxResult scanIsbn(@RequestBody @Validated IsbnScanRequest request) {
        String isbn = request.getIsbn().trim();
        Long itemId = null;
        String itemName = null;

        try {
            // 1. 驗證 ISBN 格式
            if (!isValidIsbn(isbn)) {
                saveScanLog(isbn, null, null, ScanResult.FAILURE.getCode(), "ISBN 格式不正確");
                return AjaxResult.error("ISBN 格式不正確，請輸入10位或13位數字");
            }

            // 2. 透過 CA101 Handler 爬取書籍資訊
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(CrawlerType.CA101);
            if (handler == null) {
                saveScanLog(isbn, null, null, ScanResult.FAILURE.getCode(), "CA101 爬蟲服務未初始化");
                return AjaxResult.error("CA101 爬蟲服務未初始化");
            }

            BookInfoDTO bookInfo = handler.crawlSingleAndSave(isbn, BookInfoDTO.class);

            // 3. 透過 BookItemService 儲存到資料庫
            InvItem bookItem = bookItemService.createOrGetBookItem(isbn, bookInfo);
            itemId = bookItem.getItemId();
            itemName = bookItem.getItemName();

            // 4. 寫入掃描成功記錄（在取得物品資訊後）
            saveScanLog(isbn, itemId, itemName, ScanResult.SUCCESS.getCode(), null);

            // 5. 查詢包含庫存資訊的完整資料
            InvItemWithStockDTO itemWithStock = invItemMapper.selectItemWithStockByItemId(bookItem.getItemId());

            // 6. 計算庫存狀態和價值
            if (itemWithStock != null) {
                itemWithStock.calculateStockStatus();
                itemWithStock.calculateStockValue();
            }

            // 7. 判斷是新建還是已存在
            boolean isNewItem = bookItem.getCreateTime() != null &&
                    bookItem.getCreateTime().getTime() > System.currentTimeMillis() - 5000;

            // 8. 組裝回傳資料
            Map<String, Object> result = new HashMap<>();
            result.put("item", itemWithStock != null ? itemWithStock : bookItem);
            result.put("message", isNewItem ? "書籍建立成功" : "書籍已存在");
            result.put("isNew", isNewItem);

            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("ISBN 掃描處理失敗 - ISBN: {}, 錯誤: {}", isbn, e.getMessage(), e);
            // 記錄失敗情況
            saveScanLog(isbn, itemId, itemName, ScanResult.FAILURE.getCode(), e.getMessage());
            return AjaxResult.error("處理失敗: " + e.getMessage());
        }
    }

    /**
     * 儲存掃描記錄
     *
     * @param scanCode   掃描內容
     * @param itemId     物品ID（可為null）
     * @param itemName   物品名稱（可為null）
     * @param scanResult 掃描結果（使用 ScanResult enum）
     * @param errorMsg   錯誤訊息（可為null）
     */
    private void saveScanLog(String scanCode, Long itemId, String itemName, String scanResult, String errorMsg) {
        try {
            InvScanLog scanLog = new InvScanLog();
            scanLog.setScanType(ScanType.BARCODE.getCode());
            scanLog.setScanCode(scanCode);
            scanLog.setItemId(itemId);
            scanLog.setItemName(itemName != null ? itemName : "");
            scanLog.setScanResult(scanResult);
            scanLog.setOperatorId(getUserId());
            scanLog.setOperatorName(getUsername());
            scanLog.setScanTime(new Date());
            scanLog.setIpAddress(IpUtils.getIpAddr());
            scanLog.setUserAgent(ServletUtils.getRequest().getHeader(HttpHeaders.USER_AGENT));
            scanLog.setErrorMsg(errorMsg != null ? errorMsg : "");

            invScanLogService.insertInvScanLog(scanLog);
        } catch (Exception e) {
            log.warn("寫入掃描記錄失敗: {}", e.getMessage());
            // 不中斷主流程
        }
    }

    /**
     * 驗證 ISBN 格式
     */
    private boolean isValidIsbn(String isbn) {
        if (StringUtils.isEmpty(isbn)) {
            return false;
        }
        // 移除可能的連字號或空格
        String cleanIsbn = isbn.replaceAll("[-\\s]", "");
        // 檢查是否為10位或13位數字
        return cleanIsbn.matches("^\\d{10}$") || cleanIsbn.matches("^\\d{13}$");
    }

    /**
     * 重新抓取 ISBN 資料並更新現有物品資訊（建立任務並回傳 taskId）
     * 支援並行抓取多本書籍
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:edit')")
    @Log(title = "建立重新抓取ISBN任務", businessType = BusinessType.UPDATE)
    @PostMapping("/refreshIsbn/create")
    public AjaxResult createRefreshTask(@RequestBody @Validated RefreshIsbnRequest request) {
        // 產生任務 ID
        String taskId = "refresh-" + UUID.randomUUID();

        log.info("建立重新抓取任務，TaskId: {}, ItemId: {}", taskId, request.getItemId());

        return AjaxResult.success("任務已建立", taskId);
    }

    /**
     * 訂閱重新抓取任務進度（SSE）
     */
    @Anonymous
    @GetMapping(value = "/refreshIsbn/subscribe/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeRefreshTask(
            @PathVariable String taskId,
            @RequestParam Long itemId) {

        log.info("收到 SSE 訂閱請求: taskId={}, itemId={}", taskId, itemId);

        // 建立 SseEmitter（逾時 10 分鐘）
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        // 儲存連線
        sseEmitters.put(taskId, emitter);

        // 設定完成/逾時/錯誤回調
        emitter.onCompletion(() -> {
            log.info("SSE 連線正常完成: taskId={}", taskId);
            sseEmitters.remove(taskId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 連線逾時: taskId={}", taskId);
            sseEmitters.remove(taskId);
        });

        emitter.onError((e) -> {
            log.error("SSE 連線錯誤: taskId={}", taskId, e);
            sseEmitters.remove(taskId);
        });

        // 使用 Spring 管理的執行緒池非同步執行抓取任務
        // taskExecutor 會自動複製 SecurityContext 和 MDC
        taskExecutor.execute(() -> executeRefreshTask(taskId, itemId, emitter));

        return emitter;
    }

    /**
     * 執行重新抓取任務
     */
    private void executeRefreshTask(String taskId, Long itemId, SseEmitter emitter) {
        try {
            // 發送開始事件
            sendSseEvent(emitter, "progress", Map.of(
                    "progress", 0,
                    "message", "開始重新抓取書籍資料..."
            ));

            // 1. 檢查物品是否存在
            sendSseEvent(emitter, "progress", Map.of(
                    "progress", 10,
                    "message", "檢查物品資訊..."
            ));

            InvItem existingItem = invItemMapper.selectInvItemByItemId(itemId);
            if (existingItem == null) {
                sendSseEvent(emitter, "error", Map.of("message", "物品不存在"));
                emitter.complete();
                sseEmitters.remove(taskId);
                return;
            }

            // 2. 檢查是否有條碼（ISBN）
            String isbn = existingItem.getBarcode();
            if (StringUtils.isEmpty(isbn) || !isValidIsbn(isbn)) {
                sendSseEvent(emitter, "error", Map.of("message", "物品條碼不是有效的 ISBN 格式"));
                emitter.complete();
                sseEmitters.remove(taskId);
                return;
            }

            // 3. 爬取最新的書籍資訊
            sendSseEvent(emitter, "progress", Map.of(
                    "progress", 30,
                    "message", "正在從網路抓取書籍資料...",
                    "isbn", isbn
            ));

            log.info("開始重新抓取 ISBN 資料，TaskId: {}, ItemId: {}, ISBN: {}", taskId, itemId, isbn);
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(CrawlerType.CA101);
            if (handler == null) {
                sendSseEvent(emitter, "error", Map.of("message", "CA101 爬蟲服務未初始化"));
                emitter.complete();
                sseEmitters.remove(taskId);
                return;
            }

            BookInfoDTO newBookInfo = handler.crawlSingleAndSave(isbn, BookInfoDTO.class);
            if (!newBookInfo.getSuccess()) {
                sendSseEvent(emitter, "error", Map.of(
                        "message", "爬取書籍資訊失敗: " + newBookInfo.getErrorMessage()
                ));
                emitter.complete();
                sseEmitters.remove(taskId);
                return;
            }

            // 4. 驗證新資料的完整性
            sendSseEvent(emitter, "progress", Map.of(
                    "progress", 60,
                    "message", "驗證資料完整性..."
            ));

            ValidationResult validation = validateBookInfoCompleteness(existingItem, newBookInfo);
            if (!validation.isValid()) {
                // 如果是資料相同，發送 warning 而不是 error
                String eventType = validation.getMessage().contains("資料完全相同") ? "warning" : "error";
                sendSseEvent(emitter, eventType, Map.of(
                        "message", validation.getMessage(),
                        "existingScore", validation.getExistingScore(),
                        "newScore", validation.getNewScore()
                ));
                emitter.complete();
                sseEmitters.remove(taskId);
                return;
            }

            // 5. 更新書籍資訊（不影響庫存）
            sendSseEvent(emitter, "progress", Map.of(
                    "progress", 80,
                    "message", "更新書籍資訊..."
            ));

            boolean updated = bookItemService.updateBookInfoOnly(itemId, newBookInfo);
            if (!updated) {
                sendSseEvent(emitter, "error", Map.of("message", "更新書籍資訊失敗"));
                emitter.complete();
                sseEmitters.remove(taskId);
                return;
            }

            // 6. 查詢更新後的完整資料
            sendSseEvent(emitter, "progress", Map.of(
                    "progress", 90,
                    "message", "載入更新後的資料..."
            ));

            InvItemWithStockDTO updatedItem = invItemMapper.selectItemWithStockByItemId(itemId);
            if (updatedItem != null) {
                updatedItem.calculateStockStatus();
                updatedItem.calculateStockValue();
            }

            // 7. 發送成功事件
            Map<String, Object> result = new HashMap<>();
            result.put("progress", 100);
            result.put("message", "書籍資訊更新成功");
            result.put("item", updatedItem);
            result.put("changes", validation.getChanges());
            result.put("updatedFields", validation.getUpdatedFields());
            result.put("existingScore", validation.getExistingScore());
            result.put("newScore", validation.getNewScore());

            sendSseEvent(emitter, "success", result);

            log.info("ISBN 資料更新成功，TaskId: {}, ItemId: {}, ISBN: {}, 更新欄位: {}",
                    taskId, itemId, isbn, validation.getUpdatedFields());

            // 完成連線
            Thread.sleep(500); // 確保前端收到最後一次更新
            emitter.complete();
            sseEmitters.remove(taskId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("任務被中斷: taskId={}", taskId);
            sseEmitters.remove(taskId);
        } catch (Exception e) {
            log.error("重新抓取 ISBN 資料失敗 - TaskId: {}, ItemId: {}, 錯誤: {}", taskId, itemId, e.getMessage(), e);
            try {
                sendSseEvent(emitter, "error", Map.of(
                        "message", "處理失敗: " + e.getMessage()
                ));
                emitter.complete();
            } catch (Exception ex) {
                log.error("發送錯誤事件失敗", ex);
            }
            sseEmitters.remove(taskId);
        }
    }

    /**
     * 發送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, String eventName, Map<String, Object> data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.error("發送 SSE 事件失敗: {}", eventName, e);
            throw new RuntimeException("SSE 推送失敗", e);
        }
    }

    /**
     * 重新抓取 ISBN 資料並更新現有物品資訊（同步版本，保留向後相容）
     * 只更新書籍資訊，不影響庫存
     * 會比對新舊資料完整性，只在新資料更完整時才更新
     *
     * @deprecated 建議使用 SSE 版本 (createRefreshTask + subscribeRefreshTask)
     */
    @Deprecated
    @PreAuthorize("@ss.hasPermi('inventory:management:edit')")
    @Log(title = "重新抓取ISBN資料", businessType = BusinessType.UPDATE)
    @PostMapping("/refreshIsbn")
    public AjaxResult refreshIsbn(@RequestBody @Validated RefreshIsbnRequest request) {
        Long itemId = request.getItemId();

        try {
            // 1. 檢查物品是否存在
            InvItem existingItem = invItemMapper.selectInvItemByItemId(itemId);
            if (existingItem == null) {
                return AjaxResult.error("物品不存在");
            }

            // 2. 檢查是否有條碼（ISBN）
            String isbn = existingItem.getBarcode();
            if (StringUtils.isEmpty(isbn) || !isValidIsbn(isbn)) {
                return AjaxResult.error("物品條碼不是有效的 ISBN 格式");
            }

            // 3. 爬取最新的書籍資訊
            log.info("開始重新抓取 ISBN 資料，ItemId: {}, ISBN: {}", itemId, isbn);
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(CrawlerType.CA101);
            if (handler == null) {
                return AjaxResult.error("CA101 爬蟲服務未初始化");
            }

            BookInfoDTO newBookInfo = handler.crawlSingleAndSave(isbn, BookInfoDTO.class);
            if (!newBookInfo.getSuccess()) {
                return AjaxResult.error("爬取書籍資訊失敗: " + newBookInfo.getErrorMessage());
            }

            // 4. 驗證新資料的完整性
            ValidationResult validation = validateBookInfoCompleteness(existingItem, newBookInfo);
            if (!validation.isValid()) {
                return AjaxResult.error(validation.getMessage());
            }

            // 5. 更新書籍資訊（不影響庫存）
            boolean updated = bookItemService.updateBookInfoOnly(itemId, newBookInfo);
            if (!updated) {
                return AjaxResult.error("更新書籍資訊失敗");
            }

            // 6. 查詢更新後的完整資料
            InvItemWithStockDTO updatedItem = invItemMapper.selectItemWithStockByItemId(itemId);
            if (updatedItem != null) {
                updatedItem.calculateStockStatus();
                updatedItem.calculateStockValue();
            }

            // 7. 組裝回傳資料，包含差異資訊
            Map<String, Object> result = new HashMap<>();
            result.put("item", updatedItem);
            result.put("message", "書籍資訊更新成功");
            result.put("changes", validation.getChanges());
            result.put("updatedFields", validation.getUpdatedFields());

            log.info("ISBN 資料更新成功，ItemId: {}, ISBN: {}, 更新欄位: {}",
                    itemId, isbn, validation.getUpdatedFields());

            return AjaxResult.success(result);

        } catch (Exception e) {
            log.error("重新抓取 ISBN 資料失敗 - ItemId: {}, 錯誤: {}", itemId, e.getMessage(), e);
            return AjaxResult.error("處理失敗: " + e.getMessage());
        }
    }

    /**
     * 驗證新書籍資訊的完整性
     * 使用權重分數比對，確保新資料比舊資料更完整
     */
    private ValidationResult validateBookInfoCompleteness(InvItem existingItem, BookInfoDTO newBookInfo) {
        ValidationResult result = new ValidationResult();
        Map<String, String> changes = new HashMap<>();

        // 必要欄位檢查：書名
        if (StringUtils.isEmpty(newBookInfo.getTitle())) {
            result.setValid(false);
            result.setMessage("新資料缺少書名，不予更新");
            return result;
        }

        // === 權重分數計算 ===
        int existingScore = 0;
        int newScore = 0;

        // 1. 作者（重要欄位，2分）
        String existingAuthor = getAuthorFromItem(existingItem);
        if (isNotEmpty(existingAuthor)) {
            existingScore += 2;
        }
        if (isNotEmpty(newBookInfo.getAuthor())) {
            newScore += 2;
            if (!newBookInfo.getAuthor().equals(existingAuthor)) {
                changes.put("作者", String.format("%s → %s",
                        StringUtils.isEmpty(existingAuthor) ? "(無)" : existingAuthor,
                        newBookInfo.getAuthor()));
                result.addUpdatedField("作者");
            }
        }

        // 2. 出版社（重要欄位，2分）
        if (isNotEmpty(existingItem.getBrand())) {
            existingScore += 2;
        }
        if (isNotEmpty(newBookInfo.getPublisher())) {
            newScore += 2;
            if (!newBookInfo.getPublisher().equals(existingItem.getBrand())) {
                changes.put("出版社", String.format("%s → %s",
                        StringUtils.isEmpty(existingItem.getBrand()) ? "(無)" : existingItem.getBrand(),
                        newBookInfo.getPublisher()));
                result.addUpdatedField("出版社");
            }
        }

        // 3. 封面圖片（重要欄位，2分）
        if (isNotEmpty(existingItem.getImageUrl())) {
            existingScore += 2;
        }
        if (isNotEmpty(newBookInfo.getCoverImagePath())) {
            newScore += 2;
            if (!newBookInfo.getCoverImagePath().equals(existingItem.getImageUrl())) {
                changes.put("封面圖片", "已更新");
                result.addUpdatedField("封面圖片");
            }
        }

        // 4. 出版日期（1分）
        String existingPublishDate = extractPublishDateFromSpec(existingItem.getSpecification());
        if (isNotEmpty(existingPublishDate)) {
            existingScore += 1;
        }
        if (isNotEmpty(newBookInfo.getPublishDate())) {
            newScore += 1;
            if (!newBookInfo.getPublishDate().equals(existingPublishDate)) {
                changes.put("出版日期", String.format("%s → %s",
                        StringUtils.isEmpty(existingPublishDate) ? "(無)" : existingPublishDate,
                        newBookInfo.getPublishDate()));
                result.addUpdatedField("出版日期");
            }
        }

        // 5. 語言（1分）
        String existingLanguage = extractLanguageFromSpec(existingItem.getSpecification());
        if (isNotEmpty(existingLanguage)) {
            existingScore += 1;
        }
        if (isNotEmpty(newBookInfo.getLanguage())) {
            newScore += 1;
            if (!newBookInfo.getLanguage().equals(existingLanguage)) {
                changes.put("語言", String.format("%s → %s",
                        StringUtils.isEmpty(existingLanguage) ? "(無)" : existingLanguage,
                        newBookInfo.getLanguage()));
                result.addUpdatedField("語言");
            }
        }

        // 6. 版本（1分）
        if (isNotEmpty(existingItem.getModel())) {
            existingScore += 1;
        }
        if (isNotEmpty(newBookInfo.getEdition())) {
            newScore += 1;
            if (!newBookInfo.getEdition().equals(existingItem.getModel())) {
                changes.put("版本", String.format("%s → %s",
                        StringUtils.isEmpty(existingItem.getModel()) ? "(無)" : existingItem.getModel(),
                        newBookInfo.getEdition()));
                result.addUpdatedField("版本");
            }
        }

        // 7. 簡介（根據長度，最多3分）
        if (isNotEmpty(existingItem.getDescription())) {
            existingScore += Math.min(existingItem.getDescription().length() / 50, 3);
        }
        if (isNotEmpty(newBookInfo.getIntroduction())) {
            newScore += Math.min(newBookInfo.getIntroduction().length() / 50, 3);
            if (!newBookInfo.getIntroduction().equals(existingItem.getDescription())) {
                String newDesc = newBookInfo.getIntroduction().length() > 100 ?
                        newBookInfo.getIntroduction().substring(0, 100) + "..." : newBookInfo.getIntroduction();
                changes.put("簡介", "已更新(" + newBookInfo.getIntroduction().length() + "字)");
                result.addUpdatedField("簡介");
            }
        }

        // 8. 書名變化（不計分，但記錄變化）
        if (!newBookInfo.getTitle().equals(existingItem.getItemName())) {
            changes.put("書名", String.format("%s → %s", existingItem.getItemName(), newBookInfo.getTitle()));
            result.addUpdatedField("書名");
        }

        // === 分數比對 ===
        log.info("資料完整性比對 - ItemId: {}, 現有分數: {}, 新資料分數: {}",
                existingItem.getItemId(), existingScore, newScore);

        result.setExistingScore(existingScore);
        result.setNewScore(newScore);

        // 新資料分數必須大於或等於舊資料才允許更新
        if (newScore < existingScore) {
            result.setValid(false);
            result.setMessage(String.format(
                    "新資料不夠完整（分數: %d），無法覆蓋現有資料（分數: %d）",
                    newScore, existingScore));
            return result;
        }

        // 分數相同時，如果沒有任何變化，則不需要更新
        if (newScore == existingScore && changes.isEmpty()) {
            result.setValid(false);
            result.setMessage("資料完全相同，無需更新");
            return result;
        }

        result.setValid(true);
        result.setChanges(changes);

        if (changes.isEmpty()) {
            result.setMessage("資料無變化，但分數相同，允許更新");
        } else {
            result.setMessage(String.format(
                    "新資料更完整（分數: %d > %d），共更新 %d 個欄位",
                    newScore, existingScore, changes.size()));
        }

        return result;
    }

    /**
     * 從規格中提取出版日期
     */
    private String extractPublishDateFromSpec(String specification) {
        if (StringUtils.isEmpty(specification)) {
            return null;
        }
        // 規格格式：語言 / 裝訂 / 出版日期
        String[] parts = specification.split("/");
        if (parts.length >= 3) {
            return parts[2].trim();
        }
        return null;
    }

    /**
     * 從規格中提取語言
     */
    private String extractLanguageFromSpec(String specification) {
        if (StringUtils.isEmpty(specification)) {
            return null;
        }
        // 規格格式：語言 / 裝訂 / 出版日期
        String[] parts = specification.split("/");
        if (parts.length >= 1) {
            return parts[0].trim();
        }
        return null;
    }

    /**
     * 從物品中取得作者資訊
     * 注意：inv_item 表沒有 author 欄位，需要從 inv_book_info 查詢
     */
    private String getAuthorFromItem(InvItem item) {
        try {
            InvBookInfo bookInfo = invBookInfoService.selectInvBookInfoByItemId(item.getItemId());
            if (bookInfo != null && StringUtils.isNotEmpty(bookInfo.getAuthor())) {
                return bookInfo.getAuthor();
            }
        } catch (Exception e) {
            log.warn("查詢書籍作者資訊失敗，ItemId: {}", item.getItemId());
        }
        return null;
    }

    /**
     * 檢查值是否非空
     */
    private boolean isNotEmpty(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            return !str.isEmpty() && !"null".equalsIgnoreCase(str);
        }
        return true;
    }

    /**
     * 驗證結果封裝類
     */
    @Getter
    @Setter
    private static class ValidationResult {
        private boolean valid;
        private String message;
        private Map<String, String> changes = new HashMap<>();
        private List<String> updatedFields = new ArrayList<>();
        private int existingScore;  // 現有資料分數
        private int newScore;       // 新資料分數

        public void addUpdatedField(String field) {
            updatedFields.add(field);
        }
    }

    /**
     * ISBN 掃描請求物件
     */
    @Setter
    @Getter
    public static class IsbnScanRequest {
        /**
         * ISBN 編號
         */
        @NotBlank(message = "ISBN不能為空")
        private String isbn;
    }

    /**
     * 重新抓取 ISBN 請求物件
     */
    @Setter
    @Getter
    public static class RefreshIsbnRequest {
        /**
         * 物品 ID
         */
        @NotNull(message = "物品ID不能為空")
        private Long itemId;
    }
}
