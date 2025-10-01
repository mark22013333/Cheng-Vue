package com.cheng.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任務呼叫共用常數
 *
 * @author cheng
 */
public class ScheduleConstants {
    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /**
     * 執行目標key
     */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /**
     * 預設
     */
    public static final String MISFIRE_DEFAULT = "0";

    /**
     * 立即觸發執行
     */
    public static final String MISFIRE_IGNORE_MISFIRES = "1";

    /**
     * 觸發一次執行
     */
    public static final String MISFIRE_FIRE_AND_PROCEED = "2";

    /**
     * 不觸發立即執行
     */
    public static final String MISFIRE_DO_NOTHING = "3";

    @Getter
    @AllArgsConstructor
    public enum Status {
        /**
         * 正常
         */
        NORMAL("0"),
        /**
         * 暫停
         */
        PAUSE("1");
        private final String value;
    }
}
