package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 庫存異動記錄表 inv_stock_record
 *
 * @author cheng
 */
public class InvStockRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 記錄ID
     */
    private Long recordId;

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
     * 異動類型（1入庫 2出庫 3借出 4歸還 5盤點 6損壞）
     */
    @Excel(name = "異動類型", readConverterExp = "1=入庫,2=出庫,3=借出,4=歸還,5=盤點,6=損壞")
    private String recordType;

    /**
     * 數量（正數入庫，負數出庫）
     */
    @Excel(name = "數量")
    private Integer quantity;

    /**
     * 異動前數量
     */
    @Excel(name = "異動前數量")
    private Integer beforeQty;

    /**
     * 異動後數量
     */
    @Excel(name = "異動後數量")
    private Integer afterQty;

    /**
     * 單價
     */
    @Excel(name = "單價")
    private BigDecimal unitPrice;

    /**
     * 總金額
     */
    @Excel(name = "總金額")
    private BigDecimal totalAmount;

    /**
     * 操作人員ID
     */
    @Excel(name = "操作人員ID")
    private Long operatorId;

    /**
     * 操作人員姓名
     */
    @Excel(name = "操作人員")
    private String operatorName;

    /**
     * 部門ID
     */
    @Excel(name = "部門ID")
    private Long deptId;

    /**
     * 部門名稱（關聯查詢用）
     */
    @Excel(name = "部門名稱")
    private String deptName;

    /**
     * 異動時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "異動時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    /**
     * 異動原因
     */
    @Excel(name = "異動原因")
    private String reason;

    /**
     * 備註
     */
    @Excel(name = "備註")
    private String remark;

    public InvStockRecord() {
    }

    public InvStockRecord(Long recordId) {
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getBeforeQty() {
        return beforeQty;
    }

    public void setBeforeQty(Integer beforeQty) {
        this.beforeQty = beforeQty;
    }

    public Integer getAfterQty() {
        return afterQty;
    }

    public void setAfterQty(Integer afterQty) {
        this.afterQty = afterQty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("recordId", getRecordId())
                .append("itemId", getItemId())
                .append("itemName", getItemName())
                .append("itemCode", getItemCode())
                .append("recordType", getRecordType())
                .append("quantity", getQuantity())
                .append("beforeQty", getBeforeQty())
                .append("afterQty", getAfterQty())
                .append("unitPrice", getUnitPrice())
                .append("totalAmount", getTotalAmount())
                .append("operatorId", getOperatorId())
                .append("operatorName", getOperatorName())
                .append("deptId", getDeptId())
                .append("deptName", getDeptName())
                .append("recordTime", getRecordTime())
                .append("reason", getReason())
                .append("remark", getRemark())
                .toString();
    }
}
