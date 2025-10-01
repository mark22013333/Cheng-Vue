package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 庫存表 inv_stock
 *
 * @author cheng
 */
@Setter
@Getter
public class InvStock implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 庫存ID
     */
    private Long stockId;

    /**
     * 物品ID
     */
    @Excel(name = "物品ID")
    private Long itemId;

    /**
     * 物品名稱（關聯查詢用）
     */
    @Excel(name = "物品名稱")
    private String itemName;

    /**
     * 物品編碼（關聯查詢用）
     */
    @Excel(name = "物品編碼")
    private String itemCode;

    /**
     * 分類ID（關聯查詢用）
     */
    @Excel(name = "分類ID")
    private Long categoryId;

    /**
     * 分類名稱（關聯查詢用）
     */
    @Excel(name = "分類名稱")
    private String categoryName;

    /**
     * 規格（inv_item.specification）
     */
    @Excel(name = "規格")
    private String specification;

    /**
     * 單位（inv_item.unit）
     */
    @Excel(name = "單位")
    private String unit;

    /**
     * 最低庫存（inv_item.min_stock）
     */
    @Excel(name = "最小庫存")
    private Integer minStock;

    /**
     * 最高庫存（inv_item.max_stock）
     */
    @Excel(name = "最大庫存")
    private Integer maxStock;

    /**
     * 存放位置（inv_item.location）
     */
    @Excel(name = "存放位置")
    private String location;

    /**
     * 總數量
     */
    @Excel(name = "總數量")
    private Integer totalQuantity;

    /**
     * 可用數量
     */
    @Excel(name = "可用數量")
    private Integer availableQty;

    /**
     * 借出數量
     */
    @Excel(name = "借出數量")
    private Integer borrowedQty;

    /**
     * 預留數量
     */
    @Excel(name = "預留數量")
    private Integer reservedQty;

    /**
     * 損壞數量
     */
    @Excel(name = "損壞數量")
    private Integer damagedQty;

    /**
     * 最後入庫時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最後入庫時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastInTime;

    /**
     * 最後出庫時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最後出庫時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastOutTime;

    /**
     * 更新時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public InvStock() {
    }

    public InvStock(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("stockId", getStockId())
                .append("itemId", getItemId())
                .append("itemName", getItemName())
                .append("itemCode", getItemCode())
                .append("categoryId", getCategoryId())
                .append("categoryName", getCategoryName())
                .append("specification", getSpecification())
                .append("unit", getUnit())
                .append("minStock", getMinStock())
                .append("maxStock", getMaxStock())
                .append("location", getLocation())
                .append("totalQuantity", getTotalQuantity())
                .append("availableQty", getAvailableQty())
                .append("borrowedQty", getBorrowedQty())
                .append("reservedQty", getReservedQty())
                .append("damagedQty", getDamagedQty())
                .append("lastInTime", getLastInTime())
                .append("lastOutTime", getLastOutTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
