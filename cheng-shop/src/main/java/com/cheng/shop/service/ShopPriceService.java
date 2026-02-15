package com.cheng.shop.service;

import com.cheng.shop.config.ShopConfigService;
import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.domain.vo.PriceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * 價格計算服務
 * <p>
 * 優先順序：商品特價 > 全站折扣 > 原價
 * <p>
 * 全站折扣模式：
 * - mode=0：無折扣
 * - mode=1：加價模式（顯示加價後的「原價」，實際以 price 賣出）
 * - mode=2：原價折扣模式（直接對 price 打折）
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopPriceService {

    private final ShopConfigService shopConfig;

    /**
     * 計算商品最終售價
     *
     * @param price       商品售價
     * @param salePrice   商品特惠價（可為 null）
     * @param saleEndDate 特價結束時間（可為 null，null 表示長期）
     * @return 價格計算結果
     */
    public PriceResult calculatePrice(BigDecimal price, BigDecimal salePrice, Date saleEndDate) {
        if (price == null) {
            return PriceResult.noDiscount(BigDecimal.ZERO);
        }

        // 1. 檢查商品特價是否有效
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0) {
            boolean notExpired = saleEndDate == null || saleEndDate.after(new Date());
            if (notExpired) {
                BigDecimal savings = price.subtract(salePrice);
                if (savings.compareTo(BigDecimal.ZERO) > 0) {
                    String label = "特價 -$" + savings.stripTrailingZeros().toPlainString();
                    return PriceResult.of(salePrice, label, price);
                }
                return PriceResult.noDiscount(price);
            }
        }

        // 2. 檢查全站折扣
        String mode = shopConfig.getDiscountMode();
        BigDecimal rate = shopConfig.getDiscountRate();

        if (mode != null && !mode.isBlank() && rate.compareTo(BigDecimal.ZERO) > 0) {

            if ("1".equals(mode)) {
                // 加價模式：計算加價後的「原價」，實際以 price 賣出
                // PHP: new_price = round(price / 100) * discount + price
                BigDecimal extra = price.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                        .multiply(rate);
                BigDecimal displayOriginalPrice = price.add(extra);
                if (extra.compareTo(BigDecimal.ZERO) > 0) {
                    String label = "折扣 -$" + extra.stripTrailingZeros().toPlainString();
                    return PriceResult.of(price, label, displayOriginalPrice);
                }
                return PriceResult.noDiscount(price);
            }

            if ("2".equals(mode)) {
                // 原價折扣模式：直接對 price 打折
                // PHP: new_price = price - (price/100) * discount
                BigDecimal discount = price.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        .multiply(rate);
                BigDecimal finalPrice = price.subtract(discount);
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    String label = "折扣 -$" + discount.stripTrailingZeros().toPlainString();
                    return PriceResult.of(finalPrice, label, price);
                }
                return PriceResult.noDiscount(price);
            }
        }

        // 3. 無折扣
        return PriceResult.noDiscount(price);
    }

    /**
     * 批量填充商品列表的最終售價和折扣標籤
     */
    public void enrichProductPrices(List<ShopProduct> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        for (ShopProduct product : products) {
            PriceResult result = calculatePrice(product.getPrice(), product.getSalePrice(), product.getSaleEndDate());
            product.setFinalPrice(result.getFinalPrice());
            product.setDiscountLabel(result.getDiscountLabel());
            product.setOriginalDisplayPrice(result.getOriginalDisplayPrice());
        }
    }

    /**
     * 計算 SKU 最終售價
     * <p>
     * SKU 有自己的 salePrice 時優先使用，否則跟隨商品的特價邏輯
     */
    public PriceResult calculateSkuPrice(ShopProductSku sku, ShopProduct product) {
        // SKU 有獨立特惠價
        if (sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            Date saleEndDate = product != null ? product.getSaleEndDate() : null;
            boolean notExpired = saleEndDate == null || saleEndDate.after(new Date());
            if (notExpired) {
                BigDecimal savings = sku.getPrice().subtract(sku.getSalePrice());
                String label = "特價 -$" + savings.stripTrailingZeros().toPlainString();
                return PriceResult.of(sku.getSalePrice(), label, sku.getPrice());
            }
        }

        // 否則使用全站折扣
        return calculatePrice(sku.getPrice(), null, null);
    }
}
