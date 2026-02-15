package com.cheng.shop.logistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流方式 VO
 *
 * @author cheng
 */
@Data
@Builder
public class ShippingMethodVO {

    /**
     * 物流方式代碼
     */
    private String code;

    /**
     * 物流方式名稱
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 運費
     */
    private BigDecimal fee;

    /**
     * 免運門檻（0 表示不免運）
     */
    private BigDecimal freeThreshold;

    /**
     * 是否需要填寫地址
     */
    private boolean requireAddress;

    /**
     * 是否需要選擇超商門市
     */
    private boolean requireCvsStore;
}
