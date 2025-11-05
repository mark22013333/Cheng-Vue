package com.cheng.common.utils;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Cheng
 * @since 2024/12/11 22:05
 **/
@Slf4j
@Component
public class OkHttpUtilsPool {
    private static final GenericObjectPool<OkHttpUtils> POOL;

    static {
        GenericObjectPoolConfig<OkHttpUtils> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(50); // 最大物件數量
        config.setMinIdle(5);   // 最小閒置物件數量
        config.setMaxIdle(10);  // 最大閒置物件數量
        config.setBlockWhenExhausted(true); // 當物件用盡時是否阻塞
        config.setMaxWait(Duration.ofSeconds(15)); // 阻塞時最大等待時間
        config.setMinEvictableIdleDuration(Duration.ofMinutes(10)); // 設定最小閒置存活時間
        config.setTimeBetweenEvictionRuns(Duration.ofMinutes(5));   // 設定清理間隔時間

        POOL = new GenericObjectPool<>(new BasePooledObjectFactory<>() {
            @Override
            public OkHttpUtils create() {
                return new OkHttpUtils();
            }

            @Override
            public PooledObject<OkHttpUtils> wrap(OkHttpUtils obj) {
                return new DefaultPooledObject<>(obj);
            }
        }, config);
        logPoolStatus();
    }

    /**
     * 紀錄當前物件池的狀態，包括活躍物件數、閒置物件數、已建立的物件數以及已銷毀的物件數。
     * 此方法對於即時監控物件池的健康狀態及使用情況非常有用。
     *
     * <p>
     * **物件池指標說明**：
     * <ul>
     *   <li><b>Active（活躍）：</b>當前正在被借用的物件數量。</li>
     *   <li><b>Idle（閒置）：</b>當前可供借用的閒置物件數量。</li>
     *   <li><b>Created（已建立）：</b>自物件池初始化以來，總共建立的物件數量。</li>
     *   <li><b>Destroyed（已銷毀）：</b>自物件池初始化以來，總共被銷毀的物件數量。</li>
     * </ul>
     * </p>
     *
     * <p>
     * **如何適當調整物件池參數**：
     * <ul>
     *   <li><b>MaxTotal（最大物件數）：</b>
     *       若活躍物件數（Active）經常接近或達到最大物件數，建議適當提高 {@code MaxTotal}，以允許更多物件同時被借用。</li>
     *   <li><b>MaxIdle（最大閒置數）：</b>
     *       若閒置物件數（Idle）經常降至 0，建議適當提高 {@code MaxIdle}，以確保池中有足夠的備用物件。</li>
     *   <li><b>MinIdle（最小閒置數）：</b>
     *       若頻繁地需要建立新物件，建議提高 {@code MinIdle}，以確保池中保持一定數量的預備物件。</li>
     *   <li><b>MaxWait（最大等待時間）：</b>
     *       若物件借用過程中出現過多等待，建議適當延長 {@code MaxWait} 時間，或提高 {@code MaxTotal} 來降低等待的機會。</li>
     * </ul>
     * </p>
     */
    public static void logPoolStatus() {
        log.debug("Pool Status: Active={}, Idle={}", POOL.getNumActive(), POOL.getNumIdle());
        log.debug("Pool Status: Created={}, Destroyed={}", POOL.getCreatedCount(), POOL.getDestroyedCount());
    }

    public static OkHttpUtils borrow() throws Exception {
        return POOL.borrowObject();
    }

    public static void release(OkHttpUtils instance) {
        POOL.returnObject(instance);
    }

    @PreDestroy
    public void destroyPool() {
        POOL.close();
    }
}
