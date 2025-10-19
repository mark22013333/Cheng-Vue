package com.cheng.system.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 目前線上會話
 *
 * @author cheng
 */
@Setter
@Getter
public class SysUserOnline {
    /**
     * 會話編號
     */
    private String tokenId;

    /**
     * 部門名稱
     */
    private String deptName;

    /**
     * 使用者名稱
     */
    private String userName;

    /**
     * 登入IP位置
     */
    private String ipaddr;

    /**
     * 登入地址
     */
    private String loginLocation;

    /**
     * 瀏覽器類型
     */
    private String browser;

    /**
     * 作業系統
     */
    private String os;

    /**
     * 登入時間
     */
    private Long loginTime;

}
