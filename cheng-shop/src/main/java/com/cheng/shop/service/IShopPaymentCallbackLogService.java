package com.cheng.shop.service;

import com.cheng.shop.domain.ShopPaymentCallbackLog;

/**
 * 金流回調紀錄 Service
 *
 * @author cheng
 */
public interface IShopPaymentCallbackLogService {

    void logCallback(ShopPaymentCallbackLog log);

    /**
     * 查詢金流回調紀錄列表
     */
    java.util.List<ShopPaymentCallbackLog> selectLogList(ShopPaymentCallbackLog log);

    /**
     * 查詢單筆金流回調紀錄
     */
    ShopPaymentCallbackLog selectLogById(Long logId);
}
