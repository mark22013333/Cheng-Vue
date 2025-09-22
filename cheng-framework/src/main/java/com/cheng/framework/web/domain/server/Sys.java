package com.cheng.framework.web.domain.server;

/**
 * 系統相關訊息
 *
 * @author cheng
 */
public class Sys {
    /**
     * 伺服器名稱
     */
    private String computerName;

    /**
     * 伺服器Ip
     */
    private String computerIp;

    /**
     * 專案路徑
     */
    private String userDir;

    /**
     * 作業系統
     */
    private String osName;

    /**
     * 系統架構
     */
    private String osArch;

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getComputerIp() {
        return computerIp;
    }

    public void setComputerIp(String computerIp) {
        this.computerIp = computerIp;
    }

    public String getUserDir() {
        return userDir;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }
}
