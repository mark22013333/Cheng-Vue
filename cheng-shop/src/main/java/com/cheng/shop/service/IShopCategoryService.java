package com.cheng.shop.service;

import com.cheng.shop.domain.ShopCategory;

import java.util.List;

/**
 * 商品分類 Service 介面
 *
 * @author cheng
 */
public interface IShopCategoryService {

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
     * 查詢子分類列表
     *
     * @param parentId 父分類ID
     * @return 子分類列表
     */
    List<ShopCategory> selectCategoryByParentId(Long parentId);

    /**
     * 建構分類樹狀結構
     *
     * @return 分類樹
     */
    List<ShopCategory> buildCategoryTree();

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
     * 檢查分類是否可以刪除
     *
     * @param categoryId 分類ID
     * @return 錯誤訊息，null表示可以刪除
     */
    String checkDeleteAllowed(Long categoryId);
}
