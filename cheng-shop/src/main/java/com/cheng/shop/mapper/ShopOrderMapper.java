package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 訂單 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopOrderMapper {

    /**
     * 查詢訂單列表
     *
     * @param order 查詢條件
     * @return 訂單列表
     */
    List<ShopOrder> selectOrderList(ShopOrder order);

    /**
     * 根據ID查詢訂單
     *
     * @param orderId 訂單ID
     * @return 訂單
     */
    ShopOrder selectOrderById(Long orderId);

    /**
     * 根據訂單編號查詢訂單
     *
     * @param orderNo 訂單編號
     * @return 訂單
     */
    ShopOrder selectOrderByOrderNo(String orderNo);

    /**
     * 查詢會員訂單列表
     *
     * @param memberId 會員ID
     * @param status   訂單狀態（可選）
     * @return 訂單列表
     */
    List<ShopOrder> selectOrdersByMemberId(@Param("memberId") Long memberId, @Param("status") String status);

    /**
     * 新增訂單
     *
     * @param order 訂單
     * @return 影響行數
     */
    int insertOrder(ShopOrder order);

    /**
     * 更新訂單
     *
     * @param order 訂單
     * @return 影響行數
     */
    int updateOrder(ShopOrder order);

    /**
     * 更新訂單狀態
     *
     * @param orderId 訂單ID
     * @param status  訂單狀態
     * @return 影響行數
     */
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * 更新支付狀態
     *
     * @param orderId   訂單ID
     * @param payStatus 支付狀態
     * @return 影響行數
     */
    int updatePayStatus(@Param("orderId") Long orderId, @Param("payStatus") String payStatus);

    /**
     * 更新物流狀態
     *
     * @param orderId    訂單ID
     * @param shipStatus 物流狀態
     * @return 影響行數
     */
    int updateShipStatus(@Param("orderId") Long orderId, @Param("shipStatus") String shipStatus);

    /**
     * 刪除訂單
     *
     * @param orderId 訂單ID
     * @return 影響行數
     */
    int deleteOrderById(Long orderId);

    /**
     * 統計訂單數量（依狀態）
     *
     * @param memberId 會員ID
     * @param status   訂單狀態
     * @return 數量
     */
    int countOrderByStatus(@Param("memberId") Long memberId, @Param("status") String status);
}
