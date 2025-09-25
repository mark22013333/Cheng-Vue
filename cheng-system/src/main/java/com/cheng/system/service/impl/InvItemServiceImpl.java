package com.cheng.system.service.impl;

import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.entity.SysUser;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.bean.BeanValidators;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvStock;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.mapper.InvStockMapper;
import com.cheng.system.service.IInvItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

import java.util.List;

/**
 * 物品資訊 服務層實現
 *
 * @author cheng
 */
@Service
public class InvItemServiceImpl implements IInvItemService {
    private static final Logger log = LoggerFactory.getLogger(InvItemServiceImpl.class);

    @Autowired
    private InvItemMapper invItemMapper;

    @Autowired
    private InvStockMapper invStockMapper;

    @Autowired
    protected Validator validator;

    /**
     * 查詢物品資訊
     *
     * @param itemId 物品資訊主鍵
     * @return 物品資訊
     */
    @Override
    public InvItem selectInvItemByItemId(Long itemId) {
        return invItemMapper.selectInvItemByItemId(itemId);
    }

    /**
     * 根據物品編碼查詢物品資訊
     *
     * @param itemCode 物品編碼
     * @return 物品資訊
     */
    @Override
    public InvItem selectInvItemByItemCode(String itemCode) {
        return invItemMapper.selectInvItemByItemCode(itemCode);
    }

    /**
     * 根據條碼查詢物品資訊
     *
     * @param barcode 條碼
     * @return 物品資訊
     */
    @Override
    public InvItem selectInvItemByBarcode(String barcode) {
        return invItemMapper.selectInvItemByBarcode(barcode);
    }

    /**
     * 根據QR碼查詢物品資訊
     *
     * @param qrCode QR碼
     * @return 物品資訊
     */
    @Override
    public InvItem selectInvItemByQrCode(String qrCode) {
        return invItemMapper.selectInvItemByQrCode(qrCode);
    }

    /**
     * 查詢物品資訊列表
     *
     * @param invItem 物品資訊
     * @return 物品資訊
     */
    @Override
    public List<InvItem> selectInvItemList(InvItem invItem) {
        return invItemMapper.selectInvItemList(invItem);
    }

    /**
     * 查詢低庫存物品列表
     *
     * @return 物品資訊集合
     */
    @Override
    public List<InvItem> selectLowStockItemList() {
        return invItemMapper.selectLowStockItemList();
    }

    /**
     * 掃描條碼或QR碼取得物品資訊
     *
     * @param scanCode 掃描內容
     * @param scanType 掃描類型（1條碼 2QR碼）
     * @return 物品資訊
     */
    @Override
    public InvItem scanItemByCode(String scanCode, String scanType) {
        if (StringUtils.isEmpty(scanCode)) {
            throw new ServiceException("掃描內容不能為空");
        }

        InvItem item = null;
        if ("1".equals(scanType)) {
            // 條碼掃描
            item = invItemMapper.selectInvItemByBarcode(scanCode);
        } else if ("2".equals(scanType)) {
            // QR碼掃描
            item = invItemMapper.selectInvItemByQrCode(scanCode);
        }

        if (item == null) {
            throw new ServiceException("未找到對應的物品資訊");
        }

        return item;
    }

    /**
     * 新增物品資訊
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    @Override
    @Transactional
    public int insertInvItem(InvItem invItem) {
        invItem.setCreateTime(DateUtils.getNowDate());
        invItem.setCreateBy(SecurityUtils.getUsername());

        int result = invItemMapper.insertInvItem(invItem);

        // 同時建立庫存記錄
        if (result > 0) {
            InvStock stock = new InvStock();
            stock.setItemId(invItem.getItemId());
            stock.setTotalQuantity(0);
            stock.setAvailableQty(0);
            stock.setBorrowedQty(0);
            stock.setReservedQty(0);
            stock.setDamagedQty(0);
            stock.setUpdateTime(DateUtils.getNowDate());
            invStockMapper.insertInvStock(stock);
        }

        return result;
    }

    /**
     * 修改物品資訊
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    @Override
    public int updateInvItem(InvItem invItem) {
        invItem.setUpdateTime(DateUtils.getNowDate());
        invItem.setUpdateBy(SecurityUtils.getUsername());
        return invItemMapper.updateInvItem(invItem);
    }

    /**
     * 批量刪除物品資訊
     *
     * @param itemIds 需要刪除的物品資訊主鍵
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteInvItemByItemIds(Long[] itemIds) {
        // 先刪除相關的庫存記錄
        for (Long itemId : itemIds) {
            invStockMapper.deleteInvStockByItemId(itemId);
        }
        return invItemMapper.deleteInvItemByItemIds(itemIds);
    }

    /**
     * 刪除物品資訊
     *
     * @param itemId 物品資訊主鍵
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteInvItemByItemId(Long itemId) {
        // 先刪除相關的庫存記錄
        invStockMapper.deleteInvStockByItemId(itemId);
        return invItemMapper.deleteInvItemByItemId(itemId);
    }

    /**
     * 檢查物品編碼是否唯一
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    @Override
    public boolean checkItemCodeUnique(InvItem invItem) {
        Long itemId = StringUtils.isNull(invItem.getItemId()) ? -1L : invItem.getItemId();
        InvItem info = invItemMapper.checkItemCodeUnique(invItem);
        if (StringUtils.isNotNull(info) && info.getItemId().longValue() != itemId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 檢查條碼是否唯一
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    @Override
    public boolean checkBarcodeUnique(InvItem invItem) {
        if (StringUtils.isEmpty(invItem.getBarcode())) {
            return UserConstants.UNIQUE;
        }

        Long itemId = StringUtils.isNull(invItem.getItemId()) ? -1L : invItem.getItemId();
        InvItem info = invItemMapper.checkBarcodeUnique(invItem);
        if (StringUtils.isNotNull(info) && info.getItemId().longValue() != itemId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 匯入物品資料
     *
     * @param itemList        物品資料列表
     * @param isUpdateSupport 是否更新支援，如果已存在，則進行更新資料
     * @param operName        操作使用者
     * @return 結果
     */
    @Override
    public String importItem(List<InvItem> itemList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(itemList) || itemList.size() == 0) {
            throw new ServiceException("匯入物品資料不能為空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (InvItem item : itemList) {
            try {
                // 驗證是否存在這個物品
                InvItem existItem = invItemMapper.selectInvItemByItemCode(item.getItemCode());
                if (StringUtils.isNull(existItem)) {
                    BeanValidators.validateWithException(validator, item);
                    item.setCreateBy(operName);
                    this.insertInvItem(item);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、物品編碼 ").append(item.getItemCode()).append(" 匯入成功");
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, item);
                    item.setItemId(existItem.getItemId());
                    item.setUpdateBy(operName);
                    this.updateInvItem(item);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、物品編碼 ").append(item.getItemCode()).append(" 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、物品編碼 ").append(item.getItemCode()).append(" 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、物品編碼 " + item.getItemCode() + " 匯入失敗：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，匯入失敗！共 " + failureNum + " 筆資料格式不正確，錯誤如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，資料已全部匯入成功！共 " + successNum + " 筆，資料如下：");
        }
        return successMsg.toString();
    }
}
