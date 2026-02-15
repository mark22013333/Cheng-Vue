package com.cheng.shop.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopOrderItem;
import com.cheng.shop.enums.OrderStatus;
import com.cheng.shop.enums.PayStatus;
import com.cheng.shop.enums.ShipStatus;
import com.cheng.shop.enums.ShippingMethod;
import com.cheng.shop.event.PaymentSuccessEvent;
import com.cheng.shop.logistics.EcpayLogisticsGateway;
import com.cheng.shop.logistics.LogisticsResult;
import com.cheng.shop.mapper.ShopOrderItemMapper;
import com.cheng.shop.mapper.ShopOrderMapper;
import com.cheng.shop.mapper.ShopProductSkuMapper;
import com.cheng.shop.payment.CallbackResult;
import com.cheng.shop.service.IShopGiftService;
import com.cheng.shop.service.IShopOrderService;
import com.cheng.shop.service.IShopProductService;
import com.cheng.shop.service.ShopStockSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 訂單 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopOrderServiceImpl implements IShopOrderService {

    private final ShopOrderMapper orderMapper;
    private final ShopOrderItemMapper orderItemMapper;
    private final ShopProductSkuMapper skuMapper;
    private final ShopStockSyncService stockSyncService;
    private final IShopGiftService giftService;
    private final IShopProductService productService;
    private final ApplicationEventPublisher eventPublisher;
    private final EcpayLogisticsGateway logisticsGateway;

    @Override
    public List<ShopOrder> selectOrderList(ShopOrder order) {
        return orderMapper.selectOrderList(order);
    }

    @Override
    public ShopOrder selectOrderById(Long orderId) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order != null) {
            List<ShopOrderItem> items = orderItemMapper.selectOrderItemsByOrderId(orderId);
            order.setOrderItems(items);
        }
        return order;
    }

    @Override
    public ShopOrder selectOrderByOrderNo(String orderNo) {
        ShopOrder order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order != null) {
            List<ShopOrderItem> items = orderItemMapper.selectOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(items);
        }
        return order;
    }

    @Override
    public List<ShopOrder> selectOrdersByMemberId(Long memberId, String status) {
        List<ShopOrder> orders = orderMapper.selectOrdersByMemberId(memberId, status);
        for (ShopOrder order : orders) {
            List<ShopOrderItem> items = orderItemMapper.selectOrderItemsByOrderId(order.getOrderId());
            order.setOrderItems(items);
        }
        return orders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(ShopOrder order, Long operatorId) {
        // 產生訂單編號
        order.setOrderNo(generateOrderNo());

        // 設定初始狀態
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setPayStatus(PayStatus.UNPAID.getCode());
        order.setShipStatus(ShipStatus.UNSHIPPED.getCode());
        order.setCreateTime(DateUtils.getNowDate());

        // 新增訂單
        orderMapper.insertOrder(order);

        // 新增訂單明細並扣減庫存
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (ShopOrderItem item : order.getOrderItems()) {
                item.setOrderId(order.getOrderId());

                // 扣減 SKU 庫存
                int result = skuMapper.decreaseStock(item.getSkuId(), item.getQuantity());
                if (result <= 0) {
                    throw new ServiceException("商品「" + item.getProductName() + "」庫存不足");
                }
            }
            orderItemMapper.batchInsertOrderItems(order.getOrderItems());

            // 同步扣減庫存物品
            Long syncOperatorId = operatorId != null ? operatorId : order.getMemberId();
            stockSyncService.deductStockForOrder(order.getOrderItems(), syncOperatorId, order.getOrderNo());
        }

        log.info("建立訂單成功，訂單編號: {}", order.getOrderNo());
        return order.getOrderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(Long orderId, String reason, Long operatorId) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (currentStatus == null || !currentStatus.canCancel()) {
            throw new ServiceException("當前訂單狀態無法取消");
        }

        // 恢復 SKU 庫存
        List<ShopOrderItem> items = orderItemMapper.selectOrderItemsByOrderId(orderId);
        for (ShopOrderItem item : items) {
            skuMapper.increaseStock(item.getSkuId(), item.getQuantity());
        }

        // 同步恢復庫存物品
        Long syncOperatorId = operatorId != null ? operatorId : order.getMemberId();
        stockSyncService.restoreStockForOrder(items, syncOperatorId, order.getOrderNo());

        // 更新訂單狀態
        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(OrderStatus.CANCELLED.getCode());
        updateOrder.setCancelTime(DateUtils.getNowDate());
        updateOrder.setCancelReason(reason);

        log.info("取消訂單，訂單ID: {}, 原因: {}", orderId, reason);
        return orderMapper.updateOrder(updateOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payOrder(Long orderId) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        PayStatus payStatus = order.getPayStatusEnum();
        if (payStatus != PayStatus.UNPAID) {
            throw new ServiceException("訂單支付狀態異常");
        }

        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(OrderStatus.PAID.getCode());
        updateOrder.setPayStatus(PayStatus.PAID.getCode());
        updateOrder.setPaidTime(DateUtils.getNowDate());

        int result = orderMapper.updateOrder(updateOrder);

        // 更新商品銷量
        List<ShopOrderItem> items = orderItemMapper.selectOrderItemsByOrderId(orderId);
        for (ShopOrderItem item : items) {
            if (item.getProductId() != null && item.getQuantity() != null) {
                productService.increaseSalesCount(item.getProductId(), item.getQuantity());
            }
        }

        log.info("訂單支付成功，訂單ID: {}", orderId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int shipOrder(Long orderId, String trackingNo) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (currentStatus != OrderStatus.PAID && currentStatus != OrderStatus.PROCESSING) {
            throw new ServiceException("當前訂單狀態無法出貨");
        }

        // 出貨時扣減禮物庫存
        if (order.getGiftId() != null) {
            int deducted = giftService.decreaseStock(order.getGiftId(), 1);
            if (deducted <= 0) {
                log.warn("禮物庫存扣減失敗，giftId={}，訂單仍繼續出貨", order.getGiftId());
            } else {
                log.info("禮物庫存已扣減，giftId={}, 訂單ID={}", order.getGiftId(), orderId);
            }
        }

        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(OrderStatus.SHIPPED.getCode());
        updateOrder.setShipStatus(ShipStatus.SHIPPED.getCode());
        updateOrder.setShippingNo(trackingNo);
        updateOrder.setShippedTime(DateUtils.getNowDate());

        log.info("訂單出貨，訂單ID: {}, 物流單號: {}", orderId, trackingNo);
        return orderMapper.updateOrder(updateOrder);
    }

    @Override
    public int deliverOrder(Long orderId) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        if (order.getStatusEnum() != OrderStatus.SHIPPED) {
            throw new ServiceException("當前訂單狀態無法確認收貨");
        }

        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(OrderStatus.DELIVERED.getCode());
        updateOrder.setShipStatus(ShipStatus.DELIVERED.getCode());
        updateOrder.setReceivedTime(DateUtils.getNowDate());

        log.info("訂單確認收貨，訂單ID: {}", orderId);
        return orderMapper.updateOrder(updateOrder);
    }

    @Override
    public int completeOrder(Long orderId) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        if (order.getStatusEnum() != OrderStatus.DELIVERED) {
            throw new ServiceException("當前訂單狀態無法完成");
        }

        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(OrderStatus.COMPLETED.getCode());
        updateOrder.setCompleteTime(DateUtils.getNowDate());

        log.info("訂單完成，訂單ID: {}", orderId);
        return orderMapper.updateOrder(updateOrder);
    }

    @Override
    public int updateOrderRemark(Long orderId, String adminRemark) {
        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setSellerRemark(adminRemark);
        return orderMapper.updateOrder(updateOrder);
    }

    @Override
    public String generateOrderNo() {
        // 格式: O + yyMMdd(6) + HHmmss(6) + 7碼隨機英數字 = 20碼
        // 範例: O2601300947035DE91AB
        // 綠界 MerchantTradeNo 上限 20 碼
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = IdUtils.fastSimpleUUID().substring(0, 7).toUpperCase();
        return "O" + dateTime + random;
    }

    @Override
    public Map<String, Integer> countOrderByStatus(Long memberId) {
        Map<String, Integer> result = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            int count = orderMapper.countOrderByStatus(memberId, status.getCode());
            result.put(status.getCode(), count);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePayStatus(Long orderId, String payStatus) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new ServiceException("已完成或已取消的訂單無法修改付款狀態");
        }

        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setPayStatus(payStatus);

        // 如果標記為已付款，且原本是待付款狀態，則更新訂單狀態為已付款
        if (PayStatus.PAID.getCode().equals(payStatus) && currentStatus == OrderStatus.PENDING) {
            updateOrder.setStatus(OrderStatus.PAID.getCode());
            updateOrder.setPaidTime(DateUtils.getNowDate());
        }

        int result = orderMapper.updateOrder(updateOrder);

        // 手動標記為已付款時，同步更新商品銷量
        if (PayStatus.PAID.getCode().equals(payStatus)
                && !PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            List<ShopOrderItem> items = orderItemMapper.selectOrderItemsByOrderId(orderId);
            for (ShopOrderItem item : items) {
                if (item.getProductId() != null && item.getQuantity() != null) {
                    productService.increaseSalesCount(item.getProductId(), item.getQuantity());
                }
            }
        }

        log.info("手動更新訂單付款狀態，訂單ID: {}, 新狀態: {}", orderId, payStatus);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShipStatus(Long orderId, String shipStatus) {
        ShopOrder order = orderMapper.selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        OrderStatus currentStatus = order.getStatusEnum();
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new ServiceException("已完成或已取消的訂單無法修改物流狀態");
        }

        ShopOrder updateOrder = new ShopOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setShipStatus(shipStatus);

        // 如果標記為已出貨，更新訂單狀態
        if (ShipStatus.SHIPPED.getCode().equals(shipStatus) && currentStatus != OrderStatus.SHIPPED) {
            updateOrder.setStatus(OrderStatus.SHIPPED.getCode());
            updateOrder.setShippedTime(DateUtils.getNowDate());
        }

        // 如果標記為已到貨，更新訂單狀態
        if (ShipStatus.DELIVERED.getCode().equals(shipStatus) && currentStatus != OrderStatus.DELIVERED) {
            updateOrder.setStatus(OrderStatus.DELIVERED.getCode());
            updateOrder.setReceivedTime(DateUtils.getNowDate());
        }

        log.info("手動更新訂單物流狀態，訂單ID: {}, 新狀態: {}", orderId, shipStatus);
        return orderMapper.updateOrder(updateOrder);
    }

    @Override
    public int updateShippingNo(Long orderId, String shippingNo) {
        log.info("更新訂單物流單號，訂單ID: {}, 物流單號: {}", orderId, shippingNo);
        return orderMapper.updateShippingNo(orderId, shippingNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePaymentCallback(CallbackResult result) {
        String orderNo = result.getOrderNo();

        // 1. 使用 FOR UPDATE 鎖定訂單行，防止併發回調
        ShopOrder order = orderMapper.selectOrderByOrderNoForUpdate(orderNo);
        if (order == null) {
            log.error("金流回調：訂單不存在，orderNo={}", orderNo);
            return "0|ErrorMessage=Order Not Found";
        }

        // 2. 冪等性檢查：已付款的訂單不重複處理
        if (PayStatus.PAID.getCode().equals(order.getPayStatus())) {
            log.info("金流回調：訂單已付款，跳過處理，orderNo={}", orderNo);
            return result.getResponseBody();
        }

        // 3. 根據回調結果更新訂單
        if (result.isPaymentSuccess()) {
            ShopOrder updateOrder = new ShopOrder();
            updateOrder.setOrderId(order.getOrderId());
            updateOrder.setPayStatusEnum(PayStatus.PAID);
            updateOrder.setStatusEnum(OrderStatus.PAID);
            updateOrder.setEcpayTradeNo(result.getTradeNo());
            updateOrder.setEcpayInfo(result.getRawInfo());
            updateOrder.setPaidTime(DateUtils.getNowDate());
            updateOrder.setPaymentNo(result.getTradeNo());
            orderMapper.updateOrder(updateOrder);

            log.info("金流回調：訂單更新為已付款，orderNo={}, tradeNo={}", orderNo, result.getTradeNo());

            // 4. 發布支付成功事件（銷量更新等副作用由 Listener 在事務提交後處理）
            // 注意：傳遞的是 updateOrder（含 orderId），Listener 會重新載入完整訂單
            updateOrder.setOrderNo(orderNo); // 補上 orderNo 供 Listener 記錄日誌
            updateOrder.setShippingMethod(order.getShippingMethod()); // 供物流建立判斷
            eventPublisher.publishEvent(new PaymentSuccessEvent(this, updateOrder));
        } else {
            log.warn("金流回調：付款未成功，orderNo={}", orderNo);
        }

        return result.getResponseBody();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String recreateLogistics(Long orderId) {
        ShopOrder order = selectOrderById(orderId);
        if (order == null) {
            throw new ServiceException("訂單不存在");
        }

        // 驗證訂單狀態：必須是已付款
        if (order.getStatusEnum() != OrderStatus.PAID) {
            throw new ServiceException("只有已付款的訂單才能建立物流");
        }

        // 驗證是否為超商取貨訂單
        if (order.getShippingMethod() == null) {
            throw new ServiceException("訂單缺少物流方式");
        }

        ShippingMethod shippingMethod;
        try {
            shippingMethod = ShippingMethod.fromCode(order.getShippingMethod());
        } catch (Exception e) {
            throw new ServiceException("無效的物流方式：" + order.getShippingMethod());
        }

        if (!shippingMethod.isCvs()) {
            throw new ServiceException("只有超商取貨訂單才能使用此功能");
        }

        // 呼叫 ECPay 建立物流訂單
        log.info("手動重建物流訂單，訂單ID: {}, 訂單編號: {}", orderId, order.getOrderNo());
        LogisticsResult result = logisticsGateway.createShipment(order);

        if (!result.isSuccess()) {
            throw new ServiceException("物流訂單建立失敗：" + result.getErrorMessage());
        }

        // 更新訂單物流單號
        String logisticsId = result.getLogisticsId();
        updateShippingNo(orderId, logisticsId);

        log.info("物流訂單重建成功，訂單ID: {}, 物流單號: {}", orderId, logisticsId);
        return logisticsId;
    }
}
