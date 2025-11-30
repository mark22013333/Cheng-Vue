package com.cheng.framework.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SSE 連線註冊表
 * 集中管理所有 SSE 連線，提供多種查詢和管理功能
 * 
 * @author cheng
 */
@Slf4j
@Component
public class SseConnectionRegistry {
    
    /**
     * 主要儲存：connectionId -> SseConnection
     */
    private final Map<String, SseConnection> connections = new ConcurrentHashMap<>();
    
    /**
     * 索引：channel -> Set<connectionId>
     * 用於快速查詢某頻道的所有連線
     */
    private final Map<String, Set<String>> channelIndex = new ConcurrentHashMap<>();
    
    /**
     * 索引：taskId -> connectionId
     * 用於快速查詢某任務的連線
     */
    private final Map<String, String> taskIndex = new ConcurrentHashMap<>();
    
    /**
     * 註冊連線
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @param emitter SSE 發射器
     * @return SseConnection
     */
    public SseConnection register(String channel, String taskId, SseEmitter emitter) {
        SseConnection connection = SseConnection.builder()
                .channel(channel)
                .taskId(taskId)
                .emitter(emitter)
                .createdAt(LocalDateTime.now())
                .lastHeartbeat(LocalDateTime.now())
                .metadata(new ConcurrentHashMap<>())
                .build();
        
        String connId = connection.getConnectionId();
        
        // 儲存連線
        connections.put(connId, connection);
        
        // 更新索引
        channelIndex.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(connId);
        taskIndex.put(taskId, connId);
        
        log.info("[SSE Registry] 註冊連線: {}, 當前總連線數: {}", connId, connections.size());
        return connection;
    }
    
    /**
     * 取得連線（按 channel 和 taskId）
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @return Optional<SseConnection>
     */
    public Optional<SseConnection> getConnection(String channel, String taskId) {
        String connId = channel + ":" + taskId;
        return Optional.ofNullable(connections.get(connId));
    }
    
    /**
     * 按任務 ID 取得連線
     * 
     * @param taskId 任務 ID
     * @return Optional<SseConnection>
     */
    public Optional<SseConnection> getConnectionByTaskId(String taskId) {
        String connId = taskIndex.get(taskId);
        return connId != null ? Optional.ofNullable(connections.get(connId)) : Optional.empty();
    }
    
    /**
     * 取得頻道所有連線
     * 
     * @param channel 頻道
     * @return List<SseConnection>
     */
    public List<SseConnection> getConnectionsByChannel(String channel) {
        Set<String> connIds = channelIndex.get(channel);
        if (connIds == null || connIds.isEmpty()) {
            return Collections.emptyList();
        }
        return connIds.stream()
                .map(connections::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 移除連線
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     */
    public void remove(String channel, String taskId) {
        String connId = channel + ":" + taskId;
        SseConnection connection = connections.remove(connId);
        
        if (connection != null) {
            // 清理索引
            Set<String> channelConns = channelIndex.get(channel);
            if (channelConns != null) {
                channelConns.remove(connId);
                if (channelConns.isEmpty()) {
                    channelIndex.remove(channel);
                }
            }
            taskIndex.remove(taskId);
            
            log.info("[SSE Registry] 移除連線: {}, 剩餘連線數: {}", connId, connections.size());
        }
    }
    
    /**
     * 更新心跳時間
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     */
    public void updateHeartbeat(String channel, String taskId) {
        getConnection(channel, taskId).ifPresent(SseConnection::updateHeartbeat);
    }
    
    /**
     * 清理過期連線
     * 
     * @param timeoutMillis 逾時時間（毫秒）
     * @return 清理的連線數量
     */
    public int cleanExpiredConnections(long timeoutMillis) {
        List<SseConnection> expiredConnections = connections.values().stream()
                .filter(conn -> conn.isExpired(timeoutMillis))
                .toList();
        
        expiredConnections.forEach(conn -> {
            try {
                conn.getEmitter().complete();
            } catch (Exception e) {
                log.warn("[SSE Registry] 關閉過期連線失敗: {}", conn.getConnectionId(), e);
            }
            remove(conn.getChannel(), conn.getTaskId());
        });
        
        if (!expiredConnections.isEmpty()) {
            log.info("[SSE Registry] 清理過期連線: {} 個", expiredConnections.size());
        }
        
        return expiredConnections.size();
    }
    
    /**
     * 取得統計資訊
     * 
     * @return 統計資訊
     */
    public Map<String, Object> getStatistics() {
        Map<String, Integer> channelDetails = channelIndex.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));
        
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalConnections", connections.size());
        stats.put("totalChannels", channelIndex.size());
        stats.put("channelDetails", channelDetails);
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * 取得所有連線 ID
     * 
     * @return 連線 ID 集合
     */
    public Set<String> getAllConnectionIds() {
        return new HashSet<>(connections.keySet());
    }
    
    /**
     * 檢查連線是否存在
     * 
     * @param channel 頻道
     * @param taskId 任務 ID
     * @return true 如果連線存在
     */
    public boolean exists(String channel, String taskId) {
        return connections.containsKey(channel + ":" + taskId);
    }
}
