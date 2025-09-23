package com.cheng.system.mapper;

import com.cheng.system.domain.InvBorrow;

import java.util.List;

/**
 * 借出記錄 數據層
 *
 * @author cheng
 */
public interface InvBorrowMapper {
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
     * @return 借出記錄集合
     */
    List<InvBorrow> selectOverdueBorrowList();

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

//    /**
//     * 檢查借出單號是否唯一
//     *
//     * @param invBorrow 借出記錄
//     * @return 結果
//     */
//    InvBorrow checkBorrowNoUnique(InvBorrow invBorrow);

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
     * 更新借出狀態
     *
     * @param borrowId 借出記錄ID
     * @param status   狀態
     * @return 結果
     */
    int updateBorrowStatus(Long borrowId, String status);

    /**
     * 更新歸還資訊
     *
     * @param borrowId       借出記錄ID
     * @param returnQuantity 歸還數量
     * @param actualReturn   實際歸還時間
     * @return 結果
     */
    int updateReturnInfo(Long borrowId, Integer returnQuantity, java.util.Date actualReturn);

    /**
     * 刪除借出記錄
     *
     * @param borrowId 借出記錄主鍵
     * @return 結果
     */
    int deleteInvBorrowByBorrowId(Long borrowId);

    /**
     * 批量刪除借出記錄
     *
     * @param borrowIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvBorrowByBorrowIds(Long[] borrowIds);

    /**
     * 統計今日借出記錄數量
     *
     * @return 今日借出記錄數量
     */
    int countTodayBorrows();
}
