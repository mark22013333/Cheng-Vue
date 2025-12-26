package com.cheng.line.enums;

import com.cheng.common.enums.CodedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LINE Imagemap 圖片寬度規格
 * <p>
 * LINE Imagemap 要求提供多種尺寸的圖片，
 * 客戶端會根據裝置解析度自動選擇適合的尺寸。
 *
 * @author cheng
 * @see <a href="https://developers.line.biz/en/reference/messaging-api/#imagemap-message">LINE Imagemap Message</a>
 */
@Getter
@RequiredArgsConstructor
public enum ImagemapWidth implements CodedEnum<Integer> {

    /**
     * 最小尺寸 - 適用於低解析度裝置
     */
    WIDTH_240(240, "240px"),

    /**
     * 小尺寸
     */
    WIDTH_300(300, "300px"),

    /**
     * 中尺寸
     */
    WIDTH_460(460, "460px"),

    /**
     * 大尺寸
     */
    WIDTH_700(700, "700px"),

    /**
     * 最大尺寸 - 基準尺寸
     */
    WIDTH_1040(1040, "1040px");

    private final Integer code;
    private final String description;

    /**
     * 取得基準寬度（最大尺寸）
     */
    public static int getBaseWidth() {
        return WIDTH_1040.getCode();
    }

    /**
     * 取得最小寬度
     */
    public static int getMinWidth() {
        return WIDTH_240.getCode();
    }

    /**
     * 取得所有寬度值列表（從小到大排序）
     */
    public static List<Integer> getAllWidths() {
        return Arrays.stream(values())
                .map(ImagemapWidth::getCode)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 取得所有寬度值列表（從大到小排序，用於產生圖片時先產生最大的）
     */
    public static List<Integer> getAllWidthsDescending() {
        return Arrays.stream(values())
                .map(ImagemapWidth::getCode)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * 檢查指定寬度是否為有效的 Imagemap 寬度
     */
    public static boolean isValidWidth(int width) {
        return Arrays.stream(values())
                .anyMatch(w -> w.getCode().equals(width));
    }

    /**
     * 根據寬度值取得對應的 Enum
     */
    public static ImagemapWidth fromWidth(int width) {
        return Arrays.stream(values())
                .filter(w -> w.getCode().equals(width))
                .findFirst()
                .orElse(null);
    }
}
