package com.cheng.common.utils.file;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.constant.Constants;
import com.cheng.common.utils.StringUtils;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * 圖片處理工具類
 *
 * @author cheng
 */
public class ImageUtils {
    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    public static byte[] getImage(String imagePath) {
        InputStream is = getFile(imagePath);
        try {
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            log.error("圖片載入異常:", e);
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static InputStream getFile(String imagePath) {
        try {
            byte[] result = readFile(imagePath);
            result = Arrays.copyOf(result, result.length);
            return new ByteArrayInputStream(result);
        } catch (Exception e) {
            log.error("取得圖片異常:", e);
        }
        return null;
    }

    /**
     * 讀取文件為位元組數據
     *
     * @param url 地址
     * @return 位元組數據
     */
    public static byte[] readFile(String url) {
        InputStream in = null;
        try {
            if (url.startsWith("http")) {
                // 網路地址
                URL urlObj = new URL(url);
                URLConnection urlConnection = urlObj.openConnection();
                urlConnection.setConnectTimeout(30 * 1000);
                urlConnection.setReadTimeout(60 * 1000);
                urlConnection.setDoInput(true);
                in = urlConnection.getInputStream();
            } else {
                // 本機地址
                String localPath = CoolAppsConfig.getProfile();
                String downloadPath = localPath + StringUtils.substringAfter(url, Constants.RESOURCE_PREFIX);
                in = new FileInputStream(downloadPath);
            }
            return IOUtils.toByteArray(in);
        } catch (Exception e) {
            log.error("取得文件路徑異常:", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
