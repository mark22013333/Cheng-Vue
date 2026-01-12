package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;

/**
 * 庫存物品標籤關聯物件 inv_item_tag_relation
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvItemTagRelation extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID
     */
    private Long id;

    /**
     * 物品ID
     */
    @Excel(name = "物品ID")
    @NotNull(message = "物品ID不能為空")
    private Long itemId;

    /**
     * 標籤ID
     */
    @Excel(name = "標籤ID")
    @NotNull(message = "標籤ID不能為空")
    private Long tagId;

    /**
     * 關聯的標籤名稱（查詢用，非持久化欄位）
     */
    private transient String tagName;

    /**
     * 關聯的標籤顏色（查詢用，非持久化欄位）
     */
    private transient String tagColor;

    /**
     * 關聯的物品名稱（查詢用，非持久化欄位）
     */
    private transient String itemName;

    /**
     * 關聯的物品編碼（查詢用，非持久化欄位）
     */
    private transient String itemCode;
}
