package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.Date;

/**
 * 使用者表格欄位配置物件 sys_user_table_config
 *
 * @author cheng
 * @since 2025-12-06
 */
@Setter
@Getter
public class SysUserTableConfig extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 使用者ID
     */
    @Excel(name = "使用者ID")
    private Long userId;

    /**
     * 頁面標識（如 system_user, inventory_borrow）
     */
    @Excel(name = "頁面標識")
    private String pageKey;

    /**
     * 欄位配置（JSON格式）
     */
    @Excel(name = "欄位配置")
    private String columnConfig;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("configId", getConfigId())
                .append("userId", getUserId())
                .append("pageKey", getPageKey())
                .append("columnConfig", getColumnConfig())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
