package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopProductMapper {

    /**
     * 查詢商品列表
     *
     * @param product 查詢條件
     * @return 商品列表
     */
    List<ShopProduct> selectProductList(ShopProduct product);

    /**
     * 根據ID查詢商品
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
    List<ShopProduct> selectHotProducts(@Param("limit") int limit);

    /**
     * 查詢新品
     *
     * @param limit 數量限制
     * @return 商品列表
     */
    List<ShopProduct> selectNewProducts(@Param("limit") int limit);

    /**
     * 查詢推薦商品
     *
     * @param limit 數量限制
     * @return 商品列表
     */
    List<ShopProduct> selectRecommendProducts(@Param("limit") int limit);

    /**
     * 新增商品
     *
     * @param product 商品
     * @return 影響行數
     */
    int insertProduct(ShopProduct product);

    /**
     * 更新商品
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
     * 增加瀏覽量
     *
     * @param productId 商品ID
     * @return 影響行數
     */
    int increaseViewCount(Long productId);

    /**
     * 增加銷量
     *
     * @param productId 商品ID
     * @param count     數量
     * @return 影響行數
     */
    int increaseSalesCount(@Param("productId") Long productId, @Param("count") int count);

    /**
     * 批量更新商品標記（熱門/新品/推薦）
     *
     * @param productIds 商品ID陣列
     * @param flagName   標記欄位名（is_hot / is_new / is_recommend）
     * @param flagValue  標記值（true / false）
     * @return 影響行數
     */
    int updateProductFlag(@Param("productIds") Long[] productIds, @Param("flagName") String flagName, @Param("flagValue") boolean flagValue);
}
