package com.cheng.system.domain.enums;

/**
 * 借出記錄狀態列舉
 *
 * @author cheng
 */
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

    BorrowStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

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
}
