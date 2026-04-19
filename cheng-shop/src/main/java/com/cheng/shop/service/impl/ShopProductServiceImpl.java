package com.cheng.shop.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.shop.constant.ShopPriceConstants;
import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.enums.ProductStatus;
import com.cheng.shop.mapper.ShopProductMapper;
import com.cheng.shop.mapper.ShopProductSkuMapper;
import com.cheng.shop.service.IShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 商品 Service 實作
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopProductServiceImpl implements IShopProductService {

    private final ShopProductMapper productMapper;
    private final ShopProductSkuMapper skuMapper;

    @Override
    public List<ShopProduct> selectProductList(ShopProduct product) {
        return productMapper.selectProductList(product);
    }

    @Override
    public ShopProduct selectProductById(Long productId) {
        ShopProduct product = productMapper.selectProductById(productId);
        if (product != null) {
            // 載入 SKU 列表
            List<ShopProductSku> skuList = skuMapper.selectSkuListByProductId(productId);
            product.setSkuList(skuList);
        }
        return product;
    }

    @Override
    public List<ShopProduct> selectHotProducts(int limit) {
        return productMapper.selectHotProducts(limit);
    }

    @Override
    public List<ShopProduct> selectNewProducts(int limit) {
        return productMapper.selectNewProducts(limit);
    }

    @Override
    public List<ShopProduct> selectRecommendProducts(int limit) {
        return productMapper.selectRecommendProducts(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProduct(ShopProduct product) {
        // 處理 sliderImages JSON 欄位
        normalizeSliderImages(product);

        // SKU 價格防呆驗證 + 自動補值（cost > price 直接拒絕；price 為 null 以成本推算）
        validateAndEnrichSkuPricing(product.getSkuList());

        // 從 SKU 同步最低價格到商品主表
        syncPriceFromSkus(product);

        // 新增商品
        int rows = productMapper.insertProduct(product);

        // 新增 SKU
        if (product.getSkuList() != null && !product.getSkuList().isEmpty()) {
            for (ShopProductSku sku : product.getSkuList()) {
                sku.setProductId(product.getProductId());
                sku.setCreateBy(product.getCreateBy());
            }
            skuMapper.batchInsertSku(product.getSkuList());
        }

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProduct(ShopProduct product) {
        // 處理 sliderImages JSON 欄位
        normalizeSliderImages(product);

        // SKU 價格防呆驗證 + 自動補值
        validateAndEnrichSkuPricing(product.getSkuList());

        // 從 SKU 同步最低價格到商品主表
        syncPriceFromSkus(product);

        // 更新商品
        int rows = productMapper.updateProduct(product);

        // 刪除原有 SKU
        skuMapper.deleteSkuByProductId(product.getProductId());

        // 新增新的 SKU
        if (product.getSkuList() != null && !product.getSkuList().isEmpty()) {
            for (ShopProductSku sku : product.getSkuList()) {
                sku.setProductId(product.getProductId());
                sku.setCreateBy(product.getUpdateBy());
            }
            skuMapper.batchInsertSku(product.getSkuList());
        }

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductById(Long productId) {
        // 刪除 SKU
        skuMapper.deleteSkuByProductId(productId);
        // 刪除商品
        return productMapper.deleteProductById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductByIds(Long[] productIds) {
        for (Long productId : productIds) {
            skuMapper.deleteSkuByProductId(productId);
        }
        return productMapper.deleteProductByIds(productIds);
    }

    @Override
    public int onSaleProduct(Long productId) {
        ShopProduct product = new ShopProduct();
        product.setProductId(productId);
        product.setStatus(ProductStatus.ON_SALE.getCode());
        return productMapper.updateProduct(product);
    }

    @Override
    public int offSaleProduct(Long productId) {
        ShopProduct product = new ShopProduct();
        product.setProductId(productId);
        product.setStatus(ProductStatus.OFF_SALE.getCode());
        return productMapper.updateProduct(product);
    }

    @Override
    public int increaseViewCount(Long productId) {
        return productMapper.increaseViewCount(productId);
    }

    @Override
    public int updateProductFlag(Long[] productIds, String flagName, boolean flagValue) {
        return productMapper.updateProductFlag(productIds, flagName, flagValue);
    }

    @Override
    public int batchUpdateStatus(Long[] productIds, String status) {
        return productMapper.batchUpdateStatus(productIds, status);
    }

    @Override
    public int increaseSalesCount(Long productId, int count) {
        return productMapper.increaseSalesCount(productId, count);
    }

    /**
     * SKU 價格防呆驗證 + 自動補值
     * <p>
     * 規則：
     * <ul>
     *   <li>{@code costPrice > price} → 直接拋 {@link ServiceException}（硬阻擋，不合理的虧本商品）</li>
     *   <li>{@code price == null}：若 {@code costPrice > 0}，自動以 {@code round(costPrice × MARKUP_RATIO)} 推算售價；否則留空由前端驗證處理</li>
     *   <li>{@code price == 0}：不覆寫，由前端二次確認對話框把關（允許 0 元商品但需使用者明確確認）</li>
     * </ul>
     * 此方法為後端最後一道防線，即使前端繞過也能確保資料合理。
     *
     * @see <a href="openspec/changes/sku-price-sanity-guard/specs/sku-price-sanity-guard/spec.md">SKU Price Sanity Guard Spec</a>
     * @see ShopPriceConstants#MARKUP_RATIO
     */
    private void validateAndEnrichSkuPricing(List<ShopProductSku> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            return;
        }

        for (int i = 0; i < skuList.size(); i++) {
            ShopProductSku sku = skuList.get(i);
            BigDecimal price = sku.getPrice();
            BigDecimal cost = sku.getCostPrice();

            // price 為 null 且 cost > 0：以成本推算售價
            if (price == null && cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal derived = cost.multiply(ShopPriceConstants.MARKUP_RATIO)
                        .setScale(0, RoundingMode.HALF_UP);
                sku.setPrice(derived);
                price = derived;
            }

            // 成本價高於售價 → 硬阻擋
            if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0
                    && price != null && cost.compareTo(price) > 0) {
                String skuLabel = sku.getSkuName() != null && !sku.getSkuName().isBlank()
                        ? sku.getSkuName()
                        : "第 " + (i + 1) + " 個規格";
                throw new ServiceException(
                        String.format("規格「%s」的成本價 $%s 高於售價 $%s，請先調整",
                                skuLabel, cost.toPlainString(), price.toPlainString()));
            }
        }
    }

    /**
     * 從 SKU 列表同步價格到商品主表
     * <p>
     * 確保 product.price / originalPrice / salePrice / saleEndDate 與 SKU 一致，
     * 避免列表頁和詳情頁因資料不同步而顯示不同價格。
     * <p>
     * salePrice 取未過期且有效的 SKU 特惠價最小值；
     * saleEndDate 取有效 SKU 特惠價的最大到期時間。
     */
    private void syncPriceFromSkus(ShopProduct product) {
        List<ShopProductSku> skuList = product.getSkuList();
        if (skuList == null || skuList.isEmpty()) {
            return;
        }

        BigDecimal minPrice = skuList.stream()
                .map(ShopProductSku::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);

        BigDecimal minOriginalPrice = skuList.stream()
                .map(ShopProductSku::getOriginalPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);

        // 推導 salePrice：取未過期的 SKU 特惠價最小值
        Date now = new Date();
        BigDecimal minSalePrice = skuList.stream()
                .filter(sku -> sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
                .filter(sku -> sku.getSaleEndDate() == null || sku.getSaleEndDate().after(now))
                .map(ShopProductSku::getSalePrice)
                .min(BigDecimal::compareTo)
                .orElse(null);

        // 推導 saleEndDate：取有效 SKU 特惠價的最大到期時間
        Date maxSaleEndDate = skuList.stream()
                .filter(sku -> sku.getSalePrice() != null && sku.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
                .map(ShopProductSku::getSaleEndDate)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);

        if (minPrice != null) {
            product.setPrice(minPrice);
        }
        if (minOriginalPrice != null) {
            product.setOriginalPrice(minOriginalPrice);
        }
        product.setSalePrice(minSalePrice);
        product.setSaleEndDate(maxSaleEndDate);
    }

    /**
     * 處理 sliderImages JSON 欄位
     * 支援逗號分隔字串或 JSON 陣列格式，統一轉換為 JSON 陣列
     */
    private void normalizeSliderImages(ShopProduct product) {
        String sliderImages = product.getSliderImages();
        if (sliderImages == null || sliderImages.isBlank()) {
            product.setSliderImages(null);
            return;
        }

        // 去除前後空白
        sliderImages = sliderImages.trim();

        // 檢查是否為空陣列
        if ("[]".equals(sliderImages)) {
            product.setSliderImages(null);
            return;
        }

        // 檢查是否已經是 JSON 陣列格式
        if (sliderImages.startsWith("[") && sliderImages.endsWith("]")) {
            product.setSliderImages(sliderImages);
            return;
        }

        // 如果是逗號分隔格式，轉換為 JSON 陣列
        String[] images = sliderImages.split(",");
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String image : images) {
            String trimmed = image.trim();
            if (!trimmed.isEmpty()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append("\"").append(trimmed).append("\"");
                first = false;
            }
        }
        sb.append("]");

        String result = sb.toString();
        if ("[]".equals(result)) {
            product.setSliderImages(null);
        } else {
            product.setSliderImages(result);
        }
    }
}
