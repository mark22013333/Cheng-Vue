package com.cheng.system.mapper;

import com.cheng.system.domain.InvStock;
import com.cheng.system.dto.InvStockStatisticsDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 庫存 數據層
 *
 * @author cheng
 */
public interface InvStockMapper {
    /**
     * 查詢庫存
     *
     * @param stockId 庫存主鍵
     * @return 庫存
     */
    InvStock selectInvStockByStockId(Long stockId);

    /**
     * 根據物品ID查詢庫存
     *
     * @param itemId 物品ID
     * @return 庫存
     */
    InvStock selectInvStockByItemId(Long itemId);

    /**
     * 查詢庫存列表
     *
     * @param invStock 庫存
     * @return 庫存集合
     */
    List<InvStock> selectInvStockList(InvStock invStock);

    /**
     * 查詢低庫存列表
     *
     * @return 庫存集合
     */
    List<InvStock> selectLowStockList();

    /**
     * 查詢庫存統計資訊
     *
     * @return 統計資訊
     */
    @Select("""
            select count(*) as totalItems,
                   sum(total_quantity) as totalQuantity,
                   sum(case when available_qty <= 10 and available_qty > 0 then 1 else 0 end) as lowStockItems,
                   sum(case when total_quantity = 0 then 1 else 0 end) as outOfStockItems
            from inv_stock s
            left join inv_item i on s.item_id = i.item_id
            where i.status = '0'""")
    @Results(id = "InvStockStatisticsMap", value = {
            @Result(property = "totalItems", column = "totalItems"),
            @Result(property = "totalQuantity", column = "totalQuantity"),
            @Result(property = "lowStockItems", column = "lowStockItems"),
            @Result(property = "outOfStockItems", column = "outOfStockItems")
    })
    List<InvStockStatisticsDTO> selectStockStatistics();

    /**
     * 新增庫存
     *
     * @param invStock 庫存
     * @return 結果
     */
    int insertInvStock(InvStock invStock);

    /**
     * 修改庫存
     *
     * @param invStock 庫存
     * @return 結果
     */
    int updateInvStock(InvStock invStock);

    /**
     * 更新庫存數量（入庫）
     * 使用註解方式實作，避免依賴 XML 映射。
     *
     * @param itemId   物品ID
     * @param quantity 數量（正值）
     * @return 影響筆數
     */
    @Update("update inv_stock " +
            "set total_quantity = total_quantity + #{quantity}, " +
            "    available_qty  = available_qty + #{quantity}, " +
            "    last_in_time   = now(), " +
            "    update_time    = now() " +
            "where item_id = #{itemId}")
    int updateStockIn(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * 更新庫存數量（出庫）
     *
     * @param itemId   物品ID
     * @param quantity 數量
     * @return 結果
     */
    @Update("update inv_stock " +
            "set total_quantity = total_quantity - #{quantity}, " +
            "    available_qty  = available_qty - #{quantity}, " +
            "    last_out_time  = now(), " +
            "    update_time    = now() " +
            "where item_id = #{itemId} and available_qty >= #{quantity}")
    int updateStockOut(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * 更新借出數量
     *
     * @param itemId   物品ID
     * @param quantity 數量
     * @return 結果
     */
    @Update("update inv_stock " +
            "set borrowed_qty  = borrowed_qty + #{quantity}, " +
            "    available_qty = available_qty - #{quantity}, " +
            "    update_time   = now() " +
            "where item_id = #{itemId} and available_qty >= #{quantity}")
    int updateBorrowedQty(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * 更新歸還數量
     *
     * @param itemId   物品ID
     * @param quantity 數量
     * @return 結果
     */
    @Update("update inv_stock " +
            "set borrowed_qty  = borrowed_qty - #{quantity}, " +
            "    available_qty = available_qty + #{quantity}, " +
            "    update_time   = now() " +
            "where item_id = #{itemId} and borrowed_qty >= #{quantity}")
    int updateReturnedQty(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * 更新預約數量
     *
     * @param itemId 物品ID
     * @param quantity 數量（正數表示增加，負數表示減少）
     * @return 結果
     */
    @Update("update inv_stock " +
            "set reserved_qty = case " +
            "    when reserved_qty + #{quantity} < 0 then 0 " +
            "    else reserved_qty + #{quantity} " +
            "end, " +
            "    update_time   = now() " +
            "where item_id = #{itemId}")
    int updateReservedQty(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * 刪除庫存
     *
     * @param stockId 庫存主鍵
     * @return 結果
     */
    int deleteInvStockByStockId(Long stockId);

    /**
     * 根據物品ID刪除庫存
     *
     * @param itemId 物品ID
     * @return 結果
     */
    @Delete("delete from inv_stock where item_id = #{itemId}")
    int deleteInvStockByItemId(@Param("itemId") Long itemId);

    /**
     * 批量刪除庫存
     *
     * @param stockIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvStockByStockIds(Long[] stockIds);
}
