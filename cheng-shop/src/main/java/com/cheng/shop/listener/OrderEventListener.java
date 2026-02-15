package com.cheng.shop.listener;

import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopOrderItem;
import com.cheng.shop.enums.ShippingMethod;
import com.cheng.shop.event.PaymentSuccessEvent;
import com.cheng.shop.logistics.EcpayLogisticsGateway;
import com.cheng.shop.logistics.LogisticsResult;
import com.cheng.shop.service.IShopOrderService;
import com.cheng.shop.service.IShopProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 訂單事件監聽器
 * <p>
 * 使用 {@link TransactionalEventListener} 確保只有在資料庫事務成功提交後才執行副作用。
 * 搭配 {@link Async} 異步執行，避免阻塞 Web 回調執行緒。
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final IShopProductService productService;
    private final IShopOrderService orderService;
    private final EcpayLogisticsGateway logisticsGateway;

    /**
     * 監聽支付成功事件：更新商品銷量
     * <p>
     * phase = AFTER_COMMIT 表示只有訂單狀態成功寫入資料庫後才執行。
     * 若訂單更新失敗回滾，此方法不會被觸發，確保銷量不會錯誤增加。
     *
     * @param event 支付成功事件
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        ShopOrder order = event.getOrder();
        log.info("開始異步處理訂單銷量更新，訂單號：{}", order.getOrderNo());

        try {
            // 重新載入含 OrderItems 的完整訂單資訊
            ShopOrder fullOrder = orderService.selectOrderById(order.getOrderId());
            if (fullOrder == null || fullOrder.getOrderItems() == null) {
                log.warn("訂單不存在或無明細，跳過銷量更新，orderId={}", order.getOrderId());
                return;
            }

            for (ShopOrderItem item : fullOrder.getOrderItems()) {
                if (item.getProductId() != null && item.getQuantity() != null) {
                    productService.increaseSalesCount(item.getProductId(), item.getQuantity());
                }
            }

            log.info("訂單 {} 銷量更新完成，共 {} 個商品", order.getOrderNo(), fullOrder.getOrderItems().size());
        } catch (Exception e) {
            // 銷量更新失敗不應影響已完成的支付，記錄錯誤供人工補償
            log.error("訂單 {} 銷量更新失敗，需人工介入：{}", order.getOrderNo(), e.getMessage(), e);
        }
    }

    /**
     * 監聽支付成功事件：建立物流訂單
     * <p>
     * 僅針對超商取貨訂單呼叫 ECPay 物流 API 建立託運單。
     * 宅配訂單由後台人員手動處理。
     *
     * @param event 支付成功事件
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccessCreateLogistics(PaymentSuccessEvent event) {
        ShopOrder order = event.getOrder();

        // 判斷是否為超商取貨訂單
        if (order.getShippingMethod() == null) {
            log.debug("訂單 {} 無物流方式，跳過物流建立", order.getOrderNo());
            return;
        }

        ShippingMethod shippingMethod;
        try {
            shippingMethod = ShippingMethod.fromCode(order.getShippingMethod());
        } catch (Exception e) {
            log.warn("訂單 {} 物流方式無效：{}，跳過物流建立", order.getOrderNo(), order.getShippingMethod());
            return;
        }

        // 只有超商取貨需要自動建立物流
        if (!shippingMethod.isCvs()) {
            log.debug("訂單 {} 為宅配訂單，跳過自動物流建立", order.getOrderNo());
            return;
        }

        log.info("開始建立超商物流訂單，訂單號：{}", order.getOrderNo());

        try {
            // 重新載入完整訂單資訊
            ShopOrder fullOrder = orderService.selectOrderById(order.getOrderId());
            if (fullOrder == null) {
                log.warn("訂單不存在，跳過物流建立，orderId={}", order.getOrderId());
                return;
            }

            // 呼叫 ECPay 建立物流訂單
            LogisticsResult result = logisticsGateway.createShipment(fullOrder);

            if (result.isSuccess()) {
                // 更新訂單物流單號
                orderService.updateShippingNo(fullOrder.getOrderId(), result.getLogisticsId());
                log.info("訂單 {} 物流建立成功，物流單號：{}", order.getOrderNo(), result.getLogisticsId());
            } else {
                log.error("訂單 {} 物流建立失敗：{}", order.getOrderNo(), result.getErrorMessage());
            }
        } catch (Exception e) {
            // 物流建立失敗不應影響已完成的支付，記錄錯誤供人工補償
            log.error("訂單 {} 物流建立異常，需人工介入：{}", order.getOrderNo(), e.getMessage(), e);
        }
    }
}
