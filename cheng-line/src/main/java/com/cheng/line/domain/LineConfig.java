package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.common.enums.Status;
import com.cheng.common.enums.YesNo;
import com.cheng.line.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * LINE 頻道設定物件 sys_line_config
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 設定ID
     */
    @Excel(name = "設定ID")
    private Integer configId;

    /**
     * 頻道類型
     */
    @NotNull(message = "頻道類型不能為空")
    @Excel(name = "頻道類型")
    private ChannelType channelType;

    /**
     * 頻道名稱
     */
    @NotBlank(message = "頻道名稱不能為空")
    @Size(max = 100, message = "頻道名稱長度不能超過100個字元")
    @Excel(name = "頻道名稱")
    private String channelName;

    /**
     * LINE Channel ID（加密儲存）
     */
    @NotBlank(message = "Channel ID不能為空")
    @Size(max = 255, message = "Channel ID長度不能超過255個字元")
    private String channelId;

    /**
     * Bot Basic ID（例如：@322okyxf）
     * 用於 Webhook URL 路徑參數
     */
    @NotBlank(message = "Bot Basic ID不能為空")
    @Size(max = 100, message = "Bot Basic ID長度不能超過100個字元")
    @Excel(name = "Bot Basic ID")
    private String botBasicId;

    /**
     * LINE Channel Secret（加密儲存）
     */
    @NotBlank(message = "Channel Secret不能為空")
    @Size(max = 500, message = "Channel Secret長度不能超過500個字元")
    private String channelSecret;

    /**
     * LINE Login Channel ID（加密儲存）
     * 用於 LINE Login 功能，與 Messaging API 的 Channel ID 不同
     */
    @Size(max = 255, message = "Login Channel ID長度不能超過255個字元")
    private String loginChannelId;

    /**
     * LINE Login Channel Secret（加密儲存）
     * 用於 LINE Login 功能，與 Messaging API 的 Channel Secret 不同
     */
    @Size(max = 500, message = "Login Channel Secret長度不能超過500個字元")
    private String loginChannelSecret;

    /**
     * Channel Access Token（加密儲存）
     */
    @NotBlank(message = "Access Token不能為空")
    @Size(max = 1000, message = "Access Token長度不能超過1000個字元")
    private String channelAccessToken;

    /**
     * Webhook Base URL（可選，用於自訂 API 網域）
     * 若為空則使用系統預設值（line.webhook.base-url）
     */
    @Size(max = 255, message = "Webhook Base URL長度不能超過255個字元")
    @Excel(name = "Webhook Base URL")
    private String webhookBaseUrl;

    /**
     * Webhook URL（完整的 Webhook URL，自動產生）
     */
    @Size(max = 500, message = "Webhook URL長度不能超過500個字元")
    @Excel(name = "Webhook URL")
    private String webhookUrl;

    /**
     * Webhook 狀態
     */
    @Excel(name = "Webhook狀態")
    private Status webhookStatus;

    /**
     * 啟用狀態
     */
    @Excel(name = "啟用狀態")
    private Status status;

    /**
     * 是否為預設頻道
     */
    @Excel(name = "預設頻道")
    private YesNo isDefault;

    /**
     * 排序順序
     */
    @Excel(name = "排序順序")
    private Integer sortOrder;

    /**
     * 取得頻道類型代碼（用於資料庫儲存）
     */
    public String getChannelTypeCode() {
        return channelType != null ? channelType.getCode() : null;
    }

    /**
     * 設定頻道類型（從資料庫讀取）
     */
    public void setChannelTypeCode(String code) {
        this.channelType = code != null ? ChannelType.fromCode(code) : null;
    }

    /**
     * 取得 Webhook 狀態代碼（用於資料庫儲存）
     */
    public Integer getWebhookStatusCode() {
        return webhookStatus != null ? webhookStatus.getCode() : null;
    }

    /**
     * 設定 Webhook 狀態（從資料庫讀取）
     */
    public void setWebhookStatusCode(Integer code) {
        this.webhookStatus = Status.fromCode(code);
    }

    /**
     * 取得啟用狀態代碼（用於資料庫儲存）
     */
    public Integer getStatusCode() {
        return status != null ? status.getCode() : null;
    }

    /**
     * 設定啟用狀態（從資料庫讀取）
     */
    public void setStatusCode(Integer code) {
        this.status = Status.fromCode(code);
    }

    /**
     * 取得預設頻道代碼（用於資料庫儲存）
     */
    public Integer getIsDefaultCode() {
        return isDefault != null ? isDefault.getCode() : null;
    }

    /**
     * 設定預設頻道（從資料庫讀取）
     */
    public void setIsDefaultCode(Integer code) {
        this.isDefault = YesNo.fromCode(code);
    }
}
