package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.shop.domain.ShopBanner;
import com.cheng.shop.domain.ShopCategory;
import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.service.IShopBannerService;
import com.cheng.shop.service.IShopCategoryService;
import com.cheng.shop.service.IShopProductService;
import com.cheng.shop.service.IShopProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 商城前台 Controller（消費者端）
 *
 * @author cheng
 */
@Anonymous
@RestController
@RequestMapping("/shop/front")
@RequiredArgsConstructor
public class ShopFrontController extends BaseController {

    private final IShopProductService productService;
    private final IShopProductSkuService skuService;
    private final IShopCategoryService categoryService;
    private final IShopBannerService bannerService;

    /**
     * 查詢有效輪播（前台首頁用）
     */
    @GetMapping("/banners")
    public AjaxResult listBanners(@RequestParam(defaultValue = "HOME_TOP") String position) {
        List<ShopBanner> list = bannerService.selectActiveBanners(position);
        return success(list);
    }

    /**
     * 查詢熱門商品
     */
    @GetMapping("/products/hot")
    public AjaxResult listHotProducts(@RequestParam(defaultValue = "8") Integer limit) {
        List<ShopProduct> list = productService.selectHotProducts(limit);
        return success(list);
    }

    /**
     * 查詢新品商品
     */
    @GetMapping("/products/new")
    public AjaxResult listNewProducts(@RequestParam(defaultValue = "8") Integer limit) {
        List<ShopProduct> list = productService.selectNewProducts(limit);
        return success(list);
    }

    /**
     * 查詢推薦商品
     */
    @GetMapping("/products/recommend")
    public AjaxResult listRecommendProducts(@RequestParam(defaultValue = "8") Integer limit) {
        List<ShopProduct> list = productService.selectRecommendProducts(limit);
        return success(list);
    }

    /**
     * 查詢商品列表（分頁）
     */
    @GetMapping("/products")
    public TableDataInfo listProducts(ShopProduct product,
                                      @RequestParam(required = false) String categoryIds) {
        // 只顯示上架的商品
        product.setStatus("ON_SALE");

        // 處理逗號分隔的分類 ID 列表
        if (categoryIds != null && !categoryIds.isBlank()) {
            List<Long> ids = Arrays.stream(categoryIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
            if (!ids.isEmpty()) {
                product.setCategoryIds(ids);
            }
        }

        startPage();
        List<ShopProduct> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    /**
     * 查詢商品詳情
     */
    @GetMapping("/product/{productId}")
    public AjaxResult getProduct(@PathVariable Long productId) {
        ShopProduct product = productService.selectProductById(productId);
        if (product == null || !"ON_SALE".equals(product.getStatus())) {
            return error("商品不存在或已下架");
        }
        // 增加瀏覽量
        productService.increaseViewCount(productId);
        return success(product);
    }

    /**
     * 查詢商品 SKU 列表
     */
    @GetMapping("/product/{productId}/skus")
    public AjaxResult getProductSkus(@PathVariable Long productId) {
        List<ShopProductSku> list = skuService.selectSkuListByProductId(productId);
        // 只返回啟用的 SKU（支援 'ENABLED' 和 '1' 兩種格式）
        list = list.stream().filter(sku -> "ENABLED".equals(sku.getStatus()) || "1".equals(sku.getStatus())).toList();
        return success(list);
    }

    /**
     * 查詢分類列表
     */
    @GetMapping("/categories")
    public AjaxResult listCategories() {
        ShopCategory query = new ShopCategory();
        query.setStatus("ENABLED");
        List<ShopCategory> list = categoryService.selectCategoryList(query);
        return success(list);
    }
}
