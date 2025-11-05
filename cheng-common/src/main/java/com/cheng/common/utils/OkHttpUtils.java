package com.cheng.common.utils;

import com.cheng.common.utils.dto.ApiResponse;
import com.cheng.common.utils.dto.IpProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 工具類
 * <pre>
 * 版本歷史：
 * - 1.0: 初始版本
 * - 1.1: 增加異步方法可用
 * - 1.2: 新增LINE_REQUEST_ID_HEADER
 * - 1.3: 新增CSV解析方法
 * - 1.4: 修復{@link #addParam(Map)}, {@link #addParam(ObjectNode)}問題，避免參數被覆蓋
 *        增加對{@link #addParam(Map)}的參數判斷，避免空參數導致問題
 *        重構{@link #addHeader(String, String)}，用簡單的判斷方式提升效率
 * - 1.5: 修正單例模式的問題，避免將之前的參數附加上下一次的請求
 * </pre>
 *
 * @author cheng
 * @since 2023/12/3 12:01 PM
 **/
@Slf4j
public class OkHttpUtils {

    private static final List<VersionInfo> VERSION_HISTORY = new ArrayList<>();

    static {
        VERSION_HISTORY.add(new VersionInfo(1.0, "初始版本", LocalDate.of(2023, 12, 3)));
        VERSION_HISTORY.add(new VersionInfo(1.1, "增加異步方法可用", LocalDate.of(2023, 12, 4)));
        VERSION_HISTORY.add(new VersionInfo(1.2, "新增 LINE_REQUEST_ID_HEADER", LocalDate.of(2023, 12, 5)));
        VERSION_HISTORY.add(new VersionInfo(1.3, "新增 CSV 解析方法", LocalDate.of(2023, 12, 6)));
        VERSION_HISTORY.add(new VersionInfo(1.4, """
                修復 addParam(Map), addParam(ObjectNode) 問題，避免參數被覆蓋
                增加對 addParam(Map) 的參數判斷，避免空參數導致問題
                重構 addHeader(String, String)，用簡單的判斷方式提升效率
                """, LocalDate.of(2024, 12, 7)));
        VERSION_HISTORY.add(new VersionInfo(1.5, "修正單例模式的問題，避免將之前的參數附加上下一次的請求", LocalDate.of(2024, 12, 11)));

    }

    public static void printVersionHistory() {
        log.info("OkHttpUtils 版本歷史：");
        VERSION_HISTORY.forEach(version ->
                log.info("版本: {}, 日期: {}, 描述: {}", version.version(), version.date(), version.description()));
    }

    private record VersionInfo(double version, String description, LocalDate date) {
    }

//    @Data
//    private static class VersionInfo {
//        private final double version;
//        private final String description;
//        private final LocalDate date;
//    }

    public static final String CHARSET_BIG5 = "Big5";

    private String requestIdHeader = "x-line-request-id";
    private static volatile OkHttpClient okHttpClient = null;
    private static volatile Semaphore semaphore = null;
    private Map<String, String> headerMap;
    private Map<String, String> paramMap;
    private ObjectNode paramObj;
    private Request.Builder request;

    OkHttpUtils() {
        init(null);
    }

    private OkHttpUtils(IpProxy ipProxy) {
        init(ipProxy);
    }

    private void init(IpProxy ipProxy) {
        if (okHttpClient == null) {
            synchronized (OkHttpUtils.class) {
                if (okHttpClient == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                    TrustManager[] trustManagers = buildTrustManagers();
                    OkHttpClient.Builder builder =
                            new OkHttpClient.Builder()
                                    .addInterceptor(logging)
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(8, TimeUnit.SECONDS)
                                    .readTimeout(8, TimeUnit.SECONDS)
                                    .sslSocketFactory(
                                            createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                                    .hostnameVerifier((hostName, session) -> true)
                                    .retryOnConnectionFailure(true)
//                                    .cookieJar(new OkHttpCookieManager())
                                    .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));
                    if (ipProxy != null) {
                        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipProxy.getIp(), ipProxy.getPort())));
                    }
                    okHttpClient = builder.build();
                    addHeader(HttpHeaders.ACCEPT, "*/*");
                    addHeader(HttpHeaders.CONNECTION, "keep-alive");
//                    addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
                }
            }
        }
    }

    /**
     * 控制多個執行緒對共用資源的訪問
     */
    private static Semaphore getSemaphoreInstance() {
        synchronized (OkHttpUtils.class) {
            if (semaphore == null) {
                semaphore = new Semaphore(0);
            }
        }
        return semaphore;
    }

    /**
     * 若要使用代理IP需要使用{@link OkHttpUtils#builder(IpProxy)}
     */
    // 不使用池的方法
