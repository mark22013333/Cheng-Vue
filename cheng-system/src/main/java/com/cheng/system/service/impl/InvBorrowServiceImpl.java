package com.cheng.system.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.InvStock;
import com.cheng.system.mapper.InvBorrowMapper;
import com.cheng.system.mapper.InvStockMapper;
import com.cheng.system.service.IInvBorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 借出記錄Service業務層處理
 *
 * @author cheng
 * @since 2025-09-23
 */
@Service
public class InvBorrowServiceImpl implements IInvBorrowService {
    @Autowired
    private InvBorrowMapper invBorrowMapper;

    @Autowired
    private InvStockMapper invStockMapper;

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
     * @return 借出記錄集合
     */
    @Override
    public List<InvBorrow> selectOverdueBorrowList() {
        return invBorrowMapper.selectOverdueBorrowList();
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
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    @Override
    public int updateInvBorrow(InvBorrow invBorrow) {
        invBorrow.setUpdateTime(DateUtils.getNowDate());
        return invBorrowMapper.updateInvBorrow(invBorrow);
    }

    /**
     * 借出物品
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    @Override
    @Transactional
    public int borrowItem(InvBorrow invBorrow) {
        // 檢查物品是否可借出
        if (!checkItemAvailable(invBorrow.getItemId(), invBorrow.getQuantity())) {
            throw new RuntimeException("物品庫存不足，無法借出");
        }

        // 設定借出時間
        invBorrow.setBorrowTime(DateUtils.getNowDate());
        invBorrow.setStatus("0"); // 0: 借出中
        invBorrow.setCreateTime(DateUtils.getNowDate());

        // 新增借出記錄
        int result = invBorrowMapper.insertInvBorrow(invBorrow);

        // 更新庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(invBorrow.getItemId());
        if (stock != null) {
            stock.setAvailableQty(stock.getAvailableQty() - invBorrow.getQuantity());
            stock.setBorrowedQty(stock.getBorrowedQty() + invBorrow.getQuantity());
            stock.setUpdateTime(DateUtils.getNowDate());
            invStockMapper.updateInvStock(stock);
        }

        return result;
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
     * @return 結果
     */
    @Override
    @Transactional
    public int returnItem(Long borrowId, Integer returnQuantity, Long returnerId,
                         String conditionDesc, String isDamaged, String damageDesc) {
        // 查詢借出記錄
        InvBorrow borrow = invBorrowMapper.selectInvBorrowByBorrowId(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借出記錄不存在");
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
            borrow.setStatus("1"); // 1: 已歸還
        } else {
            borrow.setStatus("3"); // 3: 部分歸還
        }

        int result = invBorrowMapper.updateInvBorrow(borrow);

        // 更新庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(borrow.getItemId());
        if (stock != null) {
            stock.setAvailableQty(stock.getAvailableQty() + returnQuantity);
            stock.setBorrowedQty(stock.getBorrowedQty() - returnQuantity);
            
            // 如果物品損壞，增加損壞數量
            if ("1".equals(isDamaged)) {
                stock.setDamagedQty(stock.getDamagedQty() + returnQuantity);
                stock.setAvailableQty(stock.getAvailableQty() - returnQuantity);
            }
            
            stock.setUpdateTime(DateUtils.getNowDate());
            invStockMapper.updateInvStock(stock);
        }

        return result;
    }

    /**
     * 審核借出申請
     *
     * @param borrowId     借出記錄ID
     * @param approverId   審核人ID
     * @param approverName 審核人姓名
     * @param isApproved   是否通過審核
     * @return 結果
     */
    @Override
    public int approveBorrow(Long borrowId, Long approverId, String approverName, boolean isApproved) {
        InvBorrow borrow = invBorrowMapper.selectInvBorrowByBorrowId(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借出記錄不存在");
        }

        borrow.setApproverId(approverId);
        borrow.setApproverName(approverName);
        borrow.setApproveTime(DateUtils.getNowDate());
        borrow.setUpdateTime(DateUtils.getNowDate());

        if (isApproved) {
            borrow.setStatus("1"); // 1: 已審核通過
        } else {
            borrow.setStatus("2"); // 2: 審核拒絕
        }

        return invBorrowMapper.updateInvBorrow(borrow);
    }

    /**
     * 批量刪除借出記錄
     *
     * @param borrowIds 需要刪除的借出記錄主鍵集合
     * @return 結果
     */
    @Override
    public int deleteInvBorrowByBorrowIds(Long[] borrowIds) {
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
        Long borrowId = invBorrow.getBorrowId() == null ? -1L : invBorrow.getBorrowId();
        InvBorrow info = invBorrowMapper.selectInvBorrowByBorrowNo(invBorrow.getBorrowNo());
        return info == null || info.getBorrowId().longValue() == borrowId.longValue();
    }

    /**
     * 產生借出單號
     *
     * @return 借出單號
     */
    @Override
    public String generateBorrowNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        
        // 查詢當天的借出記錄數量
        int count = invBorrowMapper.countTodayBorrows();
        
        // 產生借出單號：BOR + 日期 + 4位序號
        return String.format("BOR%s%04d", dateStr, count + 1);
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
