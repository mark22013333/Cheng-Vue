package com.cheng.system.domain;

import java.io.Serial;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;

/**
 * 掃描記錄物件 inv_scan_log
 *
 * @author cheng
 * @since 2025-09-23
 */
public class InvScanLog extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 掃描記錄ID
     */
    private Long scanId;

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
     * 掃描類型（1條碼 2QR碼）
     */
    @Excel(name = "掃描類型", readConverterExp = "1=條碼,2=QR碼")
    private String scanType;

    /**
     * 掃描內容
     */
    @Excel(name = "掃描內容")
    private String scanCode;

    /**
     * 掃描結果（0成功 1失敗）
     */
    @Excel(name = "掃描結果", readConverterExp = "0=成功,1=失敗")
    private String scanResult;

    /**
     * 掃描時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "掃描時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date scanTime;

    /**
     * 掃描人員ID
     */
    @Excel(name = "掃描人員ID")
    private Long scannerId;

    /**
     * 掃描人員姓名
     */
    @Excel(name = "掃描人員姓名")
    private String scannerName;

    /**
     * 掃描設備
     */
    @Excel(name = "掃描設備")
    private String scanDevice;

    /**
     * 掃描位置
     */
    @Excel(name = "掃描位置")
    private String scanLocation;

    /**
     * 失敗原因
     */
    @Excel(name = "失敗原因")
    private String failReason;

    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }

    public Long getScanId() {
        return scanId;
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

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanCode(String scanCode) {
        this.scanCode = scanCode;
    }

    public String getScanCode() {
        return scanCode;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanTime(Date scanTime) {
        this.scanTime = scanTime;
    }

    public Date getScanTime() {
        return scanTime;
    }

    public void setScannerId(Long scannerId) {
        this.scannerId = scannerId;
    }

    public Long getScannerId() {
        return scannerId;
    }

    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    public String getScannerName() {
        return scannerName;
    }

    public void setScanDevice(String scanDevice) {
        this.scanDevice = scanDevice;
    }

    public String getScanDevice() {
        return scanDevice;
    }

    public void setScanLocation(String scanLocation) {
        this.scanLocation = scanLocation;
    }

    public String getScanLocation() {
        return scanLocation;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getFailReason() {
        return failReason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("scanId", getScanId())
                .append("itemId", getItemId())
                .append("itemCode", getItemCode())
                .append("itemName", getItemName())
                .append("scanType", getScanType())
                .append("scanCode", getScanCode())
                .append("scanResult", getScanResult())
                .append("scanTime", getScanTime())
                .append("scannerId", getScannerId())
                .append("scannerName", getScannerName())
                .append("scanDevice", getScanDevice())
                .append("scanLocation", getScanLocation())
                .append("failReason", getFailReason())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
