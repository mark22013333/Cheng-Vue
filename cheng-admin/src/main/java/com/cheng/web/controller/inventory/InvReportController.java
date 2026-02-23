package com.cheng.web.controller.inventory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.ZoneId;

import com.cheng.common.core.page.TableDataInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.cheng.common.constant.PermConstants;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.enums.ScanResult;
import com.cheng.common.enums.ScanType;
import com.cheng.system.domain.enums.BorrowStatus;
import com.cheng.system.domain.enums.StockRecordType;
import com.cheng.system.domain.InvStock;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.InvStockRecord;
import com.cheng.system.domain.InvScanLog;
import com.cheng.system.service.IInvStockService;
import com.cheng.system.service.IInvBorrowService;
import com.cheng.system.service.IInvStockRecordService;
import com.cheng.system.service.IInvScanLogService;
import com.cheng.common.utils.poi.ExcelUtil;

/**
 * 庫存報表Controller
 *
 * @author cheng
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/inventory/report")
@RequiredArgsConstructor
public class InvReportController extends BaseController {

    private final IInvStockService invStockService;
    private final IInvBorrowService invBorrowService;
    private final IInvStockRecordService invStockRecordService;
    private final IInvScanLogService invScanLogService;

    /**
     * 取得庫存報表資料
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.VIEW + "')")
    @GetMapping("/stock")
    public TableDataInfo getStockReport(InvStock invStock) {
        startPage();
        List<InvStock> list = invStockService.selectInvStockList(invStock);

        // 統計資料（使用全部資料）
        List<InvStock> allList = invStockService.selectInvStockList(new InvStock());
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalItems", allList.size());
        statistics.put("totalQuantity", allList.stream().mapToInt(InvStock::getTotalQuantity).sum());
        long lowStockItems = allList.stream()
                .filter(s -> s.getTotalQuantity() > 0 && s.getTotalQuantity() <= s.getMinStock())
                .count();
        long outOfStockItems = allList.stream()
                .filter(s -> s.getTotalQuantity() <= 0)
                .count();
        statistics.put("lowStockItems", lowStockItems);
        statistics.put("outOfStockItems", outOfStockItems);

        TableDataInfo dataInfo = getDataTable(list);
        dataInfo.put("statistics", statistics);
        return dataInfo;
    }

    /**
     * 取得借出報表資料
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.VIEW + "')")
    @GetMapping("/borrow")
    public TableDataInfo getBorrowReport(InvBorrow invBorrow) {
        startPage();
        List<InvBorrow> list = invBorrowService.selectInvBorrowList(invBorrow);

        // 統計資料（使用全部資料）
        List<InvBorrow> allList = invBorrowService.selectInvBorrowList(new InvBorrow());
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalBorrows", allList.size());
        statistics.put("pendingBorrows", allList.stream().filter(b -> BorrowStatus.PENDING.getCode().equals(b.getStatus())).count());
        statistics.put("approvedBorrows", allList.stream().filter(b -> BorrowStatus.BORROWED.getCode().equals(b.getStatus())).count());
        statistics.put("returnedBorrows", allList.stream().filter(b -> BorrowStatus.RETURNED.getCode().equals(b.getStatus())).count());
        statistics.put("overdueBorrows", invBorrowService.selectOverdueBorrowList(null).size());

        TableDataInfo dataInfo = getDataTable(list);
        dataInfo.put("statistics", statistics);
        return dataInfo;
    }

    /**
     * 取得庫存異動報表資料
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.VIEW + "')")
    @GetMapping("/movement")
    public TableDataInfo getMovementReport(InvStockRecord invStockRecord) {
        startPage();
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordList(invStockRecord);

        // 統計資料（使用全部資料）
        List<InvStockRecord> allList = invStockRecordService.selectInvStockRecordList(new InvStockRecord());
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", allList.size());
        statistics.put("inRecords", allList.stream().filter(r -> StockRecordType.IN.getCode().equals(r.getRecordType())).count());
        statistics.put("outRecords", allList.stream().filter(r -> StockRecordType.OUT.getCode().equals(r.getRecordType())).count());
        statistics.put("checkRecords", allList.stream().filter(r -> StockRecordType.CHECK.getCode().equals(r.getRecordType())).count());

        TableDataInfo dataInfo = getDataTable(list);
        dataInfo.put("statistics", statistics);
        return dataInfo;
    }


    /**
     * 匯出庫存報表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.EXPORT + "')")
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
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.EXPORT + "')")
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
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.EXPORT + "')")
    @Log(title = "異動報表", businessType = BusinessType.EXPORT)
    @PostMapping("/movement/export")
    public void exportMovementReport(HttpServletResponse response, InvStockRecord invStockRecord) {
        List<InvStockRecord> list = invStockRecordService.selectInvStockRecordList(invStockRecord);
        ExcelUtil<InvStockRecord> util = new ExcelUtil<InvStockRecord>(InvStockRecord.class);
        util.exportExcel(response, list, "異動報表資料");
    }

    /**
     * 取得掃描記錄報表資料
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.VIEW + "')")
    @GetMapping("/scan")
    public TableDataInfo getScanReport(InvScanLog invScanLog) {
        startPage();
        List<InvScanLog> list = invScanLogService.selectInvScanLogList(invScanLog);

        // 統計資料（使用全部資料）
        List<InvScanLog> allList = invScanLogService.selectInvScanLogList(new InvScanLog());
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalScans", allList.size());
        statistics.put("successScans", allList.stream().filter(s -> ScanResult.SUCCESS.getCode().equals(s.getScanResult())).count());
        statistics.put("failedScans", allList.stream().filter(s -> ScanResult.FAILURE.getCode().equals(s.getScanResult())).count());
        statistics.put("barcodeScans", allList.stream().filter(s -> ScanType.BARCODE.getCode().equals(s.getScanType())).count());
        statistics.put("qrcodeScans", allList.stream().filter(s -> ScanType.QRCODE.getCode().equals(s.getScanType())).count());

        TableDataInfo dataInfo = getDataTable(list);
        dataInfo.put("statistics", statistics);
        return dataInfo;
    }

    /**
     * 匯出掃描報表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.EXPORT + "')")
    @Log(title = "掃描報表", businessType = BusinessType.EXPORT)
    @PostMapping("/scan/export")
    public void exportScanReport(HttpServletResponse response, InvScanLog invScanLog) {
        List<InvScanLog> list = invScanLogService.selectInvScanLogList(invScanLog);
        ExcelUtil<InvScanLog> util = new ExcelUtil<InvScanLog>(InvScanLog.class);
        util.exportExcel(response, list, "掃描報表資料");
    }


    /**
     * 取得儀表板統計資料
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Inventory.Report.VIEW + "')")
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
        dashboard.put("pendingBorrows", borrowList.stream().filter(b -> BorrowStatus.PENDING.getCode().equals(b.getStatus())).count());
        dashboard.put("overdueBorrows", invBorrowService.selectOverdueBorrowList(null).size());

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
