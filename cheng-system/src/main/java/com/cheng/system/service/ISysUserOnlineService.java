package com.cheng.system.service;

import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.system.domain.SysUserOnline;

/**
 * 在線使用者 服務層
 *
 * @author cheng
 */
public interface ISysUserOnlineService {
    /**
     * 通過登入地址查詢訊息
     *
     * @param ipaddr 登入地址
     * @param user   使用者訊息
     * @return 在線使用者訊息
     */
    SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user);

    /**
     * 通過使用者名稱查詢訊息
     *
     * @param userName 使用者名稱
     * @param user     使用者訊息
     * @return 在線使用者訊息
     */
    SysUserOnline selectOnlineByUserName(String userName, LoginUser user);

    /**
     * 通過登入地址/使用者名稱查詢訊息
     *
     * @param ipaddr   登入地址
     * @param userName 使用者名稱
     * @param user     使用者訊息
     * @return 在線使用者訊息
     */
    SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user);

    /**
     * 設定在線使用者訊息
     *
     * @param user 使用者訊息
     * @return 在線使用者
     */
    SysUserOnline loginUserToUserOnline(LoginUser user);
}
