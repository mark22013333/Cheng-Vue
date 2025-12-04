package com.cheng.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品預約結果對象
 *
 * @author cheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveResult {

    /**
     * 預約是否成功
     */
    private Boolean success;

    /**
     * 訊息
     */
    private String message;

    /**
     * 預約記錄ID
     */
    private Long borrowId;

    /**
     * 當前可用數量
     */
    private Integer availableQty;

    /**
     * 當前預約數量
     */
    private Integer reservedQty;
}
