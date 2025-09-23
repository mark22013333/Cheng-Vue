package com.cheng.system.service;

import com.cheng.system.domain.InvStock;

import java.util.List;

/**
 * 庫存 服務層
 *
 * @author cheng
 */
public interface IInvStockService {
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
     * 入庫操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param operatorId 操作人員ID
     * @param reason     入庫原因
     * @return 結果
     */
    int stockIn(Long itemId, Integer quantity, Long operatorId, String reason);

    /**
     * 出庫操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param operatorId 操作人員ID
     * @param reason     出庫原因
     * @return 結果
     */
    int stockOut(Long itemId, Integer quantity, Long operatorId, String reason);

    /**
     * 借出操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param borrowerId 借出人ID
     * @return 結果
     */
    int borrowStock(Long itemId, Integer quantity, Long borrowerId);

    /**
     * 歸還操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param borrowerId 借出人ID
     * @return 結果
     */
    int returnStock(Long itemId, Integer quantity, Long borrowerId);

    /**
     * 盤點操作
     *
     * @param itemId         物品ID
     * @param actualQuantity 實際數量
     * @param operatorId     操作人員ID
     * @param reason         盤點原因
     * @return 結果
     */
    int stockCheck(Long itemId, Integer actualQuantity, Long operatorId, String reason);

    /**
     * 批量刪除庫存
     *
     * @param stockIds 需要刪除的庫存主鍵集合
     * @return 結果
     */
    int deleteInvStockByStockIds(Long[] stockIds);

    /**
     * 刪除庫存
     *
     * @param stockId 庫存主鍵
     * @return 結果
     */
    int deleteInvStockByStockId(Long stockId);
}
