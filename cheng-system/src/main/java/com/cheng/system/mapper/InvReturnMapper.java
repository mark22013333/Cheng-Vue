package com.cheng.system.mapper;

import java.util.List;
import com.cheng.system.domain.InvReturn;

/**
 * 歸還記錄Mapper介面
 * 
 * @author cheng
 * @since 2024-01-01
 */
public interface InvReturnMapper 
{
    /**
     * 查詢歸還記錄
     * 
     * @param returnId 歸還記錄主鍵
     * @return 歸還記錄
     */
    public InvReturn selectInvReturnByReturnId(Long returnId);

    /**
     * 查詢歸還記錄列表
     * 
     * @param invReturn 歸還記錄
     * @return 歸還記錄集合
     */
    public List<InvReturn> selectInvReturnList(InvReturn invReturn);

    /**
     * 新增歸還記錄
     * 
     * @param invReturn 歸還記錄
     * @return 結果
     */
    public int insertInvReturn(InvReturn invReturn);

    /**
     * 修改歸還記錄
     * 
     * @param invReturn 歸還記錄
     * @return 結果
     */
    public int updateInvReturn(InvReturn invReturn);

    /**
     * 刪除歸還記錄
     * 
     * @param returnId 歸還記錄主鍵
     * @return 結果
     */
    public int deleteInvReturnByReturnId(Long returnId);

    /**
     * 批量刪除歸還記錄
     * 
     * @param returnIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    public int deleteInvReturnByReturnIds(Long[] returnIds);

    /**
     * 根據借出記錄ID查詢歸還記錄
     * 
     * @param borrowId 借出記錄ID
     * @return 歸還記錄集合
     */
    public List<InvReturn> selectInvReturnByBorrowId(Long borrowId);

    /**
     * 根據物品ID查詢歸還記錄
     * 
     * @param itemId 物品ID
     * @return 歸還記錄集合
     */
    public List<InvReturn> selectInvReturnByItemId(Long itemId);

    /**
     * 根據借用人查詢歸還記錄
     * 
     * @param borrowerId 借用人ID
     * @return 歸還記錄集合
     */
    public List<InvReturn> selectInvReturnByBorrowerId(Long borrowerId);

    /**
     * 查詢逾期歸還記錄
     * 
     * @return 逾期歸還記錄集合
     */
    public List<InvReturn> selectOverdueReturnList();

    /**
     * 查詢損壞物品歸還記錄
     * 
     * @return 損壞物品歸還記錄集合
     */
    public List<InvReturn> selectDamagedReturnList();

    /**
     * 查詢遺失物品歸還記錄
     * 
     * @return 遺失物品歸還記錄集合
     */
    public List<InvReturn> selectLostReturnList();

    /**
     * 根據歸還狀態查詢歸還記錄
     * 
     * @param returnStatus 歸還狀態
     * @return 歸還記錄集合
     */
    public List<InvReturn> selectInvReturnByStatus(String returnStatus);

    /**
     * 查詢歸還統計資訊
     * 
     * @return 統計資訊
     */
    public List<InvReturn> selectReturnStatistics();
}
