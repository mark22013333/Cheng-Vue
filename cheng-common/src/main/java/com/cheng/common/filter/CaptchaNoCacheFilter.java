package com.cheng.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * 驗證碼回應避免瀏覽器快取，避免出現 304 造成前端讀不到內容
 *
 * @author cheng
 */
public class CaptchaNoCacheFilter implements Filter {
    private static final Set<String> CAPTCHA_PATHS = Set.of("/captchaImage", "/cadm/captchaImage");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String requestPath = httpRequest.getRequestURI();
            if (CAPTCHA_PATHS.contains(requestPath)) {
                httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setDateHeader("Expires", 0);
            }
        }
        chain.doFilter(request, response);
    }
}
