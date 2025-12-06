package com.cheng.system.mapper;

import com.cheng.system.domain.InvBorrow;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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
     * @param borrowerId 借出人ID（可選，若為null則查詢所有逾期記錄）
     * @return 借出記錄集合
     */
    List<InvBorrow> selectOverdueBorrowList(@Param("borrowerId") Long borrowerId);

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

    /**
     * 檢查物品是否有未完成的借出記錄
     * 未完成狀態包括：待審核(0)、已借出(1)、部分歸還(4)、逾期(5)
     *
     * @param itemId 物品ID
     * @return 未完成的借出記錄列表
     */
    List<InvBorrow> selectActiveBorrowsByItemId(Long itemId);
    
    /**
     * 查詢重疊時間段的預約記錄
     * 
     * @param itemId 物品ID
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 重疊的預約記錄列表
     */
    List<InvBorrow> selectOverlappingReservations(@Param("itemId") Long itemId, 
                                                @Param("startDate") Date startDate, 
                                                @Param("endDate") Date endDate);
    
    /**
     * 計算重疊時間段的總預約數量
     * 
     * @param itemId 物品ID
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 重疊時間段的總預約數量
     */
    Integer sumOverlappingReservationQuantity(@Param("itemId") Long itemId, 
                                             @Param("startDate") Date startDate, 
                                             @Param("endDate") Date endDate);
    
    /**
     * 查詢用戶的待審核預約
     * 
     * @param userId 用戶ID
     * @param itemId 物品ID
     * @return 待審核的預約記錄列表
     */
    List<InvBorrow> selectPendingReservationsByUser(@Param("userId") Long userId, 
                                                   @Param("itemId") Long itemId);
    
    /**
     * 查詢所有過期的預約
     * 
     * @return 過期的預約記錄列表
     */
    List<InvBorrow> selectExpiredReservations();
}
