package com.cheng.shop.enums;

/**
 * 綠界物流子類型模式
 * <p>
 * ECPay 物流合約分為兩種型態：
 * <ul>
 *   <li>{@link #B2C}：大宗寄貨（將商品寄放物流中心，出貨量大），超商代碼為 UNIMART/FAMI/HILIFE/OKMART，支援宅配（TCAT/POST）</li>
 *   <li>{@link #C2C}：門市寄/取件（自行至超商寄件，出貨量小），超商代碼為 UNIMARTC2C/FAMIC2C/HILIFEC2C/OKMARTC2C，不支援宅配</li>
 * </ul>
 *
 * @author cheng
 */
public enum LogisticsSubTypeMode {

    B2C,
    C2C;

    /**
     * 預設模式：C2C。
     * <p>
     * 用於 sys_config 值缺失、空白或非法時的 fallback。
     */
    public static final LogisticsSubTypeMode DEFAULT = C2C;

    /**
     * 從 sys_config 字串值容錯解析。
     * <p>
     * 大小寫不敏感，會去除前後空白；解析失敗時回傳 {@link #DEFAULT}（C2C），不拋例外。
     *
     * @param raw sys_config 讀出的原始字串
     * @return 對應的模式列舉
     */
    public static LogisticsSubTypeMode fromConfigValue(String raw) {
        if (raw == null) {
            return DEFAULT;
        }
        String normalized = raw.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return DEFAULT;
        }
        try {
            return LogisticsSubTypeMode.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return DEFAULT;
        }
    }
}
