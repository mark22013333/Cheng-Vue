package com.cheng.common.utils.http;

import com.cheng.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * HTTP 路徑工具類
 *
 * <p><b>重要規則</b>：路徑匹配、比對、建立快取鍵等業務邏輯，
 * 必須使用不含 context path 的路徑，以確保在不同部署環境下行為一致。</p>
 *
 * <h3>部署環境差異</h3>
 * <pre>
 * localhost（內嵌 Tomcat）：
 *   getServletPath() → /shop/front/products
 *   getRequestURI()  → /shop/front/products
 *
 * VM（外部 Tomcat, apps.war）：
 *   getServletPath() → /shop/front/products
 *   getRequestURI()  → /apps/shop/front/products
 * </pre>
 *
 * @author cheng
 * @see #getServletPath(HttpServletRequest) 取得路徑用於業務邏輯
 * @see #getFullRequestURI(HttpServletRequest) 取得完整 URI 用於日誌記錄
 */
public final class PathUtils {

    private PathUtils() {
    }

    /**
     * 取得 Servlet 路徑（不含 context path）
     *
     * <p>適用場景：路徑匹配、快取鍵建立、路由判斷等業務邏輯。
     * 在任何部署環境下回傳格式一致。</p>
     *
     * <p><b>警告</b>：請勿使用 {@code request.getRequestURI()} 進行路徑匹配，
     * 因為它在外部 Tomcat 部署時會包含 context path。</p>
     *
     * @param request HTTP 請求
     * @return 不含 context path 的路徑，例如 /shop/front/products
     */
    public static String getServletPath(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return StringUtils.defaultString(request.getServletPath());
    }

    /**
     * 取得完整請求 URI（含 context path）
     *
     * <p>僅用於日誌記錄、錯誤訊息等資訊展示用途。
     * <b>請勿用於路徑匹配或業務邏輯。</b></p>
     *
     * @param request HTTP 請求
     * @return 完整 URI，例如 /apps/shop/front/products
     */
    public static String getFullRequestURI(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return StringUtils.defaultString(request.getRequestURI());
    }

    /**
     * 檢查請求路徑是否以指定前綴開頭
     *
     * @param request HTTP 請求
     * @param prefix 路徑前綴，例如 /shop/
     * @return 是否匹配
     */
    public static boolean pathStartsWith(HttpServletRequest request, String prefix) {
        return getServletPath(request).startsWith(prefix);
    }

    /**
     * 檢查請求路徑是否在白名單中
     *
     * @param request HTTP 請求
     * @param allowedPaths 允許的路徑集合
     * @return 是否在白名單中
     */
    public static boolean isPathInWhitelist(HttpServletRequest request, Set<String> allowedPaths) {
        if (allowedPaths == null || allowedPaths.isEmpty()) {
            return false;
        }
        return allowedPaths.contains(getServletPath(request));
    }
}
