package com.cheng.system.mapper;

import com.cheng.system.domain.SysMaterialAsset;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMaterialAssetMapper {
    SysMaterialAsset selectSysMaterialAssetById(Long assetId);

    List<SysMaterialAsset> selectSysMaterialAssetList(SysMaterialAsset asset);

    SysMaterialAsset selectByTypeAndFileName(@Param("assetType") String assetType, @Param("fileName") String fileName);

    int insertSysMaterialAsset(SysMaterialAsset asset);

    int updateSysMaterialAsset(SysMaterialAsset asset);

    int deleteSysMaterialAssetById(Long assetId);

    int deleteSysMaterialAssetByIds(Long[] assetIds);
}
