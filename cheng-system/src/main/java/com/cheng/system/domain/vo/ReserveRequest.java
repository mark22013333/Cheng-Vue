package com.cheng.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * 物品預約請求對象
 *
 * @author cheng
 */
public class ReserveRequest {

    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能為空")
    private Long itemId;

    /**
     * 預約數量（固定為1）
     */
    private Integer borrowQty = 1;

    /**
     * 樂觀鎖版本號
     */
    @NotNull(message = "版本號不能為空")
    private Integer version;

    /**
     * 預約開始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "預約開始日期不能為空")
    private Date startDate;

    /**
     * 預約結束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "預約結束日期不能為空")
    private Date endDate;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getBorrowQty() {
        return borrowQty;
    }

    public void setBorrowQty(Integer borrowQty) {
        this.borrowQty = borrowQty;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
