package com.cheng.common.utils.file;

import com.cheng.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * 音檔格式轉換工具類
 * 使用 FFmpeg 進行音檔格式轉換
 * <p>
 * 需求背景：LINE Messaging API 只支援 mp3 和 m4a 格式的音訊訊息
 * 而瀏覽器錄音（MediaRecorder）預設產生 webm 格式
 * <p>
 * 使用前提：系統需安裝 FFmpeg
 * - macOS: brew install ffmpeg
 * - Ubuntu: sudo apt install ffmpeg
 * - Windows: 下載 FFmpeg 並加入 PATH
 *
 * @author cheng
 */
@Slf4j
public class AudioConversionUtil {

    private static final int CONVERSION_TIMEOUT_SECONDS = 60;

    /**
     * 檢查 FFmpeg 是否可用
     *
     * @return true: FFmpeg 已安裝且可用
     */
    public static boolean isFfmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            if (completed) {
                int exitCode = process.exitValue();
                return exitCode == 0;
            }
            process.destroyForcibly();
            return false;
        } catch (Exception e) {
            log.warn("FFmpeg 不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 將 webm 音檔轉換為 mp3 格式
     *
     * @param inputPath  輸入檔案路徑（webm 格式）
     * @param outputPath 輸出檔案路徑（mp3 格式）
     * @return 轉換後的檔案路徑
     * @throws ServiceException 轉換失敗時拋出
     */
    public static Path convertWebmToMp3(Path inputPath, Path outputPath) throws ServiceException {
        return convertAudio(inputPath, outputPath, "mp3", "libmp3lame", "192k");
    }

    /**
     * 將 webm 音檔轉換為 m4a 格式
     *
     * @param inputPath  輸入檔案路徑（webm 格式）
     * @param outputPath 輸出檔案路徑（m4a 格式）
     * @return 轉換後的檔案路徑
     * @throws ServiceException 轉換失敗時拋出
     */
    public static Path convertWebmToM4a(Path inputPath, Path outputPath) throws ServiceException {
        return convertAudio(inputPath, outputPath, "ipod", "aac", "192k");
    }

    /**
     * 通用音檔轉換方法
     *
     * @param inputPath    輸入檔案路徑
     * @param outputPath   輸出檔案路徑
     * @param format       輸出格式（如 mp3, ipod）
     * @param audioCodec   音訊編碼器（如 libmp3lame, aac）
     * @param audioBitrate 音訊位元率（如 192k）
     * @return 轉換後的檔案路徑
     * @throws ServiceException 轉換失敗時拋出
     */
    public static Path convertAudio(Path inputPath, Path outputPath, String format,
                                    String audioCodec, String audioBitrate) throws ServiceException {
        if (!Files.exists(inputPath)) {
            throw new ServiceException("輸入檔案不存在: " + inputPath);
        }

        if (!isFfmpegAvailable()) {
            throw new ServiceException("FFmpeg 未安裝或無法執行，無法進行音檔轉換");
        }

        try {
            // 確保輸出目錄存在
            Files.createDirectories(outputPath.getParent());

            // 建構 FFmpeg 命令
            // ffmpeg -i input.webm -f mp3 -acodec libmp3lame -ab 192k -y output.mp3
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputPath.toString(),
                    "-f", format,
                    "-acodec", audioCodec,
                    "-ab", audioBitrate,
                    "-y",  // 覆蓋輸出檔案
                    outputPath.toString()
            );

            pb.redirectErrorStream(true);
            log.info("開始音檔轉換: {} -> {}", inputPath.getFileName(), outputPath.getFileName());

            Process process = pb.start();

            // 讀取輸出（避免緩衝區滿導致程式卡住）
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean completed = process.waitFor(CONVERSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new ServiceException("音檔轉換逾時（超過 " + CONVERSION_TIMEOUT_SECONDS + " 秒）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("FFmpeg 轉換失敗，退出碼: {}，輸出: {}", exitCode, output);
                throw new ServiceException("音檔轉換失敗，FFmpeg 退出碼: " + exitCode);
            }

            if (!Files.exists(outputPath)) {
                throw new ServiceException("音檔轉換失敗：輸出檔案未產生");
            }

            log.info("音檔轉換成功: {} ({} bytes)", outputPath.getFileName(), Files.size(outputPath));
            return outputPath;

        } catch (ServiceException e) {
            throw e;
        } catch (IOException e) {
            log.error("音檔轉換 IO 錯誤", e);
            throw new ServiceException("音檔轉換失敗: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("音檔轉換被中斷");
        }
    }

    /**
     * 判斷檔案副檔名是否為需要轉換的格式
     *
     * @param fileExt 檔案副檔名（不含點）
     * @return true: 需要轉換
     */
    public static boolean needsConversion(String fileExt) {
        if (fileExt == null) {
            return false;
        }
        String ext = fileExt.toLowerCase();
        // webm, opus, ogg 等瀏覽器常見格式都需要轉換
        return "webm".equals(ext) || "opus".equals(ext) || "ogg".equals(ext);
    }

    /**
     * 判斷檔案副檔名是否為 LINE 支援的音訊格式
     *
     * @param fileExt 檔案副檔名（不含點）
     * @return true: LINE 支援
     */
    public static boolean isLineSupportedFormat(String fileExt) {
        if (fileExt == null) {
            return false;
        }
        String ext = fileExt.toLowerCase();
        // LINE 只支援 mp3 和 m4a
        return "mp3".equals(ext) || "m4a".equals(ext);
    }
}
