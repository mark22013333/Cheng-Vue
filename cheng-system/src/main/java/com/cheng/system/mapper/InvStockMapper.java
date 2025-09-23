package com.cheng.system.mapper;

import com.cheng.system.domain.InvStock;

import java.util.List;

/**
 * 庫存 數據層
 *
 * @author cheng
 */
public interface InvStockMapper {
    /**
     * 查詢庫存
     *
     * @param stockId 庫存主鍵
     * @return 庫存
     */
    InvStock selectInvStockByStockId(Long stockId);

    /**
     * 根據物品ID查詢庫存
     *
     * @param itemId 物品ID
     * @return 庫存
     */
    InvStock selectInvStockByItemId(Long itemId);

    /**
     * 查詢庫存列表
     *
     * @param invStock 庫存
     * @return 庫存集合
     */
    List<InvStock> selectInvStockList(InvStock invStock);

    /**
     * 查詢低庫存列表
     *
     * @return 庫存集合
     */
    List<InvStock> selectLowStockList();

    /**
     * 查詢庫存統計資訊
     *
     * @return 統計資訊
     */
    List<InvStock> selectStockStatistics();

    /**
     * 新增庫存
     *
     * @param invStock 庫存
     * @return 結果
     */
    int insertInvStock(InvStock invStock);

    /**
     * 修改庫存
     *
     * @param invStock 庫存
     * @return 結果
     */
    int updateInvStock(InvStock invStock);

    /**
     * 更新庫存數量（入庫）
     *
     * @param itemId 物品ID
     * @param quantity 數量
     * @return 結果
     */
    int updateStockIn(Long itemId, Integer quantity);

    /**
     * 更新庫存數量（出庫）
     *
     * @param itemId 物品ID
     * @param quantity 數量
     * @return 結果
     */
    int updateStockOut(Long itemId, Integer quantity);

    /**
     * 更新借出數量
     *
     * @param itemId 物品ID
     * @param quantity 數量
     * @return 結果
     */
    int updateBorrowedQty(Long itemId, Integer quantity);

    /**
     * 更新歸還數量
     *
     * @param itemId 物品ID
     * @param quantity 數量
     * @return 結果
     */
    int updateReturnedQty(Long itemId, Integer quantity);

    /**
     * 刪除庫存
     *
     * @param stockId 庫存主鍵
     * @return 結果
     */
    int deleteInvStockByStockId(Long stockId);

    /**
     * 根據物品ID刪除庫存
     *
     * @param itemId 物品ID
     * @return 結果
     */
    int deleteInvStockByItemId(Long itemId);

    /**
     * 批量刪除庫存
     *
     * @param stockIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvStockByStockIds(Long[] stockIds);
}
