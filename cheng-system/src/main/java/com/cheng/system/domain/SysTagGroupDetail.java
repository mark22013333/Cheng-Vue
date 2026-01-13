package com.cheng.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 標籤群組明細表
 *
 * @author cheng
 */
@Data
public class SysTagGroupDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 運算子常量 */
    public static final String OPERATOR_AND = "AND";
    public static final String OPERATOR_OR = "OR";

    /** 主鍵ID */
    private Long id;

    /** 群組ID */
    private Long groupId;

    /** 規則順序（1-5） */
    private Integer groupIndex;

    /** 標籤ID */
    private Long tagId;

    /** 運算子：AND/OR */
    private String operator;

    /** 建立者 */
    private String createBy;

    /** 建立時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 標籤名稱（非資料庫欄位，JOIN 查詢用） */
    private String tagName;

    /** 標籤顏色（非資料庫欄位，JOIN 查詢用） */
    private String tagColor;
}
