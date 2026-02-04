package com.cheng.shop.utils;

import com.cheng.common.constant.HttpStatus;
import com.cheng.common.exception.ServiceException;
import com.cheng.shop.security.ShopMemberLoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 商城會員安全工具類
 *
 * @author cheng
 */
public class ShopMemberSecurityUtils {

    /**
     * 取得會員ID
     */
    public static Long getMemberId() {
        try {
            return getLoginUser().getMemberId();
        } catch (Exception e) {
            throw new ServiceException("取得會員ID異常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 取得會員登入資訊
     */
    public static ShopMemberLoginUser getLoginUser() {
        try {
            return (ShopMemberLoginUser) getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new ServiceException("取得會員資訊異常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 取得 Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
