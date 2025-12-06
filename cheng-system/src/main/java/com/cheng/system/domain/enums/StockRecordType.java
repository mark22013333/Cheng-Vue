package com.cheng.system.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 庫存異動類型列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum StockRecordType {
    /**
     * 入庫
     */
    IN("1", "入庫"),

    /**
     * 出庫
     */
    OUT("2", "出庫"),

    /**
     * 借出
     */
    BORROW("3", "借出"),

    /**
     * 歸還
     */
    RETURN("4", "歸還"),

    /**
     * 盤點
     */
    CHECK("5", "盤點"),

    /**
     * 損壞
     */
    DAMAGE("6", "損壞");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得異動類型
     *
     * @param code 異動類型代碼
     * @return 庫存異動類型
     */
    public static StockRecordType getByCode(String code) {
        for (StockRecordType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 檢查是否為入庫類型
     *
     * @return true=入庫
     */
    public boolean isIn() {
        return this == IN;
    }

    /**
     * 檢查是否為出庫類型
     *
     * @return true=出庫
     */
    public boolean isOut() {
        return this == OUT;
    }

    /**
     * 檢查是否為借出類型
     *
     * @return true=借出
     */
    public boolean isBorrow() {
        return this == BORROW;
    }

    /**
     * 檢查是否為歸還類型
     *
     * @return true=歸還
     */
    public boolean isReturn() {
        return this == RETURN;
    }

    /**
     * 檢查是否為盤點類型
     *
     * @return true=盤點
     */
    public boolean isCheck() {
        return this == CHECK;
    }

    /**
     * 檢查是否為損壞類型
     *
     * @return true=損壞
     */
    public boolean isDamage() {
        return this == DAMAGE;
    }

    /**
     * 取得異動類型對應的顏色（Element UI 色系）
     *
     * @return 顏色代碼
     */
    public String getColor() {
        return switch (this) {
            case IN -> "#67C23A";       // 入庫 - 綠色
            case OUT -> "#F56C6C";      // 出庫 - 紅色
            case BORROW -> "#E6A23C";   // 借出 - 橙色
            case RETURN -> "#409EFF";   // 歸還 - 藍色
            case CHECK -> "#909399";    // 盤點 - 灰色
            case DAMAGE -> "#F56C6C";   // 損壞 - 紅色
        };
    }

    /**
     * JSON 序列化時返回 code
     */
    @JsonValue
    public String getCode() {
        return code;
    }
}
