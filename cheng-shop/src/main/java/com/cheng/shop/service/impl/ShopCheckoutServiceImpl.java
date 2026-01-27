package com.cheng.shop.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.domain.ShopCart;
import com.cheng.shop.domain.ShopMemberAddress;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopOrderItem;
import com.cheng.shop.domain.dto.CheckoutSubmitRequest;
import com.cheng.shop.domain.vo.CheckoutPreviewVO;
import com.cheng.shop.domain.vo.CheckoutResultVO;
import com.cheng.shop.service.IShopCartService;
import com.cheng.shop.service.IShopCheckoutService;
import com.cheng.shop.service.IShopMemberAddressService;
import com.cheng.shop.service.IShopOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 結帳 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopCheckoutServiceImpl implements IShopCheckoutService {

    private final IShopCartService cartService;
    private final IShopMemberAddressService addressService;
    private final IShopOrderService orderService;

    /**
     * 運費門檻（滿額免運）
     */
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("1000");

    /**
     * 基本運費
     */
    private static final BigDecimal BASE_SHIPPING_FEE = new BigDecimal("60");

    @Override
    public CheckoutPreviewVO preview(Long memberId, Long addressId) {
        // 取得已選中的購物車項目
        List<ShopCart> selectedItems = cartService.selectSelectedCartsByMemberId(memberId);
        if (selectedItems == null || selectedItems.isEmpty()) {
            throw new ServiceException("請先選擇要結帳的商品");
        }

        // 取得地址列表
        List<ShopMemberAddress> addressList = addressService.selectAddressListByMemberId(memberId);

        // 決定使用的地址
        ShopMemberAddress address = null;
        if (addressId != null) {
            address = addressService.selectAddressById(addressId);
        }
        if (address == null) {
            // 使用預設地址
            address = addressService.selectDefaultAddress(memberId);
        }
        if (address == null && !addressList.isEmpty()) {
            // 使用第一個地址
            address = addressList.get(0);
        }

        // 計算金額
        int totalQuantity = 0;
        BigDecimal productAmount = BigDecimal.ZERO;

        for (ShopCart item : selectedItems) {
            totalQuantity += item.getQuantity();
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            productAmount = productAmount.add(itemTotal);
        }

        // 計算運費（滿 1000 免運）
        BigDecimal shippingFee = productAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : BASE_SHIPPING_FEE;

        // 折扣（目前無優惠券功能）
        BigDecimal discountAmount = BigDecimal.ZERO;

        // 應付金額
        BigDecimal payableAmount = productAmount.add(shippingFee).subtract(discountAmount);

        return CheckoutPreviewVO.builder()
                .items(selectedItems)
                .address(address)
                .addressList(addressList)
                .totalQuantity(totalQuantity)
                .productAmount(productAmount)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .payableAmount(payableAmount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResultVO submit(Long memberId, CheckoutSubmitRequest request) {
        // 取得已選中的購物車項目
        List<ShopCart> selectedItems = cartService.selectSelectedCartsByMemberId(memberId);
        if (selectedItems == null || selectedItems.isEmpty()) {
            throw new ServiceException("請先選擇要結帳的商品");
        }

        // 取得收貨地址
        ShopMemberAddress address = addressService.selectAddressById(request.getAddressId());
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new ServiceException("收貨地址無效");
        }

        // 計算金額
        BigDecimal productAmount = BigDecimal.ZERO;
        List<ShopOrderItem> orderItems = new ArrayList<>();

        for (ShopCart cartItem : selectedItems) {
            BigDecimal itemTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            productAmount = productAmount.add(itemTotal);

            // 轉換為訂單項目
            ShopOrderItem orderItem = new ShopOrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setSkuId(cartItem.getSkuId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setSkuName(cartItem.getSkuName());
            orderItem.setSkuImage(cartItem.getSkuImage() != null ? cartItem.getSkuImage() : cartItem.getProductImage());
            orderItem.setUnitPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        // 計算運費
        BigDecimal shippingFee = productAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : BASE_SHIPPING_FEE;

        // 總金額
        BigDecimal totalAmount = productAmount.add(shippingFee);

        // 建立訂單
        ShopOrder order = new ShopOrder();
        order.setMemberId(memberId);
        order.setProductAmount(productAmount);
        order.setShippingAmount(shippingFee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverMobile(address.getReceiverPhone());
        order.setReceiverAddress(address.getFullAddress());
        order.setReceiverZip(address.getPostalCode());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setBuyerRemark(request.getRemark());
        order.setOrderItems(orderItems);

        // 建立訂單（會自動扣減庫存）
        orderService.createOrder(order);

        // 清除已結帳的購物車項目
        List<Long> cartIds = selectedItems.stream()
                .map(ShopCart::getCartId)
                .toList();
        cartService.deleteCartByIds(cartIds.toArray(new Long[0]));

        log.info("結帳成功，會員ID: {}, 訂單編號: {}", memberId, order.getOrderNo());

        return CheckoutResultVO.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .payableAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .build();
    }
}
