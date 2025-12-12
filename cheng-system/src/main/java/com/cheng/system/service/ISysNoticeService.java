package com.cheng.system.service;

import com.cheng.system.domain.SysNotice;

import java.util.List;

/**
 * 公告 服務層
 *
 * @author cheng
 */
public interface ISysNoticeService {
    /**
     * 查詢公告訊息
     *
     * @param noticeId 公告ID
     * @return 公告訊息
     */
    SysNotice selectNoticeById(Long noticeId);

    /**
     * 查詢公告列表
     *
     * @param notice 公告訊息
     * @return 公告集合
     */
    List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     *
     * @param notice 公告訊息
     * @return 結果
     */
    int insertNotice(SysNotice notice);

    /**
     * 修改公告
     *
     * @param notice 公告訊息
     * @return 結果
     */
    int updateNotice(SysNotice notice);

    /**
     * 刪除公告訊息
     *
     * @param noticeId 公告ID
     * @return 結果
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批次刪除公告訊息
     *
     * @param noticeIds 需要刪除的公告ID
     * @return 結果
     */
    int deleteNoticeByIds(Long[] noticeIds);

    /**
     * 查詢首頁公告列表（類型=公告，狀態=正常）
     *
     * @return 公告列表
     */
    List<SysNotice> selectAnnouncementList();

    /**
     * 查詢使用者未讀通知列表
     *
     * @param userId 使用者ID
     * @return 未讀通知列表
     */
    List<SysNotice> selectUnreadNotifications(Long userId);

    /**
     * 標記通知為已讀
     *
     * @param noticeId 通知ID
     * @param userId   使用者ID
     * @return 結果
     */
    int markNoticeAsRead(Long noticeId, Long userId);

    /**
     * 取得使用者未讀通知數量
     *
     * @param userId 使用者ID
     * @return 未讀數量
     */
    int countUnreadNotifications(Long userId);
}
