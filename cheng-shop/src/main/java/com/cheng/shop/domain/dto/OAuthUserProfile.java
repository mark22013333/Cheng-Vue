package com.cheng.shop.domain.dto;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth 第三方用戶資料
 *
 * @author cheng
 */
@Data
@Builder
public class OAuthUserProfile {

    /** 平台端唯一 ID */
    private String providerId;

    /** 跨 App 統一 ID（LINE 無，Google 有） */
    private String unionId;

    /** 暱稱 */
    private String nickname;

    /** 頭像 URL */
    private String avatarUrl;

    /** Email（可能為 null） */
    private String email;
}
