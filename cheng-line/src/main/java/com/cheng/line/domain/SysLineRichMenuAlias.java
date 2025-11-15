package com.cheng.line.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LINE Rich Menu Alias 物件 sys_line_rich_menu_alias
 * 
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLineRichMenuAlias extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主鍵ID */
    private Long id;

    /** Rich Menu ID（關聯 sys_line_rich_menu.id） */
    private Long richMenuId;

    /** Rich Menu Alias ID（LINE API 識別碼，最多 32 字元） */
    private String aliasId;

    /** 別名描述 */
    private String description;

    // ==================== 關聯欄位 ====================

    /** Rich Menu 名稱（關聯查詢） */
    private String richMenuName;

    /** Rich Menu 的 LINE ID（關聯查詢） */
    private String lineRichMenuId;

    /** Channel 名稱（關聯查詢） */
    private String channelName;
}
