package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum CommonStatus implements CodedEnum<String> {

    ENABLED("ENABLED", "啟用"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否啟用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 取得狀態對應的顏色
     */
    public String getColor() {
        return switch (this) {
            case ENABLED -> "#67C23A";
            case DISABLED -> "#909399";
        };
    }

    /**
     * 從代碼轉換（支援舊資料格式 1/0）
     */
    public static CommonStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        // 支援舊資料格式
        if ("1".equals(code)) {
            return ENABLED;
        }
        if ("0".equals(code)) {
            return DISABLED;
        }
        return EnumUtils.fromCodeOrThrow(CommonStatus.class, code);
    }
}
