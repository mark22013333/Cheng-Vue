package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.enums.BusinessType;
import com.cheng.shop.domain.ShopPageBlock;
import com.cheng.shop.service.IShopPageBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 頁面區塊 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/block")
@RequiredArgsConstructor
public class ShopPageBlockController extends BaseController {

    private final IShopPageBlockService blockService;

    /**
     * 查詢區塊列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Block.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(ShopPageBlock block) {
        startPage();
        List<ShopPageBlock> list = blockService.selectBlockList(block);
        return getDataTable(list);
    }

    /**
     * 查詢區塊詳細
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Block.QUERY + "')")
    @GetMapping("/{blockId}")
    public AjaxResult getInfo(@PathVariable Long blockId) {
        return success(blockService.selectBlockById(blockId));
    }

    /**
     * 根據頁面和區塊識別查詢
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Block.QUERY + "')")
    @GetMapping("/key/{pageKey}/{blockKey}")
    public AjaxResult getByKey(@PathVariable String pageKey, @PathVariable String blockKey) {
        return success(blockService.selectBlockByPageAndKey(pageKey, blockKey));
    }

    /**
     * 新增區塊
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Block.ADD + "')")
    @Log(title = "區塊管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopPageBlock block) {
        if (!blockService.checkBlockKeyUnique(block)) {
            return error("新增區塊'" + block.getTitle() + "'失敗，區塊識別已存在");
        }
        return toAjax(blockService.insertBlock(block));
    }

    /**
     * 修改區塊
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Block.EDIT + "')")
    @Log(title = "區塊管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopPageBlock block) {
        if (!blockService.checkBlockKeyUnique(block)) {
            return error("修改區塊'" + block.getTitle() + "'失敗，區塊識別已存在");
        }
        return toAjax(blockService.updateBlock(block));
    }

    /**
     * 刪除區塊
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.Block.REMOVE + "')")
    @Log(title = "區塊管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{blockId}")
    public AjaxResult remove(@PathVariable Long blockId) {
        return toAjax(blockService.deleteBlockById(blockId));
    }
}
