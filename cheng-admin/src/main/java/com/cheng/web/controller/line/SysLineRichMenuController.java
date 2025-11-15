package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.SysLineRichMenu;
import com.cheng.line.service.ISysLineRichMenuService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LINE Rich Menu (圖文選單) Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/richMenu")
public class SysLineRichMenuController extends BaseController {

    @Autowired
    private ISysLineRichMenuService richMenuService;

    /**
     * 查詢 Rich Menu 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLineRichMenu richMenu) {
        startPage();
        List<SysLineRichMenu> list = richMenuService.selectRichMenuList(richMenu);
        return getDataTable(list);
    }

    /**
     * 匯出 Rich Menu 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:export')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLineRichMenu richMenu) {
        List<SysLineRichMenu> list = richMenuService.selectRichMenuList(richMenu);
        ExcelUtil<SysLineRichMenu> util = new ExcelUtil<>(SysLineRichMenu.class);
        util.exportExcel(response, list, "Rich Menu 列表");
    }

    /**
     * 取得 Rich Menu 詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(richMenuService.selectRichMenuById(id));
    }

    /**
     * 根據 LINE richMenuId 查詢
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:query')")
    @GetMapping("/byRichMenuId/{richMenuId}")
    public AjaxResult getByRichMenuId(@PathVariable("richMenuId") String richMenuId) {
        return success(richMenuService.selectRichMenuByRichMenuId(richMenuId));
    }

    /**
     * 根據頻道 ID 查詢預設選單
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:query')")
    @GetMapping("/default/{configId}")
    public AjaxResult getDefaultByConfigId(@PathVariable("configId") Integer configId) {
        return success(richMenuService.selectDefaultRichMenuByConfigId(configId));
    }

    /**
     * 新增 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:add')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysLineRichMenu richMenu) {
        return toAjax(richMenuService.insertRichMenu(richMenu));
    }

    /**
     * 修改 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:edit')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysLineRichMenu richMenu) {
        return toAjax(richMenuService.updateRichMenu(richMenu));
    }

    /**
     * 刪除 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:remove')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(richMenuService.deleteRichMenuByIds(ids));
    }

    /**
     * 發布 Rich Menu 到 LINE 平台
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/publish/{id}")
    public AjaxResult publish(@PathVariable Long id) {
        String richMenuId = richMenuService.publishRichMenu(id);
        return AjaxResult.success("發布成功", richMenuId);
    }

    /**
     * 設定為預設選單
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:setDefault')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/setDefault/{id}")
    public AjaxResult setDefault(@PathVariable Long id) {
        return toAjax(richMenuService.setDefaultRichMenu(id));
    }

    /**
     * 從 LINE 平台刪除 Rich Menu
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/deleteFromLine/{id}")
    public AjaxResult deleteFromLine(@PathVariable Long id) {
        return toAjax(richMenuService.deleteRichMenuFromLine(id));
    }

    /**
     * 綁定 Rich Menu 到使用者
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.UPDATE)
    @PostMapping("/linkToUser")
    public AjaxResult linkToUser(@RequestParam String userId, @RequestParam String richMenuId) {
        return toAjax(richMenuService.linkRichMenuToUser(userId, richMenuId));
    }

    /**
     * 解除使用者的 Rich Menu 綁定
     */
    @PreAuthorize("@ss.hasPermi('line:richMenu:publish')")
    @Log(title = "Rich Menu 管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/unlinkFromUser/{userId}")
    public AjaxResult unlinkFromUser(@PathVariable String userId) {
        return toAjax(richMenuService.unlinkRichMenuFromUser(userId));
    }

    /**
     * 檢查選單名稱是否唯一
     */
    @GetMapping("/checkNameUnique")
    public AjaxResult checkNameUnique(SysLineRichMenu richMenu) {
        boolean unique = richMenuService.checkNameUnique(richMenu);
        return success(unique);
    }
}
