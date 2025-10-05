package com.cheng.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 物品與庫存整合 DTO
 * 包含物品基本資訊和庫存狀態
 *
 * @author cheng
 * @since 2025-10-04
 */
@Data
public class InvItemWithStockDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 物品資訊 ==========
    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 物品編碼
     */
    private String itemCode;

    /**
     * 物品名稱
     */
    private String itemName;

    /**
     * 分類ID
     */
    private Long categoryId;

    /**
     * 分類名稱
     */
    private String categoryName;

    /**
     * 條碼
     */
    private String barcode;

    /**
     * QR碼
     */
    private String qrCode;

    /**
     * 規格
     */
    private String specification;

    /**
     * 單位
     */
    private String unit;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型號
     */
    private String model;

    /**
     * 採購價格
     */
    private BigDecimal purchasePrice;

    /**
     * 現價
     */
    private BigDecimal currentPrice;

    /**
     * 供應商
     */
    private String supplier;

    /**
     * 最低庫存
     */
    private Integer minStock;

    /**
     * 最高庫存
     */
    private Integer maxStock;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 描述
     */
    private String description;

    /**
     * 圖片路徑
     */
    private String imageUrl;

    /**
     * 狀態（0正常 1停用）
     */
    private String status;

    /**
     * 備註
     */
    private String remark;

    // ========== 庫存資訊 ==========
    /**
     * 庫存ID
     */
    private Long stockId;

    /**
     * 總數量
     */
    private Integer totalQuantity;

    /**
     * 可用數量
     */
    private Integer availableQty;

    /**
     * 借出數量
     */
    private Integer borrowedQty;

    /**
     * 預留數量
     */
    private Integer reservedQty;

    /**
     * 損壞數量
     */
    private Integer damagedQty;

    /**
     * 最後入庫時間
     */
    private Date lastInTime;

    /**
     * 最後出庫時間
     */
    private Date lastOutTime;

    /**
     * 庫存更新時間
     */
    private Date stockUpdateTime;

    // ========== 計算欄位 ==========
    /**
     * 庫存狀態（0正常 1低庫存 2無庫存）
     */
    private String stockStatus;

    /**
     * 庫存狀態文字
     */
    private String stockStatusText;

    /**
     * 是否低於最低庫存
     */
    private Boolean isBelowMinStock;

    /**
     * 庫存總價值
     */
    private BigDecimal stockValue;

    /**
     * 計算庫存狀態
     * 邏輯與 SQL 查詢條件完全一致
     */
    public void calculateStockStatus() {
        // 無庫存：可用數量為 null 或 <= 0
        if (availableQty == null || availableQty <= 0) {
            this.stockStatus = "2";
            this.stockStatusText = "無庫存";
            this.isBelowMinStock = true;
            return;
        }

        // 低庫存：可用數量 > 0 且有設定最低庫存，且低於最低庫存
        if (minStock != null && availableQty > 0 && availableQty <= minStock) {
            this.stockStatus = "1";
            this.stockStatusText = "低庫存";
            this.isBelowMinStock = true;
            return;
        }

        // 正常：可用數量 > 0 且（無最低庫存設定 或 高於最低庫存）
        this.stockStatus = "0";
        this.stockStatusText = "正常";
        this.isBelowMinStock = false;
    }

    /**
     * 計算庫存總價值
     */
    public void calculateStockValue() {
        if (currentPrice != null && totalQuantity != null) {
            this.stockValue = currentPrice.multiply(BigDecimal.valueOf(totalQuantity));
        }
    }

    // ========== 查詢參數 ==========
    /**
     * 自訂低庫存閾值（用於查詢條件）
     */
    private Integer lowStockThreshold;

    /**
     * 排序欄位
     */
    private String orderByColumn;

    /**
     * 排序方向（asc/desc）
     */
    private String isAsc;
}
