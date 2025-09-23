package com.cheng.system.service;

import com.cheng.system.domain.InvItem;

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
     * 掃描條碼或QR碼獲取物品資訊
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
}
