package com.cheng.crawler.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 圖片下載工具類別
 *
 * @author cheng
 */
public class ImageDownloadUtil {
    private static final Logger log = LoggerFactory.getLogger(ImageDownloadUtil.class);
    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 30000; // 30秒

    /**
     * OkHttpClient 實例（用於支援 Cloudflare 保護的圖片下載）
     */
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    /**
     * 從網址下載圖片到指定目錄
     *
     * @param imageUrl 圖片網址
     * @param savePath 儲存路徑（資料夾）
     * @param fileName 檔案名稱（不含副檔名）
     * @return 儲存後的完整路徑，失敗回傳 null
     */
    public static String downloadImage(String imageUrl, String savePath, String fileName) {
        return downloadImage(imageUrl, savePath, fileName, null);
    }

    /**
     * 從網址下載圖片（使用 Cloudflare cookies，適用於 isbn.tw）
     *
     * @param imageUrl  圖片網址
     * @param savePath  儲存路徑（資料夾）
     * @param fileName  檔案名稱（不含副檔名）
     * @param cookies   Cloudflare cookies（從 FlareSolver 取得）
     * @param userAgent User-Agent（從 FlareSolver 取得）
     * @return 儲存後的完整路徑，失敗回傳 null
     */
    public static String downloadImageWithCookies(String imageUrl, String savePath, String fileName,
                                                   String cookies, String userAgent) {
        return downloadImageWithCookies(imageUrl, savePath, fileName, cookies, userAgent, null);
    }

