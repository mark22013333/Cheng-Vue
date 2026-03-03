package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 密碼重設 Token 實體
 * <p>
 * 採用 selector + hashed token 設計：
 * <ul>
 *   <li>selector：URL-safe 隨機字串，用於 DB 查詢（索引）</li>
 *   <li>hashed_token：validator 的 SHA-256 雜湊，DB 永不儲存原始 token</li>
 * </ul>
 *
 * @author cheng
 */
@Data
public class ShopPasswordReset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主鍵 ID */
    private Long id;

    /** 選擇器（URL-safe 隨機字串，用於查詢） */
    private String selector;

    /** 驗證器 SHA-256 雜湊 */
    private String hashedToken;

    /** 綁定的 Email */
    private String email;

    /** 會員 ID */
    private Long memberId;

    /** 過期時間 */
    private Date expiresAt;

    /** 是否已使用 */
    private Boolean used;

    /** 請求時 IP 地址 */
    private String ipAddress;

    /** 請求時瀏覽器 User-Agent */
    private String userAgent;

    /** 建立時間 */
    private Date createdAt;

    /** 使用時間 */
    private Date usedAt;
}
