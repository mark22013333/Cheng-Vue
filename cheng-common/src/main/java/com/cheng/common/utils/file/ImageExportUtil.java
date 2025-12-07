package com.cheng.common.utils.file;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 圖片匯出工具類
 * <p>
 * 用於將物品的圖片檔案打包成 ZIP 壓縮檔，支援分割壓縮和進度回報
 *
 * @author cheng
 * @since 2025-12-07
 */
@Slf4j
public class ImageExportUtil {

    /**
     * 單個 ZIP 檔案最大大小（500MB）
     * <p>
     * 當圖片壓縮檔超過此大小時，會自動分割成多個檔案
     */
    private static final long MAX_ZIP_SIZE = 500 * 1024 * 1024; // 500MB

    /**
     * 緩衝區大小（8KB）
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 圖片壓縮結果
     *
     * @param zipFiles      壓縮檔案列表（如果分割會有多個）
     * @param successCount  成功壓縮的圖片數量
     * @param missingImages 缺失的圖片路徑列表
     */
    public record ImageZipResult(List<File> zipFiles, int successCount, List<String> missingImages) {

        public boolean hasMissingImages() {
            return missingImages != null && !missingImages.isEmpty();
        }
    }

    /**
     * 壓縮圖片檔案（支援分割壓縮）
     *
     * @param imageRelativePaths 圖片相對路徑列表（如：書籍封面/isbn_xxx.jpg）
     * @param uploadPath         檔案上傳根路徑
     * @param outputDir          輸出目錄（用於存放壓縮檔案）
     * @param baseFileName       基礎檔名（如：images）
     * @param progressCallback   進度回調（參數為已處理的圖片數量）
     * @return ImageZipResult 壓縮結果
     * @throws IOException IO 異常
     */
    public static ImageZipResult zipImages(
            List<String> imageRelativePaths,
            String uploadPath,
            File outputDir,
            String baseFileName,
            Consumer<Integer> progressCallback) throws IOException {

        List<File> zipFiles = new ArrayList<>();
        List<String> missingImages = new ArrayList<>();
        int successCount = 0;
        int partNumber = 1;
        int processedCount = 0;

        // 確保輸出目錄存在
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("無法建立輸出目錄：" + outputDir.getAbsolutePath());
        }

