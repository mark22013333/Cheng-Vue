package com.cheng.shop.config;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商城系統設定 Key 列舉
 * <p>
 * 統一管理 sys_config 表中所有 shop.* 開頭的設定鍵，避免魔術字串散落各處
 * </p>
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ShopConfigKey implements CodedEnum<String> {

    // ==================== ECPay 金流設定 ====================

    /**
     * 綠界金流商店代號
     */
    ECPAY_MERCHANT_ID("shop.ecpay.merchant_id", "綠界金流商店代號", ""),

    /**
     * 綠界金流 HashKey
     */
    ECPAY_HASH_KEY("shop.ecpay.hash_key", "綠界金流 HashKey", ""),

    /**
     * 綠界金流 HashIV
     */
    ECPAY_HASH_IV("shop.ecpay.hash_iv", "綠界金流 HashIV", ""),

    /**
     * 綠界金流模式 (test/prod)
     */
    ECPAY_MODE("shop.ecpay.mode", "綠界金流模式", "test"),

    // ==================== ECPay 物流設定 ====================

    /**
     * 綠界物流商店代號
     */
    ECPAY_LOGISTICS_MERCHANT_ID("shop.ecpay.logistics.merchant_id", "綠界物流商店代號", ""),

    /**
     * 綠界物流 HashKey
     */
    ECPAY_LOGISTICS_HASH_KEY("shop.ecpay.logistics.hash_key", "綠界物流 HashKey", ""),

    /**
     * 綠界物流 HashIV
     */
    ECPAY_LOGISTICS_HASH_IV("shop.ecpay.logistics.hash_iv", "綠界物流 HashIV", ""),

    /**
     * 綠界物流模式 (test/prod)
     */
    ECPAY_LOGISTICS_MODE("shop.ecpay.logistics.mode", "綠界物流模式", "test"),

    /**
     * 綠界物流狀態回調 URL
     */
    ECPAY_LOGISTICS_SERVER_REPLY_URL("shop.ecpay.logistics.server_reply_url", "綠界物流狀態回調 URL", ""),

    // ==================== 物流設定 ====================

    /**
     * 啟用的物流方式（逗號分隔，如 HOME_DELIVERY,CVS_711,CVS_FAMILY）
     */
    LOGISTICS_METHODS("shop.logistics.methods", "啟用的物流方式", "HOME_DELIVERY"),

    /**
     * 宅配運費
     */
    LOGISTICS_HOME_DELIVERY_FEE("shop.logistics.home_delivery_fee", "宅配運費", "100"),

    /**
     * 宅配免運門檻（0 表示不免運）
     */
    LOGISTICS_HOME_DELIVERY_FREE_THRESHOLD("shop.logistics.home_delivery_free_threshold", "宅配免運門檻", "1000"),

    /**
     * 超商運費
     */
    LOGISTICS_CVS_FEE("shop.logistics.cvs_fee", "超商運費", "60"),

    /**
     * 超商免運門檻（0 表示不免運）
     */
    LOGISTICS_CVS_FREE_THRESHOLD("shop.logistics.cvs_free_threshold", "超商免運門檻", "0"),

    /**
     * 物流回調基礎 URL（用於電子地圖回調）
     */
    LOGISTICS_CALLBACK_BASE_URL("shop.logistics.callback_base_url", "物流回調基礎 URL", ""),

    /**
     * 超商門市暫存過期分鐘數
     */
    LOGISTICS_CVS_STORE_EXPIRE_MINUTES("shop.logistics.cvs_store_expire_minutes", "超商門市暫存過期分鐘", "30"),

    /**
     * 寄件人名稱
     */
    LOGISTICS_SENDER_NAME("shop.logistics.sender_name", "寄件人名稱", "CoolApps"),

    /**
     * 寄件人電話
     */
    LOGISTICS_SENDER_PHONE("shop.logistics.sender_phone", "寄件人電話", ""),

    /**
     * 寄件人郵遞區號
     */
    LOGISTICS_SENDER_ZIP("shop.logistics.sender_zip", "寄件人郵遞區號", ""),

    /**
     * 寄件人地址
     */
    LOGISTICS_SENDER_ADDRESS("shop.logistics.sender_address", "寄件人地址", ""),

    // ==================== 支付設定 ====================

    /**
     * 支付基礎 URL（伺服器端回調用，如 https://your-domain.com/prod-api）
     */
    PAYMENT_BASE_URL("shop.payment.base_url", "支付基礎 URL", ""),

    /**
     * 瀏覽器基礎 URL（前端跳轉用，如 https://your-domain.com）
     */
    PAYMENT_BROWSER_BASE_URL("shop.payment.browser_base_url", "瀏覽器基礎 URL", ""),

    /**
     * 前端 URL（支付完成跳轉，如 https://your-domain.com）
     */
    PAYMENT_FRONTEND_URL("shop.payment.frontend_url", "前端 URL", ""),

    /**
     * 啟用的付款方式（逗號分隔，如 COD,ECPAY）
     */
    PAYMENT_METHODS("shop.payment.methods", "啟用的付款方式", "COD,ECPAY"),

    // ==================== 運費設定（相容舊版） ====================

    /**
     * 免運門檻
     */
    SHIPPING_FREE_THRESHOLD("shop.shipping.free_threshold", "免運門檻", "1000"),

    /**
     * 本島運費
     */
    SHIPPING_DOMESTIC_FEE("shop.shipping.domestic_fee", "本島運費", "60"),

    /**
     * 離島運費
     */
    SHIPPING_ISLAND_FEE("shop.shipping.island_fee", "離島運費", "150"),

    // ==================== 功能開關 ====================

    /**
     * 禮物功能開關（1=啟用，0=停用）
     */
    GIFT_ENABLED("shop.gift.enabled", "禮物功能開關", "0"),

    /**
     * 全站折扣模式
     * <ul>
     *   <li>0：無折扣</li>
     *   <li>1：加價模式（原價 + 加價金額）</li>
     *   <li>2：折扣模式（原價 × 折扣比例）</li>
     * </ul>
     */
    DISCOUNT_MODE("shop.discount.mode", "全站折扣模式", "0"),

    /**
     * 全站折扣比例（百分比，如 85 表示 85 折）
     */
    DISCOUNT_RATE("shop.discount.rate", "全站折扣比例", "100");

    /**
     * 設定鍵（對應 sys_config.config_key）
     */
    private final String code;

    /**
     * 設定描述
     */
    private final String description;

    /**
     * 預設值
     */
    private final String defaultValue;

    // ==================== 靜態方法 ====================

    /**
     * 根據設定鍵取得對應的列舉
     *
     * @param configKey 設定鍵
     * @return 商城設定鍵列舉
     * @throws IllegalArgumentException 找不到對應的設定鍵時拋出
     */
    public static ShopConfigKey fromCode(String configKey) {
        return EnumUtils.fromCodeOrThrow(ShopConfigKey.class, configKey);
    }

    /**
     * 根據設定鍵取得對應的列舉（找不到返回 null）
     *
     * @param configKey 設定鍵
     * @return 商城設定鍵列舉，找不到返回 null
     */
    public static ShopConfigKey fromCodeOrNull(String configKey) {
        return EnumUtils.fromCode(ShopConfigKey.class, configKey);
    }

    /**
     * 驗證設定鍵是否有效
     *
     * @param configKey 設定鍵
     * @return true=有效，false=無效
     */
    public static boolean isValidConfigKey(String configKey) {
        return EnumUtils.isValidCode(ShopConfigKey.class, configKey);
    }

    // ==================== 便捷判斷方法 ====================

    /**
     * 是否為必填設定（預設值為空）
     */
    public boolean isRequired() {
        return defaultValue.isEmpty();
    }

    /**
     * 是否為 ECPay 金流相關設定
     */
    public boolean isEcpayPaymentConfig() {
        return code.startsWith("shop.ecpay.") && !code.startsWith("shop.ecpay.logistics.");
    }

    /**
     * 是否為 ECPay 物流相關設定
     */
    public boolean isEcpayLogisticsConfig() {
        return code.startsWith("shop.ecpay.logistics.");
    }

    /**
     * 是否為物流相關設定
     */
    public boolean isLogisticsConfig() {
        return code.startsWith("shop.logistics.");
    }

    /**
     * 是否為支付相關設定
     */
    public boolean isPaymentConfig() {
        return code.startsWith("shop.payment.");
    }

    /**
     * 是否為功能開關設定
     */
    public boolean isFeatureConfig() {
        return code.startsWith("shop.gift.") || code.startsWith("shop.discount.");
    }
}
