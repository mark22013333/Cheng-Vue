package com.cheng.shop.service;

import com.cheng.shop.domain.dto.CheckoutSubmitRequest;
import com.cheng.shop.domain.vo.CheckoutPreviewVO;
import com.cheng.shop.domain.vo.CheckoutResultVO;

/**
 * 結帳 Service 介面
 *
 * @author cheng
 */
public interface IShopCheckoutService {

    /**
     * 結帳預覽
     *
     * @param memberId  會員 ID
     * @param addressId 收貨地址 ID（可選）
     * @return 結帳預覽資訊
     */
    CheckoutPreviewVO preview(Long memberId, Long addressId);

    /**
     * 提交訂單
     *
     * @param memberId 會員 ID
     * @param request  結帳請求
     * @return 訂單結果
     */
    CheckoutResultVO submit(Long memberId, CheckoutSubmitRequest request);
}
