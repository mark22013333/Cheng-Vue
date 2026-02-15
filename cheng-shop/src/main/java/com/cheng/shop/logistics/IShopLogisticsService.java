package com.cheng.shop.logistics;

import com.cheng.shop.domain.ShopCvsStoreTemp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 物流服務介面
 *
 * @author cheng
 */
public interface IShopLogisticsService {

    /**
     * 取得可用物流方式列表
     *
     * @param productAmount 商品金額（用於計算運費）
     * @return 物流方式列表
     */
    List<ShippingMethodVO> getAvailableMethods(BigDecimal productAmount);

    /**
     * 產生電子地圖表單 HTML
     *
     * @param shippingMethod 物流方式（CVS_711/CVS_FAMILY/CVS_HILIFE）
     * @param storeKey       前端識別 key
     * @param memberId       會員 ID
     * @return 自動提交表單 HTML
     */
    String generateMapFormHtml(String shippingMethod, String storeKey, Long memberId);

    /**
     * 儲存超商門市選取結果（電子地圖回調）
     *
     * @param params 綠界回調參數
     */
    void saveCvsStore(Map<String, String> params);

    /**
     * 查詢超商門市暫存資訊
     *
     * @param storeKey 前端識別 key
     * @param memberId 會員 ID
     * @return 門市暫存資訊
     */
    ShopCvsStoreTemp getCvsStore(String storeKey, Long memberId);

    /**
     * 刪除超商門市暫存記錄
     *
     * @param storeKey 前端識別 key
     */
    void deleteCvsStore(String storeKey);

    /**
     * 清除過期的門市暫存記錄
     *
     * @return 清除數量
     */
    int cleanExpiredStores();

    /**
     * 計算運費
     *
     * @param shippingMethod 物流方式
     * @param productAmount  商品金額
     * @return 運費
     */
    BigDecimal calculateShippingFee(String shippingMethod, BigDecimal productAmount);

    /**
     * 處理綠界物流狀態回調
     *
     * @param params 綠界回調參數
     */
    void handleStatusCallback(Map<String, String> params);
}
