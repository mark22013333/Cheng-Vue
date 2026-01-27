package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import com.cheng.shop.enums.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 訂單主表實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopOrder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 訂單ID
     */
    private Long orderId;

    /**
     * 訂單編號
     */
    private String orderNo;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 會員暱稱（冗餘）
     */
    private String memberNickname;

    /**
     * 商品金額
     */
    private BigDecimal productAmount;

    /**
     * 運費
     */
    private BigDecimal shippingAmount;

    /**
     * 折扣金額
     */
    private BigDecimal discountAmount;

    /**
     * 訂單總金額
     */
    private BigDecimal totalAmount;

    /**
     * 已付金額
     */
    private BigDecimal paidAmount;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人電話
     */
    private String receiverMobile;

    /**
     * 收件地址
     */
    private String receiverAddress;

    /**
     * 郵遞區號
     */
    private String receiverZip;

    /**
     * 訂單狀態
     */
    private String status;

    /**
     * 付款狀態
     */
    private String payStatus;

    /**
     * 物流狀態
     */
    private String shipStatus;

    /**
     * 付款方式
     */
    private String paymentMethod;

    /**
     * 第三方支付單號
     */
    private String paymentNo;

    /**
     * 付款時間
     */
    private Date paidTime;

    /**
     * 物流方式
     */
    private String shippingMethod;

    /**
     * 物流單號
     */
    private String shippingNo;

    /**
     * 出貨時間
     */
    private Date shippedTime;

    /**
     * 簽收時間
     */
    private Date receivedTime;

    /**
     * 買家備註
     */
    private String buyerRemark;

    /**
     * 賣家備註
     */
    private String sellerRemark;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消時間
     */
    private Date cancelTime;

    /**
     * 完成時間
     */
    private Date completeTime;

    /**
     * 訂單明細（關聯查詢）
     */
    private List<ShopOrderItem> orderItems;

    // ============ Enum Getter/Setter ============

    public OrderStatus getStatusEnum() {
        return status != null ? OrderStatus.fromCode(status) : null;
    }

    public void setStatusEnum(OrderStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }

    public PayStatus getPayStatusEnum() {
        return payStatus != null ? PayStatus.fromCode(payStatus) : null;
    }

    public void setPayStatusEnum(PayStatus payStatusEnum) {
        this.payStatus = payStatusEnum != null ? payStatusEnum.getCode() : null;
    }

    public ShipStatus getShipStatusEnum() {
        return shipStatus != null ? ShipStatus.fromCode(shipStatus) : null;
    }

    public void setShipStatusEnum(ShipStatus shipStatusEnum) {
        this.shipStatus = shipStatusEnum != null ? shipStatusEnum.getCode() : null;
    }

    public PaymentMethod getPaymentMethodEnum() {
        return paymentMethod != null ? PaymentMethod.fromCode(paymentMethod) : null;
    }

    public void setPaymentMethodEnum(PaymentMethod paymentMethodEnum) {
        this.paymentMethod = paymentMethodEnum != null ? paymentMethodEnum.getCode() : null;
    }

    public ShippingMethod getShippingMethodEnum() {
        return shippingMethod != null ? ShippingMethod.fromCode(shippingMethod) : null;
    }

    public void setShippingMethodEnum(ShippingMethod shippingMethodEnum) {
        this.shippingMethod = shippingMethodEnum != null ? shippingMethodEnum.getCode() : null;
    }
}
