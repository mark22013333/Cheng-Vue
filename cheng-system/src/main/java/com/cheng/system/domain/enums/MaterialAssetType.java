package com.cheng.system.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaterialAssetType {
    AUDIO("AUDIO", "音檔"),
    VIDEO("VIDEO", "影片"),
    IMAGE("IMAGE", "圖片");

    private final String code;
    private final String description;

    public static MaterialAssetType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MaterialAssetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
