package com.cheng.web.controller.monitor;

import com.cheng.common.constant.CacheConstants;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.utils.StringUtils;
import com.cheng.system.domain.SysCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DefaultedRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 暫存監控
 *
 * @author cheng
 */
@RestController
@RequestMapping("/monitor/cache")
public class CacheController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final static List<SysCache> CACHES = new ArrayList<>();

    {
        CACHES.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, "使用者訊息"));
        CACHES.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "設定訊息"));
        CACHES.add(new SysCache(CacheConstants.SYS_DICT_KEY, "數據字典"));
        CACHES.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "驗證碼"));
        CACHES.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        CACHES.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流處理"));
        CACHES.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密碼錯誤次數"));
    }

    @SuppressWarnings("deprecation")
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @GetMapping()
    public AjaxResult getInfo() throws Exception {
        Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) DefaultedRedisConnection::info);
        Properties commandStats = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("commandstats"));
        Object dbSize = redisTemplate.execute((RedisCallback<Object>) DefaultedRedisConnection::dbSize);

        Map<String, Object> result = new HashMap<>(3);
        result.put("info", info);
        result.put("dbSize", dbSize);

        List<Map<String, String>> pieList = new ArrayList<>();
        commandStats.stringPropertyNames().forEach(key -> {
            Map<String, String> data = new HashMap<>(2);
            String property = commandStats.getProperty(key);
            data.put("name", StringUtils.removeStart(key, "cmdstat_"));
            data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
            pieList.add(data);
        });
        result.put("commandStats", pieList);
        return AjaxResult.success(result);
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @GetMapping("/getNames")
    public AjaxResult cache() {
        return AjaxResult.success(CACHES);
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @GetMapping("/getKeys/{cacheName}")
    public AjaxResult getCacheKeys(@PathVariable String cacheName) {
        Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        return AjaxResult.success(new TreeSet<>(cacheKeys));
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public AjaxResult getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
        return AjaxResult.success(sysCache);
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public AjaxResult clearCacheName(@PathVariable String cacheName) {
        Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        redisTemplate.delete(cacheKeys);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public AjaxResult clearCacheKey(@PathVariable String cacheKey) {
        redisTemplate.delete(cacheKey);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Cache.LIST + "')")
    @DeleteMapping("/clearCacheAll")
    public AjaxResult clearCacheAll() {
        Collection<String> cacheKeys = redisTemplate.keys("*");
        redisTemplate.delete(cacheKeys);
        return AjaxResult.success();
    }
}
