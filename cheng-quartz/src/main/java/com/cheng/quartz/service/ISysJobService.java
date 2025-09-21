package com.cheng.quartz.service;

import com.cheng.common.exception.job.TaskException;
import com.cheng.quartz.domain.SysJob;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * 定時任務呼叫訊息訊息 服務層
 *
 * @author cheng
 */
public interface ISysJobService
{
    /**
     * 取得quartz呼叫器的計畫任務
     *
     * @param job 呼叫訊息
     * @return 呼叫任務集合
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     * 通過呼叫任務ID查詢呼叫訊息
     *
     * @param jobId 呼叫任務ID
     * @return 呼叫任務物件訊息
     */
    public SysJob selectJobById(Long jobId);

    /**
     * 暫停任務
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public int pauseJob(SysJob job) throws SchedulerException;

    /**
     * 恢複任務
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public int resumeJob(SysJob job) throws SchedulerException;

    /**
     * 刪除任務後，所對應的trigger也將被刪除
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public int deleteJob(SysJob job) throws SchedulerException;

    /**
     * 批量刪除呼叫訊息
     *
     * @param jobIds 需要刪除的任務ID
     * @return 結果
     */
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException;

    /**
     * 任務呼叫狀態修改
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public int changeStatus(SysJob job) throws SchedulerException;

    /**
     * 立即執行任務
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public boolean run(SysJob job) throws SchedulerException;

    /**
     * 新增任務
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public int insertJob(SysJob job) throws SchedulerException, TaskException;

    /**
     * 更新任務
     *
     * @param job 呼叫訊息
     * @return 結果
     */
    public int updateJob(SysJob job) throws SchedulerException, TaskException;

    /**
     * 校驗cron表達式是否有效
     *
     * @param cronExpression 表達式
     * @return 結果
     */
    public boolean checkCronExpressionIsValid(String cronExpression);
}
