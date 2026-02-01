package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopGift;
import com.cheng.shop.service.IShopGiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 滿額禮物 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/gift")
@RequiredArgsConstructor
public class ShopGiftController extends BaseController {

    private final IShopGiftService giftService;

    /**
     * 查詢禮物列表
     */
    @PreAuthorize("@ss.hasPermi('shop:gift:list')")
    @GetMapping("/list")
    public TableDataInfo list(ShopGift gift) {
        startPage();
        List<ShopGift> list = giftService.selectGiftList(gift);
        return getDataTable(list);
    }

    /**
     * 查詢禮物詳情
     */
    @PreAuthorize("@ss.hasPermi('shop:gift:query')")
    @GetMapping("/{giftId}")
    public AjaxResult getInfo(@PathVariable Long giftId) {
        return success(giftService.selectGiftById(giftId));
    }

    /**
     * 新增禮物
     */
    @PreAuthorize("@ss.hasPermi('shop:gift:add')")
    @Log(title = "禮物管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopGift gift) {
        gift.setCreateBy(SecurityUtils.getUsername());
        return toAjax(giftService.insertGift(gift));
    }

    /**
     * 修改禮物
     */
    @PreAuthorize("@ss.hasPermi('shop:gift:edit')")
    @Log(title = "禮物管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopGift gift) {
        gift.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(giftService.updateGift(gift));
    }

    /**
     * 刪除禮物
     */
    @PreAuthorize("@ss.hasPermi('shop:gift:remove')")
    @Log(title = "禮物管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{giftIds}")
    public AjaxResult remove(@PathVariable Long[] giftIds) {
        return toAjax(giftService.deleteGiftByIds(giftIds));
    }
}
