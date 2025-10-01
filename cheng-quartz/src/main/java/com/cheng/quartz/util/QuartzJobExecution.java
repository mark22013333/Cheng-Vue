package com.cheng.quartz.util;

import com.cheng.quartz.domain.SysJob;
import org.quartz.JobExecutionContext;

/**
 * 定時任務處理（允許併發執行）
 *
 * @author cheng
 *
 */
public class QuartzJobExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
