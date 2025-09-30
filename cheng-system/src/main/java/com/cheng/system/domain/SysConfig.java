package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 參數設定表 sys_config
 *
 * @author cheng
 */
@Setter
public class SysConfig extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 參數主鍵
     */
    @Getter
    @Excel(name = "參數主鍵", cellType = ColumnType.NUMERIC)
    private Long configId;

    /**
     * 參數名稱
     */
    @Excel(name = "參數名稱")
    private String configName;

    /**
     * 參數鍵名
     */
    @Excel(name = "參數鍵名")
    private String configKey;

    /**
     * 參數鍵值
     */
    @Excel(name = "參數鍵值")
    private String configValue;

    /**
     * 系統內建（Y是 N否）
     */
    @Getter
    @Excel(name = "系統內建", readConverterExp = "Y=是,N=否")
    private String configType;

    @NotBlank(message = "參數名稱不能為空")
    @Size(min = 0, max = 100, message = "參數名稱不能超過100個字串")
    public String getConfigName() {
        return configName;
    }

    @NotBlank(message = "參數鍵名長度不能為空")
    @Size(min = 0, max = 100, message = "參數鍵名長度不能超過100個字串")
    public String getConfigKey() {
        return configKey;
    }

    @NotBlank(message = "參數鍵值不能為空")
    @Size(min = 0, max = 500, message = "參數鍵值長度不能超過500個字串")
    public String getConfigValue() {
        return configValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("configId", getConfigId())
                .append("configName", getConfigName())
                .append("configKey", getConfigKey())
                .append("configValue", getConfigValue())
                .append("configType", getConfigType())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark()).toString();
    }
}
