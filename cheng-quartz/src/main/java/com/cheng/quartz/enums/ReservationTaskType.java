package com.cheng.quartz.enums;

import com.cheng.common.core.domain.TaskParamMetadata;
import com.cheng.quartz.task.ReservationTask;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 預約任務類型列舉
 *
 * <p>定義所有預約任務的類型、執行方法和參數
 *
 * <p><b>設計優勢：</b>
 * <ul>
 *   <li>明確的類別引用 - IDE 可追蹤使用，避免誤判為未使用</li>
 *   <li>編譯時期檢查 - 方法名稱錯誤會在編譯時發現</li>
 *   <li>集中管理 - 所有任務類型定義在一處，易於維護</li>
 *   <li>類型安全 - 避免字串拼寫錯誤</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-12-08
 */
@Getter
@AllArgsConstructor
public enum ReservationTaskType {

    /**
     * 過期預約清理
     */
    EXPIRED_BOOKING_CLEANUP(
            "RS001",
            "ExpiredBookingCleanup",
            "自動取消並清理過期的預約記錄，恢復預約數量",
            ReservationTask.class,
            "cancelExpiredReservations",
            true,
            Collections.emptyList()  // 無參數
    );

    /**
     * 任務代碼（唯一識別碼）
     */
    private final String code;

    /**
     * 任務名稱
     */
    private final String label;

    /**
     * 任務描述
     */
    private final String description;

    /**
     * 任務類別（明確引用，IDE 可追蹤）
     */
    private final Class<?> taskClass;

    /**
     * 執行方法名稱
     */
    private final String methodName;

    /**
     * 是否啟用
     */
    private final boolean enabled;

    /**
     * 參數定義
     */
    private final List<TaskParamMetadata> params;

    /**
     * 取得 Spring Bean 名稱
     * 根據命名規範：XxxTask -> xxxTask
     */
    public String getBeanName() {
        String className = taskClass.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
