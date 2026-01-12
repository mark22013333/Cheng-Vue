package com.cheng.line.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;

/**
 * LINE 使用者標籤關聯物件 line_user_tag_relation
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LineUserTagRelation extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID
     */
    private Long id;

    /**
     * LINE 使用者 ID
     */
    @Excel(name = "LINE使用者ID")
    @NotBlank(message = "LINE使用者ID不能為空")
    private String lineUserId;

    /**
     * 標籤ID
     */
    @Excel(name = "標籤ID")
    @NotNull(message = "標籤ID不能為空")
    private Long tagId;

    /**
     * 關聯的標籤名稱（查詢用，非持久化欄位）
     */
    private transient String tagName;

    /**
     * 關聯的標籤顏色（查詢用，非持久化欄位）
     */
    private transient String tagColor;

    /**
     * 關聯的使用者顯示名稱（查詢用，非持久化欄位）
     */
    private transient String lineDisplayName;
}
