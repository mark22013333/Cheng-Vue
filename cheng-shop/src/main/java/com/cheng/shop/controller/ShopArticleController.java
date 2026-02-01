package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopArticle;
import com.cheng.shop.service.IShopArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城文章 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/article")
@RequiredArgsConstructor
public class ShopArticleController extends BaseController {

    private final IShopArticleService articleService;

    /**
     * 查詢文章列表
     */
    @PreAuthorize("@ss.hasPermi('shop:article:list')")
    @GetMapping("/list")
    public TableDataInfo list(ShopArticle article) {
        startPage();
        List<ShopArticle> list = articleService.selectArticleList(article);
        return getDataTable(list);
    }

    /**
     * 查詢文章詳情
     */
    @PreAuthorize("@ss.hasPermi('shop:article:query')")
    @GetMapping("/{articleId}")
    public AjaxResult getInfo(@PathVariable Long articleId) {
        return success(articleService.selectArticleById(articleId));
    }

    /**
     * 新增文章
     */
    @PreAuthorize("@ss.hasPermi('shop:article:add')")
    @Log(title = "文章管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopArticle article) {
        article.setCreateBy(SecurityUtils.getUsername());
        return toAjax(articleService.insertArticle(article));
    }

    /**
     * 修改文章
     */
    @PreAuthorize("@ss.hasPermi('shop:article:edit')")
    @Log(title = "文章管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopArticle article) {
        article.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(articleService.updateArticle(article));
    }

    /**
     * 刪除文章
     */
    @PreAuthorize("@ss.hasPermi('shop:article:remove')")
    @Log(title = "文章管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{articleIds}")
    public AjaxResult remove(@PathVariable Long[] articleIds) {
        return toAjax(articleService.deleteArticleByIds(articleIds));
    }
}
