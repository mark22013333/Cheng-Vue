package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

/**
 * 系統訪問記錄表 sys_logininfor
 *
 * @author cheng
 */
@Setter
@Getter
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

}
