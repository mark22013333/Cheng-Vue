package com.cheng.line.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 關注狀態列舉
 * <p>
 * 資料庫儲存: Enum name (VARCHAR)
 * 例如: "UNFOLLOWED", "FOLLOWING", "BLACKLISTED"
 * <p>
 * 狀態說明：
 * - UNFOLLOWED: 使用者主動取消關注或封鎖 LINE 頻道（LINE 官方狀態）
 * - FOLLOWING: 使用者正在關注頻道（好友狀態）
 * - BLACKLISTED: 管理者設定的黑名單（即使使用者關注頻道，也無法接收訊息，優先級最高）
 * <p>
 * 優先級：BLACKLISTED > FOLLOWING > UNFOLLOWED
 * 當使用者被加入黑名單時，無論原本是什麼狀態（包括關注中），都無法收到訊息
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum FollowStatus {

    /**
     * 未關注（使用者主動取消關注或封鎖 LINE 頻道）
     */
    UNFOLLOWED("未關注"),

    /**
     * 關注中（好友狀態）
     */
    FOLLOWING("好友"),

    /**
     * 黑名單（管理者設定，該使用者將被忽略，無法參與活動或接收訊息）
     */
    BLACKLISTED("黑名單");

    private final String description;

    /**
     * 取得狀態代碼（JSON 序列化時使用）
     *
     * @return Enum 名稱，例如："FOLLOWING"
     */
    @JsonValue
    public String getCode() {
        return name();  // 直接返回 Enum 的 name()
    }

    /**
     * 根據代碼字串轉換為 Enum
     *
     * @param code 狀態代碼（Enum 名稱）
     * @return 關注狀態
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static FollowStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return valueOf(code);  // 直接使用 Java Enum 的 valueOf()
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的關注狀態: " + code, e);
        }
    }
}
