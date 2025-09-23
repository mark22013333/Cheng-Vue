package com.cheng.system.domain;

/**
 * 目前在線會話
 *
 * @author cheng
 */
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

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }
}
