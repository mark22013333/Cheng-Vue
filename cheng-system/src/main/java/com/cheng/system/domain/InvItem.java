package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 物品資訊表 inv_item
 *
 * @author cheng
 */
@Setter
@Getter
public class InvItem extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 物品編碼
     */
    @Excel(name = "物品編碼")
    @NotBlank(message = "物品編碼不能為空")
    @Size(min = 0, max = 50, message = "物品編碼長度不能超過50個字元")
    private String itemCode;

    /**
     * 物品名稱
     */
    @Excel(name = "物品名稱")
    @NotBlank(message = "物品名稱不能為空")
    @Size(min = 0, max = 100, message = "物品名稱長度不能超過100個字元")
    private String itemName;

    /**
     * 分類ID
     */
    @Excel(name = "分類ID")
    @NotNull(message = "分類ID不能為空")
    private Long categoryId;

    /**
     * 分類名稱
     */
    @Excel(name = "分類名稱")
    private String categoryName;

    /**
     * 條碼
     */
    @Excel(name = "條碼")
    @Size(min = 0, max = 100, message = "條碼長度不能超過100個字元")
    private String barcode;

    /**
     * QR碼
     */
    @Excel(name = "QR碼")
    @Size(min = 0, max = 200, message = "QR碼長度不能超過200個字元")
    private String qrCode;

    /**
     * 規格
     */
    @Excel(name = "規格")
    @Size(min = 0, max = 200, message = "規格長度不能超過200個字元")
    private String specification;

    /**
     * 單位
     */
    @Excel(name = "單位")
    @Size(min = 0, max = 20, message = "單位長度不能超過20個字元")
    private String unit;

    /**
     * 品牌
     */
    @Excel(name = "品牌")
    @Size(min = 0, max = 50, message = "品牌長度不能超過50個字元")
    private String brand;

    /**
     * 型號
     */
    @Excel(name = "型號")
    @Size(min = 0, max = 50, message = "型號長度不能超過50個字元")
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
    @Size(min = 0, max = 100, message = "供應商長度不能超過100個字元")
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
    @Size(min = 0, max = 100, message = "存放位置長度不能超過100個字元")
    private String location;

    /**
     * 描述
     */
    @Excel(name = "描述")
    private String description;

    /**
     * 圖片路徑
     */
    @Excel(name = "圖片路徑")
    @Size(min = 0, max = 200, message = "圖片路徑長度不能超過200個字元")
    private String imageUrl;

    /**
     * 狀態（0正常 1停用）
     */
    @Excel(name = "狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 刪除標誌（0存在 2刪除）
     */
    private String delFlag;

    /**
     * 當前庫存數量（關聯查詢用）
     */
    @Excel(name = "庫存數量")
    private Integer stockQuantity;

    /**
     * 可用數量（關聯查詢用）
     */
    @Excel(name = "可用數量")
    private Integer availableQuantity;

    /**
     * 樂觀鎖版本號
     */
    private Integer version;

    /**
     * 預約數量（關聯查詢用）
     */
    @Excel(name = "預約數量")
    private Integer reservedQuantity;

    public InvItem() {
    }

    public InvItem(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("itemId", getItemId())
                .append("itemCode", getItemCode())
                .append("itemName", getItemName())
                .append("categoryId", getCategoryId())
                .append("categoryName", getCategoryName())
                .append("barcode", getBarcode())
                .append("qrCode", getQrCode())
                .append("specification", getSpecification())
                .append("unit", getUnit())
                .append("brand", getBrand())
                .append("model", getModel())
                .append("purchasePrice", getPurchasePrice())
                .append("currentPrice", getCurrentPrice())
                .append("supplier", getSupplier())
                .append("minStock", getMinStock())
                .append("maxStock", getMaxStock())
                .append("location", getLocation())
                .append("description", getDescription())
                .append("imageUrl", getImageUrl())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("stockQuantity", getStockQuantity())
                .append("availableQuantity", getAvailableQuantity())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
