package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
 * 標籤群組主表
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTagGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台範圍常量
     */
    public static final String SCOPE_LINE = "LINE";
    public static final String SCOPE_INVENTORY = "INVENTORY";

    /**
     * 運算模式常量
     */
    public static final String CALC_MODE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
    public static final String CALC_MODE_OR_OF_AND = "OR_OF_AND";

    /**
     * 群組ID
     */
    private Long groupId;

    /**
     * 群組名稱
     */
    @Excel(name = "群組名稱")
    private String groupName;

    /**
     * 群組代碼
     */
    @Excel(name = "群組代碼")
    private String groupCode;

    /**
     * 適用平台：LINE/INVENTORY
     */
    @Excel(name = "適用平台")
    private String platformScope;

    /**
     * 運算模式：LEFT_TO_RIGHT/OR_OF_AND
     */
    @Excel(name = "運算模式")
    private String calcMode;

    /**
     * 符合群組的結果數量
     */
    @Excel(name = "結果數量")
    private Integer countResult;

    /**
     * 最後運算時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCalcTime;

    /**
     * 群組描述
     */
    private String description;

    /**
     * 狀態：0=停用, 1=啟用
     */
    @Excel(name = "狀態", readConverterExp = "0=停用,1=啟用")
    private Integer status;

    /**
     * 群組明細（非資料庫欄位）
     */
    private List<SysTagGroupDetail> details;
}
