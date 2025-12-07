package com.cheng.common.utils.file;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 圖片匯入工具類
 *
 * @author cheng
 */
@Slf4j
public class ImageImportUtil {

    /**
     * 從資料庫路徑提取檔名
     * 例如：/profile/book-covers/isbn_xxx.jpg -> isbn_xxx.jpg
     *
     * @param dbPath 資料庫路徑
     * @return 檔名
     */
    public static String extractFileName(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            return "";
        }

        String path = dbPath.trim().replace("\\", "/");
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(lastSlashIndex + 1);
        }
        return path;
    }

    /**
     * 從資料庫路徑提取相對路徑（去除 /profile 前綴）
     * 例如：/profile/book-covers/isbn_xxx.jpg -> book-covers/isbn_xxx.jpg
     *
     * @param dbPath 資料庫路徑
     * @return 相對路徑
     */
    public static String extractRelativePath(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            return "";
        }

        String path = dbPath.trim().replace("\\", "/");

        // 移除 /profile 前綴
        if (path.startsWith("/profile/")) {
            path = path.substring("/profile/".length());
        } else if (path.startsWith("/profile")) {
            path = path.substring("/profile".length());
        }

        // 移除開頭的斜線
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * 在指定目錄中遞迴搜尋圖片檔案
     *
     * @param imagesDir 圖片目錄
     * @param fileName  檔名
     * @return 找到的圖片檔案，未找到則返回 null
     */
    public static File findImageFile(File imagesDir, String fileName) {
        if (imagesDir == null || !imagesDir.exists() || !imagesDir.isDirectory()) {
            return null;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }

        return searchFile(imagesDir, fileName);
    }

    /**
     * 遞迴搜尋檔案
     */
    private static File searchFile(File dir, String fileName) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().equals(fileName)) {
                return file;
            } else if (file.isDirectory()) {
                File found = searchFile(file, fileName);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * 複製圖片到上傳路徑，並備份已存在的檔案
     *
     * @param sourceImage  來源圖片
     * @param relativePath 相對路徑（例如：book-covers/isbn_xxx.jpg）
     * @param uploadPath   上傳根路徑
     * @return 備份檔案（若無則為 null）
     * @throws IOException IO 異常
     */
    public static File copyImageToUploadPath(File sourceImage, String relativePath, String uploadPath) throws IOException {
        // 建立目標路徑
        File targetFile = new File(uploadPath + File.separator + relativePath.replace("/", File.separator));
        File targetDir = targetFile.getParentFile();

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("無法建立目錄: " + targetDir.getAbsolutePath());
        }

        // 如果目標檔案已存在，先備份
        File backupFile = null;
        if (targetFile.exists()) {
            String backupPath = targetFile.getAbsolutePath() + ".backup." + System.currentTimeMillis();
            backupFile = new File(backupPath);
            Files.copy(targetFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.debug("備份圖片: {} -> {}", targetFile.getName(), backupFile.getName());
        }

        // 複製圖片
        Files.copy(sourceImage.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.debug("複製圖片: {} -> {}", sourceImage.getName(), targetFile.getAbsolutePath());

        return backupFile;
    }

    /**
     * 驗證圖片格式和大小
     *
     * @param imageFile    圖片檔案
     * @param maxSizeBytes 最大大小（位元組）
     * @return 是否有效
     */
    public static boolean validateImage(File imageFile, long maxSizeBytes) {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            return false;
        }

        // 檢查檔案大小
        if (imageFile.length() > maxSizeBytes) {
            log.warn("圖片超過大小限制: {} ({}B > {}B)", imageFile.getName(), imageFile.length(), maxSizeBytes);
            return false;
        }

        // 檢查副檔名
        String fileName = imageFile.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp") || fileName.endsWith(".webp");
    }

    /**
     * 解壓縮 ZIP 檔案
     *
     * @param zipFile ZIP 檔案
     * @param destDir 目標目錄
     * @throws IOException IO 異常
     */
    public static void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException("無法建立目錄: " + destDir.getAbsolutePath());
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = newFile(destDir, zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("無法建立目錄: " + newFile);
                    }
                } else {
                    // 確保父目錄存在
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("無法建立目錄: " + parent);
                    }

                    // 寫入檔案
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        log.info("解壓縮完成: {} -> {}", zipFile.getName(), destDir.getAbsolutePath());
    }

    /**
     * 建立新檔案，防止 Zip Slip 漏洞
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
