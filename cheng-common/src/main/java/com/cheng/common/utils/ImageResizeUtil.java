package com.cheng.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 圖片調整工具類
 * 支援智慧調整圖片尺寸，保持品質
 *
 * @author cheng
 */
@Slf4j
public class ImageResizeUtil {

    /**
     * 調整模式
     */
    public enum ResizeMode {
        /**
         * 拉伸：直接縮放到目標尺寸（可能變形）
         */
        STRETCH,
        /**
         * 裁切：保持比例，裁切多餘部分（居中裁切）
         */
        CROP,
        /**
         * 包含：保持比例，縮放到適合目標尺寸（可能有留白）
         */
        CONTAIN,
        /**
         * 覆蓋：保持比例，填滿目標尺寸（可能裁切）
         */
        COVER
    }

    /**
     * 調整圖片到指定尺寸
     *
     * @param imageBytes   原始圖片位元組
     * @param targetWidth  目標寬度
     * @param targetHeight 目標高度
     * @param mode         調整模式
     * @return 調整後的圖片位元組
     * @throws IOException IO 異常
     */
    public static byte[] resize(byte[] imageBytes, int targetWidth, int targetHeight, ResizeMode mode)
            throws IOException {

        log.info("開始調整圖片尺寸：目標 {}x{}，模式：{}", targetWidth, targetHeight, mode);

        // 讀取原始圖片
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("無法讀取圖片");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        log.info("原始圖片尺寸：{}x{}", originalWidth, originalHeight);

        // 如果尺寸已經符合，直接返回
        if (originalWidth == targetWidth && originalHeight == targetHeight) {
            log.info("圖片尺寸已符合，無需調整");
            return imageBytes;
        }

        BufferedImage resizedImage = switch (mode) {
            case STRETCH -> resizeStretch(originalImage, targetWidth, targetHeight);
            case CROP -> resizeCrop(originalImage, targetWidth, targetHeight);
            case CONTAIN -> resizeContain(originalImage, targetWidth, targetHeight);
            case COVER -> resizeCover(originalImage, targetWidth, targetHeight);
        };

        // 轉換為位元組陣列
        return imageToBytes(resizedImage, getImageFormat(imageBytes));
    }

    /**
     * 拉伸模式：直接縮放
     */
    private static BufferedImage resizeStretch(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();

        // 設定高品質縮放
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resized;
    }

    /**
     * 裁切模式：保持比例，居中裁切
     */
    private static BufferedImage resizeCrop(BufferedImage original, int targetWidth, int targetHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // 計算縮放比例（選擇較大的比例以填滿目標尺寸）
        double scaleWidth = (double) targetWidth / originalWidth;
        double scaleHeight = (double) targetHeight / originalHeight;
        double scale = Math.max(scaleWidth, scaleHeight);

        // 計算縮放後的尺寸（使用 Math.ceil 確保不會太小）
        int scaledWidth = (int) Math.ceil(originalWidth * scale);
        int scaledHeight = (int) Math.ceil(originalHeight * scale);

        // 先縮放
        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g1 = scaled.createGraphics();
        g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g1.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g1.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g1.dispose();

        // 計算裁切位置（居中）
        int x = (scaledWidth - targetWidth) / 2;
        int y = (scaledHeight - targetHeight) / 2;

        // 確保裁切座標和尺寸不超出範圍
        x = Math.max(0, Math.min(x, scaledWidth - targetWidth));
        y = Math.max(0, Math.min(y, scaledHeight - targetHeight));
        
        // 確保裁切尺寸不超出圖片邊界
        int cropWidth = Math.min(targetWidth, scaledWidth - x);
        int cropHeight = Math.min(targetHeight, scaledHeight - y);

        // 裁切
        BufferedImage cropped = scaled.getSubimage(x, y, cropWidth, cropHeight);
        
        // 如果裁切後的尺寸不符合目標尺寸（由於邊界限制），再次縮放到目標尺寸
        if (cropWidth != targetWidth || cropHeight != targetHeight) {
            BufferedImage result = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = result.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(cropped, 0, 0, targetWidth, targetHeight, null);
            g2.dispose();
            return result;
        }
        
        return cropped;
    }

