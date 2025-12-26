package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * LINE 訊息範本物件 line_message_template
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineMessageTemplate extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 範本ID
     */
    private Long templateId;

    /**
     * 範本名稱
     */
    @Excel(name = "範本名稱")
    private String templateName;

    /**
     * 範本代碼
     */
    @Excel(name = "範本代碼")
    private String templateCode;

    /**
     * 訊息類型：TEXT/IMAGE/VIDEO/AUDIO/LOCATION/STICKER/IMAGEMAP/FLEX
     */
    @Excel(name = "訊息類型")
    private String msgType;

    /**
     * 訊息內容（JSON 格式）
     */
    private String content;

    /**
     * 替代文字（Flex/Imagemap 必填）
     */
    @Excel(name = "替代文字")
    private String altText;

    /**
     * 預覽圖 URL
     */
    private String previewImg;

    /**
     * 圖文範本來源ID（當 msg_type=IMAGEMAP 時，標記來源範本）
     */
    private Long imagemapSourceId;

    /**
     * 範本分類ID
     */
    private Long categoryId;

    /**
     * 使用的變數列表（JSON Array）
     */
    private String variables;

    /**
     * 狀態：0=停用, 1=啟用
     */
    @Excel(name = "狀態", readConverterExp = "0=停用,1=啟用")
    private Integer status;

    /**
     * 排序順序
     */
    private Integer sortOrder;

    /**
     * 訊息物件數量（1-5，符合 LINE Push Message API 限制）
     */
    private Integer messageCount;

    /**
     * 使用次數
     */
    @Excel(name = "使用次數")
    private Integer useCount;

    /**
     * 最後使用時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUsedAt;

    /**
     * 刪除標誌
     */
    private String delFlag;

    /**
     * 分類名稱（關聯查詢）
     */
    @Excel(name = "分類名稱")
    private String categoryName;
}
