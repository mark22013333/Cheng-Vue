package com.cheng.system.service.impl;

import com.cheng.common.annotation.DataScope;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.entity.SysRole;
import com.cheng.common.core.domain.entity.SysUser;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.spring.SpringUtils;
import com.cheng.system.domain.SysRoleDept;
import com.cheng.system.domain.SysRoleMenu;
import com.cheng.system.domain.SysUserRole;
import com.cheng.system.mapper.SysRoleDeptMapper;
import com.cheng.system.mapper.SysRoleMapper;
import com.cheng.system.mapper.SysRoleMenuMapper;
import com.cheng.system.mapper.SysUserRoleMapper;
import com.cheng.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色 業務層處理
 *
 * @author cheng
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * 根據條件分頁查詢角色數據
     *
     * @param role 角色訊息
     * @return 角色數據集合訊息
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根據使用者ID查詢角色
     *
     * @param userId 使用者ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根據使用者ID查詢權限
     *
     * @param userId 使用者ID
     * @return 權限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查詢所有角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * 根據使用者ID取得角色選擇框列表
     *
     * @param userId 使用者ID
     * @return 選中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通過角色ID查詢角色
     * 
     * @param roleId 角色ID
     * @return 角色物件訊息
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * 校驗角色名稱是否唯一
     *
     * @param role 角色訊息
     * @return 結果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校驗角色權限是否唯一
     *
     * @param role 角色訊息
     * @return 結果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校驗角色是否允許操作
     *
     * @param role 角色訊息
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允許操作超級管理員角色");
        }
    }

    /**
     * 校驗角色是否有數據權限
     * 
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            for (Long roleId : roleIds)
            {
                SysRole role = new SysRole();
                role.setRoleId(roleId);
                List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
                if (StringUtils.isEmpty(roles))
                {
                    throw new ServiceException("沒有權限訪問角色數據！");
                }
            }
        }
    }

    /**
     * 通過角色ID查詢角色使用數量
     * 
     * @param roleId 角色ID
     * @return 結果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增儲存角色訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    @Override
    @Transactional
    public int insertRole(SysRole role)
    {
        // 新增角色訊息
        roleMapper.insertRole(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改儲存角色訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    @Override
    @Transactional
    public int updateRole(SysRole role)
    {
        // 修改角色訊息
        roleMapper.updateRole(role);
        // 刪除角色與選單關聯
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改角色狀態
     *
     * @param role 角色訊息
     * @return 結果
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     * 修改數據權限訊息
     *
     * @param role 角色訊息
     * @return 結果
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role)
    {
        // 修改角色訊息
        roleMapper.updateRole(role);
        // 刪除角色與部門關聯
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部門訊息（數據權限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色選單訊息
     *
     * @param role 角色物件
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增使用者與角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色部門訊息(數據權限)
     *
     * @param role 角色物件
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 新增角色與部門（數據權限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 通過角色ID刪除角色
     * 
     * @param roleId 角色ID
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteRoleById(Long roleId)
    {
        // 刪除角色與選單關聯
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 刪除角色與部門關聯
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量刪除角色訊息
     *
     * @param roleIds 需要刪除的角色ID
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能刪除", role.getRoleName()));
            }
        }
        // 刪除角色與選單關聯
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 刪除角色與部門關聯
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授權使用者角色
     *
     * @param userRole 使用者和角色關聯訊息
     * @return 結果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授權使用者角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要取消授權的使用者數據ID
     * @return 結果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量選擇授權使用者角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要授權的使用者數據ID
     * @return 結果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 新增使用者與角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
