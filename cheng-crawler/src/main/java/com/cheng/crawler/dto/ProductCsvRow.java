package com.cheng.crawler.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * CSV 商品列 DTO（對映 Google Sheets 試算表欄位）
 *
 * @author cheng
 */
@Data
public class ProductCsvRow {

    /** 條碼編號 */
    private String barcode;

    /** 品名 */
    private String productName;

    /** 規格 */
    private String spec;

    /** 品號 */
    private String productCode;

    /** 定價一（成本價） */
    private BigDecimal costPrice;

    /** 零售價 */
    private BigDecimal retailPrice;

    /** 九折價 */
    private BigDecimal discountPrice;
}
