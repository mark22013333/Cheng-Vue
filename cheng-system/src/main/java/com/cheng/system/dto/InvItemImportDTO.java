package com.cheng.system.dto;

import com.cheng.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物品匯入DTO
 *
 * @author cheng
 * @since 2025-12-02
 */
@Data
public class InvItemImportDTO {

    /**
     * 物品編碼（可選，為空時自動產生）
     */
    @Excel(name = "物品編碼")
    private String itemCode;

    /**
     * 物品名稱
     */
    @Excel(name = "物品名稱")
    private String itemName;

    /**
     * 分類名稱（可選，為空時使用預設分類）
     */
    @Excel(name = "分類名稱")
    private String categoryName;

    /**
     * 單位（可選，為空時使用預設單位）
     */
    @Excel(name = "單位")
    private String unit;

    /**
     * 條碼
     */
    @Excel(name = "條碼")
    private String barcode;

    /**
     * 規格
     */
    @Excel(name = "規格")
    private String specification;

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
    @Excel(name = "描述")
    private String description;

    /**
     * 初始庫存數量
     */
    @Excel(name = "初始庫存數量")
    private Integer initialStock;

    /**
     * 行號，用於錯誤定位
     */
    private Integer rowNum;
}
