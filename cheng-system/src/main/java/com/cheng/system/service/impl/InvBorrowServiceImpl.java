package com.cheng.system.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvReturn;
import com.cheng.system.domain.InvStock;
import com.cheng.system.domain.enums.BorrowStatus;
import com.cheng.system.domain.enums.ItemCondition;
import com.cheng.system.domain.enums.ReserveStatus;
import com.cheng.common.enums.YesNo;
import com.cheng.system.mapper.InvBorrowMapper;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.mapper.InvReturnMapper;
import com.cheng.system.mapper.InvStockMapper;
import com.cheng.system.service.IInvBorrowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 借出記錄Service業務層處理
 *
 * @author cheng
 * @since 2025-09-23
 */
@Slf4j
@Service
public class InvBorrowServiceImpl implements IInvBorrowService {
    @Autowired
    private InvBorrowMapper invBorrowMapper;

    @Autowired
    private InvStockMapper invStockMapper;

    @Autowired
    private InvReturnMapper invReturnMapper;

    @Autowired
    private InvItemMapper invItemMapper;

    /**
     * 查詢借出記錄
     *
     * @param borrowId 借出記錄主鍵
     * @return 借出記錄
     */
    @Override
    public InvBorrow selectInvBorrowByBorrowId(Long borrowId) {
        return invBorrowMapper.selectInvBorrowByBorrowId(borrowId);
    }

    /**
     * 根據借出單號查詢借出記錄
     *
     * @param borrowNo 借出單號
     * @return 借出記錄
     */
    @Override
    public InvBorrow selectInvBorrowByBorrowNo(String borrowNo) {
        return invBorrowMapper.selectInvBorrowByBorrowNo(borrowNo);
    }

    /**
     * 查詢借出記錄列表
     *
     * @param invBorrow 借出記錄
     * @return 借出記錄
     */
    @Override
    public List<InvBorrow> selectInvBorrowList(InvBorrow invBorrow) {
        return invBorrowMapper.selectInvBorrowList(invBorrow);
    }

    /**
     * 查詢逾期借出記錄列表
     *
     * @param borrowerId 借出人ID（可選，若為null則查詢所有逾期記錄）
     * @return 借出記錄集合
     */
    @Override
    public List<InvBorrow> selectOverdueBorrowList(Long borrowerId) {
        return invBorrowMapper.selectOverdueBorrowList(borrowerId);
    }

    /**
     * 查詢用戶的借出記錄
     *
     * @param borrowerId 借出人ID
     * @return 借出記錄集合
     */
    @Override
    public List<InvBorrow> selectBorrowListByBorrowerId(Long borrowerId) {
        return invBorrowMapper.selectBorrowListByBorrowerId(borrowerId);
    }

    /**
     * 查詢物品的借出記錄
     *
     * @param itemId 物品ID
     * @return 借出記錄集合
     */
    @Override
    public List<InvBorrow> selectBorrowListByItemId(Long itemId) {
        return invBorrowMapper.selectBorrowListByItemId(itemId);
    }

