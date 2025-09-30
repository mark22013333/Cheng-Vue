package com.cheng.common.core.domain.entity;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 字典數據表 sys_dict_data
 *
 * @author cheng
 */
@Setter
public class SysDictData extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典編碼
     */
    @Getter
    @Excel(name = "字典編碼", cellType = ColumnType.NUMERIC)
    private Long dictCode;

    /**
     * 字典排序
     */
    @Getter
    @Excel(name = "字典排序", cellType = ColumnType.NUMERIC)
    private Long dictSort;

    /**
     * 字典標籤
     */
    @Excel(name = "字典標籤")
    private String dictLabel;

    /**
     * 字典鍵值
     */
    @Excel(name = "字典鍵值")
    private String dictValue;

    /**
     * 字典類型
     */
    @Excel(name = "字典類型")
    private String dictType;

    /**
     * 樣式屬性（其他樣式擴充）
     */
    private String cssClass;

    /**
     * 表格字典樣式
     */
    @Getter
    private String listClass;

    /**
     * 是否預設（Y是 N否）
     */
    @Getter
    @Excel(name = "是否預設", readConverterExp = "Y=是,N=否")
    private String isDefault;

    /**
     * 狀態（0正常 1停用）
     */
    @Getter
    @Excel(name = "狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    @NotBlank(message = "字典標籤不能為空")
    @Size(min = 0, max = 100, message = "字典標籤長度不能超過100個字串")
    public String getDictLabel() {
        return dictLabel;
    }

    @NotBlank(message = "字典鍵值不能為空")
    @Size(min = 0, max = 100, message = "字典鍵值長度不能超過100個字串")
    public String getDictValue() {
        return dictValue;
    }

    @NotBlank(message = "字典類型不能為空")
    @Size(min = 0, max = 100, message = "字典類型長度不能超過100個字串")
    public String getDictType() {
        return dictType;
    }

    @Size(min = 0, max = 100, message = "樣式屬性長度不能超過100個字串")
    public String getCssClass() {
        return cssClass;
    }

    public boolean getDefault() {
        return UserConstants.YES.equals(this.isDefault);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("dictCode", getDictCode())
                .append("dictSort", getDictSort())
                .append("dictLabel", getDictLabel())
                .append("dictValue", getDictValue())
                .append("dictType", getDictType())
                .append("cssClass", getCssClass())
                .append("listClass", getListClass())
                .append("isDefault", getIsDefault())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
