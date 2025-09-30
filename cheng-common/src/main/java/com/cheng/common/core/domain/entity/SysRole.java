package com.cheng.common.core.domain.entity;

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
import java.util.Set;

/**
 * 角色表 sys_role
 *
 * @author cheng
 */
@Setter
public class SysRole extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @Getter
    @Excel(name = "角色序號", cellType = ColumnType.NUMERIC)
    private Long roleId;

    /**
     * 角色名稱
     */
    @Excel(name = "角色名稱")
    private String roleName;

    /**
     * 角色權限
     */
    @Excel(name = "角色權限")
    private String roleKey;

    /**
     * 角色排序
     */
    @Excel(name = "角色排序")
    private Integer roleSort;

    /**
     * 數據範圍（1：所有數據權限；2：自定義數據權限；3：本部門數據權限；4：本部門及以下數據權限；5：僅本人數據權限）
     */
    @Getter
    @Excel(name = "數據範圍", readConverterExp = "1=所有數據權限,2=自定義數據權限,3=本部門數據權限,4=本部門及以下數據權限,5=僅本人數據權限")
    private String dataScope;

    /**
     * 選單樹選擇項是否關聯顯示（ 0：父子不互相關聯顯示 1：父子互相關聯顯示）
     */
    @Getter
    private boolean menuCheckStrictly;

    /**
     * 部門樹選擇項是否關聯顯示（0：父子不互相關聯顯示 1：父子互相關聯顯示 ）
     */
    @Getter
    private boolean deptCheckStrictly;

    /**
     * 角色狀態（0正常 1停用）
     */
    @Getter
    @Excel(name = "角色狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 刪除標誌（0代表存在 2代表刪除）
     */
    @Getter
    private String delFlag;

    /**
     * 使用者是否存在此角色標識 預設不存在
     */
    private boolean flag = false;

    /**
     * 選單組
     */
    @Getter
    private Long[] menuIds;

    /**
     * 部門組（數據權限）
     */
    @Getter
    private Long[] deptIds;

    /**
     * 角色選單權限
     */
    @Getter
    private Set<String> permissions;

    public SysRole() {

    }

    public SysRole(Long roleId) {
        this.roleId = roleId;
    }

    public static boolean isAdmin(Long roleId) {
        return roleId != null && 1L == roleId;
    }

    public boolean isAdmin() {
        return isAdmin(this.roleId);
    }

    @NotBlank(message = "角色名稱不能為空")
    @Size(min = 0, max = 30, message = "角色名稱長度不能超過30個字串")
    public String getRoleName() {
        return roleName;
    }

    @NotBlank(message = "權限字串不能為空")
    @Size(min = 0, max = 100, message = "權限字串長度不能超過100個字串")
    public String getRoleKey() {
        return roleKey;
    }

    @NotNull(message = "顯示順序不能為空")
    public Integer getRoleSort() {
        return roleSort;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("roleName", getRoleName())
                .append("roleKey", getRoleKey())
                .append("roleSort", getRoleSort())
                .append("dataScope", getDataScope())
                .append("menuCheckStrictly", isMenuCheckStrictly())
                .append("deptCheckStrictly", isDeptCheckStrictly())
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
