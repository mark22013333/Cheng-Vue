package com.cheng.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 掃描記錄表 inv_scan_log
 */
@Getter
@Setter
public class InvScanLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long scanId;
    /** 掃描類型（1條碼 2QR碼） */
    private String scanType;
    /** 掃描內容 */
    private String scanCode;
    /** 物品ID（如果識別成功） */
    private Long itemId;
    /** 掃描結果（0成功 1失敗） */
    private String scanResult;
    /** 操作人員ID */
    private Long operatorId;
    /** 操作人員姓名 */
    private String operatorName;
    /** 掃描時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scanTime;
    /** IP地址 */
    private String ipAddress;
    /** 使用者代理 */
    private String userAgent;
    /** 錯誤訊息 */
    private String errorMsg;
}
