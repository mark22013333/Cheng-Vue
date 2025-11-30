package com.cheng.crawler.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * FlareSolver 工具類
 * 用於透過 FlareSolver 代理伺服器處理 Cloudflare 驗證
 *
 * @author cheng
 * @since 2025-01-04
 */
@Slf4j
public class FlareSolverUtil {

    /**
     * FlareSolver 服務位址（預設值）
     */
    private static String flareSolverUrl = "http://localhost:8191/v1";

    /**
     * 設定 FlareSolver 服務位址
     *
     * @param url FlareSolver 服務 URL
     */
    public static void setFlareSolverUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            flareSolverUrl = url.trim();
            log.info("FlareSolver 服務 URL 已設定為: {}", flareSolverUrl);
        }
    }

    /**
     * 請求逾時時間（毫秒）
     */
    private static final int REQUEST_TIMEOUT = 60000; // 60 秒

    /**
     * 最大重試次數
     */
    private static final int MAX_RETRIES = 2;

    /**
     * OkHttpClient 實例（專用於 FlareSolver，配置長逾時）
     */
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(70, TimeUnit.SECONDS)  // 比 FlareSolver 的 60 秒多一點
            .retryOnConnectionFailure(true)
            .build();

    /**
     * FlareSolver 指令列舉
     */
    @Getter
    @RequiredArgsConstructor
    public enum Command {
        /**
         * 取得頁面內容（處理 Cloudflare 驗證）
         */
        REQUEST("request.get"),

        /**
         * POST 請求
         */
        REQUEST_POST("request.post"),

        /**
         * 建立 Session
         */
        SESSIONS_CREATE("sessions.create"),

        /**
         * 銷毀 Session
         */
        SESSIONS_DESTROY("sessions.destroy"),

        /**
         * 列出所有 Session
         */
        SESSIONS_LIST("sessions.list");

        private final String command;

    }

    /**
     * FlareSolver 回應結果
     */
    @Setter
    @Getter
    public static class FlareSolverResponse {
        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 狀態碼
         */
        private int status;

        /**
         * HTML 內容
         */
        private String html;

        /**
         * Cookies
         */
        private String cookies;

        /**
         * User-Agent
         */
        private String userAgent;

        /**
         * 最終 URL（重定向後的 URL）
         */
        private String url;

        /**
         * 錯誤訊息
         */
        private String message;

        /**
         * 原始回應
         */
        private JSONObject rawResponse;

    }

    /**
     * 取得頁面內容（自動處理 Cloudflare 驗證）
     *
     * @param url 目標 URL
     * @return FlareSolver 回應結果
     */
    public static FlareSolverResponse getPage(String url) {
        return getPage(url, null, null);
    }

    /**
     * 取得頁面內容（帶 Session）
     *
     * @param url       目標 URL
     * @param sessionId Session ID（可選）
     * @return FlareSolver 回應結果
     */
    public static FlareSolverResponse getPage(String url, String sessionId) {
        return getPage(url, sessionId, null);
    }

    /**
     * 取得頁面內容（完整參數）
     *
     * @param url        目標 URL
     * @param sessionId  Session ID（可選，重複使用可提升效能）
     * @param maxTimeout 最大逾時時間（毫秒，可選）
     * @return FlareSolver 回應結果
     */
    public static FlareSolverResponse getPage(String url, String sessionId, Integer maxTimeout) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cmd", Command.REQUEST.getCommand());
        requestBody.put("url", url);

        if (sessionId != null && !sessionId.trim().isEmpty()) {
            requestBody.put("session", sessionId);
        }

        if (maxTimeout != null && maxTimeout > 0) {
            requestBody.put("maxTimeout", maxTimeout);
        }

        return sendRequest(requestBody, 0);
    }

    /**
     * 建立 Session
     *
     * @return Session ID
     */
    public static String createSession() {
        return createSession(null);
    }

    /**
     * 建立 Session（指定 ID）
     *
     * @param sessionId 自訂 Session ID（可選）
     * @return Session ID
     */
    public static String createSession(String sessionId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cmd", Command.SESSIONS_CREATE.getCommand());

        if (sessionId != null && !sessionId.trim().isEmpty()) {
            requestBody.put("session", sessionId);
        }

        FlareSolverResponse response = sendRequest(requestBody, 0);

        if (response.isSuccess() && response.getRawResponse() != null) {
            return response.getRawResponse().getString("session");
        }

        return null;
    }

    /**
     * 銷毀 Session
     *
     * @param sessionId Session ID
     * @return 是否成功
     */
    public static boolean destroySession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Session ID 為空，無法銷毀");
            return false;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cmd", Command.SESSIONS_DESTROY.getCommand());
        requestBody.put("session", sessionId);

        FlareSolverResponse response = sendRequest(requestBody, 0);
        return response.isSuccess();
    }

    /**
     * 發送請求到 FlareSolver
     *
     * @param requestBody 請求體
     * @param retryCount  當前重試次數
     * @return FlareSolver 回應結果
     */
    private static FlareSolverResponse sendRequest(Map<String, Object> requestBody, int retryCount) {
        FlareSolverResponse result = new FlareSolverResponse();

        try {
            log.info("發送 FlareSolver 請求: {}", requestBody.get("url"));
            log.debug("請求內容: {}", JSON.toJSONString(requestBody));

            // 建立 POST 請求
            String jsonBody = JSON.toJSONString(requestBody);
            RequestBody body = RequestBody.create(
                    jsonBody,
                    okhttp3.MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(flareSolverUrl)
                    .post(body)
                    .build();

            // 執行請求
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";

                log.debug("FlareSolver 回應狀態碼: {}", statusCode);
                log.debug("FlareSolver 回應內容: {}", responseBody);

                if (statusCode == HttpStatus.OK.value()) {
                    JSONObject jsonResponse = JSON.parseObject(responseBody);
                    result.setRawResponse(jsonResponse);

                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    if ("ok".equalsIgnoreCase(status)) {
                        result.setSuccess(true);

                        JSONObject solution = jsonResponse.getJSONObject("solution");
                        if (solution != null) {
                            result.setStatus(solution.getIntValue("status", 200));
                            result.setHtml(solution.getString("response"));
                            result.setUserAgent(solution.getString("userAgent"));
                            result.setUrl(solution.getString("url"));  // 最終 URL（重定向後）

                            // 處理 Cookies
                            Object cookiesObj = solution.get("cookies");
                            if (cookiesObj != null) {
                                result.setCookies(cookiesObj.toString());
                            }
                        }

                        log.info("FlareSolver 請求成功: {}", message);
                    } else {
                        result.setSuccess(false);
                        result.setMessage(message != null ? message : "FlareSolver 回應狀態異常");
                        log.warn("FlareSolver 請求失敗: {}", result.getMessage());
                    }
                } else {
                    result.setSuccess(false);
                    result.setMessage(String.format("HTTP 狀態碼異常: %d", statusCode));
                    log.error("FlareSolver HTTP 請求失敗: {}", result.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("FlareSolver 請求發生錯誤: {}", e.getMessage(), e);

            // 重試機制
            if (retryCount < MAX_RETRIES) {
                log.info("嘗試重試 ({}/{})", retryCount + 1, MAX_RETRIES);
                try {
                    Thread.sleep(2000); // 等待 2 秒後重試
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return sendRequest(requestBody, retryCount + 1);
            }

            result.setSuccess(false);
            result.setMessage("請求失敗: " + e.getMessage());
        }

        return result;
    }

    /**
     * 檢查 FlareSolver 服務是否可用
     *
     * @return 是否可用
     */
    public static boolean isServiceAvailable() {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cmd", Command.SESSIONS_LIST.getCommand());

            FlareSolverResponse response = sendRequest(requestBody, 0);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("檢查 FlareSolver 服務可用性失敗: {}", e.getMessage());
            return false;
        }
    }
}
