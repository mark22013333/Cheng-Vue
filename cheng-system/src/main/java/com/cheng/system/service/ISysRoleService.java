package com.cheng.system.service;

import com.cheng.common.core.domain.entity.SysRole;
import com.cheng.system.domain.SysUserRole;

import java.util.List;
import java.util.Set;

/**
 * 角色業務層
 *
 * @author cheng
 */
public interface ISysRoleService
{
    /**
     * 根據條件分頁查詢角色數據
     *
     * @param role 角色訊息
     * @return 角色數據集合訊息
     */
    public List<SysRole> selectRoleList(SysRole role);

    /**
     * 根據使用者ID查詢角色列表
     *
     * @param userId 使用者ID
     * @return 角色列表
     */
    public List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 根據使用者ID查詢角色權限
     *
     * @param userId 使用者ID
     * @return 權限列表
     */
    public Set<String> selectRolePermissionByUserId(Long userId);

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
     * 校驗角色名稱是否唯一
     *
     * @param role 角色訊息
     * @return 結果
     */
    public boolean checkRoleNameUnique(SysRole role);

    /**
     * 校驗角色權限是否唯一
     *
     * @param role 角色訊息
     * @return 結果
     */
    public boolean checkRoleKeyUnique(SysRole role);

    /**
     * 校驗角色是否允許操作
     *
     * @param role 角色訊息
     */
    public void checkRoleAllowed(SysRole role);

    /**
     * 校驗角色是否有數據權限
     * 
     * @param roleIds 角色id
     */
    public void checkRoleDataScope(Long... roleIds);

    /**
     * 通過角色ID查詢角色使用數量
     * 
     * @param roleId 角色ID
     * @return 結果
     */
    public int countUserRoleByRoleId(Long roleId);

    /**
     * 新增儲存角色訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    public int insertRole(SysRole role);

    /**
     * 修改儲存角色訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    public int updateRole(SysRole role);

    /**
     * 修改角色狀態
     *
     * @param role 角色訊息
     * @return 結果
     */
    public int updateRoleStatus(SysRole role);

    /**
     * 修改數據權限訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    public int authDataScope(SysRole role);

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

    /**
     * 取消授權使用者角色
     *
     * @param userRole 使用者和角色關聯訊息
     * @return 結果
     */
    public int deleteAuthUser(SysUserRole userRole);

    /**
     * 批量取消授權使用者角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要取消授權的使用者數據ID
     * @return 結果
     */
    public int deleteAuthUsers(Long roleId, Long[] userIds);

    /**
     * 批量選擇授權使用者角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要刪除的使用者數據ID
     * @return 結果
     */
    public int insertAuthUsers(Long roleId, Long[] userIds);
}
