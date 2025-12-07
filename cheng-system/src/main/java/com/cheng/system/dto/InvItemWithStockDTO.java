package com.cheng.system.dto;

import com.cheng.common.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Excel(name = "物品編碼")
    private String itemCode;

    /**
     * 物品名稱
     */
    @Excel(name = "物品名稱")
    private String itemName;

    /**
     * 分類ID
     */
    private Long categoryId;

    /**
     * 分類名稱
     */
    @Excel(name = "分類")
    private String categoryName;

    /**
     * 條碼
     */
    @Excel(name = "條碼")
    private String barcode;

    /**
     * QR碼
     */
    private String qrCode;

    /**
     * 規格
     */
    @Excel(name = "規格")
    private String specification;

    /**
     * 單位
     */
    @Excel(name = "單位")
    private String unit;

    /**
     * 品牌
     */
    @Excel(name = "品牌")
    private String brand;

    /**
     * 型號
     */
    @Excel(name = "型號")
    private String model;

    /**
     * 採購價格
     */
    @Excel(name = "採購價格")
    private BigDecimal purchasePrice;

    /**
     * 現價
     */
    @Excel(name = "現價")
    private BigDecimal currentPrice;

    /**
     * 供應商
     */
    @Excel(name = "供應商")
    private String supplier;

    /**
     * 最低庫存
     */
    @Excel(name = "最低庫存")
    private Integer minStock;

    /**
     * 最高庫存
     */
    @Excel(name = "最高庫存")
    private Integer maxStock;

    /**
     * 存放位置
     */
    @Excel(name = "存放位置")
    private String location;

    /**
     * 描述
     */
    private String description;

    /**
     * 圖片路徑（相對路徑，用於匯入時對應圖片檔案）
     */
    @Excel(name = "圖片存放位置", sort = 25)
    private String imageUrl;

    /**
     * 狀態（0正常 1停用）
     */
    private String status;

    /**
     * 備註
     */
    private String remark;

    /**
     * 樂觀鎖版本號
     */
    private Integer version;

    // ========== 庫存資訊 ==========
    /**
     * 庫存ID
     */
    private Long stockId;

    /**
     * 總數量
     */
    @Excel(name = "總數量")
    private Integer totalQuantity;

    /**
     * 可用數量
     */
    @Excel(name = "可用數量")
    private Integer availableQty;

    /**
     * 借出數量
     */
    @Excel(name = "借出數量")
    private Integer borrowedQty;

    /**
     * 預留數量
     */
    @Excel(name = "預留數量")
    private Integer reservedQty;

    /**
     * 損壞數量
     */
    @Excel(name = "損壞數量")
    private Integer damagedQty;

    /**
     * 遺失數量
     */
    @Excel(name = "遺失數量")
    private Integer lostQty;

    /**
     * 最後入庫時間
     */
    @Excel(name = "最後入庫時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastInTime;

    /**
     * 最後出庫時間
     */
    @Excel(name = "最後出庫時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
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
    @Excel(name = "庫存狀態")
    private String stockStatusText;

    /**
     * 是否低於最低庫存
     */
    private Boolean isBelowMinStock;

    /**
     * 庫存總價值（現價 × 總數量）
     */
    @Excel(name = "庫存總價值")
    private BigDecimal stockValue;

    /**
     * 成本總價值（採購價 × 總數量）
     */
    @Excel(name = "成本總價值")
    private BigDecimal costValue;

    /**
     * 預期利潤（庫存總價值 - 成本總價值）
     */
    @Excel(name = "預期利潤")
    private BigDecimal expectedProfit;

    /**
     * 利潤率（%）
     */
    @Excel(name = "利潤率")
    private BigDecimal profitRate;

    /**
     * 遺失損失金額（採購價 × 遺失數量）
     */
    @Excel(name = "遺失損失")
    private BigDecimal lostValue;

    /**
     * 損壞損失金額（採購價 × 損壞數量）
     */
    @Excel(name = "損壞損失")
    private BigDecimal damagedValue;

    /**
     * 實際可用庫存價值（現價 × 可用數量）
     */
    @Excel(name = "可用庫存價值")
    private BigDecimal availableValue;

    /**
     * 歷史採購成本（採購價 × 原始採購總數量）
     * 包含已遺失和損壞的物品成本
     */
    @Excel(name = "歷史採購成本")
    private BigDecimal historicalCost;

    /**
     * 累計損失（遺失損失 + 損壞損失）
     */
    @Excel(name = "累計損失")
    private BigDecimal totalLoss;

    /**
     * 可用庫存成本（採購價 × 可用數量）
     */
    @Excel(name = "可用庫存成本")
    private BigDecimal availableCost;

    /**
     * 可實現利潤（可用庫存市值 - 可用庫存成本）
     */
    @Excel(name = "可實現利潤")
    private BigDecimal realizableProfit;

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
        if (minStock != null && availableQty <= minStock) {
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
     * 計算庫存總價值和財務指標
     */
    public void calculateStockValue() {
        // 初始化為 0，避免 null
        this.lostValue = BigDecimal.ZERO;
        this.damagedValue = BigDecimal.ZERO;
        this.totalLoss = BigDecimal.ZERO;

        if (purchasePrice == null || currentPrice == null) {
            return;
        }

        // 1. 計算歷史採購成本（原始採購總數 = 現存 + 遺失 + 損壞）
        int originalQuantity = (totalQuantity != null ? totalQuantity : 0)
                + (lostQty != null ? lostQty : 0);
        this.historicalCost = purchasePrice.multiply(BigDecimal.valueOf(originalQuantity));

        // 2. 計算當前庫存相關價值（現存物品）
        if (totalQuantity != null) {
            // 當前庫存成本 = 採購價 × 現存數量
            this.costValue = purchasePrice.multiply(BigDecimal.valueOf(totalQuantity));
            // 當前庫存市值 = 現價 × 現存數量
            this.stockValue = currentPrice.multiply(BigDecimal.valueOf(totalQuantity));
            // 預期利潤 = 當前庫存市值 - 當前庫存成本
            this.expectedProfit = this.stockValue.subtract(this.costValue);
        }

        // 3. 計算可用庫存相關價值（可售物品）
        if (availableQty != null) {
            // 可用庫存成本 = 採購價 × 可用數量
            this.availableCost = purchasePrice.multiply(BigDecimal.valueOf(availableQty));
            // 可用庫存市值 = 現價 × 可用數量
            this.availableValue = currentPrice.multiply(BigDecimal.valueOf(availableQty));
            // 可實現利潤 = 可用庫存市值 - 可用庫存成本
            this.realizableProfit = this.availableValue.subtract(this.availableCost);
        }

        // 4. 計算損失
        if (lostQty != null && lostQty > 0) {
            // 遺失損失 = 採購價 × 遺失數量
            this.lostValue = purchasePrice.multiply(BigDecimal.valueOf(lostQty));
        }

        if (damagedQty != null && damagedQty > 0) {
            // 損壞損失 = 採購價 × 損壞數量
            this.damagedValue = purchasePrice.multiply(BigDecimal.valueOf(damagedQty));
        }

        // 累計損失 = 遺失損失 + 損壞損失
        this.totalLoss = this.lostValue.add(this.damagedValue);

        // 5. 計算利潤率（基於可實現利潤）
        if (purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDiff = currentPrice.subtract(purchasePrice);
            this.profitRate = priceDiff
                    .divide(purchasePrice, 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
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
