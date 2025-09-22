package com.cheng.quartz.mapper;

import com.cheng.quartz.domain.SysJob;

import java.util.List;

/**
 * 呼叫任務訊息 數據層
 *
 * @author cheng
 */
public interface SysJobMapper
{
    /**
     * 查詢呼叫任務日誌集合
     *
     * @param job 呼叫訊息
     * @return 操作日誌集合
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     * 查詢所有呼叫任務
     *
     * @return 呼叫任務列表
     */
    public List<SysJob> selectJobAll();

    /**
     * 通過呼叫ID查詢呼叫任務訊息
     *
     * @param jobId 呼叫ID
     * @return 角色物件訊息
     */
    public SysJob selectJobById(Long jobId);

    /**
     * 通過呼叫ID刪除呼叫任務訊息
     *
     * @param jobId 呼叫ID
     * @return 結果
     */
    public int deleteJobById(Long jobId);

    /**
     * 批次刪除呼叫任務訊息
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    public int deleteJobByIds(Long[] ids);

    /**
     * 修改呼叫任務訊息
     *
     * @param job 呼叫任務訊息
     * @return 結果
     */
    public int updateJob(SysJob job);

    /**
     * 新增呼叫任務訊息
     *
     * @param job 呼叫任務訊息
     * @return 結果
     */
    public int insertJob(SysJob job);
}
