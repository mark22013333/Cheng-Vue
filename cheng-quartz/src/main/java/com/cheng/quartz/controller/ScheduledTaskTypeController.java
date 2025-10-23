package com.cheng.quartz.controller;

import com.cheng.quartz.enums.ScheduledTaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 定時任務類型 API
 * <p>
 * 提供前端查詢可用的定時任務類型，包含任務的元資料資訊
 * 讓前端可以動態產生任務設定表單
 *
 * @author Cheng
 * @since 2025-03-28
 */
@Slf4j
@RestController
@RequestMapping("/monitor/job/types")
public class ScheduledTaskTypeController {

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
        List<TaskTypeVO> taskTypes = ScheduledTaskType.all().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
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
        List<TaskTypeVO> taskTypes = ScheduledTaskType.byCategory(category).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
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
        List<String> categories = ScheduledTaskType.getAllCategories();
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
        ScheduledTaskType taskType = ScheduledTaskType.of(code);
        if (taskType == null) {
            return Result.error("找不到任務類型: " + code);
        }
        return Result.success(convertToVO(taskType));
    }

    /**
     * 轉換為前端 VO
     */
    private TaskTypeVO convertToVO(ScheduledTaskType taskType) {
        TaskTypeVO vo = new TaskTypeVO();
        vo.setCode(taskType.getCode());
        vo.setName(taskType.getName());
        vo.setDescription(taskType.getDescription());
        vo.setBeanName(taskType.getBeanName());
        vo.setMethodName(taskType.getMethodName());
        vo.setSuggestedCron(taskType.getSuggestedCron());
        vo.setCategory(taskType.getCategory());
        vo.setDefaultJobName(taskType.getDefaultJobName());

        // 轉換參數列表
        List<ParameterVO> parameters = taskType.getParameters().stream()
                .map(param -> {
                    ParameterVO paramVO = new ParameterVO();
                    paramVO.setName(param.getName());
                    paramVO.setType(param.getType());
                    paramVO.setRequired(param.isRequired());
                    paramVO.setDescription(param.getDescription());
                    paramVO.setExample(param.getExample());
                    return paramVO;
                })
                .collect(Collectors.toList());
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
