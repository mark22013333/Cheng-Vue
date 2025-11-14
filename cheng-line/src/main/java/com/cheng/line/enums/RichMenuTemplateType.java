package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE Rich Menu 版型類型列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "TWO_COLS", "THREE_ROWS", "SIX_GRID", "CUSTOM"
 * <p>
 * 預設版型提供常用的區塊配置，方便快速建立選單
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum RichMenuTemplateType {

    /**
     * 左右兩格版型
     * <p>
     * 圖片尺寸: 2500x843
     * 區塊數量: 2
     * 區塊配置: 左右均分
     */
    TWO_COLS("左右兩格", "2500x843", 2),

    /**
     * 左右兩格（高版）
     * <p>
     * 圖片尺寸: 2500x1686
     * 區塊數量: 2
     * 區塊配置: 左右均分
     */
    TWO_COLS_HIGH("左右兩格（高版）", "2500x1686", 2),

    /**
     * 上下三格版型
     * <p>
     * 圖片尺寸: 2500x1686
     * 區塊數量: 3
     * 區塊配置: 上中下均分
     */
    THREE_ROWS("上下三格", "2500x1686", 3),

    /**
     * 左右三格版型
     * <p>
     * 圖片尺寸: 2500x843
     * 區塊數量: 3
     * 區塊配置: 左中右均分
     */
    THREE_COLS("左右三格", "2500x843", 3),

    /**
     * 四格版型（2x2）
     * <p>
     * 圖片尺寸: 2500x1686
     * 區塊數量: 4
     * 區塊配置: 2列2欄
     */
    FOUR_GRID("四格（2x2）", "2500x1686", 4),

    /**
     * 六格版型（2x3）
     * <p>
     * 圖片尺寸: 2500x1686
     * 區塊數量: 6
     * 區塊配置: 2列3欄
     */
    SIX_GRID("六格（2x3）", "2500x1686", 6),

    /**
     * 六格版型（3x2）
     * <p>
     * 圖片尺寸: 2500x1686
     * 區塊數量: 6
     * 區塊配置: 3列2欄
     */
    SIX_GRID_ALT("六格（3x2）", "2500x1686", 6),

    /**
     * 自訂版型
     * <p>
     * 允許使用者自由配置區塊
     */
    CUSTOM("自訂版型", null, 0);

    private final String description;
    private final String defaultImageSize;
    private final int defaultAreaCount;

    /**
     * 取得版型類型代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："TWO_COLS"
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 版型類型代碼（Enum 名稱）
     * @return Rich Menu 版型類型
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static RichMenuTemplateType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的 Rich Menu 版型類型: " + code, e);
        }
    }

    /**
     * 驗證版型類型代碼是否有效
     *
     * @param code 版型類型代碼
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        try {
            valueOf(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
