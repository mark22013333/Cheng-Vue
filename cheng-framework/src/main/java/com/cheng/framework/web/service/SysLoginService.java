package com.cheng.framework.web.service;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.constant.Constants;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.exception.user.*;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.MessageUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.framework.manager.AsyncManager;
import com.cheng.framework.manager.factory.AsyncFactory;
import com.cheng.framework.security.context.AuthenticationContextHolder;
import com.cheng.system.service.ISysConfigService;
import com.cheng.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 登入校驗方法
 *
 * @author cheng
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登入驗證
     *
     * @param username 使用者名
     * @param password 密碼
     * @param code     驗證碼
     * @param uuid     唯一標識
     * @return 結果
     */
    public String login(String username, String password, String code, String uuid) {
        // 驗證碼校驗
        validateCaptcha(username, code, uuid);
        // 登入前置校驗
        loginPreCheck(username, password);
        // 使用者驗證
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 該方法會去呼叫UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 產生token
        return tokenService.createToken(loginUser);
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
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha)) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登入前置校驗
     *
     * @param username 使用者名
     * @param password 使用者密碼
     */
    public void loginPreCheck(String username, String password) {
        // 使用者名或密碼為空 錯誤
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密碼如果不在指定範圍内 錯誤
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 使用者名不在指定範圍内 錯誤
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名單校驗
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 記錄登入訊息
     *
     * @param userId 使用者ID
     */
    public void recordLoginInfo(Long userId) {
        userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
    }
}
