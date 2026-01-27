package com.cheng.shop.service.impl;

import com.cheng.shop.domain.ShopCategory;
import com.cheng.shop.mapper.ShopCategoryMapper;
import com.cheng.shop.service.IShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分類 Service 實作
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopCategoryServiceImpl implements IShopCategoryService {

    private final ShopCategoryMapper categoryMapper;

    @Override
    public List<ShopCategory> selectCategoryList(ShopCategory category) {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public ShopCategory selectCategoryById(Long categoryId) {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public List<ShopCategory> selectCategoryByParentId(Long parentId) {
        return categoryMapper.selectCategoryByParentId(parentId);
    }

    @Override
    public List<ShopCategory> buildCategoryTree() {
        List<ShopCategory> allCategories = categoryMapper.selectCategoryList(new ShopCategory());
        return buildTree(allCategories, 0L);
    }

    /**
     * 遞迴建構樹狀結構
     */
    private List<ShopCategory> buildTree(List<ShopCategory> categories, Long parentId) {
        return categories.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .peek(c -> c.setChildren(buildTree(categories, c.getCategoryId())))
                .collect(Collectors.toList());
    }

    @Override
    public int insertCategory(ShopCategory category) {
        return categoryMapper.insertCategory(category);
    }

    @Override
    public int updateCategory(ShopCategory category) {
        return categoryMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryById(Long categoryId) {
        return categoryMapper.deleteCategoryById(categoryId);
    }

    @Override
    public String checkDeleteAllowed(Long categoryId) {
        // 檢查是否有子分類
        int childCount = categoryMapper.countChildrenByParentId(categoryId);
        if (childCount > 0) {
            return "該分類下還有子分類，無法刪除";
        }

        // 檢查是否有商品使用此分類
        int productCount = categoryMapper.countProductsByCategoryId(categoryId);
        if (productCount > 0) {
            return "該分類下還有商品，無法刪除";
        }

        return null;
    }
}
