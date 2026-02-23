package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvStock;
import com.cheng.system.service.IInvStockService;
import com.cheng.system.dto.InvStockStatisticsDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.cheng.common.constant.PermConstants;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 庫存Controller
 *
 * @author cheng
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/inventory/stock")
@RequiredArgsConstructor
public class InvStockController extends BaseController {
    private final IInvStockService invStockService;

    /**
     * 查詢庫存列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(InvStock invStock) {
        startPage();
        List<InvStock> list = invStockService.selectInvStockList(invStock);
        return getDataTable(list);
    }

    /**
     * 查詢低庫存列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.LIST + "')")
    @GetMapping("/lowStock")
    public TableDataInfo lowStockList() {
        startPage();
        List<InvStock> list = invStockService.selectLowStockList();
        return getDataTable(list);
    }

    /**
     * 查詢庫存統計資訊
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.LIST + "')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        List<InvStockStatisticsDTO> list = invStockService.selectStockStatistics();
        return success(list);
    }

    /**
     * 匯出庫存列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.EXPORT + "')")
    @Log(title = "庫存", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvStock invStock) {
        List<InvStock> list = invStockService.selectInvStockList(invStock);
        ExcelUtil<InvStock> util = new ExcelUtil<InvStock>(InvStock.class);
        util.exportExcel(response, list, "庫存資料");
    }

    /**
     * 取得庫存詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.QUERY + "')")
    @GetMapping(value = "/{stockId:\\d+}")
    public AjaxResult getInfo(@PathVariable("stockId") Long stockId) {
        return success(invStockService.selectInvStockByStockId(stockId));
    }

    /**
     * 根據物品ID取得庫存訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.QUERY + "')")
    @GetMapping(value = "/item/{itemId}")
    public AjaxResult getInfoByItemId(@PathVariable("itemId") Long itemId) {
        InvStock stock = invStockService.selectInvStockByItemId(itemId);
        if (stock != null) {
            return success(stock);
        } else {
            return error("未找到對應的庫存資訊");
        }
    }

    /**
     * 新增庫存
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.ADD + "')")
    @Log(title = "庫存", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InvStock invStock) {
        return toAjax(invStockService.insertInvStock(invStock));
    }

    /**
     * 修改庫存
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.EDIT + "')")
    @Log(title = "庫存", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InvStock invStock) {
        return toAjax(invStockService.updateInvStock(invStock));
    }

    /**
     * 入庫操作
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.IN + "')")
    @Log(title = "入庫", businessType = BusinessType.UPDATE)
    @PostMapping("/in")
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
     * 出庫操作
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.OUT + "')")
    @Log(title = "出庫", businessType = BusinessType.UPDATE)
    @PostMapping("/out")
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
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.CHECK + "')")
    @Log(title = "盤點", businessType = BusinessType.UPDATE)
    @PostMapping("/check")
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
     * 刪除庫存
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Stock.REMOVE + "')")
    @Log(title = "庫存", businessType = BusinessType.DELETE)
    @DeleteMapping("/{stockIds:(?:\\d+,)*\\d+}")
    public AjaxResult remove(@PathVariable Long[] stockIds) {
        return toAjax(invStockService.deleteInvStockByStockIds(stockIds));
    }

    /**
     * 庫存操作請求物件
     */
    @Setter
    @Getter
    public static class StockOperationRequest {
        private Long itemId;
        private Integer quantity;
        private String reason;
    }

    /**
     * 盤點請求物件
     */
    @Setter
    @Getter
    public static class StockCheckRequest {
        private Long itemId;
        private Integer actualQuantity;
        private String reason;
    }
}
