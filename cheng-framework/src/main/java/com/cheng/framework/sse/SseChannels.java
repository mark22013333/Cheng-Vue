package com.cheng.framework.sse;

/**
 * SSE 頻道常數定義
 * 集中管理所有 SSE 頻道名稱
 * 
 * @author cheng
 */
public class SseChannels {
    
    /**
     * Rich Menu 發布
     */
    public static final String RICHMENU_PUBLISH = "richmenu-publish";
    
    /**
     * QR Code 掃碼
     */
    public static final String QR_CODE_SCAN = "qrcode-scan";
    
    /**
     * 爬蟲任務
     */
    public static final String CRAWL_TASK = "crawl-task";
    
    /**
     * 物品匯入任務
     */
    public static final String ITEM_IMPORT = "item-import";
    
    /**
     * 物品預約廣播
     */
    public static final String ITEM_RESERVE = "item-reserve";
    
    // 未來可擴充其他頻道
    // public static final String ANOTHER_CHANNEL = "another-channel";
}
