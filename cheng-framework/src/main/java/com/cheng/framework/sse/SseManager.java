package com.cheng.framework.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * SSE 管理器
 * 統一管理 SSE 連線、事件推送、心跳維護等功能
 * 
 * @author cheng
 */
@Slf4j
@Component
public class SseManager {
    
    @Autowired
    private SseConnectionRegistry registry;
    
    /**
     * 預設超時時間（30 分鐘）
     */
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;
    
    /**
     * 心跳超時時間（60 秒）
     */
    private static final long HEARTBEAT_TIMEOUT = 60 * 1000L;
    
    /**
     * 訂閱頻道
     * 
     * @param channel 頻道名稱
     * @param taskId 任務 ID
     * @param timeout 超時時間（毫秒），null 則使用預設值
     * @return SseEmitter
     */
    public SseEmitter subscribe(String channel, String taskId, Long timeout) {
        long actualTimeout = timeout != null ? timeout : DEFAULT_TIMEOUT;
        SseEmitter emitter = new SseEmitter(actualTimeout);
        
        // 註冊連線
        SseConnection connection = registry.register(channel, taskId, emitter);
        
        // 設定回調 - 正常關閉
        emitter.onCompletion(() -> {
            log.info("[SSE] 連線正常關閉: {}", connection.getConnectionId());
            registry.remove(channel, taskId);
        });
        
        // 設定回調 - 超時
        emitter.onTimeout(() -> {
            log.warn("[SSE] 連線超時: {}", connection.getConnectionId());
            registry.remove(channel, taskId);
        });
        
        // 設定回調 - 錯誤
        emitter.onError((ex) -> {
            log.error("[SSE] 連線錯誤: {}", connection.getConnectionId(), ex);
            registry.remove(channel, taskId);
        });
        
        // 發送連線成功事件
        try {
            sendInternal(connection, SseEvent.connected("連線成功"));
            log.info("[SSE] 訂閱成功: channel={}, taskId={}, timeout={}ms", channel, taskId, actualTimeout);
        } catch (Exception e) {
            log.error("[SSE] 發送連線成功事件失敗", e);
        }
        
        return emitter;
    }
    
    /**
     * 取消訂閱
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     */
    public void unsubscribe(String channel, String taskId) {
        registry.getConnection(channel, taskId).ifPresentOrElse(
            conn -> {
                try {
                    conn.getEmitter().complete();
                    log.info("[SSE] 取消訂閱: {}", conn.getConnectionId());
                } catch (Exception e) {
                    log.error("[SSE] 關閉連線失敗: {}", conn.getConnectionId(), e);
                }
                registry.remove(channel, taskId);
            },
            () -> log.warn("[SSE] 嘗試取消不存在的訂閱: {}:{}", channel, taskId)
        );
    }
    
    /**
     * 推送事件到指定任務
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @param event 事件
     */
    public void send(String channel, String taskId, SseEvent event) {
        registry.getConnection(channel, taskId).ifPresentOrElse(
            conn -> {
                try {
                    sendInternal(conn, event);
                    registry.updateHeartbeat(channel, taskId);
                    log.debug("[SSE] 事件發送成功: {}, event={}", conn.getConnectionId(), event.getEventName());
                } catch (IOException e) {
                    log.error("[SSE] 發送事件失敗: {}, event={}", conn.getConnectionId(), event.getEventName(), e);
                    registry.remove(channel, taskId);
                }
            },
            () -> log.warn("[SSE] 找不到連線，無法發送事件: {}:{}, event={}", channel, taskId, event.getEventName())
        );
    }
    
    /**
     * 廣播到頻道所有訂閱者
     * 
     * @param channel 頻道
     * @param event 事件
     */
    public void broadcast(String channel, SseEvent event) {
        List<SseConnection> connections = registry.getConnectionsByChannel(channel);
        log.info("[SSE] 廣播到頻道 {} 的 {} 個連線, event={}", channel, connections.size(), event.getEventName());
        
        int successCount = 0;
        int failCount = 0;
        
        for (SseConnection conn : connections) {
            try {
                sendInternal(conn, event);
                registry.updateHeartbeat(conn.getChannel(), conn.getTaskId());
                successCount++;
            } catch (IOException e) {
                log.error("[SSE] 廣播失敗: {}, event={}", conn.getConnectionId(), event.getEventName(), e);
                registry.remove(conn.getChannel(), conn.getTaskId());
                failCount++;
            }
        }
        
        log.info("[SSE] 廣播完成: 成功={}, 失敗={}", successCount, failCount);
    }
    
    /**
     * 心跳維護（定時清理過期連線）
     * 每 30 秒執行一次
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        int cleaned = registry.cleanExpiredConnections(HEARTBEAT_TIMEOUT);
        if (cleaned > 0) {
            log.info("[SSE] 定時心跳：清理 {} 個過期連線", cleaned);
        }
    }
    
    /**
     * 取得統計資訊
     * 
     * @return 統計資訊
     */
    public Map<String, Object> getStatistics() {
        return registry.getStatistics();
    }
    
    /**
     * 檢查連線是否存在
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @return true 如果連線存在
     */
    public boolean exists(String channel, String taskId) {
        return registry.exists(channel, taskId);
    }
    
    /**
     * 內部方法：發送 SSE 事件
     * 
     * @param connection 連線
     * @param event 事件
     * @throws IOException IO 異常
     */
    private void sendInternal(SseConnection connection, SseEvent event) throws IOException {
        connection.getEmitter().send(
            SseEmitter.event()
                .name(event.getEventName())
                .data(event)
        );
    }
}
