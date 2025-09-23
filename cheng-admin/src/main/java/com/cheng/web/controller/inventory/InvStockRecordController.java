package com.cheng.web.controller.inventory;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.system.domain.InvStockRecord;
import com.cheng.system.service.IInvStockRecordService;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.common.core.page.TableDataInfo;

/**
 * 庫存異動記錄Controller
 *
 * @author cheng
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/inventory/stockRecord")
public class InvStockRecordController extends BaseController {
    @Autowired
    private IInvStockRecordService invStockRecordService;

    /**
     * 查詢庫存異動記錄列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvStockRecord invStockRecord) {
        startPage();
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordList(invStockRecord);
        return getDataTable(list);
    }

    /**
     * 匯出庫存異動記錄列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:export')")
    @Log(title = "庫存異動記錄", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvStockRecord invStockRecord) {
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordList(invStockRecord);
        ExcelUtil<InvStockRecord> util = new ExcelUtil<InvStockRecord>(InvStockRecord.class);
        util.exportExcel(response, list, "庫存異動記錄資料");
    }

    /**
     * 獲取庫存異動記錄詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        return success(invStockRecordService.selectInvStockRecordByRecordId(recordId));
    }

    /**
     * 新增庫存異動記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:add')")
    @Log(title = "庫存異動記錄", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InvStockRecord invStockRecord) {
        return toAjax(invStockRecordService.insertInvStockRecord(invStockRecord));
    }

    /**
     * 修改庫存異動記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:edit')")
    @Log(title = "庫存異動記錄", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InvStockRecord invStockRecord) {
        return toAjax(invStockRecordService.updateInvStockRecord(invStockRecord));
    }

    /**
     * 刪除庫存異動記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:remove')")
    @Log(title = "庫存異動記錄", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        return toAjax(invStockRecordService.deleteInvStockRecordByRecordIds(recordIds));
    }

    /**
     * 根據物品ID查詢異動記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:query')")
    @GetMapping("/item/{itemId}")
    public AjaxResult getRecordsByItemId(@PathVariable("itemId") Long itemId) {
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordByItemId(itemId);
        return success(list);
    }

    /**
     * 根據操作人員ID查詢異動記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:stockRecord:query')")
    @GetMapping("/operator/{operatorId}")
    public AjaxResult getRecordsByOperatorId(@PathVariable("operatorId") Long operatorId) {
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordByOperatorId(operatorId);
        return success(list);
    }
}
