package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopOrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 訂單明細 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopOrderItemMapper {

    /**
     * 根據訂單ID查詢訂單明細
     *
     * @param orderId 訂單ID
     * @return 訂單明細列表
     */
    List<ShopOrderItem> selectOrderItemsByOrderId(Long orderId);

    /**
     * 根據ID查詢訂單明細
     *
     * @param itemId 明細ID
     * @return 訂單明細
     */
    ShopOrderItem selectOrderItemById(Long itemId);

    /**
     * 新增訂單明細
     *
     * @param orderItem 訂單明細
     * @return 影響行數
     */
    int insertOrderItem(ShopOrderItem orderItem);

    /**
     * 批量新增訂單明細
     *
     * @param orderItems 訂單明細列表
     * @return 影響行數
     */
    int batchInsertOrderItems(List<ShopOrderItem> orderItems);

    /**
     * 刪除訂單明細
     *
     * @param itemId 明細ID
     * @return 影響行數
     */
    int deleteOrderItemById(Long itemId);

    /**
     * 根據訂單ID刪除所有明細
     *
     * @param orderId 訂單ID
     * @return 影響行數
     */
    int deleteOrderItemsByOrderId(Long orderId);
}
