package com.cheng.quartz.config;

import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.quartz.enums.ReservationTaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 預約任務類型提供者
 * 向排程模組提供預約相關的任務類型及其參數定義
 * 
 * <p>預約任務歸類為業務處理（BUSINESS）分類
 * 
 * <p>使用 {@link ReservationTaskType} Enum 進行集中管理，優勢：
 * <ul>
 *   <li>明確的類別引用 - IDE 可追蹤使用</li>
 *   <li>編譯時期檢查 - 避免方法名稱錯誤</li>
 *   <li>集中管理 - 所有任務類型定義在一處</li>
 *   <li>類型安全 - 避免字串拼寫錯誤</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-12-04
 */
@Slf4j
@Component
public class ReservationTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.BUSINESS;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        // 直接從 Enum 轉換為 TaskTypeOption
        return Arrays.stream(ReservationTaskType.values())
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 50; // 預約任務優先級中等
    }

    /**
     * 從 ReservationTaskType Enum 建立 TaskTypeOption
     * 
     * @param type ReservationTaskType Enum
     * @return TaskTypeOption
     */
    private TaskTypeOption buildTaskTypeOption(ReservationTaskType type) {
        return TaskTypeOption.builder()
                .code(type.getCode())
                .label(type.getLabel())
                .category(TaskCategory.BUSINESS.getCode())
                .description(type.getDescription())
                .beanName(type.getBeanName())
                .defaultMethod(type.getMethodName())
                .params(type.getParams())
                .enabled(type.isEnabled())
                .build();
    }
}
