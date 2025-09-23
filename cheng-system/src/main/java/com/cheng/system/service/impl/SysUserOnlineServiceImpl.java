package com.cheng.system.service.impl;

import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.utils.StringUtils;
import com.cheng.system.domain.SysUserOnline;
import com.cheng.system.service.ISysUserOnlineService;
import org.springframework.stereotype.Service;

/**
 * 在線使用者 服務層處理
 *
 * @author cheng
 */
@Service
public class SysUserOnlineServiceImpl implements ISysUserOnlineService {
    /**
     * 通過登入地址查詢訊息
     *
     * @param ipaddr 登入地址
     * @param user   使用者訊息
     * @return 在線使用者訊息
     */
    @Override
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user) {
        if (StringUtils.equals(ipaddr, user.getIpaddr())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通過使用者名稱查詢訊息
     *
     * @param userName 使用者名稱
     * @param user     使用者訊息
     * @return 在線使用者訊息
     */
    @Override
    public SysUserOnline selectOnlineByUserName(String userName, LoginUser user) {
        if (StringUtils.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通過登入地址/使用者名稱查詢訊息
     *
     * @param ipaddr   登入地址
     * @param userName 使用者名稱
     * @param user     使用者訊息
     * @return 在線使用者訊息
     */
    @Override
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user) {
        if (StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 設定在線使用者訊息
     *
     * @param user 使用者訊息
     * @return 在線使用者
     */
    @Override
    public SysUserOnline loginUserToUserOnline(LoginUser user) {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUser())) {
            return null;
        }
        SysUserOnline sysUserOnline = new SysUserOnline();
        sysUserOnline.setTokenId(user.getToken());
        sysUserOnline.setUserName(user.getUsername());
        sysUserOnline.setIpaddr(user.getIpaddr());
        sysUserOnline.setLoginLocation(user.getLoginLocation());
        sysUserOnline.setBrowser(user.getBrowser());
        sysUserOnline.setOs(user.getOs());
        sysUserOnline.setLoginTime(user.getLoginTime());
        if (StringUtils.isNotNull(user.getUser().getDept())) {
            sysUserOnline.setDeptName(user.getUser().getDept().getDeptName());
        }
        return sysUserOnline;
    }
}
