package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 輪播連結類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum BannerLinkType implements CodedEnum<String> {

    NONE("NONE", "無連結"),
    PRODUCT("PRODUCT", "商品詳情"),
    CATEGORY("CATEGORY", "商品分類"),
    URL("URL", "外部連結");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    public static BannerLinkType fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(BannerLinkType.class, code);
    }
}
