package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;

/**
 * 系統標籤物件 sys_tag
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTag extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 標籤ID
     */
    @Excel(name = "標籤ID")
    private Long tagId;

    /**
     * 標籤名稱
     */
    @Excel(name = "標籤名稱")
    @NotBlank(message = "標籤名稱不能為空")
    @Size(max = 100, message = "標籤名稱長度不能超過100個字元")
    private String tagName;

    /**
     * 標籤代碼（唯一，自動產生）
     */
    @Excel(name = "標籤代碼")
    @Size(max = 100, message = "標籤代碼長度不能超過100個字元")
    private String tagCode;

    /**
     * 標籤顏色
     */
    @Excel(name = "標籤顏色")
    @Size(max = 20, message = "標籤顏色長度不能超過20個字元")
    private String tagColor;

    /**
     * 標籤描述
     */
    @Excel(name = "標籤描述")
    @Size(max = 500, message = "標籤描述長度不能超過500個字元")
    private String tagDescription;

    /**
     * 適用平台範圍：ALL=全平台/LINE=僅LINE/INVENTORY=僅庫存，多值以逗號分隔
     */
    @Excel(name = "適用平台")
    private String platformScope;

    /**
     * LINE 使用者數量
     */
    @Excel(name = "LINE使用者數")
    private Integer userCount;

    /**
     * 庫存物品數量
     */
    @Excel(name = "庫存物品數")
    private Integer itemCount;

    /**
     * 狀態：0=停用, 1=啟用
     */
    @Excel(name = "狀態", readConverterExp = "0=停用,1=啟用")
    private Integer status;

    /**
     * 排序順序
     */
    @Excel(name = "排序")
    private Integer sortOrder;

    /**
     * 平台範圍常數
     */
    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_LINE = "LINE";
    public static final String SCOPE_INVENTORY = "INVENTORY";

    /**
     * 檢查標籤是否適用於指定平台
     */
    public boolean isApplicableTo(String platform) {
        if (platformScope == null || SCOPE_ALL.equals(platformScope)) {
            return true;
        }
        return platformScope.contains(platform);
    }
}
