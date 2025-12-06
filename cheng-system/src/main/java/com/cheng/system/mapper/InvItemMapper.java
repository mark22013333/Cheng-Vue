package com.cheng.system.mapper;

import com.cheng.system.domain.InvItem;
import com.cheng.system.dto.InvItemWithStockDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("""
             select\s
                    i.item_id        as itemId,
                    i.item_code      as itemCode,
                    i.item_name      as itemName,
                    i.category_id    as categoryId,
                    c.category_name  as categoryName,
                    i.specification  as specification,
                    i.unit           as unit,
                    i.brand          as brand,
                    i.model          as model,
                    i.supplier       as supplier,
                    i.purchase_price as purchasePrice,
                    i.current_price  as currentPrice,
                    i.image_url      as imageUrl,
                    i.barcode        as barcode,
                    i.qr_code        as qrCode,
                    i.status         as status,
                    i.create_by      as createBy,
                    i.create_time    as createTime,
                    i.update_by      as updateBy,
                    i.update_time    as updateTime,
                    i.remark         as remark
             from inv_item i
             left join inv_category c on i.category_id = c.category_id
             left join inv_stock s on i.item_id = s.item_id
             where i.del_flag = '0'
               and s.total_quantity > 0
               and s.available_qty <= i.min_stock
             order by s.available_qty asc, i.item_name asc
            \s""")
    List<InvItem> selectLowStockItemList();

    /**
     * 查詢物品與庫存整合列表
     *
     * @param dto 查詢條件
     * @return 物品與庫存整合資訊集合
     */
    List<InvItemWithStockDTO> selectItemWithStockList(InvItemWithStockDTO dto);

    /**
     * 根據物品ID查詢物品與庫存整合資訊
     *
     * @param itemId 物品ID
     * @return 物品與庫存整合資訊
     */
    InvItemWithStockDTO selectItemWithStockByItemId(Long itemId);

    /**
     * 查詢低庫存物品與庫存整合列表
     *
     * @return 物品與庫存整合資訊集合
     */
    List<InvItemWithStockDTO> selectLowStockItemWithStockList();

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

    /**
     * 預約物品（使用樂觀鎖）
     *
     * @param itemId   物品ID
     * @param quantity 預約數量
     * @return 影響行數
     */
    int reserveItem(@Param("itemId") Long itemId,
                    @Param("quantity") Integer quantity);

    /**
     * 更新物品版本（用於樂觀鎖）
     *
     * @param itemId  物品ID
     * @param version 版本號
     * @param userId  用戶ID
     * @return 影響行數
     */
    int updateItemVersion(@Param("itemId") Long itemId,
                          @Param("version") Integer version,
                          @Param("userId") Long userId);
}
