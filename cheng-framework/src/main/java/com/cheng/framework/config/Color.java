package com.cheng.framework.config;

import lombok.experimental.UtilityClass;

/**
 * ANSI 顏色碼常數
 * 用於控制台日誌輸出的顏色設定
 *
 * <p>使用範例：
 * <pre>
 * import static com.cheng.framework.config.Color.*;
 *
 * log.info("{}成功訊息{}", ANSI_GREEN, ANSI_RESET);
 * log.warn("{}警告訊息{}", ANSI_YELLOW, ANSI_RESET);
 * log.error("{}錯誤訊息{}", ANSI_RED, ANSI_RESET);
 * </pre>
 *
 * @author Cheng
 * @since 2025-11-02
 */
@UtilityClass
public class Color {

    /**
     * 重置顏色
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * 黑色
     */
    public static final String ANSI_BLACK = "\u001B[30m";

    /**
     * 紅色 - 適合錯誤訊息
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * 綠色 - 適合成功訊息
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * 黃色 - 適合警告訊息
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * 藍色 - 適合一般資訊
     */
    public static final String ANSI_BLUE = "\u001B[34m";

    /**
     * 洋紅色
     */
    public static final String ANSI_MAGENTA = "\u001B[35m";

    /**
     * 青色 - 適合標題
     */
    public static final String ANSI_CYAN = "\u001B[36m";

    /**
     * 白色
     */
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * 粗體
     */
    public static final String ANSI_BOLD = "\u001B[1m";

}
