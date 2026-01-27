package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品分類 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopCategoryMapper {

    /**
     * 查詢分類列表
     *
     * @param category 查詢條件
     * @return 分類列表
     */
    List<ShopCategory> selectCategoryList(ShopCategory category);

    /**
     * 根據ID查詢分類
     *
     * @param categoryId 分類ID
     * @return 分類
     */
    ShopCategory selectCategoryById(Long categoryId);

    /**
     * 根據父ID查詢子分類
     *
     * @param parentId 父分類ID
     * @return 子分類列表
     */
    List<ShopCategory> selectCategoryByParentId(Long parentId);

    /**
     * 新增分類
     *
     * @param category 分類
     * @return 影響行數
     */
    int insertCategory(ShopCategory category);

    /**
     * 更新分類
     *
     * @param category 分類
     * @return 影響行數
     */
    int updateCategory(ShopCategory category);

    /**
     * 刪除分類
     *
     * @param categoryId 分類ID
     * @return 影響行數
     */
    int deleteCategoryById(Long categoryId);

    /**
     * 查詢子分類數量
     *
     * @param parentId 父分類ID
     * @return 子分類數量
     */
    int countChildrenByParentId(Long parentId);

    /**
     * 查詢分類下的商品數量
     *
     * @param categoryId 分類ID
     * @return 商品數量
     */
    int countProductsByCategoryId(Long categoryId);
}
