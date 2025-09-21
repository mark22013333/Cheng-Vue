package com.cheng.web.controller.system;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.domain.entity.SysMenu;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.StringUtils;
import com.cheng.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 選單訊息
 *
 * @author cheng
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController
{
    @Autowired
    private ISysMenuService menuService;

    /**
     * 取得選單列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu)
    {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return success(menus);
    }

    /**
     * 根據選單編號取得詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId)
    {
        return success(menuService.selectMenuById(menuId));
    }

    /**
     * 取得選單下拉樹列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysMenu menu)
    {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 載入對應角色選單列表樹
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId)
    {
        List<SysMenu> menus = menuService.selectMenuList(getUserId());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }

    /**
     * 新增選單
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "選單管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMenu menu)
    {
        if (!menuService.checkMenuNameUnique(menu))
        {
            return error("新增選單'" + menu.getMenuName() + "'失敗，選單名稱已存在");
        }
        else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath()))
        {
            return error("新增選單'" + menu.getMenuName() + "'失敗，地址必須以http(s)://開頭");
        }
        menu.setCreateBy(getUsername());
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * 修改選單
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "選單管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysMenu menu)
    {
        if (!menuService.checkMenuNameUnique(menu))
        {
            return error("修改選單'" + menu.getMenuName() + "'失敗，選單名稱已存在");
        }
        else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath()))
        {
            return error("修改選單'" + menu.getMenuName() + "'失敗，地址必須以http(s)://開頭");
        }
        else if (menu.getMenuId().equals(menu.getParentId()))
        {
            return error("修改選單'" + menu.getMenuName() + "'失敗，上級選單不能選擇自己");
        }
        menu.setUpdateBy(getUsername());
        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * 刪除選單
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "選單管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Long menuId)
    {
        if (menuService.hasChildByMenuId(menuId))
        {
            return warn("存在子選單,不允許刪除");
        }
        if (menuService.checkMenuExistRole(menuId))
        {
            return warn("選單已分配,不允許刪除");
        }
        return toAjax(menuService.deleteMenuById(menuId));
    }
}