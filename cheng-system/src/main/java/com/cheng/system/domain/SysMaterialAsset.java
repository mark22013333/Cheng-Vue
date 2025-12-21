package com.cheng.system.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class SysMaterialAsset extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long assetId;

    private String assetType;

    private String originalName;

    private String fileName;

    private String fileExt;

    private String mimeType;

    private Long fileSize;

    private Long durationMs;

    private String relativePath;

    private String status;
}
