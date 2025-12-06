package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvReturn;
import com.cheng.system.domain.enums.BorrowStatus;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.mapper.InvReturnMapper;
import com.cheng.system.service.IInvBorrowService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/inventory/borrow")
@RequiredArgsConstructor
public class InvBorrowController extends BaseController {

    private final IInvBorrowService invBorrowService;
    private final InvReturnMapper invReturnMapper;
    private final InvItemMapper invItemMapper;

    /**
     * 查詢借出記錄列表
     * 如果沒有 inventory:borrow:all 權限，只能查看自己的借出記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvBorrow invBorrow) {
        Long currentUserId = getUserId();
        boolean isAdmin = getLoginUser().getUser().isAdmin();
        boolean hasAllPermission = isAdmin || getLoginUser().getPermissions().contains("inventory:borrow:all");
        
        log.info("=== 借出記錄查詢權限檢查 ===");
        log.info("當前使用者ID: {}", currentUserId);
        log.info("是否管理員: {}", isAdmin);
        log.info("是否有 inventory:borrow:all 權限: {}", getLoginUser().getPermissions().contains("inventory:borrow:all"));
        log.info("hasAllPermission: {}", hasAllPermission);
        log.info("查詢參數 - borrowerId (before): {}", invBorrow.getBorrowerId());
        
        // 沒有權限時，只能查看自己的借出記錄
        if (!hasAllPermission) {
            invBorrow.setBorrowerId(currentUserId);
            log.info("❌ 無全局查看權限，設定 borrowerId: {}", currentUserId);
        } else {
            log.info("✅ 有全局查看權限，可查看所有記錄");
        }
        
        log.info("查詢參數 - borrowerId (after): {}", invBorrow.getBorrowerId());
        log.info("查詢參數 - status: {}", invBorrow.getStatus());
        log.info("查詢參數 - itemName: {}", invBorrow.getItemName());
        
        startPage();
        List<InvBorrow> list = invBorrowService.selectInvBorrowList(invBorrow);
        
        log.info("查詢結果數量: {}", list.size());
        if (!list.isEmpty()) {
            log.info("第一筆記錄的 borrowerId: {}", list.get(0).getBorrowerId());
        }
        
        return getDataTable(list);
    }

    /**
     * 查詢逾期借出記錄列表
     * 如果沒有 inventory:borrow:all 權限，只能查看自己的逾期記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/overdue")
    public TableDataInfo overdueList() {
        // 檢查是否有查看所有記錄的權限
        boolean hasAllPermission = getLoginUser().getUser().isAdmin() || 
                                    getLoginUser().getPermissions().contains("inventory:borrow:all");
        
        // 沒有權限時，只能查看自己的逾期記錄
        Long borrowerId = hasAllPermission ? null : getUserId();
        
        startPage();
        List<InvBorrow> list = invBorrowService.selectOverdueBorrowList(borrowerId);
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
     * 支援根據搜尋條件過濾統計結果
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:list')")
    @GetMapping("/stats")
    public AjaxResult getBorrowStats(InvBorrow searchParams) {
        try {
            Long currentUserId = getUserId();
            boolean isAdmin = getLoginUser().getUser().isAdmin();
            boolean hasAllPermission = isAdmin || getLoginUser().getPermissions().contains("inventory:borrow:all");
            
            log.info("=== 借出統計查詢權限檢查 ===");
            log.info("當前使用者ID: {}", currentUserId);
            log.info("是否管理員: {}", isAdmin);
            log.info("hasAllPermission: {}", hasAllPermission);
            
            // 沒有權限時，只能查看自己的統計
            if (!hasAllPermission) {
                searchParams.setBorrowerId(currentUserId);
                log.info("❌ 無全局查看權限，統計僅限使用者: {}", currentUserId);
            } else {
                log.info("✅ 有全局查看權限，統計所有記錄");
            }
            
            // 取得借出統計資料
            Map<String, Object> stats = new HashMap<>();

            // 待審核數量
            InvBorrow pendingQuery = copySearchParams(searchParams);
            pendingQuery.setStatusEnum(BorrowStatus.PENDING);
            int pendingCount = invBorrowService.selectInvBorrowList(pendingQuery).size();
            stats.put("pending", pendingCount);

            // 已借出數量（包含部分歸還）
            InvBorrow borrowedQuery = copySearchParams(searchParams);
            borrowedQuery.setStatusEnum(BorrowStatus.BORROWED);
            int borrowedCount = invBorrowService.selectInvBorrowList(borrowedQuery).size();

            InvBorrow partialReturnQuery = copySearchParams(searchParams);
            partialReturnQuery.setStatusEnum(BorrowStatus.PARTIAL_RETURNED);
            int partialReturnCount = invBorrowService.selectInvBorrowList(partialReturnQuery).size();

            stats.put("borrowed", borrowedCount + partialReturnCount);

            // 已歸還數量
            InvBorrow returnedQuery = copySearchParams(searchParams);
            returnedQuery.setStatusEnum(BorrowStatus.RETURNED);
            int returnedCount = invBorrowService.selectInvBorrowList(returnedQuery).size();
            stats.put("returned", returnedCount);

            // 逾期數量（根據權限篩選）
            Long overdueQueryBorrowerId = hasAllPermission ? null : currentUserId;
            int overdueCount = invBorrowService.selectOverdueBorrowList(overdueQueryBorrowerId).size();
            stats.put("overdue", overdueCount);
            
            log.info("統計結果 - pending: {}, borrowed: {}, returned: {}, overdue: {}", 
                       pendingCount, borrowedCount + partialReturnCount, returnedCount, overdueCount);

            return success(stats);
        } catch (Exception e) {
            log.error("取得借出統計資料失敗", e);
            return error("取得借出統計資料失敗：" + e.getMessage());
        }
    }
    
    /**
     * 複製搜尋參數（用於統計查詢）
     */
    private InvBorrow copySearchParams(InvBorrow source) {
        InvBorrow target = new InvBorrow();
        target.setBorrowerId(source.getBorrowerId());
        target.setItemName(source.getItemName());
        target.setBorrowerName(source.getBorrowerName());
        target.setParams(source.getParams());
        return target;
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
     * 新增借出申請（提交待審核）
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:add')")
    @Log(title = "借出申請", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InvBorrow invBorrow) {
        try {
            // 自動填充借出人資訊為當前登入使用者
            if (invBorrow.getBorrowerId() == null) {
                invBorrow.setBorrowerId(getUserId());
            }

            // 如果沒有提供借出單號，自動產生
            if (invBorrow.getBorrowNo() == null || invBorrow.getBorrowNo().isEmpty()) {
                invBorrow.setBorrowNo(invBorrowService.generateBorrowNo());
            }

            // 如果沒有提供借出時間，使用當前時間
            if (invBorrow.getBorrowTime() == null) {
                invBorrow.setBorrowTime(new java.util.Date());
            }

            // 設定初始狀態為待審核
            invBorrow.setStatusEnum(BorrowStatus.PENDING);

            if (!invBorrowService.checkBorrowNoUnique(invBorrow)) {
                return error("新增借出申請失敗，借出單號已存在");
            }

            // 提交借出申請（不扣減庫存，等待審核）
            int result = invBorrowService.borrowItem(invBorrow);
            return toAjax(result);
        } catch (Exception e) {
            return error("新增借出申請失敗：" + e.getMessage());
        }
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

            // 取得物品名稱用於日誌記錄
            InvItem item = invItemMapper.selectInvItemByItemId(invBorrow.getItemId());
            String itemName = item != null ? item.getItemName() : "未知物品";

            int result = invBorrowService.borrowItem(invBorrow);
            
            // 將借出的物品名稱加入返回結果，讓操作日誌能記錄
            AjaxResult ajaxResult = toAjax(result);
            ajaxResult.put("itemName", itemName);
            log.info("借出物品：{}", itemName);
            
            return ajaxResult;
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
            // 取得借出記錄用於操作日誌
            InvBorrow borrow = invBorrowService.selectInvBorrowByBorrowId(request.getBorrowId());
            String borrowNo = borrow != null ? borrow.getBorrowNo() : "未知單號";
            String itemName = borrow != null ? borrow.getItemName() : "未知物品";
            
            int result = invBorrowService.returnItem(request.getBorrowId(), request.getReturnQuantity(),
                    getUserId(), request.getConditionDesc(), request.getIsDamaged(), request.getDamageDesc(), request.getRemark());
            
            // 記錄操作日誌資訊
            AjaxResult ajaxResult = toAjax(result);
            ajaxResult.put("borrowNo", borrowNo);
            ajaxResult.put("itemName", itemName);
            ajaxResult.put("returnQuantity", request.getReturnQuantity());
            log.info("歸還物品：借出單號：{}，物品：{}，歸還數量：{}", borrowNo, itemName, request.getReturnQuantity());
            
            return ajaxResult;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
    
    /**
     * 遺失物品
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:return')")
    @Log(title = "遺失物品", businessType = BusinessType.UPDATE)
    @PostMapping("/lostItem")
    public AjaxResult lostItem(@RequestBody ReturnRequest request) {
        try {
            // 取得借出記錄用於日誌（包含借用人和借出單號，方便追溯）
            InvBorrow borrow = invBorrowService.selectInvBorrowByBorrowId(request.getBorrowId());
            String itemName = borrow != null ? borrow.getItemName() : "未知物品";
            String borrowerName = borrow != null ? borrow.getBorrowerName() : "未知借用人";
            String borrowNo = borrow != null ? borrow.getBorrowNo() : "未知單號";
            
            int result = invBorrowService.returnItem(request.getBorrowId(), request.getReturnQuantity(),
                    getUserId(), "lost", "0", null, request.getRemark());
            
            // 將遺失的詳細資訊加入返回結果，讓操作日誌能記錄
            AjaxResult ajaxResult = toAjax(result);
            ajaxResult.put("itemName", itemName);
            ajaxResult.put("quantity", request.getReturnQuantity());
            ajaxResult.put("borrowerName", borrowerName);  // 借用人姓名
            ajaxResult.put("borrowNo", borrowNo);          // 借出單號（方便從借出管理查詢）
            log.info("遺失物品：{}，數量：{}，借用人：{}，借出單號：{}", itemName, request.getReturnQuantity(), borrowerName, borrowNo);
            
            return ajaxResult;
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
            // 取得借出記錄用於操作日誌
            InvBorrow borrow = invBorrowService.selectInvBorrowByBorrowId(request.getBorrowId());
            String borrowNo = borrow != null ? borrow.getBorrowNo() : "未知單號";
            String itemName = borrow != null ? borrow.getItemName() : "未知物品";
            String borrowerName = borrow != null ? borrow.getBorrowerName() : "未知借用人";
            
            int result = invBorrowService.approveBorrow(request.getBorrowId(), getUserId(),
                    getUsername(), request.isApproved(), request.getApproveRemark());
            
            // 記錄操作日誌資訊
            AjaxResult ajaxResult = toAjax(result);
            ajaxResult.put("borrowNo", borrowNo);
            ajaxResult.put("itemName", itemName);
            ajaxResult.put("borrowerName", borrowerName);
            ajaxResult.put("approved", request.isApproved());
            log.info("審核借出申請：借出單號：{}，物品：{}，借用人：{}，審核結果：{}", 
                    borrowNo, itemName, borrowerName, request.isApproved() ? "通過" : "拒絕");
            
            return ajaxResult;
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
        try {
            return toAjax(invBorrowService.updateInvBorrow(invBorrow));
        } catch (Exception e) {
            return error("修改借出記錄失敗：" + e.getMessage());
        }
    }

    /**
     * 刪除借出記錄
     * 刪除未歸還的記錄時會自動恢復庫存
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:remove')")
    @Log(title = "借出記錄", businessType = BusinessType.DELETE)
    @DeleteMapping("/{borrowIds}")
    public AjaxResult remove(@PathVariable Long[] borrowIds) {
        try {
            return toAjax(invBorrowService.deleteInvBorrowByBorrowIds(borrowIds));
        } catch (Exception e) {
            return error("刪除借出記錄失敗：" + e.getMessage());
        }
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
     * 查詢借出記錄的歸還記錄
     */
    @PreAuthorize("@ss.hasPermi('inventory:borrow:query')")
    @GetMapping("/returnRecords/{borrowId}")
    public AjaxResult getReturnRecords(@PathVariable Long borrowId) {
        try {
            List<InvReturn> returnRecords = invReturnMapper.selectInvReturnByBorrowId(borrowId);
            return success(returnRecords);
        } catch (Exception e) {
            return error("查詢歸還記錄失敗：" + e.getMessage());
        }
    }

    /**
     * 歸還請求物件
     */
    @Setter
    @Getter
    public static class ReturnRequest {
        private Long borrowId;
        private Integer returnQuantity;
        private String conditionDesc;
        private String isDamaged;
        private String damageDesc;
        private String remark;
    }

    /**
     * 審核請求物件
     */
    @Setter
    @Getter
    public static class ApproveRequest {
        private Long borrowId;
        private boolean approved;
        private String approveRemark;  // 審核備註（拒絕原因）
    }
}
