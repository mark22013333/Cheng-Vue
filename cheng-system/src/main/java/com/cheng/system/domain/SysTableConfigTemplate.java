package com.cheng.system.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 表格欄位配置模版物件 sys_table_config_template
 *
 * @author cheng
 */
@Setter
@Getter
public class SysTableConfigTemplate extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 模版ID */
    private Long templateId;

    /** 頁面標識 */
    private String pageKey;

    /** 欄位配置（JSON格式） */
    private String columnConfig;
}