    /**
     * 包含模式：保持比例，可能有留白
     */
    private static BufferedImage resizeContain(BufferedImage original, int targetWidth, int targetHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // 計算縮放比例（選擇較小的比例以完全包含）
        double scaleWidth = (double) targetWidth / originalWidth;
        double scaleHeight = (double) targetHeight / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        // 建立目標尺寸的白色背景圖
        BufferedImage result = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();

        // 填充白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);

        // 計算居中位置
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        // 繪製縮放後的圖片
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, x, y, scaledWidth, scaledHeight, null);
        g.dispose();

        return result;
    }

    /**
     * 覆蓋模式：保持比例，填滿目標尺寸（同裁切模式）
     */
    private static BufferedImage resizeCover(BufferedImage original, int targetWidth, int targetHeight) {
        return resizeCrop(original, targetWidth, targetHeight);
    }

    /**
     * 將 BufferedImage 轉換為位元組陣列
     */
    private static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 使用高品質 JPEG 編碼
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();

                // 設定壓縮品質（0.9 = 90% 品質）
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.9f);

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(image, null, null), param);
                }
                writer.dispose();
            } else {
                ImageIO.write(image, "jpg", baos);
            }
        } else {
            ImageIO.write(image, format, baos);
        }

        byte[] result = baos.toByteArray();
        log.info("調整後圖片大小：{} bytes ({} KB)", result.length, result.length / 1024);

        return result;
    }

    /**
     * 偵測圖片格式
     */
    private static String getImageFormat(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                return "jpg"; // 預設 JPEG
            }

            // 嘗試從位元組判斷格式
            if (imageBytes.length >= 3) {
                // PNG: 89 50 4E 47
                if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E) {
                    return "png";
                }
                // JPEG: FF D8 FF
                if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
                    return "jpg";
                }
            }
        }

        return "jpg"; // 預設返回 JPEG
    }

    /**
     * 檢查調整後的圖片是否超過大小限制
     *
     * @param imageBytes   圖片位元組
     * @param maxSizeBytes 最大大小（位元組）
     * @return 是否超過限制
     */
    public static boolean exceedsSize(byte[] imageBytes, long maxSizeBytes) {
        return imageBytes.length > maxSizeBytes;
    }

    /**
     * 壓縮圖片到指定大小以下
     *
     * @param imageBytes   原始圖片
     * @param maxSizeBytes 最大大小
     * @return 壓縮後的圖片
     * @throws IOException IO 異常
     */
    public static byte[] compressToSize(byte[] imageBytes, long maxSizeBytes) throws IOException {
        if (imageBytes.length <= maxSizeBytes) {
            return imageBytes;
        }

        log.info("圖片超過大小限制，開始壓縮：{} bytes -> 目標 {} bytes", imageBytes.length, maxSizeBytes);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        String format = getImageFormat(imageBytes);

        // 嘗試不同的壓縮品質
        float[] qualities = {0.8f, 0.7f, 0.6f, 0.5f, 0.4f};

        for (float quality : qualities) {
            byte[] compressed = compressWithQuality(image, format, quality);
            if (compressed.length <= maxSizeBytes) {
                log.info("壓縮成功：品質 {}，大小 {} bytes", quality, compressed.length);
                return compressed;
            }
        }

        throw new IOException("無法將圖片壓縮到指定大小以下");
    }

    /**
     * 以指定品質壓縮圖片
     */
    private static byte[] compressWithQuality(BufferedImage image, String format, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(image, null, null), param);
                }
                writer.dispose();
            }
        } else {
            ImageIO.write(image, format, baos);
        }

        return baos.toByteArray();
    }
}
