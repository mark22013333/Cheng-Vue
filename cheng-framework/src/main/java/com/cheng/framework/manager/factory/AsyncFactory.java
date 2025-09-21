package com.cheng.framework.manager.factory;

import com.cheng.common.constant.Constants;
import com.cheng.common.utils.LogUtils;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.AddressUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.common.utils.spring.SpringUtils;
import com.cheng.system.domain.SysLogininfor;
import com.cheng.system.domain.SysOperLog;
import com.cheng.system.service.ISysLogininforService;
import com.cheng.system.service.ISysOperLogService;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * 異步工廠（產生任務用）
 *
 * @author cheng
 */
public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 記錄登入訊息
     *
     * @param username 使用者名
     * @param status   狀態
     * @param message  訊息
     * @param args     列表
     * @return 任務task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
                                             final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                String s = LogUtils.getBlock(ip) +
                        address +
                        LogUtils.getBlock(username) +
                        LogUtils.getBlock(status) +
                        LogUtils.getBlock(message);
                // 印出訊息到日誌
                sys_user_logger.info(s, args);
                // 取得客户端作業系統
                String os = userAgent.getOperatingSystem().getName();
                // 取得客户端瀏覽器
                String browser = userAgent.getBrowser().getName();
                // 封裝物件
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                // 日誌狀態
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                    logininfor.setStatus(Constants.SUCCESS);
                } else if (Constants.LOGIN_FAIL.equals(status)) {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 新增數據
                SpringUtils.getBean(ISysLogininforService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     * 操作日誌記錄
     *
     * @param operLog 操作日誌訊息
     * @return 任務task
     */
    public static TimerTask recordOper(final SysOperLog operLog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 遠端查詢操作地點
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}
