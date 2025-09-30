package com.cheng.system.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和部門關聯 sys_role_dept
 *
 * @author cheng
 */
@Setter
@Getter
public class SysRoleDept {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部門ID
     */
    private Long deptId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("deptId", getDeptId())
                .toString();
    }
}
