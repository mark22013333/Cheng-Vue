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
 * 爬蟲任務類型提供者
 * 向排程模組提供所有可用的爬蟲任務類型
 * 
 * <p>使用 {@link ScheduledTaskType} Enum 進行集中管理，優勢：
 * <ul>
 *   <li>明確的類別引用 - IDE 可追蹤使用</li>
 *   <li>編譯時期檢查 - 避免方法名稱錯誤</li>
 *   <li>集中管理 - 所有任務類型定義在一處</li>
 *   <li>類型安全 - 避免字串拼寫錯誤</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-12-08
 */
@Slf4j
@Component
public class CrawlerTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.CRAWLER;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        // 從 ScheduledTaskType Enum 中過濾出爬蟲任務
        return Arrays.stream(ScheduledTaskType.values())
                .filter(type -> "爬蟲任務".equals(type.getCategory()))
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 100; // 爬蟲任務優先級最高
    }

    /**
     * 從 ScheduledTaskType Enum 建立 TaskTypeOption
     * 
     * @param type ScheduledTaskType Enum
     * @return TaskTypeOption
     */
    private TaskTypeOption buildTaskTypeOption(ScheduledTaskType type) {
        // 將 ScheduledTaskType.TaskParameter 轉換為 TaskParamMetadata
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
                .category(TaskCategory.CRAWLER.getCode())
                .description(type.getDescription())
                .beanName(type.getBeanName())
                .defaultMethod(type.getMethodName())
                .params(params)
                .enabled(true)
                .build();
    }
}
