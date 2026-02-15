package com.cheng.shop.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopCart;
import com.cheng.shop.domain.ShopCvsStoreTemp;
import com.cheng.shop.domain.ShopGift;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.domain.ShopMemberAddress;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.domain.ShopOrderItem;
import com.cheng.shop.domain.dto.CheckoutSubmitRequest;
import com.cheng.shop.domain.vo.CheckoutPreviewVO;
import com.cheng.shop.domain.vo.CheckoutResultVO;
import com.cheng.shop.domain.vo.PriceResult;
import com.cheng.shop.enums.PaymentMethod;
import com.cheng.shop.enums.ShippingMethod;
import com.cheng.shop.logistics.IShopLogisticsService;
import com.cheng.shop.service.IShopCartService;
import com.cheng.shop.service.IShopCheckoutService;
import com.cheng.shop.service.IShopGiftService;
import com.cheng.shop.service.IShopMemberAddressService;
import com.cheng.shop.service.IShopMemberService;
import com.cheng.shop.service.IShopOrderService;
import com.cheng.shop.service.ShopPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
    private final IShopMemberService memberService;
    private final IShopOrderService orderService;
    private final IShopGiftService giftService;
    private final ShopPriceService priceService;
    private final ShopConfigService shopConfig;
    private final IShopLogisticsService logisticsService;

    /**
     * 離島地區關鍵字
     */
    private static final List<String> ISLAND_KEYWORDS = Arrays.asList(
            "金門", "馬祖", "澎湖", "蘭嶼", "綠島", "小琉球", "連江"
    );

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

        // 計算金額（含折扣）
        int totalQuantity = 0;
        BigDecimal productAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        for (ShopCart item : selectedItems) {
            totalQuantity += item.getQuantity();
            // 計算 SKU 折扣價
            BigDecimal finalUnitPrice = calculateCartItemPrice(item);
            BigDecimal itemTotal = finalUnitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            productAmount = productAmount.add(itemTotal);

            // 折扣 = 原價小計 - 折扣價小計
            BigDecimal originalTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal itemDiscount = originalTotal.subtract(itemTotal);
            if (itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.add(itemDiscount);
            }
        }

        // 計算運費（從系統設定讀取）
        BigDecimal shippingFee = calculateShippingFee(productAmount, address);

        // 應付金額
        BigDecimal payableAmount = productAmount.add(shippingFee).subtract(discountAmount);

        // 查詢可用禮物
        List<ShopGift> availableGifts = Collections.emptyList();
        if (shopConfig.isGiftEnabled()) {
            availableGifts = giftService.selectAvailableGifts(payableAmount);
        }

        return CheckoutPreviewVO.builder()
                .items(selectedItems)
                .address(address)
                .addressList(addressList)
                .totalQuantity(totalQuantity)
                .productAmount(productAmount)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .payableAmount(payableAmount)
                .availableGifts(availableGifts)
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

        // 解析物流方式
        ShippingMethod shippingMethod;
        try {
            shippingMethod = ShippingMethod.fromCode(request.getShippingMethod());
        } catch (Exception e) {
            throw new ServiceException("無效的物流方式");
        }

        // 超商取貨 + 貨到付款不允許
        if (shippingMethod.isCvs() && "COD".equals(request.getPaymentMethod())) {
            throw new ServiceException("超商取貨不支援貨到付款");
        }

        // 根據物流方式取得收貨資訊
        ShopMemberAddress address = null;
        ShopCvsStoreTemp cvsStore = null;

        if (shippingMethod.isCvs()) {
            // 超商取貨：從暫存表取得門市資訊
            if (request.getCvsStoreKey() == null || request.getCvsStoreKey().isBlank()) {
                throw new ServiceException("請選擇取貨門市");
            }
            cvsStore = logisticsService.getCvsStore(request.getCvsStoreKey(), memberId);
            if (cvsStore == null) {
                throw new ServiceException("門市資訊已過期，請重新選擇");
            }
        } else {
            // 宅配：需要收貨地址
            address = addressService.selectAddressById(request.getAddressId());
            if (address == null || !address.getMemberId().equals(memberId)) {
                throw new ServiceException("收貨地址無效");
            }
        }

        // 計算金額（含折扣）
        BigDecimal productAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<ShopOrderItem> orderItems = new ArrayList<>();

        for (ShopCart cartItem : selectedItems) {
            // 計算 SKU 折扣價
            BigDecimal finalUnitPrice = calculateCartItemPrice(cartItem);
            BigDecimal itemTotal = finalUnitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            productAmount = productAmount.add(itemTotal);

            // 累計折扣金額
            BigDecimal originalTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            BigDecimal itemDiscount = originalTotal.subtract(itemTotal);
            if (itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.add(itemDiscount);
            }

            // 轉換為訂單項目
            ShopOrderItem orderItem = new ShopOrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setSkuId(cartItem.getSkuId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setSkuName(cartItem.getSkuName());
            orderItem.setSkuImage(cartItem.getSkuImage() != null ? cartItem.getSkuImage() : cartItem.getProductImage());
            orderItem.setUnitPrice(finalUnitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        // 計算運費（使用物流服務）
        BigDecimal shippingFee = logisticsService.calculateShippingFee(
                request.getShippingMethod(), productAmount);

        // 總金額
        BigDecimal totalAmount = productAmount.add(shippingFee);

        // 建立訂單
        ShopOrder order = new ShopOrder();
        order.setMemberId(memberId);
        order.setProductAmount(productAmount);
        order.setShippingAmount(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setShippingMethod(request.getShippingMethod());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setBuyerRemark(request.getRemark());
        order.setOrderItems(orderItems);

        // 根據物流方式設定收貨資訊
        ShopMember member = memberService.selectMemberById(memberId);
        // 設定會員暱稱（用於後台顯示）
        if (member != null && member.getNickname() != null) {
            order.setMemberNickname(member.getNickname());
        }

        if (shippingMethod.isCvs()) {
            // 超商取貨：使用前端傳入的收件人資訊（物流出貨單必填）
            String receiverName = request.getReceiverName();
            String receiverMobile = request.getReceiverPhone();

            // 驗證收件人資訊
            if (receiverName == null || receiverName.isBlank()) {
                throw new ServiceException("請填寫取貨人姓名");
            }
            if (receiverMobile == null || !receiverMobile.matches("^09\\d{8}$")) {
                throw new ServiceException("請填寫正確的取貨人手機號碼");
            }

            order.setReceiverName(receiverName);
            order.setReceiverMobile(receiverMobile);
            order.setReceiverAddress(cvsStore.getStoreAddress());
            order.setCvsStoreId(cvsStore.getStoreId());
            order.setCvsStoreName(cvsStore.getStoreName());
            order.setCvsStoreAddress(cvsStore.getStoreAddress());
            order.setLogisticsSubType(cvsStore.getLogisticsSub());
        } else {
            // 宅配：使用收貨地址
            order.setReceiverName(address.getReceiverName());
            order.setReceiverMobile(address.getReceiverPhone());
            order.setReceiverAddress(address.getFullAddress());
            order.setReceiverZip(address.getPostalCode());
        }

        // 設定禮物（如果有選擇且功能啟用）
        if (request.getGiftId() != null && shopConfig.isGiftEnabled()) {
            ShopGift gift = giftService.selectGiftById(request.getGiftId());
            if (gift != null && "ENABLED".equals(gift.getStatus())
                    && gift.getStockQuantity() > 0
                    && gift.getThresholdAmount().compareTo(totalAmount) <= 0) {
                order.setGiftId(gift.getGiftId());
                order.setGiftName(gift.getName());
            }
        }

        // 建立訂單（會自動扣減庫存）
        orderService.createOrder(order, memberId);

        // 清除已結帳的購物車項目
        List<Long> cartIds = selectedItems.stream()
                .map(ShopCart::getCartId)
                .toList();
        cartService.deleteCartByIds(cartIds.toArray(new Long[0]));

        // 清除 CVS 門市暫存（如果有）
        if (request.getCvsStoreKey() != null && !request.getCvsStoreKey().isBlank()) {
            logisticsService.deleteCvsStore(request.getCvsStoreKey());
        }

        log.info("結帳成功，會員ID: {}, 訂單編號: {}, 物流方式: {}",
                memberId, order.getOrderNo(), request.getShippingMethod());

        // 判斷是否需要線上付款
        boolean needOnlinePayment = false;
        try {
            PaymentMethod pm = PaymentMethod.fromCode(request.getPaymentMethod());
            needOnlinePayment = pm.isOnlinePayment();
        } catch (Exception ignored) {
            // 無法解析付款方式時，預設不需要線上付款
        }

        return CheckoutResultVO.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .payableAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .needOnlinePayment(needOnlinePayment)
                .build();
    }

    /**
     * 計算購物車項目的最終單價（含折扣）
     */
    private BigDecimal calculateCartItemPrice(ShopCart cartItem) {
        // SKU 有獨立特惠價時優先使用
        if (cartItem.getSkuSalePrice() != null && cartItem.getSkuSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            boolean notExpired = cartItem.getProductSaleEndDate() == null
                    || cartItem.getProductSaleEndDate().after(new java.util.Date());
            if (notExpired) {
                return cartItem.getSkuSalePrice();
            }
        }

        // 商品有特惠價時，按比例套用到 SKU
        if (cartItem.getProductSalePrice() != null && cartItem.getProductSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            boolean notExpired = cartItem.getProductSaleEndDate() == null
                    || cartItem.getProductSaleEndDate().after(new java.util.Date());
            if (notExpired) {
                // 使用 ShopPriceService 計算（走商品特價路徑）
                PriceResult result = priceService.calculatePrice(
                        cartItem.getPrice(), cartItem.getProductSalePrice(), cartItem.getProductSaleEndDate());
                return result.getFinalPrice();
            }
        }

        // 否則使用全站折扣
        PriceResult result = priceService.calculatePrice(cartItem.getPrice(), null, null);
        return result.getFinalPrice();
    }

    /**
     * 計算運費（從系統設定讀取）
     */
    private BigDecimal calculateShippingFee(BigDecimal productAmount, ShopMemberAddress address) {
        BigDecimal freeThreshold = shopConfig.getShippingFreeThreshold();
        BigDecimal domesticFee = shopConfig.getShippingDomesticFee();
        BigDecimal islandFee = shopConfig.getShippingIslandFee();

        // 滿額免運
        if (freeThreshold.compareTo(BigDecimal.ZERO) > 0 && productAmount.compareTo(freeThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        // 離島檢查
        if (address != null && address.getFullAddress() != null) {
            String fullAddress = address.getFullAddress();
            for (String keyword : ISLAND_KEYWORDS) {
                if (fullAddress.contains(keyword)) {
                    return islandFee;
                }
            }
        }

        return domesticFee;
    }
}