//    public static OkHttpUtils builder() {
//        return new OkHttpUtils();
//    }
    public static OkHttpUtils builder() {
        try {
            return OkHttpUtilsPool.borrow();
        } catch (Exception e) {
            log.error("===>ERR:", e);
            return new OkHttpUtils();
        }
    }

    public void release() {
        try {
            paramMap = null;
            paramObj = null;
            headerMap = null;
            OkHttpUtilsPool.release(this);
        } catch (Exception e) {
            log.error("Failed to release OkHttpUtils back to pool", e);
        }
    }

    public static OkHttpUtils builder(IpProxy ipProxy) {
        return new OkHttpUtils(ipProxy);
    }

    public OkHttpUtils addParam(String key, String value) {
        if (paramObj == null) {
            paramObj = JacksonUtil.genJsonObject();
        }
        paramObj.put(key, value);
        return this;
    }

    public OkHttpUtils addParam(String key, Object value) {
        if (paramObj == null) {
            paramObj = JacksonUtil.genJsonObject();
        }
        paramObj.putPOJO(key, value);
        return this;
    }

    public OkHttpUtils addParam(ObjectNode params) {
        if (paramObj == null) {
            paramObj = JacksonUtil.genJsonObject();
        }
        if (params != null) {
            paramObj.setAll(params);
        }
        return this;
    }

    public OkHttpUtils addParam(Map<String, String> params) {
        if (paramMap == null) {
            paramMap = new HashMap<>(10);
        }
        if (params != null) {
            paramMap.putAll(params);
        }
        return this;
    }

    public OkHttpUtils addHeader(String key, String value) {
        if (key == null || value == null) {
            return this;
        }
        initHeaderMapIfNeeded();
        headerMap.put(key, value);
        return this;
    }

    /**
     * 使用 Map 批次新增 HTTP 請求標頭
     *
     * @param headers 包含標頭名稱與值的 Map
     * @return OkHttpUtils 實例，支援方法鏈結
     */
    public OkHttpUtils addHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return this;
        }
        initHeaderMapIfNeeded();
        headerMap.putAll(headers);
        return this;
    }

    /**
     * 使用 JsonNode 批次新增 HTTP 請求標頭
     *
     * @param headersNode 包含標頭名稱與值的 JsonNode
     * @return OkHttpUtils 實例，支援方法鏈結
     */
    public OkHttpUtils addHeader(JsonNode headersNode) {
        if (headersNode == null || !headersNode.isObject()) {
            return this;
        }
        try {
            initHeaderMapIfNeeded();
            addJsonNodeHeadersToMap(headersNode);
        } catch (Exception e) {
            log.warn("新增 JsonNode 標頭時發生錯誤: {}", e.getMessage());
        }
        return this;
    }

    /**
     * 使用 ObjectNode 批次新增 HTTP 請求標頭
     *
     * @param headersNode 包含標頭名稱與值的 ObjectNode
     * @return OkHttpUtils 實例，支援方法鏈結
     */
    public OkHttpUtils addHeader(ObjectNode headersNode) {
        if (headersNode == null) {
            return this;
        }
        try {
            initHeaderMapIfNeeded();
            addJsonNodeHeadersToMap(headersNode);
        } catch (Exception e) {
            log.warn("新增 ObjectNode 標頭時發生錯誤: {}", e.getMessage());
        }
        return this;
    }

    /**
     * 初始化 headerMap 如果為 null
     */
    private void initHeaderMapIfNeeded() {
        if (headerMap == null) {
            headerMap = new HashMap<>(10);
        }
    }

    /**
     * 從 JsonNode 提取標頭並加到 headerMap
     *
     * @param node JsonNode 或 ObjectNode
     */
    private void addJsonNodeHeadersToMap(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            try {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getValue() != null && entry.getValue().isTextual()) {
                    headerMap.put(entry.getKey(), entry.getValue().asText());
                }
            } catch (Exception e) {
                log.warn("處理標頭欄位時發生錯誤，略過此欄位: {}", e.getMessage());
                // 略過此欄位但繼續處理其他欄位
            }
        }
    }

    public OkHttpUtils addBrowserHeader() {
        addHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 OPR/93.0.0.0");
        return this;
    }

    public OkHttpUtils addLineAuthHeader(String token) {
        addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
        return this;
    }

    public OkHttpUtils get(String url) {
        String u = combinedUrlParams(url);
        log.info("===> [GET] API Url:{}", u);
        request.url(u);
        return this;
    }

    public String combinedUrlParams(String url) {
        request = new Request.Builder().get();
        Map<String, String> map;
        if (paramMap == null || paramMap.isEmpty()) {
            map = JacksonUtil.toMap(JacksonUtil.toJsonString(paramObj));
        } else {
            map = paramMap;
        }
        String encodingParams = ApiUtils.encodingParams(map);
        encodingParams = (StringUtils.isBlank(encodingParams) ? "" : "?") + encodingParams;
        return String.format("%s%s", url, encodingParams);
    }

    /**
     * 預設使用json submit，若要設定Form submit則需使用{@link OkHttpUtils#post(String, boolean)}，並將布林參數設為false
     */
    public OkHttpUtils post(String url) {
        return post(url, true);
    }

    /**
     * 使用順序<br>
     * {@link #addHeader(String, String)}/{@link #addParam(ObjectNode)} -> <br>
     * {@link #post(String)} -> <br>
     * {@link #sync()}/{@link #async()}
     * <p>
     *
     * @param isJsonPost true:Json, false:Form
     */
    public OkHttpUtils post(String url, boolean isJsonPost) {
        log.info("===>[POST] API Url:{}", url);
        RequestBody requestBody;
        if (isJsonPost) {
            String json = JacksonUtil.toJsonString(paramObj);
            log.info("===>[ParamJson]:{}", json);
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            requestBody = RequestBody.create(json, mediaType);
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            Map<String, String> params;
            if (paramMap == null || paramMap.isEmpty()) {
                params = JacksonUtil.toMap(JacksonUtil.toJsonString(paramObj));
            } else {
                params = paramMap;
            }
            if (MapUtils.isNotEmpty(params)) {
                params.forEach((k, v) -> {
                    log.info("key:{}, value:{}", k, v);
                    Optional.ofNullable(v).ifPresent(val -> formBody.add(k, val));
                });
            }
            FormBody fb = formBody.build();
            requestBody = fb;
            log.info("===>[FormBody]:{}", fb);
        }
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }

    public ApiResponse syncReadCsv() {
        setHeader(request);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Apache Commons CSV
            try (CSVParser csvParser = new CSVParser(new InputStreamReader(
                    Objects.requireNonNull(response.body()).byteStream(), CHARSET_BIG5), CSVFormat.DEFAULT)) {
                return ApiResponse.builder()
                        .httpStatusCode(response.code())
                        .csvRecords(csvParser.getRecords()).build();
            }

        } catch (IOException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return ApiResponse.empty();
        }
    }

    public ApiResponse sync() {
        setHeader(request);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {

            int code = response.code();
            String lineRequestId = response.header(requestIdHeader);
            String contentType = response.header("Content-Type");

            ApiResponse apiResponse;
            if (contentType != null && contentType.contains("application/json")) {
                String data = Objects.requireNonNull(response.body()).string();
                apiResponse = ApiResponse.builder()
                        .httpStatusCode(code)
                        .lineRequestId(lineRequestId)
                        .resultData(data)
                        .contentType(contentType)
                        .build();
            } else {
                byte[] data = Objects.requireNonNull(response.body()).bytes();
                apiResponse = ApiResponse.builder()
                        .httpStatusCode(code)
                        .lineRequestId(lineRequestId)
                        .contentType(contentType)
                        .binaryData(data)
                        .build();
            }
            int httpStatusCode = apiResponse.getHttpStatusCode();
            if (httpStatusCode != HttpStatus.OK.value()) {
                log.info("===> API httpStatusCode:{} Response:{}", httpStatusCode, apiResponse);
            }
            return apiResponse;
        } catch (IOException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return ApiResponse.empty();
        } finally {
            release();
        }
    }

    public ApiResponse async() {
        StringBuilder buffer = new StringBuilder();
        ApiResponse.ApiResponseBuilder builder = ApiResponse.builder();
        setHeader(request);
        okHttpClient
                .newCall(request.build())
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                buffer.append("ERROR:").append(e.getMessage());
                                release();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response)
                                    throws IOException {
                                try {
                                    builder.httpStatusCode(response.code());
                                    buffer.append(Objects.requireNonNull(response.body()).string());
                                } finally {
                                    release();
                                }
//                                getSemaphoreInstance().release();
                            }
                        });

        // async 版本無需使用 Semaphore，直接返回結果 (如果非同步操作需要同步完成，則應啟用)
