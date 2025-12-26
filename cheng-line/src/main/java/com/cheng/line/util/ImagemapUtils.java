package com.cheng.line.util;

import com.cheng.line.enums.ImagemapWidth;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * LINE Imagemap 圖片處理工具
 * 
 * 根據 LINE Imagemap 規範，自動產生多種尺寸的圖片。
 * LINE 客戶端會根據裝置解析度自動選擇適合的尺寸。
 *
 * @author cheng
 * @see <a href="https://developers.line.biz/en/reference/messaging-api/#imagemap-message">LINE Imagemap Message</a>
 */
public class ImagemapUtils {

    private static final Logger log = LoggerFactory.getLogger(ImagemapUtils.class);

    /**
     * LINE 推薦的圖片格式
     */
    public static final String RECOMMENDED_FORMAT = "jpg";

    /**
     * 產生 Imagemap 所需的所有尺寸圖片
     *
     * @param inputStream 原始圖片輸入流
     * @param outputDir   輸出目錄路徑
     * @param extension   副檔名 (jpg/png)
     * @return 產生結果資訊
     * @throws IOException 處理異常
     */
    public static ImageGenerationResult generateImages(InputStream inputStream, Path outputDir, String extension) throws IOException {
        // 使用 NIO.2 建立目錄
        Files.createDirectories(outputDir);

        // 讀取原始圖片取得尺寸
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("無法讀取圖片，請確認檔案格式正確");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 驗證原始圖片尺寸
        int baseWidth = ImagemapWidth.getBaseWidth();
        if (originalWidth < baseWidth) {
            log.warn("原始圖片寬度 {} 小於基準寬度 {}，圖片將被放大", originalWidth, baseWidth);
        }

        // 計算比例
        double ratio = (double) originalHeight / originalWidth;
        int baseHeight = (int) Math.round(baseWidth * ratio);

        // 取得所有需要的寬度（從大到小排序）
        List<Integer> widths = ImagemapWidth.getAllWidthsDescending();

        // 先產生最大尺寸作為基底（Thumbnails.toFile 會自動添加副檔名）
        Path baseFile = outputDir.resolve(String.valueOf(baseWidth));
        Thumbnails.of(originalImage)
                .size(baseWidth, baseHeight)
                .outputFormat(extension)
                .toFile(baseFile.toFile());

        log.info("產生基底圖片：{}x{}", baseWidth, baseHeight);

        // 從基底圖片讀取（需要加上副檔名）
        Path baseFileWithExt = outputDir.resolve(baseWidth + "." + extension);
        BufferedImage baseImage = ImageIO.read(baseFileWithExt.toFile());

        // 產生其他尺寸
        for (Integer width : widths) {
            if (width.equals(baseWidth)) {
                continue; // 基底已產生
            }

            int height = (int) Math.round(width * ratio);
            Path targetFile = outputDir.resolve(String.valueOf(width));

            Thumbnails.of(baseImage)
                    .size(width, height)
                    .outputFormat(extension)
                    .toFile(targetFile.toFile());

            log.debug("產生圖片：{}x{}", width, height);
        }

        // 重命名檔案移除副檔名（LINE Imagemap 要求）
        renameFilesToRemoveExtension(outputDir, extension);

        return new ImageGenerationResult(
                originalWidth,
                originalHeight,
                baseWidth,
                baseHeight,
                widths.size()
        );
    }

    /**
     * 產生 Imagemap 所需的所有尺寸圖片（使用預設格式）
     */
    public static ImageGenerationResult generateImages(InputStream inputStream, Path outputDir) throws IOException {
        return generateImages(inputStream, outputDir, RECOMMENDED_FORMAT);
    }

    /**
     * 重命名檔案移除副檔名
     * LINE Imagemap 的 URL 格式為 baseUrl/240，不帶副檔名
     */
    private static void renameFilesToRemoveExtension(Path outputDir, String extension) throws IOException {
        String dotExt = "." + extension;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDir)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(dotExt)) {
                    String newName = fileName.substring(0, fileName.length() - dotExt.length());
                    // 確保是我們需要的尺寸檔名
                    if (isValidWidthName(newName)) {
                        Path dest = file.resolveSibling(newName);
                        Files.deleteIfExists(dest);
                        Files.move(file, dest);
                    }
                }
            }
        }
    }

    /**
     * 檢查檔名是否為有效的寬度值
     */
    private static boolean isValidWidthName(String name) {
        try {
            int width = Integer.parseInt(name);
            return ImagemapWidth.isValidWidth(width);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 刪除 Imagemap 圖片目錄及其所有內容
     */
    public static void deleteImageDirectory(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }

        // 使用 NIO.2 遞迴刪除
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
        Files.deleteIfExists(directory);
        log.info("已刪除 Imagemap 圖片目錄：{}", directory);
    }

    /**
     * 圖片產生結果
     */
    public static class ImageGenerationResult {
        private final int originalWidth;
        private final int originalHeight;
        private final int baseWidth;
        private final int baseHeight;
        private final int generatedCount;

        public ImageGenerationResult(int originalWidth, int originalHeight, int baseWidth, int baseHeight, int generatedCount) {
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            this.baseWidth = baseWidth;
            this.baseHeight = baseHeight;
            this.generatedCount = generatedCount;
        }

        public int getOriginalWidth() { return originalWidth; }
        public int getOriginalHeight() { return originalHeight; }
        public int getBaseWidth() { return baseWidth; }
        public int getBaseHeight() { return baseHeight; }
        public int getGeneratedCount() { return generatedCount; }

        public String getOriginalSize() { return originalWidth + "x" + originalHeight; }
        public String getBaseSize() { return baseWidth + "x" + baseHeight; }
    }
}
