package com.cheng.system.mapper;

import com.cheng.system.domain.InvItem;

import java.util.List;

/**
 * 物品資訊 數據層
 *
 * @author cheng
 */
public interface InvItemMapper {
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
     * 查詢物品資訊列表（含庫存資訊）
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
     * 檢查物品編碼是否唯一
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    InvItem checkItemCodeUnique(InvItem invItem);

    /**
     * 檢查條碼是否唯一
     *
     * @param invItem 物品資訊
     * @return 結果
     */
    InvItem checkBarcodeUnique(InvItem invItem);

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
     * 刪除物品資訊
     *
     * @param itemId 物品資訊主鍵
     * @return 結果
     */
    int deleteInvItemByItemId(Long itemId);

    /**
     * 批量刪除物品資訊
     *
     * @param itemIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvItemByItemIds(Long[] itemIds);
}
