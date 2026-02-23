package com.cheng.quartz.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.Constants;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.enums.TaskCategory;
import com.cheng.common.exception.job.TaskException;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.quartz.domain.SysJob;
import com.cheng.quartz.service.ISysJobService;
import com.cheng.quartz.util.CronUtils;
import com.cheng.quartz.util.ScheduleUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 呼叫任務訊息操作處理
 *
 * @author cheng
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/job")
public class SysJobController extends BaseController {

    private final ISysJobService jobService;

    /**
     * 注入所有 TaskTypeProvider（可選，若無提供者則為空列表）
     */
    private final List<TaskTypeProvider> taskTypeProviders;

    /**
     * 取得所有任務類型選項
     * 供前端新增/編輯排程任務時，動態產生表單使用
     *
     * @return 任務類型選項，按分類分組
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.LIST + "')")
    public AjaxResult getTaskTypes() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 如果沒有任何 TaskTypeProvider，返回空結果
        if (taskTypeProviders == null || taskTypeProviders.isEmpty()) {
            result.put("categories", Collections.emptyList());
            result.put("tasks", Collections.emptyMap());
            return success(result);
        }

        // 按優先級排序提供者
        List<TaskTypeProvider> sortedProviders = taskTypeProviders.stream()
                .filter(TaskTypeProvider::isEnabled)
                .sorted(Comparator.comparingInt(TaskTypeProvider::getPriority))
                .toList();

        // 收集所有分類
        List<Map<String, String>> categories = new ArrayList<>();
        Map<String, List<TaskTypeOption>> tasksByCategory = new LinkedHashMap<>();

        for (TaskTypeProvider provider : sortedProviders) {
            TaskCategory category = provider.getCategory();
            List<TaskTypeOption> tasks = provider.getTaskTypes();

            if (tasks != null && !tasks.isEmpty()) {
                // 新增分類資訊
                Map<String, String> categoryInfo = new LinkedHashMap<>();
                categoryInfo.put("code", category.getCode());
                categoryInfo.put("label", category.getLabel());
                categories.add(categoryInfo);

                // 新增任務列表
                tasksByCategory.put(category.getCode(), tasks);
            }
        }

        result.put("categories", categories);
        result.put("tasks", tasksByCategory);

        return success(result);
    }

    /**
     * 取得特定分類的任務類型
     *
     * @param category 任務分類代碼（例如：crawler, notification）
     * @return 任務類型選項列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.LIST + "')")
    @GetMapping("/taskTypes/{category}")
    public AjaxResult getTaskTypesByCategory(@PathVariable String category) {
        if (taskTypeProviders == null || taskTypeProviders.isEmpty()) {
            return success(Collections.emptyList());
        }

        List<TaskTypeOption> tasks = taskTypeProviders.stream()
                .filter(TaskTypeProvider::isEnabled)
                .filter(provider -> provider.getCategory().getCode().equalsIgnoreCase(category))
                .flatMap(provider -> provider.getTaskTypes().stream())
                .collect(Collectors.toList());

        return success(tasks);
    }

    /**
     * 查詢定時任務列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(SysJob sysJob) {
        startPage();
        List<SysJob> list = jobService.selectJobList(sysJob);
        return getDataTable(list);
    }

    /**
     * 匯出定時任務列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.EXPORT + "')")
    @Log(title = "定時任務", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysJob sysJob) {
        List<SysJob> list = jobService.selectJobList(sysJob);
        ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
        util.exportExcel(response, list, "定時任務");
    }

    /**
     * 取得定時任務詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.QUERY + "')")
    @GetMapping(value = "/{jobId}")
    public AjaxResult getInfo(@PathVariable("jobId") Long jobId) {
        return success(jobService.selectJobById(jobId));
    }

    /**
     * 新增定時任務
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.ADD + "')")
    @Log(title = "定時任務", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysJob job) throws SchedulerException, TaskException {
        if (!CronUtils.isValid(job.getCronExpression())) {
            return error("新增任務'" + job.getJobName() + "'失敗，Cron表達式不正確");
        } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
            return error("新增任務'" + job.getJobName() + "'失敗，目標字串不允許'rmi'呼叫");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
            return error("新增任務'" + job.getJobName() + "'失敗，目標字串不允許'ldap(s)'呼叫");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
            return error("新增任務'" + job.getJobName() + "'失敗，目標字串不允許'http(s)'呼叫");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
            return error("新增任務'" + job.getJobName() + "'失敗，目標字串存在違規");
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            return error("新增任務'" + job.getJobName() + "'失敗，目標字串不在白名單内");
        }
        job.setCreateBy(getUsername());
        return toAjax(jobService.insertJob(job));
    }

    /**
     * 修改定時任務
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.EDIT + "')")
    @Log(title = "定時任務", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException, TaskException {
        if (!CronUtils.isValid(job.getCronExpression())) {
            return error("修改任務'" + job.getJobName() + "'失敗，Cron表達式不正確");
        } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
            return error("修改任務'" + job.getJobName() + "'失敗，目標字串不允許'rmi'呼叫");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
            return error("修改任務'" + job.getJobName() + "'失敗，目標字串不允許'ldap(s)'呼叫");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
            return error("修改任務'" + job.getJobName() + "'失敗，目標字串不允許'http(s)'呼叫");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
            return error("修改任務'" + job.getJobName() + "'失敗，目標字串存在違規");
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            return error("修改任務'" + job.getJobName() + "'失敗，目標字串不在白名單内");
        }
        job.setUpdateBy(getUsername());
        return toAjax(jobService.updateJob(job));
    }

    /**
     * 定時任務狀態修改
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.CHANGE_STATUS + "')")
    @Log(title = "定時任務", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException {
        SysJob newJob = jobService.selectJobById(job.getJobId());
        newJob.setStatus(job.getStatus());
        return toAjax(jobService.changeStatus(newJob));
    }

    /**
     * 定時任務立即執行一次
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.CHANGE_STATUS + "')")
    @Log(title = "定時任務", businessType = BusinessType.UPDATE)
    @PutMapping("/run")
    public AjaxResult run(@RequestBody SysJob job) throws SchedulerException {
        boolean result = jobService.run(job);
        return result ? success() : error("任務不存在或已過期！");
    }

    /**
     * 刪除定時任務
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Job.REMOVE + "')")
    @Log(title = "定時任務", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public AjaxResult remove(@PathVariable Long[] jobIds) throws SchedulerException {
        jobService.deleteJobByIds(jobIds);
        return success();
    }
}
