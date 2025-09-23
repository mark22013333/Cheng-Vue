package com.cheng.system.mapper;

import com.cheng.system.domain.InvStockRecord;

import java.util.List;

/**
 * 庫存異動記錄 數據層
 *
 * @author cheng
 */
public interface InvStockRecordMapper {
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
     * 根據物品ID查詢庫存異動記錄
     *
     * @param itemId 物品ID
     * @return 庫存異動記錄集合
     */
    List<InvStockRecord> selectRecordListByItemId(Long itemId);

    /**
     * 根據操作人員ID查詢庫存異動記錄
     *
     * @param operatorId 操作人員ID
     * @return 庫存異動記錄集合
     */
    List<InvStockRecord> selectRecordListByOperatorId(Long operatorId);

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
     * 刪除庫存異動記錄
     *
     * @param recordId 庫存異動記錄主鍵
     * @return 結果
     */
    int deleteInvStockRecordByRecordId(Long recordId);

    /**
     * 批量刪除庫存異動記錄
     *
     * @param recordIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvStockRecordByRecordIds(Long[] recordIds);
}
