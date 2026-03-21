package com.cheng.system.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 預約狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum ReserveStatus {

    PENDING(1, "待審核"),
    APPROVED(2, "預約通過"),
    REJECTED(3, "預約拒絕"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String description;

    public static ReserveStatus getByCode(int code) {
        for (ReserveStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
