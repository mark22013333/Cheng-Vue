package com.cheng.shop.security;

import com.alibaba.fastjson2.annotation.JSONField;
import com.cheng.shop.domain.ShopMember;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

/**
 * 商城會員登入資訊
 *
 * @author cheng
 */
@Setter
@Getter
public class ShopMemberLoginUser implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 會員唯一標識
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
     * 登入IP
     */
    private String ipaddr;

    /**
     * 登入地點
     */
    private String loginLocation;

    /**
     * 瀏覽器
     */
    private String browser;

    /**
     * 作業系統
     */
    private String os;

    /**
     * 會員資料
     */
    private ShopMember member;

    public ShopMemberLoginUser() {
    }

    public ShopMemberLoginUser(ShopMember member) {
        this.member = member;
        this.memberId = member != null ? member.getMemberId() : null;
    }

    @JSONField(serialize = false)
    @Override
    public String getPassword() {
        return member != null ? member.getPassword() : null;
    }

    @Override
    public String getUsername() {
        if (member == null) {
            return "";
        }
        if (member.getMobile() != null && !member.getMobile().isBlank()) {
            return member.getMobile();
        }
        if (member.getEmail() != null && !member.getEmail().isBlank()) {
            return member.getEmail();
        }
        return member.getMemberNo() != null ? member.getMemberNo() : "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