        // 第一個壓縮檔
        File currentZipFile = new File(outputDir, baseFileName + "_part" + partNumber + ".zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(currentZipFile));
        zipFiles.add(currentZipFile);

        long currentZipSize = 0;

        try {
            for (String relativePath : imageRelativePaths) {
                if (relativePath == null || relativePath.trim().isEmpty()) {
                    processedCount++;
                    if (progressCallback != null) {
                        progressCallback.accept(processedCount);
                    }
                    continue;
                }

                // 組成完整路徑（處理 /profile 前綴映射）
                String fullPath = convertToAbsolutePath(uploadPath, relativePath);
                File imageFile = new File(fullPath);

                if (!imageFile.exists() || !imageFile.isFile()) {
                    log.warn("圖片檔案不存在：{}", fullPath);
                    missingImages.add(relativePath);
                    processedCount++;
                    if (progressCallback != null) {
                        progressCallback.accept(processedCount);
                    }
                    continue;
                }

                long imageSize = imageFile.length();

                // 檢查是否需要分割（當前 ZIP 大小 + 新圖片大小 > 最大限制）
                if (currentZipSize + imageSize > MAX_ZIP_SIZE && successCount > 0) {
                    // 安全關閉當前 ZIP
                    try {
                        zos.close();
                    } catch (IOException e) {
                        log.warn("關閉 ZIP part{} 時發生錯誤", partNumber, e);
                    }
                    log.info("圖片壓縮檔 part{} 已達大小限制，建立新檔案", partNumber);

                    // 建立新的 ZIP
                    partNumber++;
                    currentZipFile = new File(outputDir, baseFileName + "_part" + partNumber + ".zip");
                    zos = new ZipOutputStream(new FileOutputStream(currentZipFile));
                    zipFiles.add(currentZipFile);
                    currentZipSize = 0;
                }

                try {
                    // 加入到 ZIP（保留原始檔名）
                    String fileName = imageFile.getName();
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);

                    // 寫入檔案內容
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }

                    zos.closeEntry();
                    currentZipSize += imageSize;
                    successCount++;

                    log.debug("成功壓縮圖片：{}", relativePath);

                } catch (IOException e) {
                    log.error("壓縮圖片時發生錯誤：{}", relativePath, e);
                    missingImages.add(relativePath);
                }

                processedCount++;
                if (progressCallback != null) {
                    progressCallback.accept(processedCount);
                }
            }

        } finally {
            // 確保最後的 ZIP 被關閉
            try {
                zos.close();
            } catch (IOException e) {
                log.error("關閉 ZIP 輸出流時發生錯誤", e);
            }
        }

        log.info("圖片壓縮完成 - 成功: {}, 缺失: {}, 分割檔案數: {}",
                successCount, missingImages.size(), zipFiles.size());

        return new ImageZipResult(zipFiles, successCount, missingImages);
    }

    /**
     * 建立缺失圖片報告檔案
     *
     * @param missingImages 缺失的圖片路徑列表
     * @return 報告檔案內容（純文字）
     */
    public static String createMissingImagesReport(List<String> missingImages) {
        if (missingImages == null || missingImages.isEmpty()) {
            return "";
        }

        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("         缺失圖片報告\n");
        report.append("========================================\n\n");
        report.append("總計缺失圖片數量：").append(missingImages.size()).append("\n\n");
        report.append("缺失圖片清單：\n");
        report.append("----------------------------------------\n");

        for (int i = 0; i < missingImages.size(); i++) {
            report.append(String.format("%4d. %s\n", i + 1, missingImages.get(i)));
        }

        report.append("----------------------------------------\n\n");
        report.append("說明：\n");
        report.append("1. 這些圖片在資料庫中有記錄，但實體檔案不存在\n");
        report.append("2. 匯入時這些物品的圖片將無法恢復\n");
        report.append("3. 請確認是否需要手動處理這些圖片\n\n");
        report.append("建議：\n");
        report.append("- 檢查圖片是否被誤刪\n");
        report.append("- 確認檔案路徑配置是否正確\n");
        report.append("- 考慮重新上傳缺失的圖片\n");

        return report.toString();
    }

    /**
     * 將文字內容寫入檔案
     *
     * @param content  文字內容
     * @param filePath 檔案路徑
     * @throws IOException IO 異常
     */
    public static void writeTextToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(path, content);
    }

    /**
     * 刪除臨時檔案
     *
     * @param file 要刪除的檔案
     */
    public static void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            if (file.delete()) {
                log.debug("已刪除臨時檔案：{}", file.getAbsolutePath());
            } else {
                log.warn("無法刪除臨時檔案：{}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 將資料庫存的相對路徑轉換為檔案系統的絕對路徑
     * <p>
     * 資料庫中存的是訪問路徑（如：/profile/book-covers/isbn_xxx.jpg），
     * 需要將 /profile 前綴替換為實際的 uploadPath
     * 
     * @param uploadPath   檔案上傳根路徑（如：/Users/cheng/uploadPath）
     * @param relativePath 資料庫中的相對路徑（如：/profile/book-covers/isbn_xxx.jpg）
     * @return 完整的檔案系統路徑
     */
    private static String convertToAbsolutePath(String uploadPath, String relativePath) {
        // 處理 null 或空字串
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return "";
        }
        
        // 去除首尾空白
        String path = relativePath.trim();
        
        // 移除 /profile 前綴（資料庫存的是訪問路徑，需要轉換為實際路徑）
        // 例如：/profile/book-covers/isbn_xxx.jpg -> book-covers/isbn_xxx.jpg
        if (path.startsWith("/profile/")) {
            path = path.substring("/profile/".length());
        } else if (path.startsWith("/profile")) {
            path = path.substring("/profile".length());
        }
        
        // 移除開頭的斜線（如果有）
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        // 將 Unix 路徑分隔符轉換為系統分隔符
        path = path.replace("/", File.separator);
        
        // 拼接完整路徑
        return uploadPath + File.separator + path;
    }

    /**
     * 刪除臨時目錄及其內容
     * <p>
     * 使用 NIO.2 API 遞迴刪除目錄樹，比傳統 File.delete() 更可靠
     *
     * @param dir 要刪除的目錄
     */
    public static void deleteTempDirectory(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            try {
                Path path = dir.toPath();
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @NotNull
                    @Override
                    public FileVisitResult visitFile(@NotNull Path file,
                                                     @NotNull BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @NotNull
                    @Override
                    public FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                log.debug("已刪除臨時目錄：{}", dir.getAbsolutePath());
            } catch (IOException e) {
                log.warn("無法刪除臨時目錄：{}, 錯誤：{}", dir.getAbsolutePath(), e.getMessage());
            }
        }
    }
}
