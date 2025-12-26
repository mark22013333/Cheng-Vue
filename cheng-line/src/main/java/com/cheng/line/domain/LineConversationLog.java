package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.line.enums.ConversationDirection;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * LINE 對話記錄物件 line_conversation_log
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineConversationLog extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID
     */
    @Excel(name = "ID")
    private Long id;

    /**
     * LINE 使用者 ID
     */
    @Excel(name = "LINE使用者ID")
    private String lineUserId;

    /**
     * 方向
     */
    @Excel(name = "方向")
    private ConversationDirection direction;

    /**
     * 訊息內容
     */
    @Excel(name = "訊息內容")
    private String messageContent;

    /**
     * 訊息格式
     */
    @Excel(name = "訊息格式")
    private String messageFormat;

    /**
     * LINE 訊息 ID
     */
    @Excel(name = "訊息ID")
    private String messageId;

    /**
     * 頻道設定ID
     */
    @Excel(name = "頻道設定ID")
    private Integer configId;

    /**
     * 訊息時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "訊息時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date messageTime;

    /**
     * 是否成功
     */
    @Excel(name = "是否成功")
    private Integer isSuccess;

    /**
     * 錯誤訊息
     */
    @Excel(name = "錯誤訊息")
    private String errorMessage;

    /**
     * 取得方向代碼（用於資料庫儲存）
     */
    public String getDirectionCode() {
        return direction != null ? direction.getCode() : null;
    }

    /**
     * 設定方向（從資料庫讀取）
     */
    public void setDirectionCode(String code) {
        this.direction = code != null ? ConversationDirection.fromCode(code) : null;
    }
}
