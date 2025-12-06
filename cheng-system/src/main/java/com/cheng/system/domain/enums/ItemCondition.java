package com.cheng.system.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 物品狀況列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ItemCondition {
    /**
     * 完好
     */
    GOOD("good", "完好"),

    /**
     * 損壞
     */
    DAMAGED("damaged", "損壞"),

    /**
     * 遺失
     */
    LOST("lost", "遺失");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得物品狀況
     *
     * @param code 狀況代碼
     * @return 物品狀況
     */
    public static ItemCondition getByCode(String code) {
        for (ItemCondition condition : values()) {
            if (condition.code.equals(code)) {
                return condition;
            }
        }
        return null;
    }

    /**
     * 根據描述取得物品狀況
     *
     * @param description 狀況描述
     * @return 物品狀況
     */
    public static ItemCondition getByDescription(String description) {
        for (ItemCondition condition : values()) {
            if (condition.description.equals(description) || condition.code.equals(description)) {
                return condition;
            }
        }
        return null;
    }

    /**
     * 檢查是否為完好狀況
     *
     * @return true=完好
     */
    public boolean isGood() {
        return this == GOOD;
    }

    /**
     * 檢查是否為損壞狀況
     *
     * @return true=損壞
     */
    public boolean isDamaged() {
        return this == DAMAGED;
    }

    /**
     * 檢查是否為遺失狀況
     *
     * @return true=遺失
     */
    public boolean isLost() {
        return this == LOST;
    }

    /**
     * 取得狀況對應的顏色（Element UI 色系）
     *
     * @return 顏色代碼
     */
    public String getColor() {
        return switch (this) {
            case GOOD -> "#67C23A";     // 完好 - 綠色
            case DAMAGED -> "#E6A23C";  // 損壞 - 橙色
            case LOST -> "#F56C6C";     // 遺失 - 紅色
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
