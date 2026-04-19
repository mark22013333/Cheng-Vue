package com.cheng.quartz.provider;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.common.enums.TaskParamType;
import com.cheng.quartz.enums.ScheduledTaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 資料處理任務類型提供者
 * 向排程模組提供所有可用的資料處理任務類型（CSV 匯入等）
 *
 * @author Cheng
 * @since 2026-04-04
 */
@Slf4j
@Component
public class DataTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.DATA;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        return Arrays.stream(ScheduledTaskType.values())
                .filter(type -> "資料處理".equals(type.getCategory()))
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 80;
    }

    private TaskTypeOption buildTaskTypeOption(ScheduledTaskType type) {
        List<TaskParamMetadata> params = type.getParameters().stream()
                .map(param -> TaskParamMetadata.builder()
                        .name(param.getName())
                        .label(param.getDescription())
                        .type(TaskParamType.valueOf(param.getType()))
                        .required(param.isRequired())
                        .defaultValue(param.getExample())
                        .description(param.getDescription())
                        .visible(true)
                        .order(0)
                        .build())
                .collect(Collectors.toList());

        return TaskTypeOption.builder()
                .code(type.getCode())
                .label(type.getName())
                .category(TaskCategory.DATA.getCode())
                .description(type.getDescription())
                .beanName(type.getBeanName())
                .defaultMethod(type.getMethodName())
                .params(params)
                .enabled(true)
                .build();
    }
}
