package com.cheng.shop.domain;

import com.cheng.shop.enums.MemberStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 前台會員實體
 *
 * @author cheng
 */
@Data
public class ShopMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 會員ID
     */
    private Long memberId;

    /**
     * 會員編號
     */
    private String memberNo;

    /**
     * 暱稱
     */
    private String nickname;

    /**
     * 頭像URL
     */
    private String avatar;

    /**
     * 手機號碼
     */
    private String mobile;

    /**
     * 電子信箱
     */
    private String email;

    /**
     * 密碼（加密）
     */
    @JsonIgnore
    private String password;

    /**
     * 性別
     */
    private String gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 會員點數
     */
    private Integer points;

    /**
     * 會員等級
     */
    private String level;

    /**
     * 狀態
     */
    private String status;

    /**
     * 最後登入時間
     */
    private Date lastLoginTime;

    /**
     * 最後登入IP
     */
    private String lastLoginIp;

    /**
     * 註冊時間
     */
    private Date createTime;

    /**
     * 更新時間
     */
    private Date updateTime;

    /**
     * 取得狀態列舉
     */
    public MemberStatus getStatusEnum() {
        return status != null ? MemberStatus.fromCode(status) : null;
    }

    /**
     * 設定狀態列舉
     */
    public void setStatusEnum(MemberStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }
}
