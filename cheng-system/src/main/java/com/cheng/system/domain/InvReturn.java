package com.cheng.system.domain;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;

/**
 * 歸還記錄物件 inv_return
 *
 * @author cheng
 * @since 2025-09-23
 */
public class InvReturn extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歸還記錄ID
     */
    private Long returnId;

    /**
     * 借出記錄ID
     */
    @Excel(name = "借出記錄ID")
    private Long borrowId;

    /**
     * 借出編號
     */
    @Excel(name = "借出編號")
    private String borrowCode;

    /**
     * 物品ID
     */
    @Excel(name = "物品ID")
    private Long itemId;

    /**
     * 物品編碼
     */
    @Excel(name = "物品編碼")
    private String itemCode;

    /**
     * 物品名稱
     */
    @Excel(name = "物品名稱")
    private String itemName;

    /**
     * 歸還數量
     */
    @Excel(name = "歸還數量")
    private Long returnQuantity;

    /**
     * 借用人ID
     */
    @Excel(name = "借用人ID")
    private Long borrowerId;

    /**
     * 借用人姓名
     */
    @Excel(name = "借用人姓名")
    private String borrowerName;

    /**
     * 歸還時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "歸還時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date returnTime;

    /**
     * 預計歸還時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "預計歸還時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expectedReturn;

    /**
     * 是否逾期（0否 1是）
     */
    @Excel(name = "是否逾期", readConverterExp = "0=否,1=是")
    private String isOverdue;

    /**
     * 逾期天數
     */
    @Excel(name = "逾期天數")
    private Long overdueDays;

    /**
     * 物品狀態（good完好 damaged損壞 lost遺失）
     */
    @Excel(name = "物品狀態", readConverterExp = "good=完好,damaged=損壞,lost=遺失")
    private String itemCondition;

    /**
     * 損壞說明
     */
    @Excel(name = "損壞說明")
    private String damageDescription;

    /**
     * 賠償金額
     */
    @Excel(name = "賠償金額")
    private BigDecimal compensationAmount;

    /**
     * 接收人ID
     */
    @Excel(name = "接收人ID")
    private Long receiverId;

    /**
     * 接收人姓名
     */
    @Excel(name = "接收人姓名")
    private String receiverName;

    /**
     * 歸還狀態（0待確認 1已確認 2有異議）
     */
    @Excel(name = "歸還狀態", readConverterExp = "0=待確認,1=已確認,2=有異議")
    private String returnStatus;

    public void setReturnId(Long returnId) {
        this.returnId = returnId;
    }

    public Long getReturnId() {
        return returnId;
    }

    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
    }

    public Long getBorrowId() {
        return borrowId;
    }

    public void setBorrowCode(String borrowCode) {
        this.borrowCode = borrowCode;
    }

    public String getBorrowCode() {
        return borrowCode;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setReturnQuantity(Long returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public Long getReturnQuantity() {
        return returnQuantity;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setReturnTime(Date returnTime) {
        this.returnTime = returnTime;
    }

    public Date getReturnTime() {
        return returnTime;
    }

    public void setExpectedReturn(Date expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public Date getExpectedReturn() {
        return expectedReturn;
    }

    public void setIsOverdue(String isOverdue) {
        this.isOverdue = isOverdue;
    }

    public String getIsOverdue() {
        return isOverdue;
    }

    public void setOverdueDays(Long overdueDays) {
        this.overdueDays = overdueDays;
    }

    public Long getOverdueDays() {
        return overdueDays;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setCompensationAmount(BigDecimal compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public BigDecimal getCompensationAmount() {
        return compensationAmount;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("returnId", getReturnId())
                .append("borrowId", getBorrowId())
                .append("borrowCode", getBorrowCode())
                .append("itemId", getItemId())
                .append("itemCode", getItemCode())
                .append("itemName", getItemName())
                .append("returnQuantity", getReturnQuantity())
                .append("borrowerId", getBorrowerId())
                .append("borrowerName", getBorrowerName())
                .append("returnTime", getReturnTime())
                .append("expectedReturn", getExpectedReturn())
                .append("isOverdue", getIsOverdue())
                .append("overdueDays", getOverdueDays())
                .append("itemCondition", getItemCondition())
                .append("damageDescription", getDamageDescription())
                .append("compensationAmount", getCompensationAmount())
                .append("receiverId", getReceiverId())
                .append("receiverName", getReceiverName())
                .append("returnStatus", getReturnStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
