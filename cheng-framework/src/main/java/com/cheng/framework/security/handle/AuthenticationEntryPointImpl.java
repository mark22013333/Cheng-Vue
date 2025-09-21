package com.cheng.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.cheng.common.constant.HttpStatus;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * 認證失敗處理類 返回未授權
 *
 * @author cheng
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
    @Serial
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        int code = HttpStatus.UNAUTHORIZED;
        String msg = StringUtils.format("請求訪問：{}，認證失敗，無法訪問系統資源", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}
