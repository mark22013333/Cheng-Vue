package com.cheng.web.controller.system;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.system.domain.SysMaterialAsset;
import com.cheng.system.service.ISysMaterialAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/system/material")
public class SysMaterialController extends BaseController {

    @Autowired
    private ISysMaterialAssetService materialAssetService;

    @PreAuthorize("@ss.hasPermi('system:material:audio:list')")
    @GetMapping("/audio/list")
    public TableDataInfo listAudio(SysMaterialAsset asset) {
        asset.setAssetType("AUDIO");
        startPage();
        List<SysMaterialAsset> list = materialAssetService.selectSysMaterialAssetList(asset);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:material:video:list')")
    @GetMapping("/video/list")
    public TableDataInfo listVideo(SysMaterialAsset asset) {
        asset.setAssetType("VIDEO");
        startPage();
        List<SysMaterialAsset> list = materialAssetService.selectSysMaterialAssetList(asset);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:material:image:list')")
    @GetMapping("/image/list")
    public TableDataInfo listImage(SysMaterialAsset asset) {
        asset.setAssetType("IMAGE");
        startPage();
        List<SysMaterialAsset> list = materialAssetService.selectSysMaterialAssetList(asset);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:material:audio:query') or @ss.hasPermi('system:material:video:query') or @ss.hasPermi('system:material:image:query')")
    @GetMapping(value = "/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId) {
        return success(materialAssetService.selectSysMaterialAssetById(assetId));
    }

    @PreAuthorize("@ss.hasPermi('system:material:audio:upload')")
    @Log(title = "素材管理-音檔", businessType = BusinessType.INSERT)
    @PostMapping("/audio/upload")
    public AjaxResult uploadAudio(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "durationMs", required = false) Long durationMs,
                                 @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite) {
        SysMaterialAsset asset = materialAssetService.uploadAsset("AUDIO", file, durationMs, getUsername(), overwrite);
        return success(buildUploadResult(asset));
    }

    @PreAuthorize("@ss.hasPermi('system:material:video:upload')")
    @Log(title = "素材管理-影片", businessType = BusinessType.INSERT)
    @PostMapping("/video/upload")
    public AjaxResult uploadVideo(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "durationMs", required = false) Long durationMs,
                                 @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite) {
        SysMaterialAsset asset = materialAssetService.uploadAsset("VIDEO", file, durationMs, getUsername(), overwrite);
        return success(buildUploadResult(asset));
    }

    @PreAuthorize("@ss.hasPermi('system:material:image:upload')")
    @Log(title = "素材管理-圖片", businessType = BusinessType.INSERT)
    @PostMapping("/image/upload")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite) {
        SysMaterialAsset asset = materialAssetService.uploadAsset("IMAGE", file, null, getUsername(), overwrite);
        return success(buildUploadResult(asset));
    }

    @PreAuthorize("@ss.hasPermi('system:material:audio:upload')")
    @GetMapping("/audio/exists")
    public AjaxResult existsAudio(@RequestParam("originalName") String originalName) {
        String storedFileName = buildStoredFileName(originalName);
        return success(materialAssetService.selectByTypeAndFileName("AUDIO", storedFileName) != null);
    }

    @PreAuthorize("@ss.hasPermi('system:material:video:upload')")
    @GetMapping("/video/exists")
    public AjaxResult existsVideo(@RequestParam("originalName") String originalName) {
        String storedFileName = buildStoredFileName(originalName);
        return success(materialAssetService.selectByTypeAndFileName("VIDEO", storedFileName) != null);
    }

    @PreAuthorize("@ss.hasPermi('system:material:image:upload')")
    @GetMapping("/image/exists")
    public AjaxResult existsImage(@RequestParam("originalName") String originalName) {
        String storedFileName = buildStoredFileName(originalName);
        return success(materialAssetService.selectByTypeAndFileName("IMAGE", storedFileName) != null);
    }

    private String buildStoredFileName(String originalName) {
        if (originalName == null) {
            return "";
        }

        String originalBaseFilename = Paths.get(originalName).getFileName().toString();
        int lastDotIndex = originalBaseFilename.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex >= originalBaseFilename.length() - 1) {
            return "";
        }

        String fileExt = originalBaseFilename.substring(lastDotIndex + 1);
        String baseName = originalBaseFilename.substring(0, lastDotIndex);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String safeBaseName = baseName.replace("/", "_").replace("\\", "_");
        return yyyyMMdd + "_" + safeBaseName + "." + fileExt;
    }

    @PreAuthorize("@ss.hasPermi('system:material:audio:remove')")
    @Log(title = "素材管理-音檔", businessType = BusinessType.DELETE)
    @DeleteMapping("/audio/{assetIds}")
    public AjaxResult removeAudio(@PathVariable Long[] assetIds) {
        return toAjax(materialAssetService.deleteSysMaterialAssetByIds(assetIds));
    }

    @PreAuthorize("@ss.hasPermi('system:material:video:remove')")
    @Log(title = "素材管理-影片", businessType = BusinessType.DELETE)
    @DeleteMapping("/video/{assetIds}")
    public AjaxResult removeVideo(@PathVariable Long[] assetIds) {
        return toAjax(materialAssetService.deleteSysMaterialAssetByIds(assetIds));
    }

    @PreAuthorize("@ss.hasPermi('system:material:image:remove')")
    @Log(title = "素材管理-圖片", businessType = BusinessType.DELETE)
    @DeleteMapping("/image/{assetIds}")
    public AjaxResult removeImage(@PathVariable Long[] assetIds) {
        return toAjax(materialAssetService.deleteSysMaterialAssetByIds(assetIds));
    }

    private Map<String, Object> buildUploadResult(SysMaterialAsset asset) {
        Map<String, Object> result = new HashMap<>();
        result.put("assetId", asset.getAssetId());
        result.put("assetType", asset.getAssetType());
        result.put("originalName", asset.getOriginalName());
        result.put("fileName", asset.getFileName());
        result.put("fileExt", asset.getFileExt());
        result.put("mimeType", asset.getMimeType());
        result.put("fileSize", asset.getFileSize());
        result.put("durationMs", asset.getDurationMs());
        result.put("relativePath", asset.getRelativePath());
        result.put("url", "/profile/" + asset.getRelativePath());
        return result;
    }
}
