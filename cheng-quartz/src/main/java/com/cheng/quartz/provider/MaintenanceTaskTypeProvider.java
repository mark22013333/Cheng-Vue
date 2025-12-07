package com.cheng.quartz.provider;

import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.quartz.enums.MaintenanceTaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 維護任務類型提供者
 * 向排程模組提供所有可用的維護任務類型
 * 
 * <p>使用 {@link MaintenanceTaskType} Enum 進行集中管理，優勢：
 * <ul>
 *   <li>明確的類別引用 - IDE 可追蹤使用</li>
 *   <li>編譯時期檢查 - 避免方法名稱錯誤</li>
 *   <li>集中管理 - 所有任務類型定義在一處</li>
 *   <li>類型安全 - 避免字串拼寫錯誤</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-12-07
 */
@Slf4j
@Component
public class MaintenanceTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.MAINTENANCE;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        // 直接從 Enum 轉換為 TaskTypeOption
        return Arrays.stream(MaintenanceTaskType.values())
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 50; // 維護任務優先級中等
    }

    /**
     * 從 MaintenanceTaskType Enum 建立 TaskTypeOption
     * 
     * @param type MaintenanceTaskType Enum
     * @return TaskTypeOption
     */
    private TaskTypeOption buildTaskTypeOption(MaintenanceTaskType type) {
        return TaskTypeOption.builder()
                .code(type.getCode())
                .label(String.format("[MAINTENANCE] 檔案維護 - %s", type.getLabel()))
                .category(TaskCategory.MAINTENANCE.getCode())
                .description(type.getDescription())
                .beanName(type.getBeanName())
                .defaultMethod(type.getMethodName())
                .params(type.getParams())
                .enabled(type.isEnabled())
                .build();
    }
}