//        try {
//            getSemaphoreInstance().acquire();
//        } catch (InterruptedException e) {
//            log.info("ERR:{}", ExceptionUtils.getStackTrace(e));
//            Thread.currentThread().interrupt();
//        }

        return builder.resultData(buffer.toString()).build();
    }

    public void async(ICallBack callBack) {
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        try {
                            callBack.onFailure(call, e.getMessage());
                        } finally {
                            release();
                        }
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response)
                            throws IOException {
                        try {
                            callBack.onSuccessful(call, Objects.requireNonNull(response.body()).string());
                        } finally {
                            release();
                        }
                    }
                });
    }

    private void setHeader(Request.Builder request) {
        if (headerMap != null) {
            headerMap.forEach(request::addHeader);
        }
    }

    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
        }
        return ssfFactory;
    }

    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    public interface ICallBack {
        void onSuccessful(Call call, String data) throws IOException;

        void onFailure(Call call, String errorMsg);
    }

    public static void main(String[] args) {
        // 參數設定順序要照以下方式，非同步或同步執行要放最後
        String domain = "https://www.test.com";
        String u = ApiUtils.getUrl(domain, "/testAPI");
        testPost(u);

        // 非同步有兩個方式，一個有callback，另一個直接返回結果
        //        testPostAsync(u);
        //        testPostAsyncCallback(u);
    }

    private static void testPost(String url) {
        OkHttpUtils.builder().post(url)
                .addParam(JacksonUtil.genJsonObject().put("參數", 1))
                .sync();
    }

    private static void testPostAsync(String u) {
        ApiResponse async = OkHttpUtils.builder().get(u).async();
        System.out.println("async = " + async);
    }

    private static void testPostAsyncCallback(String u) {
        OkHttpUtils.builder().get(u).async(
                new ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        System.out.println("call = " + call);
                        System.out.println("data = " + data);
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        System.out.println("call = " + call);
                        System.out.println("errorMsg = " + errorMsg);
                    }
                });
    }
}
