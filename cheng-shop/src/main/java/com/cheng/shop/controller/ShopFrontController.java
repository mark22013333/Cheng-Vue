package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.shop.domain.ShopBanner;
import com.cheng.shop.domain.ShopCategory;
import com.cheng.shop.domain.ShopArticle;
import com.cheng.shop.domain.ShopGift;
import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.service.IShopArticleService;
import com.cheng.shop.service.IShopBannerService;
import com.cheng.shop.service.IShopCategoryService;
import com.cheng.shop.service.IShopGiftService;
import com.cheng.shop.service.IShopProductService;
import com.cheng.shop.service.IShopProductSkuService;
import com.cheng.shop.service.ShopPriceService;
import com.cheng.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 商城前台 Controller（消費者端）
 *
 * @author cheng
 */
@Anonymous
@PublicApi
@RestController
@RequestMapping("/shop/front")
@RequiredArgsConstructor
public class ShopFrontController extends BaseController {

    private final IShopArticleService articleService;
    private final IShopProductService productService;
    private final IShopProductSkuService skuService;
    private final IShopCategoryService categoryService;
    private final IShopBannerService bannerService;
    private final IShopGiftService giftService;
    private final ShopPriceService priceService;
    private final ISysConfigService configService;

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
        priceService.enrichProductPrices(list);
        return success(list);
    }

    /**
     * 查詢新品商品
     */
    @GetMapping("/products/new")
    public AjaxResult listNewProducts(@RequestParam(defaultValue = "8") Integer limit) {
        List<ShopProduct> list = productService.selectNewProducts(limit);
        priceService.enrichProductPrices(list);
        return success(list);
    }

    /**
     * 查詢推薦商品
     */
    @GetMapping("/products/recommend")
    public AjaxResult listRecommendProducts(@RequestParam(defaultValue = "8") Integer limit) {
        List<ShopProduct> list = productService.selectRecommendProducts(limit);
        priceService.enrichProductPrices(list);
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
        priceService.enrichProductPrices(list);
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
        // 計算折扣價格
        priceService.enrichProductPrices(List.of(product));
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

    /**
     * 查詢可用禮物（前台：根據消費金額篩選）
     */
    @GetMapping("/gifts")
    public AjaxResult listAvailableGifts(@RequestParam BigDecimal amount) {
        if (!"1".equals(configService.selectConfigByKey("shop.gift.enabled"))) {
            return success(Collections.emptyList());
        }
        List<ShopGift> list = giftService.selectAvailableGifts(amount);
        return success(list);
    }

    /**
     * 查詢已發布文章列表（前台，分頁）
     */
    @GetMapping("/articles")
    public TableDataInfo listArticles() {
        startPage();
        List<ShopArticle> list = articleService.selectPublishedArticleList();
        return getDataTable(list);
    }

    /**
     * 查詢文章詳情（前台，瀏覽數+1）
     */
    @GetMapping("/article/{articleId}")
    public AjaxResult getArticle(@PathVariable Long articleId) {
        ShopArticle article = articleService.selectArticleById(articleId);
        if (article == null || !"PUBLISHED".equals(article.getStatus())) {
            return error("文章不存在或未發布");
        }
        articleService.increaseViewCount(articleId);
        return success(article);
    }

    /**
     * 查詢最新文章（首頁用）
     */
    @GetMapping("/articles/latest")
    public AjaxResult listLatestArticles(@RequestParam(defaultValue = "6") Integer limit) {
        List<ShopArticle> list = articleService.selectLatestArticles(limit);
        return success(list);
    }
}
