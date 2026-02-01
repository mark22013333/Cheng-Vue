package com.cheng.shop.event;

import com.cheng.shop.domain.ShopOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 支付成功事件
 * <p>
 * 當訂單支付成功後發布此事件，由 {@link com.cheng.shop.listener.OrderEventListener} 監聽處理。
 * 使用 Spring Event 機制解耦「訂單狀態更新」與「銷量更新」等副作用。
 *
 * @author cheng
 */
@Getter
public class PaymentSuccessEvent extends ApplicationEvent {

    /**
     * 已付款的訂單（含 orderId 和 orderNo，但不含 orderItems）
     */
    private final ShopOrder order;

    public PaymentSuccessEvent(Object source, ShopOrder order) {
        super(source);
        this.order = order;
    }
}
