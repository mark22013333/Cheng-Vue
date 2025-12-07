package com.cheng.quartz.task;

import com.cheng.common.utils.JacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 檔案維護定時任務
 *
 * <p>用於清理系統中的備份檔案、暫存檔案等，避免磁碟空間浪費
 *
 * <p><b>支援的維護類型：</b>
 * <ul>
 *   <li>BACKUP_FILES - 清理圖片備份檔案（.backup.* 格式）</li>
 *   <li>TEMP_FILES - 清理暫存檔案（未來擴展）</li>
 * </ul>
 *
 * <p><b>Quartz 設定範例：</b>
 * <pre>
 * Bean名稱: fileMaintenanceTask
 * 方法名稱: cleanupFiles
 * 參數: {"taskType":"BACKUP_FILES","retentionDays":30}
 * </pre>
 *
 * @author cheng
 * @since 2025-12-07
 */
@Slf4j
@Component("fileMaintenanceTask")
@RequiredArgsConstructor
public class FileMaintenanceTask {

    /**
     * 檔案上傳根路徑
     */
    @Value("${cheng.profile}")
    private String uploadPath;

    /**
     * 清理檔案
     *
     * @param params JSON 參數，包含：
     *               - taskType: 任務類型（BACKUP_FILES, TEMP_FILES）
     *               - retentionDays: 保留天數（超過此天數的檔案才會被刪除）
     *               - targetPath: 目標路徑（相對於 uploadPath，可選）
     */
    public void cleanupFiles(String params) {
        try {
            log.info("開始執行檔案清理任務，參數: {}", params);

            // 解析參數
            JsonNode jsonParams = JacksonUtil.toJsonNode(params);
            String taskType = jsonParams.has("taskType") ? jsonParams.get("taskType").asText() : "BACKUP_FILES";
            int retentionDays = jsonParams.has("retentionDays") ? jsonParams.get("retentionDays").asInt() : 30;
            String targetPath = jsonParams.has("targetPath") ? jsonParams.get("targetPath").asText() : "";

            // 建立完整路徑
            Path basePath = Paths.get(uploadPath);
            if (!targetPath.isEmpty()) {
                basePath = basePath.resolve(targetPath);
            }

            if (!Files.exists(basePath)) {
                log.warn("目標路徑不存在: {}", basePath);
                return;
            }

            // 根據任務類型執行清理
            switch (taskType) {
                case "BACKUP_FILES":
                    cleanupBackupFiles(basePath, retentionDays);
                    break;
                case "TEMP_FILES":
                    cleanupTempFiles(basePath, retentionDays);
                    break;
                default:
                    log.warn("不支援的任務類型: {}", taskType);
            }

        } catch (Exception e) {
            log.error("執行檔案清理任務失敗", e);
        }
    }

    /**
     * 清理備份檔案
     * 格式：原檔名.backup.時間戳記
     * 例如：isbn_9789864345106.jpg.backup.1765121386408
     *
     * @param basePath      基礎路徑
     * @param retentionDays 保留天數
     */
    private void cleanupBackupFiles(Path basePath, int retentionDays) {
        try {
            log.info("開始清理備份檔案，路徑: {}, 保留天數: {}", basePath, retentionDays);

            List<Path> deletedFiles = new ArrayList<>();
            long totalSize = 0;

            // 遞迴查找所有備份檔案
            List<Path> backupFiles = findBackupFiles(basePath);

            // 計算過期時間點
            Instant cutoffTime = Instant.now().minus(Duration.ofDays(retentionDays));

            for (Path file : backupFiles) {
                try {
                    // 取得檔案最後修改時間
                    FileTime lastModifiedTime = Files.getLastModifiedTime(file);

                    if (lastModifiedTime.toInstant().isBefore(cutoffTime)) {
                        long fileSize = Files.size(file);
                        Files.delete(file);
                        deletedFiles.add(file);
                        totalSize += fileSize;
                        log.debug("已刪除過期備份檔案: {}, 大小: {} bytes", file, fileSize);
                    }
                } catch (IOException e) {
                    log.warn("無法刪除檔案: {}", file, e);
                }
            }

            log.info("備份檔案清理完成 - 共刪除 {} 個檔案，釋放空間: {} MB",
                    deletedFiles.size(), String.format("%.2f", totalSize / 1024.0 / 1024.0));

            // 記錄前 5 個刪除的檔案
            if (!deletedFiles.isEmpty()) {
                log.info("已刪除的檔案範例（最多顯示 5 個）：");
                deletedFiles.stream().limit(5).forEach(f -> log.info("  - {}", f.getFileName()));
            }

        } catch (Exception e) {
            log.error("清理備份檔案失敗", e);
        }
    }

    /**
     * 遞迴查找所有備份檔案
     *
     * @param directory 搜尋目錄
     * @return 備份檔案列表
     */
    private List<Path> findBackupFiles(Path directory) throws IOException {
        List<Path> backupFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    // 遞迴搜尋子目錄
                    backupFiles.addAll(findBackupFiles(entry));
                } else if (isBackupFile(entry)) {
                    backupFiles.add(entry);
                }
            }
        }

        return backupFiles;
    }

    /**
     * 判斷是否為備份檔案
     * 格式：*.backup.*
     *
     * @param file 檔案路徑
     * @return 是否為備份檔案
     */
    private boolean isBackupFile(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.contains(".backup.");
    }

    /**
     * 清理暫存檔案（預留方法，未來擴展）
     *
     * @param basePath      基礎路徑
     * @param retentionDays 保留天數
     */
    private void cleanupTempFiles(Path basePath, int retentionDays) {
        log.info("清理暫存檔案功能尚未實作");
    }
}
