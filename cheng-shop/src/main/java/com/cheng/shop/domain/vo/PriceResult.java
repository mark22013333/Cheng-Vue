package com.cheng.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 價格計算結果
 *
 * @author cheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResult {

    /**
     * 最終售價
     */
    private BigDecimal finalPrice;

    /**
     * 折扣標籤（例如「特價」「95折」），null 表示無折扣
     */
    private String discountLabel;

    /**
     * 用於前端劃線顯示的原價，null 表示不顯示劃線價
     */
    private BigDecimal originalDisplayPrice;

    public static PriceResult of(BigDecimal finalPrice, String discountLabel, BigDecimal originalDisplayPrice) {
        return new PriceResult(finalPrice, discountLabel, originalDisplayPrice);
    }

    public static PriceResult noDiscount(BigDecimal price) {
        return new PriceResult(price, null, null);
    }
}
