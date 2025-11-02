package com.cheng.framework.web.filter;

import com.cheng.common.utils.TraceUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * TraceId 追蹤過濾器
 * 為每個 HTTP 請求自動產生和清理 traceId
 *
 * @author cheng
 * @since 2025-11-02
 */
@Component
@Order(1)  // 設定為最高優先級，確保最先執行
public class TraceFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TraceFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("TraceFilter 初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 嘗試從請求標頭取得 traceId（支援鏈路追蹤）
            String traceId = httpRequest.getHeader(TraceUtils.HEADER_TRACE_ID);

            // 初始化 traceId
            if (traceId != null && !traceId.isEmpty()) {
                TraceUtils.initTrace(null, traceId, TraceUtils.TRACE_ID);
            } else {
                TraceUtils.initTrace();
            }

            // 將 traceId 放到回應標頭中，方便前端追蹤
            httpResponse.setHeader(TraceUtils.HEADER_TRACE_ID, TraceUtils.getTraceId());

            // 繼續執行請求
            chain.doFilter(request, response);

        } finally {
            // 請求結束後清理 traceId，避免執行緒池複用導致的 traceId 污染
            TraceUtils.clearTrace();
        }
    }

    @Override
    public void destroy() {
        log.info("TraceFilter destroy");
    }
}
