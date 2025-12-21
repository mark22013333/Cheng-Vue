package com.cheng.system.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaterialAssetStatus {
    ACTIVE("ACTIVE", "啟用"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String description;

    public static MaterialAssetStatus getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MaterialAssetStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
