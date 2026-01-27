package com.cheng.shop.enums;

import com.cheng.common.enums.CodedEnum;
import com.cheng.common.utils.EnumUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 商品狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus implements CodedEnum<String> {

    DRAFT("DRAFT", "草稿"),
    PREVIEW("PREVIEW", "預覽"),
    ON_SALE("ON_SALE", "上架中"),
    OFF_SALE("OFF_SALE", "已下架");

    private final String code;
    private final String description;

    @JsonValue
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 是否可銷售
     */
    public boolean canSell() {
        return this == ON_SALE;
    }

    /**
     * 是否可編輯
     */
    public boolean canEdit() {
        return this == DRAFT || this == OFF_SALE;
    }

    /**
     * 取得可流轉的下一個狀態
     */
    public List<ProductStatus> getNextStatuses() {
        return switch (this) {
            case DRAFT -> List.of(PREVIEW, ON_SALE);
            case PREVIEW -> List.of(DRAFT, ON_SALE);
            case ON_SALE -> List.of(OFF_SALE);
            case OFF_SALE -> List.of(ON_SALE);
        };
    }

    /**
     * 取得狀態對應的顏色（Element UI 色系）
     */
    public String getColor() {
        return switch (this) {
            case DRAFT -> "#909399";
            case PREVIEW -> "#E6A23C";
            case ON_SALE -> "#67C23A";
            case OFF_SALE -> "#F56C6C";
        };
    }

    public static ProductStatus fromCode(String code) {
        return EnumUtils.fromCodeOrThrow(ProductStatus.class, code);
    }
}
