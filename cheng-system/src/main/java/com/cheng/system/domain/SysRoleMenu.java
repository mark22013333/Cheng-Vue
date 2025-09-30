package com.cheng.system.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和選單關聯 sys_role_menu
 *
 * @author cheng
 */
@Setter
@Getter
public class SysRoleMenu {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 選單ID
     */
    private Long menuId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("menuId", getMenuId())
                .toString();
    }
}
