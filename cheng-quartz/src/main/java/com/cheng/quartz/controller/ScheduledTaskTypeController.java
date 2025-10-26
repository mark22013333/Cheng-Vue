package com.cheng.quartz.controller;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定時任務類型 API
 * <p>
 * 提供前端查詢可用的定時任務類型，包含任務的元資料資訊
 * 讓前端可以動態產生任務設定表單
 * 
 * <p>已重構為使用 TaskTypeProvider 動態系統
 *
 * @author Cheng
 * @since 2025-03-28
 */
@Slf4j
@RestController
@RequestMapping("/monitor/job/types")
public class ScheduledTaskTypeController {

    /**
     * 注入所有 TaskTypeProvider
     */
    @Autowired(required = false)
    private List<TaskTypeProvider> taskTypeProviders;

    /**
     * 取得所有任務類型
     * <p>
     * GET /monitor/job/types
     *
     * @return 所有任務類型列表
     */
    @GetMapping
    public Result getAllTaskTypes() {
        log.info("取得所有定時任務類型");
        
        if (taskTypeProviders == null || taskTypeProviders.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        List<TaskTypeVO> taskTypes = new ArrayList<>();
        
        for (TaskTypeProvider provider : taskTypeProviders) {
            if (!provider.isEnabled()) {
                continue;
            }
            
            String category = provider.getCategory().getLabel();
            List<TaskTypeOption> tasks = provider.getTaskTypes();
            
            for (TaskTypeOption task : tasks) {
                taskTypes.add(convertToVO(task, category));
            }
        }
        
        return Result.success(taskTypes);
    }

    /**
     * 根據分類取得任務類型
     * <p>
     * GET /monitor/job/types/category/{category}
     *
     * @param category 任務分類
     * @return 該分類下的任務類型列表
     */
    @GetMapping("/category/{category}")
    public Result getTaskTypesByCategory(@PathVariable String category) {
        log.info("取得分類 {} 的定時任務類型", category);
        
        if (taskTypeProviders == null || taskTypeProviders.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        List<TaskTypeVO> taskTypes = new ArrayList<>();
        
        for (TaskTypeProvider provider : taskTypeProviders) {
            if (!provider.isEnabled()) {
                continue;
            }
            
            String providerCategory = provider.getCategory().getLabel();
            if (!providerCategory.equals(category)) {
                continue;
            }
            
            List<TaskTypeOption> tasks = provider.getTaskTypes();
            for (TaskTypeOption task : tasks) {
                taskTypes.add(convertToVO(task, providerCategory));
            }
        }
        
        return Result.success(taskTypes);
    }

    /**
     * 取得所有分類
     * <p>
     * GET /monitor/job/types/categories
     *
     * @return 所有分類列表
     */
    @GetMapping("/categories")
    public Result getAllCategories() {
        log.info("取得所有定時任務分類");
        
        if (taskTypeProviders == null || taskTypeProviders.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        List<String> categories = taskTypeProviders.stream()
                .filter(TaskTypeProvider::isEnabled)
                .map(provider -> provider.getCategory().getLabel())
                .distinct()
                .collect(Collectors.toList());
        
        return Result.success(categories);
    }

    /**
     * 根據 code 取得任務類型詳情
     * <p>
     * GET /monitor/job/types/{code}
     *
     * @param code 任務代碼
     * @return 任務類型詳情
     */
    @GetMapping("/{code}")
    public Result getTaskTypeByCode(@PathVariable String code) {
        log.info("取得任務類型詳情: {}", code);
        
        if (taskTypeProviders == null || taskTypeProviders.isEmpty()) {
            return Result.error("找不到任務類型: " + code);
        }
        
        for (TaskTypeProvider provider : taskTypeProviders) {
            if (!provider.isEnabled()) {
                continue;
            }
            
            String category = provider.getCategory().getLabel();
            List<TaskTypeOption> tasks = provider.getTaskTypes();
            
            for (TaskTypeOption task : tasks) {
                if (task.getCode().equals(code)) {
                    return Result.success(convertToVO(task, category));
                }
            }
        }
        
        return Result.error("找不到任務類型: " + code);
    }

    /**
     * 轉換為前端 VO
     */
    private TaskTypeVO convertToVO(TaskTypeOption taskType, String category) {
        TaskTypeVO vo = new TaskTypeVO();
        vo.setCode(taskType.getCode());
        vo.setName(taskType.getLabel());
        vo.setDescription(taskType.getDescription());
        vo.setBeanName(taskType.getBeanName());
        vo.setMethodName(taskType.getDefaultMethod());
        vo.setSuggestedCron(null); // 可以從 TaskTypeOption 擴展此欄位
        vo.setCategory(category);
        vo.setDefaultJobName(taskType.getLabel());

        // 轉換參數列表
        List<ParameterVO> parameters = new ArrayList<>();
        if (taskType.getParams() != null) {
            for (TaskParamMetadata param : taskType.getParams()) {
                ParameterVO paramVO = new ParameterVO();
                paramVO.setName(param.getName());
                paramVO.setType(param.getType().getCode().toUpperCase());
                paramVO.setRequired(param.getRequired());
                paramVO.setDescription(param.getLabel());
                paramVO.setExample(param.getDefaultValue() != null ? param.getDefaultValue() : "");
                paramVO.setOrder(param.getOrder() != null ? param.getOrder() : 0);
                paramVO.setVisible(param.getVisible());
                
                // 轉換選項列表（用於 SELECT 類型）
                if (param.getOptions() != null && !param.getOptions().isEmpty()) {
                    List<OptionVO> options = new ArrayList<>();
                    for (TaskParamMetadata.OptionItem option : param.getOptions()) {
                        OptionVO optionVO = new OptionVO();
                        optionVO.setValue(option.getValue());
                        optionVO.setLabel(option.getLabel());
                        optionVO.setDescription(option.getDescription());
                        options.add(optionVO);
                    }
                    paramVO.setOptions(options);
                }
                
                parameters.add(paramVO);
            }
        }
        vo.setParameters(parameters);

        return vo;
    }

    // ==================== VO 類別 ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskTypeVO {
        private String code;
        private String name;
        private String description;
        private String beanName;
        private String methodName;
        private List<ParameterVO> parameters;
        private String suggestedCron;
        private String category;
        private String defaultJobName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterVO {
        private String name;
        private String type;
        private boolean required;
        private String description;
        private String example;
        private Integer order; // 參數順序
        private List<OptionVO> options; // 下拉選單選項
        private Boolean visible; // 是否顯示在前端
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionVO {
        private String value;
        private String label;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private int code;
        private String message;
        private Object data;

        public static Result success(Object data) {
            return new Result(200, "成功", data);
        }

        public static Result error(String message) {
            return new Result(500, message, null);
        }
    }
}