    /**
     * 新增借出記錄
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    @Override
    public int insertInvBorrow(InvBorrow invBorrow) {
        invBorrow.setCreateTime(DateUtils.getNowDate());
        return invBorrowMapper.insertInvBorrow(invBorrow);
    }

    /**
     * 修改借出記錄
     * 如果修改了借出數量，需要同步更新庫存
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    @Override
    @Transactional
    public int updateInvBorrow(InvBorrow invBorrow) {
        // 取得原本的借出記錄
        InvBorrow oldBorrow = invBorrowMapper.selectInvBorrowByBorrowId(invBorrow.getBorrowId());
        if (oldBorrow == null) {
            throw new RuntimeException("借出記錄不存在");
        }

        // 🚨 安全檢查：禁止修改預約狀態的記錄
        if (oldBorrow.getReserveStatus() != null && oldBorrow.getReserveStatus() == ReserveStatus.PENDING.getCode()) {
            throw new RuntimeException("預約記錄不允許修改，請先取消預約或等待審核");
        }

        // 檢查是否修改了借出數量
        if (invBorrow.getQuantity() != null && !invBorrow.getQuantity().equals(oldBorrow.getQuantity())) {
            int quantityDiff = invBorrow.getQuantity() - oldBorrow.getQuantity();

            // 只有已借出、部分歸還、逾期狀態才需要更新庫存
            // 待審核和審核拒絕狀態不需要更新庫存
            if (oldBorrow.needsReturn()) {
                // 如果增加借出數量，需要檢查庫存是否足夠
                if (quantityDiff > 0) {
                    if (!checkItemAvailable(oldBorrow.getItemId(), quantityDiff)) {
                        throw new RuntimeException("物品庫存不足，無法增加借出數量");
                    }
                }

                // 更新庫存
                InvStock stock = invStockMapper.selectInvStockByItemId(oldBorrow.getItemId());
                if (stock != null) {
                    // 可用數量 = 原可用數量 - 數量差異（增加借出則減少可用，減少借出則增加可用）
                    stock.setAvailableQty(stock.getAvailableQty() - quantityDiff);
                    // 借出數量 = 原借出數量 + 數量差異
                    stock.setBorrowedQty(stock.getBorrowedQty() + quantityDiff);
                    stock.setUpdateTime(DateUtils.getNowDate());
                    invStockMapper.updateInvStock(stock);
                }
            }
        }

        invBorrow.setUpdateTime(DateUtils.getNowDate());
        return invBorrowMapper.updateInvBorrow(invBorrow);
    }

    /**
     * 提交借出申請（不扣減庫存，等待審核）
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    @Override
    @Transactional
    public int borrowItem(InvBorrow invBorrow) {
        // 檢查物品是否可借出（檢查可用庫存）
        if (!checkItemAvailable(invBorrow.getItemId(), invBorrow.getQuantity())) {
            throw new RuntimeException("物品庫存不足，無法提交借出申請");
        }

        // 取得物品資訊並儲存到借出記錄（冗餘設計，保留歷史記錄）
        InvItem item = invItemMapper.selectInvItemByItemId(invBorrow.getItemId());
        if (item != null) {
            invBorrow.setItemName(item.getItemName());
            invBorrow.setItemCode(item.getItemCode());
        }

        // 設定借出時間
        invBorrow.setBorrowTime(DateUtils.getNowDate());
        // 設定為待審核狀態
        invBorrow.setStatusEnum(BorrowStatus.PENDING);
        invBorrow.setCreateTime(DateUtils.getNowDate());

        // 新增借出記錄（不扣減庫存，等待審核）
        return invBorrowMapper.insertInvBorrow(invBorrow);
    }

    /**
     * 歸還物品
     *
     * @param borrowId       借出記錄ID
     * @param returnQuantity 歸還數量
     * @param returnerId     歸還人ID
     * @param conditionDesc  物品狀況描述
     * @param isDamaged      是否損壞
     * @param damageDesc     損壞描述
     * @param remark         說明/備註
     * @return 結果
     */
    @Override
    @Transactional
    public int returnItem(Long borrowId, Integer returnQuantity, Long returnerId,
                          String conditionDesc, String isDamaged, String damageDesc, String remark) {
        // 日誌：記錄傳入參數
        log.info("歸還物品 - borrowId: {}, returnQuantity: {}, conditionDesc: {}, isDamaged: {}, damageDesc: {}, remark: {}", 
                 borrowId, returnQuantity, conditionDesc, isDamaged, damageDesc, remark);
        
        // 查詢借出記錄
        InvBorrow borrow = invBorrowMapper.selectInvBorrowByBorrowId(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借出記錄不存在");
        }
        
        // 🚨 權限檢查：只有借出人本人或管理員可以歸還
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.getLoginUser().getUser().isAdmin();
        
        if (!isAdmin && !currentUserId.equals(borrow.getBorrowerId())) {
            log.warn("權限不足 - 使用者ID: {} 嘗試歸還 借出人ID: {} 的物品", currentUserId, borrow.getBorrowerId());
            throw new RuntimeException("權限不足：只有借出人本人或管理員可以歸還物品");
        }

        // 檢查歸還數量
        int remainingQty = borrow.getQuantity() - borrow.getReturnQuantity();
        if (returnQuantity > remainingQty) {
            throw new RuntimeException("歸還數量超過借出數量");
        }

        // 更新借出記錄
        borrow.setReturnQuantity(borrow.getReturnQuantity() + returnQuantity);
        borrow.setActualReturn(DateUtils.getNowDate());
        borrow.setUpdateTime(DateUtils.getNowDate());

        // 判斷是否完全歸還
        if (borrow.getReturnQuantity().equals(borrow.getQuantity())) {
            borrow.setStatusEnum(BorrowStatus.RETURNED); // 已歸還
        } else {
            borrow.setStatusEnum(BorrowStatus.PARTIAL_RETURNED); // 部分歸還
        }

        int result = invBorrowMapper.updateInvBorrow(borrow);

        // 更新庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(borrow.getItemId());
        if (stock != null) {
            // 減少借出數量
            stock.setBorrowedQty(stock.getBorrowedQty() - returnQuantity);

            // 根據物品狀況更新庫存
            ItemCondition condition = ItemCondition.getByDescription(conditionDesc);
            if (condition == null && YesNo.YES.getCodeAsString().equals(isDamaged)) {
                condition = ItemCondition.DAMAGED;
            }
            
            if (condition == ItemCondition.LOST) {
                // 遺失：總數量減少，增加遺失數量，可用數量不變
                log.info("處理遺失物品 - 原總數量: {}, 原遺失數量: {}", stock.getTotalQuantity(), stock.getLostQty());
                stock.setTotalQuantity(stock.getTotalQuantity() - returnQuantity);
                // 處理 lostQty 可能為 null 的情況
                Integer currentLostQty = stock.getLostQty();
                stock.setLostQty((currentLostQty != null ? currentLostQty : 0) + returnQuantity);
                log.info("處理遺失物品 - 新總數量: {}, 新遺失數量: {}", stock.getTotalQuantity(), stock.getLostQty());
            } else if (condition == ItemCondition.DAMAGED) {
                // 損壞：增加損壞數量，同時增加可用數量（損壞的物品仍可使用或維修後使用）
                log.info("處理損壞物品 - 原損壞數量: {}, 原可用數量: {}", stock.getDamagedQty(), stock.getAvailableQty());
                stock.setDamagedQty(stock.getDamagedQty() + returnQuantity);
                stock.setAvailableQty(stock.getAvailableQty() + returnQuantity);
                log.info("處理損壞物品 - 新損壞數量: {}, 新可用數量: {}", stock.getDamagedQty(), stock.getAvailableQty());
            } else {
                // 完好：增加可用數量
                log.info("處理完好物品 - 原可用數量: {}, 新可用數量: {}", stock.getAvailableQty(), stock.getAvailableQty() + returnQuantity);
                stock.setAvailableQty(stock.getAvailableQty() + returnQuantity);
            }

            stock.setUpdateTime(DateUtils.getNowDate());
            invStockMapper.updateInvStock(stock);
        }

        // 建立歸還記錄
        InvReturn invReturn = new InvReturn();
        invReturn.setBorrowId(borrowId);
        invReturn.setBorrowCode(borrow.getBorrowNo());
        invReturn.setItemId(borrow.getItemId());
        // itemCode 和 itemName 透過 JOIN 從 inv_item 表取得，不需要手動設定
        invReturn.setReturnQuantity(returnQuantity.longValue());
        invReturn.setBorrowerId(borrow.getBorrowerId());
        invReturn.setBorrowerName(borrow.getBorrowerName());
        invReturn.setReturnTime(DateUtils.getNowDate());
        invReturn.setExpectedReturn(borrow.getExpectedReturn());

        // 判斷是否逾期
        if (borrow.getExpectedReturn() != null && DateUtils.getNowDate().after(borrow.getExpectedReturn())) {
            invReturn.setIsOverdue(YesNo.YES.getCodeAsString());
            long diffInMillies = DateUtils.getNowDate().getTime() - borrow.getExpectedReturn().getTime();
            long overdueDays = diffInMillies / (24 * 60 * 60 * 1000);
            invReturn.setOverdueDays(overdueDays);
        } else {
            invReturn.setIsOverdue(YesNo.NO.getCodeAsString());
            invReturn.setOverdueDays(0L);
        }

        // 設定物品狀況
        ItemCondition finalCondition = ItemCondition.getByDescription(conditionDesc);
        if (finalCondition == null && YesNo.YES.getCodeAsString().equals(isDamaged)) {
            finalCondition = ItemCondition.DAMAGED;
        }
        if (finalCondition == null) {
            finalCondition = ItemCondition.GOOD;
        }
        
        invReturn.setItemCondition(finalCondition.getCode());
        if (finalCondition == ItemCondition.DAMAGED) {
            invReturn.setDamageDescription(damageDesc);
        }

        invReturn.setReceiverId(returnerId);
        invReturn.setReturnStatus(YesNo.YES.getCodeAsString()); // 已確認
        invReturn.setRemark(remark); // 設定說明/備註
        invReturn.setCreateTime(DateUtils.getNowDate());

        // 儲存歸還記錄
        invReturnMapper.insertInvReturn(invReturn);

        return result;
    }

