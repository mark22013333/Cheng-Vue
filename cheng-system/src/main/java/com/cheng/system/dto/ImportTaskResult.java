package com.cheng.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 匯入任務結果對象
 *
 * @author cheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskResult {

    /**
     * 任務ID
     */
    private String taskId;

    /**
     * 資料總行數
     */
    private Integer rowCount;
}
