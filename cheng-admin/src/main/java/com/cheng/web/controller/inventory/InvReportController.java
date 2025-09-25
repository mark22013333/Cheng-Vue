package com.cheng.web.controller.inventory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.system.domain.InvStock;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.InvStockRecord;
import com.cheng.system.service.IInvStockService;
import com.cheng.system.service.IInvBorrowService;
import com.cheng.system.service.IInvStockRecordService;
import com.cheng.common.utils.poi.ExcelUtil;

/**
 * 庫存報表Controller
 *
 * @author cheng
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/inventory/report")
public class InvReportController extends BaseController {
    @Autowired
    private IInvStockService invStockService;

    @Autowired
    private IInvBorrowService invBorrowService;

    @Autowired
    private IInvStockRecordService invStockRecordService;

    /**
     * 取得庫存報表資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:view')")
    @GetMapping("/stock")
    public AjaxResult getStockReport(InvStock invStock) {
        List<InvStock> list = invStockService.selectInvStockList(invStock);

        // 統計資料
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalItems", list.size());
        statistics.put("totalQuantity", list.stream().mapToInt(InvStock::getTotalQuantity).sum());
        statistics.put("availableQuantity", list.stream().mapToInt(InvStock::getAvailableQty).sum());
        statistics.put("borrowedQuantity", list.stream().mapToInt(InvStock::getBorrowedQty).sum());

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("statistics", statistics);

        return success(result);
    }

    /**
     * 取得借出報表資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:view')")
    @GetMapping("/borrow")
    public AjaxResult getBorrowReport(InvBorrow invBorrow) {
        List<InvBorrow> list = invBorrowService.selectInvBorrowList(invBorrow);

        // 統計資料
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalBorrows", list.size());
        statistics.put("pendingBorrows", list.stream().filter(b -> "0".equals(b.getStatus())).count());
        statistics.put("approvedBorrows", list.stream().filter(b -> "1".equals(b.getStatus())).count());
        statistics.put("returnedBorrows", list.stream().filter(b -> "2".equals(b.getStatus())).count());
        statistics.put("overdueBorrows", invBorrowService.selectOverdueBorrowList().size());

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("statistics", statistics);

        return success(result);
    }

    /**
     * 取得庫存異動報表資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:view')")
    @GetMapping("/movement")
    public AjaxResult getMovementReport(InvStockRecord invStockRecord) {
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordList(invStockRecord);

        // 統計資料
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", list.size());
        statistics.put("inRecords", list.stream().filter(r -> "1".equals(r.getRecordType())).count());
        statistics.put("outRecords", list.stream().filter(r -> "2".equals(r.getRecordType())).count());
        statistics.put("checkRecords", list.stream().filter(r -> "5".equals(r.getRecordType())).count());

        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("statistics", statistics);

        return success(result);
    }


    /**
     * 匯出庫存報表
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:export')")
    @Log(title = "庫存報表", businessType = BusinessType.EXPORT)
    @PostMapping("/stock/export")
    public void exportStockReport(HttpServletResponse response, InvStock invStock) {
        List<InvStock> list = invStockService.selectInvStockList(invStock);
        ExcelUtil<InvStock> util = new ExcelUtil<InvStock>(InvStock.class);
        util.exportExcel(response, list, "庫存報表資料");
    }

    /**
     * 匯出借出報表
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:export')")
    @Log(title = "借出報表", businessType = BusinessType.EXPORT)
    @PostMapping("/borrow/export")
    public void exportBorrowReport(HttpServletResponse response, InvBorrow invBorrow) {
        List<InvBorrow> list = invBorrowService.selectInvBorrowList(invBorrow);
        ExcelUtil<InvBorrow> util = new ExcelUtil<InvBorrow>(InvBorrow.class);
        util.exportExcel(response, list, "借出報表資料");
    }

    /**
     * 匯出異動報表
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:export')")
    @Log(title = "異動報表", businessType = BusinessType.EXPORT)
    @PostMapping("/movement/export")
    public void exportMovementReport(HttpServletResponse response, InvStockRecord invStockRecord) {
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordList(invStockRecord);
        ExcelUtil<InvStockRecord> util = new ExcelUtil<InvStockRecord>(InvStockRecord.class);
        util.exportExcel(response, list, "異動報表資料");
    }


    /**
     * 取得儀表板統計資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:report:view')")
    @GetMapping("/dashboard")
    public AjaxResult getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        // 庫存統計
        List<InvStock> stockList = invStockService.selectInvStockList(new InvStock());
        dashboard.put("totalItems", stockList.size());
        dashboard.put("totalQuantity", stockList.stream().mapToInt(InvStock::getTotalQuantity).sum());
        dashboard.put("availableQuantity", stockList.stream().mapToInt(InvStock::getAvailableQty).sum());

        // 借出統計
        List<InvBorrow> borrowList = invBorrowService.selectInvBorrowList(new InvBorrow());
        dashboard.put("totalBorrows", borrowList.size());
        dashboard.put("pendingBorrows", borrowList.stream().filter(b -> "0".equals(b.getStatus())).count());
        dashboard.put("overdueBorrows", invBorrowService.selectOverdueBorrowList().size());

        // 異動統計
        List<InvStockRecord> recordList = invStockRecordService.selectInvStockRecordList(new InvStockRecord());
        dashboard.put("totalRecords", recordList.size());
        dashboard.put("todayRecords", recordList.stream().filter(r ->
                r.getRecordTime() != null &&
                        r.getRecordTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now())
        ).count());

        return success(dashboard);
    }
}
