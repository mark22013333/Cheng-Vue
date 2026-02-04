package com.cheng.shop.domain;

import com.cheng.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 金流回調紀錄
 *
 * @author cheng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopPaymentCallbackLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 紀錄ID */
    private Long logId;

    /** 訂單編號 */
    private String orderNo;

    /** 付款方式代碼 */
    private String paymentMethod;

    /** 回調來源: SERVER/BROWSER */
    private String callbackType;

    /** 第三方交易編號 */
    private String tradeNo;

    /** 第三方回傳碼 */
    private String rtnCode;

    /** 驗簽結果: 1 成功, 0 失敗 */
    private Integer verifyStatus;

    /** 驗簽訊息 */
    private String verifyMessage;

    /** 回傳原始資訊(JSON) */
    private String rawInfo;
}
