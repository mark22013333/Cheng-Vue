package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 超商門市選取暫存表
 *
 * @author cheng
 */
@Data
public class ShopCvsStoreTemp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 前端識別 key（UUID）
     */
    private String storeKey;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 物流子類型：FAMI/UNIMART/HILIFE
     */
    private String logisticsSub;

    /**
     * 門市代號
     */
    private String storeId;

    /**
     * 門市名稱
     */
    private String storeName;

    /**
     * 門市地址
     */
    private String storeAddress;

    /**
     * 門市電話
     */
    private String storeTel;

    /**
     * 是否離島（1=是）
     */
    private String cvsOutside;

    /**
     * 過期時間
     */
    private Date expireTime;

    /**
     * 建立時間
     */
    private Date createTime;
}
