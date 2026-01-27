package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增/更新地址請求
 *
 * @author cheng
 */
@Data
public class AddressRequest {

    /**
     * 地址 ID（更新時使用）
     */
    private Long addressId;

    /**
     * 收件人姓名
     */
    @NotBlank(message = "收件人姓名不能為空")
    @Size(max = 50, message = "收件人姓名長度不能超過 50 字元")
    private String receiverName;

    /**
     * 收件人電話
     */
    @NotBlank(message = "收件人電話不能為空")
    @Size(max = 20, message = "收件人電話長度不能超過 20 字元")
    private String receiverPhone;

    /**
     * 省/縣市
     */
    @Size(max = 50, message = "縣市長度不能超過 50 字元")
    private String province;

    /**
     * 市/區
     */
    @Size(max = 50, message = "市區長度不能超過 50 字元")
    private String city;

    /**
     * 區/鄉鎮
     */
    @Size(max = 50, message = "鄉鎮長度不能超過 50 字元")
    private String district;

    /**
     * 詳細地址
     */
    @NotBlank(message = "詳細地址不能為空")
    @Size(max = 200, message = "詳細地址長度不能超過 200 字元")
    private String detailAddress;

    /**
     * 郵遞區號
     */
    @Size(max = 10, message = "郵遞區號長度不能超過 10 字元")
    private String postalCode;

    /**
     * 是否預設地址
     */
    private Boolean isDefault;
}
