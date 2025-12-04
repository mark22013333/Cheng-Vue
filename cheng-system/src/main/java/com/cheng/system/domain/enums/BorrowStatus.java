package com.cheng.system.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 借出記錄狀態列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum BorrowStatus {
    /**
     * 待審核
     */
    PENDING("0", "待審核"),

    /**
     * 已借出（審核通過）
     */
    BORROWED("1", "已借出"),

    /**
     * 審核拒絕
     */
    REJECTED("2", "審核拒絕"),

    /**
     * 已歸還
     */
    RETURNED("3", "已歸還"),

    /**
     * 部分歸還
     */
    PARTIAL_RETURNED("4", "部分歸還"),

    /**
     * 逾期
     */
    OVERDUE("5", "逾期");

    private final String code;
    private final String description;

    /**
     * 根據代碼取得狀態
     *
     * @param code 狀態代碼
     * @return 借出狀態
     */
    public static BorrowStatus getByCode(String code) {
        for (BorrowStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 檢查是否為待審核狀態
     *
     * @return true=待審核
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 檢查是否為已借出狀態
     *
     * @return true=已借出
     */
    public boolean isBorrowed() {
        return this == BORROWED;
    }

    /**
     * 檢查是否為已歸還或部分歸還狀態
     *
     * @return true=已歸還或部分歸還
     */
    public boolean isReturned() {
        return this == RETURNED || this == PARTIAL_RETURNED;
    }

    /**
     * 檢查是否為審核拒絕狀態
     *
     * @return true=審核拒絕
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * 檢查是否需要歸還（已借出或逾期）
     *
     * @return true=需要歸還
     */
    public boolean needsReturn() {
        return this == BORROWED || this == OVERDUE || this == PARTIAL_RETURNED;
    }

    /**
     * 取得狀態對應的顏色（Element UI 色系）
     *
     * @return 顏色代碼
     */
    public String getColor() {
        return switch (this) {
            case PENDING -> "#409EFF";      // 待審核 - 藍色
            case BORROWED -> "#E6A23C";     // 已借出 - 橙色
            case REJECTED -> "#909399";     // 審核拒絕 - 灰色
            case RETURNED -> "#67C23A";     // 已歸還 - 綠色
            case PARTIAL_RETURNED -> "#F56C6C"; // 部分歸還 - 紅色
            case OVERDUE -> "#F56C6C";      // 逾期 - 紅色
        };
    }
}
