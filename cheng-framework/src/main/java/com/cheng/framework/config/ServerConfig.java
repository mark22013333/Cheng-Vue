package com.cheng.framework.config;

import com.cheng.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 服務相關設定
 *
 * @author cheng
 */
@Component
public class ServerConfig {
    public static String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }

    /**
     * 取得完整的請求路徑，包括：域名，埠號，上下文訪問路徑
     *
     * @return 服務地址
     */
    public String getUrl() {
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }
}
