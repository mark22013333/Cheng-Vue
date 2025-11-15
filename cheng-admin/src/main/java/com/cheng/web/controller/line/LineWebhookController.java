package com.cheng.web.controller.line;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.LineUser;
import com.cheng.line.enums.FollowStatus;
import com.cheng.line.service.ILineConfigService;
import com.cheng.line.service.ILineUserService;
import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParser;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LINE Webhook 接收處理器（多頻道支援）
 * <p>
 * 處理 LINE Platform 發送的 Webhook 事件
 * <p>
 * 使用 LINE Bot SDK 9.12.0 + 自定義多頻道路由
 * <p>
 * Webhook URL 格式：/webhook/line/{botBasicId}
 * 例如：/webhook/line/@322okyxf 或 /webhook/line/322okyxf
 * <p>
 * 注意：由於是多頻道架構，使用自定義 endpoint + @EventMapping 手動分發
 * <p>
 *
 * @author cheng
 */
@Anonymous
@LineMessageHandler
@RestController
@RequestMapping(ILineConfigService.WEBHOOK)
public class LineWebhookController extends BaseController {

    @Resource
    private ILineUserService lineUserService;

    @Resource
    private ILineConfigService lineConfigService;

    /**
     * 當前請求的 Bot Basic ID
     * 用於識別是哪個頻道的 Webhook
     */
    private final ThreadLocal<String> currentBotBasicId = new ThreadLocal<>();

