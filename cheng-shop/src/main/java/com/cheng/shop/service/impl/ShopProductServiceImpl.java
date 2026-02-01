package com.cheng.shop.service.impl;

import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.enums.ProductStatus;
import com.cheng.shop.mapper.ShopProductMapper;
import com.cheng.shop.mapper.ShopProductSkuMapper;
import com.cheng.shop.service.IShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public int increaseSalesCount(Long productId, int count) {
        return productMapper.increaseSalesCount(productId, count);
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
