package com.cheng.common.utils.http;

import com.cheng.common.constant.Constants;
import com.cheng.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * 共用http發送方法
 *
 * @author cheng
 */
public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 向指定 URL 發送GET方法的請求
     *
     * @param url 發送請求的 URL
     * @return 所代表遠端資源的響應結果
     */
    public static String sendGet(String url) {
        return sendGet(url, StringUtils.EMPTY);
    }

    /**
     * 向指定 URL 發送GET方法的請求
     *
     * @param url   發送請求的 URL
     * @param param 請求參數，請求參數應該是 name1=value1&name2=value2 的形式。
     * @return 所代表遠端資源的響應結果
     */
    public static String sendGet(String url, String param) {
        return sendGet(url, param, Constants.UTF8);
    }

    /**
     * 向指定 URL 發送GET方法的請求
     *
     * @param url         發送請求的 URL
     * @param param       請求參數，請求參數應該是 name1=value1&name2=value2 的形式。
     * @param contentType 編碼類型
     * @return 所代表遠端資源的響應結果
     */
    public static String sendGet(String url, String param, String contentType) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
            log.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
        } catch (ConnectException e) {
            log.error("呼叫HttpUtils.sendGet ConnectException, url={},param={}", url, param, e);
        } catch (SocketTimeoutException e) {
            log.error("呼叫HttpUtils.sendGet SocketTimeoutException, url={},param={}", url, param, e);
        } catch (IOException e) {
            log.error("呼叫HttpUtils.sendGet IOException, url={},param={}", url, param, e);
        } catch (Exception e) {
            log.error("呼叫HttpsUtil.sendGet Exception, url={},param={}", url, param, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                log.error("呼叫in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    /**
     * 向指定 URL 發送POST方法的請求
     *
     * @param url   發送請求的 URL
     * @param param 請求參數，請求參數應該是 name1=value1&name2=value2 的形式。
     * @return 所代表遠端資源的響應結果
     */
    public static String sendPost(String url, String param) {
        return sendPost(url, param, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    /**
     * 向指定 URL 發送POST方法的請求
     *
     * @param url         發送請求的 URL
     * @param param       請求參數
     * @param contentType 内容類型
     * @return 所代表遠端資源的響應結果
     */
    public static String sendPost(String url, String param, String contentType) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            log.info("sendPost - {}", url);
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", contentType);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
        } catch (ConnectException e) {
            log.error("呼叫HttpUtils.sendPost ConnectException, url={},param={}", url, param, e);
        } catch (SocketTimeoutException e) {
            log.error("呼叫HttpUtils.sendPost SocketTimeoutException, url={},param={}", url, param, e);
        } catch (IOException e) {
            log.error("呼叫HttpUtils.sendPost IOException, url={},param={}", url, param, e);
        } catch (Exception e) {
            log.error("呼叫HttpsUtil.sendPost Exception, url={},param={}", url, param, e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("呼叫in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    public static String sendSSLPost(String url, String param) {
        return sendSSLPost(url, param, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    public static String sendSSLPost(String url, String param, String contentType) {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
        try {
            log.info("sendSSLPost - {}", urlNameString);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", contentType);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ret = "";
            while ((ret = br.readLine()) != null) {
                if (ret != null && !"".equals(ret.trim())) {
                    result.append(new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                }
            }
            log.info("recv - {}", result);
            conn.disconnect();
            br.close();
        } catch (ConnectException e) {
            log.error("呼叫HttpUtils.sendSSLPost ConnectException, url={},param={}", url, param, e);
        } catch (SocketTimeoutException e) {
            log.error("呼叫HttpUtils.sendSSLPost SocketTimeoutException, url={},param={}", url, param, e);
        } catch (IOException e) {
            log.error("呼叫HttpUtils.sendSSLPost IOException, url={},param={}", url, param, e);
        } catch (Exception e) {
            log.error("呼叫HttpsUtil.sendSSLPost Exception, url={},param={}", url, param, e);
        }
        return result.toString();
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
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

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}