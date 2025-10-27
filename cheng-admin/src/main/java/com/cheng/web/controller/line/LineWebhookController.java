package com.cheng.web.controller.line;

import com.cheng.line.service.ILineUserService;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * LINE Webhook 接收處理器
 * <p>
 * 處理 LINE Platform 發送的 Webhook 事件
 * <p>
 * 使用 LINE Bot SDK 9.12.0 的 Spring Boot 整合
 *
 * @author cheng
 */
@Slf4j
@LineMessageHandler
public class LineWebhookController {

    @Resource
    private ILineUserService lineUserService;

    /**
     * 處理關注事件
     * 當使用者加入好友時觸發
     */
    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String userId = event.source().userId();
        log.info("收到關注事件，使用者ID: {}", userId);

        try {
            // 處理關注事件
            lineUserService.handleFollowEvent(userId);

            // 更新使用者個人資料
            lineUserService.updateUserProfile(userId, null);

            log.info("關注事件處理成功，使用者ID: {}", userId);
        } catch (Exception e) {
            log.error("處理關注事件失敗，使用者ID: {}", userId, e);
        }
    }

    /**
     * 處理取消關注事件
     * 當使用者封鎖或刪除好友時觸發
     */
    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        String userId = event.source().userId();
        log.info("收到取消關注事件，使用者ID: {}", userId);

        try {
            lineUserService.handleUnfollowEvent(userId);
            log.info("取消關注事件處理成功，使用者ID: {}", userId);
        } catch (Exception e) {
            log.error("處理取消關注事件失敗，使用者ID: {}", userId, e);
        }
    }

    /**
     * 處理訊息事件
     * 包含文字訊息、圖片、影片、音訊等
     */
    @EventMapping
    public void handleMessageEvent(MessageEvent event) {
        String userId = event.source().userId();

        if (event.message() instanceof TextMessageContent) {
            // 處理文字訊息
            String messageText = ((TextMessageContent) event.message()).text();
            log.info("收到文字訊息，使用者ID: {}, 訊息內容: {}", userId, messageText);

            // TODO: 根據訊息內容實作自動回覆邏輯
            // 例如：
            // - 關鍵字自動回覆
            // - 綁定系統使用者
            // - 查詢功能
            // - 選單導航
        } else {
            // 處理其他類型訊息
            String messageType = event.message().getClass().getSimpleName();
            log.info("收到訊息事件，使用者ID: {}, 訊息類型: {}", userId, messageType);

            // TODO: 根據訊息類型實作處理邏輯
        }

        try {
            // 增加接收訊息計數
            lineUserService.incrementMessageCount(userId, false);
            log.info("訊息事件處理成功，使用者ID: {}", userId);
        } catch (Exception e) {
            log.error("處理訊息事件失敗，使用者ID: {}", userId, e);
        }
    }

    /**
     * 預設事件處理器
     * 處理所有未特別定義的事件
     */
    @EventMapping
    public void handleDefaultEvent(Event event) {
        String eventType = event.getClass().getSimpleName();
        log.info("收到事件: {}", eventType);

        // 記錄事件但不做特別處理
        log.debug("事件詳情: {}", event);
    }
}
