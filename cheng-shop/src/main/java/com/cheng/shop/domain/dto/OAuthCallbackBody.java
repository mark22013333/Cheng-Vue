package com.cheng.shop.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OAuth 回調請求 Body
 *
 * @author cheng
 */
@Data
public class OAuthCallbackBody {

    /** 第三方平台（LINE/GOOGLE/FACEBOOK） */
    @NotBlank(message = "provider 不可為空")
    private String provider;

    /** 授權碼 */
    @NotBlank(message = "code 不可為空")
    private String code;

    /** CSRF state 參數 */
    @NotBlank(message = "state 不可為空")
    private String state;
}
