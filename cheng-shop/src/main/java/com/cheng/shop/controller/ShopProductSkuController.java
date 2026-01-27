package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.service.IShopProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SKU Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/sku")
@RequiredArgsConstructor
public class ShopProductSkuController extends BaseController {

    private final IShopProductSkuService skuService;

    /**
     * 根據商品ID查詢SKU列表
     */
    @PreAuthorize("@ss.hasPermi('shop:product:query')")
    @GetMapping("/list/{productId}")
    public AjaxResult list(@PathVariable Long productId) {
        List<ShopProductSku> list = skuService.selectSkuListByProductId(productId);
        return success(list);
    }

    /**
     * 查詢SKU詳細
     */
    @PreAuthorize("@ss.hasPermi('shop:product:query')")
    @GetMapping("/{skuId}")
    public AjaxResult getInfo(@PathVariable Long skuId) {
        return success(skuService.selectSkuById(skuId));
    }

    /**
     * 新增SKU
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品SKU", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopProductSku sku) {
        if (sku.getSkuCode() != null && !skuService.checkSkuCodeUnique(sku)) {
            return error("新增SKU失敗，SKU編碼已存在");
        }
        return toAjax(skuService.insertSku(sku));
    }

    /**
     * 修改SKU
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品SKU", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopProductSku sku) {
        if (sku.getSkuCode() != null && !skuService.checkSkuCodeUnique(sku)) {
            return error("修改SKU失敗，SKU編碼已存在");
        }
        return toAjax(skuService.updateSku(sku));
    }

    /**
     * 刪除SKU
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品SKU", businessType = BusinessType.DELETE)
    @DeleteMapping("/{skuId}")
    public AjaxResult remove(@PathVariable Long skuId) {
        return toAjax(skuService.deleteSkuById(skuId));
    }

    /**
     * 批量儲存SKU
     */
    @PreAuthorize("@ss.hasPermi('shop:product:edit')")
    @Log(title = "商品SKU", businessType = BusinessType.UPDATE)
    @PostMapping("/batch/{productId}")
    public AjaxResult batchSave(@PathVariable Long productId, @RequestBody List<ShopProductSku> skuList) {
        return toAjax(skuService.batchSaveSku(productId, skuList));
    }
}
