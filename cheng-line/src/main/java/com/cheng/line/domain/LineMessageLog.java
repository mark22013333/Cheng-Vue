package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.line.enums.ContentType;
import com.cheng.line.enums.MessageType;
import com.cheng.line.enums.SendStatus;
import com.cheng.line.enums.TargetType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * LINE 推播訊息記錄物件 line_message_log
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineMessageLog extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 訊息ID
     */
    @Excel(name = "訊息ID")
    private Long messageId;

    /**
     * 使用的頻道設定ID
     */
    @Excel(name = "頻道設定ID")
    private Integer configId;

    /**
     * 訊息類型
     */
    @Excel(name = "訊息類型")
    private MessageType messageType;

    /**
     * 內容類型
     */
    @Excel(name = "內容類型")
    private ContentType contentType;

    /**
     * 訊息內容（JSON 格式）
     */
    private String messageContent;

    /**
     * 推播對象類型
     */
    @Excel(name = "推播對象類型")
    private TargetType targetType;

    /**
     * 目標 LINE 使用者 ID（單人推播時使用）
     */
    @Excel(name = "目標使用者ID")
    private String targetLineUserId;

    /**
     * 目標 LINE 使用者 ID 列表（JSON Array，多人推播時使用）
     */
    private String targetUserIds;

    /**
     * 目標標籤 ID（標籤推播時使用，預留欄位）
     */
    @Excel(name = "目標標籤ID")
    private Long targetTagId;

    /**
     * 目標數量
     */
    @Excel(name = "目標數量")
    private Integer targetCount;

    /**
     * 成功數量
     */
    @Excel(name = "成功數量")
    private Integer successCount;

    /**
     * 失敗數量
     */
    @Excel(name = "失敗數量")
    private Integer failCount;

    /**
     * 發送狀態
     */
    @Excel(name = "發送狀態")
    private SendStatus sendStatus;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * LINE API Request ID
     */
    @Excel(name = "Request ID")
    private String lineRequestId;

    /**
     * 實際發送時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "發送時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /**
     * 取得訊息類型代碼（用於資料庫儲存）
     */
    public String getMessageTypeCode() {
        return messageType != null ? messageType.getCode() : null;
    }

    /**
     * 設定訊息類型（從資料庫讀取）
     */
    public void setMessageTypeCode(String code) {
        this.messageType = code != null ? MessageType.fromCode(code) : null;
    }

    /**
     * 取得內容類型代碼（用於資料庫儲存）
     */
    public String getContentTypeCode() {
        return contentType != null ? contentType.getCode() : null;
    }

    /**
     * 設定內容類型（從資料庫讀取）
     */
    public void setContentTypeCode(String code) {
        this.contentType = code != null ? ContentType.fromCode(code) : null;
    }

    /**
     * 取得推播對象類型代碼（用於資料庫儲存）
     */
    public String getTargetTypeCode() {
        return targetType != null ? targetType.getCode() : null;
    }

    /**
     * 設定推播對象類型（從資料庫讀取）
     */
    public void setTargetTypeCode(String code) {
        this.targetType = code != null ? TargetType.fromCode(code) : null;
    }

    /**
     * 取得發送狀態代碼（用於資料庫儲存）
     */
    public String getSendStatusCode() {
        return sendStatus != null ? sendStatus.getCode() : null;
    }

    /**
     * 設定發送狀態（從資料庫讀取）
     */
    public void setSendStatusCode(String code) {
        this.sendStatus = code != null ? SendStatus.fromCode(code) : null;
    }
}
