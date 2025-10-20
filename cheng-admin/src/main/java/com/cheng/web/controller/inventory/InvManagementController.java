package com.cheng.web.controller.inventory;

import com.alibaba.fastjson2.JSON;
import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvItem;
import com.cheng.system.dto.InvItemWithStockDTO;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.service.IInvItemService;
import com.cheng.system.service.IInvStockService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查詢物品與庫存整合列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvItemWithStockDTO dto) {
        startPage();
        log.info("dto:{}", JSON.toJSONString(dto));
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
        return toAjax(invItemService.updateInvItem(invItem));
    }

    /**
     * 刪除物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:management:remove')")
    @Log(title = "物品資訊", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        return toAjax(invItemService.deleteInvItemByItemIds(itemIds));
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
}
