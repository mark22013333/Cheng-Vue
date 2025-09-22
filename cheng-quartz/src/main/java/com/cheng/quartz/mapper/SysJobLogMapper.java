package com.cheng.quartz.mapper;

import com.cheng.quartz.domain.SysJobLog;

import java.util.List;

/**
 * 呼叫任務日誌訊息 數據層
 *
 * @author cheng
 */
public interface SysJobLogMapper
{
    /**
     * 取得quartz呼叫器日誌的計畫任務
     *
     * @param jobLog 呼叫日誌訊息
     * @return 呼叫任務日誌集合
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 查詢所有呼叫任務日誌
     *
     * @return 呼叫任務日誌列表
     */
    public List<SysJobLog> selectJobLogAll();

    /**
     * 通過呼叫任務日誌ID查詢呼叫訊息
     *
     * @param jobLogId 呼叫任務日誌ID
     * @return 呼叫任務日誌物件訊息
     */
    public SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增任務日誌
     *
     * @param jobLog 呼叫日誌訊息
     * @return 結果
     */
    public int insertJobLog(SysJobLog jobLog);

    /**
     * 批次刪除呼叫日誌訊息
     *
     * @param logIds 需要刪除的數據ID
     * @return 結果
     */
    public int deleteJobLogByIds(Long[] logIds);

    /**
     * 刪除任務日誌
     *
     * @param jobId 呼叫日誌ID
     * @return 結果
     */
    public int deleteJobLogById(Long jobId);

    /**
     * 清除任務日誌
     */
    public void cleanJobLog();
}
