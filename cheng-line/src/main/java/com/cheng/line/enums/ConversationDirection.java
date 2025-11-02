package com.cheng.line.enums;

import lombok.Getter;

/**
 * 對話方向枚舉
 *
 * @author cheng
 */
@Getter
public enum ConversationDirection {
    
    /**
     * 系統發送給使用者
     */
    SENT("SENT", "發送"),
    
    /**
     * 使用者發送給系統
     */
    RECEIVED("RECEIVED", "接收");
    
    private final String code;
    private final String description;
    
    ConversationDirection(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根據代碼取得枚舉
     */
    public static ConversationDirection fromCode(String code) {
        for (ConversationDirection direction : values()) {
            if (direction.getCode().equals(code)) {
                return direction;
            }
        }
        return null;
    }
}
