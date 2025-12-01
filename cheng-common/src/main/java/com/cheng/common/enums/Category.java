package com.cheng.common.enums;

import com.cheng.common.utils.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 物品分類列舉
 * <p>
 * ⚠️ 同步來源: V2__init_inventory_data.sql - category_code 必須匹配
 * 如資料庫 schema 變更必須同步更新此 Enum
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum Category implements CodedEnum<String> {

    /**
     * 書籍
     */
    BOOK("BOOK", "書籍"),

    /**
     * 辦公用品
     */
    OFFICE("OFFICE", "辦公用品"),

    /**
     * 文具用品
     */
    STATIONERY("STATIONERY", "文具用品"),

    /**
     * 辦公設備
     */
    EQUIPMENT("EQUIPMENT", "辦公設備"),

    /**
     * 電子產品
     */
    ELECTRONICS("ELECTRONICS", "電子產品"),

    /**
     * 電腦設備
     */
    COMPUTER("COMPUTER", "電腦設備"),

    /**
     * 網路設備
     */
    NETWORK("NETWORK", "網路設備"),

    /**
     * 家具用品
     */
    FURNITURE("FURNITURE", "家具用品"),

    /**
     * 桌椅
     */
    DESK_CHAIR("DESK_CHAIR", "桌椅"),

    /**
     * 收納用品
     */
    STORAGE("STORAGE", "收納用品");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得分類
     *
     * @param code 分類代碼
     * @return 分類
     */
    public static Category fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(Category.class, code);
    }

    /**
     * 判斷是否為書籍
     *
     * @return true=書籍, false=其他
     */
    public boolean isBook() {
        return this == BOOK;
    }
}
