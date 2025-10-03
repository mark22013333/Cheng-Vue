package com.cheng.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 防盜鏈過濾器
 *
 * @author cheng
 */
public class RefererFilter implements Filter {
    /**
     * 允許的域名列表
     */
    public List<String> allowedDomains;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String domains = filterConfig.getInitParameter("allowedDomains");
        this.allowedDomains = Arrays.asList(domains.split(","));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String referer = req.getHeader("Referer");

        // 如果Referer為空，拒絕訪問
        if (referer == null || referer.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Referer header is required");
            return;
        }

        // 檢查Referer是否在允許的域名列表中
        boolean allowed = false;
        for (String domain : allowedDomains) {
            if (referer.contains(domain)) {
                allowed = true;
                break;
            }
        }

        // 根據檢查結果決定是否放行
        if (allowed) {
            chain.doFilter(request, response);
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Referer '" + referer + "' is not allowed");
        }
    }

    @Override
    public void destroy() {

    }
}