package com.cheng.common.utils.ip;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.constant.Constants;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 取得IP類
 *
 * @author cheng
 */
public class AddressUtils {
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP位置查詢
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        // 内網不查詢
        if (IpUtils.internalIp(ip)) {
            return "内網IP";
        }
        if (CoolAppsConfig.isAddressEnabled()) {
            try {
                String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.UTF8);
                if (StringUtils.isEmpty(rspStr)) {
                    log.error("==>取得地理位置異常 {}", ip);
                    return UNKNOWN;
                }
                JSONObject obj = JSON.parseObject(rspStr);
                String region = obj.getString("pro");
                String city = obj.getString("city");
                return String.format("%s %s", region, city);
            } catch (Exception e) {
                log.error("取得地理位置異常 {}", ip);
            }
        }
        return UNKNOWN;
    }
}
