package com.cheng.framework.sse;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 連線包裝類
 * 封裝 SSE 連線的完整資訊
 * 
 * @author cheng
 */
@Data
@Builder
public class SseConnection {
    
    /**
     * 頻道
     */
    private String channel;
    
    /**
     * 任務 ID
     */
    private String taskId;
    
    /**
     * SSE 發射器
     */
    private SseEmitter emitter;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 最後心跳時間
     */
    private LocalDateTime lastHeartbeat;
    
    /**
     * 使用者 ID (可選)
     */
    private String userId;
    
    /**
     * 額外元數據
     */
    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();
    
    /**
     * 取得唯一識別碼
     * 格式：channel:taskId
     * 
     * @return 連線唯一識別碼
     */
    public String getConnectionId() {
        return channel + ":" + taskId;
    }
    
    /**
     * 檢查是否過期
     * 
     * @param timeoutMillis 超時時間（毫秒）
     * @return true 如果已過期
     */
    public boolean isExpired(long timeoutMillis) {
        if (lastHeartbeat == null) {
            return false;
        }
        long millisSinceLastHeartbeat = ChronoUnit.MILLIS.between(lastHeartbeat, LocalDateTime.now());
        return millisSinceLastHeartbeat > timeoutMillis;
    }
    
    /**
     * 更新心跳時間
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }
    
    /**
     * 添加元數據
     * 
     * @param key 鍵
     * @param value 值
     */
    public void putMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new ConcurrentHashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * 取得元數據
     * 
     * @param key 鍵
     * @return 值
     */
    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
}
