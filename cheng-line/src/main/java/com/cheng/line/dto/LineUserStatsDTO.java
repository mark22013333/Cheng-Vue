package com.cheng.line.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LINE 使用者統計資料 DTO
 *
 * @author cheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineUserStatsDTO {

    /**
     * 總使用者數
     */
    private Long totalUsers;

    /**
     * 關注中數量
     */
    private Long followingCount;

    /**
     * 已綁定數量
     */
    private Long boundCount;

    /**
     * 已封鎖數量（取消關注或封鎖頻道）
     */
    private Long unfollowedCount;

    /**
     * 黑名單數量（管理者設定）
     */
    private Long blacklistedCount;

    /**
     * 本週新增數量
     */
    private Long weekNewCount;

    /**
     * 本月新增數量
     */
    private Long monthNewCount;

    /**
     * 今日新增數量
     */
    private Long todayNewCount;
}
