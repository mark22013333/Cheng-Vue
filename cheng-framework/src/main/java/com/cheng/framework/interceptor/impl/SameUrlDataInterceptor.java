package com.cheng.framework.interceptor.impl;

import com.alibaba.fastjson2.JSON;
import com.cheng.common.annotation.RepeatSubmit;
import com.cheng.common.constant.CacheConstants;
import com.cheng.common.core.redis.RedisCache;
import com.cheng.common.filter.RepeatedlyRequestWrapper;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.http.HttpHelper;
import com.cheng.common.utils.http.PathUtils;
import com.cheng.framework.interceptor.RepeatSubmitInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 判斷請求url和數據是否和上一次相同，
 * 如果和上次相同，則是重複提交表單。 有效時間為10秒内。
 *
 * @author cheng
 */
@Component
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor {
    public final String REPEAT_PARAMS = "repeatParams";

    public final String REPEAT_TIME = "repeatTime";

    // 令牌自定義標識
    @Value("${token.header}")
    private String header;

    @Autowired
    private RedisCache redisCache;

    @SuppressWarnings("unchecked")
    @Override
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) {
        String nowParams = "";
        if (request instanceof RepeatedlyRequestWrapper repeatedlyRequest) {
            nowParams = HttpHelper.getBodyString(repeatedlyRequest);
        }

        // body參數為空，取得Parameter的數據
        if (StringUtils.isEmpty(nowParams)) {
            nowParams = JSON.toJSONString(request.getParameterMap());
        }
        Map<String, Object> nowDataMap = new HashMap<>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());

        // 請求地址（作為存放 cache 的 key 值，使用 servlet path 排除 context path）
        String url = PathUtils.getServletPath(request);

        // 唯一值（沒有訊息頭則使用請求地址）
        String submitKey = StringUtils.trimToEmpty(request.getHeader(header));

        // 唯一標識（指定key + url + 訊息頭）
        String cacheRepeatKey = CacheConstants.REPEAT_SUBMIT_KEY + url + submitKey;

        Object sessionObj = redisCache.getCacheObject(cacheRepeatKey);
        if (sessionObj != null) {
            Map<String, Object> sessionMap = (Map<String, Object>) sessionObj;
            if (sessionMap.containsKey(url)) {
                Map<String, Object> preDataMap = (Map<String, Object>) sessionMap.get(url);
                if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap, annotation.interval())) {
                    return true;
                }
            }
        }
        Map<String, Object> cacheMap = new HashMap<String, Object>();
        cacheMap.put(url, nowDataMap);
        redisCache.setCacheObject(cacheRepeatKey, cacheMap, annotation.interval(), TimeUnit.MILLISECONDS);
        return false;
    }

    /**
     * 判斷參數是否相同
     */
    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap) {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    /**
     * 判斷兩次間隔時間
     */
    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval) {
        long time1 = (Long) nowMap.get(REPEAT_TIME);
        long time2 = (Long) preMap.get(REPEAT_TIME);
        return (time1 - time2) < interval;
    }
}
