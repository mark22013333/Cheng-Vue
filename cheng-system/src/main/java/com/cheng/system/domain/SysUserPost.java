package com.cheng.system.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 使用者和職位關聯 sys_user_post
 *
 * @author cheng
 */
@Setter
@Getter
public class SysUserPost {
    /**
     * 使用者ID
     */
    private Long userId;

    /**
     * 職位ID
     */
    private Long postId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("postId", getPostId())
                .toString();
    }
}
