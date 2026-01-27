package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 第三方登入平台列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum SocialProvider implements CodedEnum<String> {

    LINE("LINE", "LINE"),
    GOOGLE("GOOGLE", "Google"),
    FACEBOOK("FACEBOOK", "Facebook");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    public static SocialProvider fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(SocialProvider.class, code);
    }
}
