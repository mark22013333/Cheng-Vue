package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopCategory;
import com.cheng.shop.service.IShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分類 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/category")
@RequiredArgsConstructor
public class ShopCategoryController extends BaseController {

    private final IShopCategoryService categoryService;

    /**
     * 查詢分類列表
     */
    @PreAuthorize("@ss.hasPermi('shop:category:list')")
    @GetMapping("/list")
    public AjaxResult list(ShopCategory category) {
        List<ShopCategory> list = categoryService.selectCategoryList(category);
        return success(list);
    }

    /**
     * 查詢分類樹狀結構
     */
    @PreAuthorize("@ss.hasPermi('shop:category:list')")
    @GetMapping("/tree")
    public AjaxResult tree() {
        List<ShopCategory> tree = categoryService.buildCategoryTree();
        return success(tree);
    }

    /**
     * 查詢分類詳情
     */
    @PreAuthorize("@ss.hasPermi('shop:category:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId) {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 查詢子分類
     */
    @PreAuthorize("@ss.hasPermi('shop:category:list')")
    @GetMapping("/children/{parentId}")
    public AjaxResult children(@PathVariable Long parentId) {
        return success(categoryService.selectCategoryByParentId(parentId));
    }

    /**
     * 新增分類
     */
    @PreAuthorize("@ss.hasPermi('shop:category:add')")
    @Log(title = "商品分類", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopCategory category) {
        category.setCreateBy(SecurityUtils.getUsername());
        int result = categoryService.insertCategory(category);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("categoryName", category.getName());
        return ajaxResult;
    }

    /**
     * 修改分類
     */
    @PreAuthorize("@ss.hasPermi('shop:category:edit')")
    @Log(title = "商品分類", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopCategory category) {
        category.setUpdateBy(SecurityUtils.getUsername());
        int result = categoryService.updateCategory(category);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("categoryName", category.getName());
        return ajaxResult;
    }

    /**
     * 刪除分類
     */
    @PreAuthorize("@ss.hasPermi('shop:category:remove')")
    @Log(title = "商品分類", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId) {
        // 檢查是否可以刪除
        String errorMsg = categoryService.checkDeleteAllowed(categoryId);
        if (errorMsg != null) {
            return error(errorMsg);
        }

        ShopCategory category = categoryService.selectCategoryById(categoryId);
        int result = categoryService.deleteCategoryById(categoryId);
        AjaxResult ajaxResult = toAjax(result);
        if (category != null) {
            ajaxResult.put("categoryName", category.getName());
        }
        return ajaxResult;
    }
}
