package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.line.enums.BindStatus;
import com.cheng.line.enums.FollowStatus;
import com.cheng.system.domain.SysTag;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
 * LINE 使用者物件 line_user
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineUser extends BaseEntity {
    
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
     * LINE 顯示名稱
     */
    @Excel(name = "顯示名稱")
    private String lineDisplayName;

    /**
     * LINE 頭像 URL
     */
    private String linePictureUrl;

    /**
     * LINE 狀態訊息
     */
    @Excel(name = "狀態訊息")
    private String lineStatusMessage;

    /**
     * LINE 語言設定
     */
    @Excel(name = "語言")
    private String lineLanguage;

    /**
     * 系統使用者 ID
     */
    @Excel(name = "系統使用者ID")
    private Long sysUserId;

    /**
     * 綁定狀態
     */
    @Excel(name = "綁定狀態")
    private BindStatus bindStatus;

    /**
     * 關注狀態
     */
    @Excel(name = "關注狀態")
    private FollowStatus followStatus;

    /**
     * 初次加入時間（首次關注）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "初次加入時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date firstFollowTime;

    /**
     * 最近一次關注時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近關注時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date latestFollowTime;

    /**
     * 取消關注時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "取消關注時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date unfollowTime;

    /**
     * 封鎖時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "封鎖時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date blockTime;

    /**
     * 初次綁定時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "初次綁定時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date firstBindTime;

    /**
     * 最近一次綁定時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近綁定時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date latestBindTime;

    /**
     * 解除綁定時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "解除綁定時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date unbindTime;

    /**
     * 綁定次數
     */
    @Excel(name = "綁定次數")
    private Integer bindCount;

    /**
     * 累計發送訊息數
     */
    @Excel(name = "累計發送數")
    private Integer totalMessagesSent;

    /**
     * 累計接收訊息數
     */
    @Excel(name = "累計接收數")
    private Integer totalMessagesReceived;

    /**
     * 最後互動時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最後互動時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastInteractionTime;

    /**
     * 使用者標籤列表（非資料庫欄位，查詢時填充）
     */
    private transient List<SysTag> tags;

    /**
     * 標籤ID（查詢條件用）
     */
    private transient Long tagId;

    /**
     * 取得綁定狀態代碼（用於資料庫儲存）
     */
    public String getBindStatusCode() {
        return bindStatus != null ? bindStatus.getCode() : null;
    }

    /**
     * 設定綁定狀態（從資料庫讀取）
     */
    public void setBindStatusCode(String code) {
        this.bindStatus = code != null ? BindStatus.fromCode(code) : null;
    }

    /**
     * 取得關注狀態代碼（用於資料庫儲存）
     */
    public String getFollowStatusCode() {
        return followStatus != null ? followStatus.getCode() : null;
    }

    /**
     * 設定關注狀態（從資料庫讀取）
     */
    public void setFollowStatusCode(String code) {
        this.followStatus = code != null ? FollowStatus.fromCode(code) : null;
    }
}
