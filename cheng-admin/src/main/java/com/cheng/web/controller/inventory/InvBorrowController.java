package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.service.IInvBorrowService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借出記錄Controller
 *
 * @author cheng
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/inventory/borrow")
public class InvBorrowController extends BaseController {
    @Autowired
    private IInvBorrowService invBorrowService;

    /**
     * 查詢借出記錄列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvBorrow invBorrow) {
        startPage();
        List<InvBorrow> list = invBorrowService.selectInvBorrowList(invBorrow);
        return getDataTable(list);
    }

    /**
     * 查詢逾期借出記錄列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/overdue")
    public TableDataInfo overdueList() {
        startPage();
        List<InvBorrow> list = invBorrowService.selectOverdueBorrowList();
        return getDataTable(list);
    }

    /**
     * 查詢我的借出記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/my")
    public TableDataInfo myBorrowList() {
        startPage();
        List<InvBorrow> list = invBorrowService.selectBorrowListByBorrowerId(getUserId());
        return getDataTable(list);
    }

    /**
     * 匯出借出記錄列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:export')")
    @Log(title = "借出記錄", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvBorrow invBorrow) {
        List<InvBorrow> list = invBorrowService.selectInvBorrowList(invBorrow);
        ExcelUtil<InvBorrow> util = new ExcelUtil<InvBorrow>(InvBorrow.class);
        util.exportExcel(response, list, "借出記錄資料");
    }

    /**
     * 取得借出統計資料
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/stats")
    public AjaxResult getBorrowStats() {
        try {
            // 取得借出統計資料
            Map<String, Object> stats = new HashMap<>();

            // 總借出記錄數
            int totalBorrows = invBorrowService.selectInvBorrowList(new InvBorrow()).size();
            stats.put("totalBorrows", totalBorrows);

            // 待審核數量
            InvBorrow pendingQuery = new InvBorrow();
            pendingQuery.setStatus("0");
            int pendingCount = invBorrowService.selectInvBorrowList(pendingQuery).size();
            stats.put("pendingCount", pendingCount);

            // 已借出數量
            InvBorrow borrowedQuery = new InvBorrow();
            borrowedQuery.setStatus("1");
            int borrowedCount = invBorrowService.selectInvBorrowList(borrowedQuery).size();
            stats.put("borrowedCount", borrowedCount);

            // 逾期數量
            int overdueCount = invBorrowService.selectOverdueBorrowList().size();
            stats.put("overdueCount", overdueCount);

            return success(stats);
        } catch (Exception e) {
            return error("取得借出統計資料失敗：" + e.getMessage());
        }
    }

    /**
     * 取得借出記錄詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:query')")
    @GetMapping(value = "/{borrowId}")
    public AjaxResult getInfo(@PathVariable("borrowId") Long borrowId) {
        return success(invBorrowService.selectInvBorrowByBorrowId(borrowId));
    }

    /**
     * 根據借出單號取得借出記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:query')")
    @GetMapping(value = "/no/{borrowNo}")
    public AjaxResult getInfoByNo(@PathVariable("borrowNo") String borrowNo) {
        InvBorrow borrow = invBorrowService.selectInvBorrowByBorrowNo(borrowNo);
        if (borrow != null) {
            return success(borrow);
        } else {
            return error("未找到對應的借出記錄");
        }
    }

    /**
     * 新增借出記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:add')")
    @Log(title = "借出記錄", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InvBorrow invBorrow) {
        if (!invBorrowService.checkBorrowNoUnique(invBorrow)) {
            return error("新增借出記錄失敗，借出單號已存在");
        }
        if (!invBorrowService.checkItemAvailable(invBorrow.getItemId(), invBorrow.getQuantity())) {
            return error("新增借出記錄失敗，物品庫存不足");
        }
        return toAjax(invBorrowService.insertInvBorrow(invBorrow));
    }

    /**
     * 借出物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:borrow')")
    @Log(title = "借出物品", businessType = BusinessType.INSERT)
    @PostMapping("/borrowItem")
    public AjaxResult borrowItem(@Validated @RequestBody InvBorrow invBorrow) {
        try {
            // 自動產生借出單號
            if (invBorrow.getBorrowNo() == null || invBorrow.getBorrowNo().isEmpty()) {
                invBorrow.setBorrowNo(invBorrowService.generateBorrowNo());
            }

            int result = invBorrowService.borrowItem(invBorrow);
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 歸還物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:return')")
    @Log(title = "歸還物品", businessType = BusinessType.UPDATE)
    @PostMapping("/returnItem")
    public AjaxResult returnItem(@RequestBody ReturnRequest request) {
        try {
            int result = invBorrowService.returnItem(request.getBorrowId(), request.getReturnQuantity(),
                    getUserId(), request.getConditionDesc(),
                    request.getIsDamaged(), request.getDamageDesc());
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 審核借出申請
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:approve')")
    @Log(title = "審核借出申請", businessType = BusinessType.UPDATE)
    @PostMapping("/approve")
    public AjaxResult approve(@RequestBody ApproveRequest request) {
        try {
            int result = invBorrowService.approveBorrow(request.getBorrowId(), getUserId(),
                    getUsername(), request.isApproved());
            return toAjax(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 修改借出記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:edit')")
    @Log(title = "借出記錄", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InvBorrow invBorrow) {
        return toAjax(invBorrowService.updateInvBorrow(invBorrow));
    }

    /**
     * 刪除借出記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:remove')")
    @Log(title = "借出記錄", businessType = BusinessType.DELETE)
    @DeleteMapping("/{borrowIds}")
    public AjaxResult remove(@PathVariable Long[] borrowIds) {
        return toAjax(invBorrowService.deleteInvBorrowByBorrowIds(borrowIds));
    }

    /**
     * 檢查物品是否可借出
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:query')")
    @GetMapping("/checkAvailable/{itemId}/{quantity}")
    public AjaxResult checkAvailable(@PathVariable Long itemId, @PathVariable Integer quantity) {
        boolean available = invBorrowService.checkItemAvailable(itemId, quantity);
        return success(available);
    }

    /**
     * 產生借出單號
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:add')")
    @GetMapping("/generateNo")
    public AjaxResult generateBorrowNo() {
        String borrowNo = invBorrowService.generateBorrowNo();
        return success(borrowNo);
    }

    /**
     * 歸還請求物件
     */
    public static class ReturnRequest {
        private Long borrowId;
        private Integer returnQuantity;
        private String conditionDesc;
        private String isDamaged;
        private String damageDesc;

        public Long getBorrowId() {
            return borrowId;
        }

        public void setBorrowId(Long borrowId) {
            this.borrowId = borrowId;
        }

        public Integer getReturnQuantity() {
            return returnQuantity;
        }

        public void setReturnQuantity(Integer returnQuantity) {
            this.returnQuantity = returnQuantity;
        }

        public String getConditionDesc() {
            return conditionDesc;
        }

        public void setConditionDesc(String conditionDesc) {
            this.conditionDesc = conditionDesc;
        }

        public String getIsDamaged() {
            return isDamaged;
        }

        public void setIsDamaged(String isDamaged) {
            this.isDamaged = isDamaged;
        }

        public String getDamageDesc() {
            return damageDesc;
        }

        public void setDamageDesc(String damageDesc) {
            this.damageDesc = damageDesc;
        }
    }

    /**
     * 審核請求物件
     */
    public static class ApproveRequest {
        private Long borrowId;
        private boolean approved;

        public Long getBorrowId() {
            return borrowId;
        }

        public void setBorrowId(Long borrowId) {
            this.borrowId = borrowId;
        }

        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }
    }
}
