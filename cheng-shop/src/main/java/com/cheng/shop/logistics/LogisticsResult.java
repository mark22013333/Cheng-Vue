package com.cheng.shop.logistics;

import lombok.Builder;
import lombok.Data;

/**
 * 物流操作結果
 *
 * @author cheng
 */
@Data
@Builder
public class LogisticsResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 綠界物流編號（AllPayLogisticsID）
     */
    private String logisticsId;

    /**
     * 超商繳款編號（CVSPaymentNo）
     */
    private String cvsPaymentNo;

    /**
     * 超商驗證碼（CVSValidationNo）
     */
    private String cvsValidationNo;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 原始回應內容
     */
    private String rawResponse;

    /**
     * 建立失敗結果
     */
    public static LogisticsResult fail(String response) {
        return LogisticsResult.builder()
                .success(false)
                .rawResponse(response)
                .errorMessage(response)
                .build();
    }

    /**
     * 建立失敗結果（含錯誤訊息）
     */
    public static LogisticsResult fail(String errorMessage, String rawResponse) {
        return LogisticsResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .rawResponse(rawResponse)
                .build();
    }
}
