package com.cheng.shop.service;

import com.cheng.shop.domain.ShopProductSku;

import java.util.List;

/**
 * 商品SKU Service 介面
 *
 * @author cheng
 */
public interface IShopProductSkuService {

    /**
     * 根據商品ID查詢SKU列表
     *
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ShopProductSku> selectSkuListByProductId(Long productId);

    /**
     * 根據SKU ID查詢
     *
     * @param skuId SKU ID
     * @return SKU
     */
    ShopProductSku selectSkuById(Long skuId);

    /**
     * 新增SKU
     *
     * @param sku SKU
     * @return 影響行數
     */
    int insertSku(ShopProductSku sku);

    /**
     * 更新SKU
     *
     * @param sku SKU
     * @return 影響行數
     */
    int updateSku(ShopProductSku sku);

    /**
     * 刪除SKU
     *
     * @param skuId SKU ID
     * @return 影響行數
     */
    int deleteSkuById(Long skuId);

    /**
     * 根據商品ID刪除所有SKU
     *
     * @param productId 商品ID
     * @return 影響行數
     */
    int deleteSkuByProductId(Long productId);

    /**
     * 批量儲存SKU（先刪後增）
     *
     * @param productId 商品ID
     * @param skuList   SKU列表
     * @return 影響行數
     */
    int batchSaveSku(Long productId, List<ShopProductSku> skuList);

    /**
     * 檢查SKU編碼是否唯一
     *
     * @param sku SKU
     * @return 是否唯一
     */
    boolean checkSkuCodeUnique(ShopProductSku sku);
}
