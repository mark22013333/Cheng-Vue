package com.cheng.shop.service;

import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.payment.CallbackResult;

import java.util.List;
import java.util.Map;

/**
 * 訂單 Service 介面
 *
 * @author cheng
 */
public interface IShopOrderService {

    /**
     * 查詢訂單列表
     *
     * @param order 查詢條件
     * @return 訂單列表
     */
    List<ShopOrder> selectOrderList(ShopOrder order);

    /**
     * 根據ID查詢訂單（含明細）
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
    List<ShopOrder> selectOrdersByMemberId(Long memberId, String status);

    /**
     * 建立訂單
     *
     * @param order 訂單
     * @return 訂單ID
     */
    Long createOrder(ShopOrder order, Long operatorId);

    /**
     * 取消訂單
     *
     * @param orderId 訂單ID
     * @param reason  取消原因
     * @return 影響行數
     */
    int cancelOrder(Long orderId, String reason, Long operatorId);

    /**
     * 支付訂單
     *
     * @param orderId 訂單ID
     * @return 影響行數
     */
    int payOrder(Long orderId);

    /**
     * 出貨
     *
     * @param orderId    訂單ID
     * @param trackingNo 物流單號
     * @return 影響行數
     */
    int shipOrder(Long orderId, String trackingNo);

    /**
     * 確認收貨
     *
     * @param orderId 訂單ID
     * @return 影響行數
     */
    int deliverOrder(Long orderId);

    /**
     * 完成訂單
     *
     * @param orderId 訂單ID
     * @return 影響行數
     */
    int completeOrder(Long orderId);

    /**
     * 更新訂單備註
     *
     * @param orderId     訂單ID
     * @param adminRemark 管理員備註
     * @return 影響行數
     */
    int updateOrderRemark(Long orderId, String adminRemark);

    /**
     * 產生訂單編號
     *
     * @return 訂單編號
     */
    String generateOrderNo();

    /**
     * 統計各狀態訂單數量
     *
     * @param memberId 會員ID
     * @return 統計資料
     */
    Map<String, Integer> countOrderByStatus(Long memberId);

    /**
     * 手動更新付款狀態
     *
     * @param orderId   訂單ID
     * @param payStatus 付款狀態
     * @return 影響行數
     */
    int updatePayStatus(Long orderId, String payStatus);

    /**
     * 手動更新物流狀態
     *
     * @param orderId    訂單ID
     * @param shipStatus 物流狀態
     * @return 影響行數
     */
    int updateShipStatus(Long orderId, String shipStatus);

    /**
     * 更新物流單號
     *
     * @param orderId    訂單ID
     * @param shippingNo 物流單號（ECPay AllPayLogisticsID）
     * @return 影響行數
     */
    int updateShippingNo(Long orderId, String shippingNo);

    /**
     * 處理金流回調
     * <p>
     * 使用悲觀鎖（FOR UPDATE）確保併發安全，在事務內執行：
     * <ol>
     *     <li>鎖定訂單行</li>
     *     <li>冪等性檢查（已付款則跳過）</li>
     *     <li>更新訂單狀態</li>
     *     <li>發布 {@link com.cheng.shop.event.PaymentSuccessEvent} 事件</li>
     * </ol>
     * 銷量更新等副作用由事件監聯器在事務提交後異步處理。
     *
     * @param result 金流回調結果
     * @return 處理結果訊息（回傳給金流平台）
     */
    String handlePaymentCallback(CallbackResult result);

    /**
     * 重新建立物流訂單
     * <p>
     * 用於超商取貨訂單物流單號產生失敗時，手動觸發重新建立。
     *
     * @param orderId 訂單ID
     * @return 新的物流單號
     */
    String recreateLogistics(Long orderId);
}
