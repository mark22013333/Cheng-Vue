package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.SysLineRichMenuAlias;
import com.cheng.line.service.ISysLineRichMenuAliasService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * LINE Rich Menu Alias Controller
 * 
 * @author cheng
 */
@RestController
@RequestMapping("/line/richMenuAlias")
@RequiredArgsConstructor
public class SysLineRichMenuAliasController extends BaseController {

    private final ISysLineRichMenuAliasService richMenuAliasService;

    /**
     * 查詢 Rich Menu Alias 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLineRichMenuAlias richMenuAlias) {
        startPage();
        List<SysLineRichMenuAlias> list = richMenuAliasService.selectRichMenuAliasList(richMenuAlias);
        return getDataTable(list);
    }

    /**
     * 根據 Rich Menu ID 查詢所有 Alias
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:list')")
    @GetMapping("/listByRichMenuId/{richMenuId}")
    public AjaxResult listByRichMenuId(@PathVariable Long richMenuId) {
        List<SysLineRichMenuAlias> list = richMenuAliasService.selectRichMenuAliasByRichMenuId(richMenuId);
        return success(list);
    }

    /**
     * 匯出 Rich Menu Alias 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:export')")
    @Log(title = "Rich Menu Alias", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLineRichMenuAlias richMenuAlias) {
        List<SysLineRichMenuAlias> list = richMenuAliasService.selectRichMenuAliasList(richMenuAlias);
        ExcelUtil<SysLineRichMenuAlias> util = new ExcelUtil<>(SysLineRichMenuAlias.class);
        util.exportExcel(response, list, "Rich Menu Alias");
    }

    /**
     * 取得 Rich Menu Alias 詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(richMenuAliasService.selectRichMenuAliasById(id));
    }

    /**
     * 根據 Alias ID 取得詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:query')")
    @GetMapping(value = "/byAliasId/{aliasId}")
    public AjaxResult getInfoByAliasId(@PathVariable("aliasId") String aliasId) {
        return success(richMenuAliasService.selectRichMenuAliasByAliasId(aliasId));
    }

    /**
     * 新增 Rich Menu Alias
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:add')")
    @Log(title = "Rich Menu Alias", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysLineRichMenuAlias richMenuAlias) {
        return toAjax(richMenuAliasService.insertRichMenuAlias(richMenuAlias));
    }

    /**
     * 修改 Rich Menu Alias
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:edit')")
    @Log(title = "Rich Menu Alias", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysLineRichMenuAlias richMenuAlias) {
        return toAjax(richMenuAliasService.updateRichMenuAlias(richMenuAlias));
    }

    /**
     * 刪除 Rich Menu Alias
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:remove')")
    @Log(title = "Rich Menu Alias", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(richMenuAliasService.deleteRichMenuAliasByIds(ids));
    }

    /**
     * 檢查 Alias ID 是否唯一
     */
    @GetMapping("/checkAliasIdUnique")
    public AjaxResult checkAliasIdUnique(@RequestParam String aliasId) {
        return success(richMenuAliasService.checkAliasIdUnique(aliasId));
    }

    /**
     * 從 LINE 平台同步 Alias 列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:sync')")
    @Log(title = "Rich Menu Alias", businessType = BusinessType.OTHER)
    @PostMapping("/sync")
    public AjaxResult sync() {
        int count = richMenuAliasService.syncAliasFromLine();
        return success("同步成功，共同步 " + count + " 筆資料");
    }

    /**
     * 檢查別名使用情況
     * 返回使用該別名的選單列表
     */
    @PreAuthorize("@ss.hasPermi('line:richMenuAlias:query')")
    @GetMapping("/checkUsage/{aliasId}")
    public AjaxResult checkUsage(@PathVariable String aliasId) {
        return success(richMenuAliasService.checkAliasUsage(aliasId));
    }
}
