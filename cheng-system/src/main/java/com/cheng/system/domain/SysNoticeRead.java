package com.cheng.system.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

/**
 * 通知公告已讀記錄表 sys_notice_read
 *
 * @author cheng
 */
@Getter
@Setter
public class SysNoticeRead extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 已讀記錄ID
     */
    private Long readId;

    /**
     * 通知公告ID
     */
    private Long noticeId;

    /**
     * 使用者ID
     */
    private Long userId;

    /**
     * 已讀時間
     */
    private Date readTime;
}
