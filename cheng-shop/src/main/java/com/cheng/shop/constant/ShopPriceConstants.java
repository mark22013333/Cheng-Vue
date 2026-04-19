package com.cheng.shop.constant;

import java.math.BigDecimal;

/**
 * 商品價格防呆常數
 * <p>
 * 與前端 {@code cheng-ui/src/views/shop/product/composables/priceSafeguard.js}
 * 保持同步，確保前後端防呆規則一致。
 *
 * @author cheng
 */
public final class ShopPriceConstants {

    private ShopPriceConstants() {
    }

    /**
     * 視為「可信任」的最低售價門檻。
     * 低於此值的庫存現價會被視為異常（試賣價、未定價、測試資料）。
     */
    public static final BigDecimal MIN_TRUST_PRICE = new BigDecimal("100");

    /**
     * 當觸發防呆時，由採購成本推算售價的加成比例。
     * 1.2 = 20% 毛利率，為零售業保守下限，確保上架後至少不虧本。
     */
    public static final BigDecimal MARKUP_RATIO = new BigDecimal("1.2");
}
