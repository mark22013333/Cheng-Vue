package com.cheng.system.service;

import java.util.List;

import com.cheng.system.domain.InvStockRecord;

/**
 * 庫存異動記錄Service介面
 *
 * @author cheng
 * @since 2025-09-23
 */
public interface IInvStockRecordService {
    /**
     * 查詢庫存異動記錄
     *
     * @param recordId 庫存異動記錄主鍵
     * @return 庫存異動記錄
     */
    InvStockRecord selectInvStockRecordByRecordId(Long recordId);

    /**
     * 查詢庫存異動記錄列表
     *
     * @param invStockRecord 庫存異動記錄
     * @return 庫存異動記錄集合
     */
    List<InvStockRecord> selectInvStockRecordList(InvStockRecord invStockRecord);

    /**
     * 新增庫存異動記錄
     *
     * @param invStockRecord 庫存異動記錄
     * @return 結果
     */
    int insertInvStockRecord(InvStockRecord invStockRecord);

    /**
     * 修改庫存異動記錄
     *
     * @param invStockRecord 庫存異動記錄
     * @return 結果
     */
    int updateInvStockRecord(InvStockRecord invStockRecord);

    /**
     * 批次刪除庫存異動記錄
     *
     * @param recordIds 需要刪除的庫存異動記錄主鍵集合
     * @return 結果
     */
    int deleteInvStockRecordByRecordIds(Long[] recordIds);

    /**
     * 刪除庫存異動記錄資訊
     *
     * @param recordId 庫存異動記錄主鍵
     * @return 結果
     */
    int deleteInvStockRecordByRecordId(Long recordId);

    /**
     * 根據物品ID查詢異動記錄
     *
     * @param itemId 物品ID
     * @return 異動記錄列表
     */
    List<InvStockRecord> selectInvStockRecordByItemId(Long itemId);

    /**
     * 根據操作人員ID查詢異動記錄
     *
     * @param operatorId 操作人員ID
     * @return 異動記錄列表
     */
    List<InvStockRecord> selectInvStockRecordByOperatorId(Long operatorId);
}
