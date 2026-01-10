package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * LINE Flex 自訂範本物件 line_flex_template
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineFlexTemplate extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Flex範本ID
     */
    private Long flexTemplateId;

    /**
     * 範本名稱
     */
    @Excel(name = "範本名稱")
    private String templateName;

    /**
     * Flex JSON 內容
     */
    private String flexJson;

    /**
     * 替代文字
     */
    @Excel(name = "替代文字")
    private String altText;

    /**
     * 預覽圖 URL
     */
    private String previewUrl;

    /**
     * 範本說明
     */
    @Excel(name = "範本說明")
    private String description;

    /**
     * 是否公開：0=私人, 1=公開
     */
    @Excel(name = "是否公開", readConverterExp = "0=私人,1=公開")
    private Integer isPublic;

    /**
     * 使用次數
     */
    @Excel(name = "使用次數")
    private Integer useCount;

    /**
     * 建立者使用者ID
     */
    private Long creatorId;

    /**
     * 建立者名稱（冗餘欄位）
     */
    @Excel(name = "建立者")
    private String creatorName;

    /**
     * 狀態：0=停用, 1=啟用
     */
    @Excel(name = "狀態", readConverterExp = "0=停用,1=啟用")
    private Integer status;

    /**
     * 刪除標誌
     */
    private String delFlag;
}
