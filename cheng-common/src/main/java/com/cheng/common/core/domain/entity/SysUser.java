package com.cheng.common.core.domain.entity;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.annotation.Excel.Type;
import com.cheng.common.annotation.Excels;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.common.xss.Xss;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
 * 使用者物件 sys_user
 *
 * @author cheng
 */
@Setter
public class SysUser extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 使用者ID
     */
    @Getter
    @Excel(name = "使用者序號", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "使用者編號")
    private Long userId;

    /**
     * 部門ID
     */
    @Getter
    @Excel(name = "部門編號", type = Type.IMPORT)
    private Long deptId;

    /**
     * 使用者帳號
     */
    @Excel(name = "登入名稱")
    private String userName;

    /**
     * 使用者暱稱
     */
    @Excel(name = "使用者名稱")
    private String nickName;

    /**
     * 使用者信箱
     */
    @Excel(name = "使用者信箱")
    private String email;

    /**
     * 手機號碼
     */
    @Excel(name = "手機號碼", cellType = ColumnType.TEXT)
    private String phonenumber;

    /**
     * 使用者性別
     */
    @Getter
    @Excel(name = "使用者性別", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 使用者頭像
     */
    @Getter
    private String avatar;

    /**
     * 密碼
     */
    @Getter
    private String password;

    /**
     * 帳號狀態（0正常 1停用）
     */
    @Getter
    @Excel(name = "帳號狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 刪除標誌（0代表存在 2代表刪除）
     */
    @Getter
    private String delFlag;

    /**
     * 最後登入IP
     */
    @Getter
    @Excel(name = "最後登入IP", type = Type.EXPORT)
    private String loginIp;

    /**
     * 最後登入時間
     */
    @Getter
    @Excel(name = "最後登入時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    /**
     * 密碼最後更新時間
     */
    @Getter
    private Date pwdUpdateDate;

    /**
     * 部門物件
     */
    @Getter
    @Excels({
            @Excel(name = "部門名稱", targetAttr = "deptName", type = Type.EXPORT),
            @Excel(name = "部門負責人", targetAttr = "leader", type = Type.EXPORT)
    })
    private SysDept dept;

    /**
     * 角色物件
     */
    @Getter
    private List<SysRole> roles;

    /**
     * 角色組
     */
    @Getter
    private Long[] roleIds;

    /**
     * 職位組
     */
    @Getter
    private Long[] postIds;

    /**
     * 角色ID
     */
    @Getter
    private Long roleId;

    public SysUser() {

    }

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    @Xss(message = "使用者暱稱不能包含腳本字串")
    @Size(min = 0, max = 30, message = "使用者暱稱長度不能超過30個字串")
    public String getNickName() {
        return nickName;
    }

    @Xss(message = "使用者帳號不能包含腳本字串")
    @NotBlank(message = "使用者帳號不能為空")
    @Size(min = 0, max = 30, message = "使用者帳號長度不能超過30個字串")
    public String getUserName() {
        return userName;
    }

    @Email(message = "信箱格式不正確")
    @Size(min = 0, max = 50, message = "信箱長度不能超過50個字串")
    public String getEmail() {
        return email;
    }

    @Size(min = 0, max = 11, message = "手機號碼長度不能超過11個字串")
    public String getPhonenumber() {
        return phonenumber;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("deptId", getDeptId())
                .append("userName", getUserName())
                .append("nickName", getNickName())
                .append("email", getEmail())
                .append("phonenumber", getPhonenumber())
                .append("sex", getSex())
                .append("avatar", getAvatar())
                .append("password", getPassword())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("loginIp", getLoginIp())
                .append("loginDate", getLoginDate())
                .append("pwdUpdateDate", getPwdUpdateDate())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("dept", getDept())
                .toString();
    }
}
