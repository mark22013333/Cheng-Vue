package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.line.enums.PushDetailStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * LINE 逐人推播明細物件 line_push_detail
 *
 * @author cheng
 */
@Data
public class LinePushDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明細 ID
     */
    @Excel(name = "明細ID")
    private Long detailId;

    /**
     * 訊息日誌 ID (FK line_message_log)
     */
    @Excel(name = "訊息日誌ID")
    private Long messageId;

    /**
     * LINE 使用者 ID
     */
    @Excel(name = "LINE使用者ID")
    private String lineUserId;

    /**
     * LINE 顯示名稱（快照）
     */
    @Excel(name = "顯示名稱")
    private String lineDisplayName;

    /**
     * 狀態：SUCCESS/FAILED/BLOCKED/PENDING
     */
    @Excel(name = "狀態")
    private PushDetailStatus status;

    /**
     * 重試次數
     */
    @Excel(name = "重試次數")
    private Integer retryCount;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 發送時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "發送時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /**
     * 取得狀態代碼（用於資料庫儲存）
     */
    public String getStatusCode() {
        return status != null ? status.getCode() : null;
    }

    /**
     * 設定狀態（從資料庫讀取）
     */
    public void setStatusCode(String code) {
        this.status = code != null ? PushDetailStatus.fromCode(code) : null;
    }
}
