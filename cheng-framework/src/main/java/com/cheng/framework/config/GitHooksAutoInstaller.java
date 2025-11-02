package com.cheng.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.cheng.framework.config.Color.*;

/**
 * Git Hooks 自動安裝器
 * 在應用程式啟動時自動安裝專案的 Git Hooks
 *
 * @author cheng
 */
@Slf4j
@Component
@Order(1) // 優先執行
public class GitHooksAutoInstaller implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 取得專案根目錄
            Path projectRoot = findProjectRoot();
            if (projectRoot == null) {
                log.debug("無法找到專案根目錄，跳過 Git Hooks 安裝");
                return;
            }

            Path gitHooksSource = projectRoot.resolve(".githooks");
            Path gitHooksDest = projectRoot.resolve(".git/hooks");

            // 檢查來源目錄是否存在
            if (!Files.exists(gitHooksSource)) {
                log.debug("找不到 .githooks 目錄，跳過 Git Hooks 安裝");
                return;
            }

            // 檢查目標目錄是否存在
            if (!Files.exists(gitHooksDest)) {
                log.warn("找不到 .git/hooks 目錄，可能不是 Git 儲存庫");
                return;
            }

            log.info("{}════════════════════════════════════════════════════{}", ANSI_CYAN, ANSI_RESET);
            log.info("{}  Git Hooks 自動安裝{}", ANSI_CYAN, ANSI_RESET);
            log.info("{}════════════════════════════════════════════════════{}", ANSI_CYAN, ANSI_RESET);

            // 安裝所有 hooks
            List<String> installedHooks = installHooks(gitHooksSource, gitHooksDest);

            if (!installedHooks.isEmpty()) {
                log.info("{}==>成功安裝 {} 個 Git Hook(s){}", ANSI_GREEN, installedHooks.size(), ANSI_RESET);
                log.info("{}已安裝的 Hooks：{}", ANSI_BLUE, ANSI_RESET);
                installedHooks.forEach(hook -> log.info("  - {}", hook));

                log.info("");
                log.info("{}=>提示：{}", ANSI_BLUE, ANSI_RESET);
                log.info("  - pre-commit:  在提交前驗證 Mapper");
                log.info("  - commit-msg:  驗證 commit message 格式");
                log.info("  - 如需跳過驗證: git commit --no-verify");
            } else {
                log.info("{}所有 Git Hooks 已是最新狀態{}", ANSI_BLUE, ANSI_RESET);
            }

            log.info("{}════════════════════════════════════════════════════{}", ANSI_CYAN, ANSI_RESET);

        } catch (Exception e) {
            log.warn("Git Hooks 自動安裝失敗: {}", e.getMessage());
            log.debug("詳細錯誤", e);
        }
    }

    /**
     * 安裝 Git Hooks
     */
    private List<String> installHooks(Path source, Path dest) throws IOException {
        List<String> installed = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
            for (Path hookFile : stream) {
                String fileName = hookFile.getFileName().toString();

                // 跳過非 hook 檔案
                if (fileName.startsWith(".") ||
                        fileName.startsWith("README") ||
                        fileName.endsWith(".md") ||
                        Files.isDirectory(hookFile)) {
                    continue;
                }

                Path destFile = dest.resolve(fileName);

                // 檢查是否需要更新
                if (needsUpdate(hookFile, destFile)) {
                    // 複製檔案
                    Files.copy(hookFile, destFile, StandardCopyOption.REPLACE_EXISTING);

                    // 設定執行權限 (Unix/Linux/Mac)
                    setExecutable(destFile);

                    installed.add(fileName);
                    log.debug("{}安裝 {}{}", ANSI_GREEN, fileName, ANSI_RESET);
                }
            }
        }

        return installed;
    }

    /**
     * 檢查檔案是否需要更新
     */
    private boolean needsUpdate(Path source, Path dest) throws IOException {
        if (!Files.exists(dest)) {
            return true;
        }

        // 比較檔案修改時間
        long sourceTime = Files.getLastModifiedTime(source).toMillis();
        long destTime = Files.getLastModifiedTime(dest).toMillis();

        return sourceTime > destTime;
    }

    /**
     * 設定檔案為可執行
     */
    private void setExecutable(Path file) {
        try {
            File f = file.toFile();
            f.setExecutable(true, false);
        } catch (Exception e) {
            log.debug("無法設定執行權限: {}", e.getMessage());
        }
    }

    /**
     * 尋找專案根目錄（包含 .git 目錄的位置）
     */
    private Path findProjectRoot() {
        try {
            // 從當前工作目錄開始往上找
            Path current = Paths.get("").toAbsolutePath();

            while (current != null) {
                Path gitDir = current.resolve(".git");
                if (Files.exists(gitDir) && Files.isDirectory(gitDir)) {
                    return current;
                }
                current = current.getParent();
            }
        } catch (Exception e) {
            log.debug("尋找專案根目錄時發生錯誤", e);
        }

        return null;
    }
}
