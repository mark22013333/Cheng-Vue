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
import com.cheng.system.domain.InvBookInfo;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.enums.BorrowStatus;
import com.cheng.system.mapper.InvBookInfoMapper;
import com.cheng.system.mapper.InvBorrowMapper;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.mapper.InvStockMapper;
import com.cheng.system.mapper.InvStockRecordMapper;
import com.cheng.system.service.IInvItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

import java.io.File;
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
    private InvBookInfoMapper invBookInfoMapper;

    @Autowired
    private InvStockRecordMapper invStockRecordMapper;

    @Autowired
    private InvBorrowMapper invBorrowMapper;

    @Autowired
    protected Validator validator;

    /**
     * 檔案上傳根路徑
     */
    @Value("${cheng.profile:/tmp/uploadPath}")
    private String uploadPath;

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
            stock.setLostQty(0);
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
        // 先刪除相關的庫存記錄和圖片檔案
        for (Long itemId : itemIds) {
            // 刪除圖片檔案
            InvItem item = invItemMapper.selectInvItemByItemId(itemId);
            if (item != null && StringUtils.isNotEmpty(item.getImageUrl())) {
                deleteImageFile(item.getImageUrl(), item.getItemName());
            }
            // 刪除庫存記錄
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
        // 先刪除圖片檔案和相關的庫存記錄
        // 刪除圖片檔案
        InvItem item = invItemMapper.selectInvItemByItemId(itemId);
        if (item != null && StringUtils.isNotEmpty(item.getImageUrl())) {
            deleteImageFile(item.getImageUrl(), item.getItemName());
        }
        // 刪除庫存記錄
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

    /**
     * 安全刪除物品（檢查借出記錄、級聯刪除相關表）
     *
     * @param itemIds 需要刪除的物品ID陣列
     * @return 刪除結果訊息
     */
    @Override
    @Transactional
    public String safeDeleteInvItemByItemIds(Long[] itemIds) {
        if (itemIds == null || itemIds.length == 0) {
            throw new ServiceException("請選擇要刪除的物品");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();

        for (Long itemId : itemIds) {
            try {
                // 1. 檢查物品是否存在
                InvItem item = invItemMapper.selectInvItemByItemId(itemId);
                if (item == null) {
                    failCount++;
                    failMsg.append("<div style='margin-top: 10px;'>")
                            .append("<strong>").append(failCount).append("、物品ID ").append(itemId).append("</strong>")
                            .append("<div style='color: #F56C6C; margin-left: 20px;'>❌ 物品不存在</div>")
                            .append("</div>");
                    continue;
                }

                // 2. 檢查是否有未完成的借出記錄
                List<InvBorrow> activeBorrows = invBorrowMapper.selectActiveBorrowsByItemId(itemId);
                if (activeBorrows != null && !activeBorrows.isEmpty()) {
                    failCount++;
                    StringBuilder borrowInfo = new StringBuilder();

                    // --- 外層容器 ---
                    borrowInfo.append("<div style='margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid #EBEEF5;'>");

                    // 1. 標題區：編號與物品名稱
                    borrowInfo.append("<div style='display: flex; align-items: center; margin-bottom: 10px;'>")
                            .append("<span style='background: #909399; color: #fff; border-radius: 50%; width: 20px; height: 20px; display: flex; justify-content: center; align-items: center; font-size: 12px; margin-right: 8px;'>")
                            .append(failCount).append("</span>")
                            .append("<span style='font-size: 15px; font-weight: bold; color: #303133;'>")
                            .append(item.getItemName())
                            .append("</span>")
                            .append("</div>");

                    // 2. 錯誤提示區 (Alert Style)
                    borrowInfo.append("<div style='background-color: #FEF0F0; color: #F56C6C; padding: 8px 12px; border-radius: 4px; font-size: 13px; margin-bottom: 10px; display: flex; align-items: center;'>")
                            .append("<i class='el-icon-error' style='margin-right: 6px;'></i>")
                            .append("<span>無法刪除：存在 <strong>").append(activeBorrows.size()).append("</strong> 筆未完成的借出記錄</span>")
                            .append("</div>");

                    // 3. 詳細清單容器 (灰色背景)
                    borrowInfo.append("<div style='background-color: #F5F7FA; border-radius: 4px; padding: 8px 12px;'>");

                    for (int i = 0; i < activeBorrows.size(); i++) {
                        InvBorrow borrow = activeBorrows.get(i);
                        BorrowStatus status = BorrowStatus.getByCode(borrow.getStatus());

                        // 最後一筆不顯示底線
                        String borderStyle = (i == activeBorrows.size() - 1) ? "" : "border-bottom: 1px dashed #DCDFE6;";

                        if (status != null) {
                            borrowInfo.append("<div style='display: flex; align-items: center; justify-content: space-between; padding: 6px 0; font-size: 13px; color: #606266; ").append(borderStyle).append("'>")

                                    // 左側：單號與借出人
                                    .append("<div>")
                                    .append("<i class='el-icon-document' style='color: #909399; margin-right: 4px;'></i>")
                                    .append("<span style='font-family: monospace; color: #303133; margin-right: 10px;'>").append(borrow.getBorrowNo()).append("</span>")
                                    .append("<i class='el-icon-user' style='color: #909399; margin-right: 4px;'></i>")
                                    .append("<span>").append(borrow.getBorrowerName()).append("</span>")
                                    .append("</div>")

                                    // 右側：狀態標籤
                                    .append("<div>")
                                    .append("<span style='display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 12px; transform: scale(0.9); border: 1px solid ").append(status.getColor()).append("; color: ").append(status.getColor()).append(";'>")
                                    .append(status.getDescription())
                                    .append("</span>")
                                    .append("</div>")
                                    .append("</div>");
                        }
                    }
                    borrowInfo.append("</div>"); // End 清單容器
                    borrowInfo.append("</div>"); // End 外層容器

                    failMsg.append(borrowInfo);

                    log.warn("無法刪除物品，存在未完成的借出記錄，ItemId: {}, ItemName: {}, 借出記錄數: {}",
                            itemId, item.getItemName(), activeBorrows.size());
                    continue;
                }

                // 3. 檢查是否有庫存（可選警告）
                InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
                if (stock != null && stock.getTotalQuantity() > 0) {
                    log.warn("刪除物品時發現有庫存，ItemId: {}, 物品名稱: {}, 庫存數量: {}",
                            itemId, item.getItemName(), stock.getTotalQuantity());
                    // 注意：這裡選擇允許刪除有庫存的物品，如果不允許可以改成拋出異常
                }

                // 4. 刪除相關表記錄
                // 4.1 刪除書籍資訊
                InvBookInfo bookInfo = invBookInfoMapper.selectInvBookInfoByItemId(itemId);
                if (bookInfo != null) {
                    invBookInfoMapper.deleteInvBookInfoByBookInfoId(bookInfo.getBookInfoId());
                    log.info("已刪除書籍資訊，BookInfoId: {}", bookInfo.getBookInfoId());
                }

                // 4.2 刪除庫存記錄
                if (stock != null) {
                    invStockMapper.deleteInvStockByStockId(stock.getStockId());
                    log.info("已刪除庫存記錄，StockId: {}", stock.getStockId());
                }

                // 4.3 刪除庫存異動記錄
                int recordCount = invStockRecordMapper.deleteInvStockRecordByItemId(itemId);
                if (recordCount > 0) {
                    log.info("已刪除 {} 筆庫存異動記錄，ItemId: {}", recordCount, itemId);
                }

                // 4.4 刪除實體圖片檔案
                if (StringUtils.isNotEmpty(item.getImageUrl())) {
                    deleteImageFile(item.getImageUrl(), item.getItemName());
                }

                // 5. 最後刪除物品本身
                int result = invItemMapper.deleteInvItemByItemId(itemId);
                if (result > 0) {
                    successCount++;
                    log.info("成功刪除物品，ItemId: {}, 物品名稱: {}", itemId, item.getItemName());
                } else {
                    failCount++;
                    failMsg.append("<div style='margin-top: 10px;'>")
                            .append("<strong>").append(failCount).append("、物品「").append(item.getItemName()).append("」</strong>")
                            .append("<div style='color: #F56C6C; margin-left: 20px;'>❌ 刪除失敗</div>")
                            .append("</div>");
                }

            } catch (Exception e) {
                failCount++;
                failMsg.append("<div style='margin-top: 10px;'>")
                        .append("<strong>").append(failCount).append("、物品ID ").append(itemId).append("</strong>")
                        .append("<div style='color: #F56C6C; margin-left: 20px;'>❌ 刪除失敗：").append(e.getMessage()).append("</div>")
                        .append("</div>");
                log.error("刪除物品失敗，ItemId: {}", itemId, e);
            }
        }

        // 建立結果訊息
        StringBuilder resultMsg = new StringBuilder();

        // 使用 HTML 格式構建訊息
        if (successCount > 0 && failCount > 0) {
            // 部分成功
            resultMsg.append("<div style='font-size: 14px; line-height: 1.6;'>")
                    .append("<div style='margin-bottom: 10px;'>")
                    .append("✅ 成功刪除 <strong>").append(successCount).append("</strong> 個物品")
                    .append("，❌ 失敗 <strong>").append(failCount).append("</strong> 個")
                    .append("</div>");
            resultMsg.append(failMsg);
            resultMsg.append("</div>");
        } else if (failCount > 0) {
            // 全部失敗
            resultMsg.append("<div style='font-size: 14px; line-height: 1.6;'>")
                    .append("<div style='margin-bottom: 10px; color: #F56C6C; font-weight: bold;'>")
                    .append("❌ 刪除失敗（").append(failCount).append(" 個）")
                    .append("</div>");
            resultMsg.append(failMsg);
            resultMsg.append("</div>");
            throw new ServiceException(resultMsg.toString());
        } else {
            // 全部成功
            resultMsg.append("成功刪除 ").append(successCount).append(" 個物品");
        }

        return resultMsg.toString();
    }

    /**
     * 刪除實體圖片檔案
     *
     * @param imageUrl 圖片相對路徑（從 inv_item.image_url）
     * @param itemName 物品名稱（用於日誌）
     */
    private void deleteImageFile(String imageUrl, String itemName) {
        try {
            if (StringUtils.isEmpty(imageUrl)) {
                return;
            }

            // 組成完整路徑
            String fullPath = uploadPath + File.separator + imageUrl.replace("/profile/", "");
            File imageFile = new File(fullPath);

            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                if (deleted) {
                    log.info("成功刪除圖片檔案，物品: {}, 路徑: {}", itemName, fullPath);
                } else {
                    log.warn("圖片檔案刪除失敗，物品: {}, 路徑: {}", itemName, fullPath);
                }
            } else {
                log.warn("圖片檔案不存在，物品: {}, 路徑: {}", itemName, fullPath);
            }
        } catch (Exception e) {
            // 圖片刪除失敗不應影響物品刪除，只記錄警告
            log.error("刪除圖片檔案時發生異常，物品: {}, 圖片路徑: {}, 錯誤: {}", itemName, imageUrl, e.getMessage(), e);
        }
    }
}
