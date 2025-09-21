package com.cheng.framework.web.service;

import com.cheng.common.constant.Constants;
import com.cheng.common.core.domain.entity.SysRole;
import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.framework.security.context.PermissionContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * CoolApps首创 自定義權限實現，ss取自SpringSecurity首字母
 *
 * @author cheng
 */
@Service("ss")
public class PermissionService
{
    /**
     * 驗證使用者是否具備某權限
     *
     * @param permission 權限字串
     * @return 使用者是否具備某權限
     */
    public boolean hasPermi(String permission)
    {
        if (StringUtils.isEmpty(permission))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions()))
        {
            return false;
        }
        PermissionContextHolder.setContext(permission);
        return hasPermissions(loginUser.getPermissions(), permission);
    }

    /**
     * 驗證使用者是否不具備某權限，與 hasPermi邏輯相反
     *
     * @param permission 權限字串
     * @return 使用者是否不具備某權限
     */
    public boolean lacksPermi(String permission)
    {
        return hasPermi(permission) != true;
    }

    /**
     * 驗證使用者是否具有以下任意一個權限
     *
     * @param permissions 以 PERMISSION_DELIMETER 為分隔符號的權限列表
     * @return 使用者是否具有以下任意一個權限
     */
    public boolean hasAnyPermi(String permissions)
    {
        if (StringUtils.isEmpty(permissions))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions()))
        {
            return false;
        }
        PermissionContextHolder.setContext(permissions);
        Set<String> authorities = loginUser.getPermissions();
        for (String permission : permissions.split(Constants.PERMISSION_DELIMETER))
        {
            if (permission != null && hasPermissions(authorities, permission))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判斷使用者是否擁有某個角色
     *
     * @param role 角色字串
     * @return 使用者是否具備某角色
     */
    public boolean hasRole(String role)
    {
        if (StringUtils.isEmpty(role))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().getRoles()))
        {
            return false;
        }
        for (SysRole sysRole : loginUser.getUser().getRoles())
        {
            String roleKey = sysRole.getRoleKey();
            if (Constants.SUPER_ADMIN.equals(roleKey) || roleKey.equals(StringUtils.trim(role)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 驗證使用者是否不具備某角色，與 isRole邏輯相反。
     *
     * @param role 角色名稱
     * @return 使用者是否不具備某角色
     */
    public boolean lacksRole(String role)
    {
        return hasRole(role) != true;
    }

    /**
     * 驗證使用者是否具有以下任意一個角色
     *
     * @param roles 以 ROLE_NAMES_DELIMETER 為分隔符號的角色列表
     * @return 使用者是否具有以下任意一個角色
     */
    public boolean hasAnyRoles(String roles)
    {
        if (StringUtils.isEmpty(roles))
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().getRoles()))
        {
            return false;
        }
        for (String role : roles.split(Constants.ROLE_DELIMETER))
        {
            if (hasRole(role))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判斷是否包含權限
     *
     * @param permissions 權限列表
     * @param permission 權限字串
     * @return 使用者是否具備某權限
     */
    private boolean hasPermissions(Set<String> permissions, String permission)
    {
        return permissions.contains(Constants.ALL_PERMISSION) || permissions.contains(StringUtils.trim(permission));
    }
}
