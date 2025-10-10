package com.cheng.crawler.utils;

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
     * 從網址下載圖片到指定目錄
     *
     * @param imageUrl  圖片網址
     * @param savePath  儲存路徑（資料夾）
     * @param fileName  檔案名稱（不含副檔名）
     * @return 儲存後的完整路徑，失敗回傳 null
     */
    public static String downloadImage(String imageUrl, String savePath, String fileName) {
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

            // 完整檔案路徑
            String fullFileName = fileName + "." + extension;
            String fullPath = savePath + File.separator + fullFileName;

            // 建立 URL 連接
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setRequestProperty("Referer", "https://isbn.tw/");

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
        // 移除查詢參數
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            url = url.substring(0, queryIndex);
        }
        // 取得最後一個點之後的內容
        int dotIndex = url.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < url.length() - 1) {
            return url.substring(dotIndex + 1).toLowerCase();
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
