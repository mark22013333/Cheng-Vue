package com.cheng.system.service.impl;

import com.cheng.common.annotation.Excel;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.enums.ScanType;
import com.cheng.common.enums.UserStatus;
import com.cheng.common.event.ExportProgressEvent;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.bean.BeanValidators;
import com.cheng.common.utils.file.ImageExportUtil;
import com.cheng.common.utils.file.ImageImportUtil;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.common.event.ReservationEvent;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvStock;
import com.cheng.system.domain.InvStockRecord;
import com.cheng.system.domain.InvBookInfo;
import com.cheng.system.domain.InvBorrow;
import com.cheng.system.domain.InvCategory;
import com.cheng.system.domain.vo.ImportResult;
import com.cheng.system.dto.InvItemImportDTO;
import com.cheng.system.domain.vo.ReserveResult;
import com.cheng.system.domain.vo.ReserveRequest;
import com.cheng.system.dto.ImportTaskResult;
import com.cheng.system.domain.enums.BorrowStatus;
import com.cheng.system.dto.InvItemWithStockDTO;
import com.cheng.system.mapper.*;
import com.cheng.system.service.IInvItemService;
import com.cheng.system.service.IInvBorrowService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Validator;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 物品資訊 服務層實現
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvItemServiceImpl implements IInvItemService {

    private final InvItemMapper invItemMapper;
    private final InvStockMapper invStockMapper;
    private final InvBookInfoMapper invBookInfoMapper;
    private final InvStockRecordMapper invStockRecordMapper;
    private final InvBorrowMapper invBorrowMapper;
    private final InvCategoryMapper invCategoryMapper;
    private final IInvBorrowService invBorrowService;
    private final Validator validator;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 靜態 Map 供 Controller 傳遞 SseManager 實例
     * 用於 SSE 廣播功能（跨模組橋接）
     */
    public static final ConcurrentHashMap<String, Object> SSE_EMITTER_MAP = new ConcurrentHashMap<>();

    /**
     * 檔案上傳根路徑
     */
    @Value("${cheng.profile:/tmp/uploadPath}")
    private String uploadPath;

    /**
     * 匯入任務參數儲存（taskId -> ImportTaskParams）
     */
    private static final ConcurrentHashMap<String, Object> IMPORT_TASK_MAP = new ConcurrentHashMap<>();

    /**
     * 進度資訊儲存（taskId -> ProgressInfo）
     * 供 Controller 輪詢並推送到 SSE
     */
    public static final ConcurrentHashMap<String, ProgressInfo> PROGRESS_MAP = new ConcurrentHashMap<>();

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

        InvItem item;
        ScanType type = ScanType.fromCode(scanType);

        switch (type) {
            case BARCODE -> item = invItemMapper.selectInvItemByBarcode(scanCode);
            case QRCODE -> item = invItemMapper.selectInvItemByQrCode(scanCode);
            default -> throw new ServiceException("未找到對應的物品資訊");
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
        invItem.setCreateBy(getUsername());

        int result = invItemMapper.insertInvItem(invItem);

        // 同時建立庫存記錄
        if (result > 0) {
            // 取得初始庫存數量（從remark欄位臨時取得，用於匯入時的初始庫存設定）
            Integer initialStock = 0;
            if (invItem.getRemark() != null && invItem.getRemark().startsWith("INITIAL_STOCK:")) {
                try {
                    initialStock = Integer.parseInt(invItem.getRemark().substring("INITIAL_STOCK:".length()));
                    invItem.setRemark(null); // 清除臨時標記
                } catch (NumberFormatException e) {
                    log.warn("解析初始庫存數量失敗: {}", invItem.getRemark());
                }
            }

            InvStock stock = new InvStock();
            stock.setItemId(invItem.getItemId());
            stock.setTotalQuantity(initialStock);
            stock.setAvailableQty(initialStock);
            stock.setBorrowedQty(0);
            stock.setReservedQty(0);
            stock.setDamagedQty(0);
            stock.setLostQty(0);
            stock.setUpdateTime(DateUtils.getNowDate());
            invStockMapper.insertInvStock(stock);

            // 如果有初始庫存，記錄庫存異動
            if (initialStock > 0) {
                InvStockRecord record = new InvStockRecord();
                record.setItemId(invItem.getItemId());
                record.setRecordType("0"); // 入庫
                record.setQuantity(initialStock);
                record.setBeforeQty(0);
                record.setAfterQty(initialStock);
                try {
                    record.setOperatorId(SecurityUtils.getUserId());
                } catch (Exception e) {
                    record.setOperatorId(1L); // 系統管理員 ID
                }
                record.setOperatorName(getUsername());
                record.setRecordTime(DateUtils.getNowDate());
                record.setReason("初始庫存");
                invStockRecordMapper.insertInvStockRecord(record);
            }
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
        invItem.setUpdateBy(getUsername());
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
        long itemId = StringUtils.isNull(invItem.getItemId()) ? -1L : invItem.getItemId();
        InvItem info = invItemMapper.checkItemCodeUnique(invItem);
        if (StringUtils.isNotNull(info) && info.getItemId().longValue() != itemId) {
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

        long itemId = StringUtils.isNull(invItem.getItemId()) ? -1L : invItem.getItemId();
        InvItem info = invItemMapper.checkBarcodeUnique(invItem);
        if (StringUtils.isNotNull(info) && info.getItemId().longValue() != itemId) {
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
        if (StringUtils.isNull(itemList) || itemList.isEmpty()) {
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
                    int insertInvItem = this.insertInvItem(item);
                    log.info("insertInv:{}", insertInvItem);
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

    /**
     * 建立匯入任務並返回任務ID
     *
     * @param file              上傳的Excel檔案
     * @param updateSupport     是否更新支援
     * @param defaultCategoryId 預設分類ID
     * @param defaultUnit       預設單位
     * @return 任務ID
     */
    @Override
    public ImportTaskResult createImportTask(MultipartFile file, Boolean updateSupport, Long defaultCategoryId, String defaultUnit) {
        try {
            // 產生任務ID
            String taskId = UUID.randomUUID().toString();

            // 先解析Excel檔案並保存到內存（避免異步執行時臨時文件已被刪除）
            ExcelUtil<InvItemImportDTO> util = new ExcelUtil<>(InvItemImportDTO.class);
            List<InvItemImportDTO> importList = util.importExcel(file.getInputStream());
            int rowCount = importList != null ? importList.size() : 0;

            // 將任務參數和已解析的資料存入Map供後續使用
            ImportTaskParams params = new ImportTaskParams();
            params.setImportList(importList);  // 保存已解析的資料到內存
            params.setUpdateSupport(updateSupport);
            params.setDefaultCategoryId(defaultCategoryId);
            params.setDefaultUnit(defaultUnit);
            params.setTaskId(taskId);
            params.setRowCount(rowCount);
            IMPORT_TASK_MAP.put(taskId, params);

            // 註解：不在此處執行，等待 SSE 訂閱後再執行（由 Controller 調用）
            // asyncImportItems(taskId);

            // 返回任務資訊
            return new ImportTaskResult(taskId, rowCount);
        } catch (Exception e) {
            log.error("建立匯入任務失敗", e);
            throw new ServiceException("解析Excel檔案失敗: " + e.getMessage());
        }
    }

    /**
     * 異步執行匯入任務
     *
     * @param taskId 任務ID
     */
    @Async
    public void asyncImportItems(String taskId) {
        ImportTaskParams params = (ImportTaskParams) IMPORT_TASK_MAP.get(taskId);
        if (params == null) {
            log.error("找不到匯入任務參數，taskId: {}", taskId);
            return;
        }

        try {
            // 推送開始進度
            pushImportProgress(taskId, 0, "開始解析Excel檔案...");

            // 從參數中獲取已解析的資料
            List<InvItemImportDTO> importList = params.getImportList();

            pushImportProgress(taskId, 10, String.format("成功解析 %d 條資料，開始驗證...", importList.size()));

            // 驗證和轉換資料
            List<InvItem> validItemList = validateAndConvertItems(importList, params, taskId);

            pushImportProgress(taskId, 30, String.format("驗證完成，有效資料 %d 條，開始匯入...", validItemList.size()));

            // 執行匯入
            String result = importItem(validItemList, params.getUpdateSupport(), getUsername());

            pushImportProgress(taskId, 100, "匯入完成：" + result);

        } catch (Exception e) {
            log.error("匯入任務執行失敗，taskId: {}", taskId, e);
            pushImportProgress(taskId, -1, "匯入失敗：" + e.getMessage());
        } finally {
            // 清理任務參數
            IMPORT_TASK_MAP.remove(taskId);
        }
    }

    /**
     * 驗證和轉換匯入資料
     *
     * @param importList 匯入資料列表
     * @param params     任務參數
     * @param taskId     任務ID
     * @return 有效物品列表
     */
    private List<InvItem> validateAndConvertItems(List<InvItemImportDTO> importList, ImportTaskParams params, String taskId) {
        List<InvItem> validItemList = new java.util.ArrayList<>();
        int total = importList.size();
        int processed = 0;

        for (InvItemImportDTO importDTO : importList) {
            try {
                // 設定行號便於錯誤定位
                importDTO.setRowNum(processed + 2); // Excel行號從2開始

                // 驗證必填欄位
                if (StringUtils.isEmpty(importDTO.getItemName())) {
                    pushImportProgress(taskId, -1, String.format("第 %d 行錯誤：物品名稱不能為空", importDTO.getRowNum()));
                    continue;
                }

                // 轉換為InvItem對象
                InvItem item = convertImportDTOToInvItem(importDTO, params);

                // 驗證資料
                BeanValidators.validateWithException(validator, item);

                validItemList.add(item);

                // 推送進度（驗證階段佔20%進度）
                int progress = 10 + (int) ((double) processed / total * 20);
                pushImportProgress(taskId, progress, String.format("已驗證 %d/%d 條資料...", processed + 1, total));

            } catch (Exception e) {
                pushImportProgress(taskId, -1, String.format("第 %d 行錯誤：%s", importDTO.getRowNum(), e.getMessage()));
            }
            processed++;
        }

        return validItemList;
    }

    /**
     * 將ImportDTO轉換為InvItem
     *
     * @param importDTO 匯入DTO
     * @param params    任務參數
     * @return InvItem對象
     */
    private InvItem convertImportDTOToInvItem(InvItemImportDTO importDTO, ImportTaskParams params) {
        InvItem item = new InvItem();

        // 物品編碼：如果為空則自動產生
        if (StringUtils.isEmpty(importDTO.getItemCode())) {
            item.setItemCode(IdUtils.generateItemCode());
        } else {
            item.setItemCode(importDTO.getItemCode());
        }

        // 基本資訊
        item.setItemName(importDTO.getItemName());
        item.setBarcode(importDTO.getBarcode());
        item.setSpecification(importDTO.getSpecification());
        item.setBrand(importDTO.getBrand());
        item.setModel(importDTO.getModel());
        item.setSupplier(importDTO.getSupplier());
        item.setLocation(importDTO.getLocation());
        item.setDescription(importDTO.getDescription());

        // 分類：如果為空則使用預設分類
        if (StringUtils.isEmpty(importDTO.getCategoryName())) {
            item.setCategoryId(params.getDefaultCategoryId());
        } else {
            // 根據分類名稱查找分類ID
            InvCategory category = findCategoryByName(importDTO.getCategoryName());
            item.setCategoryId(category != null ? category.getCategoryId() : params.getDefaultCategoryId());
        }

        // 單位：如果為空則使用預設單位
        item.setUnit(StringUtils.isEmpty(importDTO.getUnit()) ? params.getDefaultUnit() : importDTO.getUnit());

        // 價格資訊
        item.setPurchasePrice(importDTO.getPurchasePrice());
        item.setCurrentPrice(importDTO.getCurrentPrice());

        // 庫存設定
        item.setMinStock(importDTO.getMinStock());
        item.setMaxStock(importDTO.getMaxStock());

        // 初始庫存數量（使用remark欄位臨時傳遞，在insertInvItem中處理）
        if (importDTO.getInitialStock() != null && importDTO.getInitialStock() > 0) {
            item.setRemark("INITIAL_STOCK:" + importDTO.getInitialStock());
        }

        // 設定預設值
        item.setStatus(UserStatus.OK.getCode()); // 啟用狀態
        item.setCreateBy(getUsername());
        item.setCreateTime(DateUtils.getNowDate());

        return item;
    }

    /**
     * 根據分類名稱查找分類
     *
     * @param categoryName 分類名稱
     * @return 分類對象
     */
    private InvCategory findCategoryByName(String categoryName) {
        if (StringUtils.isEmpty(categoryName)) {
            return null;
        }

        // 使用分類名稱查詢
        InvCategory queryParam = new InvCategory();
        queryParam.setCategoryName(categoryName);
        List<InvCategory> categories = invCategoryMapper.selectInvCategoryList(queryParam);

        // 返回第一個匹配的分類
        if (categories != null && !categories.isEmpty()) {
            return categories.get(0);
        }

        return null;
    }

    /**
     * 安全地取得使用者名稱
     * <p>
     * 在執行緒池環境中，即使透過 TaskDecorator 複製了 SecurityContext，
     * 仍可能出現意外情況導致無法取得使用者名稱。
     * 此方法提供備用方案，確保程式不會因此中斷。
     *
     * @return 使用者名稱，如果無法取得則返回 "system"
     */
    private String getUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            log.warn("無法取得使用者名稱，使用系統帳號: {}", e.getMessage());
            return "system";
        }
    }

    /**
     * 推送匯入進度（存入靜態Map，供 Controller 輪詢）
     *
     * @param taskId   任務ID
     * @param progress 進度百分比（-1 表示錯誤）
     * @param message  訊息
     */
    private void pushImportProgress(String taskId, int progress, String message) {
        log.info("匯入進度 - taskId: {}, progress: {}, message: {}", taskId, progress, message);

        ProgressInfo progressInfo = new ProgressInfo();
        progressInfo.setProgress(progress);
        progressInfo.setMessage(message);
        progressInfo.setTimestamp(System.currentTimeMillis());
        PROGRESS_MAP.put(taskId, progressInfo);
    }

    /**
     * 進度資訊類別
     */
    @Setter
    @Getter
    public static class ProgressInfo {
        private int progress;
        private String message;
        private long timestamp;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 建立臨時目錄
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "import_template_" + UUID.randomUUID());
        if (!tempDir.mkdirs()) {
            throw new ServiceException("無法建立臨時目錄");
        }

        try {
            // 1. 產生 Excel 範本檔案（含範例資料）
            File excelFile = new File(tempDir, "物品匯入範本.xlsx");
            generateExcelTemplate(excelFile);

            // 2. 產生圖片範例 ZIP
            File imagesZip = new File(tempDir, "images.zip");
            generateImagesZip(imagesZip);

            // 3. 產生說明文件
            File readmeFile = new File(tempDir, "匯入說明.txt");
            generateReadmeFile(readmeFile);

            // 4. 將所有檔案打包成 ZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                // 加入 Excel 檔案
                addFileToZip(zos, excelFile, "物品匯入範本.xlsx");
                // 加入圖片 ZIP
                addFileToZip(zos, imagesZip, "images.zip");
                // 加入說明文件
                addFileToZip(zos, readmeFile, "匯入說明.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 5. 設定 HTTP 響應
            response.setContentType("application/zip");
            response.setCharacterEncoding("UTF-8");
            String filename = URLEncoder.encode("物品匯入範本_完整版.zip", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // 6. 輸出到響應流
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 清理臨時目錄
            deleteDirectory(tempDir);
        }
    }

    /**
     * 產生 Excel 範本檔案（含範例資料）
     */
    private void generateExcelTemplate(File excelFile) throws Exception {
        // 使用正確的資源關閉順序：先 FileOutputStream，後 Workbook
        // 這樣關閉時會先關閉 Workbook（完成寫入），再關閉 FileOutputStream
        try (FileOutputStream fos = new FileOutputStream(excelFile);
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("物品資料");

            // 建立標題樣式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 建立資料樣式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 建立必填欄位樣式（紅色背景）
            CellStyle requiredStyle = workbook.createCellStyle();
            requiredStyle.cloneStyleFrom(headerStyle);
            requiredStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());

            // 標題列
            Row headerRow = sheet.createRow(0);
            String[] headers = {"物品編碼", "物品名稱*", "分類名稱", "規格", "單位", "品牌",
                    "型號", "ISBN", "圖片路徑", "條碼", "QR碼", "供應商",
                    "進價", "現價", "最小庫存", "最大庫存", "存放位置",
                    "初始庫存", "描述", "備註"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // 必填欄位使用特殊樣式
                if (headers[i].contains("*")) {
                    cell.setCellStyle(requiredStyle);
                } else {
                    cell.setCellStyle(headerStyle);
                }
                // 設定欄寬
                sheet.setColumnWidth(i, 4000);
            }

            // 範例資料
            String[][] examples = {
                    {"BOOK001", "Java 程式設計", "書籍", "精裝", "本", "碁峰",
                            "JV-2024", "9789863479471", "書籍封面/isbn_9789863479471.jpg", "9789863479471", "QR001", "碁峰出版",
                            "450", "550", "5", "50", "A區-001", "10", "Java 入門經典", "熱門暢銷書"},

                    {"BOOK002", "Python 資料分析", "書籍", "平裝", "本", "歐萊禮",
                            "PY-2024", "9787302123456", "書籍封面/isbn_9787302123456.jpg", "9787302123456", "QR002", "歐萊禮出版",
                            "380", "480", "3", "30", "A區-002", "8", "資料科學必讀", ""},

                    {"TOOL001", "程式設計鍵盤", "辦公用品", "機械軸", "個", "羅技",
                            "K380", "", "", "1234567890123", "QR003", "羅技科技",
                            "800", "1200", "2", "20", "B區-001", "5", "藍牙機械鍵盤", "含無線接收器"}
            };

            for (int i = 0; i < examples.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < examples[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(examples[i][j]);
                    cell.setCellStyle(dataStyle);
                }
            }

            // 凍結首列
            sheet.createFreezePane(0, 1);

            // workbook.write() 在 try-with-resources 結束時會自動執行
            // 關閉順序：先 Workbook（完成寫入），再 FileOutputStream
            workbook.write(fos);
        }
    }

    /**
     * 產生圖片範例 ZIP
     */
    private void generateImagesZip(File imagesZip) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(imagesZip))) {
            // 建立範例圖片說明檔
            ZipEntry readmeEntry = new ZipEntry("圖片說明.txt");
            zos.putNextEntry(readmeEntry);

            String imageReadme = """
                    圖片命名規則：
                    
                    1. 如果有 ISBN，建議使用：isbn_{ISBN}.jpg
                       範例：isbn_9789863479471.jpg
                    
                    2. 如果無 ISBN，可使用其他命名：
                       - 使用物品編碼：BOOK001.jpg
                       - 使用條碼：1234567890123.jpg
                       - 自訂名稱：product_image_001.jpg
                    
                    3. Excel 中的「圖片路徑」欄位需對應實際檔名
                       範例：書籍封面/isbn_9789863479471.jpg
                    
                    4. 支援的圖片格式：jpg, jpeg, png, gif, bmp
                    
                    5. 建議圖片大小：10MB 以內
                    
                    注意：此 ZIP 檔內的圖片僅為範例，實際使用時請替換為真實圖片。
                    """;

            zos.write(imageReadme.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 建立三個範例圖片檔案（空檔案，僅作為示範）
            String[] exampleImages = {
                    "isbn_9789863479471.jpg",
                    "isbn_9787302123456.jpg",
                    "product_001.jpg"
            };

            for (String imageName : exampleImages) {
                ZipEntry imageEntry = new ZipEntry(imageName);
                zos.putNextEntry(imageEntry);

                // 寫入一個極小的 1x1 像素的 JPEG 圖片（Base64 解碼）
                byte[] minimalJpeg = Base64.getDecoder().decode(
                        "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCwAAA="
                );
                zos.write(minimalJpeg);
                zos.closeEntry();
            }
        }
    }

    /**
     * 產生說明文件
     */
    private void generateReadmeFile(File readmeFile) throws Exception {
        StringBuilder readme = new StringBuilder();

        readme.append("═══════════════════════════════════════════════════\n");
        readme.append("     物品匯入功能使用說明\n");
        readme.append("═══════════════════════════════════════════════════\n\n");

        readme.append("📦 檔案說明\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("本壓縮檔包含以下檔案：\n\n");
        readme.append("  1. 物品匯入範本.xlsx - Excel 資料範本（含範例資料）\n");
        readme.append("  2. images.zip - 圖片檔案壓縮包（含範例圖片）\n");
        readme.append("  3. 匯入說明.txt - 本說明文件\n\n");

        readme.append("📋 兩種匯入方式\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("方式一：純資料匯入（無圖片）\n");
        readme.append("  • 僅上傳「物品匯入範本.xlsx」\n");
        readme.append("  • 適用於不需要圖片的物品\n");
        readme.append("  • Excel 中的「圖片路徑」欄位可留空\n\n");

        readme.append("方式二：完整匯入（含圖片）\n");
        readme.append("  • 準備好 Excel 和圖片\n");
        readme.append("  • 將圖片放入 images.zip 內\n");
        readme.append("  • 將 Excel 和 images.zip 一起打包成新的 ZIP\n");
        readme.append("  • 上傳這個新的 ZIP 檔案\n\n");

        readme.append("🔧 操作步驟（完整匯入）\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("步驟 1：編輯 Excel 檔案\n");
        readme.append("  • 開啟「物品匯入範本.xlsx」\n");
        readme.append("  • 填寫物品資料（物品名稱為必填*）\n");
        readme.append("  • 圖片路徑格式：書籍封面/isbn_9789863479471.jpg\n");
        readme.append("  • 儲存並關閉\n\n");

        readme.append("步驟 2：準備圖片檔案\n");
        readme.append("  • 解壓縮 images.zip\n");
        readme.append("  • 替換或新增您的圖片檔案\n");
        readme.append("  • 圖片命名要與 Excel 中的「圖片路徑」對應\n");
        readme.append("  • 將所有圖片重新壓縮為 images.zip\n\n");

        readme.append("步驟 3：打包上傳\n");
        readme.append("  • 建立一個新的空資料夾\n");
        readme.append("  • 將編輯好的「物品匯入範本.xlsx」放入\n");
        readme.append("  • 將準備好的「images.zip」放入\n");
        readme.append("  • 選擇這兩個檔案，壓縮成新的 ZIP\n");
        readme.append("  • 在系統中上傳這個新的 ZIP 檔案\n\n");

        readme.append("📝 Excel 欄位說明\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("必填欄位（標題有 * 符號）：\n");
        readme.append("  • 物品名稱* - 不可為空\n\n");

        readme.append("選填欄位：\n");
        readme.append("  • 物品編碼 - 系統唯一識別碼（留空則自動產生）\n");
        readme.append("  • 分類名稱 - 物品分類（留空則使用預設分類）\n");
        readme.append("  • 規格 - 物品規格說明\n");
        readme.append("  • 單位 - 計量單位（留空則使用預設單位）\n");
        readme.append("  • 品牌 - 品牌名稱\n");
        readme.append("  • 型號 - 型號編號\n");
        readme.append("  • ISBN - 國際標準書號（書籍類物品）\n");
        readme.append("  • 圖片路徑 - 對應 images.zip 內的圖片檔名\n");
        readme.append("  • 條碼 - 商品條碼\n");
        readme.append("  • QR碼 - QR Code 內容\n");
        readme.append("  • 供應商 - 供應商名稱\n");
        readme.append("  • 進價 - 進貨價格（數字）\n");
        readme.append("  • 現價 - 現行售價（數字）\n");
        readme.append("  • 最小庫存 - 低庫存警示值（數字）\n");
        readme.append("  • 最大庫存 - 最大庫存上限（數字）\n");
        readme.append("  • 存放位置 - 倉庫位置\n");
        readme.append("  • 初始庫存 - 初始庫存數量（數字）\n");
        readme.append("  • 描述 - 物品詳細描述\n");
        readme.append("  • 備註 - 其他備註資訊\n\n");

        readme.append("🖼️  圖片命名規則\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("建議命名方式：\n");
        readme.append("  1. 書籍類：isbn_{ISBN}.jpg\n");
        readme.append("     範例：isbn_9789863479471.jpg\n\n");
        readme.append("  2. 其他類：使用物品編碼或條碼\n");
        readme.append("     範例：TOOL001.jpg 或 1234567890123.jpg\n\n");

        readme.append("圖片要求：\n");
        readme.append("  • 支援格式：jpg, jpeg, png, gif, bmp\n");
        readme.append("  • 大小限制：單張圖片 10MB 以內\n");
        readme.append("  • 建議尺寸：800x800 像素以上\n\n");

        readme.append("⚠️  注意事項\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("  1. Excel 中的「圖片路徑」必須與實際圖片檔名對應\n");
        readme.append("  2. 如果物品已存在（依 ISBN 判斷），可選擇「更新」或「跳過」\n");
        readme.append("  3. 圖片路徑格式：子目錄/檔名.jpg（如：書籍封面/isbn_xxx.jpg）\n");
        readme.append("  4. images.zip 內可建立子目錄，但須與 Excel 路徑一致\n");
        readme.append("  5. 匯入完成後會顯示詳細的成功/失敗統計\n");
        readme.append("  6. 如有錯誤，請根據錯誤訊息調整後重新匯入\n\n");

        readme.append("🔄 更新模式說明\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("更新支援：開啟\n");
        readme.append("  • 若物品已存在（ISBN 相同），將更新物品資訊\n");
        readme.append("  • 圖片會覆蓋原有圖片（舊圖片會自動備份）\n\n");

        readme.append("更新支援：關閉\n");
        readme.append("  • 若物品已存在，將跳過該筆資料\n");
        readme.append("  • 不會修改現有物品資料\n\n");

        readme.append("💡 範例說明\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("範本中包含 3 筆範例資料：\n");
        readme.append("  1. Java 程式設計（書籍，含 ISBN 和圖片）\n");
        readme.append("  2. Python 資料分析（書籍，含 ISBN 和圖片）\n");
        readme.append("  3. 程式設計鍵盤（辦公用品，無 ISBN）\n\n");

        readme.append("您可以參考這些範例的格式填寫您的資料。\n");
        readme.append("建議先刪除範例資料，再填入真實資料。\n\n");

        readme.append("📞 技術支援\n");
        readme.append("─────────────────────────────────────────────────\n");
        readme.append("如有任何問題，請聯繫系統管理員。\n\n");

        readme.append("═══════════════════════════════════════════════════\n");
        readme.append("版本：1.0 | 更新日期：")
                .append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("\n");
        readme.append("═══════════════════════════════════════════════════\n");

        // 寫入檔案
        try (FileWriter writer = new FileWriter(readmeFile)) {
            writer.write(readme.toString());
        }
    }

    @Override
    public String importItems(InvItemImportDTO importDTO) {
        // 這個方法應該接收檔案和參數，而不是單個 DTO
        // 由於介面已經定義，我們暫時實作一個簡化版本
        throw new RuntimeException("此方法暫不支援，請使用 createImportTask 方法進行匯入");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReserveResult reserveItem(ReserveRequest request, Long userId) {
        log.info("開始預約物品 - itemId: {}, userId: {}", request.getItemId(), userId);

        try {
            // 1. 檢查預約日期是否有效
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new ServiceException("預約開始日期和結束日期不能為空");
            }

            if (request.getStartDate().after(request.getEndDate())) {
                throw new ServiceException("預約開始日期不能晚於結束日期");
            }

            // 2. 取得物品資訊，檢查總數量
            InvItemWithStockDTO item = invItemMapper.selectItemWithStockByItemId(request.getItemId());
            if (item == null) {
                throw new ServiceException("物品不存在");
            }

            // 3. 檢查是否已有重疊日期的預約記錄
            List<InvBorrow> existingReservations = invBorrowMapper.selectOverlappingReservations(
                    request.getItemId(), request.getStartDate(), request.getEndDate());

            // 檢查是否包含自己未審核的預約
            boolean hasOwnPendingReservation = existingReservations.stream()
                    .anyMatch(r -> r.getBorrowerId().equals(userId) && r.getReserveStatus() == 1);

            if (hasOwnPendingReservation) {
                throw new ServiceException("您已經預約了此物品在該時間段，請勿重複預約");
            }

            // 4. 計算重疊時間段的總預約數量（包含待審核和已確認的預約）
            Integer overlappingQuantity = invBorrowMapper.sumOverlappingReservationQuantity(
                    request.getItemId(), request.getStartDate(), request.getEndDate());

            if (overlappingQuantity == null) {
                overlappingQuantity = 0;
            }

            // 5. 檢查剩餘可預約數量
            int totalQuantity = item.getTotalQuantity();
            int availableForReservation = totalQuantity - overlappingQuantity;

            log.info("預約檢查 - 物品總數: {}, 已預約數量: {}, 剩餘可預約: {}, 本次預約: {}",
                    totalQuantity, overlappingQuantity, availableForReservation, request.getBorrowQty());

            if (availableForReservation < request.getBorrowQty()) {
                throw new ServiceException(
                        String.format("預約失敗：該物品在選擇的時間段剩餘可預約數量為 %d，您想預約 %d，數量不足",
                                availableForReservation, request.getBorrowQty()));
            }

            // 6. 檢查用戶是否已預約該物品且尚未審核（避免重複預約）
            List<InvBorrow> userPendingReservations = invBorrowMapper.selectPendingReservationsByUser(
                    userId, request.getItemId());

            if (!userPendingReservations.isEmpty()) {
                throw new ServiceException("您已有該物品的待審核預約，請等待管理員審核或取消後重新預約");
            }

            // 7. 使用樂觀鎖更新庫存和物品版本
            // 先更新庫存
            int updatedStockRows = invItemMapper.reserveItem(
                    request.getItemId(),
                    request.getBorrowQty()
            );

            if (updatedStockRows == 0) {
                // 檢查具體失敗原因
                InvItemWithStockDTO currentItem = invItemMapper.selectItemWithStockByItemId(request.getItemId());
                if (currentItem == null) {
                    throw new ServiceException("物品不存在");
                }

                // 檢查可用庫存
                if (currentItem.getAvailableQty() < request.getBorrowQty()) {
                    throw new ServiceException("該物品目前可用庫存不足，無法預約");
                }

                throw new ServiceException("預約失敗，請重新整理後重試");
            }

            // 再更新物品版本
            int updatedItemRows = invItemMapper.updateItemVersion(
                    request.getItemId(),
                    request.getVersion(),
                    userId
            );

            if (updatedItemRows == 0) {
                // 版本更新失敗，說明資料已被其他用戶修改
                // 需要回滾庫存更新並拋出異常
                throw new ServiceException("資料已過期，請重新整理頁面後重試");
            }

            // 6. 新增預約記錄
            InvBorrow borrow = new InvBorrow();
            borrow.setBorrowNo(invBorrowService.generateBorrowNo());  // 產生借出編號
            borrow.setItemId(request.getItemId());
            borrow.setItemName(item.getItemName());  // 冗餘欄位：物品名稱
            borrow.setItemCode(item.getItemCode());  // 冗餘欄位：物品編碼
            borrow.setBorrowerId(userId);
            borrow.setBorrowerName(SecurityUtils.getLoginUser().getUser().getNickName());  // 借出人姓名
            borrow.setQuantity(request.getBorrowQty());
            borrow.setBorrowTime(DateUtils.getNowDate());  // 借出時間（預約建立時間）
            borrow.setStatus(BorrowStatus.PENDING.getCode());  // 0=待審核（預約需要審核）
            borrow.setReserveStatus(1); // 1=待審核預約
            borrow.setReserveStartDate(request.getStartDate());
            borrow.setReserveEndDate(request.getEndDate());
            borrow.setPurpose("已預約");  // 借用目的：標記為預約
            borrow.setExpectedReturn(request.getEndDate());  // 預計歸還：預約結束日期
            borrow.setCreateBy(userId.toString());
            borrow.setCreateTime(DateUtils.getNowDate());

            invBorrowMapper.insertInvBorrow(borrow);

            // 5. 發布預約事件
            ReservationEvent event = new ReservationEvent(
                    "reserved",
                    request.getItemId(),
                    userId,
                    borrow.getBorrowId(),
                    "預約成功，等待管理員審核",
                    DateUtils.getNowDate(),
                    item.getAvailableQty(),
                    item.getReservedQty(),
                    null  // taskId 不需要，使用 broadcast
            );
            eventPublisher.publishEvent(event);

            // 6. 返回結果
            return ReserveResult.builder()
                    .success(true)
                    .message("預約成功，等待管理員審核")
                    .borrowId(borrow.getBorrowId())
                    .availableQty(item.getAvailableQty())
                    .reservedQty(item.getReservedQty())
                    .build();

        } catch (ServiceException e) {
            log.error("物品預約失敗 - itemId: {}, error: {}", request.getItemId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("物品預約異常 - itemId: {}", request.getItemId(), e);
            throw new ServiceException("預約失敗：" + e.getMessage());
        }
    }

    /**
     * 匯入任務參數類別
     */
    @Setter
    @Getter
    private static class ImportTaskParams {
        private List<InvItemImportDTO> importList;  // 已解析的資料（避免文件被刪除）
        private Boolean updateSupport;
        private Long defaultCategoryId;
        private String defaultUnit;
        private String taskId;
        private Integer rowCount;
    }

    @Override
    public boolean restoreReservedQuantity(Long itemId, Integer quantity) {
        try {
            if (itemId == null || quantity == null || quantity <= 0) {
                log.warn("恢復預約數量參數無效 - itemId: {}, quantity: {}", itemId, quantity);
                return false;
            }

            // 檢查物品是否存在
            InvItem item = invItemMapper.selectInvItemByItemId(itemId);
            if (item == null) {
                log.warn("物品不存在，無法恢復預約數量 - itemId: {}", itemId);
                return false;
            }

            // 檢查庫存記錄是否存在
            InvStock stock = invStockMapper.selectInvStockByItemId(itemId);
            if (stock == null) {
                log.warn("庫存記錄不存在，無法恢復預約數量 - itemId: {}", itemId);
                return false;
            }

            // 檢查當前預約數量是否足夠減少
            if (stock.getReservedQty() < quantity) {
                log.warn("當前預約數量不足，無法恢復 - itemId: {}, currentReserved: {}, restoreQuantity: {}",
                        itemId, stock.getReservedQty(), quantity);
                return false;
            }

            // 更新庫存：減少預約數量
            int result = invStockMapper.updateReservedQty(itemId, -quantity);

            if (result > 0) {
                // 記錄庫存變動
                InvStockRecord record = new InvStockRecord();
                record.setItemId(itemId);
                record.setItemName(item.getItemName());
                record.setItemCode(item.getItemCode());
                record.setRecordType("RESTORE_RESERVED");
                record.setQuantity(quantity);
                record.setBeforeQty(stock.getTotalQuantity());
                record.setAfterQty(stock.getTotalQuantity());
                record.setOperatorId(-1L); // 系統操作
                record.setOperatorName("system");
                record.setDeptId(-1L);
                record.setDeptName("系統");
                record.setRecordTime(new Date());
                record.setReason("預約取消，恢復預約數量");
                record.setCreateBy("system");
                record.setCreateTime(new Date());

                invStockRecordMapper.insertInvStockRecord(record);

                log.info("成功恢復物品預約數量 - itemId: {}, itemName: {}, quantity: {}",
                        itemId, item.getItemName(), quantity);
                return true;
            } else {
                log.error("恢復預約數量失敗，資料庫更新影響0行 - itemId: {}, quantity: {}", itemId, quantity);
                return false;
            }

        } catch (Exception e) {
            log.error("恢復預約數量異常 - itemId: {}, quantity: {}", itemId, quantity, e);
            return false;
        }
    }

    /**
     * 匯出任務參數儲存（taskId -> ExportTaskParams）
     */
    private static final ConcurrentHashMap<String, ExportTaskParams> EXPORT_TASK_MAP = new ConcurrentHashMap<>();

    /**
     * 匯出結果檔案儲存（taskId -> 最終 ZIP 檔案路徑）
     */
    private static final ConcurrentHashMap<String, String> EXPORT_RESULT_MAP = new ConcurrentHashMap<>();

    /**
     * 匯出任務參數類別
     */
    @Setter
    @Getter
    private static class ExportTaskParams {
        private InvItemWithStockDTO queryDto;
        private String taskId;
    }

    @Override
    public String createExportTask(InvItemWithStockDTO dto) {
        // 產生 taskId
        String taskId = UUID.randomUUID().toString();

        // 儲存任務參數
        ExportTaskParams params = new ExportTaskParams();
        params.setQueryDto(dto);
        params.setTaskId(taskId);
        EXPORT_TASK_MAP.put(taskId, params);

        // 初始化進度
        ProgressInfo progressInfo = new ProgressInfo();
        progressInfo.setProgress(0);
        progressInfo.setMessage("任務已建立，準備開始匯出");
        PROGRESS_MAP.put(taskId, progressInfo);

        log.info("匯出任務已建立 - taskId: {}", taskId);
        return taskId;
    }

    @Override
    @Async
    public void asyncExportWithImages(String taskId) {
        log.info("========== 開始執行匯出任務 - taskId: {} ==========", taskId);

        File tempDir = null;
        try {
            // 1. 取得任務參數
            ExportTaskParams params = EXPORT_TASK_MAP.get(taskId);
            if (params == null) {
                throw new ServiceException("匯出任務不存在");
            }

            // 推送 SSE 進度
            pushExportProgress(taskId, 5, "查詢資料中");

            // 2. 查詢資料
            List<InvItemWithStockDTO> allData = invItemMapper.selectItemWithStockList(params.getQueryDto());
            allData.forEach(item -> {
                item.calculateStockStatus();
                item.calculateStockValue();
            });

            int totalCount = allData.size();
            log.info("查詢到 {} 筆資料", totalCount);

            if (totalCount == 0) {
                pushExportProgress(taskId, -1, "沒有資料可匯出");
                return;
            }

            // 3. 建立臨時目錄
            tempDir = new File(uploadPath + File.separator + "export_temp" + File.separator + taskId);
            if (!tempDir.exists() && !tempDir.mkdirs()) {
                throw new ServiceException("無法建立臨時目錄");
            }

            pushExportProgress(taskId, 10, String.format("資料查詢完成，共 %d 筆", totalCount));

            // 4. 匯出多 sheet Excel
            int maxRowsPerSheet = 10000;
            String excelFileName = "物品匯出_" + DateUtils.dateTimeNow() + ".xlsx";
            File excelFile = new File(tempDir, excelFileName);

            exportMultiSheetExcel(allData, excelFile, maxRowsPerSheet, taskId);

            pushExportProgress(taskId, 50, "Excel 匯出完成");

            // 5. 收集所有圖片路徑並壓縮
            List<String> imagePaths = allData.stream()
                    .map(InvItemWithStockDTO::getImageUrl)
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .distinct()
                    .toList();

            log.info("需要壓縮 {} 張圖片", imagePaths.size());

            ImageExportUtil.ImageZipResult imageResult =
                    ImageExportUtil.zipImages(
                            imagePaths,
                            uploadPath,
                            tempDir,
                            "images",
                            processed -> {
                                int progress = 50 + (processed * 30 / imagePaths.size());
                                pushExportProgress(taskId, progress, String.format("壓縮圖片中 (%d/%d)", processed, imagePaths.size()));
                            }
                    );

            pushExportProgress(taskId, 80, "圖片壓縮完成");

            // 6. 處理缺失圖片（寫入 Excel 的 remarks 欄位）
            if (imageResult.hasMissingImages()) {
                log.warn("發現 {} 張缺失圖片", imageResult.missingImages().size());

                // 產生缺失圖片報告
                String report = ImageExportUtil.createMissingImagesReport(imageResult.missingImages());
                File reportFile = new File(tempDir, "missing_images.txt");
                ImageExportUtil.writeTextToFile(report, reportFile.getAbsolutePath());

                // 將缺失圖片資訊寫入對應物品的 remarks 欄位（需要重新產生 Excel）
                for (InvItemWithStockDTO item : allData) {
                    if (imageResult.missingImages().contains(item.getImageUrl())) {
                        String missingInfo = "【圖片缺失】";
                        if (item.getRemark() != null && !item.getRemark().trim().isEmpty()) {
                            item.setRemark(item.getRemark() + "\n" + missingInfo);
                        } else {
                            item.setRemark(missingInfo);
                        }
                    }
                }

                // 重新匯出 Excel（包含缺失圖片標記）
                exportMultiSheetExcel(allData, excelFile, maxRowsPerSheet, taskId);
            }

            pushExportProgress(taskId, 85, "打包最終檔案");

            // 7. 將 Excel + 圖片 ZIP + 報告打包成最終 ZIP
            String finalZipName = "物品匯出_" + DateUtils.dateTimeNow() + ".zip";
            File finalZipFile = new File(uploadPath + File.separator + "export_temp", finalZipName);

            packFinalZip(excelFile, imageResult.zipFiles(),
                    imageResult.hasMissingImages() ? new File(tempDir, "missing_images.txt") : null,
                    finalZipFile);

            pushExportProgress(taskId, 95, "清理臨時檔案");

            // 8. 清理臨時檔案（保留最終 ZIP）
            ImageExportUtil.deleteTempFile(excelFile);
            imageResult.zipFiles().forEach(ImageExportUtil::deleteTempFile);
            if (imageResult.hasMissingImages()) {
                ImageExportUtil.deleteTempFile(new File(tempDir, "missing_images.txt"));
            }
            ImageExportUtil.deleteTempDirectory(tempDir);

            // 9. 儲存最終結果路徑
            EXPORT_RESULT_MAP.put(taskId, finalZipFile.getAbsolutePath());

            // 10. 推送完成事件
            pushExportProgress(taskId, 100, "匯出完成！");

            log.info("========== 匯出任務完成 - taskId: {}, 檔案: {} ==========", taskId, finalZipFile.getName());

        } catch (Exception e) {
            log.error("匯出任務執行失敗 - taskId: {}", taskId, e);
            pushExportProgress(taskId, -1, "匯出失敗：" + e.getMessage());

            // 清理臨時目錄
            if (tempDir != null && tempDir.exists()) {
                ImageExportUtil.deleteTempDirectory(tempDir);
            }
        } finally {
            // 清理任務參數
            EXPORT_TASK_MAP.remove(taskId);
        }
    }

    /**
     * 匯出多 sheet Excel
     * <p>
     * 方法：將資料分批，每批匯出到臨時檔案，然後合併所有 sheet 到最終檔案
     */
    private void exportMultiSheetExcel(List<InvItemWithStockDTO> allData, File outputFile,
                                       int maxRowsPerSheet, String taskId) throws Exception {
        log.info("開始匯出多 sheet Excel - 總資料數: {}, 每 sheet 最多: {} 行",
                allData.size(), maxRowsPerSheet);

        // 計算需要幾個 sheet
        int totalSheets = (int) Math.ceil((double) allData.size() / maxRowsPerSheet);
        log.info("將匯出 {} 個 sheet", totalSheets);

        // 如果資料量小，直接單 sheet 匯出
        if (totalSheets == 1) {
            ExcelUtil<InvItemWithStockDTO> util = new ExcelUtil<>(InvItemWithStockDTO.class);
            // ⚠️ 不要調用 exportExcel()，它會在 finally 中關閉 Workbook
            // 改為手動調用 init + writeSheet
            util.init(allData, "物品資料", "", Excel.Type.EXPORT);
            util.writeSheet();

            try (Workbook wb = extractWorkbookFromExcelUtil(util);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                wb.write(fos);
            }

            log.info("單 sheet Excel 匯出完成");
            return;
        }

        // 多 sheet：先匯出到臨時檔案，再合併
        try (Workbook finalWorkbook = new XSSFWorkbook()) {
            for (int i = 0; i < totalSheets; i++) {
                int fromIndex = i * maxRowsPerSheet;
                int toIndex = Math.min((i + 1) * maxRowsPerSheet, allData.size());
                List<InvItemWithStockDTO> batchData = allData.subList(fromIndex, toIndex);

                String sheetName = "物品資料" + (i + 1);
                log.debug("匯出 sheet: {}, 資料範圍: {}-{}", sheetName, fromIndex + 1, toIndex);

                // 匯出到臨時檔案
                ExcelUtil<InvItemWithStockDTO> util = new ExcelUtil<>(InvItemWithStockDTO.class);
                // ⚠️ 不要調用 exportExcel()，它會在 finally 中關閉 Workbook
                // 改為手動調用 init + writeSheet
                util.init(batchData, sheetName, "", Excel.Type.EXPORT);
                util.writeSheet();

                try (Workbook batchWb = extractWorkbookFromExcelUtil(util)) {
                    // 複製 sheet 到最終 Workbook
                    Sheet sourceSheet = batchWb.getSheetAt(0);
                    Sheet targetSheet = finalWorkbook.createSheet(sheetName);
                    copySheet(sourceSheet, targetSheet, finalWorkbook);
                }
            }

            // 寫入最終檔案
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                finalWorkbook.write(fos);
            }

            log.info("多 sheet Excel 匯出完成，共 {} 個 sheet", totalSheets);
        }
    }

    /**
     * 從 ExcelUtil 提取 Workbook
     * <p>
     * 注意：這是一個權宜之計，因為 ExcelUtil.exportExcel() 返回 AjaxResult 而非 Workbook。
     * ExcelUtil 的設計是為了直接寫入 HttpServletResponse，不適合我們的多 sheet 合併場景。
     * 理想情況下應該重構 ExcelUtil 或直接使用 Apache POI API，但考慮到 DTO 有大量 @Excel 註解，
     * 目前使用反射是最經濟的方案。
     *
     * @param util ExcelUtil 實例
     * @return Workbook 實例
     */
    private Workbook extractWorkbookFromExcelUtil(ExcelUtil<?> util) {
        try {
            Field wbField = util.getClass().getDeclaredField("wb");
            wbField.setAccessible(true);
            return (Workbook) wbField.get(util);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("無法從 ExcelUtil 提取 Workbook，可能 ExcelUtil 內部結構已變更", e);
            throw new RuntimeException("Excel 匯出失敗：無法取得 Workbook", e);
        }
    }

    /**
     * 複製整個 Sheet（包含樣式）
     */
    private void copySheet(Sheet sourceSheet, Sheet targetSheet, Workbook targetWorkbook) {
        // 複製列寬
        for (int i = 0; i < sourceSheet.getRow(0).getLastCellNum(); i++) {
            targetSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
        }

        // 複製所有行
        for (int rowNum = 0; rowNum <= sourceSheet.getLastRowNum(); rowNum++) {
            Row sourceRow = sourceSheet.getRow(rowNum);
            if (sourceRow != null) {
                Row targetRow = targetSheet.createRow(rowNum);
                targetRow.setHeight(sourceRow.getHeight());

                // 複製所有儲存格
                for (int cellNum = 0; cellNum < sourceRow.getLastCellNum(); cellNum++) {
                    Cell sourceCell = sourceRow.getCell(cellNum);
                    if (sourceCell != null) {
                        Cell targetCell = targetRow.createCell(cellNum);

                        // 複製樣式（需要先複製到目標 Workbook）
                        CellStyle newStyle = targetWorkbook.createCellStyle();
                        newStyle.cloneStyleFrom(sourceCell.getCellStyle());
                        targetCell.setCellStyle(newStyle);

                        // 複製值
                        copyCellValue(sourceCell, targetCell);
                    }
                }
            }
        }
    }

    /**
     * 複製儲存格值
     */
    private void copyCellValue(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                targetCell.setBlank();
                break;
            default:
                break;
        }
    }

    /**
     * 打包最終 ZIP
     */
    private void packFinalZip(File excelFile, List<File> imageZipFiles, File reportFile, File outputZip) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(outputZip))) {

            // 1. 加入 Excel
            addFileToZip(zos, excelFile, excelFile.getName());

            // 2. 加入圖片 ZIP 檔案
            for (int i = 0; i < imageZipFiles.size(); i++) {
                File imageZip = imageZipFiles.get(i);
                String entryName = imageZipFiles.size() > 1
                        ? "images_part" + (i + 1) + ".zip"
                        : "images.zip";
                addFileToZip(zos, imageZip, entryName);
            }

            // 3. 加入缺失圖片報告（如果有）
            if (reportFile != null && reportFile.exists()) {
                addFileToZip(zos, reportFile, reportFile.getName());
            }
        }

        log.info("最終 ZIP 打包完成: {}", outputZip.getName());
    }

    /**
     * 將檔案加入 ZIP
     */
    private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws Exception {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }

        zos.closeEntry();
    }

    /**
     * 推送匯出進度（透過 Spring Event 解耦）
     */
    private void pushExportProgress(String taskId, int progress, String message) {
        try {
            // 更新進度 Map
            ProgressInfo progressInfo = new ProgressInfo();
            progressInfo.setProgress(progress);
            progressInfo.setMessage(message);
            PROGRESS_MAP.put(taskId, progressInfo);

            // 發布進度事件（由 Framework 層的 Listener 監聽並推送 SSE）
            ExportProgressEvent event = new ExportProgressEvent(this, taskId, progress, message);
            eventPublisher.publishEvent(event);

            log.debug("匯出進度事件已發布 - taskId: {}, progress: {}", taskId, progress);
        } catch (Exception e) {
            log.error("進度事件發布失敗 - taskId: {}", taskId, e);
        }
    }

    @Override
    public void downloadExportResult(String taskId, HttpServletResponse response) throws Exception {
        String filePath = EXPORT_RESULT_MAP.get(taskId);
        if (filePath == null || filePath.isEmpty()) {
            throw new ServiceException("匯出結果不存在或已過期");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new ServiceException("匯出檔案不存在");
        }

        // 設定響應頭
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        String fileName = file.getName();
        // 使用 RFC 5987 標準編碼中文檔名
        String encodedFileName = java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");  // 空格編碼為 %20 而非 +
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFileName);
        response.setContentLengthLong(file.length());

        // 輸出檔案
        try (FileInputStream fis = new FileInputStream(file);
             java.io.OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }

        log.info("匯出結果已下載 - taskId: {}, file: {}", taskId, fileName);

        // 下載完成後刪除檔案和記錄
        try {
            Files.deleteIfExists(file.toPath());
            log.debug("已刪除匯出檔案 - taskId: {}", taskId);
        } catch (IOException e) {
            log.warn("刪除匯出檔案失敗 - taskId: {}, 錯誤: {}", taskId, e.getMessage());
        }
        EXPORT_RESULT_MAP.remove(taskId);
        PROGRESS_MAP.remove(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importDataWithImages(MultipartFile file, Boolean updateSupport, Long defaultCategoryId, String defaultUnit) throws Exception {
        // 檢查檔案
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上傳檔案不能為空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ServiceException("無法取得檔案名稱");
        }

        // 判斷檔案類型
        boolean isZip = originalFilename.toLowerCase().endsWith(".zip");
        boolean isExcel = originalFilename.toLowerCase().endsWith(".xlsx") || originalFilename.toLowerCase().endsWith(".xls");

        if (!isZip && !isExcel) {
            throw new ServiceException("不支援的檔案格式，請上傳 Excel 或 ZIP 檔案");
        }

        // 建立臨時目錄
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "import_" + UUID.randomUUID());
        if (!tempDir.mkdirs()) {
            throw new ServiceException("無法建立臨時目錄");
        }

        try {
            File excelFile;
            File imagesDir = null;

            if (isZip) {
                // ZIP 檔案：解壓縮並尋找 Excel 和 images.zip
                log.info("開始處理 ZIP 檔案: {}", originalFilename);
                File zipFile = new File(tempDir, "upload.zip");
                file.transferTo(zipFile);

                File extractDir = new File(tempDir, "extract");
                ImageImportUtil.unzip(zipFile, extractDir);

                // 尋找 Excel 檔案
                excelFile = findExcelFile(extractDir);
                if (excelFile == null) {
                    throw new ServiceException("ZIP 檔案中未找到 Excel 檔案");
                }

                // 尋找 images.zip
                File imagesZip = new File(extractDir, "images.zip");
                if (imagesZip.exists()) {
                    imagesDir = new File(tempDir, "images");
                    ImageImportUtil.unzip(imagesZip, imagesDir);
                    log.info("解壓縮圖片: {} 張", countFiles(imagesDir));
                }
            } else {
                // Excel 檔案：直接儲存
                log.info("開始處理 Excel 檔案: {}", originalFilename);
                excelFile = new File(tempDir, originalFilename);
                file.transferTo(excelFile);
            }

            // 執行匯入
            ImportResult result = importFromExcel(excelFile, imagesDir, updateSupport, defaultCategoryId, defaultUnit);

            // 返回 HTML 格式的結果訊息
            return result.toHtmlMessage();

        } finally {
            // 清理臨時目錄
            deleteDirectory(tempDir);
        }
    }

    /**
     * 從 Excel 執行匯入
     */
    private ImportResult importFromExcel(File excelFile, File imagesDir, Boolean updateSupport, Long defaultCategoryId, String defaultUnit) throws Exception {
        ImportResult result = new ImportResult();

        // 讀取 Excel
        ExcelUtil<InvItemWithStockDTO> util = new ExcelUtil<>(InvItemWithStockDTO.class);
        List<InvItemWithStockDTO> dataList;
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            dataList = util.importExcel(fis);
        } catch (Exception e) {
            throw new ServiceException("讀取 Excel 失敗: " + e.getMessage());
        }

        result.setTotalRows(dataList.size());
        log.info("讀取 Excel 完成，共 {} 筆資料", dataList.size());

        // 統計圖片
        if (imagesDir != null) {
            result.setTotalImages(countFiles(imagesDir));
        }

        // 逐筆處理
        int rowNum = 2;  // Excel 從第 2 行開始（第 1 行是標題）
        for (InvItemWithStockDTO dto : dataList) {
            try {
                // 驗證必要欄位
                if (dto.getItemName() == null || dto.getItemName().trim().isEmpty()) {
                    result.addError(rowNum, "", "物品名稱不能為空");
                    result.setFailedRows(result.getFailedRows() + 1);
                    rowNum++;
                    continue;
                }

                // 檢查物品編碼是否重複
                InvItem existingItem = null;
                if (dto.getItemCode() != null && !dto.getItemCode().trim().isEmpty()) {
                    existingItem = invItemMapper.selectInvItemByItemCode(dto.getItemCode());
                }

                if (existingItem != null) {
                    if (updateSupport != null && updateSupport) {
                        // 更新現有資料
                        updateItemFromDTO(existingItem, dto, defaultCategoryId, defaultUnit);
                        invItemMapper.updateInvItem(existingItem);
                        result.setSuccessRows(result.getSuccessRows() + 1);
                        log.debug("更新物品: {} (編碼: {})", dto.getItemName(), dto.getItemCode());
                    } else {
                        // 跳過
                        result.setSkippedRows(result.getSkippedRows() + 1);
                        log.debug("跳過重複物品: {} (編碼: {})", dto.getItemName(), dto.getItemCode());
                    }
                } else {
                    // 新增
                    InvItem newItem = createItemFromDTO(dto, defaultCategoryId, defaultUnit);
                    invItemMapper.insertInvItem(newItem);

                    // 建立庫存記錄
                    InvStock stock = new InvStock();
                    stock.setItemId(newItem.getItemId());
                    stock.setTotalQuantity(dto.getTotalQuantity() != null ? dto.getTotalQuantity() : 0);
                    stock.setAvailableQty(dto.getTotalQuantity() != null ? dto.getTotalQuantity() : 0);
                    stock.setBorrowedQty(0);
                    stock.setReservedQty(0);
                    stock.setDamagedQty(0);
                    stock.setLostQty(0);
                    invStockMapper.insertInvStock(stock);

                    result.setSuccessRows(result.getSuccessRows() + 1);
                    log.debug("新增物品: {} (編碼: {})", dto.getItemName(), dto.getItemCode());
                }

                // 處理圖片
                if (imagesDir != null && dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
                    processImage(dto.getImageUrl(), imagesDir, result);
                }

            } catch (Exception e) {
                result.addError(rowNum, dto.getItemName(), e.getMessage());
                result.setFailedRows(result.getFailedRows() + 1);
                log.error("匯入第 {} 行失敗: {}", rowNum, e.getMessage(), e);
            }

            rowNum++;
        }

        log.info("匯入完成 - 成功: {}, 失敗: {}, 跳過: {}", result.getSuccessRows(), result.getFailedRows(), result.getSkippedRows());
        return result;
    }

    /**
     * 處理單張圖片
     */
    private void processImage(String dbPath, File imagesDir, ImportResult result) {
        try {
            // 提取檔名和相對路徑
            String fileName = com.cheng.common.utils.file.ImageImportUtil.extractFileName(dbPath);
            String relativePath = com.cheng.common.utils.file.ImageImportUtil.extractRelativePath(dbPath);

            if (fileName.isEmpty()) {
                result.setMissingImages(result.getMissingImages() + 1);
                return;
            }

            // 在 imagesDir 中搜尋圖片
            File sourceImage = com.cheng.common.utils.file.ImageImportUtil.findImageFile(imagesDir, fileName);
            if (sourceImage == null) {
                result.setMissingImages(result.getMissingImages() + 1);
                log.warn("圖片未找到: {}", fileName);
                return;
            }

            // 驗證圖片
            if (!com.cheng.common.utils.file.ImageImportUtil.validateImage(sourceImage, 10 * 1024 * 1024)) {
                result.setMissingImages(result.getMissingImages() + 1);
                log.warn("圖片驗證失敗: {}", fileName);
                return;
            }

            // 複製圖片到 uploadPath
            File backupFile = com.cheng.common.utils.file.ImageImportUtil.copyImageToUploadPath(sourceImage, relativePath, uploadPath);

            result.setCopiedImages(result.getCopiedImages() + 1);
            if (backupFile != null) {
                result.setOverwrittenImages(result.getOverwrittenImages() + 1);
            }

        } catch (Exception e) {
            result.setMissingImages(result.getMissingImages() + 1);
            log.error("處理圖片失敗: {}", dbPath, e);
        }
    }

    /**
     * 從 DTO 建立 InvItem
     */
    private InvItem createItemFromDTO(InvItemWithStockDTO dto, Long defaultCategoryId, String defaultUnit) {
        InvItem item = new InvItem();
        item.setItemName(dto.getItemName());
        item.setItemCode(dto.getItemCode());
        item.setBarcode(dto.getBarcode());
        item.setQrCode(dto.getQrCode());
        item.setCategoryId(dto.getCategoryId() != null ? dto.getCategoryId() : defaultCategoryId);
        item.setUnit(dto.getUnit() != null ? dto.getUnit() : defaultUnit);
        item.setSpecification(dto.getSpecification());
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        item.setPurchasePrice(dto.getPurchasePrice());
        item.setCurrentPrice(dto.getCurrentPrice());
        item.setSupplier(dto.getSupplier());
        item.setMinStock(dto.getMinStock());
        item.setMaxStock(dto.getMaxStock());
        item.setLocation(dto.getLocation());
        item.setDescription(dto.getDescription());
        item.setImageUrl(dto.getImageUrl());
        item.setStatus("0");  // 正常
        item.setCreateBy(SecurityUtils.getUsername());
        item.setCreateTime(new Date());
        return item;
    }

    /**
     * 從 DTO 更新 InvItem
     */
    private void updateItemFromDTO(InvItem item, InvItemWithStockDTO dto, Long defaultCategoryId, String defaultUnit) {
        if (dto.getItemName() != null) item.setItemName(dto.getItemName());
        if (dto.getItemCode() != null) item.setItemCode(dto.getItemCode());
        if (dto.getBarcode() != null) item.setBarcode(dto.getBarcode());
        if (dto.getQrCode() != null) item.setQrCode(dto.getQrCode());
        if (dto.getCategoryId() != null) {
            item.setCategoryId(dto.getCategoryId());
        } else if (defaultCategoryId != null) {
            item.setCategoryId(defaultCategoryId);
        }
        if (dto.getUnit() != null) {
            item.setUnit(dto.getUnit());
        } else if (defaultUnit != null) {
            item.setUnit(defaultUnit);
        }
        if (dto.getSpecification() != null) item.setSpecification(dto.getSpecification());
        if (dto.getBrand() != null) item.setBrand(dto.getBrand());
        if (dto.getModel() != null) item.setModel(dto.getModel());
        if (dto.getPurchasePrice() != null) item.setPurchasePrice(dto.getPurchasePrice());
        if (dto.getCurrentPrice() != null) item.setCurrentPrice(dto.getCurrentPrice());
        if (dto.getSupplier() != null) item.setSupplier(dto.getSupplier());
        if (dto.getMinStock() != null) item.setMinStock(dto.getMinStock());
        if (dto.getMaxStock() != null) item.setMaxStock(dto.getMaxStock());
        if (dto.getLocation() != null) item.setLocation(dto.getLocation());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());
        item.setUpdateBy(SecurityUtils.getUsername());
        item.setUpdateTime(new Date());
    }

    /**
     * 尋找 Excel 檔案
     */
    private File findExcelFile(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * 統計目錄中的檔案數量
     */
    private int countFiles(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFiles(file);
                }
            }
        }
        return count;
    }

    /**
     * 遞迴刪除目錄
     */
    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        try {
            Path dirPath = dir.toPath();
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder()) // 先刪除檔案，再刪除目錄
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("無法刪除檔案: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("刪除目錄失敗: {}", dir.getAbsolutePath(), e);
        }
    }
}
