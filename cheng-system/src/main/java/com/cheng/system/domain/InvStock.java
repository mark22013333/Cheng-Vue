package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }

    public Integer getBorrowedQty() {
        return borrowedQty;
    }

    public void setBorrowedQty(Integer borrowedQty) {
        this.borrowedQty = borrowedQty;
    }

    public Integer getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(Integer reservedQty) {
        this.reservedQty = reservedQty;
    }

    public Integer getDamagedQty() {
        return damagedQty;
    }

    public void setDamagedQty(Integer damagedQty) {
        this.damagedQty = damagedQty;
    }

    public Date getLastInTime() {
        return lastInTime;
    }

    public void setLastInTime(Date lastInTime) {
        this.lastInTime = lastInTime;
    }

    public Date getLastOutTime() {
        return lastOutTime;
    }

    public void setLastOutTime(Date lastOutTime) {
        this.lastOutTime = lastOutTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
