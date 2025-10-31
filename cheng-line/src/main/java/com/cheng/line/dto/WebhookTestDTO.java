package com.cheng.line.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Webhook 測試 DTO
 *
 * @author cheng
 */
@Data
public class WebhookTestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 設定ID
     */
    @NotNull(message = "設定ID不能為空")
    private Integer configId;

    /**
     * Webhook URL（可選，如果不提供則使用設定中的 URL）
     */
    private String webhookUrl;
}
