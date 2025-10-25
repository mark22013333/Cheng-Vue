package com.cheng.quartz.provider;

import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.core.provider.TaskTypeProvider;
import com.cheng.common.enums.TaskCategory;
import com.cheng.quartz.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推播訊息任務類型提供者
 * 提供 LINE 推播等任務類型
 *
 * <p>參數定義已在 NotificationType enum 中實作
 * <p>注意：實際推播功能需要另外開發
 *
 * @author Cheng
 * @since 2025-10-25
 */
@Slf4j
@Component
public class NotificationTypeProvider implements TaskTypeProvider {

    @Override
    public TaskCategory getCategory() {
        return TaskCategory.NOTIFICATION;
    }

    @Override
    public List<TaskTypeOption> getTaskTypes() {
        return Arrays.stream(NotificationType.values())
                .map(this::buildTaskTypeOption)
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return 20; // 推播任務優先級次於爬蟲
    }

    /**
     * 建立任務類型選項
     * 顯示格式：[代號] 分類 - 功能名稱
     * 例如：[LINE_PUSH] LINE Bot - 訊息推播
     *
     * <p>參數定義已經在 NotificationType enum 中實作，直接呼叫即可
     */
    private TaskTypeOption buildTaskTypeOption(NotificationType type) {
        return TaskTypeOption.builder()
                .code(type.name())
                .label(String.format("[%s] %s - %s",
                        type.name(),
                        type.getCategory(),
                        type.getFuncName()))
                .category(TaskCategory.NOTIFICATION.getCode())
                .description(String.format("透過「%s」進行「%s」",
                        type.getCategory(),
                        type.getFuncName()))
                .beanName("notificationTask")
                .defaultMethod("sendNotification")
                .params(type.buildParams()) // 直接呼叫 enum 的方法
                .enabled(true)
                .build();
    }
}
