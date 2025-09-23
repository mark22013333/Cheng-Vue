package com.cheng.system.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.exception.ServiceException;
import com.cheng.system.domain.InvStock;
import com.cheng.system.domain.InvStockRecord;
import com.cheng.system.mapper.InvStockMapper;
import com.cheng.system.mapper.InvStockRecordMapper;
import com.cheng.system.service.IInvStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 庫存 服務層實現
 *
 * @author cheng
 */
@Service
public class InvStockServiceImpl implements IInvStockService {

    @Autowired
    private InvStockMapper invStockMapper;

    @Autowired
    private InvStockRecordMapper invStockRecordMapper;

    /**
     * 查詢庫存
     *
     * @param stockId 庫存主鍵
     * @return 庫存
     */
    @Override
    public InvStock selectInvStockByStockId(Long stockId) {
        return invStockMapper.selectInvStockByStockId(stockId);
    }

    /**
     * 根據物品ID查詢庫存
     *
     * @param itemId 物品ID
     * @return 庫存
     */
    @Override
    public InvStock selectInvStockByItemId(Long itemId) {
        return invStockMapper.selectInvStockByItemId(itemId);
    }

    /**
     * 查詢庫存列表
     *
     * @param invStock 庫存
     * @return 庫存
     */
    @Override
    public List<InvStock> selectInvStockList(InvStock invStock) {
        return invStockMapper.selectInvStockList(invStock);
    }

    /**
     * 查詢低庫存列表
     *
     * @return 庫存集合
     */
    @Override
    public List<InvStock> selectLowStockList() {
        return invStockMapper.selectLowStockList();
    }

    /**
     * 查詢庫存統計資訊
     *
     * @return 統計資訊
     */
    @Override
    public List<InvStock> selectStockStatistics() {
        return invStockMapper.selectStockStatistics();
    }

    /**
     * 新增庫存
     *
     * @param invStock 庫存
     * @return 結果
     */
    @Override
    public int insertInvStock(InvStock invStock) {
        invStock.setUpdateTime(DateUtils.getNowDate());
        return invStockMapper.insertInvStock(invStock);
    }

    /**
     * 修改庫存
     *
     * @param invStock 庫存
     * @return 結果
     */
    @Override
    public int updateInvStock(InvStock invStock) {
        invStock.setUpdateTime(DateUtils.getNowDate());
        return invStockMapper.updateInvStock(invStock);
    }

    /**
     * 入庫操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param operatorId 操作人員ID
     * @param reason     入庫原因
     * @return 結果
     */
    @Override
    @Transactional
    public int stockIn(Long itemId, Integer quantity, Long operatorId, String reason) {
        if (quantity <= 0) {
            throw new ServiceException("入庫數量必須大於0");
        }

        // 查詢當前庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
        if (stock == null) {
            throw new ServiceException("物品庫存記錄不存在");
        }

        int beforeQty = stock.getTotalQuantity();
        int afterQty = beforeQty + quantity;

        // 更新庫存
        int result = invStockMapper.updateStockIn(itemId, quantity);

        if (result > 0) {
            // 記錄庫存異動
            recordStockChange(itemId, "1", quantity, beforeQty, afterQty,
                    operatorId, SecurityUtils.getUsername(), reason);
        }

        return result;
    }

    /**
     * 出庫操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param operatorId 操作人員ID
     * @param reason     出庫原因
     * @return 結果
     */
    @Override
    @Transactional
    public int stockOut(Long itemId, Integer quantity, Long operatorId, String reason) {
        if (quantity <= 0) {
            throw new ServiceException("出庫數量必須大於0");
        }

        // 查詢當前庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
        if (stock == null) {
            throw new ServiceException("物品庫存記錄不存在");
        }

        if (stock.getAvailableQty() < quantity) {
            throw new ServiceException("庫存不足，可用數量：" + stock.getAvailableQty());
        }

        int beforeQty = stock.getTotalQuantity();
        int afterQty = beforeQty - quantity;

        // 更新庫存
        int result = invStockMapper.updateStockOut(itemId, quantity);

        if (result > 0) {
            // 記錄庫存異動
            recordStockChange(itemId, "2", -quantity, beforeQty, afterQty,
                    operatorId, SecurityUtils.getUsername(), reason);
        }

        return result;
    }

