package com.cheng.shop.service;

import com.cheng.shop.domain.ShopProduct;

import java.util.List;

/**
 * 商品 Service 介面
 *
 * @author cheng
 */
public interface IShopProductService {

    /**
     * 查詢商品列表
     *
     * @param product 查詢條件
     * @return 商品列表
     */
    List<ShopProduct> selectProductList(ShopProduct product);

    /**
     * 根據ID查詢商品（含SKU）
     *
     * @param productId 商品ID
     * @return 商品
     */
    ShopProduct selectProductById(Long productId);

    /**
     * 查詢熱門商品
     *
     * @param limit 數量限制
     * @return 商品列表
     */
    List<ShopProduct> selectHotProducts(int limit);

    /**
     * 查詢新品
     *
     * @param limit 數量限制
     * @return 商品列表
     */
    List<ShopProduct> selectNewProducts(int limit);

    /**
     * 查詢推薦商品
     *
     * @param limit 數量限制
     * @return 商品列表
     */
    List<ShopProduct> selectRecommendProducts(int limit);

    /**
     * 新增商品（含SKU）
     *
     * @param product 商品
     * @return 影響行數
     */
    int insertProduct(ShopProduct product);

    /**
     * 更新商品（含SKU）
     *
     * @param product 商品
     * @return 影響行數
     */
    int updateProduct(ShopProduct product);

    /**
     * 刪除商品
     *
     * @param productId 商品ID
     * @return 影響行數
     */
    int deleteProductById(Long productId);

    /**
     * 批量刪除商品
     *
     * @param productIds 商品ID陣列
     * @return 影響行數
     */
    int deleteProductByIds(Long[] productIds);

    /**
     * 上架商品
     *
     * @param productId 商品ID
     * @return 影響行數
     */
    int onSaleProduct(Long productId);

    /**
     * 下架商品
     *
     * @param productId 商品ID
     * @return 影響行數
     */
    int offSaleProduct(Long productId);

    /**
     * 增加瀏覽量
     *
     * @param productId 商品ID
     * @return 影響行數
     */
    int increaseViewCount(Long productId);
}
