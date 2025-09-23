package com.cheng.system.service.impl;

import java.util.List;

import com.cheng.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cheng.system.mapper.InvStockRecordMapper;
import com.cheng.system.domain.InvStockRecord;
import com.cheng.system.service.IInvStockRecordService;

/**
 * 庫存異動記錄Service業務層處理
 *
 * @author cheng
 * @since 2025-09-23
 */
@Service
public class InvStockRecordServiceImpl implements IInvStockRecordService {
    @Autowired
    private InvStockRecordMapper invStockRecordMapper;

    /**
     * 查詢庫存異動記錄
     *
     * @param recordId 庫存異動記錄主鍵
     * @return 庫存異動記錄
     */
    @Override
    public InvStockRecord selectInvStockRecordByRecordId(Long recordId) {
        return invStockRecordMapper.selectInvStockRecordByRecordId(recordId);
    }

    /**
     * 查詢庫存異動記錄列表
     *
     * @param invStockRecord 庫存異動記錄
     * @return 庫存異動記錄
     */
    @Override
    public List<InvStockRecord> selectInvStockRecordList(InvStockRecord invStockRecord) {
        return invStockRecordMapper.selectInvStockRecordList(invStockRecord);
    }

    /**
     * 新增庫存異動記錄
     *
     * @param invStockRecord 庫存異動記錄
     * @return 結果
     */
    @Override
    public int insertInvStockRecord(InvStockRecord invStockRecord) {
        if (invStockRecord.getRecordTime() == null) {
            invStockRecord.setRecordTime(DateUtils.getNowDate());
        }
        return invStockRecordMapper.insertInvStockRecord(invStockRecord);
    }

    /**
     * 修改庫存異動記錄
     *
     * @param invStockRecord 庫存異動記錄
     * @return 結果
     */
    @Override
    public int updateInvStockRecord(InvStockRecord invStockRecord) {
        // 庫存異動記錄通常不允許修改，但如果需要可以更新記錄時間
        return invStockRecordMapper.updateInvStockRecord(invStockRecord);
    }

    /**
     * 批次刪除庫存異動記錄
     *
     * @param recordIds 需要刪除的庫存異動記錄主鍵
     * @return 結果
     */
    @Override
    public int deleteInvStockRecordByRecordIds(Long[] recordIds) {
        return invStockRecordMapper.deleteInvStockRecordByRecordIds(recordIds);
    }

    /**
     * 刪除庫存異動記錄資訊
     *
     * @param recordId 庫存異動記錄主鍵
     * @return 結果
     */
    @Override
    public int deleteInvStockRecordByRecordId(Long recordId) {
        return invStockRecordMapper.deleteInvStockRecordByRecordId(recordId);
    }

    /**
     * 根據物品ID查詢異動記錄
     *
     * @param itemId 物品ID
     * @return 異動記錄列表
     */
    @Override
    public List<InvStockRecord> selectInvStockRecordByItemId(Long itemId) {
        return invStockRecordMapper.selectRecordListByItemId(itemId);
    }

    /**
     * 根據操作人員ID查詢異動記錄
     *
     * @param operatorId 操作人員ID
     * @return 異動記錄列表
     */
    @Override
    public List<InvStockRecord> selectInvStockRecordByOperatorId(Long operatorId) {
        return invStockRecordMapper.selectRecordListByOperatorId(operatorId);
    }
}
