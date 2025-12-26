package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE Imagemap 動作類型
 *
 * @author cheng
 * @see <a href="https://developers.line.biz/en/reference/messaging-api/#imagemap-action-objects">Imagemap Action Objects</a>
 */
@Getter
@RequiredArgsConstructor
public enum ImagemapActionType implements CodedEnum<String> {

    /**
     * 開啟連結
     */
    URI("uri", "開啟連結"),

    /**
     * 發送文字
     */
    MESSAGE("message", "發送文字");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得 Enum（找不到返回 null）
     */
    public static ImagemapActionType fromCode(String code) {
        return EnumUtils.fromCode(ImagemapActionType.class, code);
    }

    /**
     * 根據代碼取得 Enum（找不到拋出異常）
     */
    public static ImagemapActionType fromCodeOrThrow(String code) {
        return EnumUtils.fromCodeOrThrow(ImagemapActionType.class, code);
    }

    /**
     * 檢查代碼是否有效
     */
    public static boolean isValidCode(String code) {
        return EnumUtils.isValidCode(ImagemapActionType.class, code);
    }
}
