package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.Date;

/**
 * 借出記錄表 inv_borrow
 *
 * @author cheng
 */
public class InvBorrow extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 借出ID
     */
    private Long borrowId;

    /**
     * 借出單號
     */
    @Excel(name = "借出單號")
    @NotBlank(message = "借出單號不能為空")
    @Size(min = 0, max = 50, message = "借出單號長度不能超過50個字元")
    private String borrowNo;

    /**
     * 物品ID
     */
    @Excel(name = "物品ID")
    @NotNull(message = "物品ID不能為空")
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
     * 借出人ID
     */
    @Excel(name = "借出人ID")
    @NotNull(message = "借出人ID不能為空")
    private Long borrowerId;

    /**
     * 借出人姓名
     */
    @Excel(name = "借出人姓名")
    @NotBlank(message = "借出人姓名不能為空")
    @Size(min = 0, max = 50, message = "借出人姓名長度不能超過50個字元")
    private String borrowerName;

    /**
     * 借出人部門ID
     */
    @Excel(name = "借出人部門ID")
    private Long borrowerDept;

    /**
     * 借出人部門名稱（關聯查詢用）
     */
    @Excel(name = "借出人部門")
    private String borrowerDeptName;

    /**
     * 借出數量
     */
    @Excel(name = "借出數量")
    @NotNull(message = "借出數量不能為空")
    private Integer quantity;

    /**
     * 借出時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "借出時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "借出時間不能為空")
    private Date borrowTime;

    /**
     * 預計歸還時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "預計歸還時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expectedReturn;

    /**
     * 實際歸還時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "實際歸還時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date actualReturn;

    /**
     * 已歸還數量
     */
    @Excel(name = "已歸還數量")
    private Integer returnQuantity;

    /**
     * 狀態（0借出中 1已歸還 2逾期 3部分歸還）
     */
    @Excel(name = "狀態", readConverterExp = "0=借出中,1=已歸還,2=逾期,3=部分歸還")
    private String status;

    /**
     * 審核人ID
     */
    @Excel(name = "審核人ID")
    private Long approverId;

    /**
     * 審核人姓名
     */
    @Excel(name = "審核人姓名")
    @Size(min = 0, max = 50, message = "審核人姓名長度不能超過50個字元")
    private String approverName;

    /**
     * 審核時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "審核時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /**
     * 借用目的
     */
    @Excel(name = "借用目的")
    @Size(min = 0, max = 200, message = "借用目的長度不能超過200個字元")
    private String purpose;

    private String approveRemark;

    private String categoryName;

    public InvBorrow() {
    }

    public InvBorrow(Long borrowId) {
        this.borrowId = borrowId;
    }

    public Long getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
    }

    public String getBorrowNo() {
        return borrowNo;
    }

    public void setBorrowNo(String borrowNo) {
        this.borrowNo = borrowNo;
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

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public Long getBorrowerDept() {
        return borrowerDept;
    }

    public void setBorrowerDept(Long borrowerDept) {
        this.borrowerDept = borrowerDept;
    }

    public String getBorrowerDeptName() {
        return borrowerDeptName;
    }

    public void setBorrowerDeptName(String borrowerDeptName) {
        this.borrowerDeptName = borrowerDeptName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(Date borrowTime) {
        this.borrowTime = borrowTime;
    }

    public Date getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(Date expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public Date getActualReturn() {
        return actualReturn;
    }

    public void setActualReturn(Date actualReturn) {
        this.actualReturn = actualReturn;
    }

    public Integer getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Integer returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("borrowId", getBorrowId())
                .append("borrowNo", getBorrowNo())
                .append("itemId", getItemId())
                .append("itemName", getItemName())
                .append("itemCode", getItemCode())
                .append("borrowerId", getBorrowerId())
                .append("borrowerName", getBorrowerName())
                .append("borrowerDept", getBorrowerDept())
                .append("borrowerDeptName", getBorrowerDeptName())
                .append("quantity", getQuantity())
                .append("borrowTime", getBorrowTime())
                .append("expectedReturn", getExpectedReturn())
                .append("actualReturn", getActualReturn())
                .append("returnQuantity", getReturnQuantity())
                .append("status", getStatus())
                .append("approverId", getApproverId())
                .append("approverName", getApproverName())
                .append("approveTime", getApproveTime())
                .append("purpose", getPurpose())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
