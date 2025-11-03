package com.cheng.quartz.service.impl;

import com.cheng.common.constant.ScheduleConstants;
import com.cheng.common.exception.job.TaskException;
import com.cheng.quartz.domain.SysJob;
import com.cheng.quartz.mapper.SysJobMapper;
import com.cheng.quartz.service.ISysJobService;
import com.cheng.quartz.util.CronUtils;
import com.cheng.quartz.util.ScheduleUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定時任務呼叫訊息 服務層
 *
 * @author cheng
 */
@Slf4j
@Service
public class SysJobServiceImpl implements ISysJobService {
    @Resource
    private Scheduler scheduler;

    @Resource
    private SysJobMapper jobMapper;

    /**
     * 專案啟動時，初始化定時器 主要是防止手動修改資料庫導致未同步到定時任務處理（註：不能手動修改資料庫ID和任務組名，否則會導致脏數據）
     * 使用 ApplicationReadyEvent 確保在應用完全啟動（包括 Flyway 執行完畢）後才執行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() throws SchedulerException, TaskException {
        scheduler.clear();
        List<SysJob> jobList = jobMapper.selectJobAll();
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    /**
     * 取得quartz呼叫器的計畫任務列表
     *
     * @param job 呼叫訊息
     */
    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return jobMapper.selectJobList(job);
    }

    /**
     * 通過呼叫任務ID查詢呼叫訊息
     *
     * @param jobId 呼叫任務ID
     * @return 呼叫任務物件訊息
     */
    @Override
    public SysJob selectJobById(Long jobId) {
        return jobMapper.selectJobById(jobId);
    }

    /**
     * 暫停任務
     *
     * @param job 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = jobMapper.updateJob(job);
        if (rows > 0) {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 恢複任務
     *
     * @param job 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
        int rows = jobMapper.updateJob(job);
        if (rows > 0) {
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 刪除任務後，所對應的trigger也將被刪除
     *
     * @param job 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();

        // 先刪除 Quartz 的排程
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            boolean deleted = scheduler.deleteJob(jobKey);
            if (!deleted) {
                log.warn("刪除 Quartz Job 失敗: jobId={}, jobGroup={}", jobId, jobGroup);
                throw new SchedulerException("刪除 Quartz Job 失敗");
            }
            log.info("已刪除 Quartz Job: jobId={}, jobGroup={}", jobId, jobGroup);
        }

        // 再刪除資料庫記錄
        int rows = jobMapper.deleteJobById(jobId);
        if (rows == 0) {
            log.warn("刪除 sys_job 記錄失敗: jobId={}", jobId);
        }

        return rows;
    }

    /**
     * 批次刪除呼叫訊息
     *
     * @param jobIds 需要刪除的任務ID
     * @return 結果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException {
        for (Long jobId : jobIds) {
            SysJob job = jobMapper.selectJobById(jobId);
            deleteJob(job);
        }
    }

    /**
     * 任務呼叫狀態修改
     *
     * @param job 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(SysJob job) throws SchedulerException {
        int rows = 0;
        String status = job.getStatus();
        if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
            rows = resumeJob(job);
        } else if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
            rows = pauseJob(job);
        }
        return rows;
    }

    /**
     * 立即執行任務
     *
     * @param job 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean run(SysJob job) throws SchedulerException {
        boolean result = false;
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        SysJob properties = selectJobById(job.getJobId());
        // 參數
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            result = true;
            scheduler.triggerJob(jobKey, dataMap);
        }
        return result;
    }

    /**
     * 新增任務
     *
     * @param job 呼叫訊息 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertJob(SysJob job) throws SchedulerException, TaskException {
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = jobMapper.insertJob(job);
        if (rows > 0) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return rows;
    }

    /**
     * 更新任務的時間表達式
     *
     * @param job 呼叫訊息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateJob(SysJob job) throws SchedulerException, TaskException {
        SysJob properties = selectJobById(job.getJobId());
        int rows = jobMapper.updateJob(job);
        if (rows > 0) {
            updateSchedulerJob(job, properties.getJobGroup());
        }
        return rows;
    }

    /**
     * 更新任務
     *
     * @param job      任務物件
     * @param jobGroup 任務組名
     */
    public void updateSchedulerJob(SysJob job, String jobGroup) throws SchedulerException, TaskException {
        Long jobId = job.getJobId();
        // 判斷是否存在
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            // 防止建立時存在數據問題 先移除，然後在執行建立操作
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }

    /**
     * 校驗Cron表達式是否有效
     *
     * @param cronExpression 表達式
     * @return 結果
     */
    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }
}
