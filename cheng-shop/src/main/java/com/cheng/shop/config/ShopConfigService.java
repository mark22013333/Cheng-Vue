package com.cheng.shop.config;

import com.cheng.common.exception.ServiceException;
import com.cheng.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 商城設定服務
 * <p>
 * 提供型別安全的設定存取方法，封裝 {@link ISysConfigService}
 * </p>
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopConfigService {

    private final ISysConfigService sysConfigService;

    // ==================== 通用取值方法 ====================

    /**
     * 取得字串設定值
     *
     * @param key 設定 key
     * @return 設定值，若未設定則返回預設值
     */
    public String getString(ShopConfigKey key) {
        String value = sysConfigService.selectConfigByKey(key.getCode());
        if (value == null || value.isBlank()) {
            return key.getDefaultValue();
        }
        return value.trim();
    }

    /**
     * 取得字串設定值（必填，未設定時拋出異常）
     *
     * @param key 設定 key
     * @return 設定值
     * @throws ServiceException 未設定時拋出
     */
    public String getRequiredString(ShopConfigKey key) {
        String value = getString(key);
        if (value.isBlank()) {
            throw new ServiceException("缺少系統設定：" + key.getCode() +
                    "（" + key.getDescription() + "）");
        }
        return value;
    }

    /**
     * 取得 BigDecimal 設定值
     *
     * @param key 設定 key
     * @return 設定值，解析失敗返回預設值
     */
    public BigDecimal getBigDecimal(ShopConfigKey key) {
        String value = getString(key);
        if (value.isBlank()) {
            value = key.getDefaultValue();
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("設定 {} 值無效：{}，使用預設值：{}",
                    key.getCode(), value, key.getDefaultValue());
            return new BigDecimal(key.getDefaultValue());
        }
    }

    /**
     * 取得整數設定值
     *
     * @param key 設定 key
     * @return 設定值，解析失敗返回預設值
     */
    public int getInt(ShopConfigKey key) {
        String value = getString(key);
        if (value.isBlank()) {
            value = key.getDefaultValue();
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("設定 {} 值無效：{}，使用預設值：{}",
                    key.getCode(), value, key.getDefaultValue());
            return Integer.parseInt(key.getDefaultValue());
        }
    }

    /**
     * 取得布林設定值
     * <p>
     * 支援格式：1/0、true/false、yes/no
     * </p>
     *
     * @param key 設定 key
     * @return 設定值
     */
    public boolean getBoolean(ShopConfigKey key) {
        String value = getString(key);
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    /**
     * 取得 Optional 包裝的設定值
     *
     * @param key 設定 key
     * @return Optional 包裝的值，未設定或為空時返回 empty
     */
    public Optional<String> getOptional(ShopConfigKey key) {
        String value = sysConfigService.selectConfigByKey(key.getCode());
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }

    // ==================== 功能開關便捷方法 ====================

    /**
     * 禮物功能是否啟用
     */
    public boolean isGiftEnabled() {
        return getBoolean(ShopConfigKey.GIFT_ENABLED);
    }

    /**
     * 取得啟用的物流方式
     */
    public String getLogisticsMethods() {
        return getString(ShopConfigKey.LOGISTICS_METHODS);
    }

    /**
     * 取得啟用的付款方式
     */
    public String getPaymentMethods() {
        return getString(ShopConfigKey.PAYMENT_METHODS);
    }

    // ==================== ECPay 金流便捷方法 ====================

    /**
     * 取得 ECPay 金流商店代號（必填）
     */
    public String getEcpayMerchantId() {
        return getRequiredString(ShopConfigKey.ECPAY_MERCHANT_ID);
    }

    /**
     * 取得 ECPay 金流 HashKey（必填）
     */
    public String getEcpayHashKey() {
        return getRequiredString(ShopConfigKey.ECPAY_HASH_KEY);
    }

    /**
     * 取得 ECPay 金流 HashIV（必填）
     */
    public String getEcpayHashIv() {
        return getRequiredString(ShopConfigKey.ECPAY_HASH_IV);
    }

    /**
     * 取得 ECPay 金流模式
     */
    public String getEcpayMode() {
        return getString(ShopConfigKey.ECPAY_MODE);
    }

    /**
     * 是否為 ECPay 金流正式環境
     */
    public boolean isEcpayProdMode() {
        return "prod".equals(getEcpayMode());
    }

    // ==================== ECPay 物流便捷方法 ====================

    /**
     * 取得 ECPay 物流商店代號（必填）
     */
    public String getEcpayLogisticsMerchantId() {
        return getRequiredString(ShopConfigKey.ECPAY_LOGISTICS_MERCHANT_ID);
    }

    /**
     * 取得 ECPay 物流 HashKey（必填）
     */
    public String getEcpayLogisticsHashKey() {
        return getRequiredString(ShopConfigKey.ECPAY_LOGISTICS_HASH_KEY);
    }

    /**
     * 取得 ECPay 物流 HashIV（必填）
     */
    public String getEcpayLogisticsHashIv() {
        return getRequiredString(ShopConfigKey.ECPAY_LOGISTICS_HASH_IV);
    }

    /**
     * 取得 ECPay 物流模式
     */
    public String getEcpayLogisticsMode() {
        return getString(ShopConfigKey.ECPAY_LOGISTICS_MODE);
    }

    /**
     * 是否為 ECPay 物流正式環境
     */
    public boolean isEcpayLogisticsProdMode() {
        return "prod".equals(getEcpayLogisticsMode());
    }

    /**
     * 取得 ECPay 物流狀態回調 URL（必填）
     */
    public String getEcpayLogisticsServerReplyUrl() {
        return getRequiredString(ShopConfigKey.ECPAY_LOGISTICS_SERVER_REPLY_URL);
    }

    // ==================== 物流運費便捷方法 ====================

    /**
     * 取得宅配運費
     */
    public BigDecimal getHomeDeliveryFee() {
        return getBigDecimal(ShopConfigKey.LOGISTICS_HOME_DELIVERY_FEE);
    }

    /**
     * 取得宅配免運門檻
     */
    public BigDecimal getHomeDeliveryFreeThreshold() {
        return getBigDecimal(ShopConfigKey.LOGISTICS_HOME_DELIVERY_FREE_THRESHOLD);
    }

    /**
     * 取得超商運費
     */
    public BigDecimal getCvsFee() {
        return getBigDecimal(ShopConfigKey.LOGISTICS_CVS_FEE);
    }

    /**
     * 取得超商免運門檻
     */
    public BigDecimal getCvsFreeThreshold() {
        return getBigDecimal(ShopConfigKey.LOGISTICS_CVS_FREE_THRESHOLD);
    }

    /**
     * 取得超商門市暫存過期分鐘數
     */
    public int getCvsStoreExpireMinutes() {
        return getInt(ShopConfigKey.LOGISTICS_CVS_STORE_EXPIRE_MINUTES);
    }

    /**
     * 取得物流回調基礎 URL（必填）
     */
    public String getLogisticsCallbackBaseUrl() {
        return getRequiredString(ShopConfigKey.LOGISTICS_CALLBACK_BASE_URL);
    }

    /**
     * 取得寄件人名稱
     */
    public String getSenderName() {
        return getString(ShopConfigKey.LOGISTICS_SENDER_NAME);
    }

    /**
     * 取得寄件人電話
     */
    public String getSenderPhone() {
        return getString(ShopConfigKey.LOGISTICS_SENDER_PHONE);
    }

    /**
     * 取得寄件人郵遞區號
     */
    public String getSenderZip() {
        return getString(ShopConfigKey.LOGISTICS_SENDER_ZIP);
    }

    /**
     * 取得寄件人地址
     */
    public String getSenderAddress() {
        return getString(ShopConfigKey.LOGISTICS_SENDER_ADDRESS);
    }

    // ==================== 支付相關便捷方法 ====================

    /**
     * 取得支付基礎 URL
     */
    public String getPaymentBaseUrl() {
        return getString(ShopConfigKey.PAYMENT_BASE_URL);
    }

    /**
     * 取得瀏覽器基礎 URL
     */
    public String getPaymentBrowserBaseUrl() {
        return getString(ShopConfigKey.PAYMENT_BROWSER_BASE_URL);
    }

    /**
     * 取得前端 URL
     */
    public String getPaymentFrontendUrl() {
        return getString(ShopConfigKey.PAYMENT_FRONTEND_URL);
    }

    // ==================== 運費相關便捷方法（相容舊版） ====================

    /**
     * 取得免運門檻
     */
    public BigDecimal getShippingFreeThreshold() {
        return getBigDecimal(ShopConfigKey.SHIPPING_FREE_THRESHOLD);
    }

    /**
     * 取得本島運費
     */
    public BigDecimal getShippingDomesticFee() {
        return getBigDecimal(ShopConfigKey.SHIPPING_DOMESTIC_FEE);
    }

    /**
     * 取得離島運費
     */
    public BigDecimal getShippingIslandFee() {
        return getBigDecimal(ShopConfigKey.SHIPPING_ISLAND_FEE);
    }

    // ==================== 折扣相關便捷方法 ====================

    /**
     * 取得全站折扣模式
     */
    public String getDiscountMode() {
        return getString(ShopConfigKey.DISCOUNT_MODE);
    }

    /**
     * 取得全站折扣比例
     */
    public BigDecimal getDiscountRate() {
        return getBigDecimal(ShopConfigKey.DISCOUNT_RATE);
    }
}
