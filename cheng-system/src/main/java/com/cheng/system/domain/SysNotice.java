package com.cheng.system.domain;

import com.cheng.common.core.domain.BaseEntity;
import com.cheng.common.xss.Xss;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 通知公告表 sys_notice
 *
 * @author cheng
 */
@Setter
public class SysNotice extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @Getter
    private Long noticeId;

    /**
     * 公告標題
     */
    private String noticeTitle;

    /**
     * 公告類型（1通知 2公告）
     */
    @Getter
    private String noticeType;

    /**
     * 公告内容
     */
    @Getter
    private String noticeContent;

    /**
     * 公告狀態（0正常 1關閉）
     */
    @Getter
    private String status;

    @Xss(message = "公告標題不能包含腳本字串")
    @NotBlank(message = "公告標題不能為空")
    @Size(min = 0, max = 50, message = "公告標題不能超過50個字串")
    public String getNoticeTitle() {
        return noticeTitle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("noticeId", getNoticeId())
                .append("noticeTitle", getNoticeTitle())
                .append("noticeType", getNoticeType())
                .append("noticeContent", getNoticeContent())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