    /**
     * 從網址下載圖片（使用 Cloudflare cookies，並可指定 Referer）
     *
     * @param imageUrl  圖片網址
     * @param savePath  儲存路徑（資料夾）
     * @param fileName  檔案名稱（不含副檔名）
     * @param cookies   Cloudflare cookies（從 FlareSolver 取得）
     * @param userAgent User-Agent（從 FlareSolver 取得）
     * @param referer   自訂 Referer（可為 null，則自動判斷）
     * @return 儲存後的完整路徑，失敗回傳 null
     */
    public static String downloadImageWithCookies(String imageUrl, String savePath, String fileName,
                                                   String cookies, String userAgent, String referer) {
        try {
            // 建立目錄
            Path dirPath = Paths.get(savePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("建立目錄: {}", savePath);
            }

            // 取得圖片副檔名
            String extension = getFileExtension(imageUrl);
            if (extension == null || extension.isEmpty()) {
                extension = "jpg"; // 預設
            }

            // 產生檔案名稱（不加時間戳記，重複掃描時直接覆蓋）
            String fullFileName = fileName + "." + extension;
            String fullPath = savePath + File.separator + fullFileName;

            log.info("準備下載圖片（使用 Cloudflare cookies）: {} -> {}", imageUrl, fullPath);

            // 建立請求（使用 cookies 和 user-agent）
            Request.Builder requestBuilder = new Request.Builder()
                    .url(imageUrl)
                    .get();

            // 設定 User-Agent
            if (userAgent != null && !userAgent.isEmpty()) {
                requestBuilder.header("User-Agent", userAgent);
            } else {
                requestBuilder.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
            }

            // 設定 Cookies
            if (cookies != null && !cookies.isEmpty()) {
                requestBuilder.header("Cookie", cookies);
            }

            // 設定 Referer（防盜鏈保護）
            if (referer != null && !referer.isEmpty()) {
                // 使用指定的 Referer
                requestBuilder.header("Referer", referer);
                log.debug("使用自訂 Referer: {}", referer);
            } else {
                // 自動判斷 Referer
                if (imageUrl.contains("isbn.tw")) {
                    requestBuilder.header("Referer", "https://isbn.tw/");
                } else if (imageUrl.contains("sanmin.com.tw")) {
                    requestBuilder.header("Referer", "https://www.sanmin.com.tw/");
                } else if (imageUrl.contains("books.com.tw")) {
                    requestBuilder.header("Referer", "https://www.books.com.tw/");
                }
            }

            Request request = requestBuilder.build();

            // 執行請求
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("下載圖片失敗，HTTP 回應碼: {}, URL: {}", response.code(), imageUrl);
                    return null;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    log.error("圖片回應內容為空，URL: {}", imageUrl);
                    return null;
                }

                // 檢查 Content-Type 是否為圖片（但允許部分 CDN 配置錯誤的情況）
                String contentType = response.header("Content-Type");
                if (contentType != null && !contentType.startsWith("image/")) {
                    // 如果是 HTML，很可能是防盜鏈或錯誤頁面
                    if (contentType.contains("text/html")) {
                        log.error("回應內容是 HTML 而不是圖片，Content-Type: {}, URL: {}", contentType, imageUrl);
                        log.error("可能原因：1) 防盜鏈保護 2) 需要正確的 Referer 3) Cookie 已過期");
                        return null;
                    }
                    // 其他非圖片 Content-Type 只記錄警告，仍嘗試下載（某些 CDN 可能配置錯誤）
                    log.warn("Content-Type 不是 image/*，但仍嘗試下載: {}, URL: {}", contentType, imageUrl);
                }

                // 寫入檔案
                long fileSize = 0;
                try (InputStream inputStream = body.byteStream();
                     FileOutputStream outputStream = new FileOutputStream(fullPath)) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        fileSize += bytesRead;
                    }
                }

                // 驗證檔案大小（至少 2KB，避免是錯誤頁面）
                File downloadedFile = new File(fullPath);
                if (!downloadedFile.exists() || downloadedFile.length() < 2048) {
                    log.error("下載的圖片檔案過小（可能是錯誤頁面），大小: {} bytes, URL: {}", 
                            downloadedFile.length(), imageUrl);
                    // 刪除無效檔案
                    if (downloadedFile.exists()) {
                        downloadedFile.delete();
                        log.info("已刪除無效圖片檔案: {}", fullPath);
                    }
                    return null;
                }

                log.info("圖片下載成功: {}，檔案大小: {} bytes", fullPath, fileSize);
                return fullPath;
            }

        } catch (Exception e) {
            log.error("下載圖片時發生錯誤: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 從網址下載圖片到指定目錄（可指定 Referer）
     *
     * @param imageUrl 圖片網址
     * @param savePath 儲存路徑（資料夾）
     * @param fileName 檔案名稱（不含副檔名）
     * @param referer  Referer 標頭（可為 null）
     * @return 儲存後的完整路徑，失敗回傳 null
     */
    public static String downloadImage(String imageUrl, String savePath, String fileName, String referer) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // 建立目錄
            Path dirPath = Paths.get(savePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("建立目錄: {}", savePath);
            }

            // 取得圖片副檔名
            String extension = getFileExtension(imageUrl);
            if (extension == null || extension.isEmpty()) {
                extension = "jpg"; // 預設
            }

            // 產生檔案名稱（不加時間戳記，重複掃描時直接覆蓋）
            String fullFileName = fileName + "." + extension;
            String fullPath = savePath + File.separator + fullFileName;

            log.info("準備下載圖片: {} -> {}", imageUrl, fullPath);

            // 建立 URL 連接
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 設定 Referer，如果沒有提供則根據圖片 URL 自動判斷
            if (referer != null && !referer.isEmpty()) {
                connection.setRequestProperty("Referer", referer);
            } else {
                // 自動判斷 Referer
                if (imageUrl.contains("nicebooks.com")) {
                    connection.setRequestProperty("Referer", "https://us.nicebooks.com/");
                } else if (imageUrl.contains("googleapis.com") || imageUrl.contains("googleusercontent.com")) {
                    connection.setRequestProperty("Referer", "https://books.google.com/");
                } else {
                    connection.setRequestProperty("Referer", "https://isbn.tw/");
                }
            }

            // 檢查回應碼
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("下載圖片失敗，HTTP 回應碼: {}, URL: {}", responseCode, imageUrl);
                return null;
            }

            // 讀取並儲存圖片
            inputStream = connection.getInputStream();
            outputStream = new FileOutputStream(fullPath);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            log.info("圖片下載成功: {}", fullPath);
            return fullPath;

        } catch (Exception e) {
            log.error("下載圖片時發生錯誤: {}", e.getMessage(), e);
            return null;
        } finally {
            // 關閉資源
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                log.error("關閉連接時發生錯誤: {}", e.getMessage());
            }
        }
    }

    /**
     * 從 URL 取得副檔名
     *
     * @param url 圖片 URL
     * @return 副檔名（不含點）
     */
    private static String getFileExtension(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // 移除查詢參數
            int queryIndex = url.indexOf('?');
            if (queryIndex > 0) {
                url = url.substring(0, queryIndex);
            }

            // 移除 fragment (#)
            int fragmentIndex = url.indexOf('#');
            if (fragmentIndex > 0) {
                url = url.substring(0, fragmentIndex);
            }

            // 取得 URL 路徑部分（移除協議和域名）
            // 例如: https://images.nicebooks.com/images/abc.jpg -> /images/abc.jpg
            if (url.contains("://")) {
                int pathStart = url.indexOf('/', url.indexOf("://") + 3);
                if (pathStart > 0) {
                    url = url.substring(pathStart);
                }
            }

            // 取得最後一個斜線之後的檔案名稱
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0) {
                url = url.substring(lastSlash + 1);
            }

            // 取得副檔名
            int dotIndex = url.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < url.length() - 1) {
                String ext = url.substring(dotIndex + 1).toLowerCase();
                // 驗證副檔名是否合理（只包含字母和數字，長度 2-5）
                if (ext.matches("[a-z0-9]{2,5}")) {
                    return ext;
                }
            }
        } catch (Exception e) {
            log.warn("解析圖片副檔名失敗: {}", url, e);
        }

        return null;
    }

    /**
     * 產生帶時間戳記的檔案名稱
     *
     * @param prefix 前綴
     * @return 檔案名稱
     */
    public static String generateFileName(String prefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return prefix + "_" + timestamp;
    }
}
