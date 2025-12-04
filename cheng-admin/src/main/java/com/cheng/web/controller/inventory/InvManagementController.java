package com.cheng.web.controller.inventory;

import com.alibaba.fastjson2.JSON;
import com.cheng.common.annotation.Log;
import com.cheng.common.annotation.Anonymous;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.common.event.ReservationEvent;
import com.cheng.system.domain.InvItem;
import com.cheng.system.dto.ImportTaskResult;
import com.cheng.system.dto.InvItemWithStockDTO;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.service.IInvItemService;
import com.cheng.system.service.IInvStockService;
import com.cheng.framework.sse.SseChannels;
import com.cheng.framework.sse.SseManager;
import com.cheng.framework.sse.SseEvent;
import com.cheng.system.service.impl.InvItemServiceImpl;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 物品與庫存整合管理Controller
 * 提供物品資訊和庫存狀態的統一查詢和操作介面
 *
 * @author cheng
 * @since 2025-10-04
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory/management")
public class InvManagementController extends BaseController {

    private final InvItemMapper invItemMapper;
    private final IInvItemService invItemService;
    private final IInvStockService invStockService;
    private final SseManager sseManager;

    /**
     * 查詢物品與庫存整合列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvItemWithStockDTO dto) {
        startPage();
        log.info("接收到查詢請求 - DTO: {}", JSON.toJSONString(dto));
        log.info("stockStatus 值: [{}], 類型: [{}]", dto.getStockStatus(),
                dto.getStockStatus() != null ? dto.getStockStatus().getClass().getName() : "null");
        List<InvItemWithStockDTO> list = invItemMapper.selectItemWithStockList(dto);

        // 計算庫存狀態和總價值
        list.forEach(item -> {
            item.calculateStockStatus();
            item.calculateStockValue();
        });

        return getDataTable(list);
    }

    /**
     * 查詢低庫存物品列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:list')")
    @GetMapping("/lowStock")
    public TableDataInfo lowStockList() {
        startPage();
        List<InvItemWithStockDTO> list = invItemMapper.selectLowStockItemWithStockList();

        // 計算庫存狀態和總價值
        list.forEach(item -> {
            item.calculateStockStatus();
            item.calculateStockValue();
        });

        return getDataTable(list);
    }

    /**
     * 取得物品與庫存詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId) {
        InvItemWithStockDTO dto = invItemMapper.selectItemWithStockByItemId(itemId);
        if (dto != null) {
            dto.calculateStockStatus();
            dto.calculateStockValue();
            return success(dto);
        }
        return error("未找到對應的物品資訊");
    }

    /**
     * 匯出物品與庫存列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:export')")
    @Log(title = "物品與庫存管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvItemWithStockDTO dto) {
        List<InvItemWithStockDTO> list = invItemMapper.selectItemWithStockList(dto);

        // 計算庫存狀態和總價值
        list.forEach(item -> {
            item.calculateStockStatus();
            item.calculateStockValue();
        });

        ExcelUtil<InvItemWithStockDTO> util = new ExcelUtil<>(InvItemWithStockDTO.class);
        util.exportExcel(response, list, "物品與庫存資料");
    }

    /**
     * 匯入物品資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:import')")
    @Log(title = "物品資訊", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, Boolean updateSupport, Long defaultCategoryId, String defaultUnit) throws Exception {
        // 檢查檔案是否為空
        if (file.isEmpty()) {
            return error("上傳檔案不能為空");
        }

        // 檢查檔案格式
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            return error("請上傳Excel檔案（.xlsx或.xls格式）");
        }

        // 檢查必要參數
        if (defaultCategoryId == null) {
            return error("請選擇預設分類");
        }
        if (defaultUnit == null || defaultUnit.trim().isEmpty()) {
            return error("請輸入預設單位");
        }

        // 建立匯入任務並返回taskId
        ImportTaskResult taskResult = invItemService.createImportTask(file, updateSupport, defaultCategoryId, defaultUnit);

        return success("匯入任務已啟動，準備匯入 " + taskResult.getRowCount() + " 筆資料，taskId: " + taskResult.getTaskId());
    }


    /**
     * 新增物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:add')")
    @Log(title = "物品資訊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InvItem invItem) {
        if (!invItemService.checkItemCodeUnique(invItem)) {
            return error("新增物品'" + invItem.getItemName() + "'失敗，物品編碼已存在");
        }
        if (!invItemService.checkBarcodeUnique(invItem)) {
            return error("新增物品'" + invItem.getItemName() + "'失敗，條碼已存在");
        }
        return toAjax(invItemService.insertInvItem(invItem));
    }

    /**
     * 修改物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:edit')")
    @Log(title = "物品資訊", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InvItem invItem) {
        if (!invItemService.checkItemCodeUnique(invItem)) {
            return error("修改物品'" + invItem.getItemName() + "'失敗，物品編碼已存在");
        }
        if (!invItemService.checkBarcodeUnique(invItem)) {
            return error("修改物品'" + invItem.getItemName() + "'失敗，條碼已存在");
        }

        // 記錄修改的物品名稱，讓操作日誌能記錄
        AjaxResult result = toAjax(invItemService.updateInvItem(invItem));
        result.put("itemName", invItem.getItemName());
        log.info("修改物品：{}", invItem.getItemName());

        return result;
    }

    /**
     * 刪除物品資訊（級聯刪除相關表）
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:remove')")
    @Log(title = "物品資訊", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        try {
            // 在刪除前取得物品名稱列表，用於操作日誌記錄
            StringBuilder itemNames = new StringBuilder();
            for (Long itemId : itemIds) {
                InvItem item = invItemMapper.selectInvItemByItemId(itemId);
                if (item != null) {
                    if (!itemNames.isEmpty()) {
                        itemNames.append("、");
                    }
                    itemNames.append(item.getItemName());
                }
            }

            // 執行刪除
            String resultMsg = invItemService.safeDeleteInvItemByItemIds(itemIds);

            // 將刪除的物品名稱加入返回結果，讓操作日誌能記錄
            AjaxResult result = success(resultMsg);
            if (!itemNames.isEmpty()) {
                result.put("deletedItems", itemNames.toString());
                log.info("刪除物品：{}", itemNames);
            }

            return result;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 入庫操作
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:stockIn')")
    @Log(title = "入庫", businessType = BusinessType.UPDATE)
    @PostMapping("/stockIn")
    public AjaxResult stockIn(@RequestBody StockOperationRequest request) {
        try {
            int result = invStockService.stockIn(request.getItemId(), request.getQuantity(),
                    getUserId(), request.getReason());
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 手機端快速入庫（掃碼後使用）
     * 權限要求：只需掃描權限即可
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:use')")
    @Log(title = "手機端快速入庫", businessType = BusinessType.UPDATE)
    @PostMapping("/quickStockIn")
    public AjaxResult quickStockIn(@RequestBody StockOperationRequest request) {
        try {
            int result = invStockService.stockIn(request.getItemId(), request.getQuantity(),
                    getUserId(), request.getReason());
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 出庫操作
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:stockOut')")
    @Log(title = "出庫", businessType = BusinessType.UPDATE)
    @PostMapping("/stockOut")
    public AjaxResult stockOut(@RequestBody StockOperationRequest request) {
        try {
            int result = invStockService.stockOut(request.getItemId(), request.getQuantity(),
                    getUserId(), request.getReason());
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 盤點操作
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:stockCheck')")
    @Log(title = "盤點", businessType = BusinessType.UPDATE)
    @PostMapping("/stockCheck")
    public AjaxResult stockCheck(@RequestBody StockCheckRequest request) {
        try {
            int result = invStockService.stockCheck(request.getItemId(), request.getActualQuantity(),
                    getUserId(), request.getReason());
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 預約物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:reserve')")
    @Log(title = "物品預約", businessType = BusinessType.INSERT)
    @PostMapping("/reserve")
    public AjaxResult reserveItem(@Validated @RequestBody com.cheng.system.domain.vo.ReserveRequest request) {
        try {
            com.cheng.system.domain.vo.ReserveResult result = invItemService.reserveItem(request, getUserId());
            return success(result);
        } catch (Exception e) {
            log.error("物品預約失敗", e);
            return error(e.getMessage());
        }
    }

    /**
     * 訂閱預約廣播（SSE）
     * 使用 SseManager 訂閱頻道
     */
    @Anonymous
    @GetMapping(value = "/subscribe/reserve", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeReserveUpdates(@RequestParam String taskId) {
        log.info("========== SSE 預約廣播訂閱請求 - taskId: {} ==========", taskId);

        // 使用 taskId 作為訂閱 ID（前端產生的 UUID）
        // 使用 SseManager 訂閱物品預約頻道（超時: 5分鐘）
        SseEmitter emitter = sseManager.subscribe(SseChannels.ITEM_RESERVE, taskId, 300000L);

        // 將 SseManager 實例傳遞給 Service 層用於廣播
        InvItemServiceImpl.SSE_EMITTER_MAP.put("sse-manager", sseManager);

        return emitter;
    }


    /**
     * 下載匯入範本
     */
    @PostMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            invItemService.downloadTemplate(response);
        } catch (Exception e) {
            log.error("下載匯入範本失敗", e);
        }
    }

    /**
     * 建立匯入任務（支援SSE進度顯示）
     * 使用 SseManager 統一管理 SSE 連線
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:import')")
    @PostMapping("/importData/create")
    public AjaxResult createImportTask(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "updateSupport", defaultValue = "false") Boolean updateSupport,
                                       @RequestParam(value = "defaultCategoryId", required = false) Long defaultCategoryId,
                                       @RequestParam(value = "defaultUnit", required = false) String defaultUnit) {
        try {
            // 驗證參數
            if (file == null || file.isEmpty()) {
                return error("請選擇要匯入的檔案");
            }
            if (defaultCategoryId == null) {
                return error("請選擇預設分類");
            }
            if (defaultUnit == null || defaultUnit.trim().isEmpty()) {
                return error("請輸入預設單位");
            }

            // 調用 service 建立任務
            ImportTaskResult taskResult = invItemService.createImportTask(file, updateSupport, defaultCategoryId, defaultUnit);

            log.info("匯入任務已建立 - taskId: {}, rowCount: {}", taskResult.getTaskId(), taskResult.getRowCount());

            // 返回響應（包含taskId和rowCount）
            AjaxResult result = success("匯入任務已建立");
            result.put("taskId", taskResult.getTaskId());
            result.put("rowCount", taskResult.getRowCount());
            return result;
        } catch (Exception e) {
            log.error("建立匯入任務失敗", e);
            return error("建立匯入任務失敗: " + e.getMessage());
        }
    }

    /**
     * 訂閱匯入任務進度（SSE）
     * 使用 SseManager 訂閱頻道
     */
    @Anonymous
    @GetMapping(value = "/importData/subscribe/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeImportTask(@PathVariable String taskId) {
        log.info("========== SSE 訂閱請求 - taskId: {} ==========", taskId);

        // 使用 SseManager 訂閱物品匯入頻道（超時: 10分鐘）
        SseEmitter emitter = sseManager.subscribe(SseChannels.ITEM_IMPORT, taskId, 600000L);

        // 啟動定期輪詢任務，從 PROGRESS_MAP 取得進度並推送
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                InvItemServiceImpl.ProgressInfo progressInfo = InvItemServiceImpl.PROGRESS_MAP.get(taskId);
                if (progressInfo != null) {
                    int progress = progressInfo.getProgress();
                    String message = progressInfo.getMessage();

                    if (progress == -1) {
                        // 錯誤 - 發送事件並立即停止輪詢
                        SseEvent event = SseEvent.error(message);
                        sseManager.send(SseChannels.ITEM_IMPORT, taskId, event);

                        // 立即清除進度，停止輪詢
                        InvItemServiceImpl.PROGRESS_MAP.remove(taskId);

                        // 延遲關閉連線，確保前端有時間處理事件
                        scheduler.schedule(() -> {
                            sseManager.unsubscribe(SseChannels.ITEM_IMPORT, taskId);
                            scheduler.shutdown();
                        }, 1000, TimeUnit.MILLISECONDS);
                    } else if (progress >= 100) {
                        // 完成 - 發送事件並立即停止輪詢
                        SseEvent event = SseEvent.success(message, null);
                        sseManager.send(SseChannels.ITEM_IMPORT, taskId, event);

                        // 立即清除進度，停止輪詢
                        InvItemServiceImpl.PROGRESS_MAP.remove(taskId);

                        // 延遲關閉連線，確保前端有時間處理事件
                        scheduler.schedule(() -> {
                            sseManager.unsubscribe(SseChannels.ITEM_IMPORT, taskId);
                            scheduler.shutdown();
                        }, 1000, TimeUnit.MILLISECONDS);
                    } else {
                        // 進度更新
                        SseEvent event = SseEvent.progress("processing", progress, message);
                        sseManager.send(SseChannels.ITEM_IMPORT, taskId, event);
                    }
                }
            } catch (Exception e) {
                log.error("輪詢進度失敗 - taskId: {}", taskId, e);
                scheduler.shutdown();
            }
        }, 100, 200, TimeUnit.MILLISECONDS);  // 每 200ms 檢查一次

        // 訂閱成功後，啟動異步匯入任務（避免競爭條件）
        invItemService.asyncImportItems(taskId);

        return emitter;
    }

    /**
     * 庫存操作請求類
     */
    @Setter
    @Getter
    public static class StockOperationRequest {
        private Long itemId;
        private Integer quantity;
        private String reason;
    }

    /**
     * 盤點請求類
     */
    @Setter
    @Getter
    public static class StockCheckRequest {
        private Long itemId;
        private Integer actualQuantity;
        private String reason;
    }

    /**
     * 監聽預約事件並廣播 SSE 更新
     */
    @Async
    @EventListener
    public void handleReservationEvent(ReservationEvent event) {
        try {
            log.info("收到預約事件 - itemId: {}, eventType: {}, userId: {}",
                    event.getItemId(), event.getEventType(), event.getUserId());

            // 建立廣播事件
            SseEvent sseEvent = SseEvent.success(event.getEventType(), Map.of(
                    "itemId", event.getItemId(),
                    "userId", event.getUserId(),
                    "borrowId", event.getBorrowId(),
                    "message", event.getMessage(),
                    "availableQuantity", event.getAvailableQuantity(),
                    "reservedQuantity", event.getReservedQuantity(),
                    "timestamp", event.getEventTime().getTime()
            ));

            // 廣播到所有訂閱者
            sseManager.broadcast(SseChannels.ITEM_RESERVE, sseEvent);

            log.info("預約事件廣播成功 - itemId: {}, eventType: {}",
                    event.getItemId(), event.getEventType());

        } catch (Exception e) {
            log.error("處理預約事件失敗 - itemId: {}", event.getItemId(), e);
        }
    }
}
