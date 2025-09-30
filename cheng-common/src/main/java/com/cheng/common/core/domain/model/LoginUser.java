package com.cheng.common.core.domain.model;

import com.alibaba.fastjson2.annotation.JSONField;
import com.cheng.common.core.domain.entity.SysUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;

/**
 * 登入使用者身份權限
 *
 * @author cheng
 */
@Setter
@Getter
public class LoginUser implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 使用者ID
     */
    private Long userId;

    /**
     * 部門ID
     */
    private Long deptId;

    /**
     * 使用者唯一標識
     */
    private String token;

    /**
     * 登入時間
     */
    private Long loginTime;

    /**
     * 過期時間
     */
    private Long expireTime;

    /**
     * 登入IP位置
     */
    private String ipaddr;

    /**
     * 登入地點
     */
    private String loginLocation;

    /**
     * 瀏覽器類型
     */
    private String browser;

    /**
     * 作業系統
     */
    private String os;

    /**
     * 權限列表
     */
    private Set<String> permissions;

    /**
     * 使用者訊息
     */
    private SysUser user;

    public LoginUser() {
    }

    public LoginUser(SysUser user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    public LoginUser(Long userId, Long deptId, SysUser user, Set<String> permissions) {
        this.userId = userId;
        this.deptId = deptId;
        this.user = user;
        this.permissions = permissions;
    }

    @JSONField(serialize = false)
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    /**
     * 帳號是否未過期,過期無法驗證
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 指定使用者是否解鎖,鎖定的使用者無法進行身份驗證
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 指示是否已過期的使用者的憑證(密碼),過期的憑證防止認證
     */
    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否可用 ,禁用的使用者不能身份驗證
     */
    @JSONField(serialize = false)
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }
}
