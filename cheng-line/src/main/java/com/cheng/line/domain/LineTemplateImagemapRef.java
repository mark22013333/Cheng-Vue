package com.cheng.line.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 訊息範本與圖文範本關聯物件 line_template_imagemap_ref
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineTemplateImagemapRef extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 關聯ID
     */
    private Long refId;

    /**
     * 訊息範本ID
     */
    private Long templateId;

    /**
     * 圖文範本ID
     */
    private Long imagemapId;

    /**
     * 訊息索引（多訊息組合時的位置）
     */
    private Integer messageIndex;

    /**
     * 訊息範本名稱（非資料庫欄位，用於顯示）
     */
    private String templateName;

    /**
     * 圖文範本名稱（非資料庫欄位，用於顯示）
     */
    private String imagemapName;
}
