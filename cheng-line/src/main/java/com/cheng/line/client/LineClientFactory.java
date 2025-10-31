package com.cheng.line.client;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * LINE Messaging API Client 工廠
 * <p>
 * 管理 MessagingApiClient 實例，避免重複建立造成資源浪費
 * <p>
 * 特性：
 * - 使用快取機制，相同 Access Token 復用同一個 Client
 * - 執行緒安全（使用 ConcurrentHashMap）
 * - 支援多頻道（不同 Access Token）
 * - 提供清除機制（當頻道刪除或 Token 更新時）
 *
 * @author cheng
 */
@Slf4j
@Component
public class LineClientFactory {

    /**
     * Client 快取
     * Key: Access Token
     * Value: MessagingApiClient 實例
     */
    private final ConcurrentHashMap<String, MessagingApiClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 取得 MessagingApiClient
     * 如果快取中存在則復用，否則建立新的並加入快取
     *
     * @param accessToken Channel Access Token
     * @return MessagingApiClient 實例
     */
    public MessagingApiClient getClient(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access Token 不能為空");
        }

        return clientCache.computeIfAbsent(accessToken, token -> {
            log.info("建立新的 MessagingApiClient，Token 前綴：{}...",
                    token.substring(0, Math.min(10, token.length())));
            return MessagingApiClient.builder(token).build();
        });
    }

    /**
     * 移除特定 Access Token 的 Client
     * 當頻道被刪除或 Access Token 更新時應呼叫此方法
     *
     * @param accessToken Channel Access Token
     */
    public void removeClient(String accessToken) {
        if (accessToken != null) {
            MessagingApiClient removed = clientCache.remove(accessToken);
            if (removed != null) {
                log.info("移除 MessagingApiClient 快取，Token 前綴：{}...",
                        accessToken.substring(0, Math.min(10, accessToken.length())));
            }
        }
    }

    /**
     * 清除所有 Client 快取
     * 謹慎使用，會影響所有頻道
     */
    public void clearAll() {
        int size = clientCache.size();
        clientCache.clear();
        log.warn("清除所有 MessagingApiClient 快取，共 {} 個", size);
    }

    /**
     * 取得當前快取的 Client 數量
     *
     * @return 快取數量
     */
    public int getCacheSize() {
        return clientCache.size();
    }
}
