package com.cheng.system.service.impl;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.file.AudioConversionUtil;
import com.cheng.system.domain.SysMaterialAsset;
import com.cheng.system.domain.enums.MaterialAssetStatus;
import com.cheng.system.domain.enums.MaterialAssetType;
import com.cheng.system.mapper.SysMaterialAssetMapper;
import com.cheng.system.service.ISysMaterialAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMaterialAssetServiceImpl implements ISysMaterialAssetService {
    private final SysMaterialAssetMapper materialAssetMapper;

    @Override
    public SysMaterialAsset selectSysMaterialAssetById(Long assetId) {
        return materialAssetMapper.selectSysMaterialAssetById(assetId);
    }

    @Override
    public SysMaterialAsset selectByTypeAndFileName(String assetType, String fileName) {
        if (StringUtils.isEmpty(assetType) || StringUtils.isEmpty(fileName)) {
            return null;
        }
        return materialAssetMapper.selectByTypeAndFileName(assetType, fileName);
    }

    @Override
    public List<SysMaterialAsset> selectSysMaterialAssetList(SysMaterialAsset asset) {
        return materialAssetMapper.selectSysMaterialAssetList(asset);
    }

    @Override
    public SysMaterialAsset uploadAsset(String assetType, MultipartFile file, Long durationMs, String createBy, boolean overwrite) {
        // 1. 基本參數驗證（類型、檔案本身）
        if (StringUtils.isEmpty(assetType)) {
            throw new ServiceException("素材類型不能為空");
        }
        MaterialAssetType type = MaterialAssetType.getByCode(assetType);
        if (type == null) {
            throw new ServiceException("不支援的素材類型：" + assetType);
        }
        if (file == null || file.isEmpty()) {
            throw new ServiceException("請選擇要上傳的檔案");
        }

        // 2. 檔名與副檔名解析（避免空檔名、避免無副檔名）
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new ServiceException("檔案名稱不能為空");
        }

        // 以檔名最後一段為準（避免瀏覽器/客戶端帶入路徑造成混淆或安全問題）
        String originalBaseFilename = Paths.get(originalFilename).getFileName().toString();

        int lastDotIndex = originalBaseFilename.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex >= originalBaseFilename.length() - 1) {
            throw new ServiceException("無效的檔案格式");
        }

        String fileExt = originalBaseFilename.substring(lastDotIndex + 1);
        String baseName = originalBaseFilename.substring(0, lastDotIndex);

        // 檔名規則：{yyyyMMdd}_{檔名}.{ext}
        // 注意：目錄固定為英文；檔名允許保留原檔名，但需移除路徑分隔符避免跨平台問題
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String safeBaseName = baseName.replace("/", "_").replace("\\", "_");
        String storedFileName = yyyyMMdd + "_" + safeBaseName + "." + fileExt;

        // 3. 目錄規則：${cheng.profile}/material/{audio|video|image}/
        String typeDir = type.getCode().toLowerCase();
        Path uploadDir = Paths.get(CoolAppsConfig.getMaterialPath(), typeDir);
        try {
            Files.createDirectories(uploadDir);

            // 3.1. 重複檔名檢查（以同素材類型 + 儲存檔名為準）
            SysMaterialAsset existed = materialAssetMapper.selectByTypeAndFileName(type.getCode(), storedFileName);
            if (existed != null && !overwrite) {
                throw new ServiceException("檔名已存在，請確認是否覆蓋");
            }

            // 4. 路徑安全：resolve + normalize 後確認仍在 materialRoot 之下，避免路徑穿越
            Path targetPath = uploadDir.resolve(storedFileName).normalize();
            Path materialRoot = Paths.get(CoolAppsConfig.getMaterialPath()).normalize();
            if (!targetPath.startsWith(materialRoot)) {
                throw new ServiceException("非法檔案路徑");
            }

            // 5. 寫入檔案（NIO.2，並使用 try-with-resources 管理 InputStream）
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 5.1. 音檔格式轉換：webm/opus/ogg -> mp3（LINE 只支援 mp3/m4a）
            // 注意：前端錄音已在上傳前轉換為 mp3，此處為備援機制
            String finalFileExt = fileExt;
            String finalStoredFileName = storedFileName;
            Path finalTargetPath = targetPath;
            long finalFileSize = file.getSize();
            String finalMimeType = file.getContentType();

            if (type == MaterialAssetType.AUDIO && AudioConversionUtil.needsConversion(fileExt)) {
                log.info("偵測到需要轉換的音檔格式: {}，嘗試轉換為 mp3", fileExt);

                // 檢查 FFmpeg 是否可用
                if (AudioConversionUtil.isFfmpegAvailable()) {
                    // 產生轉換後的檔名和路徑
                    String mp3FileName = storedFileName.substring(0, storedFileName.lastIndexOf('.')) + ".mp3";
                    Path mp3Path = uploadDir.resolve(mp3FileName).normalize();

                    try {
                        // 執行轉換
                        AudioConversionUtil.convertWebmToMp3(targetPath, mp3Path);

                        // 刪除原始 webm 檔案
                        Files.deleteIfExists(targetPath);

                        // 更新檔案資訊
                        finalFileExt = "mp3";
                        finalStoredFileName = mp3FileName;
                        finalTargetPath = mp3Path;
                        finalFileSize = Files.size(mp3Path);
                        finalMimeType = "audio/mpeg";

                        log.info("音檔轉換完成: {} -> {}", storedFileName, mp3FileName);
                    } catch (Exception e) {
                        log.warn("後端音檔轉換失敗，保留原始格式: {}", e.getMessage());
                        // 繼續使用原始檔案
                    }
                } else {
                    log.warn("FFmpeg 未安裝，無法進行後端音檔轉換，保留原始格式: {}", fileExt);
                    // 繼續使用原始檔案（前端應該已經轉換過了）
                }
            }

            // 6. DB 僅保存相對路徑：material/**（對外 URL 由 /profile/** 靜態映射提供）
            SysMaterialAsset asset = new SysMaterialAsset();
            asset.setAssetType(type.getCode());
            asset.setOriginalName(originalBaseFilename);
            asset.setFileName(finalStoredFileName);
            asset.setFileExt(finalFileExt);
            asset.setMimeType(finalMimeType);
            asset.setFileSize(finalFileSize);
            asset.setDurationMs(durationMs);
            asset.setRelativePath("material/" + typeDir + "/" + finalStoredFileName);
            asset.setStatus(MaterialAssetStatus.ACTIVE.getCode());

            // 7. DB 寫入規則
            // - 不覆蓋：insert
            // - 覆蓋：update（保留 asset_id，不新增資料）
            if (existed == null) {
                asset.setCreateBy(createBy);
                materialAssetMapper.insertSysMaterialAsset(asset);
                return asset;
            }

            asset.setAssetId(existed.getAssetId());
            asset.setUpdateBy(createBy);
            materialAssetMapper.updateSysMaterialAsset(asset);
            return materialAssetMapper.selectSysMaterialAssetById(existed.getAssetId());

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("素材上傳失敗 - type: {}, file: {}", assetType, originalFilename, e);
            throw new ServiceException("素材上傳失敗：" + e.getMessage());
        }
    }

    @Override
    public int deleteSysMaterialAssetByIds(Long[] assetIds) {
        if (assetIds == null || assetIds.length == 0) {
            return 0;
        }

        for (Long assetId : assetIds) {
            // 1. 以 DB 為準取得相對路徑；不存在的 ID 直接略過
            SysMaterialAsset asset = materialAssetMapper.selectSysMaterialAssetById(assetId);
            if (asset == null) {
                continue;
            }

            String relativePath = asset.getRelativePath();
            if (StringUtils.isNotEmpty(relativePath)) {
                try {
                    // 2. 路徑安全：僅允許刪除 ${cheng.profile} 之下的檔案
                    Path profileRoot = Paths.get(CoolAppsConfig.getProfile()).normalize();
                    Path filePath = profileRoot.resolve(relativePath).normalize();
                    if (filePath.startsWith(profileRoot)) {
                        // 3. NIO.2 刪檔；檔案不存在視為成功（避免因檔案缺失阻塞 DB 刪除）
                        Files.deleteIfExists(filePath);
                    }
                } catch (Exception e) {
                    // 4. 檔案刪除失敗不影響 DB 刪除：避免權限/檔案鎖定造成整體刪除中斷
                    log.warn("刪除素材檔案失敗 - assetId: {}, path: {}", assetId, relativePath, e);
                }
            }

            // 5. 刪除 DB 記錄
            materialAssetMapper.deleteSysMaterialAssetById(assetId);
        }

        return assetIds.length;
    }
}
