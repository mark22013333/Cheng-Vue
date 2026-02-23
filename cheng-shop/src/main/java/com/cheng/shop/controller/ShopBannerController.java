package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopBanner;
import com.cheng.shop.service.IShopBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 輪播 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/banner")
@RequiredArgsConstructor
public class ShopBannerController extends BaseController {

    private final IShopBannerService bannerService;

    /**
     * 查詢輪播列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Banner.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(ShopBanner banner) {
        startPage();
        List<ShopBanner> list = bannerService.selectBannerList(banner);
        return getDataTable(list);
    }

    /**
     * 查詢輪播詳情
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Banner.QUERY + "')")
    @GetMapping("/{bannerId}")
    public AjaxResult getInfo(@PathVariable Long bannerId) {
        return success(bannerService.selectBannerById(bannerId));
    }

    /**
     * 新增輪播
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Banner.ADD + "')")
    @Log(title = "輪播管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopBanner banner) {
        banner.setCreateBy(SecurityUtils.getUsername());
        int result = bannerService.insertBanner(banner);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("bannerTitle", banner.getTitle());
        return ajaxResult;
    }

    /**
     * 修改輪播
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Banner.EDIT + "')")
    @Log(title = "輪播管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopBanner banner) {
        banner.setUpdateBy(SecurityUtils.getUsername());
        int result = bannerService.updateBanner(banner);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("bannerTitle", banner.getTitle());
        return ajaxResult;
    }

    /**
     * 刪除輪播
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Banner.REMOVE + "')")
    @Log(title = "輪播管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bannerIds}")
    public AjaxResult remove(@PathVariable Long[] bannerIds) {
        // 取得輪播名稱用於日誌
        StringBuilder names = new StringBuilder();
        for (Long bannerId : bannerIds) {
            ShopBanner banner = bannerService.selectBannerById(bannerId);
            if (banner != null) {
                if (!names.isEmpty()) {
                    names.append("、");
                }
                names.append(banner.getTitle());
            }
        }

        int result = bannerService.deleteBannerByIds(bannerIds);
        AjaxResult ajaxResult = toAjax(result);
        if (!names.isEmpty()) {
            ajaxResult.put("deletedBanners", names.toString());
        }
        return ajaxResult;
    }
}
