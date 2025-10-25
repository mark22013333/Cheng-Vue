package com.cheng.common.core.provider;

import com.cheng.common.core.domain.TaskTypeOption;
import com.cheng.common.enums.TaskCategory;

import java.util.List;

/**
 * 任務類型提供者介面
 * 各模組（爬蟲、推播、報表等）實作此介面，向排程模組提供可用的任務類型
 *
 * <p>設計理念：
 * <ul>
 *   <li>模組解耦：各模組獨立定義自己的任務類型，不依賴排程模組</li>
 *   <li>動態註冊：Spring 自動掃描並註冊所有 TaskTypeProvider 實作</li>
 *   <li>擴展性強：新增任務類型只需實作此介面，無需修改排程模組</li>
 * </ul>
 *
 * @author Cheng
 * @since 2025-10-25
 */
public interface TaskTypeProvider {

    /**
     * 取得任務分類
     *
     * @return 任務分類（爬蟲、推播、報表等）
     */
    TaskCategory getCategory();

    /**
     * 取得此分類下所有可用的任務類型
     *
     * @return 任務類型選項列表
     */
    List<TaskTypeOption> getTaskTypes();

    /**
     * 取得提供者優先級（數字越小優先級越高）
     * 用於控制前端顯示順序
     *
     * @return 優先級，預設為 100
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 是否啟用此提供者
     * 可用於條件式啟用某些任務類型
     *
     * @return 是否啟用，預設為 true
     */
    default boolean isEnabled() {
        return true;
    }
}
