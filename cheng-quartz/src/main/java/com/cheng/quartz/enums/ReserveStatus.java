package com.cheng.quartz.enums;

/**
 * 預約狀態枚舉
 * 
 * @author Cheng
 * @since 2025-12-04
 */
public enum ReserveStatus {
    
    /**
     * 正常借出
     */
    NORMAL(0, "正常借出"),
    
    /**
     * 待審核預約
     */
    PENDING(1, "待審核預約"),
    
    /**
     * 預約通過
     */
    APPROVED(2, "預約通過"),
    
    /**
     * 預約拒絕
     */
    REJECTED(3, "預約拒絕"),
    
    /**
     * 預約取消
     */
    CANCELLED(4, "預約取消");

    private final Integer code;
    private final String description;

    ReserveStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根據代碼取得預約狀態
     * 
     * @param code 狀態代碼
     * @return 預約狀態，如果找不到則返回 null
     */
    public static ReserveStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (ReserveStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根據代碼取得狀態描述
     * 
     * @param code 狀態代碼
     * @return 狀態描述，如果找不到則返回 "未知狀態"
     */
    public static String getDescriptionByCode(Integer code) {
        ReserveStatus status = getByCode(code);
        return status != null ? status.getDescription() : "未知狀態";
    }
}
