package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.InvCategory;
import com.cheng.system.service.IInvCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 物品分類Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/inventory/category")
public class InvCategoryController extends BaseController {
    @Autowired
    private IInvCategoryService invCategoryService;

    /**
     * 查詢物品分類列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(InvCategory invCategory) {
        startPage();
        List<InvCategory> list = invCategoryService.selectInvCategoryList(invCategory);
        return getDataTable(list);
    }

    /**
     * 取得物品分類詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('inventory:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return success(invCategoryService.selectInvCategoryByCategoryId(categoryId));
    }

    /**
     * 新增物品分類
     */
    @PreAuthorize("@ss.hasPermi('inventory:category:add')")
    @Log(title = "物品分類", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody InvCategory invCategory) {
        return toAjax(invCategoryService.insertInvCategory(invCategory));
    }

    /**
     * 修改物品分類
     */
    @PreAuthorize("@ss.hasPermi('inventory:category:edit')")
    @Log(title = "物品分類", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody InvCategory invCategory) {
        return toAjax(invCategoryService.updateInvCategory(invCategory));
    }

    /**
     * 刪除物品分類
     */
    @PreAuthorize("@ss.hasPermi('inventory:category:remove')")
    @Log(title = "物品分類", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds) {
        return toAjax(invCategoryService.deleteInvCategoryByCategoryIds(categoryIds));
    }

    /**
     * 匯出物品分類列表
     */
    @PreAuthorize("@ss.hasPermi('inventory:category:export')")
    @Log(title = "物品分類", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InvCategory invCategory) {
        List<InvCategory> list = invCategoryService.selectInvCategoryList(invCategory);
        ExcelUtil<InvCategory> util = new ExcelUtil<InvCategory>(InvCategory.class);
        util.exportExcel(response, list, "分類資料");
    }
}
