package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

/**
 * 掃描記錄表 inv_scan_log
 */
@Getter
@Setter
public class InvScanLog extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 掃描ID */
    @Excel(name = "掃描ID")
    private Long scanId;
    
    /** 掃描類型（1條碼 2QR碼） */
    @Excel(name = "掃描類型", readConverterExp = "1=條碼,2=QR碼")
    private String scanType;
    
    /** 掃描內容 */
    @Excel(name = "掃描內容")
    private String scanCode;
    
    /** 物品ID（如果識別成功） */
    private Long itemId;
    
    /** 物品名稱（關聯查詢用） */
    @Excel(name = "物品名稱")
    private String itemName;
    
    /** 掃描結果（0成功 1失敗） */
    @Excel(name = "掃描結果", readConverterExp = "0=成功,1=失敗")
    private String scanResult;
    
    /** 操作人員ID */
    private Long operatorId;
    
    /** 操作人員姓名 */
    @Excel(name = "掃描人")
    private String operatorName;
    
    /** 掃描時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "掃描時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date scanTime;
    
    /** IP地址 */
    @Excel(name = "IP地址")
    private String ipAddress;
    
    /** 使用者代理 */
    private String userAgent;
    
    /** 錯誤訊息 */
    @Excel(name = "錯誤訊息")
    private String errorMsg;
}
