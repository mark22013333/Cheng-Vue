package com.cheng.line.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LINE API 端點列舉
 *
 * @author cheng
 */
@Getter
@RequiredArgsConstructor
public enum LineApiEndpoint {

    /**
     * Rich Menu 相關 API
     */
    RICH_MENU_CREATE("https://api.line.me/v2/bot/richmenu", "建立 Rich Menu"),
    RICH_MENU_VALIDATE("https://api.line.me/v2/bot/richmenu/validate", "驗證 Rich Menu"),
    RICH_MENU_DELETE("https://api.line.me/v2/bot/richmenu/{richMenuId}", "刪除 Rich Menu"),
    RICH_MENU_GET("https://api.line.me/v2/bot/richmenu/{richMenuId}", "取得 Rich Menu"),
    RICH_MENU_LIST("https://api.line.me/v2/bot/richmenu/list", "取得 Rich Menu 列表"),
    RICH_MENU_UPLOAD_IMAGE("https://api-data.line.me/v2/bot/richmenu/{richMenuId}/content", "上傳 Rich Menu 圖片"),
    RICH_MENU_DOWNLOAD_IMAGE("https://api-data.line.me/v2/bot/richmenu/{richMenuId}/content", "下載 Rich Menu 圖片"),
    DEFAULT_RICH_MENU_SET("https://api.line.me/v2/bot/user/all/richmenu/{richMenuId}", "設定預設 Rich Menu"),
    DEFAULT_RICH_MENU_CLEAR("https://api.line.me/v2/bot/user/all/richmenu", "清除預設 Rich Menu"),
    
    /**
     * Rich Menu Alias 相關 API
     */
    RICH_MENU_ALIAS_CREATE("https://api.line.me/v2/bot/richmenu/alias", "建立 Rich Menu Alias"),
    RICH_MENU_ALIAS_DELETE("https://api.line.me/v2/bot/richmenu/alias/{richMenuAliasId}", "刪除 Rich Menu Alias"),
    RICH_MENU_ALIAS_UPDATE("https://api.line.me/v2/bot/richmenu/alias/{richMenuAliasId}", "更新 Rich Menu Alias"),
    RICH_MENU_ALIAS_GET("https://api.line.me/v2/bot/richmenu/alias/{richMenuAliasId}", "取得 Rich Menu Alias"),
    RICH_MENU_ALIAS_LIST("https://api.line.me/v2/bot/richmenu/alias/list", "取得 Rich Menu Alias 列表"),

    /**
     * 訊息相關 API
     */
    MESSAGING_API_BASE("https://api.line.me", "LINE Messaging API 基礎 URL"),
    DATA_API_BASE("https://api-data.line.me", "LINE Data API 基礎 URL"),
    PUSH_MESSAGE("https://api.line.me/v2/bot/message/push", "推送訊息"),
    REPLY_MESSAGE("https://api.line.me/v2/bot/message/reply", "回覆訊息"),
    BROADCAST("https://api.line.me/v2/bot/message/broadcast", "廣播訊息"),

    /**
     * 用戶
     */
    GET_PROFILE("https://api.line.me/v2/bot/profile/{userId}", "取得用戶資料"),
    GET_GROUP_SUMMARY("https://api.line.me/v2/bot/group/{groupId}/summary", "取得群組摘要");
    /**
     * API 端點 URL
     */
    private final String url;

    /**
     * 描述
     */
    private final String description;

    /**
     * 取得完整的 URL（替換路徑參數）
     *
     * @param params 路徑參數（例如：richMenuId、richMenuAliasId、userId、groupId）
     * @return 完整的 URL
     */
    public String getUrl(String... params) {
        String result = this.url;
        boolean isOverSize = params != null && params.length > 0;
        if (isOverSize) {
            // 參數替換（依序替換各種參數）
            if (result.contains("{richMenuId}")) {
                result = result.replace("{richMenuId}", params[0]);
            }
            if (result.contains("{richMenuAliasId}")) {
                result = result.replace("{richMenuAliasId}", params[0]);
            }
            if (result.contains("{userId}")) {
                result = result.replace("{userId}", params[0]);
            }
            if (result.contains("{groupId}")) {
                result = result.replace("{groupId}", params[0]);
            }
        }
        return result;
    }

    /**
     * 取得 Rich Menu 圖片上傳 URL
     *
     * @param richMenuId Rich Menu ID
     * @return 完整的上傳 URL
     */
    public static String getRichMenuUploadUrl(String richMenuId) {
        return RICH_MENU_UPLOAD_IMAGE.getUrl(richMenuId);
    }

    /**
     * 取得 Rich Menu 圖片下載 URL
     *
     * @param richMenuId Rich Menu ID
     * @return 完整的下載 URL
     */
    public static String getRichMenuDownloadImageUrl(String richMenuId) {
        return RICH_MENU_DOWNLOAD_IMAGE.getUrl(richMenuId);
    }
}
