package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 職位表 sys_post
 *
 * @author cheng
 */
@Setter
public class SysPost extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 職位序號
     */
    @Getter
    @Excel(name = "職位序號", cellType = ColumnType.NUMERIC)
    private Long postId;

    /**
     * 職位編碼
     */
    @Excel(name = "職位編碼")
    private String postCode;

    /**
     * 職位名稱
     */
    @Excel(name = "職位名稱")
    private String postName;

    /**
     * 職位排序
     */
    @Excel(name = "職位排序")
    private Integer postSort;

    /**
     * 狀態（0正常 1停用）
     */
    @Getter
    @Excel(name = "狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 使用者是否存在此職位標識 預設不存在
     */
    private boolean flag = false;

    @NotBlank(message = "職位編碼不能為空")
    @Size(min = 0, max = 64, message = "職位編碼長度不能超過64個字串")
    public String getPostCode() {
        return postCode;
    }

    @NotBlank(message = "職位名稱不能為空")
    @Size(min = 0, max = 50, message = "職位名稱長度不能超過50個字串")
    public String getPostName() {
        return postName;
    }

    @NotNull(message = "顯示順序不能為空")
    public Integer getPostSort() {
        return postSort;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("postId", getPostId())
                .append("postCode", getPostCode())
                .append("postName", getPostName())
                .append("postSort", getPostSort())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