    /**
     * 審核借出申請
     *
     * @param borrowId     借出記錄ID
     * @param approverId   審核人ID
     * @param approverName 審核人姓名
     * @param isApproved   是否通過審核
     * @param approveRemark 審核備註（拒絕原因）
     * @return 結果
     */
    @Override
    @Transactional
    public int approveBorrow(Long borrowId, Long approverId, String approverName, boolean isApproved, String approveRemark) {
        InvBorrow borrow = invBorrowMapper.selectInvBorrowByBorrowId(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借出記錄不存在");
        }

        // 檢查是否為待審核狀態
        if (!borrow.isPending()) {
            throw new RuntimeException("只能審核待審核狀態的借出申請");
        }

        // 加入日誌追蹤
        log.info("審核借出申請 - borrowId: {}, isApproved: {}, reserveStatus: {}, status: {}", 
                borrowId, isApproved, borrow.getReserveStatus(), borrow.getStatus());
        
        borrow.setApproverId(approverId);
        borrow.setApproverName(approverName);
        borrow.setApproveTime(DateUtils.getNowDate());
        borrow.setApproveRemark(approveRemark);  // 設定審核備註
        borrow.setUpdateTime(DateUtils.getNowDate());

        InvStock stock = invStockMapper.selectInvStockByItemId(borrow.getItemId());
        if (isApproved) {
            // 更新庫存
            if (stock != null) {
                log.info("當前庫存狀態 - available: {}, reserved: {}, borrowed: {}", 
                        stock.getAvailableQty(), stock.getReservedQty(), stock.getBorrowedQty());
                
                // 判斷是否為預約記錄（reserve_status = 1 代表待審核預約）
                if (borrow.getReserveStatus() != null && borrow.getReserveStatus() == ReserveStatus.PENDING.getCode()) {
                    log.info("處理預約記錄 - 從 reserved_qty 轉移到 borrowed_qty");
                    // 預約記錄：庫存已在預約時扣除，只需從 reserved_qty 轉移到 borrowed_qty
                    stock.setReservedQty(stock.getReservedQty() - borrow.getQuantity());
                    stock.setBorrowedQty(stock.getBorrowedQty() + borrow.getQuantity());
                    
                    // 更新預約狀態為已通過
                    borrow.setReserveStatus(ReserveStatus.APPROVED.getCode());
                } else {
                    // 一般借出記錄：檢查庫存並扣減
                    if (!checkItemAvailable(borrow.getItemId(), borrow.getQuantity())) {
                        throw new RuntimeException("物品庫存不足，無法審核通過");
                    }
                    
                    stock.setAvailableQty(stock.getAvailableQty() - borrow.getQuantity());
                    stock.setBorrowedQty(stock.getBorrowedQty() + borrow.getQuantity());
                }
                
                stock.setUpdateTime(DateUtils.getNowDate());
                invStockMapper.updateInvStock(stock);
            }

            // 設定為已借出狀態
            borrow.setStatusEnum(BorrowStatus.BORROWED);
        } else {
            // 審核拒絕
            if (stock != null) {
                // 判斷是否為預約記錄
                if (borrow.getReserveStatus() != null && borrow.getReserveStatus() == ReserveStatus.PENDING.getCode()) {
                    // 預約記錄被拒絕：歸還預留的庫存
                    stock.setReservedQty(stock.getReservedQty() - borrow.getQuantity());
                    stock.setAvailableQty(stock.getAvailableQty() + borrow.getQuantity());
                    stock.setUpdateTime(DateUtils.getNowDate());
                    invStockMapper.updateInvStock(stock);
                    
                    // 更新預約狀態為已拒絕
                    borrow.setReserveStatus(ReserveStatus.REJECTED.getCode());
                }
                // 一般借出記錄被拒絕：不需要恢復庫存（因為還沒扣減）
            }
            
            borrow.setStatusEnum(BorrowStatus.REJECTED);
        }

        return invBorrowMapper.updateInvBorrow(borrow);
    }

