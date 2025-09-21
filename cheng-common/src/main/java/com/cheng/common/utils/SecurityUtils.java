package com.cheng.common.utils;

import com.cheng.common.constant.Constants;
import com.cheng.common.constant.HttpStatus;
import com.cheng.common.core.domain.entity.SysRole;
import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.PatternMatchUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全服務工具類
 *
 * @author cheng
 */
public class SecurityUtils {

    /**
     * 使用者ID
     **/
    public static Long getUserId() {
        try {
            return getLoginUser().getUserId();
        } catch (Exception e) {
            throw new ServiceException("取得使用者ID異常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 取得部門ID
     **/
    public static Long getDeptId() {
        try {
            return getLoginUser().getDeptId();
        } catch (Exception e) {
            throw new ServiceException("取得部門ID異常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 取得使用者帳號
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new ServiceException("取得使用者帳號異常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 取得使用者
     **/
    public static LoginUser getLoginUser() {
        try {
            return (LoginUser) getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new ServiceException("取得使用者訊息異常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 取得Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 產生BCryptPasswordEncoder密碼
     *
     * @param password 密碼
     * @return 加密字串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判斷密碼是否相同
     *
     * @param rawPassword     真實密碼
     * @param encodedPassword 加密後字串
     * @return 結果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否為管理員
     *
     * @param userId 使用者ID
     * @return 結果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 驗證使用者是否具備某權限
     *
     * @param permission 權限字串
     * @return 使用者是否具備某權限
     */
    public static boolean hasPermi(String permission) {
        return hasPermi(getLoginUser().getPermissions(), permission);
    }

    /**
     * 判斷是否包含權限
     *
     * @param authorities 權限列表
     * @param permission  權限字串
     * @return 使用者是否具備某權限
     */
    public static boolean hasPermi(Collection<String> authorities, String permission) {
        return authorities.stream().filter(StringUtils::hasText)
                .anyMatch(x -> Constants.ALL_PERMISSION.equals(x) || PatternMatchUtils.simpleMatch(x, permission));
    }

    /**
     * 驗證使用者是否擁有某個角色
     *
     * @param role 角色標識
     * @return 使用者是否具備某角色
     */
    public static boolean hasRole(String role) {
        List<SysRole> roleList = getLoginUser().getUser().getRoles();
        Collection<String> roles = roleList.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
        return hasRole(roles, role);
    }

    /**
     * 判斷是否包含角色
     *
     * @param roles 角色列表
     * @param role  角色
     * @return 使用者是否具備某角色權限
     */
    public static boolean hasRole(Collection<String> roles, String role) {
        return roles.stream().filter(StringUtils::hasText)
                .anyMatch(x -> Constants.SUPER_ADMIN.equals(x) || PatternMatchUtils.simpleMatch(x, role));
    }

}
