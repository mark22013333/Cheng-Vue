package com.cheng.quartz.service.impl;

import com.cheng.quartz.domain.SysJobLog;
import com.cheng.quartz.mapper.SysJobLogMapper;
import com.cheng.quartz.service.ISysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定時任務呼叫日誌訊息 服務層
 *
 * @author cheng
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService
{
    @Autowired
    private SysJobLogMapper jobLogMapper;

    /**
     * 取得quartz呼叫器日誌的計畫任務
     *
     * @param jobLog 呼叫日誌訊息
     * @return 呼叫任務日誌集合
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog)
    {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     * 通過呼叫任務日誌ID查詢呼叫訊息
     *
     * @param jobLogId 呼叫任務日誌ID
     * @return 呼叫任務日誌物件訊息
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId)
    {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     * 新增任務日誌
     *
     * @param jobLog 呼叫日誌訊息
     */
    @Override
    public void addJobLog(SysJobLog jobLog)
    {
        jobLogMapper.insertJobLog(jobLog);
    }

    /**
     * 批量刪除呼叫日誌訊息
     *
     * @param logIds 需要刪除的數據ID
     * @return 結果
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds)
    {
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    /**
     * 刪除任務日誌
     *
     * @param jobId 呼叫日誌ID
     */
    @Override
    public int deleteJobLogById(Long jobId)
    {
        return jobLogMapper.deleteJobLogById(jobId);
    }

    /**
     * 清除任務日誌
     */
    @Override
    public void cleanJobLog()
    {
        jobLogMapper.cleanJobLog();
    }
}
