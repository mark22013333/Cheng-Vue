package com.cheng.common.utils.ip;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.constant.Constants;
import com.cheng.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2>取得IP地理位置</h2>
 * <p>
 * <h3>可參考的API</h3>
 * <ol>
 *  <li><a href="https://ipapi.co/1.34.8.160/json/">ipApi.co</a></li>
 *  <li><a href="http://ip-api.com/json/1.34.8.160?lang=zh-TW">ip-api.com</a></li>
 *  <li><a href="https://ipinfo.io/1.34.8.160/json">ipInfo.io</a></li>
 *  <li><a href="https://get.geojs.io/v1/ip/geo/1.34.8.160.json">geoJs.io</a></li>
 * </ol>
 *
 * @author cheng
 */
public class AddressUtils {
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    public static String getRealAddressByIP(String ip) {
        if (IpUtils.internalIp(ip)) {
            return "內網IP";
        }

        try {
            String url = "https://get.geojs.io/v1/ip/geo/" + ip + ".json";
            String rspStr = HttpUtils.sendGet(url, "", Constants.UTF8);

            JSONObject obj = JSON.parseObject(rspStr);
            String region = obj.getString("country");
            String city = obj.getString("city");

            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("取得地理位置異常 {}", ip);
        }
        return "UNKNOWN";
    }
}
