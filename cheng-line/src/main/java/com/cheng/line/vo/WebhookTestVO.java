package com.cheng.line.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Webhook 測試結果 VO
 *
 * @author cheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookTestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 訊息
     */
    private String message;

    /**
     * HTTP 狀態碼
     */
    private Integer statusCode;

    /**
     * 回應時間（毫秒）
     */
    private Long responseTime;

    /**
     * 測試時間
     */
    private LocalDateTime testTime;

    /**
     * 錯誤詳情（失敗時回傳）
     */
    private String errorDetails;
}
