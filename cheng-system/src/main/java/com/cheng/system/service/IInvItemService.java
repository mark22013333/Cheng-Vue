package com.cheng.system.service;

import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.vo.ReserveRequest;
import com.cheng.system.dto.ImportTaskResult;
import com.cheng.system.dto.InvItemImportDTO;
import com.cheng.system.domain.vo.ReserveResult;
import com.cheng.system.dto.InvItemWithStockDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 物品資訊 服務層
 *
 * @author cheng
 */
public interface IInvItemService {
    /**
     * 查詢物品資訊
     *
     * @param itemId 物品資訊主鍵
     * @return 物品資訊
     */
    InvItem selectInvItemByItemId(Long itemId);

    /**
     * 根據物品編碼查詢物品資訊
     *
     * @param itemCode 物品編碼
     * @return 物品資訊
     */
    InvItem selectInvItemByItemCode(String itemCode);

    /**
     * 根據條碼查詢物品資訊
     *
     * @param barcode 條碼
     * @return 物品資訊
     */
    InvItem selectInvItemByBarcode(String barcode);

    /**
     * 根據QR碼查詢物品資訊
     *
     * @param qrCode QR碼
     * @return 物品資訊
     */
    InvItem selectInvItemByQrCode(String qrCode);

    /**
     * 查詢物品資訊列表
     *
     * @param invItem 物品資訊
     * @return 物品資訊集合
     */
    List<InvItem> selectInvItemList(InvItem invItem);

    /**
     * 查詢低庫存物品列表
     *
     * @return 物品資訊集合
     */
    List<InvItem> selectLowStockItemList();

    /**
     * 掃描條碼或QR碼取得物品資訊
     *
     * @param scanCode 掃描內容
     * @param scanType 掃描類型（1條碼 2QR碼）
     * @return 物品資訊
     */
    InvItem scanItemByCode(String scanCode, String scanType);

    /**
     * 新增物品資訊
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    int insertInvItem(InvItem invItem);

    /**
     * 修改物品資訊
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    int updateInvItem(InvItem invItem);

    /**
     * 批量刪除物品資訊
     *
     * @param itemIds 需要刪除的物品資訊主鍵集合
     * @return 結果
     */
    int deleteInvItemByItemIds(Long[] itemIds);

    /**
     * 刪除物品資訊
     *
     * @param itemId 物品資訊主鍵
     * @return 結果
     */
    int deleteInvItemByItemId(Long itemId);

    /**
     * 檢查物品編碼是否唯一
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    boolean checkItemCodeUnique(InvItem invItem);

    /**
     * 檢查條碼是否唯一
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    boolean checkBarcodeUnique(InvItem invItem);

    /**
     * 匯入物品資料
     *
     * @param itemList        物品資料列表
     * @param isUpdateSupport 是否更新支援，如果已存在，則進行更新資料
     * @param operName        操作使用者
     * @return 結果
     */
    String importItem(List<InvItem> itemList, Boolean isUpdateSupport, String operName);

    /**
     * 安全刪除物品（檢查借出記錄、級聯刪除相關表）
     *
     * @param itemIds 需要刪除的物品ID陣列
     * @return 刪除結果訊息
     */
    String safeDeleteInvItemByItemIds(Long[] itemIds);

    /**
     * 建立匯入任務並返回任務資訊
     *
     * @param file              上傳的Excel檔案
     * @param updateSupport     是否更新支援
     * @param defaultCategoryId 預設分類ID
     * @param defaultUnit       預設單位
     * @return 任務資訊（包含taskId和rowCount）
     */
    ImportTaskResult createImportTask(MultipartFile file, Boolean updateSupport, Long defaultCategoryId, String defaultUnit);

    /**
     * 異步執行匯入任務
     *
     * @param taskId 任務ID
     */
    void asyncImportItems(String taskId);

    /**
     * 下載匯入範本
     *
     * @param response HTTP響應
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 匯入物品資料
     *
     * @param importDTO 匯入DTO
     * @return 匯入結果
     */
    String importItems(InvItemImportDTO importDTO);

    /**
     * 預約物品
     *
     * @param request 預約請求
     * @param userId  用戶ID
     * @return 預約結果
     */
    ReserveResult reserveItem(ReserveRequest request, Long userId);

    /**
     * 恢復物品的預約數量
     * <p>
     * 當預約被取消時，需要將預約數量從庫存的 reserved_qty 中減去
     *
     * @param itemId   物品ID
     * @param quantity 要恢復的數量
     * @return 是否成功恢復
     */
    boolean restoreReservedQuantity(Long itemId, Integer quantity);

    /**
     * 建立完整匯出任務（Excel + 圖片）
     *
     * @param dto 查詢條件
     * @return taskId
     */
    String createExportTask(InvItemWithStockDTO dto);

    /**
     * 異步執行完整匯出任務
     *
     * @param taskId 任務ID
     */
    void asyncExportWithImages(String taskId);

    /**
     * 下載匯出結果
     *
     * @param taskId   任務ID
     * @param response HTTP響應
     */
    void downloadExportResult(String taskId, HttpServletResponse response) throws Exception;

    /**
     * 匯入物品資料（支援 Excel 或 ZIP 檔案）
     *
     * @param file            上傳的檔案（Excel 或 ZIP）
     * @param updateSupport   是否更新已存在的資料
     * @param defaultCategoryId 預設分類ID
     * @param defaultUnit     預設單位
     * @return 匯入結果
     */
    String importDataWithImages(MultipartFile file, Boolean updateSupport, Long defaultCategoryId, String defaultUnit) throws Exception;
}