    /**
     * LINE Webhook 接收端點
     * 接收 LINE Platform 發送的 Webhook 事件
     *
     * @param botBasicId Bot Basic ID
     * @param signature  LINE 簽名 (X-Line-Signature header)
     * @param payload    Webhook 請求內容（原始 JSON 字串）
     * @return 處理結果
     */
    @PostMapping("/{botBasicId}")
    public AjaxResult handleWebhook(
            @PathVariable("botBasicId") String botBasicId,
            @RequestHeader(value = "X-Line-Signature", required = false) String signature,
            @RequestBody String payload) {

        log.info("==================== 收到 LINE Webhook 請求 ====================");
        log.debug("[Webhook] Bot Basic ID (原始): {}", botBasicId);
        log.debug("[Webhook] 是否包含簽名: {}", signature != null && !signature.isEmpty());
        log.debug("[Webhook] Payload 長度: {} bytes", payload != null ? payload.length() : 0);

        // 正規化 Bot Basic ID（確保有 @ 前綴）
        String normalizedBotBasicId = botBasicId.startsWith("@") ? botBasicId : "@" + botBasicId;
        log.debug("[Webhook] Bot Basic ID (正規化): {}", normalizedBotBasicId);

        // 設定當前請求的 Bot Basic ID
        currentBotBasicId.set(normalizedBotBasicId);

        try {
            // 1. 驗證頻道是否存在且啟用
            log.debug("[Stop1] 開始驗證頻道配置...");
            LineConfig config = lineConfigService.selectLineConfigByBotBasicId(normalizedBotBasicId);
            if (config == null) {
                log.error("[錯誤] Bot Basic ID 不存在: {}", botBasicId);
                return notFound("頻道設定不存在");
            }
            log.debug("[Stop1] 頻道驗證成功: {} ({})", config.getChannelName(), config.getBotBasicId());

            // 2. 解析並驗證 Webhook 事件
            log.debug("[Stop2] 開始解析並驗證 Webhook 事件...");

            // 檢查是否有簽名
            if (signature == null || signature.isEmpty()) {
                log.error("[錯誤] 請求缺少 X-Line-Signature header");
                log.error("[提示] LINE Platform 的所有 Webhook 請求都必須包含簽名");
                return unauthorized("缺少 X-Line-Signature header");
            }

            CallbackRequest callbackRequest;
            try {
                // WebhookParser 會自動驗證簽名並解析事件
                callbackRequest = parseAndValidateWebhook(config.getChannelSecret(), signature, payload);
                log.debug("[Stop2] 簽名驗證成功");
            } catch (Exception e) {
                log.error("[錯誤] 簽名驗證失敗或解析錯誤: {}", e.getMessage());
                return unauthorized("簽名驗證失敗或解析錯誤");
            }

            List<Event> events = callbackRequest.events();
            log.debug("[Stop2] 解析成功，共 {} 個事件", events.size());

            // 3. 處理每個事件
            if (!events.isEmpty()) {
                log.debug("[Stop3] 開始處理事件...");
            }

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                String eventType = event.getClass().getSimpleName();
                log.info("[事件 {}/{}] 類型: {}", i + 1, events.size(), eventType);
                log.debug("[事件 {}/{}] 時間戳: {}", i + 1, events.size(), event.timestamp());

                processEvent(event);
                log.debug("[事件 {}/{}] 處理完成", i + 1, events.size());
            }

            log.info("==================== Webhook 請求處理完成 ====================");
            return success();

        } catch (Exception e) {
            log.error("==================== Webhook 處理失敗 ====================", e);
            log.error("[錯誤詳情] {}", e.getMessage());
            return error(e.getMessage());
        } finally {
            // 清理 ThreadLocal
            currentBotBasicId.remove();
        }
    }

    /**
     * 驗證 LINE 簽名並解析 Webhook 請求
     *
     * @param channelSecret 頻道密鑰
     * @param signature     請求簽名
     * @param payload       請求內容
     * @return CallbackRequest 物件
     */
    private CallbackRequest parseAndValidateWebhook(String channelSecret, String signature, String payload) {
        try {
            // 建立簽名驗證器
            LineSignatureValidator validator = new LineSignatureValidator(channelSecret.getBytes());

            // 建立 Webhook 解析器（需要傳入驗證器）
            WebhookParser parser = new WebhookParser(validator);

            // 解析並驗證（需要傳入 signature 和 payload bytes）
            return parser.handle(signature, payload.getBytes());
        } catch (Exception e) {
            log.error("[Webhook 解析] 解析或驗證失敗", e);
            throw new RuntimeException("Webhook 解析失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 處理單個事件
     * 使用 LINE SDK 的事件分發機制，將事件路由到對應的 @EventMapping 方法
     *
     * @param event 事件物件
     */
    private void processEvent(Event event) {
        log.debug("[事件分發] 開始分發事件: {}", event.getClass().getSimpleName());

        // 使用 LINE SDK 的事件類型判斷，自動路由到對應的 @EventMapping 方法
        if (event instanceof FollowEvent) {
            handleFollowEvent((FollowEvent) event);
        } else if (event instanceof UnfollowEvent) {
            handleUnfollowEvent((UnfollowEvent) event);
        } else if (event instanceof MessageEvent) {
            handleMessageEvent((MessageEvent) event);
        } else if (event instanceof PostbackEvent) {
            handlePostbackEvent((PostbackEvent) event);
        } else if (event instanceof BeaconEvent) {
            handleBeaconEvent((BeaconEvent) event);
        } else if (event instanceof JoinEvent) {
            handleJoinEvent((JoinEvent) event);
        } else if (event instanceof LeaveEvent) {
            handleLeaveEvent((LeaveEvent) event);
        } else if (event instanceof MemberJoinedEvent) {
            handleMemberJoinedEvent((MemberJoinedEvent) event);
        } else if (event instanceof MemberLeftEvent) {
            handleMemberLeftEvent((MemberLeftEvent) event);
        } else {
            handleDefaultEvent(event);
        }
    }

    /**
     * 處理關注事件
     * 當使用者加入好友時觸發
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
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
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
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
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleMessageEvent(MessageEvent event) {
        String userId = event.source().userId();

        // 檢查使用者是否在黑名單中
        if (isUserBlacklisted(userId)) {
            log.warn("使用者在黑名單中，忽略訊息事件, userId={}", userId);
            return;
        }

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
     * 處理 Postback 事件（按鈕點擊Callback）
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String userId = event.source().userId();

        // 檢查使用者是否在黑名單中
        if (isUserBlacklisted(userId)) {
            log.warn("使用者在黑名單中，忽略 Postback 事件, userId={}", userId);
            return;
        }

        String data = event.postback().data();
        log.info("收到 Postback 事件，使用者ID: {}，資料: {}", userId, data);

        // 取得 params（日期時間選擇器會包含此欄位）
        var params = event.postback().params();
        
        // 處理日期時間選擇器回傳
        if (params != null && params.get("datetime") != null) {
            String selectedDatetime = params.get("datetime");
            log.info("========================================");
            log.info("【日期時間選擇器回傳】");
            log.info("使用者ID: {}", userId);
            log.info("Postback Data: {}", data);
            log.info("選擇的日期時間: {}", selectedDatetime);
            log.info("========================================");
            
            // TODO: 根據業務需求處理日期時間
            // 例如：儲存預約時間、觸發排程任務等
        }
        // 處理日期選擇器回傳（只有日期）
        else if (params != null && params.get("date") != null) {
            String selectedDate = params.get("date");
            log.info("========================================");
            log.info("【日期選擇器回傳】");
            log.info("使用者ID: {}", userId);
            log.info("Postback Data: {}", data);
            log.info("選擇的日期: {}", selectedDate);
            log.info("========================================");
            
            // TODO: 根據業務需求處理日期
        }
        // 處理時間選擇器回傳（只有時間）
        else if (params != null && params.get("time") != null) {
            String selectedTime = params.get("time");
            log.info("========================================");
            log.info("【時間選擇器回傳】");
            log.info("使用者ID: {}", userId);
            log.info("Postback Data: {}", data);
            log.info("選擇的時間: {}", selectedTime);
            log.info("========================================");
            
            // TODO: 根據業務需求處理時間
        }
        // 一般 Postback 事件
        else {
            log.debug("一般 Postback 事件（非日期時間選擇器）");
            
            // TODO: 實作其他 Postback 處理邏輯
            // 例如：處理 Template Message 的按鈕點擊、Rich Menu Switch 等
        }
    }

    /**
     * 處理 Beacon 事件（藍牙訊號）
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        String userId = event.source().userId();
        log.info("收到 Beacon 事件，使用者ID: {}", userId);

        // TODO: 實作 Beacon 處理邏輯
    }

    /**
     * 處理加入群組/聊天室事件
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        Source source = event.source();
        if (source instanceof GroupSource) {
            String groupId = ((GroupSource) source).groupId();
            log.info("Bot 加入群組，群組ID: {}", groupId);
        } else if (source instanceof RoomSource) {
            String roomId = ((RoomSource) source).roomId();
            log.info("Bot 加入聊天室，聊天室ID: {}", roomId);
        }

        // TODO: 實作加入群組處理邏輯
    }

    /**
     * 處理離開群組/聊天室事件
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleLeaveEvent(LeaveEvent event) {
        Source source = event.source();
        if (source instanceof GroupSource) {
            String groupId = ((GroupSource) source).groupId();
            log.info("Bot 離開群組，群組ID: {}", groupId);
        } else if (source instanceof RoomSource) {
            String roomId = ((RoomSource) source).roomId();
            log.info("Bot 離開聊天室，聊天室ID: {}", roomId);
        }

        // TODO: 實作離開群組處理邏輯
    }

    /**
     * 處理成員加入群組事件
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleMemberJoinedEvent(MemberJoinedEvent event) {
        log.info("成員加入群組事件，成員數: {}", event.joined().members().size());

        // TODO: 實作成員加入處理邏輯
    }

    /**
     * 處理成員離開群組事件
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleMemberLeftEvent(MemberLeftEvent event) {
        log.info("成員離開群組事件，成員數: {}", event.left().members().size());

        // TODO: 實作成員離開處理邏輯
    }

    /**
     * 預設事件處理器
     * 處理所有未特別定義的事件
     * <p>
     * 使用 @EventMapping 標記，符合 LINE SDK 標準
     */
    @EventMapping
    public void handleDefaultEvent(Event event) {
        String eventType = event.getClass().getSimpleName();
        log.info("收到事件: {}", eventType);

        // 記錄事件但不做特別處理
        log.debug("事件詳情: {}", event);
    }

    /**
     * 檢查使用者是否在黑名單中
     *
     * @param lineUserId LINE 使用者 ID
     * @return true: 在黑名單中, false: 不在黑名單中
     */
    private boolean isUserBlacklisted(String lineUserId) {
        try {
            LineUser user = lineUserService.selectLineUserByLineUserId(lineUserId);
            if (user != null && user.getFollowStatus() == FollowStatus.BLACKLISTED) {
                return true;
            }
        } catch (Exception e) {
            log.error("檢查黑名單狀態失敗, userId={}", lineUserId, e);
        }
        return false;
    }
}
