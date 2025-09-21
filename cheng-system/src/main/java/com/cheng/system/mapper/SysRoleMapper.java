package com.cheng.system.mapper;

import com.cheng.common.core.domain.entity.SysRole;

import java.util.List;

/**
 * 角色表 數據層
 *
 * @author cheng
 */
public interface SysRoleMapper
{
    /**
     * 根據條件分頁查詢角色數據
     *
     * @param role 角色訊息
     * @return 角色數據集合訊息
     */
    public List<SysRole> selectRoleList(SysRole role);

    /**
     * 根據使用者ID查詢角色
     *
     * @param userId 使用者ID
     * @return 角色列表
     */
    public List<SysRole> selectRolePermissionByUserId(Long userId);

    /**
     * 查詢所有角色
     * 
     * @return 角色列表
     */
    public List<SysRole> selectRoleAll();

    /**
     * 根據使用者ID取得角色選擇框列表
     *
     * @param userId 使用者ID
     * @return 選中角色ID列表
     */
    public List<Long> selectRoleListByUserId(Long userId);

    /**
     * 通過角色ID查詢角色
     * 
     * @param roleId 角色ID
     * @return 角色物件訊息
     */
    public SysRole selectRoleById(Long roleId);

    /**
     * 根據使用者ID查詢角色
     *
     * @param userName 使用者名
     * @return 角色列表
     */
    public List<SysRole> selectRolesByUserName(String userName);

    /**
     * 校驗角色名稱是否唯一
     *
     * @param roleName 角色名稱
     * @return 角色訊息
     */
    public SysRole checkRoleNameUnique(String roleName);

    /**
     * 校驗角色權限是否唯一
     *
     * @param roleKey 角色權限
     * @return 角色訊息
     */
    public SysRole checkRoleKeyUnique(String roleKey);

    /**
     * 修改角色訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    public int updateRole(SysRole role);

    /**
     * 新增角色訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    public int insertRole(SysRole role);

    /**
     * 通過角色ID刪除角色
     * 
     * @param roleId 角色ID
     * @return 結果
     */
    public int deleteRoleById(Long roleId);

    /**
     * 批量刪除角色訊息
     *
     * @param roleIds 需要刪除的角色ID
     * @return 結果
     */
    public int deleteRoleByIds(Long[] roleIds);
}
