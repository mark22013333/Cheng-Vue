package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.line.enums.RichMenuStatus;
import com.cheng.line.enums.RichMenuTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * LINE Rich Menu（圖文選單）物件 sys_line_rich_menu
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLineRichMenu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID
     */
    @Excel(name = "ID")
    private Long id;

    /**
     * 關聯的 LINE 頻道設定ID
     */
    @NotNull(message = "頻道設定不能為空")
    @Excel(name = "頻道ID")
    private Integer configId;

    /**
     * 選單名稱（系統內部使用）
     */
    @NotBlank(message = "選單名稱不能為空")
    @Size(max = 255, message = "選單名稱長度不能超過255個字元")
    @Excel(name = "選單名稱")
    private String name;

    /**
     * 選單說明
     */
    @Size(max = 500, message = "選單說明長度不能超過500個字元")
    @Excel(name = "選單說明")
    private String description;

    /**
     * 版型類型
     */
    @Excel(name = "版型類型")
    private RichMenuTemplateType templateType;

    /**
     * 聊天欄位顯示文字（最多14字）
     */
    @Size(max = 50, message = "聊天欄位文字長度不能超過50個字元")
    @Excel(name = "聊天欄位文字")
    private String chatBarText;

    /**
     * 選單圖片 URL（本機路徑或 CDN）
     */
    @Size(max = 1000, message = "圖片URL長度不能超過1000個字元")
    @Excel(name = "圖片URL")
    private String imageUrl;

    /**
     * 本地圖片路徑（用於預覽和重新發布）
     */
    @Size(max = 255, message = "本地圖片路徑長度不能超過255個字元")
    private String localImagePath;

    /**
     * 圖片尺寸
     * 支援：2500x1686/2500x843/1200x810/1200x405/800x540/800x270
     */
    @NotBlank(message = "圖片尺寸不能為空")
    @Size(max = 20, message = "圖片尺寸長度不能超過20個字元")
    @Excel(name = "圖片尺寸")
    private String imageSize;

    /**
     * 是否為預設選單
     */
    @Excel(name = "預設選單", readConverterExp = "0=否,1=是")
    private Integer isDefault;

    /**
     * 是否為當前使用中的選單
     */
    @Excel(name = "使用中", readConverterExp = "0=否,1=是")
    private Integer selected;

    /**
     * 狀態
     */
    @Excel(name = "狀態")
    private RichMenuStatus status;

    /**
     * LINE 平台返回的 richMenuId（發布後才有值）
     */
    @Size(max = 255, message = "richMenuId長度不能超過255個字元")
    @Excel(name = "LINE Rich Menu ID")
    private String richMenuId;

    /**
     * 建議的 Alias ID（發布時自動建立 Alias）
     */
    @Size(max = 32, message = "Alias ID長度不能超過32個字元")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Alias ID只能包含小寫字母、數字和連字號")
    private String suggestedAliasId;

    /**
     * 前一個 Rich Menu ID（用於異常回滾）
     */
    @Size(max = 100, message = "前一個richMenuId長度不能超過100個字元")
    private String previousRichMenuId;

    /**
     * 前一版本配置快照（JSON格式，用於緊急恢復）
     */
    private String previousConfig;

    /**
     * Rich Menu 區塊設定（JSON 格式）
     */
    private String areasJson;

    /**
     * 版本號
     */
    @Excel(name = "版本")
    private Integer version;

    /**
     * 關聯的頻道設定（查詢時填充）
     */
    private LineConfig lineConfig;

    /**
     * 頻道名稱（用於列表顯示，查詢時填充）
     */
    @Excel(name = "頻道名稱")
    private String channelName;

    /**
     * 取得版型類型代碼（用於資料庫儲存）
     */
    public String getTemplateTypeCode() {
        return templateType != null ? templateType.getCode() : null;
    }

    /**
     * 設定版型類型（從資料庫讀取）
     */
    public void setTemplateTypeCode(String code) {
        this.templateType = code != null ? RichMenuTemplateType.fromCode(code) : null;
    }

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
        this.status = code != null ? RichMenuStatus.fromCode(code) : null;
    }

    /**
     * 判斷是否為預設選單
     */
    public boolean isDefaultMenu() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 判斷是否為使用中的選單
     */
    public boolean isSelectedMenu() {
        return selected != null && selected == 1;
    }

    /**
     * 判斷是否已發布到 LINE 平台
     */
    public boolean isPublished() {
        return richMenuId != null && !richMenuId.trim().isEmpty();
    }

    /**
     * 判斷是否為草稿狀態
     */
    public boolean isDraft() {
        return status == RichMenuStatus.DRAFT;
    }

    /**
     * 判斷是否為啟用狀態
     */
    public boolean isActive() {
        return status == RichMenuStatus.ACTIVE;
    }
}
