package com.cheng.shop.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 會員第三方登入綁定實體
 *
 * @author cheng
 */
@Data
public class ShopMemberSocial implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 會員ID */
    private Long memberId;

    /** 第三方平台：LINE/GOOGLE/FACEBOOK */
    private String provider;

    /** 第三方用戶唯一ID */
    private String providerId;

    /** UnionID（部分平台有） */
    private String unionId;

    /** Access Token */
    private String accessToken;

    /** Refresh Token */
    private String refreshToken;

    /** Token 過期時間 */
    private Date tokenExpire;

    /** 第三方暱稱 */
    private String nickname;

    /** 第三方頭像 URL */
    private String avatar;

    /** 額外資料（JSON） */
    private String extraData;

    /** 綁定時間 */
    private Date createTime;

    /** 更新時間 */
    private Date updateTime;
}
