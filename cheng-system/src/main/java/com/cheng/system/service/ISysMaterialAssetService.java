package com.cheng.system.service;

import com.cheng.system.domain.SysMaterialAsset;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISysMaterialAssetService {
    SysMaterialAsset selectSysMaterialAssetById(Long assetId);

    List<SysMaterialAsset> selectSysMaterialAssetList(SysMaterialAsset asset);

    SysMaterialAsset uploadAsset(String assetType, MultipartFile file, Long durationMs, String createBy, boolean overwrite);

    SysMaterialAsset selectByTypeAndFileName(String assetType, String fileName);

    int deleteSysMaterialAssetByIds(Long[] assetIds);
}
