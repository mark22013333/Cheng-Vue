package com.cheng.system.service;

import com.cheng.system.domain.InvBorrow;

import java.util.List;

/**
 * 借出記錄 服務層
 *
 * @author cheng
 */
public interface IInvBorrowService {
    /**
     * 查詢借出記錄
     *
     * @param borrowId 借出記錄主鍵
     * @return 借出記錄
     */
    InvBorrow selectInvBorrowByBorrowId(Long borrowId);

    /**
     * 根據借出單號查詢借出記錄
     *
     * @param borrowNo 借出單號
     * @return 借出記錄
     */
    InvBorrow selectInvBorrowByBorrowNo(String borrowNo);

    /**
     * 查詢借出記錄列表
     *
     * @param invBorrow 借出記錄
     * @return 借出記錄集合
     */
    List<InvBorrow> selectInvBorrowList(InvBorrow invBorrow);

    /**
     * 查詢逾期借出記錄列表
     *
     * @param borrowerId 借出人ID（可選，若為null則查詢所有逾期記錄）
     * @return 借出記錄集合
     */
    List<InvBorrow> selectOverdueBorrowList(Long borrowerId);

    /**
     * 查詢用戶的借出記錄
     *
     * @param borrowerId 借出人ID
     * @return 借出記錄集合
     */
    List<InvBorrow> selectBorrowListByBorrowerId(Long borrowerId);

    /**
     * 查詢物品的借出記錄
     *
     * @param itemId 物品ID
     * @return 借出記錄集合
     */
    List<InvBorrow> selectBorrowListByItemId(Long itemId);

    /**
     * 新增借出記錄
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    int insertInvBorrow(InvBorrow invBorrow);

    /**
     * 修改借出記錄
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    int updateInvBorrow(InvBorrow invBorrow);

    /**
     * 借出物品
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    int borrowItem(InvBorrow invBorrow);

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
    int returnItem(Long borrowId, Integer returnQuantity, Long returnerId,
                   String conditionDesc, String isDamaged, String damageDesc, String remark);

    /**
     * 審核借出申請
     *
     * @param borrowId     借出記錄ID
     * @param approverId   審核人ID
     * @param approverName 審核人姓名
     * @param isApproved   是否通過審核
     * @return 結果
     */
    int approveBorrow(Long borrowId, Long approverId, String approverName, boolean isApproved);

    /**
     * 批量刪除借出記錄
     *
     * @param borrowIds 需要刪除的借出記錄主鍵集合
     * @return 結果
     */
    int deleteInvBorrowByBorrowIds(Long[] borrowIds);

    /**
     * 刪除借出記錄
     *
     * @param borrowId 借出記錄主鍵
     * @return 結果
     */
    int deleteInvBorrowByBorrowId(Long borrowId);

    /**
     * 檢查借出單號是否唯一
     *
     * @param invBorrow 借出記錄
     * @return 結果
     */
    boolean checkBorrowNoUnique(InvBorrow invBorrow);

    /**
     * 產生借出單號
     *
     * @return 借出單號
     */
    String generateBorrowNo();

    /**
     * 檢查物品是否可借出
     *
     * @param itemId   物品ID
     * @param quantity 數量
     * @return 結果
     */
    boolean checkItemAvailable(Long itemId, Integer quantity);
}
