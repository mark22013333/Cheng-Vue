package com.cheng.common.core.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具類
 *
 * @author cheng
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 暫存基本的物件，Integer、String、實體類等
     *
     * @param key   暫存的鍵值
     * @param value 暫存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 暫存基本的物件，Integer、String、實體類等
     *
     * @param key      暫存的鍵值
     * @param value    暫存的值
     * @param timeout  時間
     * @param timeUnit 時間顆粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 設定有效時間
     *
     * @param key     Redis鍵
     * @param timeout 逾時時間
     * @return true=設定成功；false=設定失敗
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 設定有效時間
     *
     * @param key     Redis鍵
     * @param timeout 逾時時間
     * @param unit    時間單位
     * @return true=設定成功；false=設定失敗
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 取得有效時間
     *
     * @param key Redis鍵
     * @return 有效時間
     */
    public long getExpire(final String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判斷 key是否存在
     *
     * @param key 鍵
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 取得暫存的基本物件。
     *
     * @param key 暫存鍵值
     * @return 暫存鍵值對應的數據
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 刪除單個物件
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 刪除集合物件
     *
     * @param collection 多個物件
     * @return
     */
    public boolean deleteObject(final Collection collection) {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 暫存List數據
     *
     * @param key      暫存的鍵值
     * @param dataList 待暫存的List數據
     * @return 暫存的物件
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 取得暫存的list物件
     *
     * @param key 暫存的鍵值
     * @return 暫存鍵值對應的數據
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 暫存Set
     *
     * @param key     暫存鍵值
     * @param dataSet 暫存的數據
     * @return 暫存數據的物件
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        for (T t : dataSet) {
            setOperation.add(t);
        }
        return setOperation;
    }

    /**
     * 取得暫存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 暫存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 取得暫存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入數據
     *
     * @param key   Redis鍵
     * @param hKey  Hash鍵
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 取得Hash中的數據
     *
     * @param key  Redis鍵
     * @param hKey Hash鍵
     * @return Hash中的物件
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 取得多個Hash中的數據
     *
     * @param key   Redis鍵
     * @param hKeys Hash鍵集合
     * @return Hash物件集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 刪除Hash中的某則數據
     *
     * @param key  Redis鍵
     * @param hKey Hash鍵
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 取得暫存的基本物件列表
     *
     * @param pattern 字串前綴
     * @return 物件列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }
}
