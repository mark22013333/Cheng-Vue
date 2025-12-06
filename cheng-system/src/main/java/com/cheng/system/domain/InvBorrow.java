package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.system.domain.enums.BorrowStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.Date;

/**
 * 借出記錄表 inv_borrow
 *
 * @author cheng
 */
@Setter
@Getter
public class InvBorrow extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 借出ID
     */
    private Long borrowId;

    /**
     * 借出單號（系統自動產生）
     */
    @Excel(name = "借出單號")
    @Size(min = 0, max = 50, message = "借出單號長度不能超過50個字元")
    private String borrowNo;

    /**
     * 物品ID
     */
    @Excel(name = "物品ID")
    @NotNull(message = "物品ID不能為空")
    private Long itemId;

    /**
     * 物品名稱（冗餘欄位，保留歷史記錄）
     */
    @Excel(name = "物品名稱")
    private String itemName;

    /**
     * 物品編碼（冗餘欄位，保留歷史記錄）
     */
    @Excel(name = "物品編碼")
    private String itemCode;

    /**
     * 借出人ID（系統自動設定為當前使用者）
     */
    @Excel(name = "借出人ID")
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
     * 借出時間（系統自動設定為當前時間）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "借出時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
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
     * 狀態
     * 0=待審核, 1=已借出, 2=審核拒絕, 3=已歸還, 4=部分歸還, 5=逾期
     *
     * @see BorrowStatus
     */
    @Excel(name = "狀態", readConverterExp = "0=待審核,1=已借出,2=審核拒絕,3=已歸還,4=部分歸還,5=逾期")
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
     * 預約狀態
     * 0=正常借出, 1=待審核預約, 2=預約通過, 3=預約拒絕, 4=預約取消
     */
    @Excel(name = "預約狀態", readConverterExp = "0=正常借出,1=待審核預約,2=預約通過,3=預約拒絕,4=預約取消")
    private Integer reserveStatus;

    /**
     * 預約開始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "預約開始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reserveStartDate;

    /**
     * 預約結束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "預約結束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reserveEndDate;

    /**
     * 借用目的
     */
    @Excel(name = "借用目的")
    @Size(min = 0, max = 200, message = "借用目的長度不能超過200個字元")
    private String purpose;

    /**
     * 審核備註（拒絕原因）
     */
    @Excel(name = "拒絕原因")
    @Size(min = 0, max = 500, message = "拒絕原因的長度不能超過500個字元")
    private String approveRemark;

    private String categoryName;

    /**
     * 查詢用：開始借出時間
     */
    @JsonIgnore
    private String beginBorrowTime;

    /**
     * 查詢用：結束借出時間
     */
    @JsonIgnore
    private String endBorrowTime;

    /**
     * 查詢用：開始實際歸還時間
     */
    @JsonIgnore
    private String beginActualReturn;

    /**
     * 查詢用：結束實際歸還時間
     */
    @JsonIgnore
    private String endActualReturn;

    public InvBorrow() {
    }

    public InvBorrow(Long borrowId) {
        this.borrowId = borrowId;
    }

    /**
     * 取得借出狀態 Enum
     *
     * @return BorrowStatus
     */
    public BorrowStatus getStatusEnum() {
        return BorrowStatus.getByCode(this.status);
    }

    /**
     * 設定借出狀態 Enum
     *
     * @param statusEnum 借出狀態
     */
    public void setStatusEnum(BorrowStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }

    /**
     * 檢查是否為待審核狀態
     */
    public boolean isPending() {
        BorrowStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.isPending();
    }

    /**
     * 檢查是否為已借出狀態
     */
    public boolean isBorrowed() {
        BorrowStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.isBorrowed();
    }

    /**
     * 檢查是否需要歸還
     */
    public boolean needsReturn() {
        BorrowStatus statusEnum = getStatusEnum();
        return statusEnum != null && statusEnum.needsReturn();
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
