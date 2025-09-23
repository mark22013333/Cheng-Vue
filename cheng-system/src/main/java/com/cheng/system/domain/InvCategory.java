package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 物品分類表 inv_category
 *
 * @author cheng
 */
public class InvCategory extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分類ID
     */
    private Long categoryId;

    /**
     * 父分類ID
     */
    @Excel(name = "父分類ID")
    private Long parentId;

    /**
     * 分類名稱
     */
    @Excel(name = "分類名稱")
    @NotBlank(message = "分類名稱不能為空")
    @Size(min = 0, max = 50, message = "分類名稱長度不能超過50個字元")
    private String categoryName;

    /**
     * 分類編碼
     */
    @Excel(name = "分類編碼")
    @Size(min = 0, max = 30, message = "分類編碼長度不能超過30個字元")
    private String categoryCode;

    /**
     * 排序
     */
    @Excel(name = "排序")
    private Integer sortOrder;

    /**
     * 狀態（0正常 1停用）
     */
    @Excel(name = "狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 刪除標誌（0存在 2刪除）
     */
    private String delFlag;

    public InvCategory() {
    }

    public InvCategory(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("categoryId", getCategoryId())
                .append("parentId", getParentId())
                .append("categoryName", getCategoryName())
                .append("categoryCode", getCategoryCode())
                .append("sortOrder", getSortOrder())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
