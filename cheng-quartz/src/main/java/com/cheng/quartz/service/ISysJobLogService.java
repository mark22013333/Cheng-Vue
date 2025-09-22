package com.cheng.quartz.service;

import com.cheng.quartz.domain.SysJobLog;

import java.util.List;

/**
 * 定時任務呼叫日誌訊息訊息 服務層
 *
 * @author cheng
 */
public interface ISysJobLogService
{
    /**
     * 取得quartz呼叫器日誌的計畫任務
     *
     * @param jobLog 呼叫日誌訊息
     * @return 呼叫任務日誌集合
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

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
     */
    public void addJobLog(SysJobLog jobLog);

    /**
     * 批次刪除呼叫日誌訊息
     *
     * @param logIds 需要刪除的日誌ID
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
