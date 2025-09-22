package com.cheng.framework.web.service;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.constant.Constants;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.entity.SysUser;
import com.cheng.common.core.domain.model.RegisterBody;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.exception.user.CaptchaException;
import com.cheng.common.exception.user.CaptchaExpireException;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.MessageUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.framework.manager.AsyncManager;
import com.cheng.framework.manager.factory.AsyncFactory;
import com.cheng.system.service.ISysConfigService;
import com.cheng.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 註冊校驗方法
 *
 * @author cheng
 */
@Component
public class SysRegisterService {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 註冊
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 驗證碼開關
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username)) {
            msg = "使用者名不能為空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "使用者密碼不能為空";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "帳號長度必須在2到20個字串之間";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密碼長度必須在5到20個字串之間";
        } else if (!userService.checkUserNameUnique(sysUser)) {
            msg = "儲存使用者'" + username + "'失敗，註冊帳號已存在";
        } else {
            sysUser.setNickName(username);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag) {
                msg = "註冊失敗,請聯絡系統管理人員";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校驗驗證碼
     *
     * @param username 使用者名
     * @param code     驗證碼
     * @param uuid     唯一標識
     * @return 結果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
