package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 會員收貨地址實體
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopMemberAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 地址 ID
     */
    private Long addressId;

    /**
     * 會員 ID
     */
    private Long memberId;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人電話
     */
    private String receiverPhone;

    /**
     * 省/縣市
     */
    private String province;

    /**
     * 市/區
     */
    private String city;

    /**
     * 區/鄉鎮
     */
    private String district;

    /**
     * 詳細地址
     */
    private String detailAddress;

    /**
     * 郵遞區號
     */
    private String postalCode;

    /**
     * 是否預設地址
     */
    private Boolean isDefault;

    /**
     * 取得完整地址
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (district != null) sb.append(district);
        if (detailAddress != null) sb.append(detailAddress);
        return sb.toString();
    }
}
