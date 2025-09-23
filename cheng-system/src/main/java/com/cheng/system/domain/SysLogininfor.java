package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.util.Date;

/**
 * 系統訪問記錄表 sys_logininfor
 *
 * @author cheng
 */
public class SysLogininfor extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Excel(name = "序號", cellType = ColumnType.NUMERIC)
    private Long infoId;

    /**
     * 使用者帳號
     */
    @Excel(name = "使用者帳號")
    private String userName;

    /**
     * 登入狀態 0成功 1失敗
     */
    @Excel(name = "登入狀態", readConverterExp = "0=成功,1=失敗")
    private String status;

    /**
     * 登入IP位置
     */
    @Excel(name = "登入地址")
    private String ipaddr;

    /**
     * 登入地點
     */
    @Excel(name = "登入地點")
    private String loginLocation;

    /**
     * 瀏覽器類型
     */
    @Excel(name = "瀏覽器")
    private String browser;

    /**
     * 作業系統
     */
    @Excel(name = "作業系統")
    private String os;

    /**
     * 提示訊息
     */
    @Excel(name = "提示訊息")
    private String msg;

    /**
     * 訪問時間
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "訪問時間", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    public Long getInfoId() {
        return infoId;
    }

    public void setInfoId(Long infoId) {
        this.infoId = infoId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