    /**
     * 批量刪除借出記錄
     * 刪除時需要恢復庫存（只處理未歸還的記錄）
     *
     * @param borrowIds 需要刪除的借出記錄主鍵集合
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteInvBorrowByBorrowIds(Long[] borrowIds) {
        // 先恢復庫存
        for (Long borrowId : borrowIds) {
            InvBorrow borrow = invBorrowMapper.selectInvBorrowByBorrowId(borrowId);
            if (borrow != null) {
                InvStock stock = invStockMapper.selectInvStockByItemId(borrow.getItemId());
                if (stock != null) {
                    // 判斷記錄類型並恢復庫存
                    if (borrow.getReserveStatus() != null && borrow.getReserveStatus() == 1 && borrow.isPending()) {
                        // 待審核的預約記錄：恢復預留的庫存
                        stock.setReservedQty(stock.getReservedQty() - borrow.getQuantity());
                        stock.setAvailableQty(stock.getAvailableQty() + borrow.getQuantity());
                        stock.setUpdateTime(DateUtils.getNowDate());
                        invStockMapper.updateInvStock(stock);
                    } else if (borrow.needsReturn()) {
                        // 已借出、部分歸還、逾期的記錄：恢復借出數量
                        int remainingQty = borrow.getQuantity() - borrow.getReturnQuantity();
                        stock.setAvailableQty(stock.getAvailableQty() + remainingQty);
                        stock.setBorrowedQty(stock.getBorrowedQty() - remainingQty);
                        stock.setUpdateTime(DateUtils.getNowDate());
                        invStockMapper.updateInvStock(stock);
                    }
                    // 待審核的一般借出記錄和已拒絕的記錄不需要恢復（沒有扣減過庫存）
                }
            }
        }

        return invBorrowMapper.deleteInvBorrowByBorrowIds(borrowIds);
    }

    /**
     * 刪除借出記錄資訊
     *
     * @param borrowId 借出記錄主鍵
     * @return 結果
     */
    @Override
    public int deleteInvBorrowByBorrowId(Long borrowId) {
        return invBorrowMapper.deleteInvBorrowByBorrowId(borrowId);
    }

    /**
     * 檢查借出單號是否唯一
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    @Override
    public boolean checkBorrowNoUnique(InvBorrow invBorrow) {
        long borrowId = invBorrow.getBorrowId() == null ? -1L : invBorrow.getBorrowId();
        InvBorrow info = invBorrowMapper.selectInvBorrowByBorrowNo(invBorrow.getBorrowNo());
        return info == null || info.getBorrowId() == borrowId;
    }

    /**
     * 產生借出單號
     * 格式：BOR + 時間戳（毫秒）+ 3位隨機數
     * 確保唯一性
     *
     * @return 借出單號
     */
    @Override
    public String generateBorrowNo() {
        // 使用時間戳 + 隨機數確保唯一性
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);

        // 產生借出單號：BOR + 時間戳 + 3位隨機數
        return String.format("BOR%d%03d", timestamp, random);
    }

    /**
     * 檢查物品是否可借出
     *
     * @param itemId   物品ID
     * @param quantity 數量
     * @return 結果
     */
    @Override
    public boolean checkItemAvailable(Long itemId, Integer quantity) {
        InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
        return stock != null && stock.getAvailableQty() >= quantity;
    }
}
