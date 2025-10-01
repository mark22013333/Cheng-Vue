package com.cheng.quartz.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.Date;

/**
 * 定時任務呼叫日誌表 sys_job_log
 *
 * @author cheng
 */
@Setter
@Getter
public class SysJobLog extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Excel(name = "日誌序號")
    private Long jobLogId;

    /**
     * 任務名稱
     */
    @Excel(name = "任務名稱")
    private String jobName;

    /**
     * 任務組名
     */
    @Excel(name = "任務組名")
    private String jobGroup;

    /**
     * 呼叫目標字串
     */
    @Excel(name = "呼叫目標字串")
    private String invokeTarget;

    /**
     * 日誌訊息
     */
    @Excel(name = "日誌訊息")
    private String jobMessage;

    /**
     * 執行狀態（0正常 1失敗）
     */
    @Excel(name = "執行狀態", readConverterExp = "0=正常,1=失敗")
    private String status;

    /**
     * 異常訊息
     */
    @Excel(name = "異常訊息")
    private String exceptionInfo;

    /**
     * 開始時間
     */
    private Date startTime;

    /**
     * 停止時間
     */
    private Date stopTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("jobLogId", getJobLogId())
                .append("jobName", getJobName())
                .append("jobGroup", getJobGroup())
                .append("jobMessage", getJobMessage())
                .append("status", getStatus())
                .append("exceptionInfo", getExceptionInfo())
                .append("startTime", getStartTime())
                .append("stopTime", getStopTime())
                .toString();
    }
}
