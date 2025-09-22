package com.cheng.system.mapper;

import com.cheng.system.domain.SysRoleDept;

import java.util.List;

/**
 * 角色與部門關聯表 數據層
 *
 * @author cheng
 */
public interface SysRoleDeptMapper
{
    /**
     * 通過角色ID刪除角色和部門關聯
     * 
     * @param roleId 角色ID
     * @return 結果
     */
    public int deleteRoleDeptByRoleId(Long roleId);

    /**
     * 批次刪除角色部門關聯訊息
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    public int deleteRoleDept(Long[] ids);

    /**
     * 查詢部門使用數量
     *
     * @param deptId 部門ID
     * @return 結果
     */
    public int selectCountRoleDeptByDeptId(Long deptId);

    /**
     * 批次新增角色部門訊息
     *
     * @param roleDeptList 角色部門列表
     * @return 結果
     */
    public int batchRoleDept(List<SysRoleDept> roleDeptList);
}
