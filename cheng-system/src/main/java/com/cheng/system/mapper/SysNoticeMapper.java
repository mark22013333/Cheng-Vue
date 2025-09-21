package com.cheng.system.mapper;

import com.cheng.system.domain.SysNotice;

import java.util.List;

/**
 * 通知公告表 數據層
 *
 * @author cheng
 */
public interface SysNoticeMapper
{
    /**
     * 查詢公告訊息
     * 
     * @param noticeId 公告ID
     * @return 公告訊息
     */
    public SysNotice selectNoticeById(Long noticeId);

    /**
     * 查詢公告列表
     *
     * @param notice 公告訊息
     * @return 公告集合
     */
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     *
     * @param notice 公告訊息
     * @return 結果
     */
    public int insertNotice(SysNotice notice);

    /**
     * 修改公告
     *
     * @param notice 公告訊息
     * @return 結果
     */
    public int updateNotice(SysNotice notice);

    /**
     * 批量刪除公告
     * 
     * @param noticeId 公告ID
     * @return 結果
     */
    public int deleteNoticeById(Long noticeId);

    /**
     * 批量刪除公告訊息
     *
     * @param noticeIds 需要刪除的公告ID
     * @return 結果
     */
    public int deleteNoticeByIds(Long[] noticeIds);
}