    /**
     * 借出操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param borrowerId 借出人ID
     * @return 結果
     */
    @Override
    @Transactional
    public int borrowStock(Long itemId, Integer quantity, Long borrowerId) {
        if (quantity <= 0) {
            throw new ServiceException("借出數量必須大於0");
        }

        // 查詢當前庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
        if (stock == null) {
            throw new ServiceException("物品庫存記錄不存在");
        }

        if (stock.getAvailableQty() < quantity) {
            throw new ServiceException("庫存不足，可用數量：" + stock.getAvailableQty());
        }

        // 更新借出數量
        int result = invStockMapper.updateBorrowedQty(itemId, quantity);

        if (result > 0) {
            // 記錄庫存異動
            recordStockChange(itemId, "3", -quantity, stock.getAvailableQty(),
                    stock.getAvailableQty() - quantity, borrowerId,
                    SecurityUtils.getUsername(), "物品借出");
        }

        return result;
    }

    /**
     * 歸還操作
     *
     * @param itemId     物品ID
     * @param quantity   數量
     * @param borrowerId 借出人ID
     * @return 結果
     */
    @Override
    @Transactional
    public int returnStock(Long itemId, Integer quantity, Long borrowerId) {
        if (quantity <= 0) {
            throw new ServiceException("歸還數量必須大於0");
        }

        // 查詢當前庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
        if (stock == null) {
            throw new ServiceException("物品庫存記錄不存在");
        }

        if (stock.getBorrowedQty() < quantity) {
            throw new ServiceException("歸還數量超過借出數量，借出數量：" + stock.getBorrowedQty());
        }

        // 更新歸還數量
        int result = invStockMapper.updateReturnedQty(itemId, quantity);

        if (result > 0) {
            // 記錄庫存異動
            recordStockChange(itemId, "4", quantity, stock.getAvailableQty(),
                    stock.getAvailableQty() + quantity, borrowerId,
                    SecurityUtils.getUsername(), "物品歸還");
        }

        return result;
    }

    /**
     * 盤點操作
     *
     * @param itemId         物品ID
     * @param actualQuantity 實際數量
     * @param operatorId     操作人員ID
     * @param reason         盤點原因
     * @return 結果
     */
    @Override
    @Transactional
    public int stockCheck(Long itemId, Integer actualQuantity, Long operatorId, String reason) {
        if (actualQuantity < 0) {
            throw new ServiceException("實際數量不能小於0");
        }

        // 查詢當前庫存
        InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
        if (stock == null) {
            throw new ServiceException("物品庫存記錄不存在");
        }

        int beforeQty = stock.getTotalQuantity();
        int difference = actualQuantity - beforeQty;

        if (difference != 0) {
            // 更新庫存為實際數量
            stock.setTotalQuantity(actualQuantity);
            stock.setAvailableQty(actualQuantity - stock.getBorrowedQty() - stock.getReservedQty() - stock.getDamagedQty());
            stock.setUpdateTime(DateUtils.getNowDate());

            int result = invStockMapper.updateInvStock(stock);

            if (result > 0) {
                // 記錄庫存異動
                recordStockChange(itemId, "5", difference, beforeQty, actualQuantity,
                        operatorId, SecurityUtils.getUsername(), reason);
            }

            return result;
        }

        return 1; // 數量無變化，返回成功
    }

    /**
     * 記錄庫存異動
     */
    private void recordStockChange(Long itemId, String recordType, Integer quantity,
                                   Integer beforeQty, Integer afterQty, Long operatorId,
                                   String operatorName, String reason) {
        InvStockRecord record = new InvStockRecord();
        record.setItemId(itemId);
        record.setRecordType(recordType);
        record.setQuantity(quantity);
        record.setBeforeQty(beforeQty);
        record.setAfterQty(afterQty);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setRecordTime(DateUtils.getNowDate());
        record.setReason(reason);

        invStockRecordMapper.insertInvStockRecord(record);
    }

    /**
     * 批量刪除庫存
     *
     * @param stockIds 需要刪除的庫存主鍵集合
     * @return 結果
     */
    @Override
    public int deleteInvStockByStockIds(Long[] stockIds) {
        return invStockMapper.deleteInvStockByStockIds(stockIds);
    }

    /**
     * 刪除庫存
     *
     * @param stockId 庫存主鍵
     * @return 結果
     */
    @Override
    public int deleteInvStockByStockId(Long stockId) {
        return invStockMapper.deleteInvStockByStockId(stockId);
    }
}
