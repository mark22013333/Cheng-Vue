package com.cheng.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.cheng.common.constant.Constants;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.utils.MessageUtils;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.framework.manager.AsyncManager;
import com.cheng.framework.manager.factory.AsyncFactory;
import com.cheng.framework.web.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * 自定義登出處理類 返回成功
 *
 * @author cheng
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Autowired
    private TokenService tokenService;

    /**
     * 登出處理
     *
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            String userName = loginUser.getUsername();
            // 刪除使用者暫存記錄
            tokenService.delLoginUser(loginUser.getToken());
            // 記錄使用者登出日誌
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }
}
