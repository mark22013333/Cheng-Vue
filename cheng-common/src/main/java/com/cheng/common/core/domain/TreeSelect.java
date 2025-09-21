package com.cheng.common.core.domain;

import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.entity.SysDept;
import com.cheng.common.core.domain.entity.SysMenu;
import com.cheng.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TreeSelect樹結構實體類
 *
 * @author cheng
 */
public class TreeSelect implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 節點ID
     */
    private Long id;

    /**
     * 節點名稱
     */
    private String label;

    /**
     * 節點禁用
     */
    private boolean disabled = false;

    /**
     * 子節點
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect() {

    }

    public TreeSelect(SysDept dept) {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());
        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public TreeSelect(SysMenu menu) {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<TreeSelect> getChildren() {
        return children;
    }

    public void setChildren(List<TreeSelect> children) {
        this.children = children;
    }
}
