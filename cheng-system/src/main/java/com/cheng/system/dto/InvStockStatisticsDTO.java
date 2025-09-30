package com.cheng.system.dto;

import lombok.Data;

/**
 * 庫存統計 DTO
 */
@Data
public class InvStockStatisticsDTO {
    /**
     * 總物品數量（有庫存記錄的品項數）
     */
    private Long totalItems;
    /**
     * 總庫存數量（sum(total_quantity)）
     */
    private Long totalQuantity;
    /**
     * 低庫存品項數（available_qty <= 10 且 > 0）
     */
    private Long lowStockItems;
    /**
     * 無庫存品項數（total_quantity = 0）
     */
    private Long outOfStockItems;
}
