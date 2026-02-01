package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.service.IShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 商品 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/product")
@RequiredArgsConstructor
public class ShopProductController extends BaseController {

    private final IShopProductService productService;

    /**
     * 查詢商品列表
     */
    @PreAuthorize("@ss.hasPermi('shop:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(ShopProduct product,
                              @RequestParam(required = false) String categoryIds) {
        // 處理逗號分隔的分類 ID 列表（支援父分類包含子分類搜尋）
        if (categoryIds != null && !categoryIds.isBlank()) {
            List<Long> ids = Arrays.stream(categoryIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
            if (!ids.isEmpty()) {
                product.setCategoryIds(ids);
                product.setCategoryId(null);
            }
        }
        startPage();
        List<ShopProduct> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    /**
     * 查詢商品詳情
     */
    @PreAuthorize("@ss.hasPermi('shop:product:query')")
    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId) {
        return success(productService.selectProductById(productId));
    }

    /**
     * 新增商品
     */
    @PreAuthorize("@ss.hasPermi('shop:product:add')")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopProduct product) {
        product.setCreateBy(SecurityUtils.getUsername());
        int result = productService.insertProduct(product);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("productTitle", product.getTitle());
        // 返回 productId 供前端後續儲存 SKU 使用
        ajaxResult.put("data", Map.of("productId", product.getProductId()));
        return ajaxResult;
    }

    /**
     * 修改商品
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopProduct product) {
        product.setUpdateBy(SecurityUtils.getUsername());
        int result = productService.updateProduct(product);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("productTitle", product.getTitle());
        return ajaxResult;
    }

    /**
     * 刪除商品
     */
    @PreAuthorize("@ss.hasPermi('shop:product:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds) {
        // 取得商品名稱用於日誌
        StringBuilder names = new StringBuilder();
        for (Long productId : productIds) {
            ShopProduct product = productService.selectProductById(productId);
            if (product != null) {
                if (!names.isEmpty()) {
                    names.append("、");
                }
                names.append(product.getTitle());
            }
        }

        int result = productService.deleteProductByIds(productIds);
        AjaxResult ajaxResult = toAjax(result);
        if (!names.isEmpty()) {
            ajaxResult.put("deletedProducts", names.toString());
        }
        return ajaxResult;
    }

    /**
     * 上架商品
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping("/onSale/{productId}")
    public AjaxResult onSale(@PathVariable Long productId) {
        ShopProduct product = productService.selectProductById(productId);
        int result = productService.onSaleProduct(productId);
        AjaxResult ajaxResult = toAjax(result);
        if (product != null) {
            ajaxResult.put("productTitle", product.getTitle());
            ajaxResult.put("action", "上架");
        }
        return ajaxResult;
    }

    /**
     * 下架商品
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping("/offSale/{productId}")
    public AjaxResult offSale(@PathVariable Long productId) {
        ShopProduct product = productService.selectProductById(productId);
        int result = productService.offSaleProduct(productId);
        AjaxResult ajaxResult = toAjax(result);
        if (product != null) {
            ajaxResult.put("productTitle", product.getTitle());
            ajaxResult.put("action", "下架");
        }
        return ajaxResult;
    }

    /**
     * 批量更新商品標記（熱門/新品/推薦）
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping("/flag")
    public AjaxResult updateFlag(@RequestBody Map<String, Object> params) {
        List<?> rawIds = (List<?>) params.get("productIds");
        Long[] productIds = rawIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toArray(Long[]::new);
        String flagName = (String) params.get("flagName");
        boolean flagValue = Boolean.parseBoolean(params.get("flagValue").toString());

        // 白名單驗證
        if (!Arrays.asList("is_hot", "is_new", "is_recommend").contains(flagName)) {
            return error("不支援的標記類型：" + flagName);
        }

        return toAjax(productService.updateProductFlag(productIds, flagName, flagValue));
    }
}
