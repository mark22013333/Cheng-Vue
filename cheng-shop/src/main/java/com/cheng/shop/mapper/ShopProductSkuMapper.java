package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SKU Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopProductSkuMapper {

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
     * 根據SKU編碼查詢
     *
     * @param skuCode SKU編碼
     * @return SKU
     */
    ShopProductSku selectSkuByCode(String skuCode);

    /**
     * 新增SKU
     *
     * @param sku SKU
     * @return 影響行數
     */
    int insertSku(ShopProductSku sku);

    /**
     * 批量新增SKU
     *
     * @param skuList SKU列表
     * @return 影響行數
     */
    int batchInsertSku(List<ShopProductSku> skuList);

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
     * 扣減庫存
     *
     * @param skuId    SKU ID
     * @param quantity 數量
     * @return 影響行數
     */
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    /**
     * 增加庫存
     *
     * @param skuId    SKU ID
     * @param quantity 數量
     * @return 影響行數
     */
    int increaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    /**
     * 檢查SKU編碼是否唯一
     *
     * @param skuCode SKU編碼
     * @param skuId   排除的SKU ID
     * @return 數量
     */
    int checkSkuCodeUnique(@Param("skuCode") String skuCode, @Param("skuId") Long skuId);
}
