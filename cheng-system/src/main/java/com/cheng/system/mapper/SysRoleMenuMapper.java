package com.cheng.system.mapper;

import com.cheng.system.domain.SysRoleMenu;

import java.util.List;

/**
 * 角色與選單關聯表 數據層
 *
 * @author cheng
 */
public interface SysRoleMenuMapper
{
    /**
     * 查詢選單使用數量
     *
     * @param menuId 選單ID
     * @return 結果
     */
    public int checkMenuExistRole(Long menuId);

    /**
     * 通過角色ID刪除角色和選單關聯
     * 
     * @param roleId 角色ID
     * @return 結果
     */
    public int deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量刪除角色選單關聯訊息
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    public int deleteRoleMenu(Long[] ids);

    /**
     * 批量新增角色選單訊息
     *
     * @param roleMenuList 角色選單列表
     * @return 結果
     */
    public int batchRoleMenu(List<SysRoleMenu> roleMenuList);
}
