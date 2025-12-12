package com.cheng.system.mapper;

import com.cheng.system.domain.SysNoticeRead;
import org.apache.ibatis.annotations.Param;

/**
 * 通知公告已讀記錄 數據層
 *
 * @author cheng
 */
public interface SysNoticeReadMapper {

    /**
     * 新增已讀記錄
     *
     * @param noticeRead 已讀記錄
     * @return 結果
     */
    int insertNoticeRead(SysNoticeRead noticeRead);

    /**
     * 檢查使用者是否已讀某通知
     *
     * @param noticeId 通知ID
     * @param userId   使用者ID
     * @return 已讀記錄數量（0=未讀，1=已讀）
     */
    int checkUserRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    /**
     * 取得使用者未讀通知數量
     *
     * @param userId 使用者ID
     * @return 未讀數量
     */
    int countUnreadNotifications(@Param("userId") Long userId);

    /**
     * 刪除已讀記錄（根據通知ID）
     *
     * @param noticeId 通知ID
     * @return 結果
     */
    int deleteByNoticeId(Long noticeId);

    /**
     * 批次刪除已讀記錄（根據通知ID陣列）
     *
     * @param noticeIds 通知ID陣列
     * @return 結果
     */
    int deleteByNoticeIds(Long[] noticeIds);
}
