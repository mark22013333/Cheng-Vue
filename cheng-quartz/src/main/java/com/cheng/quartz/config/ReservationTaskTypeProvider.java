package com.cheng.quartz.config;

import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 預約任務類型提供者
 * 向排程模組提供預約相關的任務類型及其參數定義
 *
 * @author Cheng
 * @since 2025-12-04
 */
@Slf4j
@Component
public class ReservationTaskTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.RESERVATION;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        List<TaskTypeOption> taskTypes = new ArrayList<>();

        // 過期預約清理任務
        TaskTypeOption expiredCleanupTask = TaskTypeOption.builder()
                .code("RS001")
                .label("ExpiredBookingCleanup")
                .category("RESERVATION")
                .description("自動取消並清理過期的預約記錄，恢復預約數量")
                .beanName("reservationTask")
                .defaultMethod("cancelExpiredReservations")
                .enabled(true)
                .params(null) // 暫時不使用參數，保持簡單
                .build();

        taskTypes.add(expiredCleanupTask);

        return taskTypes;
    }

    @Override
    public int getPriority() {
        return 50; // 預約任務優先級中等
    }
}
