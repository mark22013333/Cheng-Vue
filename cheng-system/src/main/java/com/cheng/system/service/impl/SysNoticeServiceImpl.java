package com.cheng.system.service.impl;

import com.cheng.system.domain.SysNotice;
import com.cheng.system.domain.SysNoticeRead;
import com.cheng.system.mapper.SysNoticeMapper;
import com.cheng.system.mapper.SysNoticeReadMapper;
import com.cheng.system.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 公告 服務層實現
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl implements ISysNoticeService {
    private final SysNoticeMapper noticeMapper;
    private final SysNoticeReadMapper noticeReadMapper;

    /**
     * 查詢公告訊息
     *
     * @param noticeId 公告ID
     * @return 公告訊息
     */
    @Override
    public SysNotice selectNoticeById(Long noticeId) {
        return noticeMapper.selectNoticeById(noticeId);
    }

    /**
     * 查詢公告列表
     *
     * @param notice 公告訊息
     * @return 公告集合
     */
    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice) {
        return noticeMapper.selectNoticeList(notice);
    }

    /**
     * 新增公告
     *
     * @param notice 公告訊息
     * @return 結果
     */
    @Override
    public int insertNotice(SysNotice notice) {
        return noticeMapper.insertNotice(notice);
    }

    /**
     * 修改公告
     *
     * @param notice 公告訊息
     * @return 結果
     */
    @Override
    public int updateNotice(SysNotice notice) {
        return noticeMapper.updateNotice(notice);
    }

    /**
     * 刪除公告物件
     *
     * @param noticeId 公告ID
     * @return 結果
     */
    @Override
    public int deleteNoticeById(Long noticeId) {
        return noticeMapper.deleteNoticeById(noticeId);
    }

    /**
     * 批次刪除公告訊息
     *
     * @param noticeIds 需要刪除的公告ID
     * @return 結果
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return noticeMapper.deleteNoticeByIds(noticeIds);
    }

    /**
     * 查詢首頁公告列表（類型=公告，狀態=正常）
     *
     * @return 公告列表
     */
    @Override
    public List<SysNotice> selectAnnouncementList() {
        return noticeMapper.selectAnnouncementList();
    }

    /**
     * 查詢使用者未讀通知列表
     *
     * @param userId 使用者ID
     * @return 未讀通知列表
     */
    @Override
    public List<SysNotice> selectUnreadNotifications(Long userId) {
        return noticeMapper.selectUnreadNotifications(userId);
    }

    /**
     * 標記通知為已讀
     *
     * @param noticeId 通知ID
     * @param userId   使用者ID
     * @return 結果
     */
    @Override
    public int markNoticeAsRead(Long noticeId, Long userId) {
        // 檢查是否已讀
        int readCount = noticeReadMapper.checkUserRead(noticeId, userId);
        if (readCount > 0) {
            return 1; // 已讀過，直接返回成功
        }

        // 建立已讀記錄
        SysNoticeRead noticeRead = new SysNoticeRead();
        noticeRead.setNoticeId(noticeId);
        noticeRead.setUserId(userId);
        noticeRead.setReadTime(new Date());
        return noticeReadMapper.insertNoticeRead(noticeRead);
    }

    /**
     * 取得使用者未讀通知數量
     *
     * @param userId 使用者ID
     * @return 未讀數量
     */
    @Override
    public int countUnreadNotifications(Long userId) {
        return noticeReadMapper.countUnreadNotifications(userId);
    }
}
