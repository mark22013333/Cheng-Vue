package com.cheng.common.core.domain.entity;

import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 選單權限表 sys_menu
 *
 * @author cheng
 */
@Setter
public class SysMenu extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 選單ID
     */
    @Getter
    private Long menuId;

    /**
     * 選單名稱
     */
    private String menuName;

    /**
     * 父選單名稱
     */
    private String parentName;

    /**
     * 父選單ID
     */
    @Getter
    private Long parentId;

    /**
     * 顯示順序
     */
    private Integer orderNum;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 元件路徑
     */
    private String component;

    /**
     * 路由參數
     */
    @Getter
    private String query;

    /**
     * 路由名稱，預設和路由地址相同的駝峰格式（註意：因為vue3版本的router會刪除名稱相同路由，為避免名字的衝突，特殊情況可以自定義）
     */
    @Getter
    private String routeName;

    /**
     * 是否為外部連結（0是 1否）
     */
    @Getter
    private String isFrame;

    /**
     * 是否暫存（0暫存 1不暫存）
     */
    @Getter
    private String isCache;

    /**
     * 類型（M目錄 C選單 F按鈕）
     */
    private String menuType;

    /**
     * 顯示狀態（0顯示 1隱藏）
     */
    @Getter
    private String visible;

    /**
     * 選單狀態（0正常 1停用）
     */
    @Getter
    private String status;

    /**
     * 權限字串
     */
    private String perms;

    /**
     * 選單圖標
     */
    @Getter
    private String icon;

    /**
     * 子選單
     */
    @Getter
    private List<SysMenu> children = new ArrayList<SysMenu>();

    @NotBlank(message = "選單名稱不能為空")
    @Size(min = 0, max = 50, message = "選單名稱長度不能超過50個字串")
    public String getMenuName() {
        return menuName;
    }

    @NotNull(message = "顯示順序不能為空")
    public Integer getOrderNum() {
        return orderNum;
    }

    @Size(min = 0, max = 200, message = "路由地址不能超過200個字串")
    public String getPath() {
        return path;
    }

    @Size(min = 0, max = 200, message = "元件路徑不能超過255個字串")
    public String getComponent() {
        return component;
    }

    @NotBlank(message = "選單類型不能為空")
    public String getMenuType() {
        return menuType;
    }

    @Size(min = 0, max = 100, message = "權限標識長度不能超過100個字串")
    public String getPerms() {
        return perms;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("menuId", getMenuId())
                .append("menuName", getMenuName())
                .append("parentId", getParentId())
                .append("orderNum", getOrderNum())
                .append("path", getPath())
                .append("component", getComponent())
                .append("query", getQuery())
                .append("routeName", getRouteName())
                .append("isFrame", getIsFrame())
                .append("IsCache", getIsCache())
                .append("menuType", getMenuType())
                .append("visible", getVisible())
                .append("status ", getStatus())
                .append("perms", getPerms())
                .append("icon", getIcon())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
